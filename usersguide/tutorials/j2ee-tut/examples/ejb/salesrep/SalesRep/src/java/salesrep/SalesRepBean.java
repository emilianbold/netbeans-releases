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
import javax.rmi.PortableRemoteObject;


public class SalesRepBean implements EntityBean {
    private static final String dbName = "java:comp/env/jdbc/SalesDB";
    private String salesRepId;
    private String name;
    private ArrayList customerIds;
    private Connection con;
    private EntityContext context;
    private CustomerRemoteHome customerHome;

    public ArrayList getCustomerIds() {
        System.out.println("in getCustomerIds");

        return customerIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ejbCreate(String salesRepId, String name)
        throws CreateException {
        System.out.println("in ejbCreate");

        try {
            insertSalesRep(salesRepId, name);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.salesRepId = salesRepId;
        this.name = name;
        System.out.println("about to leave ejbCreate");

        return salesRepId;
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
            deleteSalesRep(salesRepId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        System.out.println("in setEntityContext");
        this.context = context;
        customerIds = new ArrayList();

        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/Customer");

            customerHome =
                (CustomerRemoteHome) PortableRemoteObject.narrow(objref,
                    CustomerRemoteHome.class);
        } catch (Exception ex) {
            throw new EJBException("setEntityContext: " + ex.getMessage());
        }

        System.out.println("leaving setEntityContext");
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
        salesRepId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        salesRepId = null;
    }

    public void ejbLoad() {
        System.out.println("in ejbLoad");

        try {
            loadSalesRep();
            loadCustomerIds();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }

        System.out.println("leaving ejbLoad");
    }

    private void loadCustomerIds() {
        System.out.println("in loadCustomerIds");
        customerIds.clear();

        try {
            Collection c = customerHome.findBySalesRep(salesRepId);
            Iterator i = c.iterator();

            while (i.hasNext()) {
                CustomerRemote customer = (CustomerRemote) i.next();
                String id = (String) customer.getPrimaryKey();

                System.out.println("adding " + id + " to list");
                customerIds.add(id);
            }
        } catch (Exception ex) {
            throw new EJBException("Exception in loadCustomerIds: " +
                ex.getMessage());
        }
    }

    public void ejbStore() {
        System.out.println("in ejbStore");

        try {
            storeSalesRep();
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }

        System.out.println("leaving ejbStore");
    }

    public void ejbPostCreate(String salesRepId, String name) {
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

    private void insertSalesRep(String salesRepId, String name)
        throws SQLException {
        makeConnection();
        System.out.println("in insertSalesRep");

        String insertStatement = "insert into salesrep values ( ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, salesRepId);
        prepStmt.setString(2, name);

        prepStmt.executeUpdate();
        prepStmt.close();
        System.out.println("leaving insertSalesRep");
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select salesrepid " + "from salesrep where salesrepid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private void deleteSalesRep(String salesRepId) throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from salesrep  " + "where salesrepid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, salesRepId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void loadSalesRep() throws SQLException {
        makeConnection();

        String selectStatement =
            "select name " + "from salesRep where salesrepid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, salesRepId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            name = rs.getString(1);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for salesRepId " + salesRepId +
                " not found in database.");
        }

        releaseConnection();
    }

    private void storeSalesRep() throws SQLException {
        makeConnection();

        String updateStatement =
            "update salesrep set name =  ? " + "where salesrepid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, name);
        prepStmt.setString(2, salesRepId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for salesRepId " + salesRepId +
                " failed.");
        }

        releaseConnection();
    }    
}
 // SalesRepBean 
