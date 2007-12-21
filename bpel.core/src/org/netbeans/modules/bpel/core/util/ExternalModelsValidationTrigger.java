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
package org.netbeans.modules.bpel.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;


/**
 * @author ads
 *
 */
class ExternalModelsValidationTrigger implements ChangeEventListener {
    
    ExternalModelsValidationTrigger( BPELValidationController controller ){
        myController = controller;
        isTriggerDirty = new AtomicBoolean( false );
        myListener = new WeakTriggerListener( this );
    }

    public void notifyArrayUpdated( ArrayUpdateEvent event ) {
        BpelEntity parent = event.getParent();
        BpelEntity[] newArray =  event.getNewArray();
        BpelEntity[] oldArray =  event.getOldArray();
        if ( !(parent instanceof Process) ) {
            return;
        }
        if ( oldArray!= null && oldArray.length >0 ) {
            if ( oldArray[0] instanceof Import ) {
                for (BpelEntity entity : oldArray) {
                    Import impt = (Import) entity;
                    Collection<Model> collection = getModels(impt);
                    for (Model model : collection) {
                        removeListener(model);
                    }
                }
                loadImports( );
            }
            else {
                return;
            }
        }
        else if ( newArray!= null && newArray.length >0 ) {
            if ( newArray[0] instanceof Import ) {
                loadImports( );
            }
            else {
                return;
            }
        }
    }

    public void notifyEntityInserted( EntityInsertEvent event ) {
        BpelEntity parent = event.getParent();
        BpelEntity entity = event.getValue();
        if ( parent instanceof Process && entity instanceof Import ) {
            Import impt = ( Import )entity;
            Collection<Model> collection = getModels(impt);
            for (Model model : collection) {
                addListener(model);
            }
        }
    }

    public void notifyEntityRemoved( EntityRemoveEvent event ) {
        BpelEntity parent = event.getParent();
        BpelEntity entity = event.getOldValue();
        if ( parent instanceof Process && entity instanceof Import ) {
            Import impt = ( Import )entity;
            Collection<Model> collection = getModels(impt);
            for (Model model : collection) {
                removeListener(model);
            }
        }        
    }

    public void notifyEntityUpdated( EntityUpdateEvent event ) {
        BpelEntity parent = event.getParent();
        BpelEntity old = event.getOldValue();
        BpelEntity newValue = event.getNewValue();
        if ( parent instanceof Process && 
                ( ( old instanceof Import ) || ( newValue instanceof Import) )){
            Import oldImpt = ( Import ) old;
            Import newImpt = ( Import ) newValue;
            Collection<Model> collection = getModels( oldImpt);
            for (Model model : collection) {
                removeListener(model);
            }
            collection = getModels(newImpt);
            for (Model model : collection) {
                addListener(model);
            }
        }
    }

    public void notifyPropertyRemoved( PropertyRemoveEvent event ) {
        BpelEntity parent = event.getParent();
        if ( parent instanceof Import ) {
            Import impt = ( Import ) parent;
            Collection<Model> oldCollection = getOldModels( impt, 
                    event.getName() , event.getOldValue() );
            reloadImport(impt, oldCollection);
        }
    }

    public void notifyPropertyUpdated( PropertyUpdateEvent event ) {
        BpelEntity parent = event.getParent();
        if ( parent instanceof Import ) {
            Import impt = ( Import ) parent;
            Collection<Model> oldCollection = getOldModels( impt, 
                    event.getName() , event.getOldValue() );
            reloadImport(impt, oldCollection);
        }
        else if ( BpelModel.STATE.equals( event.getName() ) ) {
            Object value = event.getNewValue();
            if ( Model.State.VALID.equals(value) && !importsLoaded) {
                loadImports();
            }
        }
    }
    
