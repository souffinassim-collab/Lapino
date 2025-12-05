import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';
import { formatDateFR, getVaccinationStatus, getStatusColor } from '../utils/dateUtils';

const VaccineAlert = ({ item, onPress }) => {
    const { colors } = useTheme();
    const status = getVaccinationStatus(item.date_prochain);
    const statusColor = getStatusColor(status);

    return (
        <TouchableOpacity
            style={[styles.container, { backgroundColor: colors.surface }]}
            onPress={onPress}
        >
            <View style={[styles.badge, { backgroundColor: statusColor }]} />
            <View style={styles.content}>
                <Text style={[styles.title, { color: colors.text }]}>
                    {item.femelle_numero} - {item.vaccin_nom}
                </Text>
                <Text style={[styles.date, { color: colors.textSecondary }]}>
                    À faire le: {formatDateFR(item.date_prochain)}
                </Text>
            </View>
        </TouchableOpacity>
    );
};

const styles = StyleSheet.create({
    container: {
        flexDirection: 'row',
        borderRadius: 8,
        padding: 12,
        marginVertical: 4,
        marginHorizontal: 16,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    badge: {
        width: 8,
        borderRadius: 4,
        marginRight: 12,
    },
    content: {
        flex: 1,
    },
    title: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 4,
    },
    date: {
        fontSize: 14,
    },
});

export default VaccineAlert;
