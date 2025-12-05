import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TouchableOpacity,
    RefreshControl
} from 'react-native';
import { useTheme, useFocusEffect } from '@react-navigation/native';
import { getAllFemelles } from '../../database/db';
import { formatDateFR, getVaccinationStatus, getStatusColor } from '../../utils/dateUtils';

const FemellesListScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [femelles, setFemelles] = useState([]);
    const [refreshing, setRefreshing] = useState(false);

    const loadFemelles = async () => {
        try {
            const data = await getAllFemelles();
            setFemelles(data);
        } catch (error) {
            console.error('Erreur chargement femelles:', error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadFemelles();
        }, [])
    );

    const onRefresh = async () => {
        setRefreshing(true);
        await loadFemelles();
        setRefreshing(false);
    };

    const renderFemelle = ({ item }) => {
        const status = getVaccinationStatus(item.prochain_vaccin);
        const statusColor = getStatusColor(status);

        return (
            <TouchableOpacity
                style={[styles.item, { backgroundColor: colors.surface }]}
                onPress={() => navigation.navigate('FemelleDetail', {
                    femelleId: item.id
                })}
            >
                <View style={styles.row}>
                    <View style={styles.mainInfo}>
                        <Text style={[styles.numero, { color: colors.text }]}>
                            🐰 {item.numero}
                        </Text>
                        <Text style={[styles.detail, { color: colors.textSecondary }]}>
                            Clapet: {item.clapet_numero || 'Aucun'}
                        </Text>
                    </View>

                    <View style={styles.vaccineInfo}>
                        <Text style={[styles.detail, { color: colors.textSecondary }]}>
                            Dernier: {formatDateFR(item.dernier_vaccin)}
                        </Text>
                        <View style={styles.statusRow}>
                            <View style={[styles.statusDot, { backgroundColor: statusColor }]} />
                            <Text style={[styles.detail, { color: colors.textSecondary }]}>
                                Prochain: {formatDateFR(item.prochain_vaccin)}
                            </Text>
                        </View>
                    </View>
                </View>
            </TouchableOpacity>
        );
    };

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <FlatList
                data={femelles}
                renderItem={renderFemelle}
                keyExtractor={(item) => item.id.toString()}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyState}>
                        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                            Aucune femelle enregistrée
                        </Text>
                    </View>
                }
            />

            <TouchableOpacity
                style={[styles.fab, { backgroundColor: colors.primary }]}
                onPress={() => navigation.navigate('FemelleAddEdit')}
            >
                <Text style={styles.fabText}>+</Text>
            </TouchableOpacity>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    item: {
        marginHorizontal: 16,
        marginVertical: 6,
        padding: 16,
        borderRadius: 8,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    mainInfo: {
        flex: 1,
    },
    numero: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    detail: {
        fontSize: 14,
        marginTop: 2,
    },
    vaccineInfo: {
        alignItems: 'flex-end',
    },
    statusRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 2,
    },
    statusDot: {
        width: 8,
        height: 8,
        borderRadius: 4,
        marginRight: 6,
    },
    emptyState: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 16,
    },
    fab: {
        position: 'absolute',
        right: 20,
        bottom: 20,
        width: 60,
        height: 60,
        borderRadius: 30,
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 6,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
    },
    fabText: {
        fontSize: 32,
        color: '#FFFFFF',
        fontWeight: 'bold',
    },
});

export default FemellesListScreen;
