import React from 'react';
import { View, StyleSheet, TouchableOpacity } from 'react-native';
import { Card, Text, ProgressBar, Button, Chip, IconButton, useTheme } from 'react-native-paper';
import { formatDateFR, daysUntil } from '../utils/dateUtils';

const calculateProgress = (startDate, endDate) => {
    if (!startDate || !endDate) return 0;
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();

    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;
    if (now <= start) return 0;
    if (now >= end) return 1;

    const total = end - start;
    const current = now - start;
    return Math.max(0, Math.min(1, current / total));
};

const getDaysElapsed = (startDate) => {
    if (!startDate) return 0;
    const start = new Date(startDate);
    if (isNaN(start.getTime())) return 0;
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    start.setHours(0, 0, 0, 0);
    return Math.max(0, Math.floor((now - start) / (1000 * 60 * 60 * 24)));
};

const RabbitStatusCard = ({ femelle, onAction, onPressCard }) => {
    const { colors } = useTheme();
    const { cycle } = femelle;

    let statusConfig = {
        label: 'Repos',
        icon: 'sleep',
        color: colors.disabled || '#9E9E9E',
        bgColor: '#E0E0E0',
        progress: 0,
        detail: 'Prête pour une nouvelle saillie',
        actionLabel: 'Mettre au mâle',
        actionIcon: 'heart-outline',
        actionType: 'start'
    };

    let alertBanner = null;
    let quickNestButton = null;

    if (cycle) {
        if (cycle.statut === 'saillie') {
            const elapsed = getDaysElapsed(cycle.date_saillie);
            const isPalpationDue = elapsed >= 10;

            statusConfig = {
                label: `Saillie (J${elapsed})`,
                icon: 'heart',
                color: '#E91E63',
                bgColor: '#FCE4EC',
                progress: Math.min(1, elapsed / 11),
                detail: `Accouplée le ${formatDateFR(cycle.date_saillie)}${cycle.male_numero ? ` avec ${cycle.male_numero}` : ''}`,
                actionLabel: 'Palpation (Diagnostic)',
                actionIcon: 'stethoscope',
                actionType: 'verify'
            };

            if (isPalpationDue) {
                alertBanner = {
                    type: 'warning',
                    text: `🔍 Palpation à réaliser ! (J${elapsed} - ampoules fœtales palpables)`
                };
            } else {
                alertBanner = {
                    type: 'info',
                    text: `Palpation conseillée à J11 (dans ${11 - elapsed} jour(s))`
                };
            }

        } else if (cycle.statut === 'gestante') {
            const elapsed = getDaysElapsed(cycle.date_saillie);
            const daysLeft = daysUntil(cycle.date_mise_bas_prevue);
            const progress = calculateProgress(cycle.date_saillie, cycle.date_mise_bas_prevue);
            const isOverdue = daysLeft !== null && daysLeft < 0;

            statusConfig = {
                label: `Gestante (J${elapsed}/31)`,
                icon: 'rabbit',
                color: '#2196F3',
                bgColor: '#E3F2FD',
                progress: progress,
                detail: isOverdue
                    ? `⚠️ Mise bas dépassée de ${Math.abs(daysLeft)} jour(s) !`
                    : (daysLeft === 0 ? "Mise bas prévue aujourd'hui !" : `Mise bas prévue dans ${daysLeft}j (${formatDateFR(cycle.date_mise_bas_prevue)})`),
                actionLabel: 'Déclarer Naissance',
                actionIcon: 'baby-face-outline',
                actionType: 'birth'
            };

            // Étape critique : Pose de la boîte à nid à J28
            if (elapsed >= 27 && !cycle.boite_nid_posee) {
                alertBanner = {
                    type: 'critical',
                    text: `📦 Urgent (J${elapsed}) : Poser la boîte à nid avec paille !`
                };
                quickNestButton = true;
            } else if (cycle.boite_nid_posee) {
                alertBanner = {
                    type: 'success',
                    text: `✓ Boîte à nid propre installée`
                };
            }

        } else if (cycle.statut === 'allaitante') {
            const elapsed = getDaysElapsed(cycle.date_mise_bas_reelle);
            const daysLeft = daysUntil(cycle.date_sevrage_prevue);
            const progress = calculateProgress(cycle.date_mise_bas_reelle, cycle.date_sevrage_prevue);
            const vivants = cycle.nombre_vivants || 0;

            statusConfig = {
                label: `Allaitante (J${elapsed}/35)`,
                icon: 'baby-bottle',
                color: '#4CAF50',
                bgColor: '#E8F5E9',
                progress: progress,
                detail: `${vivants} lapereau(x) vivant(s) • Nés le ${formatDateFR(cycle.date_mise_bas_reelle)}`,
                actionLabel: elapsed >= 30 ? '🧺 Effectuer Sevrage' : '🧺 Sevrage',
                actionIcon: 'check-decagram',
                actionType: 'wean'
            };

            if (elapsed >= 32) {
                alertBanner = {
                    type: 'warning',
                    text: `🧺 Sevrage recommandé (J${elapsed} - prêts pour l'engraissement)`
                };
            } else if (elapsed >= 21) {
                alertBanner = {
                    type: 'info',
                    text: `Sortie du nid : les petits commencent à manger du solide`
                };
            }
        }
    }

    return (
        <Card style={[styles.card, { backgroundColor: colors.surface }]}>
            <TouchableOpacity
                activeOpacity={0.7}
                onPress={() => onPressCard && onPressCard(femelle)}
                style={styles.cardClickableArea}
            >
                {/* En-tête : Nom / Cage et Badge de statut */}
                <View style={styles.header}>
                    <View style={styles.titleContainer}>
                        <Text style={[styles.name, { color: colors.text }]}>{femelle.numero}</Text>
                        <Text style={[styles.cage, { color: colors.textSecondary }]}>
                            🏠 Loge : {femelle.clapet_numero ? `Clapet ${femelle.clapet_numero}` : 'Non assignée'}
                        </Text>
                    </View>
                    <Chip
                        icon={statusConfig.icon}
                        style={{ backgroundColor: statusConfig.bgColor }}
                        textStyle={{ color: statusConfig.color, fontWeight: 'bold' }}
                    >
                        {statusConfig.label}
                    </Chip>
                </View>

                {/* Détail textuel */}
                <Text style={[styles.detailText, { color: colors.text }]}>
                    {statusConfig.detail}
                </Text>

                {/* Barre de progression du cycle */}
                {cycle && (
                    <View style={styles.progressContainer}>
                        <ProgressBar
                            progress={statusConfig.progress}
                            color={statusConfig.color}
                            style={styles.progressBar}
                        />
                    </View>
                )}

                {/* Alerte contextuelle cuniculicole */}
                {alertBanner && (
                    <View style={[
                        styles.alertBox,
                        alertBanner.type === 'critical' ? styles.alertCritical :
                        alertBanner.type === 'warning' ? styles.alertWarning :
                        alertBanner.type === 'success' ? styles.alertSuccess : styles.alertInfo
                    ]}>
                        <Text style={[
                            styles.alertText,
                            alertBanner.type === 'critical' ? styles.alertCriticalText :
                            alertBanner.type === 'warning' ? styles.alertWarningText :
                            alertBanner.type === 'success' ? styles.alertSuccessText : styles.alertInfoText
                        ]}>
                            {alertBanner.text}
                        </Text>
                        {quickNestButton && (
                            <Button
                                mode="contained"
                                compact
                                uppercase={false}
                                style={styles.quickNestBtn}
                                labelStyle={{ fontSize: 11, marginVertical: 2, marginHorizontal: 6 }}
                                onPress={() => onAction(femelle, 'nest')}
                            >
                                Marquer posé ✓
                            </Button>
                        )}
                    </View>
                )}
            </TouchableOpacity>

            {/* Boutons d'actions */}
            <Card.Actions style={styles.actions}>
                {cycle && (
                    <IconButton
                        icon="alert-circle-outline"
                        size={22}
                        iconColor={colors.error || '#F44336'}
                        onPress={() => onAction(femelle, 'fail')}
                        accessibilityLabel="Signaler un incident"
                    />
                )}
                <Button
                    mode="contained"
                    compact
                    icon={statusConfig.actionIcon}
                    onPress={() => onAction(femelle, statusConfig.actionType)}
                    style={[styles.actionBtn, { backgroundColor: statusConfig.color }]}
                    labelStyle={{ fontWeight: 'bold' }}
                >
                    {statusConfig.actionLabel}
                </Button>
            </Card.Actions>
        </Card>
    );
};

