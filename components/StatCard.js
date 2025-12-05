import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';

const StatCard = ({ icon, title, value, onPress }) => {
    const { colors } = useTheme();

    return (
        <TouchableOpacity
            style={[styles.card, { backgroundColor: colors.surface }]}
            onPress={onPress}
            disabled={!onPress}
        >
            <Text style={styles.icon}>{icon}</Text>
            <Text style={[styles.title, { color: colors.textSecondary }]}>{title}</Text>
            <Text style={[styles.value, { color: colors.text }]}>{value}</Text>
        </TouchableOpacity>
    );
};

const styles = StyleSheet.create({
    card: {
        flex: 1,
        borderRadius: 12,
        padding: 20,
        margin: 8,
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 120,
        elevation: 3,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    icon: {
        fontSize: 40,
        marginBottom: 8,
    },
    title: {
        fontSize: 14,
        marginBottom: 8,
        textAlign: 'center',
    },
    value: {
        fontSize: 28,
        fontWeight: 'bold',
    },
});

export default StatCard;
