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

/*
 * WSEditorProviderRegistry.java
 *
 * Created on February 17, 2006, 10:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.wseditor.spi;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Roderico Cruz
 */
public class WSEditorProviderRegistry {
    
    static WSEditorProviderRegistry registry = new WSEditorProviderRegistry();
    
    private Set<WSEditorProvider> editors = new HashSet<WSEditorProvider>();
    
    /**
     * Creates a new instance of WSEditorProviderRegistry
     */
    private WSEditorProviderRegistry() {
    }
    
    public static WSEditorProviderRegistry getDefault(){
        return registry;
    }
    
    public void register(WSEditorProvider provider){
        editors.add(provider);
    }
    
    public void unregister(WSEditorProvider provider){
        editors.remove(provider);
    }
    
    public Set<WSEditorProvider> getEditorProviders(){
        return editors;
    }
}
