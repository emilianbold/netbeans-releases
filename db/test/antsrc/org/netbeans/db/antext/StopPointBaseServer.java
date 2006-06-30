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

package org.netbeans.db.antext;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class implementing way how to shutdown PointDase server.
 * Necessary system properties: <tt>pointbase.db.driverclass</tt>, <tt>pointbase.db.url</tt>, <tt>pointbase.db.user</tt>, <tt>pointbase.db.password</tt>.
 * This class is called from <tt>pointbase-server.xml</tt> script.
 *
 * @author Patrik Knakal
 * @version 1.0
 */
public class StopPointBaseServer extends Object {
    private static final String shutdown = "shutdown force"; //NOI18N

    public static void main(String[] atrg) {
        //System.out.println("driverclass ... " + System.getProperty("pointbase.db.driverclass") ); //NOI18N
        //System.out.println("url         ... " + System.getProperty("pointbase.db.url") ); //NOI18N
        //System.out.println("user        ... " + System.getProperty("pointbase.db.user") ); //NOI18N
        //System.out.println("password    ... " + System.getProperty("pointbase.db.password") ); //NOI18N
        try {
            Class.forName(System.getProperty("pointbase.db.driverclass") ); //NOI18N
            Connection connection = DriverManager.getConnection(System.getProperty("pointbase.db.url"), System.getProperty("pointbase.db.user"), System.getProperty("pointbase.db.password") ); //NOI18N
            Statement statement = connection.createStatement();
            statement.executeUpdate(shutdown);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
