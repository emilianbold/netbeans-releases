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
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author nk160297
 * @version 1.0
 */
public class PseudoCompsImpl extends EditorEntityImpl implements PseudoComps {

    PseudoCompsImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    PseudoCompsImpl(EditorEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, EditorElements.PSEUDO_COMPS);
    }

    @Override
    protected BpelEntity create( Element element ) {
        if ( EditorElements.PSEUDO_COMPS.getName().equals(element.getLocalName())) {
            return new PseudoCompsImpl(getFactory(), getModel(), element);
        }
        return null;
    }

    public Class<? extends BpelEntity> getElementType() {
        return PseudoComps.class;
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }

    public EntityUpdater getEntityUpdater() {
        return PseudoCompsEntityUpdater.getInstance();
    }

    private static class PseudoCompsEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new PseudoCompsEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private PseudoCompsEntityUpdater() {
        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof Editor) {
                Editor editor = (Editor)target;
                PseudoComps pseudoComps = (PseudoComps)child;
                switch (operation) {
                case ADD:
                    editor.setPseudoComps(pseudoComps);
                    break;
                case REMOVE:
                    editor.removePseudoComps();
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            update(target, child, operation);
        }
    }

    public PseudoComp[] getPseudoComps() {
        readLock();
        try {
            List<PseudoComp> list = getChildren( PseudoComp.class );
            return list.toArray( new PseudoComp[ list.size()] );
        }
        finally {
            readUnlock();
        }
    }

    public PseudoComp getPseudoComp(int i) {
        return getChild( PseudoComp.class , i );
    }

    public void removePseudoComp(int i) {
        removeChild( PseudoComp.class , i );
    }

    public void setPseudoComps(PseudoComp[] pseudoComps) {
        setArrayBefore( pseudoComps , PseudoComp.class );
    }

    public void setPseudoComp(PseudoComp pseudoComp, int i) {
        setChildAtIndex(pseudoComp, PseudoComp.class, i);
    }

    public void addPseudoComp(PseudoComp pseudoComp) {
        addChild(pseudoComp, PseudoComp.class);
    }

    public void insertPseudoComp(PseudoComp pseudoComp, int i) {
        insertAtIndex(pseudoComp, PseudoComp.class, i );
    }

}
