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

import java.util.List;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformerDescriptor;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ParamImpl extends TMapComponentAbstract 
    implements Param 
{

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

    public String getName() {
        return getAttribute(TMapAttributes.NAME);
    }

    public void setName(String name) {
        setAttribute(Param.NAME, TMapAttributes.NAME, name);
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

    // TODO m
    public void setContent(String content) {
        setText(Param.CONTENT, content);
    }

    // TODO m
    public String getContent() {
        return getText();
    }

}
