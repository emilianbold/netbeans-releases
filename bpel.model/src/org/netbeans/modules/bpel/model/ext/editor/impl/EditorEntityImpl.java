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

import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelContainerImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class EditorEntityImpl extends BpelContainerImpl implements ExtensionEntity {

    private EditorEntityFactory mFactory;


    EditorEntityImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(model, e);
        mFactory = factory;
    }

    EditorEntityImpl(EditorEntityFactory factory, BpelBuilderImpl builder, EditorElements editorElements) {
        super(builder.getModel(),
                builder.getModel().getDocument().createElementNS(
                editorElements.getNamespace(), editorElements.getName()));
        mFactory = factory;
    }

    public void accept(BpelModelVisitor visitor) {
        visitor.visit(this);
    }

    public EditorEntityFactory getFactory() {
        return mFactory;
    }

    public boolean canExtend(ExtensibleElements extensible) {
        if (getFactory().canExtend(extensible, getElementType())) {
            return true;
        } else {
            return false;
        }
    }

}
