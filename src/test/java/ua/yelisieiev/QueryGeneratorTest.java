package ua.yelisieiev;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Tests foe QueryGenerator class
 * has methods:
 * selectAll
 * selectByKey
 * update
 * delete
 * insert
 */
public class QueryGeneratorTest {
    /*
     * generate query for all records from the table of given entities
     *   - no Table annotation exists
     *   - no columns defined
     * generate query for a single record identified by key field(s)
     *   - no key columns defined
     * generate query to update the table with values from given entity
     * generate query to delete corresponding record
     * generate query to insert given entity to the table
     * TODO: check notnulls?
     * */

    @DisplayName("Generate query for all records from the table of given entities")
    @Test
    public void test_GenerateQueryAllRecords() {
        Location location = new Location(10L, 20L);
        String query = QueryGenerator.genQueryForAllRecords(Location.class);
        assertEquals("SELECT longitude, latitude, name, visitors FROM sites;", query);
    }

    @DisplayName("Try without @Table annotation - IllegalArgument")
    @Test
    public void test_noTableAnnotation() {
        assertThrows(IllegalArgumentException.class,
                () -> QueryGenerator.genQueryForAllRecords(NoTable.class),
                "Missing @Table annotation");
    }

    @DisplayName("Try without any @Column annotations - IllegalArgument")
    @Test
    public void test_noColumnAnnotation() {
        assertThrows(IllegalArgumentException.class,
                () -> QueryGenerator.genQueryForAllRecords(NoColumns.class),
                "Missing @Column annotation");
    }

    @DisplayName("Generate query for a single entity identified by key fields")
    @Test
    public void test_getSingleEntity() {
        Location location = new Location(10L, 20L);
        String query = QueryGenerator.genQueryForSingleRecord(location);
        assertEquals("SELECT longitude, latitude, name, visitors FROM sites" +
                " WHERE longitude = 10 AND latitude = 20;", query);
    }

    @DisplayName("Query for single entity for class without @Key annotations")
    @Test
    public void test_entityNoKeys() {
        NoKeys location = new NoKeys(10L, 20L);
        assertThrows(IllegalArgumentException.class,
                () -> QueryGenerator.genQueryForSingleRecord(location),
                "Missing @Key annotation");
    }

    @DisplayName("Query for single entity for class with @Key annotations on fields without @Column")
    @Test
    public void test_entityKeysNotColumns() {
        KeysNotColumns location = new KeysNotColumns(10L, 20L);
        assertThrows(IllegalArgumentException.class,
                () -> QueryGenerator.genQueryForSingleRecord(location),
                "@Key without @Column");
    }

    @DisplayName("Query for single entity update")
    @Test
    public void test_Update() {
        Location location = new Location(10L, 20L, "Moon");
        String query = QueryGenerator.genQueryForUpdate(location);
        assertEquals("UPDATE sites SET name = 'Moon', visitors = NULL" +
                " WHERE longitude = 10 AND latitude = 20;", query);
    }

    @DisplayName("Query for single entity delete")
    @Test
    public void test_Delete() {
        Location location = new Location(10L, 20L);
        String query = QueryGenerator.genQueryForDelete(location);
        assertEquals("DELETE FROM sites" +
                " WHERE longitude = 10 AND latitude = 20;", query);
    }

    @DisplayName("Query for single entity insert")
    @Test
    public void test_Insert() {
        Location location = new Location(10L, 20L, "Moon");
        String query = QueryGenerator.genQueryForInsert(location);
        assertEquals("INSERT INTO sites (longitude, latitude, name, visitors)" +
                " VALUES (10, 20, 'Moon', NULL);", query);
    }


    @Table
    private static class NoKeys {
        @Column
        private Long longitude;
        @Column
        private Long latitude;

        public NoKeys(Long longitude, Long latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }

    @Table
    private static class KeysNotColumns {
        @Key
        private Long longitude;
        @Column
        private Long latitude;

        public KeysNotColumns(Long longitude, Long latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }


    private static class NoTable {
        @Column
        @Key
        private Long longitude;
        @Column
        @Key
        private Long latitude;
    }

    @Table("sites")
    private static class NoColumns {
        private Long longitude;
        private Long latitude;
    }

    @Table("sites")
    private static class Location {
        @Column
        @Key
        private final Long  longitude;
        @Column
        @Key
        private Long latitude;
        @Column
        private String name;
        @Column("visitors")
        private Long visitorsPerYear;

        public Location(Long longitude, Long latitude) {
            this(longitude, latitude, "Some place");
        }

        public Location(Long longitude, Long latitude, String name) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.name = name;
        }
    }

}
