import { DefaultTheme, DarkTheme } from '@react-navigation/native';

// Couleurs principales
const COLORS = {
    light: {
        primary: '#4CAF50',
        primaryDark: '#388E3C',
        primaryLight: '#C8E6C9',
        background: '#FFFFFF',
        surface: '#F5F5F5',
        text: '#212121',
        textSecondary: '#757575',
        error: '#F44336',
        warning: '#FF9800',
        success: '#4CAF50',
        border: '#E0E0E0',
    },
    dark: {
        primary: '#66BB6A',
        primaryDark: '#4CAF50',
        primaryLight: '#81C784',
        background: '#121212',
        surface: '#1E1E1E',
        text: '#FFFFFF',
        textSecondary: '#B0B0B0',
        error: '#F44336',
        warning: '#FF9800',
        success: '#66BB6A',
        border: '#333333',
    },
};

// Thème clair
export const lightTheme = {
    ...DefaultTheme,
    colors: {
        ...DefaultTheme.colors,
        ...COLORS.light,
    },
};

// Thème sombre
export const darkTheme = {
    ...DarkTheme,
    colors: {
        ...DarkTheme.colors,
        ...COLORS.dark,
    },
};

// Styles communs
export const commonStyles = {
    container: {
        flex: 1,
    },
    padding: {
        padding: 16,
    },
    card: {
        borderRadius: 8,
        padding: 16,
        marginVertical: 8,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    button: {
        borderRadius: 8,
        paddingVertical: 12,
        paddingHorizontal: 24,
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 48,
    },
    buttonText: {
        fontSize: 16,
        fontWeight: '600',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 16,
    },
    subtitle: {
        fontSize: 18,
        fontWeight: '600',
        marginVertical: 8,
    },
    label: {
        fontSize: 14,
        fontWeight: '500',
        marginBottom: 4,
    },
    input: {
        borderWidth: 1,
        borderRadius: 8,
        paddingHorizontal: 12,
        paddingVertical: 10,
        fontSize: 16,
        marginBottom: 16,
    },
};

export default {
    lightTheme,
    darkTheme,
    commonStyles,
};
