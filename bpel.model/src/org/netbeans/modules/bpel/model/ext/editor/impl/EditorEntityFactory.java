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

import java.util.Arrays;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.impl.*;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class EditorEntityFactory implements EntityFactory {

    public EditorEntityFactory() {
    }

    public boolean isApplicable(String namespaceUri) {
        if (Editor.EDITOR_NAMESPACE_URI.equals(namespaceUri)) {
            return true;
        } else {
            return false;
        }
    }

    public Set<QName> getElementQNames() {
        return EditorElements.allQNames();
    }

    public BpelEntity create(BpelContainer container, Element element) {
        QName elementQName = new QName(
                element.getNamespaceURI(), element.getLocalName());
        if (EditorElements.EDITOR.getQName().equals(elementQName)) {
            return new EditorImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (EditorElements.CASTS.getQName().equals(elementQName)) {
            return new CastsImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (EditorElements.CAST.getQName().equals(elementQName)) {
            return new CastImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (EditorElements.PSEUDO_COMPS.getQName().equals(elementQName)) {
            return new PseudoCompsImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else if (EditorElements.PSEUDO_COMP.getQName().equals(elementQName)) {
            return new PseudoCompImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else {
            return null;
        }
    }

    public <T extends BpelEntity> T create(BpelBuilderImpl builder, Class<T> clazz) {
        T newEntity = null;
        if (Editor.class.equals(clazz)) {
            newEntity = (T)new EditorImpl(this, builder);
        } else if (Casts.class.equals(clazz)) {
            newEntity = (T)new CastsImpl(this, builder);
        } else if (Cast.class.equals(clazz)) {
            newEntity = (T)new CastImpl(this, builder);
        } else if (PseudoComps.class.equals(clazz)) {
            newEntity = (T)new PseudoCompsImpl(this, builder);
        } else if (PseudoComp.class.equals(clazz)) {
            newEntity = (T)new PseudoCompImpl(this, builder);
        }
        return newEntity;
    }

    public boolean canExtend(ExtensibleElements extensible, Class<? extends BpelEntity> extensionType) {
        return Editor.class.equals(extensionType) 
                && sSupportedParents.contains(extensible.getElementType());
    }

    private static Set<Class<? extends ExtensibleElements>> sSupportedParents =
            new HashSet<Class<? extends ExtensibleElements>>(Arrays.asList(
                    Variable.class,
                    If.class,
                    Copy.class,
                    ForEach.class,
                    RepeatUntil.class,
                    While.class,
                    Log.class,
                    Alert.class)
                    );
}
