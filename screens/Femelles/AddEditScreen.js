import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    Alert,
    TouchableOpacity
} from 'react-native';
import { useTheme } from '@react-navigation/native';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import {
    addFemelle,
    updateFemelle,
    getFemelleById,
    getAllClapets
} from '../../database/db';
import { formatDateISO, formatDateFR } from '../../utils/dateUtils';
import { STATUTS_FEMELLE } from '../../utils/constants';
import CustomButton from '../../components/CustomButton';
import CustomInput from '../../components/CustomInput';

const AddEditScreen = ({ route, navigation }) => {
    const { femelleId } = route.params || {};
    const isEdit = !!femelleId;
    const { colors } = useTheme();

    const [numero, setNumero] = useState('');
    const [clapatId, setClapatId] = useState(null);
    const [dateNaissance, setDateNaissance] = useState(new Date());
    const [statut, setStatut] = useState('vivante');
    const [clapets, setClapets] = useState([]);
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadClapets();
        if (isEdit) {
            loadFemelle();
        }
    }, []);

    const loadClapets = async () => {
        try {
            const data = await getAllClapets();
            setClapets(data);
        } catch (error) {
            console.error('Erreur chargement clapets:', error);
        }
    };

    const loadFemelle = async () => {
        try {
            const data = await getFemelleById(femelleId);
            setNumero(data.numero);
            setClapatId(data.clapet_id);
            if (data.date_naissance) {
                setDateNaissance(new Date(data.date_naissance));
            }
            setStatut(data.statut);
        } catch (error) {
            console.error('Erreur chargement femelle:', error);
        }
    };

    const handleSave = async () => {
        if (!numero.trim()) {
            Alert.alert('Erreur', 'Le numéro est obligatoire');
            return;
        }

        setLoading(true);
        try {
            if (isEdit) {
                await updateFemelle(
                    femelleId,
                    numero,
                    clapatId,
                    formatDateISO(dateNaissance),
                    statut
                );
            } else {
                await addFemelle(
                    numero,
                    clapatId,
                    formatDateISO(dateNaissance),
                    statut
                );
            }
            navigation.goBack();
        } catch (error) {
            console.error('Erreur sauvegarde:', error);
            Alert.alert('Erreur', 'Impossible de sauvegarder');
        } finally {
            setLoading(false);
        }
    };

    return (
        <ScrollView style={[styles.container, { backgroundColor: colors.background }]}>
            <View style={styles.form}>

                <CustomInput
                    label="Numéro *"
                    value={numero}
                    onChangeText={setNumero}
                    placeholder="Ex: F001"
                />

                <Text style={[styles.label, { color: colors.textSecondary }]}>
                    Clapet:
                </Text>
                <View style={[styles.pickerContainer, {
                    borderColor: colors.border,
                    backgroundColor: colors.surface
                }]}>
                    <Picker
                        selectedValue={clapatId}
                        onValueChange={setClapatId}
                        style={{ color: colors.text }}
                    >
                        <Picker.Item label="Aucun" value={null} />
                        {clapets.map((c) => (
                            <Picker.Item
                                key={c.id}
                                label={c.numero}
                                value={c.id}
                            />
                        ))}
                    </Picker>
                </View>

                <Text style={[styles.label, { color: colors.textSecondary }]}>
                    Date de naissance:
                </Text>
                <TouchableOpacity
                    style={[styles.dateButton, {
                        borderColor: colors.border,
                        backgroundColor: colors.surface
                    }]}
                    onPress={() => setShowDatePicker(true)}
                >
                    <Text style={{ color: colors.text }}>
                        {formatDateFR(formatDateISO(dateNaissance))}
                    </Text>
                </TouchableOpacity>

                {showDatePicker && (
                    <DateTimePicker
                        value={dateNaissance}
                        mode="date"
                        display="default"
                        onChange={(event, selectedDate) => {
                            setShowDatePicker(false);
                            if (selectedDate) {
                                setDateNaissance(selectedDate);
                            }
                        }}
                    />
                )}

                <Text style={[styles.label, { color: colors.textSecondary }]}>
                    Statut:
                </Text>
                <View style={[styles.pickerContainer, {
                    borderColor: colors.border,
                    backgroundColor: colors.surface
                }]}>
                    <Picker
                        selectedValue={statut}
                        onValueChange={setStatut}
                        style={{ color: colors.text }}
                    >
                        {STATUTS_FEMELLE.map((s) => (
                            <Picker.Item
                                key={s.value}
                                label={s.label}
                                value={s.value}
                            />
                        ))}
                    </Picker>
                </View>

                <CustomButton
                    title={isEdit ? 'Modifier' : 'Ajouter'}
                    onPress={handleSave}
                    loading={loading}
                    style={styles.saveButton}
                />
            </View>
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    form: {
        padding: 16,
    },
    label: {
        fontSize: 16,
        fontWeight: '500',
        marginBottom: 8,
        marginTop: 12,
    },
    pickerContainer: {
        borderWidth: 1,
        borderRadius: 8,
        marginBottom: 16,
    },
    dateButton: {
        borderWidth: 1,
        borderRadius: 8,
        padding: 12,
        marginBottom: 16,
    },
    saveButton: {
        marginTop: 24,
    },
});

export default AddEditScreen;
