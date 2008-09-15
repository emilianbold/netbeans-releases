/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.db.mysql.sakila;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.SQLExecutor;
import org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo;
import org.netbeans.modules.db.test.DBTestBase;

/**
 *
 * @author David
 */
public class SakilaSampleProviderTest  extends DBTestBase {
    SakilaSampleProvider provider;
    DatabaseConnection dbconn;
    
    private static final String DBNAME = "sakila";

    public SakilaSampleProviderTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        provider = SakilaSampleProvider.getDefault();
        
        dbconn = getDatabaseConnection(true);
    }
        
    /**
     * Test of create method, of class SakilaSampleProvider.
     */
    @Test
    public void testCreate() throws Exception {
        if (! isMySQL() ) {
            System.out.println("testCreate() only runs with MySQL database");
        }

        checkExecution(SQLExecutor.execute(dbconn, "DROP DATABASE IF EXISTS " + DBNAME));
        checkExecution(SQLExecutor.execute(dbconn, "CREATE DATABASE  " + DBNAME));

        String baseUrl = getDbUrl();
        String[] chunks = baseUrl.split("/");
        assertTrue(chunks.length == 4);
        String originalDbName = chunks[3];
        String newUrl = baseUrl.replace(originalDbName, DBNAME);

        dbconn = DatabaseConnection.create(getJDBCDriver(), newUrl, getUsername(), DBNAME, getPassword(), false);
        ConnectionManager.getDefault().addConnection(dbconn);
        ConnectionManager.getDefault().connect(dbconn);
        assertTrue(dbconn.getJDBCConnection(true) != null);
        assertFalse(dbconn.getJDBCConnection(true).isClosed());

        provider.create("sakila", dbconn);
        
        HashMap<String, Integer> tables = new HashMap<String, Integer>();
        tables.put("actor", 200);
        tables.put("address", 603);
        tables.put("category", 16);
        tables.put("city", 600);
        tables.put("country", 109);
        tables.put("customer", 599);
        tables.put("film", 1000);
        tables.put("film_actor", 5462);
        tables.put("film_category", 1000);
        tables.put("film_text", 1000);
        tables.put("inventory", 4581);
        tables.put("language", 6);
        tables.put("payment", 16049);
        tables.put("rental", 16044);
        tables.put("staff", 2);
        tables.put("store", 2);

        for (Entry<String,Integer> entry : tables.entrySet()) {
            checkTable(entry.getKey(), entry.getValue());
        }

        HashMap<String, String> views = new HashMap<String, String>();
        views.put("actor_info", "/* ALGORITHM=UNDEFINED */ select `a`.`actor_id` AS `actor_id`,`a`.`first_name` AS `first_name`,`a`.`last_name` AS `last_name`,group_concat(distinct concat(`c`.`name`,_utf8': ',(select group_concat(`f`.`title` order by `f`.`title` ASC separator ', ') AS `GROUP_CONCAT(f.title ORDER BY f.title SEPARATOR ', ')` from ((`sakila`.`film` `f` join `sakila`.`film_category` `fc` on((`f`.`film_id` = `fc`.`film_id`))) join `sakila`.`film_actor` `fa` on((`f`.`film_id` = `fa`.`film_id`))) where ((`fc`.`category_id` = `c`.`category_id`) and (`fa`.`actor_id` = `a`.`actor_id`)))) order by `c`.`name` ASC separator '; ') AS `film_info` from (((`sakila`.`actor` `a` left join `sakila`.`film_actor` `fa` on((`a`.`actor_id` = `fa`.`actor_id`))) left join `sakila`.`film_category` `fc` on((`fa`.`film_id` = `fc`.`film_id`))) left join `sakila`.`category` `c` on((`fc`.`category_id` = `c`.`category_id`))) group by `a`.`actor_id`,`a`.`first_name`,`a`.`last_name`");
        views.put("customer_list", "/* ALGORITHM=UNDEFINED */ select `cu`.`customer_id` AS `ID`,concat(`cu`.`first_name`,_utf8' ',`cu`.`last_name`) AS `name`,`a`.`address` AS `address`,`a`.`postal_code` AS `zip code`,`a`.`phone` AS `phone`,`sakila`.`city`.`city` AS `city`,`sakila`.`country`.`country` AS `country`,if(`cu`.`active`,_utf8'active',_utf8'') AS `notes`,`cu`.`store_id` AS `SID` from (((`sakila`.`customer` `cu` join `sakila`.`address` `a` on((`cu`.`address_id` = `a`.`address_id`))) join `sakila`.`city` on((`a`.`city_id` = `sakila`.`city`.`city_id`))) join `sakila`.`country` on((`sakila`.`city`.`country_id` = `sakila`.`country`.`country_id`)))");
        views.put("film_list", "/* ALGORITHM=UNDEFINED */ select `sakila`.`film`.`film_id` AS `FID`,`sakila`.`film`.`title` AS `title`,`sakila`.`film`.`description` AS `description`,`sakila`.`category`.`name` AS `category`,`sakila`.`film`.`rental_rate` AS `price`,`sakila`.`film`.`length` AS `length`,`sakila`.`film`.`rating` AS `rating`,group_concat(concat(`sakila`.`actor`.`first_name`,_utf8' ',`sakila`.`actor`.`last_name`) separator ', ') AS `actors` from ((((`sakila`.`category` left join `sakila`.`film_category` on((`sakila`.`category`.`category_id` = `sakila`.`film_category`.`category_id`))) left join `sakila`.`film` on((`sakila`.`film_category`.`film_id` = `sakila`.`film`.`film_id`))) join `sakila`.`film_actor` on((`sakila`.`film`.`film_id` = `sakila`.`film_actor`.`film_id`))) join `sakila`.`actor` on((`sakila`.`film_actor`.`actor_id` = `sakila`.`actor`.`actor_id`))) group by `sakila`.`film`.`film_id`");
        views.put("nicer_but_slower_film_list", "/* ALGORITHM=UNDEFINED */ select `sakila`.`film`.`film_id` AS `FID`,`sakila`.`film`.`title` AS `title`,`sakila`.`film`.`description` AS `description`,`sakila`.`category`.`name` AS `category`,`sakila`.`film`.`rental_rate` AS `price`,`sakila`.`film`.`length` AS `length`,`sakila`.`film`.`rating` AS `rating`,group_concat(concat(concat(ucase(substr(`sakila`.`actor`.`first_name`,1,1)),lcase(substr(`sakila`.`actor`.`first_name`,2,length(`sakila`.`actor`.`first_name`))),_utf8' ',concat(ucase(substr(`sakila`.`actor`.`last_name`,1,1)),lcase(substr(`sakila`.`actor`.`last_name`,2,length(`sakila`.`actor`.`last_name`)))))) separator ', ') AS `actors` from ((((`sakila`.`category` left join `sakila`.`film_category` on((`sakila`.`category`.`category_id` = `sakila`.`film_category`.`category_id`))) left join `sakila`.`film` on((`sakila`.`film_category`.`film_id` = `sakila`.`film`.`film_id`))) join `sakila`.`film_actor` on((`sakila`.`film`.`film_id` = `sakila`.`film_actor`.`film_id`))) join `sakila`.`actor` on((`sakila`.`film_actor`.`actor_id` = `sakila`.`actor`.`actor_id`))) group by `sakila`.`film`.`film_id`");
        views.put("sales_by_film_category", "/* ALGORITHM=UNDEFINED */ select `c`.`name` AS `category`,sum(`p`.`amount`) AS `total_sales` from (((((`sakila`.`payment` `p` join `sakila`.`rental` `r` on((`p`.`rental_id` = `r`.`rental_id`))) join `sakila`.`inventory` `i` on((`r`.`inventory_id` = `i`.`inventory_id`))) join `sakila`.`film` `f` on((`i`.`film_id` = `f`.`film_id`))) join `sakila`.`film_category` `fc` on((`f`.`film_id` = `fc`.`film_id`))) join `sakila`.`category` `c` on((`fc`.`category_id` = `c`.`category_id`))) group by `c`.`name` order by sum(`p`.`amount`) desc");
        views.put("sales_by_store", "/* ALGORITHM=UNDEFINED */ select concat(`c`.`city`,_utf8',',`cy`.`country`) AS `store`,concat(`m`.`first_name`,_utf8' ',`m`.`last_name`) AS `manager`,sum(`p`.`amount`) AS `total_sales` from (((((((`sakila`.`payment` `p` join `sakila`.`rental` `r` on((`p`.`rental_id` = `r`.`rental_id`))) join `sakila`.`inventory` `i` on((`r`.`inventory_id` = `i`.`inventory_id`))) join `sakila`.`store` `s` on((`i`.`store_id` = `s`.`store_id`))) join `sakila`.`address` `a` on((`s`.`address_id` = `a`.`address_id`))) join `sakila`.`city` `c` on((`a`.`city_id` = `c`.`city_id`))) join `sakila`.`country` `cy` on((`c`.`country_id` = `cy`.`country_id`))) join `sakila`.`staff` `m` on((`s`.`manager_staff_id` = `m`.`staff_id`))) group by `s`.`store_id` order by `cy`.`country`,`c`.`city`");
        views.put("staff_list", "/* ALGORITHM=UNDEFINED */ select `s`.`staff_id` AS `ID`,concat(`s`.`first_name`,_utf8' ',`s`.`last_name`) AS `name`,`a`.`address` AS `address`,`a`.`postal_code` AS `zip code`,`a`.`phone` AS `phone`,`sakila`.`city`.`city` AS `city`,`sakila`.`country`.`country` AS `country`,`s`.`store_id` AS `SID` from (((`sakila`.`staff` `s` join `sakila`.`address` `a` on((`s`.`address_id` = `a`.`address_id`))) join `sakila`.`city` on((`a`.`city_id` = `sakila`.`city`.`city_id`))) join `sakila`.`country` on((`sakila`.`city`.`country_id` = `sakila`.`country`.`country_id`)))");
        
        for (Entry<String,String> entry : views.entrySet()) {
            checkView(entry.getKey(), entry.getValue());
        }

        checkFilmInStock(963, 1, 2);
        checkFilmInStock(512, 2, 4);

        checkFilmNotInStock(2, 2, 1);
        checkFilmNotInStock(963, 2, 0);
        
        checkRewardsReport(7, 20, 0);

        checkGetCustomerBalance(298, 0);
        checkInventoryHeldByCustomer(8, 0);
        checkInventoryHeldByCustomer(9, 366);
        checkInventoryInStock(9, 0);
        checkInventoryInStock(8, 1);

        HashMap<String, String> triggers = new HashMap<String, String>();
        triggers.put("customer_create_date", "SET NEW.create_date = NOW()");
        triggers.put("ins_film", "BEGIN\n" +
            "    INSERT INTO film_text (film_id, title, description)\n" +
            "        VALUES (new.film_id, new.title, new.description);\n" +
            "END");
        triggers.put("upd_film", "BEGIN\n" +
            "    IF (old.title != new.title) or (old.description != new.description)\n" +
            "    THEN\n" +
            "        UPDATE film_text\n" +
            "            SET title=new.title,\n" +
            "                description=new.description,\n" +
            "                film_id=new.film_id\n" +
            "        WHERE film_id=old.film_id;\n" +
            "    END IF;\n" +
            "  END");
        triggers.put("payment_date", "SET NEW.payment_date = NOW()");
        triggers.put("rental_date", "SET NEW.rental_date = NOW()");

        for (Entry<String,String> entry : triggers.entrySet()) {
            checkTrigger(entry.getKey(), entry.getValue());
        }
        
        checkMrHillyer();
    }
    
    @Test
    public void testBadCreate() throws Exception {
        if (! isMySQL() ) {
            System.out.println("testCreate() only runs with MySQL database");
        }

        try {
            provider.create("sample", dbconn);
            fail("This should have failed");
        } catch (DatabaseException e) {
            // Expected
        }        
    }

    /**
     * Test of supportsSample method, of class SakilaSampleProvider.
     */
    @Test
    public void testSupportsSample() {
        assertTrue(provider.supportsSample("sakila"));
        assertFalse(provider.supportsSample("sample"));
    }

    /**
     * Test of getSampleNames method, of class SakilaSampleProvider.
     */
    @Test
    public void testGetSampleNames() {
        List<String> names = provider.getSampleNames();
        assertTrue(names.size() == 1);
        assertTrue(names.get(0).equals("sakila"));
    }

    private void checkFilmInStock(int filmId, int storeId, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        CallableStatement stmt = conn.prepareCall("{call film_in_stock(?, ?, ?)}");
        stmt.setInt(1, filmId);
        stmt.setInt(2, storeId);
        stmt.registerOutParameter(3, Types.INTEGER);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            // Skip results
        }

        assertEquals(expectedResult, stmt.getInt(3));
    }

    private void checkFilmNotInStock(int filmId, int storeId, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        CallableStatement stmt = conn.prepareCall("{call film_not_in_stock(?, ?, ?)}");
        stmt.setInt(1, filmId);
        stmt.setInt(2, storeId);
        stmt.registerOutParameter(3, Types.INTEGER);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            // Skip results
        }

        assertEquals(expectedResult, stmt.getInt(3));
    }

    private void checkGetCustomerBalance(int customerId, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT get_customer_balance(" + customerId + ", NOW())");
        assertTrue(rs.next());
        assertEquals("Incorrect balance for customer " + customerId + ".  ", expectedResult, rs.getInt(1));
    }

    private void checkInventoryHeldByCustomer(int customerId, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT inventory_held_by_customer(" + customerId + ")");
        assertTrue(rs.next());
        assertEquals("Incorrect inventory for customer " + customerId + ".  ", expectedResult, rs.getInt(1));
    }

    private void checkInventoryInStock(int inventoryId, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT inventory_in_stock(" + inventoryId + ")");
        assertTrue(rs.next());
        assertEquals("Incorrect stock count for inventory id " + inventoryId + ".  ", expectedResult, rs.getInt(1));
    }

    private void checkMrHillyer() throws Exception {
        String workingdir = System.getProperty("user.dir");
        String filename = workingdir + "/hillyer-test.png";
        FileInputStream fis = null;
        InputStream resourceStream = null;
        try {
            Connection conn = dbconn.getJDBCConnection();
            conn.createStatement().executeQuery("SELECT picture FROM staff WHERE last_name='Hillyer' " +
                    "INTO DUMPFILE '" + filename + "'");
            
            fis = new FileInputStream(filename);
            assertNotNull(fis);

            resourceStream = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/db/mysql/sakila/resources/hillyer.png");
            assertNotNull(resourceStream);

            int c1;
            int c2;
            for (c1 = fis.read() ; c1 != -1 ; c1 = fis.read()) {
                c2 = resourceStream.read();
                assertFalse("Picture data not the same length", c2 == -1);
                assertEquals("Picture data does not match", c1, c2);
            }
            
        } finally {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }

            if (fis != null) {
                fis.close();
            }

            if (resourceStream != null) {
                resourceStream.close();
            }            
        }
    }
    
    private void checkRewardsReport(int minPurchases, int minDollars, int expectedResult) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        CallableStatement stmt = conn.prepareCall("{call rewards_report(?, ?, ?)}");
        stmt.setInt(1, minPurchases);
        stmt.setInt(2, minDollars);
        stmt.registerOutParameter(3, Types.INTEGER);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            // Skip results
        }

        assertEquals(expectedResult, stmt.getInt(3));
        
    }

    private void checkTable(String tableName, int expectedSize) throws Exception {
        ResultSet rs = null;
        try {
            Connection conn = dbconn.getJDBCConnection();
            assert(conn != null);
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + tableName);

            assertTrue(rs.next());

            assertEquals("Incorrect number of rows for table '" + tableName + "'.  ", expectedSize, rs.getInt(1));
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private void checkTrigger(String triggerName, String triggerText) throws Exception {
        Connection conn = dbconn.getJDBCConnection();
        assert(conn != null);

        ResultSet rs = conn.createStatement().executeQuery("SELECT ACTION_STATEMENT FROM information_schema.triggers " +
                "WHERE TRIGGER_NAME = '" + triggerName + "'");
        
        assertTrue(rs.next());
        assertEquals("Incorrect action statement for trigger " + triggerName + ".  ", triggerText, rs.getString(1));
    }

    private void checkView(String viewName, String text) throws Exception {
        ResultSet rs = null;
        try {
            Connection conn = dbconn.getJDBCConnection();
            assert(conn != null);
            rs = conn.createStatement().executeQuery("SELECT VIEW_DEFINITION FROM " +
                    "information_schema.views WHERE TABLE_NAME = '" + viewName + "'");

            assertTrue(rs.next());

            assertEquals("Incorrect view definition for view '" + viewName + "'.  ", text, rs.getString(1));
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private void checkExecution(SQLExecutionInfo info) throws Exception {
        assertNotNull(info);

        Throwable throwable = null;
        if (info.hasExceptions()) {
            for (StatementExecutionInfo stmtinfo : info.getStatementInfos()) {
                if (stmtinfo.hasExceptions()) {
                    System.err.println("The following SQL had exceptions:");
                } else {
                    System.err.println("The following SQL executed cleanly:");
                }
                System.err.println(stmtinfo.getSQL());

                for  (Throwable t : stmtinfo.getExceptions()) {
                    t.printStackTrace();

                    throwable = t;
                }
            }

            Exception e = new Exception("Executing SQL generated exceptions - see output for details");
            e.initCause(throwable);
            throw e;
        }
    }
}