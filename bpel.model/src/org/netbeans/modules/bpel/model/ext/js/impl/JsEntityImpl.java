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
package org.netbeans.modules.bpel.model.ext.js.impl;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.ext.js.xam.JsElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.ExtensibleElementsImpl;
import org.netbeans.modules.bpel.model.impl.events.BuildEvent;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 */
public abstract class JsEntityImpl  extends ExtensibleElementsImpl implements ExtensionEntity {

    private JsEntityFactory mFactory;


    JsEntityImpl(JsEntityFactory factory, BpelModelImpl model, Element e ) {
        super(model, e);
        mFactory = factory;
    }

    JsEntityImpl(JsEntityFactory factory, BpelBuilderImpl builder, JsElements loggingElements) {
        this(factory, builder.getModel(),
                builder.getModel().getDocument().createElementNS(
                loggingElements.getNamespace(), loggingElements.getName()));
        // below code is reqired to invoke generating UID, unique name e.c. services
        writeLock();
        try {
            BuildEvent<? extends BpelEntity> event = preCreated(this);
            postEvent(event);
        } finally {
            writeUnlock();
        }
    }

    public void accept(BpelModelVisitor visitor) {
        visitor.visit(this);
    }

    public JsEntityFactory getFactory() {
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
