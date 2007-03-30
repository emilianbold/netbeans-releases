/*
 * WSConfigurationProviderRegistry.java
 *
 * Created on March 29, 2007, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.configuration;

import java.util.HashSet;
import java.util.Set;


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
        return providers;
    }
    
}
