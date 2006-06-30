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
package org.netbeans.modules.j2ee.sun.share.config;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/** Custom Editor for version of server specific Deployment Descriptor
 *
 * This class badly needs to be merged/refactored into/with ASDDVersion.  Too
 * much duplication and maintenance.  In particular, a VersionEditor that directly
 * used ASDDVersion objects instead of converting them to/from strings would be 
 * infinitely better, but no time to research how to write such a beast right now.
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
//    public static final int APP_SERVER_7_1 = 1;
    public static final int APP_SERVER_8_0 = 1;
    public static final int APP_SERVER_8_1 = 2;
    public static final int APP_SERVER_9_0 = 3;
    
    private String curr_Sel;
    private String [] choices;
    
    public VersionEditor(final int minVersion, final int maxVersion) {
        assert minVersion >= APP_SERVER_7_0 && minVersion <= APP_SERVER_9_0;
        assert maxVersion >= APP_SERVER_7_0 && maxVersion <= APP_SERVER_9_0;
        assert minVersion <= maxVersion;
        
        int numChoices = maxVersion - minVersion + 1;
        curr_Sel = ASDDVersion.asDDVersions[APP_SERVER_8_1].toString();
        choices = new String[numChoices];
        for(int i = 0; i < numChoices; i++) {
            choices[i] = ASDDVersion.asDDVersions[minVersion + i].toString();
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

    /** Convert ASDDVersion into one of the int types in this editor.
     */
    static int fromASDDVersion(ASDDVersion asDDVersion) {
        int result = APP_SERVER_8_1;
        
        if(ASDDVersion.SUN_APPSERVER_7_0.equals(asDDVersion)) {
            result = APP_SERVER_7_0;
//        } else if(ASDDVersion.SUN_APPSERVER_7_1.equals(asDDVersion)) {
//            result = APP_SERVER_7_1;
        } else if(ASDDVersion.SUN_APPSERVER_8_0.equals(asDDVersion)) {
            result = APP_SERVER_8_0;
        } else if(ASDDVersion.SUN_APPSERVER_8_1.equals(asDDVersion)) {
            result = APP_SERVER_8_1;
        } else if(ASDDVersion.SUN_APPSERVER_9_0.equals(asDDVersion)) {
            result = APP_SERVER_9_0;
        } else {
            throw new IllegalArgumentException("Unrecognized appserver version: " + asDDVersion);
        }
        
        return result;
    }
}
