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

/**
 *
 * @author  Vitezslav Stejskal
 */
public class FileStateEditor extends ListImageEditor {

    private String action_unchanged = null;
    private String action_define = null;
    private String action_delete = null;
    
    private Node.Property prop = null;

    /** Creates new FileStatePropertyEditor */
    public FileStateEditor () {
        super ();
        
        action_unchanged = NbBundle.getMessage (FileStateEditor.class, "LBL_action_unchanged");
        action_define = NbBundle.getMessage (FileStateEditor.class, "LBL_action_define");
        action_delete = NbBundle.getMessage (FileStateEditor.class, "LBL_action_delete");
    }

    public void attachEnv (PropertyEnv env) {
        super.attachEnv (env);
        
        prop = (Node.Property)env.getFeatureDescriptor ();
    }
    
    public String getAsText () {
        return action_unchanged;
    }

    public void setAsText (String str) throws java.lang.IllegalArgumentException {
        if (action_unchanged.equals (str))
            return;
        
        try {
            if (action_define.equals (str)) {
                prop.setValue (new Integer (FileStateManager.FSTATE_DEFINED));
            }
            if (action_delete.equals (str)) {
                prop.setValue (new Integer (FileStateManager.FSTATE_UNDEFINED));
            }
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }
    
    public String[] getTags () {
        Integer val = (Integer) getValue ();

        if (val != null &&
            val.intValue () == FileStateManager.FSTATE_IGNORED &&
            !SettingChildren.PROP_LAYER_MODULES.equals (prop.getName ()))
            return new String [] {
                action_unchanged,
                action_define,
                action_delete
            };
        else
            return new String [] {
                action_unchanged,
                action_define
            };
    }
}
