import React, { useState, useCallback } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import { FAB, useTheme, Portal, Modal, Text, TextInput, Button, Title, HelperText } from 'react-native-paper';
import { useFocusEffect } from '@react-navigation/native';
import RabbitStatusCard from '../../components/RabbitStatusCard';
import DateInput from '../../components/DateInput';
import {
    getFemellesWithStatus,
    startCycle,
    confirmBirth,

    stopCycle,
    verifyGestation,
    deleteFemelle // Keep regular delete if needed, though hidden behind detail
} from '../../database/db';
import { formatDateISO } from '../../utils/dateUtils';

const FemellesListScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [femelles, setFemelles] = useState([]);
    const [refreshing, setRefreshing] = useState(false);

    // Modals state
    const [modalDatesVisible, setModalDatesVisible] = useState(false); // Pour Saillie
    const [modalBirthVisible, setModalBirthVisible] = useState(false); // Pour Naissance
    const [modalFailVisible, setModalFailVisible] = useState(false);   // Pour Echec
    const [modalVerifyVisible, setModalVerifyVisible] = useState(false);

    const [selectedFemelle, setSelectedFemelle] = useState(null);

    // Form inputs
    const [dateInput, setDateInput] = useState(new Date().toISOString().split('T')[0]);
    const [vivantsInput, setVivantsInput] = useState('');
    const [mortsInput, setMortsInput] = useState('0');

    const loadData = async () => {
        setRefreshing(true);
        try {
            const data = await getFemellesWithStatus();
            setFemelles(data);
        } catch (error) {
            console.error(error);
        } finally {
            setRefreshing(false);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadData();
        }, [])
    );

    const handleAction = (femelle, actionType) => {
        setSelectedFemelle(femelle);
        setDateInput(new Date().toISOString().split('T')[0]); // Reset date to today

        if (actionType === 'fail') {
            setModalFailVisible(true);
            return;
        }

        // Primary actions based on cycle status
        if (!femelle.cycle) {
            // Pas de cycle -> Déclarer Saillie
            setModalDatesVisible(true);
        } else if (femelle.cycle.statut === 'saillie') {
            // Saillie -> Vérification
            setModalVerifyVisible(true);
        } else if (femelle.cycle.statut === 'gestante') {
            // Gestante -> Déclarer Naissance
            setVivantsInput('');
            setMortsInput('0');
            setModalBirthVisible(true);
        } else if (femelle.cycle.statut === 'allaitante') {
            // Allaitante -> Sevrage (Stop cycle)
            handleStopCycle('termine');
        }
    };

    const handleStartCycle = async () => {
        if (!selectedFemelle || !dateInput) return;
        try {
            await startCycle(selectedFemelle.id, dateInput);
            setModalDatesVisible(false);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleConfirmBirth = async () => {
        if (!selectedFemelle || !selectedFemelle.cycle || !vivantsInput) return;
        try {
            await confirmBirth(
                selectedFemelle.cycle.id,
                dateInput,
                parseInt(vivantsInput),
                parseInt(mortsInput || 0)
            );
            setModalBirthVisible(false);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleStopCycle = async (reason) => {
        if (!selectedFemelle || !selectedFemelle.cycle) return;
        try {
            await stopCycle(selectedFemelle.cycle.id, reason);
            setModalFailVisible(false); // Ferme si ouvert
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <View style={styles.container}>
            <FlatList
                data={femelles}
                renderItem={({ item }) => (
                    <RabbitStatusCard
                        femelle={item}
                        onAction={handleAction}
                    />
                )}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.list}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={loadData} />
                }
            />

            <FAB
                style={[styles.fab, { backgroundColor: colors.accent }]}
                icon="plus"
                onPress={() => navigation.navigate('FemelleAddEdit')}
            />

            {/* MODAL SAILLIE */}
            <Portal>
                <Modal visible={modalDatesVisible} onDismiss={() => setModalDatesVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Déclarer Saillie 🌪️</Title>
                    <HelperText type="info">Date de mise au mâle</HelperText>
                    <DateInput
                        label="Date Saillie"
                        value={dateInput}
                        onChange={setDateInput}
                        style={styles.input}
                    />
                    <Button mode="contained" onPress={handleStartCycle} style={styles.button}>Valider</Button>
                </Modal>
            </Portal>

            {/* MODAL NAISSANCE */}
            <Portal>
                <Modal visible={modalBirthVisible} onDismiss={() => setModalBirthVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Carnet Rose 🐰</Title>
                    <HelperText type="info">Félicitations ! Combien de petits ?</HelperText>

                    <TextInput
                        label="Date Naissance (YYYY-MM-DD)"
                        value={dateInput}
                        onChangeText={setDateInput}
                        mode="outlined"
                        style={styles.input}
                    />

                    <View style={styles.row}>
                        <TextInput
                            label="Vivants"
                            value={vivantsInput}
                            onChangeText={setVivantsInput}
                            keyboardType="numeric"
                            mode="outlined"
                            style={[styles.input, { flex: 1, marginRight: 8 }]}
                        />
                        <TextInput
                            label="Morts-nés"
                            value={mortsInput}
                            onChangeText={setMortsInput}
                            keyboardType="numeric"
                            mode="outlined"
                            style={[styles.input, { flex: 1 }]}
                        />
                    </View>

                    <Button mode="contained" onPress={handleConfirmBirth} style={styles.button}>Valider Naissance</Button>
                </Modal>
            </Portal>

            {/* MODAL ECHEC */}
            <Portal>
                <Modal visible={modalFailVisible} onDismiss={() => setModalFailVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Signaler un problème ⚠️</Title>
                    <Text style={{ marginBottom: 16 }}>Voulez-vous arrêter le cycle pour "{selectedFemelle?.numero}" ?</Text>
                    <Button
                        mode="contained"
                        color={colors.error}
                        onPress={() => handleStopCycle('echec')}
                        style={styles.button}
                    >
                        Confirmer Echec / Fausse Couche
                    </Button>
                    <Button onPress={() => setModalFailVisible(false)} style={{ marginTop: 8 }}>Annuler</Button>
                </Modal>
            </Portal>

            {/* MODAL VERIFICATION */}
            <Portal>
                <Modal visible={modalVerifyVisible} onDismiss={() => setModalVerifyVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>Vérification Gestation 🩺</Title>
                    <Text style={{ marginBottom: 16 }}>Résultat de la palpation ?</Text>
                    <TextInput label="Date" value={dateInput} onChangeText={setDateInput} mode="outlined" style={styles.input} />

                    <Button mode="contained" onPress={() => handleVerifyGestation(true)} style={[styles.button, { backgroundColor: colors.success }]}>
                        OUI - Gestante
                    </Button>
                    <Button mode="contained" onPress={() => handleVerifyGestation(false)} style={[styles.button, { backgroundColor: colors.error, marginTop: 12 }]}>
                        NON - Vide
                    </Button>
                </Modal>
            </Portal>

        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    list: {
        padding: 16,
        paddingBottom: 80,
    },
    fab: {
        position: 'absolute',
        margin: 16,
        right: 0,
        bottom: 0,
    },
    modal: {
        backgroundColor: 'white',
        padding: 20,
        margin: 20,
        borderRadius: 8,
    },
    input: {
        marginBottom: 12,
        backgroundColor: 'white',
    },
    button: {
        marginTop: 8,
    },
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    }
});

export default FemellesListScreen;
