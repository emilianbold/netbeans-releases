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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapModelImpl extends AbstractDocumentModel<TMapComponent> implements TMapModel {
    private TMapComponentFactory myFactory;
    private TransformMap myRoot;
    private AtomicReference<SyncUpdateVisitor> myUpdateVisitor = 
            new AtomicReference<SyncUpdateVisitor>();
    
    private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

    private final Lock readLock = myLock.readLock();

    private final Lock writeLock = myLock.writeLock();

    public TMapModelImpl(ModelSource source) {
        super(source);
        myFactory = new TMapComponentFactoryImpl(this);
    }
    
    public TMapComponent createRootComponent(Element root) {
        TransformMap transformMap = (TransformMap)myFactory.create(root, null);
        if (transformMap != null) {
            myRoot = transformMap;
        } else {
            return null;
        }
        return getTransformMap();
    }

    protected ComponentUpdater<TMapComponent> getComponentUpdater() {
        SyncUpdateVisitor updater = myUpdateVisitor.get();
        if (updater == null) {
            myUpdateVisitor.compareAndSet(null, new SyncUpdateVisitor());
            updater = myUpdateVisitor.get();
        }
        return updater;
    }

    public TransformMap getTransformMap() {
        return (TransformMap)getRootComponent();
    }
    
    public TMapComponent getRootComponent() {
        return myRoot;
    }

    public TMapComponent createComponent(TMapComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public TMapComponentFactory getFactory() {
        return myFactory;
    }

    public <V> V invoke(Callable<V> action) throws Exception {
//        boolean isInTransaction = isIntransaction();
        V result = null;
        
        try {
//            if (!isInTransaction) {
                startTransaction();         
//            }

            result = action.call();
        } finally {
//            if (!isInTransaction) {
                endTransaction();
//            }
        }
        return result;
    }
}
