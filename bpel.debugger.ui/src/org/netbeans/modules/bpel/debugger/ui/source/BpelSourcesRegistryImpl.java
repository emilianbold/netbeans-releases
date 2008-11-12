/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.bpel.debugger.ui.source;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry;

/**
 *
 * @author Alexander Zgursky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry.class)
public class BpelSourcesRegistryImpl implements BpelSourcesRegistry {
    private Set<String> mySourceRoots = new HashSet<String>();
    
    /** Creates a new instance of BpelSourcesRegistryImpl */
    public BpelSourcesRegistryImpl() {
    }

    public synchronized boolean addSourceRoot(String sourceRoot) {
        return mySourceRoots.add(sourceRoot);
    }

    public synchronized boolean removeSourceRoot(String sourceRoot) {
        return mySourceRoots.remove(sourceRoot);
    }

    public synchronized String[] getSourceRoots() {
        return mySourceRoots.toArray(new String[mySourceRoots.size()]);
    }
    
}
