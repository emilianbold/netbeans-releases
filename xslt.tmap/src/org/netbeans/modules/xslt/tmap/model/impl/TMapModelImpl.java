/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xslt.tmap.model.impl;

import java.io.IOException;
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
}
