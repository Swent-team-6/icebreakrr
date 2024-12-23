// ONLY FOR DOCUMENTATION, CODE NOT USED (it is server side in the cloud function)

const functions = require('firebase-functions/v2');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendMeetingRequest = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    // Access the data object within the request body
    const { targetToken, senderUID, senderName, message1, message2, location} = request.body.data;
    // Validate input
    if (!targetToken || !message1 || !senderUID || !location) {
        console.error('Missing data');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'MEETING REQUEST',
            senderUID: senderUID,
            senderName: senderName,
            message1: message1,
            message2: message2,
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

exports.sendMeetingResponse = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    // Access the data object within the request body
    const { targetToken, senderToken, senderUID, senderName, message, accepted, location} = request.body.data;
    // Validate input
    if (!targetToken || !senderToken || !message || !senderUID || !senderName || !accepted || !location) {
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
            accepted: accepted,
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

exports.sendMeetingCancellation = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    // Access the data object within the request body
    const { targetToken, senderUID, senderName, message} = request.body.data;
    // Validate input
    if (!targetToken || !senderUID || !senderName || !message) {
        console.error('Missing targetToken or body or senderUid');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'MEETING CANCELLATION',
            senderUID: senderUID,
            senderName: senderName,
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

exports.sendEngagementNotification = functions.https.onRequest(async (request, response) => {
    console.log('Received Request Body:', request.body);
    // Access the data object within the request body
    const { targetToken,senderUID, senderName, message} = request.body.data;
    // Validate input
    if (!targetToken || !senderUID || !senderName || !message) {
        console.error('Missing targetToken or body or senderUid');
        response.status(400).json({ data: { error: 'Token or message or senderUid' } }); // Wrap error in data
        return;
    }

    const payload = {
        data: {
            title: 'ENGAGEMENT NOTIFICATION',
            senderUID: senderUID, // Not used
            senderName: senderName,
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