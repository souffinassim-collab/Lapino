import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    Modal,
    Alert
} from 'react-native';
import { useTheme, useFocusEffect } from '@react-navigation/native';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import { Chip, Button, Divider, Card } from 'react-native-paper';
import {
    getFemelleById,
    getVaccinationsByFemelle,
    getAllVaccins,
    addVaccination,
    deleteFemelle,
    getFemelleStats,
    getFemelleHistory
} from '../../database/db';
import { formatDateFR, formatDateISO } from '../../utils/dateUtils';
import CustomButton from '../../components/CustomButton';
import RabbitStatusCard from '../../components/RabbitStatusCard';
import ReproductionModals from '../../components/ReproductionModals';

const calculateAge = (birthDateStr) => {
    if (!birthDateStr) return 'Inconnu';
    const birth = new Date(birthDateStr);
    if (isNaN(birth.getTime())) return 'Inconnu';
    const now = new Date();
    const months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth());
    if (months < 1) return 'Moins d\'un mois';
    if (months < 12) return `${months} mois`;
    const years = Math.floor(months / 12);
    const remMonths = months % 12;
    return remMonths > 0 ? `${years} an(s) et ${remMonths} mois` : `${years} an(s)`;
};

const FemelleDetailScreen = ({ route, navigation }) => {
    const { femelleId } = route.params;
    const { colors } = useTheme();
    const [femelle, setFemelle] = useState(null);
    const [vaccinations, setVaccinations] = useState([]);
    const [stats, setStats] = useState({
        total_saillies: 0,
        total_mises_bas: 0,
        total_vivants: 0,
        total_morts: 0,
        total_sevres: 0,
        taux_fertilite: 0,
        moyenne_portee: 0,
        taux_sevrage: 0
    });
    const [history, setHistory] = useState([]);
    const [modalVaccinVisible, setModalVaccinVisible] = useState(false);
    const [vaccins, setVaccins] = useState([]);
    const [selectedVaccin, setSelectedVaccin] = useState(null);
    const [dateVaccination, setDateVaccination] = useState(new Date());
    const [showDatePicker, setShowDatePicker] = useState(false);

    // Modal reproduction
    const [activeReproModal, setActiveReproModal] = useState(null);

    const loadData = async () => {
        try {
            const femelleData = await getFemelleById(femelleId);
            setFemelle(femelleData);

            const vaccinationsData = await getVaccinationsByFemelle(femelleId);
            setVaccinations(vaccinationsData);

            const statsData = await getFemelleStats(femelleId);
            setStats(statsData);

            const historyData = await getFemelleHistory(femelleId);
            setHistory(historyData);

            const vaccinsData = await getAllVaccins();
            setVaccins(vaccinsData);
            if (vaccinsData.length > 0) {
                setSelectedVaccin(vaccinsData[0].id);
            }
        } catch (error) {
            console.error('Erreur chargement détail femelle:', error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadData();
        }, [femelleId])
    );

    const handleAddVaccin = async () => {
        if (!selectedVaccin) {
            Alert.alert('Erreur', 'Veuillez sélectionner un vaccin');
            return;
        }

        try {
            await addVaccination(
                femelleId,
                selectedVaccin,
                formatDateISO(dateVaccination)
            );
            setModalVaccinVisible(false);
            setDateVaccination(new Date());
            await loadData();
        } catch (error) {
            console.error('Erreur ajout vaccination:', error);
            Alert.alert('Erreur', "Impossible d'ajouter la vaccination");
        }
    };

    const handleDelete = () => {
        Alert.alert(
            'Confirmer la suppression',
            'Êtes-vous sûr de vouloir supprimer cette lapine ? Toutes ses données de reproduction seront effacées.',
            [
                { text: 'Annuler', style: 'cancel' },
                {
                    text: 'Supprimer',
                    style: 'destructive',
                    onPress: async () => {
                        try {
                            await deleteFemelle(femelleId);
                            navigation.goBack();
                        } catch (error) {
                            Alert.alert('Erreur', 'Impossible de supprimer');
                        }
                    },
                },
            ]
        );
    };

    if (!femelle) {
        return (
            <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
                <Text style={{ color: colors.text }}>Chargement de la fiche lapine...</Text>
            </View>
        );
    }

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <ScrollView style={styles.scrollView} contentContainerStyle={{ paddingBottom: 40 }}>

                {/* 1. CARTE PROFIL LAPINE */}
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <View style={styles.cardHeaderRow}>
                        <View style={{ flex: 1 }}>
                            <Text style={[styles.title, { color: colors.text }]}>🐰 {femelle.numero}</Text>
                            <Text style={{ color: colors.textSecondary, marginTop: 2 }}>
                                Loge : {femelle.clapet_numero ? `Clapet ${femelle.clapet_numero}` : 'Aucun'}
                            </Text>
                        </View>
                        <Button
                            mode="outlined"
                            compact
                            onPress={() => navigation.navigate('FemelleAddEdit', { femelleId: femelle.id })}
                        >
                            Modifier
                        </Button>
                    </View>

                    <Divider style={{ marginVertical: 12 }} />

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>Âge estimé :</Text>
                        <Text style={[styles.value, { color: colors.text }]}>{calculateAge(femelle.date_naissance)}</Text>
                    </View>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>Date de naissance :</Text>
                        <Text style={[styles.value, { color: colors.text }]}>{formatDateFR(femelle.date_naissance)}</Text>
                    </View>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>Statut général :</Text>
                        <Chip compact style={{ backgroundColor: femelle.statut === 'vivante' ? '#E8F5E9' : '#FFEBEE' }}>
                            {femelle.statut}
                        </Chip>
                    </View>
                </View>

                {/* 2. CYCLE DE REPRODUCTION ACTUEL */}
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <Text style={[styles.title, { color: colors.text, marginBottom: 10 }]}>
                        🌪️ Cycle de Reproduction Actuel
                    </Text>

                    <RabbitStatusCard
                        femelle={femelle}
                        onAction={(f, actionType) => setActiveReproModal(actionType)}
                    />
                </View>

                {/* 3. PERFORMANCE DE REPRODUCTION CUNICULICOLE */}
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <Text style={[styles.title, { color: colors.text, marginBottom: 12 }]}>
                        📊 Bilan Zootechnique de la Lapine
                    </Text>

                    <View style={styles.statsGrid}>
                        <View style={styles.statBox}>
                            <Text style={[styles.statValue, { color: colors.primary }]}>{stats.taux_fertilite}%</Text>
                            <Text style={styles.statLabel}>Fertilité (saillies)</Text>
                        </View>
                        <View style={styles.statBox}>
                            <Text style={[styles.statValue, { color: '#2196F3' }]}>{stats.total_mises_bas}</Text>
                            <Text style={styles.statLabel}>Portées nées</Text>
                        </View>
                        <View style={styles.statBox}>
                            <Text style={[styles.statValue, { color: '#4CAF50' }]}>{stats.moyenne_portee}</Text>
                            <Text style={styles.statLabel}>Moy. vivants/portée</Text>
                        </View>
                        <View style={styles.statBox}>
                            <Text style={[styles.statValue, { color: '#388E3C' }]}>{stats.total_sevres}</Text>
                            <Text style={styles.statLabel}>Total sevrés</Text>
                        </View>
                    </View>

                    <View style={[styles.infoRow, { marginTop: 12 }]}>
                        <Text style={{ fontSize: 13, color: colors.textSecondary }}>
                            Total nés : {stats.total_vivants} vivants • {stats.total_morts} mort-nés
                        </Text>
                        <Text style={{ fontSize: 13, fontWeight: 'bold', color: '#2E7D32' }}>
                            Taux de sevrage : {stats.taux_sevrage}%
                        </Text>
                    </View>
                </View>

                {/* 4. HISTORIQUE DÉTAILLÉ DES PORTÉES */}
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <Text style={[styles.title, { color: colors.text, marginBottom: 8 }]}>
                        📜 Historique des Cycles & Portées ({history.length})
                    </Text>

                    {history.length === 0 ? (
                        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                            Aucun cycle précédent enregistré
                        </Text>
                    ) : (
                        history.map((cycle, index) => {
                            const isSuccess = cycle.statut === 'termine';
                            const isFail = cycle.statut === 'echec';
                            return (
                                <View key={cycle.id || index} style={[styles.historyCard, { borderColor: colors.border }]}>
                                    <View style={styles.historyHeader}>
                                        <Text style={[styles.historyDate, { color: colors.text }]}>
                                            Saillie du {formatDateFR(cycle.date_saillie)}
                                            {cycle.male_numero ? ` • Mâle: ${cycle.male_numero}` : ''}
                                        </Text>
                                        <Chip
                                            compact
                                            style={{
                                                backgroundColor: isSuccess ? '#E8F5E9' : (isFail ? '#FFEBEE' : '#E3F2FD')
                                            }}
                                            textStyle={{
                                                color: isSuccess ? '#2E7D32' : (isFail ? '#C62828' : '#1565C0'),
                                                fontSize: 11
                                            }}
                                        >
                                            {cycle.statut === 'termine' ? 'Sevrage réussi' :
                                             cycle.statut === 'echec' ? 'Échec' : cycle.statut}
                                        </Chip>
                                    </View>

                                    {cycle.date_mise_bas_reelle && (
                                        <Text style={styles.historyDetailText}>
                                            🐰 Mise bas : {formatDateFR(cycle.date_mise_bas_reelle)} • {cycle.nombre_vivants || 0} nés vivants, {cycle.nombre_morts || 0} mort-nés
                                        </Text>
                                    )}

                                    {cycle.nombre_sevres !== null && cycle.nombre_sevres !== undefined && (
                                        <Text style={[styles.historyDetailText, { color: '#2E7D32', fontWeight: 'bold' }]}>
                                            🧺 Sevrés : {cycle.nombre_sevres} lapereaux
                                            {cycle.nombre_vivants > 0 && ` (${Math.round((cycle.nombre_sevres / cycle.nombre_vivants) * 100)}% de survie)`}
                                        </Text>
                                    )}

                                    {cycle.motif_echec && (
                                        <Text style={[styles.historyDetailText, { color: '#C62828' }]}>
                                            Motif incident : {cycle.motif_echec}
                                        </Text>
                                    )}

                                    {cycle.notes && (
                                        <Text style={[styles.historyDetailText, { fontStyle: 'italic', color: '#666' }]}>
                                            Obs : {cycle.notes}
                                        </Text>
                                    )}
                                </View>
                            );
                        })
                    )}
                </View>

                {/* 5. VACCINATIONS */}
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <View style={styles.cardHeaderRow}>
                        <Text style={[styles.title, { color: colors.text }]}>
                            💉 Vaccinations ({vaccinations.length})
                        </Text>
                        <CustomButton
                            title="+ Ajouter"
                            onPress={() => setModalVaccinVisible(true)}
                            style={{ paddingHorizontal: 12, paddingVertical: 4 }}
                        />
                    </View>

                    {vaccinations.length === 0 ? (
                        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                            Aucune vaccination enregistrée
                        </Text>
                    ) : (
                        vaccinations.map((item) => (
                            <View
                                key={item.id}
                                style={[styles.vaccinItem, { borderBottomColor: colors.border }]}
                            >
                                <Text style={[styles.vaccinName, { color: colors.text }]}>
                                    {item.vaccin_nom}
                                </Text>
                                <Text style={[styles.vaccinDate, { color: colors.textSecondary }]}>
                                    Fait le : {formatDateFR(item.date_vaccination)} • Prochain : {formatDateFR(item.date_prochain)}
                                </Text>
                            </View>
                        ))
                    )}
                </View>

                {/* BOUTON SUPPRIMER */}
                <CustomButton
                    title="Supprimer la lapine"
                    variant="danger"
                    onPress={handleDelete}
                    style={styles.deleteButton}
                />
            </ScrollView>

            {/* MODAL VACCIN */}
            <Modal
                visible={modalVaccinVisible}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setModalVaccinVisible(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={[styles.modalContent, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.modalTitle, { color: colors.text }]}>
                            Ajouter un vaccin
                        </Text>

                        <Text style={[styles.label, { color: colors.textSecondary, marginTop: 8 }]}>
                            Vaccin :
                        </Text>
                        <View style={[styles.pickerContainer, { borderColor: colors.border }]}>
                            <Picker
                                selectedValue={selectedVaccin}
                                onValueChange={setSelectedVaccin}
                                style={{ color: colors.text }}
                            >
                                {vaccins.map((v) => (
                                    <Picker.Item
                                        key={v.id}
                                        label={`${v.nom} (${v.duree_jours}j)`}
                                        value={v.id}
                                    />
                                ))}
                            </Picker>
                        </View>

                        <Text style={[styles.label, { color: colors.textSecondary, marginTop: 14 }]}>
                            Date de vaccination :
                        </Text>
                        <TouchableOpacity
                            style={[styles.dateButton, { borderColor: colors.border }]}
                            onPress={() => setShowDatePicker(true)}
                        >
                            <Text style={{ color: colors.text }}>
                                {formatDateFR(formatDateISO(dateVaccination))}
                            </Text>
                        </TouchableOpacity>

                        {showDatePicker && (
                            <DateTimePicker
                                value={dateVaccination}
                                mode="date"
                                display="default"
                                onChange={(event, selectedDate) => {
                                    setShowDatePicker(false);
                                    if (selectedDate) setDateVaccination(selectedDate);
                                }}
                            />
                        )}

                        <View style={styles.modalButtons}>
                            <CustomButton
                                title="Annuler"
                                variant="secondary"
                                onPress={() => setModalVaccinVisible(false)}
                                style={styles.modalButton}
                            />
                            <CustomButton
                                title="Enregistrer"
                                onPress={handleAddVaccin}
                                style={styles.modalButton}
                            />
                        </View>
                    </View>
                </View>
            </Modal>

            {/* MODAL REPRODUCTION */}
            <ReproductionModals
                activeModal={activeReproModal}
                femelle={femelle}
                onClose={() => setActiveReproModal(null)}
                onSuccess={loadData}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    container: { flex: 1 },
    scrollView: { flex: 1 },
    card: {
        marginHorizontal: 16,
        marginTop: 14,
        padding: 16,
        borderRadius: 12,
        elevation: 2,
    },
    cardHeaderRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    title: {
        fontSize: 18,
        fontWeight: 'bold',
    },
    infoRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 5,
    },
    label: {
        fontSize: 14,
    },
    value: {
        fontSize: 14,
        fontWeight: '600',
    },
    statsGrid: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    statBox: {
        flex: 1,
        alignItems: 'center',
        padding: 8,
        backgroundColor: '#F5F5F5',
        borderRadius: 8,
        marginHorizontal: 3,
    },
    statValue: {
        fontSize: 18,
        fontWeight: 'bold',
    },
    statLabel: {
        fontSize: 10.5,
        color: '#666',
        marginTop: 3,
        textAlign: 'center',
    },
    historyCard: {
        padding: 10,
        borderRadius: 8,
        borderWidth: 1,
        marginBottom: 8,
        backgroundColor: '#FFFFFF',
    },
    historyHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 4,
    },
    historyDate: {
        fontSize: 13.5,
        fontWeight: 'bold',
    },
    historyDetailText: {
        fontSize: 12.5,
        color: '#444',
        marginTop: 2,
    },
    emptyText: {
        fontSize: 14,
        fontStyle: 'italic',
        textAlign: 'center',
        marginVertical: 10,
    },
    vaccinItem: {
        paddingVertical: 10,
        borderBottomWidth: 1,
    },
    vaccinName: {
        fontSize: 15,
        fontWeight: '600',
    },
    vaccinDate: {
        fontSize: 12.5,
        marginTop: 3,
    },
    deleteButton: {
        marginHorizontal: 16,
        marginTop: 20,
    },
    modalOverlay: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        padding: 20,
    },
    modalContent: {
        width: '100%',
        maxWidth: 400,
        padding: 20,
        borderRadius: 12,
        elevation: 5,
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 8,
    },
    pickerContainer: {
        borderWidth: 1,
        borderRadius: 8,
        marginTop: 4,
    },
    dateButton: {
        borderWidth: 1,
        borderRadius: 8,
        padding: 12,
        marginTop: 4,
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'flex-end',
        marginTop: 20,
        gap: 10,
    },
    modalButton: {
        minWidth: 100,
    },
});

export default FemelleDetailScreen;
