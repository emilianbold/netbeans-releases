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

package org.netbeans.modules.editor.mimelookup.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class DefaultMimeDataProviderTest extends NbTestCase {

    /** Creates a new instance of DefaultMimeDataProviderTest */
    public DefaultMimeDataProviderTest(String name) {
        super(name);
    }

    public void testProviderRegistration() {
        Collection providers = Lookup.getDefault().lookupAll(MimeDataProvider.class);
        assertTrue("No providers registered", providers.size() > 0);
        
        ArrayList defaultProviders = new ArrayList();
        for (Iterator i = providers.iterator(); i.hasNext(); ) {
            MimeDataProvider provider = (MimeDataProvider) i.next();
            if (provider instanceof DefaultMimeDataProvider) {
                defaultProviders.add(provider);
            }
        }
        
        assertTrue("No default provider registered", defaultProviders.size() > 0);
        if (defaultProviders.size() > 1) {
            String msg = "Too many default providers registered:\n";
            
            for (Iterator i = defaultProviders.iterator(); i.hasNext();) {
                DefaultMimeDataProvider provider = (DefaultMimeDataProvider) i.next();
                msg += provider + "\n";
            }
            
            fail(msg);
        }
    }
}
