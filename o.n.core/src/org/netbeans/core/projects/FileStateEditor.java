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

package org.netbeans.core.projects;

import org.netbeans.beaninfo.editors.ListImageEditor;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.PropertyEnv;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.core.projects.SettingChildren.FileStateProperty;

/**
 *
 * @author  Vitezslav Stejskal
 */
class FileStateEditor extends ListImageEditor {

    private String action_define = null;
    private String action_revert = null;
    private String action_delete = null;
    
    private Node.Property prop = null;

    /** Creates new FileStatePropertyEditor */
    public FileStateEditor () {
        super ();
        
        action_define = NbBundle.getMessage (FileStateEditor.class, "LBL_action_define");
        action_revert = NbBundle.getMessage (FileStateEditor.class, "LBL_action_revert");
        action_delete = NbBundle.getMessage (FileStateEditor.class, "LBL_action_delete");
    }

    public void attachEnv (PropertyEnv env) {
        super.attachEnv (env);
        
        try {
            prop = (Node.Property)env.getFeatureDescriptor ();
        } catch (ClassCastException cce) {
            ClassCastException cce2 = new ClassCastException("Expected a Node.Property but got a " + env.getFeatureDescriptor() + " descriptor " + env.getFeatureDescriptor().getClass().getName());
            throw cce2;
        }
    }
    
    public String getAsText () {
        return null;
    }

    public void setAsText (String str) throws java.lang.IllegalArgumentException {        
        try {
            Integer value = null;
            if (action_define.equals (str)) {
                value  = new Integer (SettingChildren.FileStateProperty.ACTION_DEFINE);
            }
            if (action_revert.equals (str)) {
                value = new Integer (FileStateProperty.ACTION_REVERT);
            }
            if (action_delete.equals (str)) {
                value = new Integer (FileStateProperty.ACTION_DELETE);                
            }
            if (value != null) {
                prop.setValue (value);
                super.setValue(value);                
            }
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }
    
    public String[] getTags () {
        Integer val = (Integer) getValue ();

        if (SettingChildren.PROP_LAYER_MODULES.equals (prop.getName ())) {
            return new String [] {
                action_revert
            };
        }
        if (val != null &&
            val.intValue () == FileStateManager.FSTATE_IGNORED) {
            return new String [] {
                action_define,
                action_revert,
                action_delete
            };
        }
        return new String [] {
            action_define
        };
    }
}
