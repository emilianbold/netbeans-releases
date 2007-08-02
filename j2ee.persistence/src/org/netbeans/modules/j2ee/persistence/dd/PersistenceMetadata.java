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

package org.netbeans.modules.j2ee.persistence.dd;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.openide.filesystems.FileObject;

/**
 * Provider of model based on persistence.xsd schema.
 * Provided model is representation of deployment descriptor
 * as defined in persistence specification.
 *
 * @author Martin Adamek
 */
public final class PersistenceMetadata {
    
    private static final PersistenceMetadata DEFAULT = new PersistenceMetadata();
    private Map ddMap;
    
    private PersistenceMetadata() {
        ddMap = new WeakHashMap(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static PersistenceMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in persistence.xsd
     * 
     * @param fo persistence.xml deployment descriptor. 
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public Persistence getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        Persistence persistence = null;
        synchronized (ddMap) {
            persistence = (Persistence) ddMap.get(fo);
            if (persistence == null) {
                persistence = Persistence.createGraph(fo.getInputStream());
                ddMap.put(fo, persistence);
            }
        }
        return persistence;
    }

}