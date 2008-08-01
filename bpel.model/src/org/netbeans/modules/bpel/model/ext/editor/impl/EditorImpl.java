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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @author nk160297
 * @version 1.0
 */
public class EditorImpl extends EditorEntityImpl implements Editor {

    EditorImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    EditorImpl(EditorEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, EditorElements.EDITOR);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.modules.soa.model.bpel2020.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Editor.class;
    }

    public EntityUpdater getEntityUpdater() {
        return EditorEntityUpdater.getInstance();
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }

    private static class EditorEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new EditorEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private EditorEntityUpdater() {

        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof ExtensibleElements) {
                ExtensibleElements ee = (ExtensibleElements)target;
                switch (operation) {
                case ADD:
                    ee.addExtensionEntity(Editor.class, (Editor)child);
                    break;
                case REMOVE:
                    ee.remove(child);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof ExtensibleElements) {
                ExtensibleElements ee = (ExtensibleElements)target;
                switch (operation) {
                case ADD:
                    ee.addExtensionEntity(Editor.class, (Editor)child);
                    break;
                case REMOVE:
                    ee.remove(child);
                    break;
                }
            }
        }

    }

    public Casts getCasts() {
        return getChild(Casts.class);
    }

    public void removeCasts() {
        removeChild(Casts.class);
    }

    public void setCasts(Casts value) {
        setChild(value, Casts.class);
    }


    public PseudoComps getPseudoComps() {
        return getChild(PseudoComps.class);
    }

    public void removePseudoComps() {
        removeChild(PseudoComps.class);
    }

    public void setPseudoComps(PseudoComps pseudoComps) {
        setChild(pseudoComps, PseudoComps.class);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if ( EditorElements.CASTS.getName().equals(element.getLocalName())) {
            return new CastsImpl(getFactory(), getModel(), element);
        }
        return null;
    }

}
