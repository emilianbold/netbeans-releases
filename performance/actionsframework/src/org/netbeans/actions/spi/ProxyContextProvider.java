/*
 * ProxyContextProvider.java
 *
 * Created on January 25, 2004, 9:28 PM
 */

package org.netbeans.actions.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.actions.api.ContextProvider;

/** Convenience implementation of ContextProvider which can proxy an
 * array of ContextProviders.
 *
 * @author  Tim Boudreau
 */
public final class ProxyContextProvider implements ContextProvider {
    private ContextProvider[] providers = null;
    
    public ProxyContextProvider() {
    }
    
    /** Creates a new instance of ProxyContextProvider */
    public ProxyContextProvider(ContextProvider[] providers) {
        setProviders(providers);
        assert !Arrays.asList(providers).contains(this) : 
            "ProxyContextProvider cannot recursively proxy itself"; //NOI18N
    }
    
    /** Set the providers from which this provider will compose its 
     * context */
    public void setProviders(ContextProvider[] providers) {
        this.providers = providers;
    }
    
    public Map getContext() {
        if (providers == null || providers.length == 0) {
            return Collections.EMPTY_MAP;
        }
        Map[] m = new Map[providers.length];
        for (int i=0; i < m.length; i++) {
            if (providers[i] == this) {
                throw new IllegalStateException (
                "ProxyContextProvider cannot recursively proxy itself"); //NOI18N
            }
            m[i] = providers[i].getContext();
        }
        return new ContextProviderSupport.ProxyMap(m);
    }    
    
}
