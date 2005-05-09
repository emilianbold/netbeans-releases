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

package order;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;


public class OrderBean implements EntityBean, OrderRemoteBusiness {
    private static final String dbName = "jdbc/OrderDB";
    private String orderId;
    private ArrayList lineItems;
    private String customerId;
    private double totalPrice;
    private String status;
    private Connection con;
    private EntityContext context;

    public ArrayList getLineItems() {
        return lineItems;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public String ejbCreate(String orderId, String customerId, String status,
        double totalPrice, ArrayList lineItems) throws CreateException {
        try {
            insertOrder(orderId, customerId, status, totalPrice);

            for (int i = 0; i < lineItems.size(); i++) {
                LineItem item = (LineItem) lineItems.get(i);

                insertItem(item);
            }
        } catch (Exception ex) {
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }

        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.lineItems = lineItems;

        return orderId;
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

    public Collection ejbFindByProductId(String productId)
        throws FinderException {
        Collection result;

        try {
            result = selectByProductId(productId);
        } catch (Exception ex) {
            throw new EJBException("ejbFindByProductId " + ex.getMessage());
        }

        return result;
    }

    public void ejbRemove() {
        try {
            deleteOrder(orderId);
            deleteItems(orderId);
        } catch (Exception ex) {
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;
        lineItems = new ArrayList();
    }

    public void unsetEntityContext() {
        lineItems = null;
    }

    public void ejbActivate() {
        orderId = (String) context.getPrimaryKey();
    }

    public void ejbPassivate() {
        orderId = null;
    }

    public void ejbLoad() {
        try {
            loadOrder();
            loadItems();
        } catch (Exception ex) {
            throw new EJBException("ejbLoad: " + ex.getMessage());
        }
    }

    public void ejbStore() {
        try {
            storeOrder();

            for (int i = 0; i < lineItems.size(); i++) {
                LineItem item = (LineItem) lineItems.get(i);

                storeItem(item);
            }
        } catch (Exception ex) {
            throw new EJBException("ejbStore: " + ex.getMessage());
        }
    }

    public void ejbPostCreate(String orderId, String customerId, String status,
        double totalPrice, ArrayList lineItems) {
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

    private void insertOrder(String orderId, String customerId, String status,
        double totalPrice) throws SQLException {
        makeConnection();

        String insertStatement = "insert into orders values ( ? , ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setString(1, orderId);
        prepStmt.setString(2, customerId);
        prepStmt.setDouble(3, totalPrice);
        prepStmt.setString(4, status);

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void insertItem(LineItem lineItem) throws SQLException {
        makeConnection();

        String insertStatement =
            "insert into lineitems values ( ? , ? , ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);

        prepStmt.setInt(1, lineItem.getItemNo());
        prepStmt.setString(2, lineItem.getOrderId());
        prepStmt.setString(3, lineItem.getProductId());
        prepStmt.setDouble(4, lineItem.getUnitPrice());
        prepStmt.setInt(5, lineItem.getQuantity());

        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private boolean selectByPrimaryKey(String primaryKey)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select orderid " + "from orders where orderid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, primaryKey);

        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();

        prepStmt.close();
        releaseConnection();

        return result;
    }

    private Collection selectByProductId(String productId)
        throws SQLException {
        makeConnection();

        String selectStatement =
            "select distinct orderid " + "from lineitems where productid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, productId);

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

    private void deleteItems(String orderId) throws SQLException {
        makeConnection();

        String deleteStatement =
            "delete from lineitems  " + "where orderid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, orderId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void deleteOrder(String orderId) throws SQLException {
        makeConnection();

        String deleteStatement = "delete from orders  " + "where orderid = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

        prepStmt.setString(1, orderId);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }

    private void loadOrder() throws SQLException {
        makeConnection();

        String selectStatement =
            "select customerid, totalprice, status " +
            "from orders where orderid = ? ";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, orderId);

        ResultSet rs = prepStmt.executeQuery();

        if (rs.next()) {
            customerId = rs.getString(1);
            totalPrice = rs.getDouble(2);
            status = rs.getString(3);
            prepStmt.close();
        } else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for orderId " + orderId +
                " not found in database.");
        }

        releaseConnection();
    }

    private void loadItems() throws SQLException {
        makeConnection();

        String selectStatement =
            "select itemno, productid, unitprice, quantity " +
            "from  lineitems  where orderid = ? " + "order by itemno";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, orderId);

        ResultSet rs = prepStmt.executeQuery();

        lineItems.clear();

        int count = 0;

        while (rs.next()) {
            int itemNo = rs.getInt(1);
            String productId = rs.getString(2);
            double unitPrice = rs.getDouble(3);
            int quantity = rs.getInt(4);

            lineItems.add(new LineItem(productId, quantity, unitPrice, itemNo,
                    orderId));
            count++;
        }

        prepStmt.close();

        if (count == 0) {
            throw new NoSuchEntityException("No items for orderId " + orderId +
                " found in database.");
        }

        releaseConnection();
    }

    private void storeOrder() throws SQLException {
        makeConnection();

        String updateStatement =
            "update orders set customerid =  ? ," +
            "totalprice = ? , status = ? " + "where orderid = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, customerId);
        prepStmt.setDouble(2, totalPrice);
        prepStmt.setString(3, status);
        prepStmt.setString(4, orderId);

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing row for orderId " + orderId +
                " failed.");
        }

        releaseConnection();
    }

    private void storeItem(LineItem item) throws SQLException {
        makeConnection();

        String updateStatement =
            "update lineitems set productid =  ? ," +
            "unitprice = ? , quantity = ? " +
            "where orderid = ? and itemno = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateStatement);

        prepStmt.setString(1, item.getProductId());
        prepStmt.setDouble(2, item.getUnitPrice());
        prepStmt.setInt(3, item.getQuantity());
        prepStmt.setString(4, orderId);
        prepStmt.setInt(5, item.getItemNo());

        int rowCount = prepStmt.executeUpdate();

        prepStmt.close();

        if (rowCount == 0) {
            throw new EJBException("Storing itemNo " + item.getItemNo() +
                "for orderId " + orderId + " failed.");
        }

        releaseConnection();
    }
}
 // OrderBean 