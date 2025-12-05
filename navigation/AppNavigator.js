import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import { useTheme } from '@react-navigation/native';
import { TouchableOpacity, Text, View } from 'react-native';

// Screens
import DashboardScreen from '../screens/DashboardScreen';
import FemellesListScreen from '../screens/Femelles/ListScreen';
import FemelleDetailScreen from '../screens/Femelles/DetailScreen';
import FemelleAddEditScreen from '../screens/Femelles/AddEditScreen';
import ClapletsScreen from '../screens/ClapletsScreen';
import VaccinsScreen from '../screens/VaccinsScreen';
import SettingsScreen from '../screens/SettingsScreen';
import AlimentsScreen from '../screens/AlimentsScreen';

const Stack = createStackNavigator();

const AppNavigator = ({ toggleTheme }) => {
    const { colors } = useTheme();

    return (
        <Stack.Navigator
            screenOptions={{
                headerStyle: {
                    backgroundColor: colors.primary,
                },
                headerTintColor: '#FFFFFF',
                headerTitleStyle: {
                    fontWeight: 'bold',
                },
            }}
        >
            <Stack.Screen
                name="Dashboard"
                component={DashboardScreen}
                options={({ navigation }) => ({
                    title: 'Accueil',
                    headerRight: () => (
                        <View style={{ flexDirection: 'row' }}>
                            <TouchableOpacity
                                onPress={toggleTheme}
                                style={{ marginRight: 16 }}
                            >
                                <Text style={{ fontSize: 24 }}>🌓</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                onPress={() => navigation.navigate('Settings')}
                                style={{ marginRight: 16 }}
                            >
                                <Text style={{ fontSize: 24 }}>⚙️</Text>
                            </TouchableOpacity>
                        </View>
                    ),
                })}
            />

            <Stack.Screen
                name="Settings"
                component={SettingsScreen}
                options={{ title: 'Réglages' }}
            />

            <Stack.Screen
                name="AlimentsList"
                component={AlimentsScreen}
                options={{ title: 'Gestion Alimentation' }}
            />

            <Stack.Screen
                name="FemellesList"
                component={FemellesListScreen}
                options={{ title: 'Femelles' }}
            />

            <Stack.Screen
                name="FemelleDetail"
                component={FemelleDetailScreen}
                options={{ title: 'Détail Femelle' }}
            />

            <Stack.Screen
                name="FemelleAddEdit"
                component={FemelleAddEditScreen}
                options={({ route }) => ({
                    title: route.params?.femelleId ? 'Modifier Femelle' : 'Nouvelle Femelle'
                })}
            />

            <Stack.Screen
                name="ClapetsList"
                component={ClapletsScreen}
                options={{ title: 'Clapets' }}
            />

            <Stack.Screen
                name="VaccinsList"
                component={VaccinsScreen}
                options={{ title: 'Vaccins' }}
            />
        </Stack.Navigator>
    );
};

export default AppNavigator;
