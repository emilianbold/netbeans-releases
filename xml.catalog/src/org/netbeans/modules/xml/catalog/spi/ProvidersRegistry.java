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
package org.netbeans.modules.xml.catalog.spi;

import java.beans.*;
import java.util.*;
import java.io.IOException;

import javax.swing.event.*;

import org.openide.util.Lookup;

/**
 * A utility class representing the registry of SPI implementations.
 * It contains implementations classes.
 * <p>
 * It should be moved out of SPI package.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class ProvidersRegistry {

    /**
     * Queries Lookup for all registered catalog providers returning provided Classes.
     * @param  filter an array of SPI interfaces that must the catalog Class implement or <tt>null</tt>
     * @return Iterator<Class> of currently registered catalogs.
     */
    public static final synchronized Iterator getProviderClasses(Class[] filter) {

        Lookup.Template template = new Lookup.Template(CatalogProvider.class);
        Lookup.Result res = Lookup.getDefault().lookup(template);
        Iterator it = res.allInstances().iterator();
        HashSet set = new HashSet();

        while(it.hasNext()) {
            try {
                CatalogProvider next = (CatalogProvider) it.next();
                set.add(next.provideClass());
            } catch (ClassNotFoundException ex) {
                //ignore it
            } catch (IOException ex) {
                //ignore it
            }                                                                               
        }
        
        it = set.iterator();
        
        if (filter == null)
            return it;

        ArrayList list = new ArrayList();
                
try_next_provider_class:
        while (it.hasNext()) {
            Class next = (Class) it.next();
            
            // provider test
            
            for (int i=0; i<filter.length; i++) {
                
                if (filter[i].isAssignableFrom(next) == false)
                    break try_next_provider_class;
            }
            
            // test passed
            
            list.add(next);
        }
        
        return list.iterator();
    }
          
}
