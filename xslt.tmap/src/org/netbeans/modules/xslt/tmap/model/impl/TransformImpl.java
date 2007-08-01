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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformerDescriptor;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformImpl extends TMapComponentAbstract 
    implements Transform 
{

    public TransformImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.TRANSFORM, model));
    }

    public TransformImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public Class<? extends TMapComponent> getComponentType() {
        return Transform.class;
    }

    public String getFile() {
        return getAttribute(TMapAttributes.FILE);
    }

    public void setFile(String locationURI) {
        setAttribute(Transform.FILE, TMapAttributes.FILE, locationURI);
    }

    public VariableReference getSource() {
        return getTMapVarReference(TMapAttributes.SOURCE);
    }

    public void setSource(String source) {
        setAttribute(Transform.SOURCE, TMapAttributes.SOURCE, source);
    }

    public VariableReference getResult() {
        return getTMapVarReference(TMapAttributes.RESULT);
    }

    public void setResult(String result) {
        setAttribute(Transform.RESULT, TMapAttributes.RESULT, result);
    }

    public List<Param> getParams() {
        return getChildren(Param.class);
    }

    public void addParam(Param param) {
        addAfter(TYPE.getTagName(), param, TYPE.getChildTypes());
    }

    public void removeParam(Param param) {
        removeChild(TYPE.getTagName(), param);
    }

    public Reference[] getReferences() {
        List<Reference> refs = new ArrayList<Reference>();
        VariableReference sourceRef = getSource();
        if (sourceRef != null) {
            refs.add(sourceRef);
            refs.add(sourceRef.getPart());
        }
        
        VariableReference resultRef = getResult();
        if (resultRef != null) {
            refs.add(resultRef);
            refs.add(resultRef.getPart());
        }
        
        return refs.toArray(new Reference[refs.size()]);
    }

}
