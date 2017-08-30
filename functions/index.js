const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase)

exports.sendAlarmNotification = functions.database.ref('/trigger').onWrite(event => {
    const data = event.data;
    console.log('Alarm state event triggered');
    if (!data.changed() || data.val() === false) {
        console.log('Initial conditions for launching the alarm were not met');
        return;
    }

    const alarmArmingStatePromise = admin.database().ref('/active').once('value');

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
        return admin.messaging().sendToTopic("Alarm_Notifications", payload, options).then(response => {
           // For each message check if there was an error.
           const tokensToRemove = [];
           response.results.forEach((result, index) => {
               const error = result.error;
               if (error) {
                  console.error('Failure sending notification to', tokens[index], error);
                  // Cleanup the tokens who are not registered anymore.
                  if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                      console.log("registration error occurred");
                  }
               }
           });
        });
    });
});