/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import org.netbeans.junit.NbTestCase;

/**
 * @author Martin Krauskopf
 */
public class ClassPathParserTest extends NbTestCase {
    
    public ClassPathParserTest(String testName) {
        super(testName);
    }
    
    public void testParse_71770() throws Exception {
        String cpS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +
                "<classpath>" +
                "<classpathentry kind=\"src\" path=\"\"/>" +
                "<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/integrationServerApi.jar\">" +
                "<attributes>" +
                "<attribute value=\"jar:file:/C:/MyProjects/JavaAPI/docs/javaApiDoc.jar!/\" name=\"javadoc_location\"/>" +
                "</attributes>" +
                "</classpathentry>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/wsdl4j.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/activation.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/axis.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-codec.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-collections-3.1.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-configuration-1.0.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-discovery.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-httpclient.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-lang-2.0.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/commons-logging.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/jaxrpc.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/log4j-1.2.8.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/mailapi.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/saaj.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/servlet.jar\"/>" +
                "<classpathentry kind=\"lib\" path=\"C:/MyProjects/JavaAPI/util-concurrent.jar\"/>" +
                "<classpathentry kind=\"output\" path=\"\"/>" +
                "</classpath>";
        ClassPath cp = ClassPathParser.parse(cpS);
        assertEquals("nineteen classpath entries", 19, cp.getEntries().size());
    }
    
}
