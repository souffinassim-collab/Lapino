import React, { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, Platform, KeyboardAvoidingView } from 'react-native';
import { Modal, Portal, Title, Text, TextInput, Button, HelperText, useTheme, RadioButton, Divider } from 'react-native-paper';
import { formatDateISO } from '../utils/dateUtils';
import {
    startCycle,
    verifyGestation,
    confirmNestBox,
    confirmBirth,
    confirmWeaning,
    stopCycle
} from '../database/db';

const ReproductionModals = ({
    activeModal,       // null, 'start', 'verify', 'nest', 'birth', 'wean', 'fail'
    femelle,           // Objet femelle sélectionnée
    onClose,           // Callback fermeture modal
    onSuccess          // Callback succès pour recharger les données
}) => {
    const { colors } = useTheme();

    // Inputs
    const [dateInput, setDateInput] = useState(formatDateISO(new Date()));
    const [maleInput, setMaleInput] = useState('');
    const [notesInput, setNotesInput] = useState('');
    const [vivantsInput, setVivantsInput] = useState('');
    const [mortsInput, setMortsInput] = useState('0');
    const [sevresInput, setSevresInput] = useState('');
    const [motifFail, setMotifFail] = useState('fausse_couche');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (femelle) {
            setDateInput(formatDateISO(new Date()));
            setNotesInput('');
            setMaleInput(femelle.cycle?.male_numero || '');
            setVivantsInput('');
            setMortsInput('0');
            setSevresInput(femelle.cycle?.nombre_vivants ? String(femelle.cycle.nombre_vivants) : '');
            setMotifFail('fausse_couche');
        }
    }, [femelle, activeModal]);

    if (!activeModal || !femelle) return null;

    // Handlers
    const handleStart = async () => {
        if (!dateInput) return;
        setLoading(true);
        try {
            await startCycle(femelle.id, dateInput, maleInput.trim(), notesInput.trim());
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur démarrage cycle:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (isPregnant) => {
        if (!femelle.cycle) return;
        setLoading(true);
        try {
            await verifyGestation(femelle.cycle.id, dateInput, isPregnant, notesInput.trim());
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur diagnostic gestation:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleNest = async () => {
        if (!femelle.cycle) return;
        setLoading(true);
        try {
            await confirmNestBox(femelle.cycle.id, 1);
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur pose boîte à nid:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleBirth = async () => {
        if (!femelle.cycle || vivantsInput === '') return;
        setLoading(true);
        try {
            await confirmBirth(
                femelle.cycle.id,
                dateInput,
                parseInt(vivantsInput) || 0,
                parseInt(mortsInput) || 0,
                notesInput.trim()
            );
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur déclaration naissance:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleWean = async () => {
        if (!femelle.cycle || sevresInput === '') return;
        setLoading(true);
        try {
            await confirmWeaning(
                femelle.cycle.id,
                dateInput,
                parseInt(sevresInput) || 0,
                notesInput.trim()
            );
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur déclaration sevrage:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFail = async () => {
        if (!femelle.cycle) return;
        setLoading(true);
        try {
            await stopCycle(femelle.cycle.id, 'echec', motifFail, notesInput.trim());
            onSuccess();
            onClose();
        } catch (error) {
            console.error('Erreur arrêt cycle:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Portal>
            <Modal
                visible={!!activeModal}
                onDismiss={onClose}
                contentContainerStyle={[styles.modalCard, { backgroundColor: colors.surface || '#FFFFFF' }]}
            >
                <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
                    <ScrollView showsVerticalScrollIndicator={false}>

                        {/* --- 1. MODAL SAILLIE --- */}
                        {activeModal === 'start' && (
                            <View>
                                <Title style={styles.modalTitle}>🌪️ Déclarer une Saillie</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <View style={styles.cuniTip}>
                                    <Text style={styles.cuniTipText}>
                                        💡 Conseil : Amenez toujours la lapine dans la cage du mâle (jamais l'inverse).
                                        La palpation abdominale sera conseillée dans 11 jours.
                                    </Text>
                                </View>

                                <TextInput
                                    label="Date de la saillie (YYYY-MM-DD)"
                                    value={dateInput}
                                    onChangeText={setDateInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <TextInput
                                    label="Mâle reproducteur (Nom / N°)"
                                    placeholder="Ex: M01 - Rex, Fauve..."
                                    value={maleInput}
                                    onChangeText={setMaleInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <TextInput
                                    label="Notes / Remarques (Optionnel)"
                                    placeholder="Ex: Acceptation rapide..."
                                    value={notesInput}
                                    onChangeText={setNotesInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <Button
                                    mode="contained"
                                    onPress={handleStart}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#E91E63' }]}
                                >
                                    Valider la Saillie
                                </Button>
                            </View>
                        )}

                        {/* --- 2. MODAL PALPATION --- */}
                        {activeModal === 'verify' && (
                            <View>
                                <Title style={styles.modalTitle}>🩺 Palpation Abdominale</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <View style={styles.cuniTip}>
                                    <Text style={styles.cuniTipText}>
                                        💡 Palpez délicatement le bas-ventre entre le pouce et les doigts.
                                        À J10-J14, les embryons forment un chapelet de billes de la taille d'une noisette.
                                    </Text>
                                </View>

                                <TextInput
                                    label="Date du contrôle (YYYY-MM-DD)"
                                    value={dateInput}
                                    onChangeText={setDateInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <TextInput
                                    label="Remarques (Optionnel)"
                                    placeholder="Ex: Embryons bien distincts..."
                                    value={notesInput}
                                    onChangeText={setNotesInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <Button
                                    mode="contained"
                                    icon="check-circle"
                                    onPress={() => handleVerify(true)}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#2E7D32', marginBottom: 10 }]}
                                >
                                    ✅ OUI - Gestante confirmée
                                </Button>

                                <Button
                                    mode="contained"
                                    icon="close-circle"
                                    onPress={() => handleVerify(false)}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#C62828' }]}
                                >
                                    ❌ NON - Vide (Échec fécondation)
                                </Button>
                            </View>
                        )}

                        {/* --- 3. MODAL BOITE A NID --- */}
                        {activeModal === 'nest' && (
                            <View>
                                <Title style={styles.modalTitle}>📦 Pose de la Boîte à Nid</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <View style={styles.cuniTip}>
                                    <Text style={styles.cuniTipText}>
                                        💡 À J28 de gestation, la boîte à nid propre garnie de copeaux et paille est indispensable.
                                        La lapine va s'arracher les poils pour le capitonner avant de mettre bas.
                                    </Text>
                                </View>

                                <Button
                                    mode="contained"
                                    icon="check"
                                    onPress={handleNest}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#2E7D32' }]}
                                >
                                    Confirmer la pose du nid ✓
                                </Button>
                            </View>
                        )}

                        {/* --- 4. MODAL NAISSANCE --- */}
                        {activeModal === 'birth' && (
                            <View>
                                <Title style={styles.modalTitle}>🐰 Déclarer la Mise Bas</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <View style={styles.cuniTip}>
                                    <Text style={styles.cuniTipText}>
                                        💡 Contrôlez le nid avec calme : retirez les mort-nés éventuels et vérifiez que les ventres
                                        des petits sont bien ronds (preuve d'une bonne lactation).
                                    </Text>
                                </View>

                                <TextInput
                                    label="Date de mise bas (YYYY-MM-DD)"
                                    value={dateInput}
                                    onChangeText={setDateInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <View style={styles.row}>
                                    <TextInput
                                        label="Nés Vivants"
                                        value={vivantsInput}
                                        onChangeText={setVivantsInput}
                                        keyboardType="numeric"
                                        mode="outlined"
                                        style={[styles.input, { flex: 1, marginRight: 8 }]}
                                    />
                                    <TextInput
                                        label="Mort-nés"
                                        value={mortsInput}
                                        onChangeText={setMortsInput}
                                        keyboardType="numeric"
                                        mode="outlined"
                                        style={[styles.input, { flex: 1 }]}
                                    />
                                </View>

                                <TextInput
                                    label="Notes sur la portée (Optionnel)"
                                    placeholder="Ex: Nid propre, mère calme..."
                                    value={notesInput}
                                    onChangeText={setNotesInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <Button
                                    mode="contained"
                                    icon="baby-face-outline"
                                    onPress={handleBirth}
                                    disabled={vivantsInput === ''}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#2E7D32' }]}
                                >
                                    Enregistrer la Naissance
                                </Button>
                            </View>
                        )}

                        {/* --- 5. MODAL SEVRAGE --- */}
                        {activeModal === 'wean' && (
                            <View>
                                <Title style={styles.modalTitle}>🧺 Déclarer le Sevrage</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <View style={styles.cuniTip}>
                                    <Text style={styles.cuniTipText}>
                                        💡 Le sevrage (30 à 35 jours) sépare les lapereaux de la mère vers les cages d'engraissement.
                                        La mère revient au statut Repos.
                                    </Text>
                                </View>

                                <TextInput
                                    label="Date de sevrage (YYYY-MM-DD)"
                                    value={dateInput}
                                    onChangeText={setDateInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <TextInput
                                    label="Lapereaux sevrés vivants"
                                    value={sevresInput}
                                    onChangeText={setSevresInput}
                                    keyboardType="numeric"
                                    mode="outlined"
                                    style={styles.input}
                                />

                                {femelle.cycle?.nombre_vivants && sevresInput !== '' && (
                                    <HelperText type="info">
                                        Survie au nid : {sevresInput} / {femelle.cycle.nombre_vivants} petits
                                        ({Math.round(((parseInt(sevresInput) || 0) / femelle.cycle.nombre_vivants) * 100)}%)
                                    </HelperText>
                                )}

                                <TextInput
                                    label="Notes / Destination (Optionnel)"
                                    placeholder="Ex: Cages engraissement B1..."
                                    value={notesInput}
                                    onChangeText={setNotesInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <Button
                                    mode="contained"
                                    icon="check-decagram"
                                    onPress={handleWean}
                                    disabled={sevresInput === ''}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#388E3C' }]}
                                >
                                    Clôturer le Cycle (Sevrage Réussi)
                                </Button>
                            </View>
                        )}

                        {/* --- 6. MODAL INCIDENT / ECHEC --- */}
                        {activeModal === 'fail' && (
                            <View>
                                <Title style={[styles.modalTitle, { color: '#C62828' }]}>⚠️ Signaler un Problème</Title>
                                <Text style={styles.femelleRef}>Lapine : {femelle.numero}</Text>

                                <Text style={{ marginBottom: 8, fontWeight: 'bold' }}>Motif de l'arrêt du cycle :</Text>

                                <RadioButton.Group onValueChange={setMotifFail} value={motifFail}>
                                    <View style={styles.radioRow}>
                                        <RadioButton value="fausse_couche" />
                                        <Text>Fausse couche / avortement</Text>
                                    </View>
                                    <View style={styles.radioRow}>
                                        <RadioButton value="non_gestante" />
                                        <Text>Non gestante (palpation négative)</Text>
                                    </View>
                                    <View style={styles.radioRow}>
                                        <RadioButton value="mortalite_portee" />
                                        <Text>Mortalité complète de la portée</Text>
                                    </View>
                                    <View style={styles.radioRow}>
                                        <RadioButton value="abandon" />
                                        <Text>Abandon du nid / Pas de lait</Text>
                                    </View>
                                    <View style={styles.radioRow}>
                                        <RadioButton value="autre" />
                                        <Text>Autre incident</Text>
                                    </View>
                                </RadioButton.Group>

                                <TextInput
                                    label="Détails de l'incident (Optionnel)"
                                    value={notesInput}
                                    onChangeText={setNotesInput}
                                    mode="outlined"
                                    style={styles.input}
                                />

                                <Button
                                    mode="contained"
                                    onPress={handleFail}
                                    loading={loading}
                                    style={[styles.mainBtn, { backgroundColor: '#C62828' }]}
                                >
                                    Confirmer et Remettre en Repos
                                </Button>
                            </View>
                        )}

                        <Button
                            mode="text"
                            onPress={onClose}
                            style={{ marginTop: 8 }}
                        >
                            Fermer
                        </Button>

                    </ScrollView>
                </KeyboardAvoidingView>
            </Modal>
        </Portal>
    );
};

const styles = StyleSheet.create({
    modalCard: {
        padding: 20,
        margin: 16,
        borderRadius: 14,
        maxHeight: '90%',
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    femelleRef: {
        fontSize: 14,
        color: '#666',
        marginBottom: 12,
        fontWeight: '500',
    },
    cuniTip: {
        backgroundColor: '#E8F5E9',
        borderLeftWidth: 4,
        borderLeftColor: '#4CAF50',
        padding: 10,
        borderRadius: 6,
        marginBottom: 14,
    },
    cuniTipText: {
        fontSize: 12.5,
        color: '#1B5E20',
        lineHeight: 18,
    },
    input: {
        marginBottom: 12,
        backgroundColor: '#FFFFFF',
    },
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    radioRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 4,
    },
    mainBtn: {
        marginTop: 10,
        paddingVertical: 4,
        borderRadius: 8,
    },
});

export default ReproductionModals;

