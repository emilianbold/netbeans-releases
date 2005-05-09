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

package storagebin;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;


public class StorageBinBean implements EntityBean, StorageBinRemoteBusiness {
    private static final String dbName = "jdbc/pointbase";
    private String storageBinId;
    private String widgetId;
    private int quantity;
    private EntityContext context;
    private Connection con;

    public String getWidgetId() {
        return widgetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String ejbCreate(String storageBinId, String widgetId, int quantity)
        throws CreateException {
        try {
            insertRow(storageBinId, widgetId, quantity);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.storageBinId = storageBinId;
        this.widgetId = widgetId;
        this.quantity = quantity;

        return storageBinId;
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

    public String ejbFindByWidgetId(String widgetId) throws FinderException {
        String storageBinId;

        try {
            storageBinId = selectByWidgetId(widgetId);
        } catch (Exception ex) {
            throw new EJBException("ejbFindByWidgetId: " + ex.getMessage());
        }

        return storageBinId;
    }

    public void ejbRemove() {
        try {
            deleteRow(storageBinId);
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
        storageBinId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        storageBinId = null;
    }

    public void ejbLoad() {
        try {
            loadRow();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }
    }

    public void ejbStore() {
        try {
            storeRow();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }
    }

    public void ejbPostCreate(String storageBinId, String widgetId, int quantity) {
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

    private void insertRow(String storageBinId, String widgetId, int quantity)
        throws SQLException {
        makeConnection();

        String insertStatement = "insert into storagebin values ( ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, storageBinId);
        prepStmt.setString(2, widgetId);
        prepStmt.setInt(3, quantity);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteRow(String storageBinId) throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from storagebin where storagebinid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, storageBinId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select storagebinid " + "from storagebin where storagebinid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private String selectByWidgetId(String widgetId) throws SQLException {
        makeConnection();

        String storageBinId;

        String selectStatement =
            "select storagebinid " + "from storagebin where widgetid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, widgetId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            storageBinId = rs.getString(1);
        } else {
            storageBinId = null;
        }

        prepStmt.close();
        releaseConnection();

        return storageBinId;
    }

    private void loadRow() throws SQLException {
        makeConnection();

        String selectStatement =
            "select widgetId, quantity " +
            "from storagebin where storagebinid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, this.storageBinId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            this.widgetId = rs.getString(1);
            this.quantity = rs.getInt(2);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for storageBinId " +
                storageBinId + " not found in database.");
        }

        releaseConnection();
    }

    private void storeRow() throws SQLException {
        makeConnection();

        String updateStatement =
            "update storagebin set widgetId =  ? , " + "quantity = ? " +
            "where storagebinid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, widgetId);
        prepStmt.setInt(2, quantity);
        prepStmt.setString(3, storageBinId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for storageBinId " +
                storageBinId + " failed.");
        }

        releaseConnection();
    }
}
 // StorageBinBean 
