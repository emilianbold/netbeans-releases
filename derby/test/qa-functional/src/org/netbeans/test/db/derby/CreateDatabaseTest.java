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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.db.nodes.ConnectionNode;
import org.netbeans.jellytools.modules.db.nodes.DatabasesNode;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.test.db.derby.lib.CreateDerbyDatabaseOperator;

/**
 *
 * @author lg198683
 */
public class CreateDatabaseTest  extends DbJellyTestCase {
    private static String location="";
    
    private static String USER="czesiu";
    
    private static String PASSWORD="czesiu";
    
    private static String DB="newdatabase";
    
    private static String URL="jdbc:derby://localhost:1527/newdatabase";

    public CreateDatabaseTest(String s) {
        super(s);
    }
    
    public void testCreateDatabase(){
        debug("Creating Java DB Database");
        new ActionNoBlock("Tools|Java DB Database|Create Database...", null).perform();
        CreateDerbyDatabaseOperator operator=new CreateDerbyDatabaseOperator();
        operator.typeDatabaseName(DB);
        operator.typeUserName(USER);
        operator.typePassword(PASSWORD);
        operator.ok();
        debug("Database created");
        sleep(2000);
        
    }
    
    public void testConnect() throws Exception {
        debug("Connection to Java DB server");
        ConnectionNode connection=ConnectionNode.invoke(URL,USER,PASSWORD);   
        connection.connect();
        debug("Disconecting");
        sleep(2000);
        connection.disconnect();
        sleep(1000);
    }
    
    
    public void testStopServer(){
         debug("Stopping database derver");
         new ActionNoBlock("Tools|Java DB Database|Stop Java DB Server", null).perform();
    }
    
    public static Test suite() {
        TestSuite suite=new TestSuite();
        suite.addTest(new CreateDatabaseTest("testCreateDatabase"));
        suite.addTest(new CreateDatabaseTest("testConnect"));
        suite.addTest(new CreateDatabaseTest("testStopServer"));        
        TestSetup setup=new TestSetup(suite){
           public void setUp() throws Exception {
               init();
           } 
           
        };
        return setup;
        
    }
    
    public static void init() throws Exception {
       // location=System.getProperty("derby.path");
        String systemHome=System.getProperty("xtest.tmpdir");
      //  DerbyOptions.getDefault().setLocation(location);
        DerbyOptions.getDefault().setSystemHome(systemHome);
    }
    
    
    
   
    
}
