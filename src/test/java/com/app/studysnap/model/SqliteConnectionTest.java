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

    @Test
    void testConnection() {
        Connection conn = SqliteConnection.getInstance();
        assertNotNull(conn);
    }

    // Test if different instances point to the same connection
    @Test
    void getInstance_ReturnsSameSingletonAcrossCalls() {
        Connection c1 = SqliteConnection.getInstance();
        Connection c2 = SqliteConnection.getInstance();
        assertSame(c1, c2);
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(SqliteConnection.class);
    }
}