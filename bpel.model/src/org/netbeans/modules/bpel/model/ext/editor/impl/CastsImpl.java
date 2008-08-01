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
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CastsImpl extends EditorEntityImpl implements Casts {

    CastsImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    CastsImpl(EditorEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, EditorElements.CASTS);
    }

    @Override
    protected BpelEntity create( Element element ) {
        if ( EditorElements.CASTS.getName().equals(element.getLocalName())) {
            return new CastsImpl(getFactory(), getModel(), element);
        }
        return null;
    }

    public Class<? extends BpelEntity> getElementType() {
        return Casts.class;
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }

    public EntityUpdater getEntityUpdater() {
        return CastsEntityUpdater.getInstance();
    }

    private static class CastsEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new CastsEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private CastsEntityUpdater() {
        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof Editor) {
                Editor editor = (Editor)target;
                Casts casts = (Casts)child;
                switch (operation) {
                case ADD:
                    editor.setCasts(casts);
                    break;
                case REMOVE:
                    editor.removeCasts();
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            update(target, child, operation);
        }
    }

    public Cast[] getCasts() {
        readLock();
        try {
            List<Cast> list = getChildren( Cast.class );
            return list.toArray( new Cast[ list.size()] );
        }
        finally {
            readUnlock();
        }
    }

    public Cast getCast(int i) {
        return getChild( Cast.class , i );
    }

    public void removeCast(int i) {
        removeChild( Cast.class , i );
    }

    public void setCasts(Cast[] casts) {
        setArrayBefore( casts , Cast.class );
    }

    public void setCast(Cast cast, int i) {
        setChildAtIndex(cast, Cast.class, i);
    }

    public void addCast(Cast cast) {
        addChild(cast, Cast.class);
    }

    public void insertCast(Cast cast, int i) {
        insertAtIndex(cast, Cast.class, i );
    }
}
