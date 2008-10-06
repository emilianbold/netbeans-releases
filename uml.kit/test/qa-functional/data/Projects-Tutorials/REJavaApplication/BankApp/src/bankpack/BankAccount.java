/*
 * BankAccount.java
 *
 * Created on August 11, 2005, 1:19 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package bankpack;

/**
 *
 * @author Administrator
 */
abstract class BankAccount implements Account {
    private double balance;
    
    private String accountNumber;
    
    private History mHistory  = new History();   
    
    private double interestRate;      

    
    /** Creates a new instance of BankAccount */
    public BankAccount() {
    }
    public BankAccount(java.lang.String accNumber, double initialAmount) {
        balance = initialAmount;
        accountNumber = accNumber;
    }
    
    public BankAccount(java.lang.String accNumber, double initialAmount,double rate ) {
        balance = initialAmount;
        accountNumber = accNumber;
        interestRate = rate;
    }
    public double getBalance() {
        return balance;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void withdraw(double val) throws bankpack.NoAvailableFundsException{
     
         if (getBalance() >= val){
              setBalance(balance - val);
              mHistory.incrementTransaction();
         }
         else
            noAvailableFunds();
    }
    
    public void deposit(double val) {
        setBalance(balance + val);
    }
    
    
    
    private void setBalance(double val) {
        this.balance = val;
    }
    
    public void setAccountNumber(String val) {
        this.accountNumber = val;
    }
    
    public boolean equals(Object o) {
        return this == o ||
        ( (o instanceof BankAccount) && ((BankAccount)o).getAccountNumber()==getAccountNumber());
    }
    
    public int hashCode() {
        return getAccountNumber().hashCode();
    }
    
    private void noAvailableFunds()throws NoAvailableFundsException{
        throw  new NoAvailableFundsException("Not enough funds for this account  " + getAccountNumber());
     
     }
    
    public String toString()    
       {
           return accountType()+ " Number:  "  +getAccountNumber()+ "\nBalance:  "+getBalance();
    }
    
    public History getHistory() {
        return mHistory;
    }
    
    public void setHistory(History val) {
        this.mHistory = val;
    }
    
    public String getMessage() {
        return mHistory.getMsg();
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    public void setInterestRate(double val) {
        this.interestRate = val;
    }

}
