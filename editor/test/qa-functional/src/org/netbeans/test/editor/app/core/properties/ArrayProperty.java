/*
 * ArrayProperty.java
 *
 * Created on November 25, 2002, 11:40 AM
 */

package org.netbeans.test.editor.app.core.properties;

/**
 *
 * @author  eh103527
 */
public class ArrayProperty implements Property {
    
    private Object value;
    
    private Object[] values;
    
    /** Creates a new instance of ArrayProperty */
    public ArrayProperty(Object val,Object[] vals) {
        value=val;
        values=vals;
    }
    
    public String getProperty() {
        return value.toString();
    }
    
    public void setProperty(String value) {
        boolean found=false;
        for (int i=0;i < values.length;i++) {
            if (values[i].toString().compareTo(value) == 0) {
                this.value=values[i];
                found=true;
                break;
            }
        }
    }
    
    public Object[] getValues() {
        return values;
    }
    
    public Object getValue() {
        return value;
    }
    
}
