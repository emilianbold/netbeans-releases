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
 * @see I18nSupport
 * @see I18nSupport.Factory
 * @see org.netbeans.modules.i18n.form.FormI18nSupport
 * @see org.netbeans.modules.i18n.form.FormI18nSupport.Factory
 */
public abstract class FactoryRegistry extends Object {

    /** Default i18n support factory. Now <code>JavaI18nSupportFactory<code>. */
    private static final I18nSupport.Factory defaultFactory = new JavaI18nSupport.Factory();

    /** Maps data object class names to i18n support factory objects. */
    private static HashMap map = new HashMap(2);

    /** Registers factory to data object class. */
    public static void registerSupport(String dataObjectClassName, I18nSupport.Factory factory) {
        map.put(dataObjectClassName, factory);
    }

    /** Gets <code>I18nSupportFactory</code> for specified data object class.
     * @return registered factory for specified data object class name or default factory
     * if no was registered */
    public static I18nSupport.Factory getFactory(String dataObjectClassName) {
        I18nSupport.Factory factory = (I18nSupport.Factory)map.get(dataObjectClassName);

        if(factory == null)
            factory = defaultFactory;

        return factory;
    }

    
}
