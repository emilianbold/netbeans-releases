/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.awt.Component;
import java.awt.Image;
import java.beans.*;

import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;

/** BeanInfo for {@link DataLoader}. */
public class DataLoaderBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor representationClass = new PropertyDescriptor ("representationClass", DataLoader.class, "getRepresentationClass", null); // NOI18N
            representationClass.setDisplayName (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("PROP_representationClass"));
            representationClass.setShortDescription (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("HINT_representationClass"));
            representationClass.setExpert (true);
            PropertyDescriptor actions = new PropertyDescriptor ("actions", DataLoader.class); // NOI18N
            actions.setDisplayName (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("PROP_actions"));
            actions.setShortDescription (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("HINT_actions"));
            actions.setPropertyEditorClass (ActionsEditor.class);
            return new PropertyDescriptor[] { actions, representationClass };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon;
    private static Image icon32;
    public Image getIcon (int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) icon = loadImage ("/org/netbeans/core/resources/objectTypes.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null) icon32 = loadImage ("/org/netbeans/core/resources/objectTypes32.gif"); // NOI18N
            return icon32;
        }
    }

    public static class ActionsEditor extends PropertyEditorSupport {

        public boolean supportsCustomEditor () {
            return true;
        }

        public Component getCustomEditor () {
            return new LoaderActionsPanel (this);
        }

    }

}

/*
 * Log
 *  4    Gandalf   1.3         1/13/00  Jaroslav Tulach I18N
 *  3    Gandalf   1.2         1/13/00  Jesse Glick     DataLoader.actions now 
 *       editable.
 *  2    Gandalf   1.1         11/25/99 Jesse Glick     representationClass 
 *       expert loader property.
 *  1    Gandalf   1.0         11/3/99  Jesse Glick     
 * $
 */
