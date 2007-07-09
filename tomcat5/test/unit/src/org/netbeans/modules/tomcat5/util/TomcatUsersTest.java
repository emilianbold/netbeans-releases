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
package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.FileWriter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sherold
 */
public class TomcatUsersTest extends NbTestCase {

    private final String CONTENT = "<tomcat-users>\n" +
                         "<user name='tomcat' password='tomcat' roles='tomcat,manager' />\n" +
                         "<user name='ide'  password='tomcat' roles='role1'  />\n" +
                         "<user name='test'  password='tomcat' roles='manager,admin,role1'  />\n" +
                         "</tomcat-users>\n";
    
    private final String CONTENT2 = "<tomcat-users>\n" +
                         "<user username='tomcat' password='tomcat' roles='tomcat,manager' />\n" +
                         "<user username='ide'  password='tomcat' roles='role1'  />\n" +
                         "<user username='test'  password='tomcat' roles='manager,admin,role1'  />\n" +
                         "</tomcat-users>\n";
    
    public TomcatUsersTest(String testName) {
        super(testName);
    }
    
    public void testHasRole() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertTrue(TomcatUsers.hasManagerRole(file, "tomcat"));
        assertTrue(TomcatUsers.hasManagerRole(file, "test"));
        assertFalse(TomcatUsers.hasManagerRole(file, "ide"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertTrue(TomcatUsers.hasManagerRole(file, "tomcat"));
        assertTrue(TomcatUsers.hasManagerRole(file, "test"));
        assertFalse(TomcatUsers.hasManagerRole(file, "ide"));
    }
    
    public void testCreateUser() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertFalse(TomcatUsers.hasManagerRole(file, "ide"));
        TomcatUsers.createUser(file, "ide", "tomcat");
        assertTrue(TomcatUsers.hasManagerRole(file, "ide"));
        assertFalse(TomcatUsers.hasManagerRole(file, "nonexisting"));
        TomcatUsers.createUser(file, "new", "tomcat");
        assertTrue(TomcatUsers.hasManagerRole(file, "new"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertFalse(TomcatUsers.hasManagerRole(file, "ide"));
        TomcatUsers.createUser(file, "ide", "tomcat");
        assertTrue(TomcatUsers.hasManagerRole(file, "ide"));
        assertFalse(TomcatUsers.hasManagerRole(file, "nonexisting"));
        TomcatUsers.createUser(file, "new", "tomcat");
        assertTrue(TomcatUsers.hasManagerRole(file, "new"));
    }
    
    public void testUserExists() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertTrue(TomcatUsers.userExists(file, "tomcat"));
        assertTrue(TomcatUsers.userExists(file, "test"));
        assertFalse(TomcatUsers.userExists(file, "nonexisting"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertTrue(TomcatUsers.userExists(file, "tomcat"));
        assertTrue(TomcatUsers.userExists(file, "test"));
        assertFalse(TomcatUsers.userExists(file, "nonexisting"));
    }
    
    private File createTomcatUsersXml(String fileName, String content) throws Exception {
        File file = new File(getWorkDir(), fileName);
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
        return file;
    }

}
