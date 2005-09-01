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
package org.netbeans.modules.j2ee.sun.share.config;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/**
 * Custom Editor for version of server specific Deployment Descriptor
 *
 * @author Peter Williams
 */
public class VersionEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {

    /** Used for min/max version constructor.  Min version is whatever is required
     *  by the J2EE module in question (e.g. Servlet 2.4 requires 8.0 and newer).
     *  Max version is whatever the connected or installed server is (e.g. if 8.1
     *  is the installed server, 9.0 should not be a valid choice and if the file
     *  on disk that is loaded is 9.0, it should be downgraded w/ warning.)
     */
    public static final int APP_SERVER_7_0 = 0;
    public static final int APP_SERVER_7_1 = 1;
    public static final int APP_SERVER_8_0 = 2;
    public static final int APP_SERVER_8_1 = 3;
    public static final int APP_SERVER_9_0 = 4;
    
    public static final String[] availableChoices = {
        "SunONE Application Server 7.0", // NOI18N
        "SunONE Application Server 7.1", // NOI18N
        "Sun Java System Application Server 8.0", // NOI18N
        "Sun Java System Application Server 8.1", // NOI18N
        "Sun Java System Application Server 9.0", // NOI18N
    };
    
    private String curr_Sel;
    private String [] choices;
    
    public VersionEditor(final int minVersion, final int maxVersion) {
        assert minVersion >= APP_SERVER_7_0 && minVersion <= APP_SERVER_9_0;
        assert maxVersion >= APP_SERVER_7_0 && maxVersion <= APP_SERVER_9_0;
        assert minVersion <= maxVersion;
        
        int numChoices = maxVersion - minVersion + 1;
        curr_Sel = availableChoices[APP_SERVER_8_1];
        choices = new String[numChoices];
        for(int i = 0; i < numChoices; i++) {
            choices[i] = availableChoices[minVersion + i];
        }
    }
    
    public String getAsText() {
        return curr_Sel;
    }
    
    public void setAsText(String string) throws IllegalArgumentException {
        if((string == null) || (string.equals(""))) { // NOI18N
            throw new IllegalArgumentException("text field cannot be empty"); // NOI18N
        } else {
            curr_Sel = string;
        }
        this.firePropertyChange();
    }
    
    public void setValue(Object val) {
        if (! (val instanceof String)) {
            throw new IllegalArgumentException("value must be String"); // NOI18N
        }
        
        curr_Sel = (String) val;
        super.setValue(curr_Sel);
    }
    
    public Object getValue() {
        return curr_Sel;
    }
    
    public String getJavaInitializationString() {
        return getAsText();
    }
    
    public String[] getTags() {
        return choices;
    }
    
    /** -----------------------------------------------------------------------
     * Implementation of ExPropertyEditor interface
     */
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    /** -----------------------------------------------------------------------
     * Implementation of InplaceEditor.Factory interface
     */
    public InplaceEditor getInplaceEditor() {
        return null;
    }
}
