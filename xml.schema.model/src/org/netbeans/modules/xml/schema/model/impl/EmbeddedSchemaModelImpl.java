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
