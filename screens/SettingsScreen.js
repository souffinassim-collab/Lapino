import React, { useState, useEffect } from 'react';
import { View, StyleSheet, Platform, TouchableOpacity } from 'react-native';
import { List, Divider, Text, Button, Portal, Modal } from 'react-native-paper';
import DateTimePicker from '@react-native-community/datetimepicker';
import { getSetting, saveSetting } from '../database/db';
import { scheduleDailyReminder } from '../utils/notifications';

const SettingsScreen = ({ navigation }) => {
    const [notificationTime, setNotificationTime] = useState(new Date());
    const [showPicker, setShowPicker] = useState(false);

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        const timeStr = await getSetting('daily_time');
        if (timeStr) {
            const [h, m] = timeStr.split(':');
            const d = new Date();
            d.setHours(parseInt(h), parseInt(m));
            setNotificationTime(d);
        } else {
            const d = new Date();
            d.setHours(9, 0);
            setNotificationTime(d);
        }
    };

    const handleTimeChange = async (event, selectedDate) => {
        setShowPicker(Platform.OS === 'ios');
        if (selectedDate) {
            setNotificationTime(selectedDate);
            const timeStr = `${selectedDate.getHours()}:${selectedDate.getMinutes().toString().padStart(2, '0')}`;
            await saveSetting('daily_time', timeStr);
            console.log('Saved time:', timeStr);
            await scheduleDailyReminder(); // Reschedule with new time
        }
    };

    return (
        <View style={styles.container}>
            <List.Section>
                <List.Subheader>Notifications</List.Subheader>
                <List.Item
                    title="Heure de l'alerte quotidienne"
                    description={`Actuellement : ${notificationTime.getHours()}h${notificationTime.getMinutes().toString().padStart(2, '0')}`}
                    left={props => <List.Icon {...props} icon="clock-outline" />}
                    right={() => (
                        Platform.OS === 'web' ? null :
                            <Button onPress={() => setShowPicker(true)}>Modifier</Button>
                    )}
                    onPress={() => setShowPicker(true)}
                    style={styles.item}
                />
                {showPicker && (
                    <DateTimePicker
                        testID="dateTimePicker"
                        value={notificationTime}
                        mode="time"
                        is24Hour={true}
                        display="default"
                        onChange={handleTimeChange}
                    />
                )}
            </List.Section>
            <Divider />

            <List.Section>
                <List.Subheader>Gestion de l'élevage</List.Subheader>

                <List.Item
                    title="Gérer les Clapets"
                    description="Ajouter ou supprimer des cages"
                    left={props => <List.Icon {...props} icon="home-outline" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('ClapetsList')}
                    style={styles.item}
                />
                <Divider />

                <List.Item
                    title="Gérer les Vaccins"
                    description="Types de vaccins et durées"
                    left={props => <List.Icon {...props} icon="needle" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('VaccinsList')}
                    style={styles.item}
                />
                <Divider />

                <List.Item
                    title="Gérer l'Alimentation"
                    description="Stocks et consommation"
                    left={props => <List.Icon {...props} icon="food-apple" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('AlimentsList')}
                    style={styles.item}
                />
            </List.Section>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    item: {
        backgroundColor: 'white',
    }
});

export default SettingsScreen;
