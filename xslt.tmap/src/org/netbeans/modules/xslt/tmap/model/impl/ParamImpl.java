/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xslt.tmap.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.openide.ErrorManager;
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

    protected void setValue(String value) {
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

    public VariableReference getVariableReference() {
        return ParamType.PART.equals(getType()) ? 
            getTMapVarReference(TMapAttributes.VALUE) : null;
    }

    public void setVariableReference(VariableReference varRef) {
        setTMapVarReference(TMapAttributes.VALUE, varRef);
        setType(ParamType.PART);
    }

    public URI getUri() throws URISyntaxException {
        URI uri = null;
        if (ParamType.URI.equals(getType())) {
            uri = new URI(getValue());
        }
        return uri;
    }

    public void setUri(URI uri) {
        setValue(uri.toString());
        setType(ParamType.URI);
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
