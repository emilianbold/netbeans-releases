/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

import java.sql.*;

public class CreateDerbyTravelDB {
    public static void main(String args[]) {
        new CreateDerbyTravelDB(args);
    }

    CreateDerbyTravelDB(String args[]) {
        try {
            Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver").newInstance(); //NOI18N
        } catch (Exception e) {
            System.err.println("Class.forName: " + e.getMessage()); //NOI18N
            System.exit(1);
        }

        createTravelDatabase(args);
    }

    void createTravelDatabase(String args[]) {
        String port = (args.length > 0)? args[0]: "1527";
        String url  = "jdbc:db2://localhost:" + port + "/travel;create=true"; //NOI18N

        Connection con;
        Statement stmt;

        try {
            con = DriverManager.getConnection(url, "dummy", "dummy"); //NOI18N

            stmt = con.createStatement();

            try {
                createUser(stmt, "travel", "travel"); //NOI18N
            } catch (Exception e) {
            }
            try {
                createSchema(stmt, "Travel", "travel"); //NOI18N
            } catch (Exception e) {
            }

            con.close();

            con = DriverManager.getConnection(url, "travel", "travel"); //NOI18N

            stmt = con.createStatement();

            createTable(stmt, "TripType", //NOI18N
                "TripTypeID INTEGER NOT NULL, Name VARCHAR(15), Description VARCHAR(50)"); //NOI18N

            addConstraint(stmt, "TripType", "TripTypePK PRIMARY KEY (TripTypeID)"); //NOI18N

            insertRow(stmt, "TripType", "1, 'TRNG', 'Training'"); //NOI18N
            insertRow(stmt, "TripType", "2, 'SALES', 'Sales'"); //NOI18N
            insertRow(stmt, "TripType", "3, 'OTHER', 'Other'"); //NOI18N
            insertRow(stmt, "TripType", "4, 'PR/AR', 'Press and Analyst Meeting'"); //NOI18N
            insertRow(stmt, "TripType", "5, 'OFFSITE', 'Offsite Meeting'"); //NOI18N
            insertRow(stmt, "TripType", "6, 'CONF', 'Conference/Tradeshow'"); //NOI18N
            insertRow(stmt, "TripType", "7, 'REM MTG', 'Remote Office Meeting'"); //NOI18N
            insertRow(stmt, "TripType", "8, 'CUST VST', 'Customer Visit'"); //NOI18N
            insertRow(stmt, "TripType", "9, 'RECRUIT', 'Recruiting'"); //NOI18N
            insertRow(stmt, "TripType", "10, 'BUS DEV', 'Business Development'"); //NOI18N

            createTable(stmt, "Person", "PersonID INTEGER NOT NULL, Name VARCHAR(50), " //NOI18N
                + "JobTitle VARCHAR(50), FrequentFlyer SMALLINT"); //NOI18N

            addConstraint(stmt, "Person", "PersonPK PRIMARY KEY (PersonID)"); //NOI18N

            insertRow(stmt, "Person", "1, 'Able, Tony', 'CEO', 1"); //NOI18N
            insertRow(stmt, "Person", "2, 'Black, John', 'VPO/CXO - SGMS', 1"); //NOI18N
            insertRow(stmt, "Person", "3, 'Kent, Richard', 'VP', 1"); //NOI18N
            insertRow(stmt, "Person", "4, 'Chen, Larry','VP/CXO - SGMS', 0"); //NOI18N
            insertRow(stmt, "Person", "5, 'Donaldson, Sue', 'VP', 1"); //NOI18N     

            createTable(stmt, "Trip", //NOI18N
                "TripID INTEGER NOT NULL, PersonID INTEGER NOT NULL, DepDate DATE, " + //NOI18N
                "DepCity VARCHAR(32), DestCity VARCHAR(32), TripTypeID INTEGER NOT NULL"); //NOI18N

            addConstraint(stmt, "Trip", "TripPK PRIMARY KEY (TripID)"); //NOI18N
            addConstraint(stmt, "Trip", "TripPersonFK FOREIGN KEY (PersonID) REFERENCES " + //NOI18N
                "Person (PersonID)"); //NOI18N
            addConstraint(stmt, "Trip", "TripTypeFK FOREIGN KEY (TripTypeID) REFERENCES " + //NOI18N
                "TripType (TripTypeID)"); //NOI18N

            //Tony Able
            insertRow(stmt, "Trip", "128, 1, '2003-06-16', 'San Francisco', 'New York', 4"); //NOI18N
            insertRow(stmt, "Trip", "199, 1, '2003-09-14', 'San Francisco', 'New York', 4"); //NOI18N
            insertRow(stmt, "Trip", "202, 1, '2003-10-22', 'San Francisco', 'Toronto', 4"); //NOI18N
            insertRow(stmt, "Trip", "203, 1, '2003-11-23', 'San Francisco', 'Tokyo', 5"); //NOI18N
            insertRow(stmt, "Trip", "367, 1, '2003-12-12', 'San Francisco', 'Chicago', 2"); //NOI18N
            //John Black
            insertRow(stmt, "Trip", "100, 4, '2004-05-01', 'Aspen', 'San Francisco', 7"); //NOI18N
            insertRow(stmt, "Trip", "159, 4, '2003-09-01', 'Aspen','Park City', 4"); //NOI18N
            insertRow(stmt, "Trip", "252, 4, '2003-11-01', 'Aspen','Chicago', 4"); //NOI18N
            insertRow(stmt, "Trip", "359, 4, '2004-01-26', 'Aspen','Los Angeles', 4"); //NOI18N
            insertRow(stmt, "Trip", "460, 4, '2004-05-06', 'Aspen', 'San Francisco', 6"); //NOI18N
            //Richard Kent
            insertRow(stmt, "Trip", "200, 2, '2004-06-11', 'San Jose', 'Washington DC', 3"); //NOI18N
            insertRow(stmt, "Trip", "310, 2, '2003-08-03', 'San Jose', 'Washington DC', 3"); //NOI18N
            insertRow(stmt, "Trip", "333, 2, '2004-02-02', 'San Jose', 'Tokyo', 5"); //NOI18N
            insertRow(stmt, "Trip", "422, 2, '2004-04-11', 'San Jose', 'Washington DC', 3"); //NOI18N
            insertRow(stmt, "Trip", "455, 2, '2004-05-13', 'San Jose', 'Stockholm', 8"); //NOI18N
            //Larry Chen
            insertRow(stmt, "Trip", "592, 3, '2003-06-16', 'San Jose', 'Novosibirsk', 10"); //NOI18N
            insertRow(stmt, "Trip", "201, 3, '2003-07-01', 'San Jose', 'Washington DC', 8"); //NOI18N
            insertRow(stmt, "Trip", "590, 3, '2003-08-11', 'San Jose', 'Orlando', 6"); //NOI18N
            insertRow(stmt, "Trip", "380, 3, '2003-10-23', 'San Jose', 'Washington DC', 3"); //NOI18N
            insertRow(stmt, "Trip", "421, 3, '2003-11-09', 'San Jose', 'Washington DC', 3"); //NOI18N
            //Sue Donaldson
            insertRow(stmt, "Trip", "198, 5, '2004-06-11', 'San Jose', 'Grenoble', 3"); //NOI18N
            insertRow(stmt, "Trip", "208, 5, '2003-06-21', 'San Jose',  'Washington DC', 2"); //NOI18N
            insertRow(stmt, "Trip", "383, 5, '2003-10-23', 'San Jose', 'Grenoble', 3"); //NOI18N
            insertRow(stmt, "Trip", "420, 5, '2004-06-11', 'San Jose', 'Philadelphia', 8"); //NOI18N
            insertRow(stmt, "Trip", "463, 5, '2004-05-26', 'San Jose', 'Los Angeles', 6"); //NOI18N

            createTable(stmt, "NoRelation", "Col1 INTEGER NOT NULL, Col2 VARCHAR(15)"); //NOI18N

            createView(stmt, "PersonTrip", "select tripid, name from trip, person where " + //NOI18N
                "trip.personid = person.personid"); //NOI18N

            System.out.println("Travel database was created."); 

            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("ERROR: SQLException: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    void createUser(Statement stmt, String username, String password) throws SQLException {
        try {
            stmt.executeUpdate("DROP USER " + username); //NOI18N
        } catch (SQLException e) {
        }
        stmt.executeUpdate("CREATE USER " + username + " PASSWORD " + password); //NOI18N
    }

    void createSchema(Statement stmt, String schemaName, String username) throws SQLException {
        try {
            stmt.executeUpdate("DROP SCHEMA " + schemaName); //NOI18N
        } catch (SQLException e) {
        }
        stmt.executeUpdate("CREATE SCHEMA " + schemaName + " AUTHORIZATION " + username); //NOI18N
    }

    void createTable(Statement stmt, String name, String cols) throws SQLException {
        try {
            stmt.executeUpdate("DROP TABLE " + name); //NOI18N
        } catch (SQLException e) {
        }
        stmt.executeUpdate("CREATE TABLE " + name +  " (" + cols + ")"); //NOI18N
    }

    void createView(Statement stmt, String name, String select) throws SQLException {
        try {
            stmt.executeUpdate("DROP VIEW " + name); //NOI18N
        } catch (SQLException e) {
        }
        stmt.executeUpdate("CREATE VIEW " + name +  " AS " + select); //NOI18N
    }

    void addConstraint(Statement stmt, String name, String constraint) throws SQLException {
        stmt.executeUpdate("ALTER TABLE " + name +  " ADD CONSTRAINT " + constraint); //NOI18N
    }

    void insertRow(Statement stmt, String name, String values) throws SQLException {
        stmt.executeUpdate("INSERT INTO " + name + " VALUES (" + values + ")"); //NOI18N
    }
}
