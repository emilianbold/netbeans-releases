/*
 * Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package enroller;

import java.rmi.RemoteException;
import javax.ejb.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.naming.*;


public class EnrollerBean implements SessionBean, EnrollerRemoteBusiness {
    private static final String dbName = "jdbc/pointbase";
    private Connection con;
    private SessionContext context;

    public EnrollerBean() {
    }

    public void enroll(String studentId, String courseId) {
        try {
            insertEntry(studentId, courseId);
        } catch (Exception ex) {
            throw new EJBException("enroll: " + ex.getMessage());
        }
    }

    public void unEnroll(String studentId, String courseId) {
        try {
            deleteEntry(studentId, courseId);
        } catch (Exception ex) {
            throw new EJBException("unEnroll: " + ex.getMessage());
        }
    }

    public void deleteStudent(String studentId) {
        try {
            deleteStudentEntries(studentId);
        } catch (Exception ex) {
            throw new EJBException("deleteStudent: " + ex.getMessage());
        }
    }

    public void deleteCourse(String courseId) {
        try {
            deleteCourseEntries(courseId);
        } catch (Exception ex) {
            throw new EJBException("deleteCourse: " + ex.getMessage());
        }
    }

    public ArrayList getStudentIds(String courseId) {
        try {
            return selectStudent(courseId);
        } catch (Exception ex) {
            throw new EJBException("getStudentIds: " + ex.getMessage());
        }
    }

    public ArrayList getCourseIds(String studentId) {
        try {
            return selectCourse(studentId);
        } catch (Exception ex) {
            throw new EJBException("getCourseIds: " + ex.getMessage());
        }
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext(SessionContext context) {
        this.context = context;
    }

    /*********************** Database Routines *************************/
    private void makeConnection() {
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dbName);

            con = ds.getConnection();
        } catch (Exception ex) {
            throw new EJBException("Unable to connect to database. " +
                ex.getMessage());
        }
    }

    private void releaseConnection() {
        try {
            con.close();
        } catch (SQLException ex) {
            throw new EJBException("releaseConnection: " + ex.getMessage());
        }
    }

    private void insertEntry(String studentId, String courseId)
        throws SQLException {
        makeConnection();

        String insertStatement = "insert into enrollment values ( ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteEntry(String studentId, String courseId)
        throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from enrollment " + "where studentid = ? and courseid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.setString(2, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteStudentEntries(String studentId)
        throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from enrollment " + "where studentid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, studentId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteCourseEntries(String courseId) throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from enrollment " + "where courseid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private ArrayList selectStudent(String courseId) throws SQLException {
        makeConnection();

        String selectStatement =
            "select studentid " + "from enrollment where courseid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, courseId);

        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);

            a.add(id);
        }

        prepStmt.close();
        releaseConnection();

        return a;
    }

    private ArrayList selectCourse(String studentId) throws SQLException {
        makeConnection();

        String selectStatement =
            "select courseid " + "from enrollment where studentid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, studentId);

        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);

            a.add(id);
        }

        prepStmt.close();
        releaseConnection();

        return a;
    }
}
 // EnrollerBean
