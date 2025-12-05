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
import { getAllClapets, addClapet, deleteClapet } from '../database/db';
import CustomButton from '../components/CustomButton';

const ClapletsScreen = () => {
    const { colors } = useTheme();
    const [clapets, setClapets] = useState([]);
    const [modalVisible, setModalVisible] = useState(false);
    const [newNumero, setNewNumero] = useState('');
    const [refreshing, setRefreshing] = useState(false);

    const loadClapets = async () => {
        try {
            const data = await getAllClapets();
            setClapets(data);
        } catch (error) {
            console.error('Erreur chargement clapets:', error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadClapets();
        }, [])
    );

    const onRefresh = async () => {
        setRefreshing(true);
        await loadClapets();
        setRefreshing(false);
    };

    const handleAdd = async () => {
        if (!newNumero.trim()) {
            Alert.alert('Erreur', 'Le numéro est obligatoire');
            return;
        }

        try {
            await addClapet(newNumero);
            setModalVisible(false);
            setNewNumero('');
            await loadClapets();
        } catch (error) {
            console.error('Erreur ajout clapet:', error);
            Alert.alert('Erreur', 'Impossible d\'ajouter le clapet (numéro déjà utilisé?)');
        }
    };

    const handleDelete = (id, numero) => {
        Alert.alert(
            'Confirmer la suppression',
            `Supprimer le clapet ${numero} ?`,
            [
                { text: 'Annuler', style: 'cancel' },
                {
                    text: 'Supprimer',
                    style: 'destructive',
                    onPress: async () => {
                        try {
                            await deleteClapet(id);
                            await loadClapets();
                        } catch (error) {
                            Alert.alert('Erreur', 'Impossible de supprimer');
                        }
                    },
                },
            ]
        );
    };

    const renderClapet = ({ item }) => {
        const isOccupied = !!item.femelle_numero;

        return (
            <View style={[styles.item, { backgroundColor: colors.surface }]}>
                <View style={styles.itemContent}>
                    <Text style={[styles.numero, { color: colors.text }]}>
                        🏠 {item.numero}
                    </Text>
                    <View style={styles.statusContainer}>
                        <View style={[
                            styles.statusBadge,
                            { backgroundColor: isOccupied ? colors.success : colors.border }
                        ]}>
                            <Text style={styles.statusText}>
                                {isOccupied ? 'Occupé' : 'Vide'}
                            </Text>
                        </View>
                    </View>
                    {isOccupied && (
                        <Text style={[styles.femelle, { color: colors.textSecondary }]}>
                            → Femelle: {item.femelle_numero}
                        </Text>
                    )}
                </View>
                <CustomButton
                    title="✕"
                    variant="danger"
                    onPress={() => handleDelete(item.id, item.numero)}
                    style={styles.deleteBtn}
                />
            </View>
        );
    };

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <FlatList
                data={clapets}
                renderItem={renderClapet}
                keyExtractor={(item) => item.id.toString()}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyState}>
                        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
                            Aucun clapet enregistré
                        </Text>
                    </View>
                }
                contentContainerStyle={styles.listContent}
            />

            <CustomButton
                title="+ Ajouter un Clapet"
                onPress={() => setModalVisible(true)}
                style={styles.addButton}
            />

            {/* Modal ajout clapet */}
            <Modal
                visible={modalVisible}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={[styles.modalContent, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.modalTitle, { color: colors.text }]}>
                            Nouveau Clapet
                        </Text>

                        <Text style={[styles.label, { color: colors.textSecondary }]}>
                            Numéro:
                        </Text>
                        <TextInput
                            style={[styles.input, {
                                borderColor: colors.border,
                                color: colors.text
                            }]}
                            value={newNumero}
                            onChangeText={setNewNumero}
                            placeholder="Ex: C001"
                            placeholderTextColor={colors.textSecondary}
                        />

                        <View style={styles.modalButtons}>
                            <CustomButton
                                title="Annuler"
                                variant="secondary"
                                onPress={() => {
                                    setModalVisible(false);
                                    setNewNumero('');
                                }}
                                style={styles.modalButton}
                            />
                            <CustomButton
                                title="Ajouter"
                                onPress={handleAdd}
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
    numero: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 8,
    },
    statusContainer: {
        marginBottom: 4,
    },
    statusBadge: {
        alignSelf: 'flex-start',
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 12,
    },
    statusText: {
        color: '#FFFFFF',
        fontSize: 12,
        fontWeight: '600',
    },
    femelle: {
        fontSize: 14,
        marginTop: 4,
    },
    deleteBtn: {
        width: 44,
        height: 44,
        minHeight: 44,
        paddingHorizontal: 0,
        marginLeft: 8,
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
        marginBottom: 20,
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    modalButton: {
        flex: 1,
        marginHorizontal: 4,
    },
});

export default ClapletsScreen;
