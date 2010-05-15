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
package org.netbeans.modules.bpel.model.ext.editor.impl;

import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmContainer;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.w3c.dom.Element;

/**
 * A base class for different classes which implements NestedExtensionsContainer.
 * 
 * @author Nikita Krjukov
 * @version 1.0
 */
public abstract class EditorExtensionContainerImpl extends EditorEntityImpl 
        implements LsmContainer {

    EditorExtensionContainerImpl(BpelModelImpl model, Element e) {
        super(model, e);
    }

    EditorExtensionContainerImpl(BpelBuilderImpl builder, EditorElements ee) {
        super(builder, ee);
    }

    //-------------------------------------------------------------------------
    
    public EntityUpdater getEntityUpdater() {
        return NestedExtensionUpdater.getInstance();
    }

    private static class NestedExtensionUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE = new NestedExtensionUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private NestedExtensionUpdater() {
        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof LsmContainer) {
                LsmContainer cont = (LsmContainer)target;
                LocationStepModifier childExt = (LocationStepModifier)child;
                switch (operation) {
                case ADD:
                    cont.addExtension(childExt);
                    break;
                case REMOVE:
                    cont.removeExtension(childExt);
                    break;
                }
            } else
            if (target instanceof ExtensibleElements) {
                switch (operation) {
                case ADD:
                    ((ExtensibleElements)target).addExtensionEntity(ExtensionEntity.class, child);
                    break;
                case REMOVE:
                    ((ExtensibleElements)target).remove(child);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof LsmContainer) {
                LsmContainer cont = (LsmContainer)target;
                LocationStepModifier childExt = (LocationStepModifier)child;
                switch (operation) {
                case ADD:
                    cont.insertExtension(childExt, index);
                    break;
                case REMOVE:
                    cont.removeExtension(index);
                    break;
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    
    public <T extends LocationStepModifier> List<T> getChildrenLsm(Class<T> extClass) {
        if (extClass == null) {
            List<LocationStepModifier> result = getChildren(LocationStepModifier.class);
            return (List<T>)result;
        } else {
            return getChildren(extClass);
        }
    }

    public LocationStepModifier getChildLsm(int index) {
         return getChild(LocationStepModifier.class, index);
   }

    public void addExtension(LocationStepModifier newEntry) {
        addChild(newEntry, LocationStepModifier.class);
    }

    public void setExtensions(List<LocationStepModifier> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        //
        LocationStepModifier[] extArr =
                extensions.toArray(new LocationStepModifier[extensions.size()]);
        setArrayBefore(extArr , LocationStepModifier.class);
    }

    public void setExtension(int index, LocationStepModifier newEntry) {
        setChildAtIndex(newEntry, LocationStepModifier.class, index);
    }

    public void removeExtension(int index) {
        removeChild(LocationStepModifier.class , index);
    }

    public void insertExtension(LocationStepModifier newEntry, int index) {
        insertAtIndex(newEntry, LocationStepModifier.class, index);
    }

    public void removeExtension(LocationStepModifier deletedEntry) {
        remove(deletedEntry);
    }

}
