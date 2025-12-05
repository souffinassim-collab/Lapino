import React, { useState, useEffect } from 'react';
import { View, Text, Image } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { Provider as PaperProvider } from 'react-native-paper';
import AppNavigator from './navigation/AppNavigator';
import { lightTheme, darkTheme } from './theme/theme';
import { initDatabase } from './database/db';
import ErrorBoundary from './components/ErrorBoundary';

export default function App() {
    const [isDarkMode, setIsDarkMode] = useState(false);
    const [isReady, setIsReady] = useState(false);

    useEffect(() => {
        prepareApp();
    }, []);

    const prepareApp = async () => {
        try {
            console.log('Starting DB Init...');

            // Sécurité: Si la DB met trop de temps, on force le démarrage quand même
            // Cela évite de rester bloqué sur l'écran de chargement
            const dbInitPromise = initDatabase();
            const timeoutPromise = new Promise(resolve => setTimeout(resolve, 3000));

            await Promise.race([dbInitPromise, timeoutPromise]);

            console.log('Database initialized (or timeout)');
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
        return (
            <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#4CAF50' }}>
                <Image
                    source={require('./assets/rabbit_family_loading.png')}
                    style={{ width: 200, height: 200, marginBottom: 20 }}
                    resizeMode="contain"
                />
                <Text style={{ color: 'white', fontSize: 24, fontWeight: 'bold' }}>Lapino Config...</Text>
                <Text style={{ color: 'white', marginTop: 10 }}>Chargement de vos lapins...</Text>
            </View>
        );
    }

    return (
        <ErrorBoundary>
            <PaperProvider theme={theme}>
                <NavigationContainer theme={theme}>
                    <StatusBar style={isDarkMode ? 'light' : 'dark'} />
                    <AppNavigator toggleTheme={toggleTheme} />
                </NavigationContainer>
            </PaperProvider>
        </ErrorBoundary>
    );
}
