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

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class CourseBean implements EntityBean, CourseRemoteBusiness {
    private static final String dbName = "jdbc/CollegeDB";
    private String courseId;
    private String name;
    private ArrayList studentIds;
    private Connection con;
    private EntityContext context;
    private EnrollerRemoteHome enrollerHome;

    public ArrayList getStudentIds() {
        System.out.println("in getStudentIds");

        return studentIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ejbCreate(String courseId, String name)
        throws CreateException {
        System.out.println("in ejbCreate");

        try {
            insertCourse(courseId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.courseId = courseId;
        this.name = name;
        System.out.println("about to leave ejbCreate");

        return courseId;
    }

    public String ejbFindByPrimaryKey(String primaryKey)
        throws FinderException {
        boolean result;

        try {
            result = selectByPrimaryKey(primaryKey);
        } catch (Exception ex) {
            throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
        }

        if (result) {
            return primaryKey;
        } else {
            throw new ObjectNotFoundException("Row for id " + primaryKey +
                " not found.");
        }
    }

    public void ejbRemove() {
        try {
            deleteCourse(courseId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        System.out.println("in setEntityContext");
        this.context = context;
        studentIds = new ArrayList();

        try {
            enrollerHome = lookupEnrollerBean();
        } catch (Exception ex) {
            throw new EJBException("Unable to connect to database. " +
                ex.getMessage());
        }
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
        courseId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        courseId = null;
    }

    public void ejbLoad() {
        System.out.println("in ejbLoad");

        try {
            loadCourse();
            loadStudentIds();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }

        System.out.println("leaving ejbLoad");
    }

    private void loadStudentIds() {
        System.out.println("in loadStudentIds");
        studentIds.clear();

        try {
            EnrollerRemote enroller = enrollerHome.create();
            ArrayList a = enroller.getStudentIds(courseId);

            studentIds.addAll(a);
        } catch (Exception ex) {
            throw new EJBException("loadStudentIds: " + ex.getMessage());
        }

        System.out.println("leaving loadStudentIds");
    }

    public void ejbStore() {
        System.out.println("in ejbStore");

        try {
            storeCourse();
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }

        System.out.println("leaving ejbStore");
    }

    public void ejbPostCreate(String courseId, String name) {
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

    private void insertCourse(String courseId, String name)
        throws SQLException {
        makeConnection();

        String insertStatement = "insert into course values ( ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, courseId);
        prepStmt.setString(2, name);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select courseid " + "from course where courseid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private void deleteCourse(String courseId) throws SQLException {
        makeConnection();

        String deleteStatement = "delete from course  " + "where courseid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, courseId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void loadCourse() throws SQLException {
        makeConnection();

        String selectStatement =
            "select name " + "from course where courseid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, courseId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            name = rs.getString(1);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for courseId " + courseId +
                " not found in database.");
        }

        releaseConnection();
    }

    private void storeCourse() throws SQLException {
        makeConnection();

        String updateStatement =
            "update course set name =  ? " + "where courseid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, name);
        prepStmt.setString(2, courseId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for courseId " + courseId +
                " failed.");
        }

        releaseConnection();
    }

    private enroller.EnrollerRemoteHome lookupEnrollerBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            Object remote = c.lookup("ejb/SimpleEnroller");
            enroller.EnrollerRemoteHome rv = (enroller.EnrollerRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, enroller.EnrollerRemoteHome.class);
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
     
    }
}
 // CourseBean 
