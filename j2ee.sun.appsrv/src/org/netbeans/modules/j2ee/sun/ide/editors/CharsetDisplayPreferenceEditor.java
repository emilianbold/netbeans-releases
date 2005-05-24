/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CharsetDisplayPreferenceEditor.java
 *
 * Created on March 19, 2004, 1:17 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;
import org.openide.util.NbBundle;

/**
 *
 * @author  vkraemer
 */
public class CharsetDisplayPreferenceEditor extends LogLevelEditor{
    
    public static Integer DEFAULT_PREF_VAL = Integer.valueOf("1"); // NOI18N
    
    private Integer val = DEFAULT_PREF_VAL;

    
    /** Creates a new instance of CharsetDisplayPreferenceEditor */
    public CharsetDisplayPreferenceEditor() {
    }
    
    static String[] choices = {
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_CANONICAL"), // NOI18N
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_ALIAS_ASIDE"), // NOI18N
        NbBundle.getMessage(CharsetDisplayPreferenceEditor.class,"VAL_ALIAS"),    // NOI18N
    };
    
    public String[] getTags() {
        return choices;
    }
        
    public String getAsText() {
        return choices[val.intValue()];
    }
    
    public void setAsText(String string) throws IllegalArgumentException {
        int intVal = 1; 
        if((string==null)||(string.equals(""))) // NOI18N
            throw new IllegalArgumentException();
        else
            intVal = java.util.Arrays.binarySearch(choices,string); 
        if (intVal < 0) 
            intVal = 1;
        if (intVal > 2)
            intVal = 1;
        String valS = String.valueOf(intVal);
        val = Integer.valueOf(valS);
        this.firePropertyChange();
    }
    
    public void setValue(Object val) {
        if (val==null)
            val=DEFAULT_PREF_VAL;
        if (! (val instanceof Integer)) {
            throw new IllegalArgumentException();
        }
        
        this.val = (Integer) val;
        int ival = this.val.intValue();
        if (ival < 0 || ival > 2)
            this.val = DEFAULT_PREF_VAL;
//        super.setValue(this.val);
    }
    
    public Object getValue() {
        return this.val;
    }
}
