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
package org.netbeans.modules.xml.axi.impl;

import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelAccess;

/**
 * ModelAccess implementation.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ModelAccessImpl extends ModelAccess {
    
    /**
     * Creates a new instance of ModelAccessImpl
     */
    public ModelAccessImpl(AXIModel model) {
        this.model = (AXIModelImpl)model;
    }
        
    public void addUndoableEditListener(UndoableEditListener listener) {
        model.addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
        model.removeUndoableEditListener(listener);
    }
    
    public void prepareForUndoRedo() {        
    }
    
    public void finishUndoRedo() {
    }
    
    private SchemaModel getSchemaModel() {
	return model.getSchemaModel();
    }
    
    public Model.State sync() throws IOException {
        //build the ref cache and listens to referenced AXIModels
        model.buildReferenceableCache();
        
        //run the validator
        if(!model.validate()) {
            setAutoSync(true);
            return Model.State.NOT_WELL_FORMED;
        }
        
        //initialize the AXIDocument for the first time.
        //and sets auto-sync to true.
        if(!model.isAXIDocumentInitialized()) {
            model.initializeAXIDocument();
            setAutoSync(true);
            return Model.State.VALID;
        }
        
        if(!model.doSync()) {
            return Model.State.NOT_SYNCED;
        }
        
        //if everythings goes well, return a valid state.
	return Model.State.VALID;
    }   
        
    public void flush() {
        try {
            SchemaGeneratorFactory sgf = SchemaGeneratorFactory.getDefault();			
            sgf.updateSchema(model.getSchemaModel(), model.getSchemaDesignPattern());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Exception during flush: ",ex); //NOI18N
        } finally {
            model.getPropertyChangeListener().clearEvents();
        }
    }
    
    /**
     * Returns length in milliseconds since last edit if the model source buffer 
     * is dirty, or 0 if the model source is not dirty.
     */
    private long dirtyTimeMillis = 0;
    public long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }
    
    public void setDirty() {
        dirtyTimeMillis = System.currentTimeMillis();
    }
    
    public void unsetDirty() {
        dirtyTimeMillis = 0;
    }
    
    private AXIModelImpl model;        
}
