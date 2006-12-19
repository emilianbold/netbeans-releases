/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.db.derby;




import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.modules.derby.StartAction;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.StopAction;
import org.netbeans.test.db.util.DbUtil;
import org.openide.util.actions.SystemAction;



/*
 * MyTestCase.java
 *
 * Created on February 6, 2006, 1:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author lg198683
 */
public class DerbyDatabaseTest extends DbJellyTestCase {
    
    public static final String DERBY_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    public static final String PORT="1527";
    public static String location="";
    
    public DerbyDatabaseTest(String s) {
        
        super(s);
        debug("derby location:"+location);
    }
    
    
    
    public void testStartAction() {
       debug("Starting Java DB server"); 
       StartAction start=(StartAction)SystemAction.get(StartAction.class);
       start.performAction();
       sleep(2000);
      
    }
    
    public void testStopAction() throws Exception {
        debug("Stoping Java DB server");

        StopAction stop=(StopAction)SystemAction.get(StopAction.class);
        stop.performAction();
                
    }
    
    
    public void testConnect() throws Exception{
        debug("Connection to Java DB server");
        DbUtil util=new DbUtil(location);
        String s="jdbc:derby://localhost:1527/db;create=true";
        Connection con=util.createConnection(s);
        con.close();
        
   }
        
    
    
    
    
    public static Test suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new DerbyDatabaseTest("testStartAction")); 
        suite.addTest(new DerbyDatabaseTest("testConnect"));
        suite.addTest(new DerbyDatabaseTest("testStopAction"));
        TestSetup setup=new TestSetup(suite){
            
            protected void setUp() throws Exception{
                init();
            }
            
            protected void tearDown() throws Exception {

            }
        };
     
        return setup;
    }

    private static void init() throws Exception {
        location=DerbyOptions.getDefault().getLocation();
        //String systemHome=System.getProperty("xtest.tmpdir");
        //DerbyOptions.getDefault().setLocation(location);
        //DerbyOptions.getDefault().setSystemHome(systemHome);
        
    }
    
    

    
    
   
    
}
