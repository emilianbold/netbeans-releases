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

/*
 * WSConfigurationProviderRegistry.java
 *
 * Created on March 29, 2007, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.configuration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.util.Lookup;


/**
 *
 * @author rico
 */
public class WSConfigurationProviderRegistry {
    static WSConfigurationProviderRegistry registry = new WSConfigurationProviderRegistry();
    
    private Set<WSConfigurationProvider> providers = new LinkedHashSet<WSConfigurationProvider>();
    
    
    /** Creates a new instance of WSConfigurationProviderRegistry */
    private WSConfigurationProviderRegistry() {
    }
    
    public static WSConfigurationProviderRegistry getDefault(){
        return registry;
    }
    
    public void register(WSConfigurationProvider provider){
        providers.add(provider);
    }
    
    public void unregister(WSConfigurationProvider provider){
        providers.remove(provider);
    }
    
    public Set<WSConfigurationProvider> getWSConfigurationProviders(){
        populateRegistry();
        return providers;
    }
    
    private void populateRegistry(){
        if(providers.isEmpty()){
            Lookup.Result<WSConfigurationProvider> results = Lookup.getDefault().
                    lookup(new Lookup.Template<WSConfigurationProvider>(WSConfigurationProvider.class));
            Collection<? extends WSConfigurationProvider> providers = results.allInstances();
            for(WSConfigurationProvider provider : providers){
                register(provider);
            }
        }
    }
    
}
