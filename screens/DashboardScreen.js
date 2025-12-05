import React, { useState, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    RefreshControl,
    TouchableOpacity
} from 'react-native';
import { useTheme, useFocusEffect } from '@react-navigation/native';
import StatCard from '../components/StatCard';
import VaccineAlert from '../components/VaccineAlert';
import {
    getStatistics,
    getVaccinsEnRetard,
    getVaccinsBientot
} from '../database/db';

const DashboardScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [stats, setStats] = useState({
        totalFemelles: 0,
        clapetsRemplis: 0,
        clapetsVides: 0
    });
    const [vaccinsEnRetard, setVaccinsEnRetard] = useState([]);
    const [vaccinsBientot, setVaccinsBientot] = useState([]);
    const [refreshing, setRefreshing] = useState(false);

    const loadData = async () => {
        try {
            const statistics = await getStatistics();
            setStats(statistics);

            const retard = await getVaccinsEnRetard();
            setVaccinsEnRetard(retard);

            const bientot = await getVaccinsBientot();
            setVaccinsBientot(bientot);
        } catch (error) {
            console.error('Erreur chargement données dashboard:', error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadData();
        }, [])
    );

    const onRefresh = async () => {
        setRefreshing(true);
        await loadData();
        setRefreshing(false);
    };

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            refreshControl={
                <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
            }
        >
            <View style={styles.header}>
                <Text style={[styles.title, { color: colors.text }]}>
                    🐰 Gestion Lapins
                </Text>
            </View>

            <View style={styles.statsRow}>
                <StatCard
                    icon="🐰"
                    title="Femelles"
                    value={stats.totalFemelles}
                    onPress={() => navigation.navigate('FemellesList')}
                />
                <StatCard
                    icon="🏠"
                    title="Clapets Remplis"
                    value={stats.clapetsRemplis}
                    onPress={() => navigation.navigate('ClapetsList')}
                />
            </View>

            <View style={styles.statsRow}>
                <StatCard
                    icon="📦"
                    title="Clapets Vides"
                    value={stats.clapetsVides}
                />
                <StatCard
                    icon="💉"
                    title="Vaccins"
                    value={vaccinsEnRetard.length + vaccinsBientot.length}
                    onPress={() => navigation.navigate('VaccinsList')}
                />
            </View>

            {/* Vaccins en retard */}
            {vaccinsEnRetard.length > 0 && (
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, { color: colors.error }]}>
                        🔴 Vaccins en Retard ({vaccinsEnRetard.length})
                    </Text>
                    {vaccinsEnRetard.map((item) => (
                        <VaccineAlert
                            key={item.id}
                            item={item}
                            onPress={() => navigation.navigate('FemelleDetail', {
                                femelleId: item.femelle_id
                            })}
                        />
                    ))}
                </View>
            )}

            {/* Vaccins à faire bientôt */}
            {vaccinsBientot.length > 0 && (
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, { color: colors.warning }]}>
                        🟠 Vaccins à Faire Bientôt ({vaccinsBientot.length})
                    </Text>
                    {vaccinsBientot.map((item) => (
                        <VaccineAlert
                            key={item.id}
                            item={item}
                            onPress={() => navigation.navigate('FemelleDetail', {
                                femelleId: item.femelle_id
                            })}
                        />
                    ))}
                </View>
            )}

            {/* Message si tout est OK */}
            {vaccinsEnRetard.length === 0 && vaccinsBientot.length === 0 && (
                <View style={styles.emptyState}>
                    <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                        ✅ Tous les vaccins sont à jour !
                    </Text>
                </View>
            )}
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    header: {
        padding: 20,
        paddingTop: 10,
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
    },
    statsRow: {
        flexDirection: 'row',
        paddingHorizontal: 8,
    },
    section: {
        marginTop: 20,
        marginBottom: 16,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        marginHorizontal: 16,
        marginBottom: 12,
    },
    emptyState: {
        alignItems: 'center',
        justifyContent: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 16,
        textAlign: 'center',
    },
});

export default DashboardScreen;
