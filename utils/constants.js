// Constantes de l'application

export const STATUTS_FEMELLE = [
    { label: 'Vivante', value: 'vivante' },
    { label: 'Vendue', value: 'vendue' },
    { label: 'Morte', value: 'morte' },
];

export const ALERT_THRESHOLD_DAYS = 7; // Alertes pour vaccins dans les 7 prochains jours

export const MESSAGES = {
    confirmDelete: 'Êtes-vous sûr de vouloir supprimer ?',
    deleteSuccess: 'Supprimé avec succès',
    saveSuccess: 'Enregistré avec succès',
    error: 'Une erreur est survenue',
};

export default {
    STATUTS_FEMELLE,
    ALERT_THRESHOLD_DAYS,
    MESSAGES,
};
