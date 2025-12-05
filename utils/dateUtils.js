// Utilitaires pour la manipulation des dates

// Ajouter N jours à une date
export const addDays = (date, days) => {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
};

// Formater une date en ISO (YYYY-MM-DD)
export const formatDateISO = (date) => {
    if (!date) return null;
    const d = new Date(date);
    if (isNaN(d.getTime())) return null;

    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

// Formater une date en français (DD/MM/YYYY)
export const formatDateFR = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
};

// Calculer le nombre de jours entre aujourd'hui et une date
export const daysUntil = (dateString) => {
    if (!dateString) return null;
    const targetDate = new Date(dateString);
    const today = new Date();

    // Réinitialiser les heures pour comparer seulement les dates
    today.setHours(0, 0, 0, 0);
    targetDate.setHours(0, 0, 0, 0);

    const diffTime = targetDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
};

// Déterminer le statut d'alerte d'une vaccination
export const getVaccinationStatus = (dateProchainString) => {
    if (!dateProchainString) return 'none';

    const days = daysUntil(dateProchainString);

    if (days === null) return 'none';
    if (days < 0) return 'overdue'; // En retard (rouge)
    if (days <= 7) return 'soon'; // Bientôt (orange)
    return 'ok'; // OK (vert)
};

// Obtenir la couleur selon le statut
export const getStatusColor = (status) => {
    switch (status) {
        case 'overdue':
            return '#F44336'; // Rouge
        case 'soon':
            return '#FF9800'; // Orange
        case 'ok':
            return '#4CAF50'; // Vert
        default:
            return '#9E9E9E'; // Gris
    }
};

// Obtenir le texte du statut
export const getStatusText = (dateProchainString) => {
    const days = daysUntil(dateProchainString);

    if (days === null) return '';
    if (days < 0) return `En retard (${Math.abs(days)}j)`;
    if (days === 0) return "Aujourd'hui";
    if (days === 1) return 'Demain';
    if (days <= 7) return `Dans ${days} jours`;
    return '';
};

export default {
    addDays,
    formatDateISO,
    formatDateFR,
    daysUntil,
    getVaccinationStatus,
    getStatusColor,
    getStatusText,
};
