/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans;

import java.beans.*;
import java.awt.Component;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;

import org.netbeans.tax.*;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public class Lib {

    /** */
    private static final boolean DEBUG_CUSTOMIZER = true;

    private static String CREATE_ATTRIBUTE_NAME  = Util.getString ("TEXT_new_attribute_name");
    private static String CREATE_ATTRIBUTE_VALUE = Util.getString ("TEXT_new_attribute_value");
    


    /** Returns the customizer component for <CODE>object</CODE>.
     *
     * @param <CODE>object</CODE> bean to get its customizer
     * @return the component or <CODE>null</CODE> if there is no customizer
     */
    public static Component getCustomizer (Object object) {
	if ( DEBUG_CUSTOMIZER ) {
	    Util.debug ("BeanUtil::getCustomizer: object = " + object); // NOI18N
	}
        if (object == null)
            return null;
        
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo (object.getClass());
        } catch (IntrospectionException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("BeanUtil::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        }
        Class clazz = beanInfo.getBeanDescriptor().getCustomizerClass();
        if (clazz == null) {
            return null;
        }

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("BeanUtil::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        } catch (IllegalAccessException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("BeanUtil::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        }

        if (!!! (o instanceof Customizer) ) {
            // no customizer => no fun
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("BeanUtil::getCustomizer: is NOT instanceof Customizer: " + o); // NOI18N
	    }
            return null;
        }

        Customizer cust = ((java.beans.Customizer)o);

        // looking for the component
        Component comp = null;
        if (o instanceof Component) {
            comp = (Component)o;
        } else {
            // no component provided
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("BeanUtil::getCustomizer: is NOT instanceof Component: " + o); // NOI18N
	    }
            return null;
        }

        cust.setObject (object);

        return comp;
    }

    /**
     */
    public static Component getCustomizer (Class classClass, Object property, String propertyName) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo (classClass);
        } catch (IntrospectionException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        }
	if ( DEBUG_CUSTOMIZER ) {
	    Util.debug ("Util::getCustomizer: beaninfo = " + beanInfo); // NOI18N
	}
	PropertyDescriptor[] propDescrs = beanInfo.getPropertyDescriptors();
	PropertyDescriptor propertyDescriptor = null;
	for ( int i = 0; i < propDescrs.length; i++ ) {
	    if ( propertyName.equals (propDescrs[i].getName()) ) {
		propertyDescriptor = propDescrs[i];
		break;
	    }
	}
	if ( propertyDescriptor == null ) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: have NOT property: " + propertyName); // NOI18N
	    }
	    return null;
	}
	if ( DEBUG_CUSTOMIZER ) {
	    Util.debug ("Util::getCustomizer: propertyDescriptor: " + propertyDescriptor); // NOI18N
	}
        Class clazz = propertyDescriptor.getPropertyEditorClass();
	if ( DEBUG_CUSTOMIZER ) {
	    Util.debug ("Util::getCustomizer: propertyEditorClass: " + clazz); // NOI18N
	}
        if (clazz == null) {
            return null;
        }
        Object peo;
        try {
            peo = clazz.newInstance ();
        } catch (InstantiationException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        } catch (IllegalAccessException e) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: exception = " + e); // NOI18N
	    }
            return null;
        }
	
        if (!!! (peo instanceof PropertyEditor) ) {
            // no customizer => no fun
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: is NOT instanceof PropertyEditor: " + peo); // NOI18N
	    }
            return null;
        }

        PropertyEditor editor = ((PropertyEditor)peo);
	editor.setValue (property);
	Component comp = editor.getCustomEditor();
	if ( comp == null ) {
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: have NOT customizer: " + editor); // NOI18N
	    }
	    return null;
	}
	if (!!! (comp instanceof Customizer) ) {
	    // no customizer => no fun
	    if ( DEBUG_CUSTOMIZER ) {
		Util.debug ("Util::getCustomizer: is NOT instanceof Customizer: " + comp); // NOI18N
	    }
            return null;
        }
        Customizer cust = ((Customizer)comp);

//          cust.setObject (property); // done by editor.setValue (property);

        return comp;
    }

    /**
     */
    public static boolean confirmAction (String message) {
	NotifyDescriptor nd = new NotifyDescriptor.Confirmation (message, NotifyDescriptor.YES_NO_OPTION);

	Object option = TopManager.getDefault().notify (nd);

	return ( option == NotifyDescriptor.YES_OPTION );
    }



    /**
     */
    public static TreeAttribute createAttributeDialog () {
        try {
            TreeAttribute attr = new TreeAttribute (CREATE_ATTRIBUTE_NAME, CREATE_ATTRIBUTE_VALUE);
            Component customizer = getCustomizer (attr);

            if ( customizer == null ) {
                return null;
            }

	    return (TreeAttribute)customNode (attr, customizer, Util.getString ("TITLE_new_attribute"));
	} catch (TreeException exc) {
	    Util.notifyTreeException (exc);
            return null;
        }
    }

    /**
     */
    private static TreeNode customNode (TreeNode treeNode, Component panel, String title) {
	DialogDescriptor dd = new DialogDescriptor
	    (panel, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
	     DialogDescriptor.BOTTOM_ALIGN, null, null);

	TopManager.getDefault ().createDialog (dd).show();

	if (dd.getValue() != DialogDescriptor.OK_OPTION) {
	    return null;
        }
	return treeNode;
    }

}
