/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.xml.pof.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.coherence.xml.pof.AllowInterfaces;
import org.netbeans.modules.coherence.xml.pof.AllowSubclasses;
import org.netbeans.modules.coherence.xml.pof.ClassName;
import org.netbeans.modules.coherence.xml.pof.DefaultSerializer;
import org.netbeans.modules.coherence.xml.pof.Include;
import org.netbeans.modules.coherence.xml.pof.InitParam;
import org.netbeans.modules.coherence.xml.pof.InitParams;
import org.netbeans.modules.coherence.xml.pof.ParamType;
import org.netbeans.modules.coherence.xml.pof.ParamValue;
import org.netbeans.modules.coherence.xml.pof.PofConfig;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponent;
import org.netbeans.modules.coherence.xml.pof.PofConfigVisitor;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.SerializerType;
import org.netbeans.modules.coherence.xml.pof.TypeId;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class PofConfigVisitorImpl implements PofConfigVisitor {

    private PofConfigComponent myResult;
    private Element myElement;
    private PofConfigModelImpl myModel;

    private PofConfigModelImpl getModel() {
        return myModel;
    }

    private Element getElement() {
        return myElement;
    }
    
    private String getLocalName() {
        return getElement().getLocalName();
    }

    public void setResult(PofConfigComponent myResult) {
        this.myResult = myResult;
    }
    
    private boolean isOk(PofConfigElements elements) {
        return elements.getName().equals(getLocalName());
    }
    
    public PofConfigVisitorImpl(PofConfigModelImpl model) {
        myModel = model;
    }

    public void init() {
        myResult = null;
        myElement = null;
    }

    PofConfigComponent create(PofConfigComponent context, Element element) {
        QName qName = AbstractDocumentComponent.getQName(element);
        if (!PofConfigComponent.NAMESPACE.equals(qName.getNamespaceURI())) {
            return null;
        }
        if (context == null) {
            return new PofConfigImpl(getModel(), element);
        } else {
            myElement = element;
            context.accept(this);
        }
        return myResult;
    }

    @Override
    public void visit(PofConfig pofConfig) {
        if (isOk(PofConfigElements.ALLOWINTERFACES)) setResult(new AllowInterfacesImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.ALLOWSUBCLASSES)) setResult(new AllowSubclassesImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.USERTYPELIST)) setResult(new UserTypeListImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.DEFAULTSERIALIZER)) setResult(new DefaultSerializerImpl(getModel(), getElement()));
    }

    @Override
    public void visit(AllowInterfaces allowInterfaces) {
    }

    @Override
    public void visit(AllowSubclasses allowSubclasses) {
    }

    @Override
    public void visit(ClassName className) {
    }

    @Override
    public void visit(DefaultSerializer defaultSerializer) {
        visit((SerializerType)defaultSerializer);
    }

    @Override
    public void visit(Include include) {
    }

    @Override
    public void visit(InitParam initParam) {
        if (isOk(PofConfigElements.PARAMTYPE)) setResult(new ParamTypeImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.PARAMVALUE)) setResult(new ParamValueImpl(getModel(), getElement()));
    }

    @Override
    public void visit(InitParams initParams) {
        if (isOk(PofConfigElements.INITPARAM)) setResult(new InitParamImpl(getModel(), getElement()));
    }

    @Override
    public void visit(ParamType paramType) {
    }

    @Override
    public void visit(ParamValue paramValue) {
    }

    @Override
    public void visit(Serializer serializer) {
        visit((SerializerType)serializer);
    }

    @Override
    public void visit(SerializerType serializerType) {
        if (isOk(PofConfigElements.CLASSNAME)) setResult(new ClassNameImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.INITPARAMS)) setResult(new InitParamsImpl(getModel(), getElement()));
    }

    @Override
    public void visit(TypeId typeId) {
    }

    @Override
    public void visit(UserType userType) {
        if (isOk(PofConfigElements.TYPEID)) setResult(new TypeIdImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.CLASSNAME)) setResult(new ClassNameImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.SERIALIZER)) setResult(new SerializerImpl(getModel(), getElement()));
    }

    @Override
    public void visit(UserTypeList userTypeList) {
        if (isOk(PofConfigElements.USERTYPE)) setResult(new UserTypeImpl(getModel(), getElement()));
        else if (isOk(PofConfigElements.INCLUDE)) setResult(new IncludeImpl(getModel(), getElement()));
    }

}
