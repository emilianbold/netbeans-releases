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

package org.netbeans.modules.vmd.api.inspector;

import java.util.Collection;
import java.util.Collections;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.openide.util.WeakSet;

/**
 *
 * @author Karol Harezlak
 */
public final class InspectorRegistry {

    private static WeakSet<DesignComponent> registry = new WeakSet<DesignComponent>();;

    private InspectorRegistry() {
    }

    public static void addComponent(DesignComponent component) {
        registry.add(component);
    }

    public static Collection<DesignComponent> getRegistry() {
        if (registry == null)
            return null;
        
        return Collections.unmodifiableCollection(registry);
    }
    
    public static void clearRegistry() {
        if (registry != null)
            registry.clear();
    }
    
    public static void removeAll(Collection<DesignComponent> components) {
        if ((registry != null && registry.size() > 0) && (components != null && components.size() == 0))
            registry.removeAll(components);
    }
    
    public static void remove(DesignComponent component) {
        if (registry != null || component != null)
            registry.remove(component);
    }
    
}
