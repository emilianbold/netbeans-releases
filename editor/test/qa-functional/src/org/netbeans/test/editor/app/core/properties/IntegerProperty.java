/*
 * StringProperty.java
 *
 * Created on December 10, 2002, 5:50 PM
 */

package org.netbeans.test.editor.app.core.properties;

/**
 *
 * @author  eh103527
 */
public class IntegerProperty implements Property {
    
    private int value = 0;
    
    /** Creates a new instance of StringProperty */
    public IntegerProperty(int value) {
        this.value=value;
    }
    
    public String getProperty() {
        return Integer.toString(value);
    }
    
    public void setProperty(String value) {
        try {
            this.value=Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
    
    public int getValue() {
        return value;
    }
}
