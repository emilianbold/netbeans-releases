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
public class MultiLineStringProperty implements Property {
    
    private String value = "";
    
    /** Creates a new instance of StringProperty */
    public MultiLineStringProperty(String value) {
        this.value=value;
    }
    
    public String getProperty() {
        return value;
    }
    
    public void setProperty(String value) {
        this.value=value;
    }
}
