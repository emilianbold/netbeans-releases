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

package org.netbeans.modules.xml.wsdl.ui.spi;

import java.util.ArrayList;
import java.util.List;

import org.openide.util.Lookup;

public class WSDLLookupProviderFactory {
    private static List<WSDLLookupProvider> providers;

    public static <T> T getObject(String namespace, Class<T> className ) {
        if (providers == null) {
            lookupFactories();
        }

        for (WSDLLookupProvider provider : providers) {
            if (provider.getProvider(namespace) != null) {
                return className.cast(provider.getProvider(namespace).getLookup().lookup(className));
            }
        }

        return null;
    }

    private static synchronized void lookupFactories() {
        if(providers != null)
            return;
        
        providers = new ArrayList<WSDLLookupProvider>();
        
        Lookup.Result result = Lookup.getDefault().lookup(
                new Lookup.Template(WSDLLookupProvider.class));
        
        for(Object obj: result.allInstances()) {
            WSDLLookupProvider factory = (WSDLLookupProvider) obj;
            providers.add(factory);
        }
        
    }
}
