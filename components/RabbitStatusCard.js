import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, ProgressBar, Button, Chip, IconButton, useTheme } from 'react-native-paper';

// Helpers pour calculer les jours restants et le %
const calculateProgress = (startDate, endDate) => {
    if (!startDate || !endDate) return 0;

    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();

    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;

    if (now < start) return 0;
    if (now > end) return 1;

    const total = end - start;
    const current = now - start;

    return Math.max(0, Math.min(1, current / total));
};

const getDaysRemaining = (targetDate) => {
    if (!targetDate) return 0;

    const target = new Date(targetDate);
    if (isNaN(target.getTime())) return 0;

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
        icon: 'sleep',
        color: colors.disabled,
        progress: 0,
        detail: 'Pas de cycle en cours',
        actionLabel: 'Déclarer Saillie',
        actionIcon: 'heart'
    };

    if (cycle) {
        if (cycle.statut === 'saillie' || cycle.statut === 'gestante') {
            const daysLeft = getDaysRemaining(cycle.date_mise_bas_prevue);
            const progress = calculateProgress(cycle.date_saillie, cycle.date_mise_bas_prevue);

            statusConfig = {
                label: 'Gestante',
                icon: 'rabbit',
                color: '#2196F3', // Bleu
                progress: progress,
                detail: `Gestation confirmée le ${cycle.date_verification || '?'}
${daysLeft > 0 ? `Mise bas dans ${daysLeft} jours` : 'Mise bas imminente !'}`,
                actionLabel: 'Déclarer Naissance',
                actionIcon: 'baby-face-outline'
            };

            if (cycle.statut === 'saillie') {
                statusConfig.label = 'Saillie';
                statusConfig.icon = 'heart';
                statusConfig.color = '#E91E63'; // Rose
                statusConfig.actionLabel = 'Vérifier Gestation';
                statusConfig.actionIcon = 'stethoscope';
            }

        } else if (cycle.statut === 'allaitante') {
            const daysLeft = getDaysRemaining(cycle.date_sevrage_prevue);
            const progress = calculateProgress(cycle.date_mise_bas_reelle, cycle.date_sevrage_prevue);

            // Calculate elapsed days for segmentation
            const birth = new Date(cycle.date_mise_bas_reelle);
            const now = new Date();
            const elapsed = Math.max(0, Math.floor((now - birth) / (1000 * 60 * 60 * 24)));

            statusConfig = {
                label: 'Allaitante',
                icon: 'baby-bottle',
                color: '#4CAF50', // Vert
                progress: progress,
                daysElapsed: elapsed,
                detail: `Jour ${elapsed}/35 - ${daysLeft}j restants (${cycle.nombre_vivants} petits 🐰)`,
                actionLabel: 'Sevrage / Fin',
                actionIcon: 'check-bold'
            };
        }
    }

    return (
        <Card style={[styles.card, { backgroundColor: colors.surface }]}>
            <Card.Content>
                <View style={styles.header}>
                    <View>
                        <Text style={styles.name}>{femelle.numero}</Text>
                        <Text style={styles.cage}>Loge: {femelle.clapet_numero || 'Aucune'}</Text>
                    </View>
                    <Chip
                        icon={statusConfig.icon}
                        style={{ backgroundColor: (statusConfig.color || colors.disabled) + '20' }}
                        textStyle={{ color: statusConfig.color || colors.disabled }}
                    >
                        {statusConfig.label}
                    </Chip>
                </View>

                {cycle && (
                    <View style={styles.progressContainer}>
                        {cycle.statut === 'allaitante' ? (
                            <View>
                                {/* Informations textuelles */}
                                <Text style={styles.detailText}>{statusConfig.detail}</Text>
                                <View style={styles.segmentsRow}>
                                    {/* Segment 1: 0-11j (Mise au mâle) */}
                                    <View style={styles.segmentContainer}>
                                        <View style={[styles.segment, {
                                            backgroundColor: getSegmentColor(statusConfig.daysElapsed, 0, 11, colors)
                                        }]} />
                                        <Text style={styles.segmentLabel}>Mise mâle (11j)</Text>
                                    </View>

                                    {/* Separator */}
                                    <View style={styles.segmentSeparator} />

                                    {/* Segment 2: 11-21j (Vérification) */}
                                    <View style={styles.segmentContainer}>
                                        <View style={[styles.segment, {
                                            backgroundColor: getSegmentColor(statusConfig.daysElapsed, 11, 21, colors)
                                        }]} />
                                        <Text style={styles.segmentLabel}>Vérif (21j)</Text>
                                    </View>

                                    {/* Separator */}
                                    <View style={styles.segmentSeparator} />

                                    {/* Segment 3: 21-35j (Sevrage) */}
                                    <View style={styles.segmentContainer}>
                                        <View style={[styles.segment, {
                                            backgroundColor: getSegmentColor(statusConfig.daysElapsed, 21, 35, colors)
                                        }]} />
                                        <Text style={styles.segmentLabel}>Sevrage (35j)</Text>
                                    </View>
                                </View>
                            </View>
                        ) : (
                            <>
                                <View style={styles.progressLabelRow}>
                                    <Text style={styles.detailText}>{statusConfig.detail}</Text>
                                    <Text style={styles.percentText}>{Math.round(statusConfig.progress * 100)}%</Text>
                                </View>
                                <ProgressBar
                                    progress={statusConfig.progress}
                                    color={statusConfig.color || colors.disabled}
                                    style={styles.progressBar}
                                />
                            </>
                        )}
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

            </Card.Content >
        </Card >
    );
};

const styles = StyleSheet.create({
    card: {
        marginBottom: 12,
        elevation: 2,
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
        fontSize: 14,
        opacity: 0.7,
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
    },
    segmentsRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 8,
        height: 30, // Adjust height for labels
    },
    segmentContainer: {
        flex: 1,
        alignItems: 'center',
    },
    segment: {
        height: 8,
        width: '100%',
        borderRadius: 4,
        marginBottom: 4,
    },
    segmentSeparator: {
        width: 4,
    },
    segmentLabel: {
        fontSize: 10,
        opacity: 0.7,
        textAlign: 'center',
    },
});

// Helper for segment color
const getSegmentColor = (elapsed, startDay, endDay, colors) => {
    if (elapsed >= endDay) return colors.primary; // Completed (Green/Blue usually) or Primary
    if (elapsed >= startDay) return '#FF9800';   // In progress (Orange)
    return '#E0E0E0';                            // Future (Grey)
};

export default RabbitStatusCard;
