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
public class BooleanProperty implements Property {
    
    private static String[] VALUES = {Boolean.toString(true),Boolean.toString(false)};
    private boolean value = false;
    
    /** Creates a new instance of StringProperty */
    public BooleanProperty(boolean value) {
        this.value=value;
    }
    
    public String getProperty() {
        if (value) {
            return VALUES[0];
        } else {
            return VALUES[1];
        }
    }
    
    public void setProperty(String value) {
        if (Boolean.toString(this.value).compareTo(value) != 0) {
            this.value=!this.value;
        }
    }
    
    public String[] getValues() {
        return VALUES;
    }
    
    public boolean getValue() {
        return value;
    }
    
}
