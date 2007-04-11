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
package org.netbeans.modules.java.source.usages;

import java.util.EventListener;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;

/**
 *
 * @author Tomas Zezula
 */
public interface ClassIndexImplListener extends EventListener {

    /**
     * Called when the new declared types are added
     * into the {@link ClassIndexImpl}
     * @param event specifying the added types
     */
    public void typesAdded (ClassIndexImplEvent event);
    
    /**
     * Called when declared types are removed
     * from the {@link ClassIndexImpl}
     * @param event specifying the removed types
     */
    public void typesRemoved (ClassIndexImplEvent event);
        
    /**
     * Called when some declared types are changed.
     * @param event specifying the changed types
     */
    public void typesChanged (ClassIndexImplEvent event);

}
