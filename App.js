import React, { useState, useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { Provider as PaperProvider } from 'react-native-paper';
import AppNavigator from './navigation/AppNavigator';
import { lightTheme, darkTheme } from './theme/theme';
import { initDatabase } from './database/db';

export default function App() {
    const [isDarkMode, setIsDarkMode] = useState(false);
    const [isReady, setIsReady] = useState(false);

    useEffect(() => {
        prepareApp();
    }, []);

    const prepareApp = async () => {
        try {
            // Initialiser la base de données
            await initDatabase();
            console.log('Database initialized successfully');
            setIsReady(true);
        } catch (error) {
            console.error('Error preparing app:', error);
            setIsReady(true); // Continue même en cas d'erreur
        }
    };

    const toggleTheme = () => {
        setIsDarkMode(!isDarkMode);
    };

    const theme = isDarkMode ? darkTheme : lightTheme;

    if (!isReady) {
        return null; // Ou un splash screen personnalisé
    }

    return (
        <PaperProvider theme={theme}>
            <NavigationContainer theme={theme}>
                <StatusBar style={isDarkMode ? 'light' : 'dark'} />
                <AppNavigator toggleTheme={toggleTheme} />
            </NavigationContainer>
        </PaperProvider>
    );
}
