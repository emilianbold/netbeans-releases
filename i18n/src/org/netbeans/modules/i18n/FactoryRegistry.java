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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.util.Lookup;


/**
 * Registry which gets i18n support factories for specified data objects.
 * It gets the factories which are registered in SFS/Services/i18n directory
 * via lookup.
 *
 * @author  Peter Zavadsky
 * @see I18nSupport.Factory
 * @see org.netbeans.modules.i18n.form.FormI18nSupport.Factory
 * @see org.netbeans.modules.i18n.jsp.JspI18nSupport.Factory
 */
public abstract class FactoryRegistry extends Object {

    
    /** All i18n supports kept as <code>Lookup.Result</code>. */
    private static Lookup.Result result;
    
    
    /** Gets lookup result holding script type instances. */
    private static Lookup.Result getSupports() {
        if(result == null) {
            result = Lookup.getDefault().lookup(new Lookup.Template(I18nSupport.Factory.class));
        }
        
        return result;
    }
    
    /** Gets <code>I18nSupportFactory</code> for specified data object class.
     * @return factory for specified data object class or <code>null</code> */
    public static I18nSupport.Factory getFactory(Class dataObjectClass) {
        List candidates = new ArrayList(3);
        
        for(Iterator it = getSupports().allInstances().iterator(); it.hasNext(); ) {
            I18nSupport.Factory factory = (I18nSupport.Factory)it.next();

            if(factory.getDataObjectClass().isAssignableFrom(dataObjectClass)) {
                candidates.add(factory);
            }
        }
        
        if(candidates.size() == 0) {
            return null;
        } else if(candidates.size() == 1) {
            return (I18nSupport.Factory)candidates.get(0);
        } else {
            I18nSupport.Factory chosen = null;
            
            // Find factory which supported class data object 
            // is the lowest one in the class hierarchy.
            for(Iterator it = candidates.iterator(); it.hasNext(); ) {
                I18nSupport.Factory fct = (I18nSupport.Factory)it.next();
                
                if(chosen == null) {
                    chosen = fct;
                    continue;
                }
                
                if(chosen.getDataObjectClass().isAssignableFrom(fct.getDataObjectClass()) ) {
                    chosen = fct;
                }
            }
            
            return chosen;
        }
    }

    /** Indicates if there is a factory for that data object class. */
    public static boolean hasFactory(Class dataObjectClass) {
        for(Iterator it = getSupports().allInstances().iterator(); it.hasNext(); ) {
            I18nSupport.Factory factory = (I18nSupport.Factory)it.next();
            
            if(factory.getDataObjectClass().isAssignableFrom(dataObjectClass)) {
                return true;
            }
        }
        
        return false;
    }
    
}
