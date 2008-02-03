/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.service;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;

/**
 * This class tests the services provided by HibernateEnvironment class
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateEnvironmentTest extends NbTestCase {

    private HibernateConfiguration hibernateConfiguration;

    private ExecutorTask task;

    public HibernateEnvironmentTest(String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() {
        hibernateConfiguration = new HibernateConfiguration().createGraph((java.io.InputStream) new java.io.ByteArrayInputStream(hibConfigString.getBytes()));

    }

    @After
    @Override
    public void tearDown() {
        try {
            hibernateConfiguration = null;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of addConfiguration method, of class HibernateEnvironment.
     */
    @Test
    public void testAddConfiguration() {
        System.out.println("addConfiguration");

        HibernateEnvironment instance = new HibernateEnvironment();
        instance.addConfiguration(hibernateConfiguration);
        ArrayList<HibernateConfiguration> allConfigurations = instance.getAllHibernateConfigurations();
        assertEquals(hibernateConfiguration, allConfigurations.get(0));
    }

    /**
     * Test of removeConfiguration method, of class HibernateEnvironment.
     */
    @Test
    public void testRemoveConfiguration() {
        System.out.println("removeConfiguration");

        HibernateEnvironment instance = new HibernateEnvironment();
        instance.removeConfiguration(hibernateConfiguration);
        ArrayList<HibernateConfiguration> allConfigurations = instance.getAllHibernateConfigurations();
        assertEquals(0, allConfigurations.size());
    }

    /**
     * Test of getAllDatabaseTables method, of class HibernateEnvironment.
     */
    @Test
    public void testGetAllDatabaseTables() {
        System.out.println("getAllDatabaseTables");

        Util.startDB(getDataDir().getAbsolutePath() + java.io.File.separator + "db-derby-10.2.2.0-bin");
        Util.prepareDB();
        HibernateConfiguration[] configurations = new HibernateConfiguration[1];
        configurations[0] = hibernateConfiguration;
        HibernateEnvironment instance = new HibernateEnvironment();
        ArrayList<String> expResult = Util.getAllDatabaseTables();

        ArrayList<String> result = instance.getAllDatabaseTables(configurations);

        Util.clearDB();
        Util.stopDB(getDataDir().getAbsolutePath() + java.io.File.separator + "db-derby-10.2.2.0-bin");
        assertEquals(expResult, result);


    }

    /**
     * Test of getAllHibernateConfigurations method, of class HibernateEnvironment.
     */
    @Test
    public void testGetAllHibernateConfigurations() {
        System.out.println("getAllHibernateConfigurations");
        HibernateEnvironment instance = new HibernateEnvironment();
        ArrayList<HibernateConfiguration> expResult = new ArrayList<HibernateConfiguration>();

        expResult.add(hibernateConfiguration);
        instance.addConfiguration(hibernateConfiguration);

        ArrayList<HibernateConfiguration> result = instance.getAllHibernateConfigurations();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAllHibernateMappings method, of class HibernateEnvironment.
     */
    @Test
    public void testGetAllHibernateMappingsFromConfiguration() {
        System.out.println("getAllHibernateMappings");
        HibernateEnvironment instance = new HibernateEnvironment();
        ArrayList<String> expResult = new ArrayList<String>();
        //TODO now hardcoded. Need to be retrieved from config.
        expResult.add("map1.xml"); expResult.add("map2.xml"); expResult.add("map3.xml");
        expResult.add("map$1.xml");

        ArrayList<String> result = instance.getAllHibernateMappingsFromConfiguration(hibernateConfiguration);
        assertEquals(expResult, result);

    }

    /* This string contains the sample test data for hibernate configurations */
    private String hibConfigString = "<?xml version='1.0' encoding='utf-8'?>" +
            "<!DOCTYPE hibernate-configuration PUBLIC " +
            "\"-//Hibernate/Hibernate Configuration DTD 3.0//EN\" " +
            "\"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">" +
            "<hibernate-configuration>" +
            "<session-factory>" +
            "<!-- Database connection settings -->" +
            //org.apache.derby.jdbc.EmbeddedDriver
            "<property name=\"hibernate.connection.driver_class\">org.apache.derby.jdbc.ClientDriver</property>" +
            "<property name=\"hibernate.connection.url\">jdbc:derby://localhost:1527/derbyDB</property>" +
            //jdbc:derby://localhost:1527/samplejdbc:derby:sample
            "<property name=\"hibernate.connection.username\">user1</property>" +
            "<property name=\"hibernate.connection.password\">user1</property>" +
            "<!-- JDBC connection pool (use the built-in) -->" +
            "<property name=\"hibernate.connection.pool_size\">1</property>" +
            "<!-- SQL dialect -->" +
            "<property name=\"hibernate.dialect\">org.hibernate.dialect.DerbyDialect</property>" +
            "<!-- Enable Hibernate's automatic session context management -->" +
            "<property name=\"current_session_context_class\">thread</property>" +
            "<!-- Disable the second-level cache  -->" +
            "<property name=\"cache.provider_class\">org.hibernate.cache.NoCacheProvider</property>" +
            "<!-- Echo all executed SQL to stdout -->" +
            "<property name=\"show_sql\">true</property>" +
            "<!-- Drop and re-create the database schema on startup -->" +
            "<property name=\"hbm2ddl.auto\">create</property>" +
            "<mapping resource=\"map1.xml\"/>" +
            "<mapping resource=\"map2.xml\"/>" +
            "<mapping resource=\"map3.xml\"/>" +
            "<mapping resource=\"map$1.xml\"/>" +
            "</session-factory>" +
            "</hibernate-configuration>";
}
