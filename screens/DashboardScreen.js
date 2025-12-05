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
import RabbitStatusCard from '../components/RabbitStatusCard';
import DateInput from '../components/DateInput';
import {
    getStatistics,
    getVaccinsEnRetard,
    getVaccinsBientot,
    getAllAliments,
    getFemellesWithStatus,
    addCycle,
    confirmBirth,
    stopCycle,
    verifyGestation,
    getDailyCheckStatus,
    performDailyCheck
} from '../database/db';
import { Portal, Modal, Title, TextInput, Button, HelperText, Text as PaperText } from 'react-native-paper';

const DashboardScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [stats, setStats] = useState({});
    const [vaccinsEnRetard, setVaccinsEnRetard] = useState([]);
    const [vaccinsBientot, setVaccinsBientot] = useState([]);
    const [alimentsBas, setAlimentsBas] = useState([]);
    const [femelles, setFemelles] = useState([]);
    const [dailyCheckDone, setDailyCheckDone] = useState(true); // Default true to avoid flash
    const [refreshing, setRefreshing] = useState(false);

    // -- Modal States (identiques à ListScreen pour gérer les actions depuis le Dashboard) --
    const [modalDatesVisible, setModalDatesVisible] = useState(false);
    const [modalBirthVisible, setModalBirthVisible] = useState(false);
    const [modalFailVisible, setModalFailVisible] = useState(false);
    const [modalVerifyVisible, setModalVerifyVisible] = useState(false); // Verification gestation
    const [selectedFemelle, setSelectedFemelle] = useState(null);
    const [dateInput, setDateInput] = useState(new Date().toISOString().split('T')[0]);
    const [vivantsInput, setVivantsInput] = useState('');
    const [mortsInput, setMortsInput] = useState('0');

    const loadData = async () => {
        try {
            const statistics = await getStatistics();
            setStats(statistics);
            setVaccinsEnRetard(await getVaccinsEnRetard());
            setVaccinsBientot(await getVaccinsBientot());

            // Daily Check
            const today = new Date().toISOString().split('T')[0];
            const check = await getDailyCheckStatus(today);
            setDailyCheckDone(!!check);

            const allAliments = await getAllAliments();
            setAlimentsBas(allAliments.filter(a => a.jours_restants <= 7));

            const femellesData = await getFemellesWithStatus();
            setFemelles(femellesData);

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

    // -- Handlers d'actions (Duplication de logique car le user veut tout sur le dashboard) --
    const handleAction = (femelle, actionType) => {
        setSelectedFemelle(femelle);
        setDateInput(new Date().toISOString().split('T')[0]);

        if (actionType === 'fail') {
            setModalFailVisible(true);
            return;
        }

        if (!femelle.cycle) {
            setModalDatesVisible(true);
        } else if (femelle.cycle.statut === 'saillie') {
            setModalVerifyVisible(true);
        } else if (femelle.cycle.statut === 'gestante') {
            setVivantsInput('');
            setMortsInput('0');
            setModalBirthVisible(true);
        } else if (femelle.cycle.statut === 'allaitante') {
            handleStopCycle('termine');
        }
    };

    const handleStartCycle = async () => {
        if (!selectedFemelle || !dateInput) return;
        await addCycle(selectedFemelle.id, dateInput);
        setModalDatesVisible(false);
        loadData();
    };

    const handleConfirmBirth = async () => {
        if (!selectedFemelle || !selectedFemelle.cycle || !vivantsInput) return;
        await confirmBirth(selectedFemelle.cycle.id, dateInput, parseInt(vivantsInput), parseInt(mortsInput || 0));
        setModalBirthVisible(false);
        loadData();
    };

    const handleStopCycle = async (reason) => {
        if (!selectedFemelle || !selectedFemelle.cycle) return;
        await stopCycle(selectedFemelle.cycle.id, reason);
        setModalFailVisible(false);
        loadData();
    };

    return (
        <View style={{ flex: 1 }}>
            <ScrollView
                style={[styles.container, { backgroundColor: colors.background }]}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
            >
                <View style={[styles.header, { paddingBottom: 0, paddingTop: 12 }]} />

                {/* ALERTES STOCK ALIMENTATION */}
                {(alimentsBas || []).length > 0 && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: colors.error }]}>
                            ⚠️ Aliments Stock Bas ({(alimentsBas || []).length})
                        </Text>
                        {(alimentsBas || []).map((item) => (
                            <TouchableOpacity
                                key={item.id}
                                style={[
                                    styles.alertCard,
                                    {
                                        backgroundColor: colors.surface,
                                        borderLeftColor: item.jours_restants < 3 ? colors.error : colors.warning
                                    }
                                ]}
                                onPress={() => navigation.navigate('AlimentsScreen')}
                            >
                                <Text style={[styles.alertText, { color: colors.text }]}>{item.nom}: Reste {item.jours_restants} jours</Text>
                                <Text style={[styles.alertSubtext, { color: colors.textSecondary }]}>{item.stock_kg} kg en stock</Text>
                            </TouchableOpacity>
                        ))}
                    </View>
                )}

                {/* ALERTES VACCINS */}
                {((vaccinsEnRetard || []).length > 0 || (vaccinsBientot || []).length > 0) && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: colors.error }]}>
                            Vaccinations ({(vaccinsEnRetard || []).length + (vaccinsBientot || []).length})
                        </Text>
                        {(vaccinsEnRetard || []).map((item) => (
                            <VaccineAlert key={item.id} item={item} onPress={() => navigation.navigate('FemelleDetail', { femelleId: item.femelle_id })} />
                        ))}
                        {(vaccinsBientot || []).map((item) => (
                            <VaccineAlert key={item.id} item={item} onPress={() => navigation.navigate('FemelleDetail', { femelleId: item.femelle_id })} />
                        ))}
                    </View>
                )}

                {/* LISTE DES FEMELLES (DIRECTEMENT SUR DASHBOARD) */}
                <View style={styles.section}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                        <Text style={[styles.sectionTitle, { marginHorizontal: 16, marginBottom: 0 }]}>
                            Mes Lapines ({(femelles || []).length})
                        </Text>
                        <TouchableOpacity onPress={() => navigation.navigate('FemelleAddEdit')} style={{ marginRight: 16 }}>
                            <Text style={{ color: colors.primary, fontWeight: 'bold' }}>+ AJOUTER</Text>
                        </TouchableOpacity>
                    </View>

                    {(femelles || []).filter(f => f).map((femelle) => (
                        <View key={femelle.id} style={{ paddingHorizontal: 16 }}>
                            <RabbitStatusCard
                                femelle={femelle}
                                onAction={handleAction}
                            />
                        </View>
                    ))}

                    {femelles.length === 0 && (
                        <View style={styles.emptyState}>
                            <Text style={{ color: colors.disabled }}>Aucune lapine. Cliquez sur + pour commencer.</Text>
                        </View>
                    )}
                </View>

            </ScrollView>

            {/* MODALS POUR ACTIONS RAPIDES */}
            <Portal>
                {/* MODAL SAILLIE */}
                <Modal visible={modalDatesVisible} onDismiss={() => setModalDatesVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Déclarer Saillie 🌪️</Title>
                    <HelperText type="info">Date de mise au mâle</HelperText>
                    <TextInput label="Date (YYYY-MM-DD)" value={dateInput} onChangeText={setDateInput} mode="outlined" style={styles.input} />
                    <Button mode="contained" onPress={handleStartCycle} style={styles.button}>Valider</Button>
                </Modal>

                {/* MODAL NAISSANCE */}
                <Modal visible={modalBirthVisible} onDismiss={() => setModalBirthVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Carnet Rose 🐰</Title>
                    <HelperText type="info">Combien de petits ?</HelperText>
                    <TextInput label="Date Naissance" value={dateInput} onChangeText={setDateInput} mode="outlined" style={styles.input} />
                    <View style={styles.row}>
                        <TextInput label="Vivants" value={vivantsInput} onChangeText={setVivantsInput} keyboardType="numeric" mode="outlined" style={[styles.input, { flex: 1, marginRight: 8 }]} />
                        <TextInput label="Morts-nés" value={mortsInput} onChangeText={setMortsInput} keyboardType="numeric" mode="outlined" style={[styles.input, { flex: 1 }]} />
                    </View>
                    <Button mode="contained" onPress={handleConfirmBirth} style={styles.button}>Valider Naissance</Button>
                </Modal>

                {/* MODAL ECHEC */}
                <Modal visible={modalFailVisible} onDismiss={() => setModalFailVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Arrêter Cycle ⚠️</Title>
                    <Text>Confirmer échec/fausse couche ?</Text>
                    <Button mode="contained" color={colors.error} onPress={() => handleStopCycle('echec')} style={styles.button}>Confirmer</Button>
                    <Button onPress={() => setModalFailVisible(false)}>Annuler</Button>
                </Modal>

                {/* MODAL VERIFICATION GESTATION */}
                <Modal visible={modalVerifyVisible} onDismiss={() => setModalVerifyVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Vérification Gestation 🩺</Title>
                    <Text style={{ marginBottom: 16 }}>La lapine est-elle gestante après palpation ?</Text>
                    <DateInput label="Date Vérification" value={dateInput} onChange={setDateInput} style={styles.input} />

                    <Button
                        mode="contained"
                        onPress={() => handleVerifyGestation(true)}
                        style={[styles.button, { backgroundColor: colors.success }]}
                        icon="check"
                    >
                        OUI - Gestante confirmée
                    </Button>

                    <Button
                        mode="contained"
                        onPress={() => handleVerifyGestation(false)}
                        style={[styles.button, { backgroundColor: colors.error, marginTop: 12 }]}
                        icon="close"
                    >
                        NON - Echec (Pas de petits)
                    </Button>
                </Modal>
            </Portal>
        </View>
    );
};

const styles = StyleSheet.create({
    container: { flex: 1 },
    header: { padding: 24, paddingBottom: 10 },
    title: { fontSize: 32, fontWeight: 'bold' },
    section: { marginTop: 16, marginBottom: 8 },
    sectionTitle: { fontSize: 18, fontWeight: 'bold' },
    alertCard: { marginHorizontal: 16, marginBottom: 8, padding: 16, borderRadius: 8, borderLeftWidth: 4, elevation: 2 },
    alertText: { fontSize: 16, fontWeight: 'bold' },
    alertSubtext: { fontSize: 14, marginTop: 4 },
    emptyState: { alignItems: 'center', justifyContent: 'center', padding: 40 },
    modal: { backgroundColor: 'white', padding: 20, margin: 20, borderRadius: 8 },
    input: { marginBottom: 12, backgroundColor: 'white' },
    button: { marginTop: 8 },
    row: { flexDirection: 'row', justifyContent: 'space-between' }
});

export default DashboardScreen;
