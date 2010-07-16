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

import java.util.Arrays;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionAssignOperation;
import org.netbeans.modules.bpel.model.ext.js.api.Expression;
import org.netbeans.modules.bpel.model.ext.js.xam.JsElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 */
public class JsEntityFactory implements EntityFactory {

    public JsEntityFactory() {
    }

    public boolean isApplicable(String namespaceUri) {
        return Expression.JS_NAMESPACE_URI.equals(namespaceUri);
    }

    public Set<QName> getElementQNames() {
        return JsElements.allQNames();
    }

    public BpelEntity create(BpelContainer container, Element element) {
        return create(container, element, element.getNamespaceURI());
    }

    /**
     * Namespace context could be losted if elemnt is from cutted subtree
     *
     * @param container
     * @param element
     * @param namespaceURI
     * @return
     */
    public BpelEntity create(BpelContainer container, Element element, String namespaceURI) {
        QName elementQName = new QName(
                namespaceURI, element.getLocalName());
        if (JsElements.EXPRESSION.getQName().equals(elementQName)) {
            return new ExpressionImpl(this, (BpelModelImpl)container.getBpelModel(), element);
        } else {
            return null;
        }
    }

    public <T extends BpelEntity> T create(BpelBuilderImpl builder, Class<T> clazz) {
        T newEntity = null;
        if (Expression.class.equals(clazz)) {
            newEntity = (T)new ExpressionImpl(this, builder);
        }
        return newEntity;
    }

    public boolean canExtend(ExtensibleElements extensible, Class<? extends BpelEntity> extensionType) {
        if (Expression.class.equals(extensionType)) {
            if (sSupportedParents.contains(extensible.getElementType())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Set<Class<? extends ExtensibleElements>> sSupportedParents =
            new HashSet<Class<? extends ExtensibleElements>>(Arrays.asList(
                    ExtensionAssignOperation.class)
                    );
}