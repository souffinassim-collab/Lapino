import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    FlatList,
    TextInput,
    Modal,
    Alert,
    RefreshControl
} from 'react-native';
import { useTheme, useFocusEffect } from '@react-navigation/native';
import { getAllVaccins, addVaccin, updateVaccin, deleteVaccin } from '../database/db';
import CustomButton from '../components/CustomButton';

const VaccinsScreen = () => {
    const { colors } = useTheme();
    const [vaccins, setVaccins] = useState([]);
    const [modalVisible, setModalVisible] = useState(false);
    const [editingVaccin, setEditingVaccin] = useState(null);
    const [nom, setNom] = useState('');
    const [dureeJours, setDureeJours] = useState('');
    const [refreshing, setRefreshing] = useState(false);

    const loadVaccins = async () => {
        try {
            const data = await getAllVaccins();
            setVaccins(data);
        } catch (error) {
            console.error('Erreur chargement vaccins:', error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadVaccins();
        }, [])
    );

    const onRefresh = async () => {
        setRefreshing(true);
        await loadVaccins();
        setRefreshing(false);
    };

    const openAddModal = () => {
        setEditingVaccin(null);
        setNom('');
        setDureeJours('');
        setModalVisible(true);
    };

    const openEditModal = (vaccin) => {
        setEditingVaccin(vaccin);
        setNom(vaccin.nom);
        setDureeJours(vaccin.duree_jours.toString());
        setModalVisible(true);
    };

    const handleSave = async () => {
        if (!nom.trim()) {
            Alert.alert('Erreur', 'Le nom du vaccin est obligatoire');
            return;
        }

        const duree = parseInt(dureeJours);
        if (isNaN(duree) || duree <= 0) {
            Alert.alert('Erreur', 'La durée doit être un nombre positif');
            return;
        }

        try {
            if (editingVaccin) {
                await updateVaccin(editingVaccin.id, nom, duree);
            } else {
                await addVaccin(nom, duree);
            }
            setModalVisible(false);
            setNom('');
            setDureeJours('');
            setEditingVaccin(null);
            await loadVaccins();
        } catch (error) {
            console.error('Erreur sauvegarde vaccin:', error);
            Alert.alert('Erreur', 'Impossible de sauvegarder le vaccin');
        }
    };

    const handleDelete = (id, nom) => {
        Alert.alert(
            'Confirmer la suppression',
            `Supprimer le vaccin "${nom}" ?`,
            [
                { text: 'Annuler', style: 'cancel' },
                {
                    text: 'Supprimer',
                    style: 'destructive',
                    onPress: async () => {
                        try {
                            await deleteVaccin(id);
                            await loadVaccins();
                        } catch (error) {
                            Alert.alert('Erreur', 'Impossible de supprimer');
                        }
                    },
                },
            ]
        );
    };

    const renderVaccin = ({ item }) => {
        return (
            <View style={[styles.item, { backgroundColor: colors.surface }]}>
                <View style={styles.itemContent}>
                    <Text style={[styles.name, { color: colors.text }]}>
                        💉 {item.nom}
                    </Text>
                    <Text style={[styles.duration, { color: colors.textSecondary }]}>
                        Durée: {item.duree_jours} jours
                    </Text>
                </View>
                <View style={styles.actions}>
                    <CustomButton
                        title="✏"
                        variant="secondary"
                        onPress={() => openEditModal(item)}
                        style={styles.actionBtn}
                    />
                    <CustomButton
                        title="✕"
                        variant="danger"
                        onPress={() => handleDelete(item.id, item.nom)}
                        style={styles.actionBtn}
                    />
                </View>
            </View>
        );
    };

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <FlatList
                data={vaccins}
                renderItem={renderVaccin}
                keyExtractor={(item) => item.id.toString()}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyState}>
                        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                            Aucun vaccin enregistré
                        </Text>
                    </View>
                }
                contentContainerStyle={styles.listContent}
            />

            <CustomButton
                title="+ Ajouter un Vaccin"
                onPress={openAddModal}
                style={styles.addButton}
            />

            {/* Modal ajout/édition vaccin */}
            <Modal
                visible={modalVisible}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={[styles.modalContent, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.modalTitle, { color: colors.text }]}>
                            {editingVaccin ? 'Modifier le Vaccin' : 'Nouveau Vaccin'}
                        </Text>

                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Nom du vaccin:
                        </Text>
                        <TextInput
                            style={[styles.input, {
                                borderColor: colors.border,
                                color: colors.text
                            }]}
                            value={nom}
                            onChangeText={setNom}
                            placeholder="Ex: Myxomatose"
                            placeholderTextColor={colors.textSecondary}
                        />

                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Durée (jours):
                        </Text>
                        <TextInput
                            style={[styles.input, {
                                borderColor: colors.border,
                                color: colors.text
                            }]}
                            value={dureeJours}
                            onChangeText={setDureeJours}
                            placeholder="Ex: 180"
                            placeholderTextColor={colors.textSecondary}
                            keyboardType="numeric"
                        />

                        <View style={styles.modalButtons}>
                            <CustomButton
                                title="Annuler"
                                variant="secondary"
                                onPress={() => {
                                    setModalVisible(false);
                                    setNom('');
                                    setDureeJours('');
                                    setEditingVaccin(null);
                                }}
                                style={styles.modalButton}
                            />
                            <CustomButton
                                title={editingVaccin ? 'Modifier' : 'Ajouter'}
                                onPress={handleSave}
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
    listContent: {
        padding: 16,
        paddingBottom: 80,
    },
    item: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
        padding: 16,
        borderRadius: 8,
        elevation: 2,
    },
    itemContent: {
        flex: 1,
    },
    name: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    duration: {
        fontSize: 14,
    },
    actions: {
        flexDirection: 'row',
        gap: 8,
    },
    actionBtn: {
        width: 44,
        height: 44,
        minHeight: 44,
        paddingHorizontal: 0,
    },
    emptyState: {
        alignItems: 'center',
        justifyContent: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 16,
    },
    addButton: {
        position: 'absolute',
        bottom: 20,
        left: 20,
        right: 20,
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
    label: {
        fontSize: 16,
        fontWeight: '500',
        marginBottom: 8,
    },
    input: {
        borderWidth: 1,
        borderRadius: 8,
        paddingHorizontal: 12,
        paddingVertical: 12,
        fontSize: 16,
        marginBottom: 16,
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 8,
    },
    modalButton: {
        flex: 1,
        marginHorizontal: 4,
    },
});

export default VaccinsScreen;
