/*
 * History.java
 *
 * Created on August 11, 2005, 1:48 PM
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
public class History {
    
    private static int transaction;
    
    private String msg;
    
    /** Creates a new instance of History */
    public History() {
        transaction = 0;
    }
    
    public int getTransaction() {
        return transaction;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String val) {
        this.msg = val;
    }
    
    public void incrementTransaction() {
        transaction ++;
         setMsg("   Transaction count is " + transaction);
    }
    
    
}
