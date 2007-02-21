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
package org.netbeans.modules.soa.ui;

import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * This lookup is a kind of ProxyLookup.
 * It allows extending an original lookup with additional elements.
 *
 * @author nk160297
 */
public class ExtendedLookup extends ProxyLookup {

    public ExtendedLookup(Lookup original, Object... newItems) {
        super(new Lookup[] {Lookups.fixed(newItems), original});
    }

    public ExtendedLookup(Lookup original, List newItemsList) {
        super(new Lookup[] {Lookups.fixed(newItemsList.toArray()), original});
    }
    
    public static Lookup createExtendedLookup(Lookup original, Object... newItems) {
        if (original == null) {
            return Lookups.fixed(newItems);
        } else {
            Lookup[] lookupArr = new Lookup[] {Lookups.fixed(newItems), original};
            return new ProxyLookup(lookupArr);
        }
    }
    
}
