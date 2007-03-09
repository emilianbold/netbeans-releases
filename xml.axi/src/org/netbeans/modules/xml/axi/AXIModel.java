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

import java.util.List;
import org.netbeans.modules.xml.axi.impl.AXIDocumentImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelBuilderQuery;
import org.netbeans.modules.xml.axi.impl.ModelAccessImpl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;

/**
 * Represents an AXI model for a schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIModel extends AbstractModel<AXIComponent> {

    /**
     * Creates a new instance AXIModel.
     */
    public AXIModel(ModelSource modelSource) {
        super(modelSource);
        this.factory = new AXIComponentFactory(this);
        this.root = new AXIDocumentImpl(this, getSchemaModel().getSchema());
        this.modelAccess = new ModelAccessImpl(this);
    }
    
    /**
     * Returns other AXIModels this model refers to.
     */
    public abstract List<AXIModel> getReferencedModels();
            
    /**
     * Returns the schema design pattern property.
     */	
    public abstract SchemaGenerator.Pattern getSchemaDesignPattern();
	
    /**
     * Sets the schema design pattern property.
     */	
    public abstract void setSchemaDesignPattern(SchemaGenerator.Pattern p);
    
    /**
     * Returns the corresponding SchemaModel.
     * @return Returns the corresponding SchemaModel.
     */
    public SchemaModel getSchemaModel() {
        return (SchemaModel)getModelSource().getLookup().
                lookup(SchemaModel.class);
    }
        
    /**
     * Returns the root of the AXI model.
     */
    public AXIDocument getRoot() {
        return root;
    }
    
    /**
     * Returns the component factory.
     */
    public AXIComponentFactory getComponentFactory() {
        return factory;
    }
    
    /**
     * Returns true if the underlying document is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        ModelSource ms = getModelSource();
        assert(ms != null);
        if (ms.isEditable()) {
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            assert(fo != null);
            return !fo.canWrite();
        }
        return true;
    }
    
    /**
     * Returns true if there exists a corresponding visible AXIComponent.
     */
    public boolean canView(SchemaComponent component) {
        AXIModelBuilderQuery factory = new AXIModelBuilderQuery((AXIModelImpl)this);
        return factory.canView(component);
    }

    /////////////////////////////////////////////////////////////////////
    ///////////////////////////// XAM methods ///////////////////////////
    /////////////////////////////////////////////////////////////////////    
    public ModelAccess getAccess() {
        return modelAccess;
    }
    
    public void addChildComponent(Component parent, Component child, int index) {
        AXIComponent axiParent = (AXIComponent)parent;
        AXIComponent axiChild = (AXIComponent)child;
        axiParent.addChildAtIndex(axiChild, index);
    }

    public void removeChildComponent(Component child) {
        AXIComponent axiChild = (AXIComponent)child;        
        AXIComponent axiParent = axiChild.getParent();
        axiParent.removeChild(axiChild);
    }
    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * Keeps a component factory.
     */
    private AXIComponentFactory factory;
    
    /**
     * ModelAccess
     */
    private ModelAccess modelAccess;
    
    /**
     * Root of the AXI tree.
     */
    private AXIDocument root;
}
