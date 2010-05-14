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

package org.netbeans.modules.soa.jca.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Registry of global rar, caller of operations on this instance need to
 * synchronize on this instance.
 *
 * @author echou
 */
public class GlobalRarRegistry {

    private static GlobalRarRegistry instance;

    // list of registered rars
    private List<GlobalRarProvider> knownRars;

    public synchronized static GlobalRarRegistry getInstance() {
        if (instance == null) {
            instance = new GlobalRarRegistry();
        }
        return instance;
    }

    private GlobalRarRegistry() {
        Lookup.Template<GlobalRarProvider> template =
                new Lookup.Template<GlobalRarProvider> (GlobalRarProvider.class);
        final Lookup.Result<GlobalRarProvider> result = Lookup.getDefault().lookup(template);
        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                initialize(result);
            }
        });
        initialize(result);
    }

    private synchronized void initialize(Lookup.Result<GlobalRarProvider> result) {
        knownRars = new ArrayList<GlobalRarProvider> ();
        for(GlobalRarProvider rarProvider : result.allInstances()) {
            knownRars.add(rarProvider);
        }

        Collections.sort(knownRars, new Comparator<GlobalRarProvider>() {
            public int compare(GlobalRarProvider o1, GlobalRarProvider o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public synchronized GlobalRarProvider getItemAt(int i) {
        return knownRars.get(i);
    }

    public synchronized int getRegistrySize() {
        return knownRars.size();
    }

    public synchronized List<GlobalRarProvider> getKnownRars() {
        return Collections.unmodifiableList(knownRars);
    }

    public synchronized GlobalRarProvider getRar(String name) {
        for (GlobalRarProvider rarProvider : knownRars) {
            if (rarProvider.getName().equals(name)) {
                return rarProvider;
            }
        }
        return null;
    }

    public synchronized int getRarIndex(String name) {
        int i = 0;
        for (GlobalRarProvider rarProvider : knownRars) {
            if (rarProvider.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