const styles = StyleSheet.create({
    card: {
        marginBottom: 12,
        borderRadius: 12,
        elevation: 2,
        overflow: 'hidden',
    },
    cardClickableArea: {
        paddingHorizontal: 16,
        paddingTop: 14,
        paddingBottom: 4,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 8,
    },
    titleContainer: {
        flex: 1,
        marginRight: 8,
    },
    name: {
        fontSize: 17,
        fontWeight: 'bold',
    },
    cage: {
        fontSize: 13,
        marginTop: 2,
    },
    detailText: {
        fontSize: 14,
        marginTop: 2,
        marginBottom: 8,
        lineHeight: 18,
    },
    progressContainer: {
        marginBottom: 10,
    },
    progressBar: {
        height: 7,
        borderRadius: 4,
    },
    alertBox: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingVertical: 6,
        paddingHorizontal: 10,
        borderRadius: 6,
        marginBottom: 8,
    },
    alertCritical: {
        backgroundColor: '#FFEBEE',
        borderLeftWidth: 4,
        borderLeftColor: '#D32F2F',
    },
    alertWarning: {
        backgroundColor: '#FFF3E0',
        borderLeftWidth: 4,
        borderLeftColor: '#F57C00',
    },
    alertInfo: {
        backgroundColor: '#E1F5FE',
        borderLeftWidth: 4,
        borderLeftColor: '#0288D1',
    },
    alertSuccess: {
        backgroundColor: '#E8F5E9',
        borderLeftWidth: 4,
        borderLeftColor: '#388E3C',
    },
    alertText: {
        fontSize: 12,
        fontWeight: '600',
        flex: 1,
    },
    alertCriticalText: { color: '#B71C1C' },
    alertWarningText: { color: '#E65100' },
    alertInfoText: { color: '#01579B' },
    alertSuccessText: { color: '#1B5E20' },
    quickNestBtn: {
        backgroundColor: '#2E7D32',
        marginLeft: 8,
    },
    actions: {
        justifyContent: 'flex-end',
        paddingHorizontal: 12,
        paddingBottom: 10,
        paddingTop: 0,
    },
    actionBtn: {
        borderRadius: 8,
        minWidth: 140,
    },
});

export default RabbitStatusCard;
