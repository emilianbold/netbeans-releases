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

package org.netbeans.modules.editor.options;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for common options
 *
 *  @author Martin Roskanin
 *  @since 08/2001
 */
public class AllOptionsFolderBeanInfo extends BaseOptionsBeanInfo {
    
    public static final String EDITOR_STATE_PROP = "editorState"; // NOI18N
    
    public static final String[] PROP_NAMES = new String[] {
        BaseOptions.KEY_BINDING_LIST_PROP,
        EDITOR_STATE_PROP,
        BaseOptions.OPTIONS_VERSION_PROP
    };
    
    public AllOptionsFolderBeanInfo() {
        super("/org/netbeans/modules/editor/resources/allOptions", "base_"); // NOI18N
    }
    
    protected Class getBeanClass() {
        return AllOptionsFolder.class;
    }
    
    protected String[] getPropNames() {
        return PROP_NAMES;
    }
    
    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();
        
        setHidden(new String[] {
            EDITOR_STATE_PROP,
            BaseOptions.OPTIONS_VERSION_PROP
        });
    }
    
}
