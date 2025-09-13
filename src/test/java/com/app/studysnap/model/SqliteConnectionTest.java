package com.app.studysnap.model;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteConnectionTest {

    // Test if constructor exists and is private
    @Test
    void SqliteConnection_EmptyConstructor_MethodExistsAndIsPrivate() throws Exception {
        Class<?> MyClass = SqliteConnection.class;
        assertNotNull(MyClass.getDeclaredConstructor());
        assertTrue(Modifier.isPrivate(MyClass.getDeclaredConstructor().getModifiers()));
    }

    // Test if getters exist
    @Test
    void getInstance_MethodExists() throws Exception {
        Class<?> clazz = SqliteConnection.class;
        assertNotNull(clazz.getDeclaredMethod("getInstance"));
    }

    // Test if instance returns a connection
    @Test
    void getInstance_ReturnsNonNullConnection() {
        Connection c = SqliteConnection.getInstance();
        assertNotNull(c);
    }

    // Test if different instances point to the same connection
    @Test
    void getInstance_ReturnsSameSingletonAcrossCalls() {
        Connection c1 = SqliteConnection.getInstance();
        Connection c2 = SqliteConnection.getInstance();
        assertSame(c1, c2);
    }

    // Test for statement execution via connection
    @Test
    void connection_CanExecuteStatement() throws Exception {
        Connection c = SqliteConnection.getInstance();
        try (Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS __test__(x INTEGER)");
            st.execute("DROP TABLE IF EXISTS __test__");
        }
        assertTrue(true);
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(SqliteConnection.class);
    }
}