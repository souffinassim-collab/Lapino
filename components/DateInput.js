import React, { useState } from 'react';
import { View, TouchableOpacity, Platform, StyleSheet } from 'react-native';
import { Text, TextInput } from 'react-native-paper';
import DateTimePicker from '@react-native-community/datetimepicker';
import { formatDateFR } from '../utils/dateUtils';

const DateInput = ({ label, value, onChange, style, mode = 'date' }) => {
    // Value management: handle both String (YYYY-MM-DD) and Date object
    const getDateObj = () => {
        if (!value) return new Date();
        if (value instanceof Date) return value;
        return new Date(value);
    };

    const [show, setShow] = useState(false);
    const dateValue = getDateObj();

    const handleChange = (event, selectedDate) => {
        setShow(false);
        if (selectedDate) {
            // Return ISO string part (YYYY-MM-DD) to match existing app logic which uses strings mostly
            const isoDate = selectedDate.toISOString().split('T')[0];
            onChange(isoDate);
        }
    };

    // Web Fallback: Manual Text Input
    if (Platform.OS === 'web') {
        return (
            <TextInput
                label={label}
                value={value}
                onChangeText={onChange}
                placeholder="YYYY-MM-DD"
                mode="outlined"
                style={style}
                right={<TextInput.Icon icon="calendar" />}
            />
        );
    }

    // Native: Read-only input that triggers Picker
    return (
        <View style={style}>
            <TouchableOpacity onPress={() => setShow(true)} activeOpacity={1}>
                <View pointerEvents="none">
                    <TextInput
                        label={label}
                        value={formatDateFR(dateValue.toISOString())}
                        editable={false}
                        mode="outlined"
                        right={<TextInput.Icon icon="calendar" />}
                    />
                </View>
            </TouchableOpacity>

            {show && (
                <DateTimePicker
                    value={dateValue}
                    mode={mode}
                    display="default" // 'calendar' on Android
                    onChange={handleChange}
                />
            )}
        </View>
    );
};

export default DateInput;
