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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.db.derby;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.modules.db.derby.CreateJavaDBDatabaseOperator;
import org.netbeans.jellytools.modules.db.derby.actions.CreateDatabaseAction;
import org.netbeans.jellytools.modules.db.derby.actions.StopServerAction;
import org.netbeans.jellytools.modules.db.nodes.ConnectionNode;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.StartAction;
import org.openide.util.actions.SystemAction;

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
        // <workaround for #112788> - FIXME
        SystemAction.get(StartAction.class).performAction();
        sleep(2000);
        // </workaround>
        new CreateDatabaseAction().perform();
        CreateJavaDBDatabaseOperator operator = new CreateJavaDBDatabaseOperator();
        operator.setDatabaseName(DB);
        operator.setUserName(USER);
        operator.setPassword(PASSWORD);
        operator.ok();
    }
    
    public void testConnect() throws Exception {
        ConnectionNode connection=ConnectionNode.invoke(URL,USER,PASSWORD);   
        connection.connect();
        sleep(2000);
        connection.disconnect();
        sleep(1000);
    }
    
    public void testStopServer(){
         new StopServerAction().perform();
         sleep(5000);
    }
    
    public static Test suite() {
        String tmpdir = System.getProperty("xtest.tmpdir");
        System.out.println("> Setting the Derby System Home to: "+tmpdir);
        DerbyOptions.getDefault().setSystemHome(tmpdir);
        
        TestSuite suite=new TestSuite();
        suite.addTest(new CreateDatabaseTest("testCreateDatabase"));
        suite.addTest(new CreateDatabaseTest("testConnect"));
        suite.addTest(new CreateDatabaseTest("testStopServer"));        
        return suite;
    }
}
