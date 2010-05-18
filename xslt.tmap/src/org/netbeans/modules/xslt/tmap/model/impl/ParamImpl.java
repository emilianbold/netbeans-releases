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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ParamImpl extends NameableImpl implements Param {

    public ParamImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.PARAM, model));
    }

    public ParamImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public Class<Param> getComponentType() {
        return Param.class;
    }

    public ParamType getType() {
        return ParamType.parseParamType(getAttribute(TMapAttributes.TYPE));
    }

    public void setType(ParamType type) {
        setAttribute(Param.TYPE, TMapAttributes.TYPE,
                ParamType.INVALID.equals(type) ? null : type);
    }

    public String getValue() {
        return getAttribute(TMapAttributes.VALUE);
    }

    public void setValue(String value) {
        setAttribute(Param.VALUE, TMapAttributes.VALUE, value);
    }

    public void removeValue() {
        setAttribute(Param.VALUE, TMapAttributes.VALUE, null);
    }

    // TODO m
    public void setContent(String content) {
        setText(Param.CONTENT, content);
    }

    public void removeContent() {
        setText(Param.CONTENT, null);
    }

    // TODO m
    public String getContent() {
        return getCorrectedText();
    }

    public VariableReference getVariableReference() {
        return ParamType.PART.equals(getType()) ?
            getTMapVarReference(TMapAttributes.VALUE) : null;
    }

    public void setVariableReference(VariableReference varRef) {
        setTMapVarReference(TMapAttributes.VALUE, varRef);
        setType(ParamType.PART);
    }

    public Reference[] getReferences() {
        VariableReference varRef = getVariableReference();
        Reference[] refs = null;
        if (varRef != null) {
            refs = new Reference[] {varRef, varRef.getPart()};
        } else {
            refs = new Reference[0];
        }

        return refs;
    }

    public void setLiteralValue(String value) {
        setValue(value);
        setType(ParamType.LITERAL);
    }

    public String getLiteralValue() {
        return ParamType.LITERAL.equals(getType()) ? getValue(): null;
    }

}
