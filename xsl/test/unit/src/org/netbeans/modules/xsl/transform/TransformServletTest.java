/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl.transform;

//import java.io.*;
import java.net.*;
//import java.util.*;

//import javax.servlet.*;
//import javax.servlet.http.*;

import junit.framework.*;
import org.netbeans.junit.*;

/*import org.openide.util.HttpServer;
import org.openide.filesystems.FileObject;
import org.openide.util.SharedClassObject;
import org.openide.filesystems.FileUtil;
import org.openide.execution.NbfsURLConnection;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xsl.utils.TransformUtil;
*/
/**
 *
 * @author Libor Kramolis
 */
public class TransformServletTest extends NbTestCase {
    
    public TransformServletTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformServletTest.class);
        
        return suite;
    }
    
        
    /** Test of getServletURL method, of class org.netbeans.modules.xsl.transform.TransformServlet. */
    public void testGetServletURL() {
        System.out.println("testGetServletURL");
        
        URL servletURL = null;
        boolean exceptionThrown = false;
        try {
            servletURL = TransformServlet.getServletURL();
        } catch (Exception exc) {
            System.err.println("!!! " + exc);
            exceptionThrown = true;
        }
        
        assertTrue ("I need correct Transform Servlet URL!", (servletURL!=null & exceptionThrown!= true));
    }
    
}
