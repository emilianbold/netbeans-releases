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

package salesrep;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;


public class CustomerBean implements EntityBean, CustomerRemoteBusiness {
    private static final String dbName = "java:comp/env/jdbc/SalesDB";
    private String customerId;
    private String salesRepId;
    private String name;
    private Connection con;
    private EntityContext context;

    public String getSalesRepId() {
        return salesRepId;
    }

    public String getName() {
        System.out.println("entering getName()");

        return name;
    }

    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ejbCreate(String customerId, String salesRepId, String name)
        throws CreateException {
        System.out.println("in ejbCreate");

        try {
            insertCustomer(customerId, salesRepId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.customerId = customerId;
        this.salesRepId = salesRepId;
        this.name = name;

        System.out.println("about to leave ejbCreate");

        return customerId;
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

    public Collection ejbFindBySalesRep(String salesRepId)
        throws FinderException {
        Collection result;

        try {
            result = selectBySalesRep(salesRepId);
        } catch (Exception ex) {
            throw new EJBException("ejbFindBySalesRep: " + ex.getMessage());
        }

        return result;
    }

    public void ejbRemove() {
        try {
            deleteCustomer(customerId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
        customerId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        customerId = null;
    }

    public void ejbLoad() {
        System.out.println("in ejbLoad");

        try {
            loadCustomer();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }

        System.out.println("leaving ejbLoad");
    }

    public void ejbStore() {
        System.out.println("in ejbStore");

        try {
            storeCustomer();
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }

        System.out.println("leaving ejbStore");
    }

    public void ejbPostCreate(String customerId, String salesRepId, String name) {
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

    private void insertCustomer(String customerId, String salesRepId,
        String name) throws SQLException {
        makeConnection();

        String insertStatement = "insert into customer values ( ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, customerId);
        prepStmt.setString(2, salesRepId);
        prepStmt.setString(3, name);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select customerid " + "from customer where customerid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private Collection selectBySalesRep(String salesRepId)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select customerid " + "from customer where salesrepid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, salesRepId);

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

    private void deleteCustomer(String customerId) throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from customer  " + "where customerid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, customerId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void loadCustomer() throws SQLException {
        makeConnection();

        String selectStatement =
            "select customerid, salesRepid, name " +
            "from customer where customerid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, customerId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            customerId = rs.getString(1);
            salesRepId = rs.getString(2);
            name = rs.getString(3);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for customerId " + customerId +
                " not found in database.");
        }

        releaseConnection();
    }

    private void storeCustomer() throws SQLException {
        makeConnection();
        System.out.println("entering storeCustomer");

        String updateStatement =
            "update customer " + "set salesRepid = ? , name = ? " +
            "where customerid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, salesRepId);
        prepStmt.setString(2, name);
        prepStmt.setString(3, customerId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for customerId " + customerId +
                " failed.");
        }

        releaseConnection();
        System.out.println("leaving storeCustomer");
    }
}
 // CustomerBean 