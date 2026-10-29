import React, { useState, useCallback } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import { FAB, useTheme, Searchbar, Chip } from 'react-native-paper';
import { useFocusEffect } from '@react-navigation/native';
import RabbitStatusCard from '../../components/RabbitStatusCard';
import ReproductionModals from '../../components/ReproductionModals';
import { getFemellesWithStatus } from '../../database/db';

const FemellesListScreen = ({ navigation }) => {
    const { colors } = useTheme();
    const [femelles, setFemelles] = useState([]);
    const [filteredFemelles, setFilteredFemelles] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('all'); // 'all', 'repos', 'saillie', 'gestante', 'allaitante'
    const [refreshing, setRefreshing] = useState(false);

    // Modal reproduction
    const [activeModal, setActiveModal] = useState(null);
    const [selectedFemelle, setSelectedFemelle] = useState(null);

    const loadData = async () => {
        setRefreshing(true);
        try {
            const data = await getFemellesWithStatus();
            setFemelles(data);
            applyFilter(data, searchQuery, statusFilter);
        } catch (error) {
            console.error('Erreur chargement liste femelles:', error);
        } finally {
            setRefreshing(false);
        }
    };

    useFocusEffect(
        useCallback(() => {
            loadData();
        }, [])
    );

    const applyFilter = (data, query, filter) => {
        let result = data;
        if (query.trim()) {
            const q = query.toLowerCase();
            result = result.filter(f =>
                (f.numero && f.numero.toLowerCase().includes(q)) ||
                (f.clapet_numero && f.clapet_numero.toLowerCase().includes(q))
            );
        }
        if (filter !== 'all') {
            if (filter === 'repos') {
                result = result.filter(f => !f.cycle);
            } else {
                result = result.filter(f => f.cycle && f.cycle.statut === filter);
            }
        }
        setFilteredFemelles(result);
    };

    const handleSearch = (text) => {
        setSearchQuery(text);
        applyFilter(femelles, text, statusFilter);
    };

    const handleFilterChange = (filter) => {
        setStatusFilter(filter);
        applyFilter(femelles, searchQuery, filter);
    };

    const handleAction = (femelle, actionType) => {
        setSelectedFemelle(femelle);
        setActiveModal(actionType);
    };

    const handleCardPress = (femelle) => {
        navigation.navigate('FemelleDetail', { femelleId: femelle.id });
    };

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            {/* Barre de recherche */}
            <View style={styles.searchContainer}>
                <Searchbar
                    placeholder="Rechercher par nom ou clapet..."
                    onChangeText={handleSearch}
                    value={searchQuery}
                    style={styles.searchBar}
                    inputStyle={{ fontSize: 14 }}
                />
            </View>

            {/* Filtres par cycle reproductif */}
            <View style={styles.filterBar}>
                <Chip
                    selected={statusFilter === 'all'}
                    onPress={() => handleFilterChange('all')}
                    style={styles.filterChip}
                >
                    Toutes ({femelles.length})
                </Chip>
                <Chip
                    selected={statusFilter === 'repos'}
                    onPress={() => handleFilterChange('repos')}
                    style={styles.filterChip}
                >
                    Repos
                </Chip>
                <Chip
                    selected={statusFilter === 'saillie'}
                    onPress={() => handleFilterChange('saillie')}
                    style={styles.filterChip}
                >
                    Saillie
                </Chip>
                <Chip
                    selected={statusFilter === 'gestante'}
                    onPress={() => handleFilterChange('gestante')}
                    style={styles.filterChip}
                >
                    Gestante
                </Chip>
                <Chip
                    selected={statusFilter === 'allaitante'}
                    onPress={() => handleFilterChange('allaitante')}
                    style={styles.filterChip}
                >
                    Allaitante
                </Chip>
            </View>

            {/* Liste fluide optimisée pour tout smartphone Android */}
            <FlatList
                data={filteredFemelles}
                renderItem={({ item }) => (
                    <RabbitStatusCard
                        femelle={item}
                        onAction={handleAction}
                        onPressCard={handleCardPress}
                    />
                )}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.list}
                initialNumToRender={8}
                maxToRenderPerBatch={10}
                windowSize={5}
                removeClippedSubviews={true}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={loadData} />
                }
            />

            <FAB
                style={[styles.fab, { backgroundColor: colors.primary }]}
                icon="plus"
                color="#FFFFFF"
                onPress={() => navigation.navigate('FemelleAddEdit')}
            />

            {/* Modals de cycle reproductif */}
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
    container: {
        flex: 1,
    },
    searchContainer: {
        paddingHorizontal: 16,
        paddingTop: 10,
        paddingBottom: 4,
    },
    searchBar: {
        borderRadius: 10,
        elevation: 1,
        height: 44,
    },
    filterBar: {
        flexDirection: 'row',
        paddingHorizontal: 16,
        paddingVertical: 8,
        gap: 6,
        flexWrap: 'wrap',
    },
    filterChip: {
        height: 32,
    },
    list: {
        padding: 16,
        paddingBottom: 85,
    },
    fab: {
        position: 'absolute',
        margin: 16,
        right: 0,
        bottom: 0,
    },
});

export default FemellesListScreen;
