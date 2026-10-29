import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    RefreshControl,
    TouchableOpacity
} from 'react-native';
import { useTheme, useFocusEffect } from '@react-navigation/native';
import { Card, Chip, Button } from 'react-native-paper';
import StatCard from '../components/StatCard';
import VaccineAlert from '../components/VaccineAlert';
import RabbitStatusCard from '../components/RabbitStatusCard';
import ReproductionModals from '../components/ReproductionModals';
import {
    getStatistics,
    getVaccinsEnRetard,
    getVaccinsBientot,
    getAllAliments,
    getFemellesWithStatus,
    getReproductionAlerts
} from '../database/db';

const DashboardScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [stats, setStats] = useState({});
    const [reproAlerts, setReproAlerts] = useState({ palpations: [], boitesNid: [], misesBas: [], sevrages: [] });
    const [vaccinsEnRetard, setVaccinsEnRetard] = useState([]);
    const [vaccinsBientot, setVaccinsBientot] = useState([]);
    const [alimentsBas, setAlimentsBas] = useState([]);
    const [femelles, setFemelles] = useState([]);
    const [refreshing, setRefreshing] = useState(false);

    // Modal reproduction unifié
    const [activeModal, setActiveModal] = useState(null);
    const [selectedFemelle, setSelectedFemelle] = useState(null);

    const loadData = async () => {
        try {
            const [statistics, alerts, vRetard, vBientot, allAliments, femellesData] = await Promise.all([
                getStatistics(),
                getReproductionAlerts(),
                getVaccinsEnRetard(),
                getVaccinsBientot(),
                getAllAliments(),
                getFemellesWithStatus()
            ]);

            setStats(statistics);
            setReproAlerts(alerts);
            setVaccinsEnRetard(vRetard);
            setVaccinsBientot(vBientot);
            setAlimentsBas(allAliments.filter(a => a.jours_restants <= 7));
            setFemelles(femellesData);
        } catch (error) {
            console.error('Erreur chargement dashboard:', error);
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

    const handleAction = (femelle, actionType) => {
        setSelectedFemelle(femelle);
        setActiveModal(actionType);
    };

    const handleCardPress = (femelle) => {
        navigation.navigate('FemelleDetail', { femelleId: femelle.id });
    };

    const totalCuniAlerts =
        reproAlerts.palpations.length +
        reproAlerts.boitesNid.length +
        reproAlerts.misesBas.length +
        reproAlerts.sevrages.length;

    return (
        <View style={{ flex: 1, backgroundColor: colors.background }}>
            <ScrollView
                style={styles.container}
                contentContainerStyle={styles.contentContainer}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
            >
                {/* 1. RÉSUMÉ DU CHEPTEL & REPRODUCTION */}
                <View style={styles.statsRow}>
                    <StatCard
                        title="Lapines Total"
                        value={stats.totalFemelles || 0}
                        icon="rabbit"
                        color={colors.primary}
                    />
                    <StatCard
                        title="Lapereaux au nid"
                        value={stats.lapereauxSousMere || 0}
                        icon="baby-bottle"
                        color="#4CAF50"
                    />
                </View>

                {/* Micro-indicateurs d'état de reproduction */}
                <View style={styles.statusChipsRow}>
                    <Chip icon="sleep" style={styles.miniChip}>Repos: {stats.enRepos || 0}</Chip>
                    <Chip icon="heart" style={[styles.miniChip, { backgroundColor: '#FCE4EC' }]}>Saillie: {stats.enSaillie || 0}</Chip>
                    <Chip icon="rabbit" style={[styles.miniChip, { backgroundColor: '#E3F2FD' }]}>Gestante: {stats.gestantes || 0}</Chip>
                    <Chip icon="baby-bottle" style={[styles.miniChip, { backgroundColor: '#E8F5E9' }]}>Allaitante: {stats.allaitantes || 0}</Chip>
                </View>

                {/* 2. ALERTES CUNICULICOLES PRIORITAIRES */}
                {totalCuniAlerts > 0 && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: '#E65100' }]}>
                            🚨 Actions Élevage Prioritaires ({totalCuniAlerts})
                        </Text>

                        {/* Palpations à faire */}
                        {reproAlerts.palpations.map((item, idx) => (
                            <TouchableOpacity
                                key={`palp-${idx}`}
                                style={[styles.alertCard, styles.alertPalpation]}
                                onPress={() => handleAction(item.femelle, 'verify')}
                            >
                                <View style={styles.alertHeader}>
                                    <Text style={styles.alertCardTitle}>🩺 {item.femelle.numero} • Palpation à faire !</Text>
                                    <Chip compact style={{ backgroundColor: '#FFE0B2' }}>J{item.daysSinceSaillie}</Chip>
                                </View>
                                <Text style={styles.alertCardSub}>
                                    Saillie le {item.cycle.date_saillie} {item.cycle.male_numero ? `avec ${item.cycle.male_numero}` : ''}. Diagnostic de gestation à réaliser.
                                </Text>
                            </TouchableOpacity>
                        ))}

                        {/* Boîtes à nid à poser */}
                        {reproAlerts.boitesNid.map((item, idx) => (
                            <TouchableOpacity
                                key={`nest-${idx}`}
                                style={[styles.alertCard, styles.alertNest]}
                                onPress={() => handleAction(item.femelle, 'nest')}
                            >
                                <View style={styles.alertHeader}>
                                    <Text style={[styles.alertCardTitle, { color: '#B71C1C' }]}>
                                        📦 {item.femelle.numero} • Poser la boîte à nid !
                                    </Text>
                                    <Chip compact style={{ backgroundColor: '#FFCDD2' }}>Urgent J28</Chip>
                                </View>
                                <Text style={styles.alertCardSub}>
                                    Mise bas imminente ! Garnir le nid de paille propre pour éviter la perte des petits sur le grillage.
                                </Text>
                            </TouchableOpacity>
                        ))}

                        {/* Mises bas imminentes */}
                        {reproAlerts.misesBas.map((item, idx) => (
                            <TouchableOpacity
                                key={`birth-${idx}`}
                                style={[styles.alertCard, styles.alertBirth]}
                                onPress={() => handleAction(item.femelle, 'birth')}
                            >
                                <View style={styles.alertHeader}>
                                    <Text style={styles.alertCardTitle}>🐰 {item.femelle.numero} • {item.message}</Text>
                                    <Chip compact style={{ backgroundColor: '#E1BEE7' }}>Naissance</Chip>
                                </View>
                                <Text style={styles.alertCardSub}>
                                    Surveiller le nid et déclarer la naissance dès que les petits sont arrivés.
                                </Text>
                            </TouchableOpacity>
                        ))}

                        {/* Sevrages à réaliser */}
                        {reproAlerts.sevrages.map((item, idx) => (
                            <TouchableOpacity
                                key={`wean-${idx}`}
                                style={[styles.alertCard, styles.alertWean]}
                                onPress={() => handleAction(item.femelle, 'wean')}
                            >
                                <View style={styles.alertHeader}>
                                    <Text style={styles.alertCardTitle}>🧺 {item.femelle.numero} • {item.message}</Text>
                                    <Chip compact style={{ backgroundColor: '#C8E6C9' }}>Sevrage</Chip>
                                </View>
                                <Text style={styles.alertCardSub}>
                                    Les lapereaux ont l'âge d'être sevrés et transférés en engraissement.
                                </Text>
                            </TouchableOpacity>
                        ))}
                    </View>
                )}

                {/* 3. ALERTES STOCK ALIMENTATION */}
                {alimentsBas.length > 0 && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: colors.error }]}>
                            ⚠️ Aliments Stock Bas ({alimentsBas.length})
                        </Text>
                        {alimentsBas.map((item) => (
                            <TouchableOpacity
                                key={item.id}
                                style={[
                                    styles.alertCard,
                                    {
                                        backgroundColor: colors.surface,
                                        borderLeftColor: item.jours_restants < 3 ? colors.error : colors.warning
                                    }
                                ]}
                                onPress={() => navigation.navigate('AlimentsList')}
                            >
                                <Text style={[styles.alertText, { color: colors.text }]}>{item.nom}: Reste {item.jours_restants} jours</Text>
                                <Text style={[styles.alertSubtext, { color: colors.textSecondary }]}>{item.stock_kg} kg en stock</Text>
                            </TouchableOpacity>
                        ))}
                    </View>
                )}

                {/* 4. ALERTES VACCINS */}
                {(vaccinsEnRetard.length > 0 || vaccinsBientot.length > 0) && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: colors.error }]}>
                            💉 Vaccinations ({vaccinsEnRetard.length + vaccinsBientot.length})
                        </Text>
                        {vaccinsEnRetard.map((item) => (
                            <VaccineAlert key={item.id} item={item} onPress={() => navigation.navigate('FemelleDetail', { femelleId: item.femelle_id })} />
                        ))}
                        {vaccinsBientot.map((item) => (
                            <VaccineAlert key={item.id} item={item} onPress={() => navigation.navigate('FemelleDetail', { femelleId: item.femelle_id })} />
                        ))}
                    </View>
                )}

                {/* 5. GESTION DU CHEPTEL (MES LAPINES) */}
                <View style={styles.section}>
                    <View style={styles.sectionHeaderRow}>
                        <Text style={[styles.sectionTitle, { color: colors.text }]}>
                            Mes Lapines ({femelles.length})
                        </Text>
                        <Button
                            mode="text"
                            compact
                            icon="plus"
                            onPress={() => navigation.navigate('FemelleAddEdit')}
                        >
                            Ajouter
                        </Button>
                    </View>

                    {femelles.map((femelle) => (
                        <RabbitStatusCard
                            key={femelle.id}
                            femelle={femelle}
                            onAction={handleAction}
                            onPressCard={handleCardPress}
                        />
                    ))}

                    {femelles.length === 0 && (
                        <View style={styles.emptyState}>
                            <Text style={{ color: colors.disabled, textAlign: 'center' }}>
                                Aucune lapine enregistrée. Cliquez sur "Ajouter" pour créer votre cheptel reproducteur.
                            </Text>
                        </View>
                    )}
                </View>
            </ScrollView>

            {/* MODALS REPRODUCTION CUNICULICOLE */}
            <ReproductionModals
                activeModal={activeModal}
                femelle={selectedFemelle}
                onClose={() => {
                    setActiveModal(null);
                    setSelectedFemelle(null);
                }}
                onSuccess={loadData}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    container: { flex: 1 },
    contentContainer: { padding: 16, paddingBottom: 60 },
    statsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 10,
    },
    statusChipsRow: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 6,
        marginBottom: 14,
    },
    miniChip: {
        height: 32,
    },
    section: {
        marginBottom: 16,
    },
    sectionTitle: {
        fontSize: 17,
        fontWeight: 'bold',
        marginBottom: 10,
    },
    sectionHeaderRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 8,
    },
    alertCard: {
        padding: 12,
        borderRadius: 8,
        borderLeftWidth: 4,
        marginBottom: 8,
        backgroundColor: '#FFFFFF',
        elevation: 1,
    },
    alertPalpation: {
        borderLeftColor: '#F57C00',
        backgroundColor: '#FFF8E1',
    },
    alertNest: {
        borderLeftColor: '#D32F2F',
        backgroundColor: '#FFEBEE',
    },
    alertBirth: {
        borderLeftColor: '#8E24AA',
        backgroundColor: '#F3E5F5',
    },
    alertWean: {
        borderLeftColor: '#388E3C',
        backgroundColor: '#E8F5E9',
    },
    alertHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    alertCardTitle: {
        fontSize: 14.5,
        fontWeight: 'bold',
        flex: 1,
    },
    alertCardSub: {
        fontSize: 12.5,
        color: '#555',
        marginTop: 4,
    },
    alertText: { fontSize: 15, fontWeight: 'bold' },
    alertSubtext: { fontSize: 13, marginTop: 2 },
    emptyState: {
        alignItems: 'center',
        justifyContent: 'center',
        padding: 30,
        backgroundColor: '#FAFAFA',
        borderRadius: 8,
    },
});

export default DashboardScreen;
