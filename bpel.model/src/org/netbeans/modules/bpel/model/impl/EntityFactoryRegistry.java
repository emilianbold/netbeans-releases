/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * @author ads
 */
final class EntityFactoryRegistry {

    private EntityFactoryRegistry() {
        myFactories = new LinkedList<EntityFactory>();
        myAllQNames = new HashSet<QName>(BpelElements.allQNames());
        myExtensionsQNames = new HashSet<QName>();
        
        Result<EntityFactory> result = Lookup.getDefault().
                lookup(new Lookup.Template<EntityFactory>(EntityFactory.class));
        
        for (EntityFactory factory : result.allInstances()) {
            myFactories.add(factory);
            myAllQNames.addAll(factory.getElementQNames());
            myExtensionsQNames.addAll(factory.getElementQNames());
        }
    }

    public static EntityFactoryRegistry getInstance(){
        return INSTANCE;
    }

    public Collection<EntityFactory> getFactories(){
        return Collections.unmodifiableCollection( myFactories );
    }
    
    public Set<QName> getAllQNames() {
        return Collections.unmodifiableSet(myAllQNames);
    }
    
    public Set<QName> getExtensionsQNames() {
        return Collections.unmodifiableSet(myExtensionsQNames);
    }

    private static final EntityFactoryRegistry INSTANCE = 
        new EntityFactoryRegistry();
    
    private Collection<EntityFactory> myFactories ;
    private Set<QName> myAllQNames;
    private Set<QName> myExtensionsQNames;
}
