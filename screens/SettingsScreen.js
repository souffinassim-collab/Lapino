import React from 'react';
import { View, StyleSheet } from 'react-native';
import { List, Divider } from 'react-native-paper';

const SettingsScreen = ({ navigation }) => {
    return (
        <View style={styles.container}>
            <List.Section>
                <List.Subheader>Gestion de l'élevage</List.Subheader>

                <List.Item
                    title="Gérer les Clapets"
                    description="Ajouter ou supprimer des cages"
                    left={props => <List.Icon {...props} icon="home-outline" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('ClapetsList')}
                    style={styles.item}
                />
                <Divider />

                <List.Item
                    title="Gérer les Vaccins"
                    description="Types de vaccins et durées"
                    left={props => <List.Icon {...props} icon="needle" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('VaccinsList')}
                    style={styles.item}
                />
                <Divider />

                <List.Item
                    title="Gérer l'Alimentation"
                    description="Stocks et consommation"
                    left={props => <List.Icon {...props} icon="food-apple" />}
                    right={props => <List.Icon {...props} icon="chevron-right" />}
                    onPress={() => navigation.navigate('AlimentsList')}
                    style={styles.item}
                />
            </List.Section>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    item: {
        backgroundColor: 'white',
    }
});

export default SettingsScreen;