    void clearTrigger() {
        Runnable runnable = new Runnable() {

            public void run() {
                importsLoaded = false;
                Process process = getController().getModel().getProcess();
                if (process == null) {
                    return;
                }
                isTriggerDirty.set(false);
                Import[] imports = process.getImports();
                for (Import impt : imports) {
                    Collection<Model> collection = getModels(impt);
                    for (Model model : collection) {
                        removeListener(model);
                    }
                }
            }
        };
        getController().getModel().invoke(runnable);
    }
    
    void loadImports() {
        Runnable runnable = new Runnable() {

            public void run() {
                Process process = getController().getModel().getProcess();
                if (process == null) {
                    importsLoaded = false;
                    return;
                }
                isTriggerDirty.set(false);
                importsLoaded = true;
                Import[] imports = process.getImports();
                for (Import impt : imports) {
                    Collection<Model> collection = getModels(impt);
                    for (Model model : collection) {
                        addListener(model);
                    }
                }
            }
        };
        getController().getModel().invoke(runnable);
    }
    
    void changeHappened() {
        isTriggerDirty.compareAndSet( false, true );
    }
    
    boolean isTriggerDirty() {
        return isTriggerDirty.getAndSet(false);
    }
    
    private void reloadImport( Import impt, Collection<Model> oldCollection ) {
        for (Model model : oldCollection) {
            removeListener(model);
        }
        Collection<Model> collection = getModels(impt);
        for (Model model : collection) {
            addListener(model);
        }
    }
    
    private Collection<Model> getOldModels( Import impt, 
            String name, Object oldValue ) 
    {
        List<Model> models = new LinkedList<Model>();
        String location = null;
        String importType = null;
        String namespace = null;
        if ( Import.IMPORT_TYPE.equals( name ) ) {
            assert oldValue instanceof String;
            location = impt.getLocation();
            importType = (String) oldValue;
            namespace = impt.getNamespace();
        }
        else if ( Import.LOCATION.equals( name ) ) {
            assert oldValue instanceof String;
            location = (String) oldValue;
            importType = impt.getImportType();
            namespace = impt.getNamespace();
        }
        else if ( Import.NAMESPACE.equals( name ) ) {
            assert oldValue instanceof String;
            location = impt.getLocation();
            importType = impt.getImportType();
            namespace = (String) oldValue;
        }
        Model model = ImportHelper.getWsdlModel( getController().getModel(),
                location, importType );
        if ( model != null ) {
            models.add( model );
        }
        model = ImportHelper.getSchemaModel( getController().getModel(), 
                location, importType);
        if ( model != null ) {
            models.add( model );
        }
        Collection<SchemaModel> collection = ImportHelper.getInlineSchema(
                getController().getModel(), namespace, 
                location, importType );
        if ( collection!= null ) {
            models.addAll( collection );
        }
        return models;
    }
    
    private void addListener( Model model ) {
        model.addComponentListener( getListener() );
        model.addPropertyChangeListener(getListener());
    }
    
    private void removeListener( Model model ) {
        model.removeComponentListener(getListener());
        model.removePropertyChangeListener(getListener());
    }
    
    private BPELValidationController getController() {
        return myController;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Model> getModels( Import impt ) {
        if ( impt == null ) {
            return Collections.EMPTY_LIST;
        }
        List<Model> list = new LinkedList<Model>();
        Model wsdlModel = ImportHelper.getWsdlModel(impt);
        if ( wsdlModel!= null ) {
            list.add( wsdlModel );
        }
        Model schemaModel = ImportHelper.getSchemaModel(impt);
        if ( schemaModel != null ) {
            list.add( schemaModel );
        }
        String ns = impt.getNamespace();
        if ( ns!= null ) {
            Collection<SchemaModel> collection = 
                ImportHelper.getInlineSchema(impt, ns);
            if ( collection != null ) {
                list.addAll( collection );
            }
        }
        return list;
    }
    
    private WeakTriggerListener getListener() {
        return myListener;
    }
    
    private BPELValidationController myController;
    private AtomicBoolean isTriggerDirty;
    private WeakTriggerListener myListener;
    private boolean importsLoaded;
}
