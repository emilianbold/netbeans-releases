/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * SchemaModelImpl.java
 *
 * Created on October 3, 2005, 2:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.DocumentModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class EmbeddedSchemaModelImpl extends SchemaModelImpl {
    
    private DocumentModel delegate;
    private Element element;
    private SchemaImpl schema;
    
    public EmbeddedSchemaModelImpl(DocumentModel delegate, Element element) {
        super(delegate.getModelSource());
        if (! (delegate instanceof AbstractModel)) {
            throw new IllegalArgumentException("Expect instance of AbstractComponent");
        }
        this.delegate = delegate;
        this.element = element;
    }
    
    //reimplementation of Model APIs to account for delegate model
    public void addComponentListener(ComponentListener cl){
        delegate.addComponentListener(cl);
    }
    
    public void removeComponentListener(ComponentListener cl){
        delegate.removeComponentListener(cl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        delegate.removePropertyChangeListener(pcl);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        delegate.addPropertyChangeListener(pcl);
    }
    
    public void removeUndoableEditListener(UndoableEditListener uel){
        delegate.removeUndoableEditListener(uel);
    }
    
    public void addUndoableEditListener(UndoableEditListener uel){
        delegate.addUndoableEditListener(uel);
    }
    
    public ModelSource getModelSource() {
        return delegate.getModelSource();
    }
    
    public void sync() throws java.io.IOException{
        delegate.sync();
    }
    
    public boolean inSync(){
        return delegate.inSync();
    }
    
    public State getState(){
        return delegate.getState();
    }
    
    public void startTransaction(){
        delegate.startTransaction();
    }
    
    public void endTransaction() throws IOException{
        delegate.endTransaction();
    }
    
    public SchemaImpl getSchema() {
        if(schema == null){
            schema = (SchemaImpl)this.getFactory().create(element, null);
        }
        return schema;
    }
    
    //from AbstractModel
    public  Document getDocument(){
        return delegate.getDocument();
    }
    
    public synchronized void validateWrite() {
        getDelegate().validateWrite();
    }
    
    public ModelAccess getAccess(){
        return getDelegate().getAccess();
    }
    
    public void firePropertyChangeEvent(PropertyChangeEvent event) {
        getDelegate().firePropertyChangeEvent(event);
    }
    
    public void fireComponentChangedEvent(ComponentEvent evt) {
        getDelegate().fireComponentChangedEvent(evt);
    }

    public SchemaComponent getRootComponent() {
        return getSchema();
    }
    
    private AbstractModel getDelegate() {
        return (AbstractModel) delegate;
    }
}
