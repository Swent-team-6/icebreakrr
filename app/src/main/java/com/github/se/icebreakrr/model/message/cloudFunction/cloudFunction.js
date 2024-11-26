/**
Those functions are used as in the Firebase Cloud functions we have set up in our Firebase (the one here are just for documentation)
*/

const functions = require('firebase-functions/v2');
const admin = require('firebase-admin');

admin.initializeApp();

// The function used to send the meeting request package
exports.sendMeetingRequest = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    const { targetToken, senderUID, message} = request.body.data;
    if (!targetToken || !message || !senderUID) {
        console.error('Missing targetToken or body or senderUid');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'MEETING REQUEST',
            senderUID: senderUID,
            message: message,
        }
    };

    try {
        await admin.messaging().send({
            token: targetToken,
            data: payload.data
        });
        response.status(200).json({ data: { message: 'Message sent successfully!' } }); // Wrap success message in data
    } catch (error) {
        console.error('Error sending message:', error.message);
        response.status(500).json({ data: { error: `Error sending message: ${error.message}` } }); // Wrap error in data
    }
});

// The function used to send the meeting response package
exports.sendMeetingResponse = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    const { targetToken, senderToken, senderUID, senderName, message, accepted} = request.body.data;
    if (!targetToken || !senderToken || !message || !senderUID || !senderName || !accepted) {
        console.error('Missing targetToken or body or senderUid');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'MEETING RESPONSE',
            senderUID: senderUID,
            senderToken: senderToken,
            senderName: senderName,
            message: message,
            accepted: accepted
        }
    };

    try {
        await admin.messaging().send({
            token: targetToken,
            data: payload.data
        });
        response.status(200).json({ data: { message: 'Message sent successfully!' } }); // Wrap success message in data
    } catch (error) {
        console.error('Error sending message:', error.message);
        response.status(500).json({ data: { error: `Error sending message: ${error.message}` } }); // Wrap error in data
    }
});

// The function used to send the meeting confirmation package
exports.sendMeetingConfirmation = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    const { targetToken, senderUID, senderName, message, location} = request.body.data;
    if (!targetToken || !senderUID || !senderName || !message || !location) {
        console.error('Missing targetToken or body or senderUid');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'MEETING CONFIRMATION',
            senderUID: senderUID,
            senderName: senderName,
            message: message,
            location: location
        }
    };

    try {
        await admin.messaging().send({
            token: targetToken,
            data: payload.data
        });
        response.status(200).json({ data: { message: 'Message sent successfully!' } }); // Wrap success message in data
    } catch (error) {
        console.error('Error sending message:', error.message);
        response.status(500).json({ data: { error: `Error sending message: ${error.message}` } }); // Wrap error in data
    }
});