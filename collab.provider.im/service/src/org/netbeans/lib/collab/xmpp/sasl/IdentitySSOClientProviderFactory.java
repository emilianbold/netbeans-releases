/*
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.
 */

package org.netbeans.lib.collab.xmpp.sasl;

import org.netbeans.lib.collab.SASLClientProvider;
import org.netbeans.lib.collab.SASLClientProviderFactory;

/**
 *
 * @author mridul
 */
public class IdentitySSOClientProviderFactory implements SASLClientProviderFactory{
    
    public static final String SUN_IDENTITY_SSO_SASL_MECHANISM = "SUN-IDENTITY-SSO-TOKEN";
    
    public String[] getSupportedMechanisms() {
        return new String[] {SUN_IDENTITY_SSO_SASL_MECHANISM};
    }

    public SASLClientProvider createInstance(String mechanism) {
        if (SUN_IDENTITY_SSO_SASL_MECHANISM.equals(mechanism)){
            return new IdentitySSOSASLClientProvider();
        }
        return null;
    }
}