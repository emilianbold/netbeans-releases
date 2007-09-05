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

import java.sql.Connection;
import junit.framework.Test;
import org.netbeans.modules.derby.StartAction;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.StopAction;
import org.netbeans.test.db.util.DbUtil;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author lg198683
 */
public class DerbyDatabaseTest extends DbJellyTestCase {
    
    public DerbyDatabaseTest(String s) {
        super(s);
    }
    
    public void testStartAction() {
       StartAction start=SystemAction.get(StartAction.class);
       start.performAction();
       sleep(15000);
    }
    
    public void testStopAction() throws Exception {
        StopAction stop=SystemAction.get(StopAction.class);
        stop.performAction();
    }
    
    public void testConnect() throws Exception{
        String url="jdbc:derby://localhost:1527/db;create=true";
        Connection con=DbUtil.createDerbyConnection(url);
        con.close();
   }
    
   public static Test suite() {
        String tmpdir = System.getProperty("xtest.tmpdir");
        System.out.println("> Setting the Derby System Home to: "+tmpdir);
        DerbyOptions.getDefault().setSystemHome(tmpdir);
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new DerbyDatabaseTest("testStartAction")); 
        suite.addTest(new DerbyDatabaseTest("testConnect"));
        suite.addTest(new DerbyDatabaseTest("testStopAction"));
        return suite;
    }
}
