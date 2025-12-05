import React, { useState, useCallback } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import { FAB, Card, Text, Title, Paragraph, Button, Portal, Modal, TextInput, useTheme, Chip, IconButton } from 'react-native-paper';
import { useFocusEffect } from '@react-navigation/native';
import { getAllAliments, addAliment, updateAliment, deleteAliment } from '../database/db';

const AlimentsScreen = () => {
    const { colors } = useTheme();
    const [aliments, setAliments] = useState([]);
    const [refreshing, setRefreshing] = useState(false);

    // Modal state
    const [visible, setVisible] = useState(false);
    const [editingAliment, setEditingAliment] = useState(null);
    const [nom, setNom] = useState('');
    const [stockKg, setStockKg] = useState('');
    const [consommationG, setConsommationG] = useState('150');

    const loadData = async () => {
        setRefreshing(true);
        try {
            const data = await getAllAliments();
            setAliments(data);
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

    const handleAdd = () => {
        setEditingAliment(null);
        setNom('');
        setStockKg('');
        setConsommationG('150');
        setVisible(true);
    };

    const handleEdit = (aliment) => {
        setEditingAliment(aliment);
        setNom(aliment.nom);
        setStockKg(aliment.stock_kg.toString());
        setConsommationG(aliment.consommation_g.toString());
        setVisible(true);
    };

    const handleDelete = async (id) => {
        try {
            await deleteAliment(id);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleSave = async () => {
        if (!nom || !stockKg || !consommationG) return;

        try {
            if (editingAliment) {
                await updateAliment(editingAliment.id, nom, parseFloat(stockKg), parseInt(consommationG));
            } else {
                await addAliment(nom, parseFloat(stockKg), parseInt(consommationG));
            }
            setVisible(false);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const renderItem = ({ item }) => {
        // Déterminer la couleur de l'alerte
        let statusColor = colors.primary; // Vert par défaut
        let statusIcon = 'check-circle';

        if (item.jours_restants <= 3) {
            statusColor = colors.error; // Rouge critique
            statusIcon = 'alert-circle';
        } else if (item.jours_restants <= 7) {
            statusColor = 'orange'; // Orange attention
            statusIcon = 'alert';
        }

        return (
            <Card style={styles.card}>
                <Card.Content>
                    <View style={styles.cardHeader}>
                        <View style={styles.titleContainer}>
                            <Title>{item.nom}</Title>
                            <Chip
                                icon={statusIcon}
                                style={{ backgroundColor: statusColor + '20' }}
                                textStyle={{ color: statusColor }}
                            >
                                {item.jours_restants === 999 ? 'Infini' : `${item.jours_restants} jours`}
                            </Chip>
                        </View>
                        <View style={styles.actions}>
                            <IconButton icon="pencil" size={20} onPress={() => handleEdit(item)} />
                            <IconButton icon="delete" size={20} color={colors.error} onPress={() => handleDelete(item.id)} />
                        </View>
                    </View>

                    <Divider style={styles.divider} />

                    <View style={styles.statsRow}>
                        <View style={styles.stat}>
                            <Text style={styles.statLabel}>Stock Actuel</Text>
                            <Text style={styles.statValue}>{item.stock_kg} kg</Text>
                        </View>
                        <View style={styles.stat}>
                            <Text style={styles.statLabel}>Conso/Lapin</Text>
                            <Text style={styles.statValue}>{item.consommation_g} g/j</Text>
                        </View>
                        <View style={styles.stat}>
                            <Text style={styles.statLabel}>Conso Totale</Text>
                            <Text style={styles.statValue}>{item.conso_jour_kg} kg/j</Text>
                        </View>
                    </View>
                </Card.Content>
            </Card>
        );
    };

    return (
        <View style={styles.container}>
            <FlatList
                data={aliments}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.list}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={loadData} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyContainer}>
                        <Text style={styles.emptyText}>Aucun aliment configuré</Text>
                        <Text style={styles.emptySubtext}>Appuyez sur + pour ajouter un stock</Text>
                    </View>
                }
            />

            <FAB
                style={[styles.fab, { backgroundColor: colors.accent }]}
                icon="plus"
                onPress={handleAdd}
                label="Ajouter Stock"
            />

            <Portal>
                <Modal visible={visible} onDismiss={() => setVisible(false)} contentContainerStyle={styles.modal}>
                    <Title>{editingAliment ? 'Modifier Stock' : 'Nouveau Stock'}</Title>

                    <TextInput
                        label="Nom de l'aliment (ex: Granulés, Foin)"
                        value={nom}
                        onChangeText={setNom}
                        style={styles.input}
                        mode="outlined"
                    />

                    <TextInput
                        label="Stock actuel (kg)"
                        value={stockKg}
                        onChangeText={setStockKg}
                        keyboardType="numeric"
                        style={styles.input}
                        mode="outlined"
                        right={<TextInput.Affix text="kg" />}
                    />

                    <TextInput
                        label="Consommation par lapin (g/jour)"
                        value={consommationG}
                        onChangeText={setConsommationG}
                        keyboardType="numeric"
                        style={styles.input}
                        mode="outlined"
                        right={<TextInput.Affix text="g" />}
                    />

                    <View style={styles.modalButtons}>
                        <Button onPress={() => setVisible(false)} style={styles.button}>Annuler</Button>
                        <Button mode="contained" onPress={handleSave} style={styles.button}>Enregistrer</Button>
                    </View>
                </Modal>
            </Portal>
        </View>
    );
};

// Simple Divider component since we didn't import it
const Divider = ({ style }) => <View style={[{ height: 1, backgroundColor: '#e0e0e0' }, style]} />;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    list: {
        padding: 16,
        paddingBottom: 80,
    },
    card: {
        marginBottom: 16,
        elevation: 2,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    titleContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginRight: 8,
    },
    actions: {
        flexDirection: 'row',
    },
    divider: {
        marginVertical: 12,
    },
    statsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    stat: {
        alignItems: 'center',
        flex: 1,
    },
    statLabel: {
        fontSize: 12,
        color: '#757575',
    },
    statValue: {
        fontWeight: 'bold',
        fontSize: 16,
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
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'flex-end',
        marginTop: 16,
    },
    button: {
        marginLeft: 8,
    },
    emptyContainer: {
        alignItems: 'center',
        marginTop: 50,
    },
    emptyText: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#757575',
    },
    emptySubtext: {
        color: '#9e9e9e',
        marginTop: 8,
    }
});

export default AlimentsScreen;
