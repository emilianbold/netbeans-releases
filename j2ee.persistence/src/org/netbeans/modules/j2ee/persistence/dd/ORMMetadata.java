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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;

/**
 * Provider of model based on persistence_ORM.xsd schema.
 * Provided model is merged representation of metadata defined in both
 * annotations and deployment descriptor with filled default values
 * as defined in persistence specification.
 *
 * @author  Martin Adamek
 */
public final class ORMMetadata {
    private static final ORMMetadata instance = new ORMMetadata();
    private Map<MetadataUnit, EntityMappings> annotationDDMap;
    
    private ORMMetadata() {
        // TODO: RETOUCHE
        //        annotationDDMap = new HashMap<MetadataUnit, EntityMappings>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static ORMMetadata getDefault() {
        return instance;
    }
    
    /**
     * Provides root element as defined in persistence_ORM.xsd
     *
     * @param mu unit providing ORM metadata defined in annotations and deployment descriptor.
     * It can be retrieved from {@link PersistenceProvider}
     * @return root element of schema or null if it doesn't exist for provided
     * MetadataUnit
     * @throws java.io.IOException
     * @see PersistenceProvider
     */
    public EntityMappings getRoot(MetadataUnit mu) throws IOException {
        // TODO: RETOUCHE
        //        EntityMappings annotationRoot = getAnnotationRoot(mu);
        //        EntityMappings entityMappings = (EntityMappings) MergedProvider.getDefault().getRoot(annotationRoot, null);
        //        return entityMappings;
        return null;
    }
    
    
    
    public boolean isScanInProgress() {
        // TODO: RETOUCHE
        //        return NNMDRListener.getDefault().isScanInProgress();
        return false;
    }
    
    public void waitScanFinished() {
        // TODO: RETOUCHE
        //NNMDRListener.getDefault().waitScanFinished();
        return;
    }
}
