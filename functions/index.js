const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase)

exports.sendAlarmNotification = functions.database.ref('/{currentCoreDeviceUid}/state/triggered').onWrite(event => {
    const data = event.data;
    const coreUid = event.params.currentCoreDeviceUid

    console.log('Alarm state event triggered');
    if (!data.changed() || data.val() === false) {
        console.log('Initial conditions for launching the alarm were not met');
        return;
    }

    const alarmArmingStatePromise = admin.database().ref(`/${coreUid}/active`).once('value');

    return alarmArmingStatePromise.then(alarmArmingStateSnapshot => {

        if (alarmArmingStateSnapshot.val() === false) {
            return console.log('Alarm is not armed');
        }

        //notification definition
        const payload = {
            notification: {
                title: 'SmartAlarm',
                body: 'There is a possible break-in threat',
                sound: "default"
            }
        };

        const options = {
            priority: "high",
            timeToLive: 60 * 60 * 24 //24 hours
        };

        console.log('Alarm triggered!');
        return admin.messaging().sendToTopic(coreUid, payload, options).then(response => {
           // For each message check if there was an error.
           const tokensToRemove = [];
           response.results.forEach((result, index) => {
               const error = result.error;
               if (error) {
                  console.error('Failure sending notification to', tokens[index], error);
                  // Cleanup the tokens which are not registered anymore.
                  if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                      console.log("registration error occurred");
                  }
               }
           });
        });
    });
});


exports.sendCoreDevicePresenceNotification = functions.database.ref('/{currentCoreDeviceUid}/state/connected').onWrite(event => {
    const data = event.data;
    const coreUid = event.params.currentCoreDeviceUid

    console.log('Alarm presence event fired');
    if (!data.changed()) {
        console.log('Data not changed, no notification will be sent');
        return;
    }


    var notification;

    if(data.val() === false) {
            notification = {
                notification: {
                    title: 'SmartAlarm',
                    body: 'Your core device has been disconnected. This may be due to the Internet connection or power shortage.',
                    sound: "default"
                }
            };
    } else {
            notification = {
                notification: {
                    title: 'SmartAlarm',
                    body: 'Your core device has come back online.',
                    sound: "default"
                }
            };
    }

    const options = {
                priority: "high",
                timeToLive: 60 * 60 * 24 //24 hours
            };

            console.log('Sending presence notification!');
            return admin.messaging().sendToTopic(coreUid, notification, options).then(response => {
               // For each message check if there was an error.
               const tokensToRemove = [];
               response.results.forEach((result, index) => {
                   const error = result.error;
                   if (error) {
                      console.error('Failure sending notification to', tokens[index], error);
                      // Cleanup the tokens which are not registered anymore.
                      if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                          console.log("registration error occurred");
                      }
                   }
               });
            });
});



'use strict';

const mkdirp = require('mkdirp-promise');
// Include a Service Account Key to use a Signed URL
const gcs = require('@google-cloud/storage')({keyFilename: 'service-account-credentials.json'});
const spawn = require('child-process-promise').spawn;
const path = require('path');
const os = require('os');
const fs = require('fs');

const THUMB_MAX_HEIGHT = 256;
const THUMB_MAX_WIDTH = 256;
const THUMB_PREFIX = 'thumb_';

exports.generateThumbnail = functions.storage.object().onChange(event => {
  // File and directory paths.
  const filePath = event.data.name;
  const fileDir = path.dirname(filePath);
  const fileName = path.basename(filePath);
  const thumbFilePath = path.normalize(path.join(fileDir, `${THUMB_PREFIX}${fileName}`));
  const tempLocalFile = path.join(os.tmpdir(), filePath);
  const tempLocalDir = path.dirname(tempLocalFile);
  const tempLocalThumbFile = path.join(os.tmpdir(), thumbFilePath);

  const pathParts = fileDir.split('/')

  const coreDeviceUid = pathParts[pathParts.length - 1]
  const incidentUid = fileName.slice(0, -4);

  // Exit if this is triggered on a file that is not an image.
  if (!event.data.contentType.startsWith('image/')) {
    console.log('This is not an image.');
    return;
  }

  // Exit is this isn't a new file, when only metadata changed
  if (event.data.resourceState === 'exists' && event.data.metageneration > 1) {
    console.log('This is a metadata change event.');
    return;
  }

  // Exit if the image is already a thumbnail.
  if (fileName.startsWith(THUMB_PREFIX)) {
    console.log('Already a Thumbnail.');
    return;
  }

  // Exit if this is a move or deletion event.
  if (event.data.resourceState === 'not_exists') {
    console.log('This is a deletion event.');
    return;
  }

  // Cloud Storage files.
  const bucket = gcs.bucket(event.data.bucket);
  const file = bucket.file(filePath);
  const thumbFile = bucket.file(thumbFilePath);

  // Create the temp directory where the storage file will be downloaded.
  return mkdirp(tempLocalDir).then(() => {
    // Download file from bucket.
    return file.download({destination: tempLocalFile});
  }).then(() => {
    console.log('The file has been downloaded to', tempLocalFile);
    // Generate a thumbnail using ImageMagick.
    return spawn('convert', [tempLocalFile, '-thumbnail', `${THUMB_MAX_WIDTH}x${THUMB_MAX_HEIGHT}>`, tempLocalThumbFile]);
  }).then(() => {
    console.log('Thumbnail created at', tempLocalThumbFile);
    // Uploading the Thumbnail.
    return bucket.upload(tempLocalThumbFile, {destination: thumbFilePath});
  }).then(() => {
    console.log('Thumbnail uploaded to Storage at', thumbFilePath);
    // Once the image has been uploaded delete the local files to free up disk space.
    fs.unlink(tempLocalFile, function () { console.log('done deleting tempLocalFile'); });
    fs.unlink(tempLocalThumbFile, function () { console.log('Done deleting tempLocalThumbFile'); });
    // Get the Signed URLs for the thumbnail and original image.
    const config = {
      action: 'read',
      expires: '03-01-2500'
    };
    return Promise.all([
      thumbFile.getSignedUrl(config),
      file.getSignedUrl(config)
    ]);
  }).then(results => {
    console.log('Got Signed URLs.');
    const thumbResult = results[0];
    const originalResult = results[1];
    const thumbFileUrl = thumbResult[0];
    const fileUrl = originalResult[0];

    return admin.database().ref(`/${coreDeviceUid}/incidents/${incidentUid}/thumbnailUrl`).set(thumbFileUrl);
  }).then(() => console.log('Thumbnail URLs saved to database.'));
});
