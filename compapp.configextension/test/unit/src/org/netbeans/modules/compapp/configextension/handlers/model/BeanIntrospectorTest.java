/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.model;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class BeanIntrospectorTest {

    public BeanIntrospectorTest() {
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
     * Test of getParameters method, of class BeanIntrospector.
     */
    @Test
    public void testGetParameters() 
            throws MalformedURLException, ClassNotFoundException, IntrospectionException {
        System.out.println("getParameters");
        String jarPath = "C:/nb_hg/hg-soabi.stc.com/gfesbv22/nbbuild/netbeans/soa/modules/org-netbeans-modules-compapp-configextension.jar";
        String className = "org.netbeans.modules.compapp.configextension.handlers.model.HandlerParameter";
        List<HandlerParameter> result = BeanIntrospector.getParameters(
                Arrays.asList(new String[] {jarPath}), className, true);
        assertEquals(4, result.size());
    }

    /**
     * Test of getSubClasses method, of class BeanIntrospector.
     */
    @Test
    public void testGetSubClasses() throws ClassNotFoundException, IOException {
        System.out.println("getSubClasses");
        String jarPath = "C:/nb_hg/hg-soabi.stc.com/gfesbv22/nbbuild/netbeans/soa/modules/org-netbeans-modules-compapp-configextension.jar";
        Class baseClass = org.netbeans.modules.compapp.configextension.handlers.model.Handler.class;

        List<Class> result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClass, true);
        assertEquals(1, result.size());

        result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClass, false);
        assertEquals(0, result.size());

        jarPath = "C:/Documents and Settings/jqian/My Documents/NetBeansProjects/MyJAXWSHandlerProject/dist/MyJAXWSHandlerProject.jar";
        baseClass = javax.xml.ws.handler.Handler.class;
        result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClass, false);
        assertEquals(3, result.size());
    }

    /**
     * Test of isDecendent method, of class BeanIntrospector.
     */
    @Test
    public void testIsDecendent() throws ClassNotFoundException, IOException {
        System.out.println("isDecendent");
        Class baseClass = java.awt.event.ActionListener.class;

        Class clazz = java.awt.event.ActionListener.class;
        boolean result = BeanIntrospector.isDecendent(clazz, baseClass, true);
        assertTrue(result);
        result = BeanIntrospector.isDecendent(clazz, baseClass, false);
        assertFalse(result);

        clazz = javax.swing.Action.class;
        result = BeanIntrospector.isDecendent(clazz, baseClass, true);
        assertTrue(result);

        clazz = javax.swing.AbstractAction.class;
        result = BeanIntrospector.isDecendent(clazz, baseClass, true);
        assertTrue(result);

        clazz = javax.swing.text.TextAction.class;
        result = BeanIntrospector.isDecendent(clazz, baseClass, true);
        assertTrue(result);

        clazz = javax.swing.JComponent.class;
        result = BeanIntrospector.isDecendent(clazz, baseClass, true);
        assertFalse(result);

    }
    
     /**
     * Test of getSubClasses method, of class BeanIntrospector.
     */
    @Test
    public void testGetSubClasses2() throws ClassNotFoundException, IOException {
        System.out.println("getSubClasses");
       
        String jarPath = "C:/Documents and Settings/jqian/My Documents/NetBeansProjects/MyJAXRSFilterProject/dist/MyJAXRSFilterProject.jar";
        String baseClassName = "com.sun.jersey.api.client.ClientHandler";
        List<Class> result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClassName, false);
        assertEquals(1, result.size());

        List<String> baseClassNames = new ArrayList<String>();
        baseClassNames.add("com.sun.jersey.spi.container.ContainerRequestFilter");
        baseClassNames.add("com.sun.jersey.spi.container.ContainerResponseFilter");
        result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClassNames, false);
        assertEquals(2, result.size());

        baseClassName = "java.lang.Object";
        result = BeanIntrospector.getSubClasses(
                Arrays.asList(new String[] {jarPath}), baseClassName, false);
        assertEquals(3, result.size());
    }

}