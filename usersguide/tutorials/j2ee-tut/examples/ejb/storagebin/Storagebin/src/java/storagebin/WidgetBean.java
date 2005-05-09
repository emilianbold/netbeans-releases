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


public class WidgetBean implements EntityBean, WidgetRemoteBusiness {
    private String widgetId;
    private String description;
    private double price;
    private EntityContext context;
    private Connection con;
    private String dbName = "jdbc/WidgetDB";

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String ejbCreate(String widgetId, String description, double price)
        throws CreateException {
        try {
            insertRow(widgetId, description, price);
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.widgetId = widgetId;
        this.description = description;
        this.price = price;

        return widgetId;
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
            deleteRow(widgetId);
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
        widgetId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        widgetId = null;
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

    public void ejbPostCreate(String widgetId, String description, double price) {
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

    private void insertRow(String widgetId, String description, double price)
        throws SQLException {
        makeConnection();

        String insertStatement = "insert into widget values ( ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, widgetId);
        prepStmt.setString(2, description);
        prepStmt.setDouble(3, price);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteRow(String widgetId) throws SQLException {
        makeConnection();

        String deleteStatement = "delete from widget where widgetid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, widgetId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select widgetid " + "from widget where widgetid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private void loadRow() throws SQLException {
        makeConnection();

        String selectStatement =
            "select description, price " + "from widget where widgetid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, this.widgetId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            this.description = rs.getString(1);
            this.price = rs.getDouble(2);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for widgetId " + widgetId +
                " not found in database.");
        }

        releaseConnection();
    }

    private void storeRow() throws SQLException {
        makeConnection();

        String updateStatement =
            "update widget set description =  ? , " + "price = ? " +
            "where widgetid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, description);
        prepStmt.setDouble(2, price);
        prepStmt.setString(3, widgetId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for widgetId " + widgetId +
                " failed.");
        }

        releaseConnection();
    }
}
 // WidgetBean 
