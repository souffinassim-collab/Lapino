import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { getSetting } from '../database/db';

// Configure notification handler
Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
    }),
});

export async function registerForPushNotificationsAsync() {
    let token;

    if (Platform.OS === 'android') {
        await Notifications.setNotificationChannelAsync('daily-check', {
            name: 'Daily Check',
            importance: Notifications.AndroidImportance.MAX,
            vibrationPattern: [0, 250, 250, 250],
            lightColor: '#FF231F7C',
        });
    }

    if (Platform.OS === 'web') {
        console.log("Notifications not supported on web");
        return;
    }

    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== 'granted') {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
    }

    if (finalStatus !== 'granted') {
        console.log('Failed to get push token for push notification!');
        return;
    }
}

export async function scheduleDailyReminder() {
    if (Platform.OS === 'web') return;

    // Get time from settings or default to 09:00
    const timeStr = await getSetting('daily_time');
    let hour = 9;
    let minute = 0;

    if (timeStr) {
        const parts = timeStr.split(':');
        if (parts.length === 2) {
            hour = parseInt(parts[0], 10);
            minute = parseInt(parts[1], 10);
        }
    }

    // Cancel existing to reschedule
    await Notifications.cancelAllScheduledNotificationsAsync();

    await Notifications.scheduleNotificationAsync({
        content: {
            title: "Lapinos a soif ! 💧",
            body: "N'oubliez pas de vérifier l'eau et l'aliment de vos lapins aujourd'hui.",
            data: { type: 'daily_check' },
        },
        trigger: {
            hour: hour,
            minute: minute,
            repeats: true,
        },
    });
}

export async function cancelDailyReminder() {
    // Optional: if we wanted to cancel it
    await Notifications.cancelAllScheduledNotificationsAsync();
}
