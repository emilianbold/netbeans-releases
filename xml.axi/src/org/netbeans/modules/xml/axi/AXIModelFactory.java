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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Factory class to create an AXI model.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelFactory extends AbstractModelFactory<AXIModel> {
            
    /**
     * Creates a new instance of AXIModelFactory
     */
    private AXIModelFactory() {
    }
    
    /**
     * Return the single factory instance.
     */
    public static AXIModelFactory getDefault() {
        return instance;
    }
    
    /**
     * Convenient method to get the AXI model.
     */
    public AXIModel getModel(SchemaModel schemaModel) {
        FileObject file = (FileObject)schemaModel.getModelSource().
                getLookup().lookup(FileObject.class);
        Lookup lookup = null;
        if(file == null) {
            Object[] objectsToLookup = {schemaModel};
            lookup = Lookups.fixed(objectsToLookup);
        } else {
            Object[] objectsToLookup = {schemaModel, file};
            lookup = Lookups.fixed(objectsToLookup);
        }
        ModelSource source = new ModelSource(lookup, true);
        assert(source != null);
        return getModel(source);
    }    
    
    /**
     * Get model from given model source.  Model source should at very least 
     * provide lookup for SchemaModel
     */
    protected AXIModel getModel(ModelSource modelSource) {
        Lookup lookup = modelSource.getLookup();
        assert lookup.lookup(SchemaModel.class) != null;
        return super.getModel(modelSource);
    }
    
    /**
     * For AXI, SchemaModel is the key.
     */
    protected Object getKey(ModelSource modelSource) {
        return modelSource.getLookup().lookup(SchemaModel.class);
    }
    
    /**
     * Creates the AXI model here.
     */
    protected AXIModel createModel(ModelSource modelSource) {
        return new AXIModelImpl(modelSource);
    }
    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * Singleton instance of the factory class.
     */
    private static AXIModelFactory instance = new AXIModelFactory();
}
