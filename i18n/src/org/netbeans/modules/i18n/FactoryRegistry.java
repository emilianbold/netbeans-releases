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


package org.netbeans.modules.i18n;


import java.util.HashMap;


/**
 * Registry which maps i18n support factories to data object names.
 * E.g. for <code>FormDataObject</code> i18n-form module registers <code>FormI18nSupportFactory</code>.
 *
 * @author  Peter Zavadsky
 * @see I18nSupport.Factory
 * @see org.netbeans.modules.i18n.form.FormI18nSupport.Factory
 * @see org.netbeans.modules.i18n.jsp.JspI18nSupport.Factory
 */
public abstract class FactoryRegistry extends Object {

    /** Maps data object class names to i18n support factory objects. */
    private static HashMap registry = new HashMap(3);

    
    /** Registers factory to data object class.
     * @param dataObjectClassName class name of data object for which factory to register
     * @param factpry i18n factory for specified data object */
    public static synchronized void registerSupport(String dataObjectClassName, I18nSupport.Factory factory) {
        registry.put(dataObjectClassName, factory);
    }
    
    /** Unregisters factory for the spcified data object. 
     * @param dataObjectClassName class name of data object which factory to unregister */
    public static synchronized void unregisterSupport(String dataObjectClassName) {
        registry.remove(dataObjectClassName);
    }

    /** Gets <code>I18nSupportFactory</code> for specified data object class.
     * @return registered factory for specified data object class name or null if no was registered */
    public static I18nSupport.Factory getFactory(String dataObjectClassName) {
        return (I18nSupport.Factory)registry.get(dataObjectClassName);
    }

    /** Indicates if there is registered factory for that data object class name. */
    public static boolean hasFactory(String dataObjectClassName) {
        return registry.containsKey(dataObjectClassName);
    }
}
