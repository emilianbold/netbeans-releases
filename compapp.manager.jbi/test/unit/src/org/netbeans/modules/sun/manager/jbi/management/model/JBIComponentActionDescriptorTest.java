/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.sun.manager.jbi.actions.MBeanOperationAction;
import org.netbeans.modules.sun.manager.jbi.actions.MBeanOperationGroupAction;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class JBIComponentActionDescriptorTest {

    public JBIComponentActionDescriptorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getActions method, of class ComponentActionDescriptor.
     */
    @Test
    public void getActions() throws URISyntaxException {

        System.out.println("getActions");

        URI xmlURI = getClass().getResource("resources/sun-bpel-engine-actions.xml").toURI();
        File xmlFile = new File(xmlURI);
        String xmlText = getContent(xmlFile);

        List<MBeanOperationAction> result =
                JBIComponentActionDescriptor.getActions(xmlText);
        assertEquals(2, result.size());
        assertTrue(result.get(1) instanceof MBeanOperationGroupAction);

        MBeanOperationGroupAction groupAction = (MBeanOperationGroupAction) result.get(1);
        assertEquals(2, groupAction.getActions().size());
    }

    private String getContent(File file) {
        String ret = "";

        BufferedReader is = null;
        try {
            is = new BufferedReader(new FileReader(file));
            String inputLine;
            while ((inputLine = is.readLine()) != null) {
                ret += inputLine;
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }

        return ret;
    }
}
