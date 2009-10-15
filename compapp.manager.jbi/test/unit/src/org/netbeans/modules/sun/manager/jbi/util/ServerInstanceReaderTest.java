/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.sun.manager.jbi.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class ServerInstanceReaderTest {
            
    public ServerInstanceReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws URISyntaxException {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getServerInstances method, of class ServerInstanceReader.
     */
    @Test
    public void getServerInstances() throws URISyntaxException {
        System.out.println("getServerInstances");
        
        URI xmlURI = getClass().getResource("resources/nbattrs").toURI();
        File xmlFile = new File(xmlURI);
        ServerInstanceReader reader = new ServerInstanceReader(xmlFile.getAbsolutePath());
        
        List<ServerInstance> result = reader.getServerInstances();
        assertEquals(3, result.size());
    }
}
