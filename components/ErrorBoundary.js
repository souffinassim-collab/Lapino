import React from 'react';
import { View, Text, StyleSheet, ScrollView, Button } from 'react-native';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null, errorInfo: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error("Uncaught error:", error, errorInfo);
        this.setState({ errorInfo });
    }

    render() {
        if (this.state.hasError) {
            return (
                <View style={styles.container}>
                    <ScrollView contentContainerStyle={styles.scroll}>
                        <Text style={styles.title}>Oups ! Une erreur est survenue.</Text>
                        <Text style={styles.subtitle}>L'application a rencontré un problème inattendu.</Text>

                        <View style={styles.errorBox}>
                            <Text style={styles.errorText}>
                                {this.state.error && this.state.error.toString()}
                            </Text>
                        </View>

                        <Button
                            title="Relancer l'application"
                            onPress={() => {
                                this.setState({ hasError: false });
                                // Optionnel: recharger complètement si possible
                            }}
                        />
                    </ScrollView>
                </View>
            );
        }

        return this.props.children;
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#FFEBEE',
        justifyContent: 'center',
        padding: 20
    },
    scroll: {
        flexGrow: 1,
        justifyContent: 'center',
        alignItems: 'center'
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#D32F2F',
        marginBottom: 10
    },
    subtitle: {
        fontSize: 16,
        color: '#555',
        marginBottom: 20,
        textAlign: 'center'
    },
    errorBox: {
        backgroundColor: 'white',
        padding: 15,
        borderRadius: 5,
        borderWidth: 1,
        borderColor: '#D32F2F',
        marginBottom: 20,
        width: '100%',
        maxHeight: 200
    },
    errorText: {
        color: '#D32F2F',
        fontFamily: 'monospace',
        fontSize: 12
    }
});

export default ErrorBoundary;
