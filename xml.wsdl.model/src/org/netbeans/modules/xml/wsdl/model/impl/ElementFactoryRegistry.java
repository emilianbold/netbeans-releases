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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.spi.*;
import org.netbeans.modules.xml.xam.AbstractModel;

/**
 *
 * @author rico
 * Registry for factories of WSDL elements. In order to register an ElementFactory,
 * a QName must be provided of an element for which the factory will create a
 * WSDLComponent.
 */
public class ElementFactoryRegistry {
    
    static ElementFactoryRegistry registry = new ElementFactoryRegistry();
    private Map<QName, ElementFactory> map =
             new Hashtable<QName, ElementFactory>();
    private List<ElementFactoryProvider> providers = new ArrayList<ElementFactoryProvider>();
    
    /** Creates a new instance of ElementRegistry */
    private ElementFactoryRegistry() {
    }
    
    public static ElementFactoryRegistry getDefault(){
        return registry;
    }

    public void register(ElementFactoryProvider provider) {
        Collection<ElementFactory> factories = provider.getElementFactories();
        for(ElementFactory factory : factories){
            register(factory.getElementQNames(), factory);
        }
        providers.add(provider);
    }
    
    public void unregister(ElementFactoryProvider provider) {
        Collection<ElementFactory> factories = provider.getElementFactories();
        for(ElementFactory factory : factories){
            unregister(factory);
        }
        providers.remove(provider);
    }
    
    public List<ElementFactoryProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }
    
    public void register(Set<QName> types, ElementFactory fac){
        for (QName q : types) {
            map.put(q, fac);
        }
        resetQNameCache();
    }
    
    public void unregister(ElementFactory fac){
        for (QName q : fac.getElementQNames()) {
            map.remove(q);
        }
        resetQNameCache();
    }
    
    public ElementFactory get(QName type){
        return map.get(type);
    }
    
    private Set<Class> knownEmbeddedModelTypes = null;
    private Set<QName> knownQNames = null;
    private Set<String> knownNames = null;
    
    public void resetQNameCache() {
        knownEmbeddedModelTypes = null;
        knownQNames = null;
        knownNames = null;
    }
    
    private Set<QName> getQNames(ElementFactoryProvider p) {
        Set<QName> ret = new HashSet<QName>();
        for (ElementFactory f : p.getElementFactories()) {
            ret.addAll(f.getElementQNames());
        }
        return ret;
    }
    
    public Set<QName> getKnownQNames() {
        if (knownQNames == null) {
            knownQNames = new HashSet<QName>();
            for (ElementFactoryProvider p : getProviders()) {
                knownQNames.addAll(getQNames(p));
            }
        }
        return knownQNames;
    }

    public Set<String> getKnownElementNames() {
        if (knownNames == null) {
            knownNames = new HashSet<String>();
            for (QName q : getKnownQNames()) {
                knownNames.add(q.getLocalPart());
            }
        }
        return knownNames;
    }
    
    public void addEmbeddedModelQNames(AbstractModel embeddedModel) {
        if (knownEmbeddedModelTypes == null) {
            knownEmbeddedModelTypes = new HashSet();
        }
        if (! knownEmbeddedModelTypes.contains(embeddedModel.getClass())) {
            getKnownQNames().addAll(embeddedModel.getQNames());
            knownNames = null;
        }
    }
}
