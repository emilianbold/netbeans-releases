/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
        
        prop = (Node.Property)env.getFeatureDescriptor ();
    }
    
    public String getAsText () {
        return null;
    }

    public void setAsText (String str) throws java.lang.IllegalArgumentException {
        try {
            if (action_define.equals (str)) {
                prop.setValue (new Integer (FileStateProperty.ACTION_DEFINE));
            }
            if (action_revert.equals (str)) {
                prop.setValue (new Integer (FileStateProperty.ACTION_REVERT));
            }
            if (action_delete.equals (str)) {
                prop.setValue (new Integer (FileStateProperty.ACTION_DELETE));
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
