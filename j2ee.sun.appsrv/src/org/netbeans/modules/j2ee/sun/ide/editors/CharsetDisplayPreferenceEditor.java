/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
