import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, ProgressBar, Button, Chip, IconButton, useTheme } from 'react-native-paper';

// Helpers pour calculer les jours restants et le %
const calculateProgress = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();

    if (now < start) return 0;
    if (now > end) return 1;

    const total = end - start;
    const current = now - start;

    return current / total;
};

const getDaysRemaining = (targetDate) => {
    const target = new Date(targetDate);
    const now = new Date();
    const diffTime = target - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
};

const RabbitStatusCard = ({ femelle, onAction }) => {
    const { colors } = useTheme();
    const { cycle } = femelle;

    // Déterminer le statut, couleur et icône
    let statusConfig = {
        label: 'Repos',
        icon: 'paw',
        color: colors.disabled,
        progress: 0,
        detail: 'Pas de cycle en cours',
        actionLabel: 'Déclarer Saillie',
        actionIcon: 'heart'
    };

    if (cycle) {
        return (
            <Card style={styles.card}>
                <Card.Content>
                    <View style={styles.header}>
                        <View>
                            <Text style={styles.name}>{femelle.numero}</Text>
                            <Text style={styles.cage}>Loge: {femelle.clapet_numero || 'Aucune'}</Text>
                        </View>
                        <Chip
                            icon={statusConfig.icon}
                            style={{ backgroundColor: statusConfig.color + '20' }}
                            textStyle={{ color: statusConfig.color }}
                        >
                            {statusConfig.label}
                        </Chip>
                    </View>

                    {cycle && (
                        <View style={styles.progressContainer}>
                            <View style={styles.progressLabelRow}>
                                <Text style={styles.detailText}>{statusConfig.detail}</Text>
                                <Text style={styles.percentText}>{Math.round(statusConfig.progress * 100)}%</Text>
                            </View>
                            <ProgressBar
                                progress={statusConfig.progress}
                                color={statusConfig.color}
                                style={styles.progressBar}
                            />
                        </View>
                    )}

                    <View style={styles.actions}>
                        {/* Botuon d'action principale du cycle */}
                        <Button
                            mode="contained"
                            compact
                            icon={statusConfig.actionIcon}
                            onPress={() => onAction(femelle, 'primary')}
                            style={[styles.actionBtn, { backgroundColor: statusConfig.color === colors.disabled ? colors.primary : statusConfig.color }]}
                        >
                            {statusConfig.actionLabel}
                        </Button>

                        {/* Option d'échec si cycle en cours */}
                        {cycle && (
                            <IconButton
                                icon="close-circle-outline"
                                size={24}
                                color={colors.error}
                                onPress={() => onAction(femelle, 'fail')}
                            />
                        )}
                    </View>

                </Card.Content>
            </Card>
        );
    };

    const styles = StyleSheet.create({
        card: {
            marginBottom: 12,
            elevation: 2,
            backgroundColor: 'white',
        },
        header: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            marginBottom: 8,
        },
        name: {
            fontSize: 18,
            fontWeight: 'bold',
        },
        cage: {
            color: '#757575',
            fontSize: 14,
        },
        progressContainer: {
            marginVertical: 12,
        },
        progressLabelRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginBottom: 6,
        },
        detailText: {
            fontSize: 14,
            fontWeight: '500',
        },
        percentText: {
            fontSize: 12,
            color: '#757575',
        },
        progressBar: {
            height: 8,
            borderRadius: 4,
        },
        actions: {
            flexDirection: 'row',
            justifyContent: 'flex-end',
            alignItems: 'center',
            marginTop: 4,
        },
        actionBtn: {
            marginRight: 8,
        }
    });

    export default RabbitStatusCard;
