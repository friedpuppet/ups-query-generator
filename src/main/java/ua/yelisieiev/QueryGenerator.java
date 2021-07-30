package ua.yelisieiev;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class QueryGenerator {
    public static String genQueryForAllRecords(Class<?> ormEntityType) {
        validateEntityType(ormEntityType);
        return "SELECT " + getFieldsNamesAsString(ormEntityType) +
                " FROM " + getTableName(ormEntityType) +
                ";";
    }


    public static String genQueryForSingleRecord(Object ormEntity) {
        validateSingleEntity(ormEntity);
        return "SELECT " + getFieldsNamesAsString(ormEntity.getClass()) +
                " FROM " + getTableName(ormEntity.getClass()) +
                " WHERE " + getKeysWithValuesAsString(ormEntity) +
                ";";
    }

    public static String genQueryForUpdate(Object ormEntity) {
        return "UPDATE " + getTableName(ormEntity.getClass()) +
                " SET " + getFieldsUpdatesAsString(ormEntity) +
                " WHERE " + getKeysWithValuesAsString(ormEntity) +
                ";";
    }

    public static String genQueryForDelete(Object ormEntity) {
        return "DELETE FROM " + getTableName(ormEntity.getClass()) +
                " WHERE " + getKeysWithValuesAsString(ormEntity) +
                ";";
    }


    public static String genQueryForInsert(Object ormEntity) {
        return "INSERT INTO " + getTableName(ormEntity.getClass()) +
                " (" + getFieldsNamesAsString(ormEntity.getClass()) + ")" +
                " VALUES (" + getFieldsValuesAsString(ormEntity) + ")" +
                ";";
    }

    private static void validateEntityType(Class<?> ormEntityType) {
        if (ormEntityType.getAnnotation(Table.class) == null) {
            throw new IllegalArgumentException("Missing @Table annotation");
        }
        boolean hasColumns = false;
        for (Field declaredField : ormEntityType.getDeclaredFields()) {
            if (declaredField.getAnnotation(Column.class) != null) {
                hasColumns = true;
                break;
            }
        }
        if (!hasColumns) {
            throw new IllegalArgumentException("Missing @Column annotation");
        }
    }

    private static void validateSingleEntity(Object ormEntity) {
        validateEntityType(ormEntity.getClass());
        boolean hasKeys = false;
        for (Field declaredField : ormEntity.getClass().getDeclaredFields()) {
            if (declaredField.getAnnotation(Key.class) != null) {
                if (declaredField.getAnnotation(Column.class) == null) {
                    throw new IllegalArgumentException("@Key without @Column");
                }
                hasKeys = true;
                break;
            }
        }
        if (!hasKeys) {
            throw new IllegalArgumentException("Missing @Key annotation");
        }
    }

    private static String getTableName(Class<?> ormEntityType) {
        Table tableAnnotation = ormEntityType.getAnnotation(Table.class);
        return tableAnnotation.value().equals("") ? ormEntityType.getCanonicalName() :
                tableAnnotation.value();
    }

    private static String getFieldsNamesAsString(Class<?> ormEntityType) {
        StringJoiner columnsJoined = new StringJoiner(", ");
        for (Field declaredField : ormEntityType.getDeclaredFields()) {
            if (isFieldAColumn(declaredField)) {
                columnsJoined.add(getColumnName(declaredField));
            }
        }
        return columnsJoined.toString();
    }

    private static String getFieldsUpdatesAsString(Object ormEntity) {
        StringJoiner valuesJoined = new StringJoiner(", ");
        for (Field declaredField : ormEntity.getClass().getDeclaredFields()) {
            if (isFieldAColumn(declaredField) && !isFieldAKey(declaredField)) {
                valuesJoined.add(getColumnName(declaredField) + " = " + getColumnValue(declaredField, ormEntity));
            }
        }
        return valuesJoined.toString();

    }

    private static String getFieldsValuesAsString(Object ormEntity) {
        StringJoiner valuesJoined = new StringJoiner(", ");
        for (Field declaredField : ormEntity.getClass().getDeclaredFields()) {
            if (isFieldAColumn(declaredField)) {
                valuesJoined.add(getColumnValue(declaredField, ormEntity));
            }
        }
        return valuesJoined.toString();
    }

    private static String getKeysWithValuesAsString(Object ormEntity) {
        StringJoiner keysJoined = new StringJoiner(" AND ");
        for (Field declaredField : ormEntity.getClass().getDeclaredFields()) {
            if (isFieldAKey(declaredField)) {
                keysJoined.add(getColumnName(declaredField) + " = " + getColumnValue(declaredField, ormEntity));
            }
        }
        return keysJoined.toString();
    }

    private static boolean isFieldAColumn(Field field) {
        return field.getAnnotation(Column.class) != null;
    }

    private static boolean isFieldAKey(Field field) {
        return field.getAnnotation(Key.class) != null;
    }

    private static String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        return columnAnnotation.value().equals("") ? field.getName() :
                columnAnnotation.value();
    }

    private static String getColumnValue(Field field, Object ormEntity) {
        field.setAccessible(true);
        try {
            Object value = field.get(ormEntity);
            if (value == null) {
                return "NULL";
            }
            if (field.getType() == String.class) {
                return "'" + value + "'";
            }
            return String.valueOf(value);
        } catch (IllegalAccessException e) {
            // shouldn't happen, we've set accessible to true
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
