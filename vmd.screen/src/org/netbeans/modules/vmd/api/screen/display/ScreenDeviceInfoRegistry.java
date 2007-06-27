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
package org.netbeans.modules.vmd.api.screen.display;

import java.util.WeakHashMap;
import org.netbeans.modules.vmd.api.model.DesignDocument;

/**
 *
 * @author Karol Harezlak
 */
        
public final class ScreenDeviceInfoRegistry {
    
    private static WeakHashMap<DesignDocument, ScreenDeviceInfo> registry = new WeakHashMap<DesignDocument, ScreenDeviceInfo>();
    
    private ScreenDeviceInfoRegistry() {
    }
    
    /**
     * Registry ScreenDeviceInfo with connection with DesignDocument
     * @param document 
     * @param screenInfo 
     */
    public void registerScreenDeviceInfo(DesignDocument document, ScreenDeviceInfo screenInfo) {
        registry.put(document, screenInfo);
    }
    
    /**
     * Returns ScreenDeviceInfo for given DesignDocument 
     * @param document 
     * @return 
     */
    public ScreenDeviceInfo getScreenDeviceInfo(DesignDocument document) {
        if (registry.get(document) == null)
            throw new IllegalArgumentException("No ScreenDeviceInfo for " + document); //NOI18N
        return registry.get(document);
    }
   
}
