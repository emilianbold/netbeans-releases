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

package bank;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.*;

/**
 * This is the bean class for the SavingsAccountBean enterprise bean.
 * Created Mar 23, 2005 12:58:37 PM
 * @author blaha
 */
public class SavingsAccountBean implements EntityBean, SavingsAccountRemoteBusiness {
    private EntityContext context;
    private Connection con;
    private String id;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise beans, Web services)
    // TODO Add business methods
    // TODO Add create methods
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        id = (String)context.getPrimaryKey();
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        id = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        try{
            deleteRow(id);
        }catch(SQLException ex){
            throw new EJBException("ejbRemove: " + ex.getMessage());
        }
    }
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        // TODO add code to retrieve data
        try{
        loadRow();
        }catch(SQLException ex){
            throw new EJBException("ejbLoad() " + ex.getMessage());
        }
            
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        // TODO add code to persist data
        try{
        storeRow();
        }catch(SQLException ex){
            throw new EJBException("ejbStore " + ex.getMessage());
        }
    }
    
    // </editor-fold>
    
    /**
     * See EJB 2.0 and EJB 2.1 section 12.2.5
     */
    public java.lang.String ejbFindByPrimaryKey(java.lang.String aKey) throws FinderException {
        // TODO add code to locate aKey from persistent storage
        // throw javax.ejb.ObjectNotFoundException if aKey is not in
        // persistent storage.
        boolean result;
        try{
            result = selectByPrimaryKey(aKey);
        }catch(SQLException ex){
            throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
        }
        if(result){
            return aKey;
        }else{
            throw new ObjectNotFoundException("Account for id " + aKey + " not found.");
        }
    }
    
    public java.lang.String ejbCreate(java.lang.String id, java.lang.String firstName, java.lang.String lastName, java.math.BigDecimal balance) throws CreateException {
        //TODO implement ejbCreate
        if(balance.signum() == -1){
            throw new CreateException("A negative initial balance is not allowed.");
        }
        try{
            insertRow(id, firstName, lastName, balance);
        }catch(SQLException ex){
            throw new EJBException("ejbCreate: " + ex.getMessage());
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
        return id;
    }
    
    public void ejbPostCreate(java.lang.String id, java.lang.String firstName, java.lang.String lastName, java.math.BigDecimal balance) throws CreateException {
        //TODO implement ejbPostCreate
    }
    
    private javax.sql.DataSource getSavingsAccountDB() throws javax.naming.NamingException {
        javax.naming.Context c = new javax.naming.InitialContext();
        return (javax.sql.DataSource) c.lookup("java:comp/env/jdbc/pointbase");
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public void credit(BigDecimal credit){
        balance = balance.add(credit);
    }
    
    // business method
    public void debit(BigDecimal amount) throws InsufficientBalanceException {
        if (balance.compareTo(amount) == -1) {
            throw new InsufficientBalanceException();
        }
                                                                                            
        balance = balance.subtract(amount);
    }
    
    private void makeConnection(){
        try{
            con = getSavingsAccountDB().getConnection();
        }catch(Exception ex){
            throw new EJBException("Unable to connect to database. " +
                    ex.getMessage());
        }
    }
    
    private void releaseConnection(){
        try{
            con.close();
        }catch(SQLException ex){
            throw new EJBException("Unable to connect to database. " +
                    ex.getMessage());
        }
    }
	
	// finder methods
	
    public java.util.Collection ejbFindInRange(BigDecimal low, BigDecimal high) throws FinderException {
        //TODO implement ejbFindByInRange
        Collection result;
         try{
          result = selectInRange(low, high);
         }catch(SQLException ex){
             throw new EJBException("ejbFindByInRange" + ex.getMessage());
         }
        return result;
    }

    public java.util.Collection ejbFindByLastName(java.lang.String lastName) throws FinderException {
        //TODO implement ejbFindByLastName
         Collection result;
         try{
          result = selectByLastName(lastName);
         }catch(SQLException ex){
             throw new EJBException("ejbFindByLastName" + ex.getMessage());
         }
        return result;
    }
    
    // home method
    public void ejbHomeChargeForLowBalance(BigDecimal minimumBalance,
        BigDecimal charge) throws InsufficientBalanceException {
        SavingsAccountRemoteHome home = (SavingsAccountRemoteHome)context.getEJBHome();
        try{
        Collection c =
                home.findInRange(new BigDecimal(0.00), 
                minimumBalance.subtract(new BigDecimal(0.01)));
        Iterator it = c.iterator();
        while(it.hasNext()){
            SavingsAccountRemote account = (SavingsAccountRemote)it.next();
            if(account.getBalance().compareTo(charge) == 1){
                account.debit(charge);
            }
        }
        }catch(Exception ex){
            throw new EJBException("ejbHomeChargeForLowBalance" + ex.getMessage());
        }
    }

    // database methods
    
    private void insertRow(String id, String firstName,
            String lastName, BigDecimal balance) throws SQLException{
        makeConnection();
        String insertStatement = "insert into savingsaccount values ( ? , ? , ? , ? )";
        PreparedStatement prepStmt = con.prepareStatement(insertStatement);
        
        prepStmt.setString(1, id);
        prepStmt.setString(2, firstName);
        prepStmt.setString(3, lastName);
        prepStmt.setBigDecimal(4, balance);
        
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }
    
    private void deleteRow(String id) throws SQLException {
        makeConnection();
        String deleteStatement = "delete from savingsaccount where id = ?";
        PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
        prepStmt.setString(1, id);
        prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
    }
    
    private boolean selectByPrimaryKey(String id) throws SQLException{
        makeConnection();
        String selectStatement = "select id from savingsaccount where id = ?";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);
        prepStmt.setString(1, id);
        
        ResultSet rs = prepStmt.executeQuery();
        boolean result = rs.next();
        
        releaseConnection();
        return result;
    }
    
    private void loadRow() throws SQLException{
        makeConnection();
        String selectString =
                "select firstname, lastname, balance from savingsaccount where id = ?";
        PreparedStatement prepStmt = con.prepareStatement(selectString);
        prepStmt.setString(1, this.id);
        ResultSet rs = prepStmt.executeQuery();
        
        if(rs.next()){
            this.firstName = rs.getString(1);
            this.lastName = rs.getString(2);
            this.balance = rs.getBigDecimal(3);
            prepStmt.close();
        }else {
            prepStmt.close();
            throw new NoSuchEntityException("Row for id " + id +
                    " not found in database");
        }
        
        releaseConnection();
    }
    
    private void storeRow() throws SQLException {
        makeConnection();
        String updateString =
                "update savingsaccount set firstname = ? , lastname = ? , " +
                "balance = ? where id = ?";
        PreparedStatement prepStmt = con.prepareStatement(updateString);
        prepStmt.setString(1, firstName);
        prepStmt.setString(2, lastName);
        prepStmt.setBigDecimal(3, balance);
        prepStmt.setString(4, id);
        
        int rows = prepStmt.executeUpdate();
        prepStmt.close();
        releaseConnection();
        if(rows == 0){
            throw new EJBException("Storing row id " + id + " failed.");
        }
    }
    
    private Collection selectByLastName(String lastName) throws SQLException{
        makeConnection();
        String selectStatement = "select id from savingsaccount where lastname = ?";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);
        prepStmt.setString(1, lastName);
        ResultSet rs = prepStmt.executeQuery();
        ArrayList accounts = new ArrayList();
        
         while(rs.next()){
            accounts.add(rs.getString(1));
         }
        prepStmt.close();
        releaseConnection();
        return accounts;
    }
    
    private Collection selectInRange(BigDecimal low, BigDecimal high) throws SQLException {
        makeConnection();
        String selectStatement = "select id from savingsaccount where balance between ? and ?";
        PreparedStatement prepStmt = con.prepareStatement(selectStatement);
        prepStmt.setBigDecimal(1, low);
        prepStmt.setBigDecimal(2, high);
        
        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();
        
        while(rs.next()){
            a.add(rs.getString(1));
        }
        prepStmt.close();
        releaseConnection();
        return a;
    }    
}
