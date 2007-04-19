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
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Lookup;


/**
 *
 * @author rico
 */
public class WSConfigurationProviderRegistry {
    static WSConfigurationProviderRegistry registry = new WSConfigurationProviderRegistry();
    
    private Set<WSConfigurationProvider> providers = new HashSet<WSConfigurationProvider>();
    
    
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
