/*
 * MyBean.java
 *
 * Created on June 1, 2006, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mypackage;

/**
 *
 * @author luke
 */
public class MyBean {
    
    /** Creates a new instance of MyBean */
    public MyBean() {
    }
    
    private String name;
    private boolean noErrors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String doSome(){
        return null;
    }

    public boolean isNoErrors(){
        return noErrors;
    }

    public void setIsNoErrors(boolean newValue){
        noErrors = newValue;
    }
}
