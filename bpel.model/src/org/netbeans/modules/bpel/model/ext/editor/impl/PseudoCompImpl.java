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

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorAttributes;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author nk160297
 * @version 1.0
 */
public class PseudoCompImpl extends EditorEntityImpl implements PseudoComp {

    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

    PseudoCompImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    PseudoCompImpl(EditorEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, EditorElements.PSEUDO_COMP);
    }

    @Override
    protected BpelEntity create( Element element ) {
        return null;
    }

    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                EditorAttributes.SOURCE,
                EditorAttributes.PATH,
                EditorAttributes.TYPE, 
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return PseudoComp.class;
    }

    public EntityUpdater getEntityUpdater() {
        return PseudoCompEntityUpdater.getInstance();
    }

    public Source getSource() {
        readLock();
        try {
            String str = getAttribute(EditorAttributes.SOURCE);
            if (str == null) {
                return Source.FROM;
            } else {
                return Source.forString(str);
            }
        }
        finally {
            readUnlock();
        }
    }

    public void setSource(Source value) {
        setBpelAttribute(EditorAttributes.SOURCE, value);
    }

    public void removeSource() {
        removeAttribute(EditorAttributes.SOURCE);
    }

    public String getParentPath() {
        readLock();
        try {
            return getAttribute(EditorAttributes.PARENT_PATH);
        }
        finally {
            readUnlock();
        }
    }

    public void setParentPath(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.PARENT_PATH, value);
    }

    public void removeParentPath() {
        removeAttribute(EditorAttributes.PARENT_PATH);
    }

    public SchemaReference<? extends GlobalType> getType() {
        readLock();
        try {
            return getSchemaReference(EditorAttributes.TYPE, GlobalType.class);
        }
        finally {
            readUnlock();
        }
    }

    public void setType(SchemaReference<? extends GlobalType> value) {
        setSchemaReference(EditorAttributes.TYPE, value);
    }

    public void removeType() {
        removeAttribute(EditorAttributes.TYPE);
    }

    public String getName() {
        readLock();
        try {
            return getAttribute(EditorAttributes.NAME);
        }
        finally {
            readUnlock();
        }
    }

    public void setName(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.NAME, value);
    }

    public void removeName() {
        removeAttribute(EditorAttributes.NAME);
    }

    public String getNamespace() {
        readLock();
        try {
            return getAttribute(EditorAttributes.NAMESPACE);
        }
        finally {
            readUnlock();
        }
    }

    public void setNamespace(String newValue) throws VetoException {
        setBpelAttribute(EditorAttributes.NAMESPACE, newValue);
    }

    public void removeNamespace() {
        removeAttribute(EditorAttributes.NAMESPACE);
    }

    public boolean isAttribute() {
        readLock();
        try {
            TBoolean result = getBooleanAttribute(EditorAttributes.IS_ATTRIBUTE);
            return TBoolean.YES.equals(result);  
        }
        finally {
            readUnlock();
        }
    }

    public void setIsAttribute(boolean castAny) {
        if (castAny) {
            setBpelAttribute(EditorAttributes.IS_ATTRIBUTE, TBoolean.YES);
        } else {
            removeAttribute(EditorAttributes.IS_ATTRIBUTE);
        }
    }

    private static class PseudoCompEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new PseudoCompEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private PseudoCompEntityUpdater() {

        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof PseudoComps) {
                PseudoComps pseudoComps = (PseudoComps)target;
                PseudoComp pseudoComp = (PseudoComp)child;
                switch (operation) {
                case ADD:
                    pseudoComps.addPseudoComp(pseudoComp);
                    break;
                case REMOVE:
                    pseudoComps.remove(pseudoComp);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof PseudoComps) {
                PseudoComps pseudoComps = (PseudoComps)target;
                PseudoComp pseudoComp = (PseudoComp)child;
                switch (operation) {
                case ADD:
                    pseudoComps.insertPseudoComp(pseudoComp, index);
                    break;
                case REMOVE:
                    pseudoComps.remove(pseudoComp);
                    break;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getType()};
    }

}

