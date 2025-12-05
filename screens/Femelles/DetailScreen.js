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
import {
    getFemelleById,
    getVaccinationsByFemelle,
    getAllVaccins,
    addVaccination,
    deleteFemelle
} from '../../database/db';
import { formatDateFR, formatDateISO } from '../../utils/dateUtils';
import CustomButton from '../../components/CustomButton';

const FemelleDetailScreen = ({ route, navigation }) => {
    const { femelleId } = route.params;
    const { colors } = useTheme();
    const [femelle, setFemelle] = useState(null);
    const [vaccinations, setVaccinations] = useState([]);
    const [modalVisible, setModalVisible] = useState(false);
    const [vaccins, setVaccins] = useState([]);
    const [selectedVaccin, setSelectedVaccin] = useState(null);
    const [dateVaccination, setDateVaccination] = useState(new Date());
    const [showDatePicker, setShowDatePicker] = useState(false);

    const loadData = async () => {
        try {
            const femelleData = await getFemelleById(femelleId);
            setFemelle(femelleData);

            const vaccinationsData = await getVaccinationsByFemelle(femelleId);
            setVaccinations(vaccinationsData);

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
            setModalVisible(false);
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
            'Êtes-vous sûr de vouloir supprimer cette femelle ?',
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
            <View style={[styles.container, { backgroundColor: colors.background }]}>
                <Text style={{ color: colors.text }}>Chargement...</Text>
            </View>
        );
    }

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <ScrollView style={styles.scrollView}>
                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <Text style={[styles.title, { color: colors.text }]}>
                        🐰 Informations
                    </Text>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Numéro:
                        </Text>
                        <Text style={[styles.value, { color: colors.text }]}>
                            {femelle.numero}
                        </Text>
                    </View>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Clapet:
                        </Text>
                        <Text style={[styles.value, { color: colors.text }]}>
                            {femelle.clapet_numero || 'Aucun'}
                        </Text>
                    </View>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Date naissance:
                        </Text>
                        <Text style={[styles.value, { color: colors.text }]}>
                            {formatDateFR(femelle.date_naissance)}
                        </Text>
                    </View>

                    <View style={styles.infoRow}>
                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Statut:
                        </Text>
                        <Text style={[styles.value, { color: colors.text }]}>
                            {femelle.statut}
                        </Text>
                    </View>
                </View>

                <View style={[styles.card, { backgroundColor: colors.surface }]}>
                    <View style={styles.header}>
                        <Text style={[styles.title, { color: colors.text }]}>
                            💉 Vaccinations
                        </Text>
                        <CustomButton
                            title="Ajouter"
                            onPress={() => setModalVisible(true)}
                            style={styles.addButton}
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
                                    Fait le: {formatDateFR(item.date_vaccination)}
                                </Text>
                                <Text style={[styles.vaccinDate, { color: colors.textSecondary }]}>
                                    Prochain: {formatDateFR(item.date_prochain)}
                                </Text>
                            </View>
                        ))
                    )}
                </View>

                <CustomButton
                    title="Supprimer la femelle"
                    variant="danger"
                    onPress={handleDelete}
                    style={styles.deleteButton}
                />
            </ScrollView>

            {/* Modal ajout vaccination */}
            <Modal
                visible={modalVisible}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={[styles.modalContent, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.modalTitle, { color: colors.text }]}>
                            Ajouter un vaccin
                        </Text>

                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Vaccin:
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

                        <Text style={[styles.label, { color: colors.textSecondary, marginTop: 16 }]}>
                            Date de vaccination:
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
                                    if (selectedDate) {
                                        setDateVaccination(selectedDate);
                                    }
                                }}
                            />
                        )}

                        <View style={styles.modalButtons}>
                            <CustomButton
                                title="Annuler"
                                variant="secondary"
                                onPress={() => setModalVisible(false)}
                                style={styles.modalButton}
                            />
                            <CustomButton
                                title="Ajouter"
                                onPress={handleAddVaccin}
                                style={styles.modalButton}
                            />
                        </View>
                    </View>
                </View>
            </Modal>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollView: {
        flex: 1,
    },
    card: {
        margin: 16,
        padding: 16,
        borderRadius: 8,
        elevation: 2,
    },
    title: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 16,
    },
    infoRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    label: {
        fontSize: 16,
    },
    value: {
        fontSize: 16,
        fontWeight: '600',
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 16,
    },
    addButton: {
        paddingHorizontal: 16,
        paddingVertical: 8,
        minHeight: 40,
    },
    emptyText: {
        textAlign: 'center',
        padding: 20,
    },
    vaccinItem: {
        paddingVertical: 12,
        borderBottomWidth: 1,
    },
    vaccinName: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 4,
    },
    vaccinDate: {
        fontSize: 14,
        marginTop: 2,
    },
    deleteButton: {
        margin: 16,
        marginTop: 8,
    },
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    modalContent: {
        width: '85%',
        borderRadius: 12,
        padding: 20,
        elevation: 5,
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 20,
    },
    pickerContainer: {
        borderWidth: 1,
        borderRadius: 8,
        marginTop: 8,
    },
    dateButton: {
        borderWidth: 1,
        borderRadius: 8,
        padding: 12,
        marginTop: 8,
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 24,
    },
    modalButton: {
        flex: 1,
        marginHorizontal: 4,
    },
});

export default FemelleDetailScreen;
