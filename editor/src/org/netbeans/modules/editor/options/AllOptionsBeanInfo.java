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

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for common options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AllOptionsBeanInfo extends BaseOptionsBeanInfo {

    public static final String EDITOR_STATE_PROP = "editorState"; // NOI18N

    public static final String[] PROP_NAMES = new String[] {
                BaseOptions.KEY_BINDING_LIST_PROP,
                EDITOR_STATE_PROP
    };

    public AllOptionsBeanInfo() {
        super("/org/netbeans/modules/editor/resources/allOptions", "base_"); // NOI18N
    }
    
    protected Class getBeanClass() {
        return AllOptions.class;
    }

    protected String[] getPropNames() {
        return PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();

        setHidden(new String[] {
            EDITOR_STATE_PROP
        });
    }

}
