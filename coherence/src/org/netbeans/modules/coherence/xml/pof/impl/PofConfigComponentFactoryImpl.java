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
import org.netbeans.modules.coherence.xml.pof.PofConfigComponentFactory;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.SerializerType;
import org.netbeans.modules.coherence.xml.pof.TypeId;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;
import org.w3c.dom.Element;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class PofConfigComponentFactoryImpl implements PofConfigComponentFactory {

    private PofConfigModelImpl myModel;
    private ThreadLocal<PofConfigVisitorImpl> myBuilder;

    public PofConfigModelImpl getModel() {
        return myModel;
    }

    public ThreadLocal<PofConfigVisitorImpl> getBuilder() {
        return myBuilder;
    }

    PofConfigComponentFactoryImpl(PofConfigModelImpl model) {
        myModel = model;
        myBuilder = new ThreadLocal<PofConfigVisitorImpl>();
    }

    @Override
    public PofConfigComponent createComponent(Element element, PofConfigComponent context) {
        PofConfigVisitorImpl visitor = getVisitor();
        return visitor.create(context, element);
    }

    @Override
    public PofConfig createPofConfig() {
        return new PofConfigImpl(getModel());
    }

    @Override
    public UserTypeList createUserTypeList() {
        return new UserTypeListImpl(getModel());
    }

    @Override
    public UserType createUserType() {
        return new UserTypeImpl(getModel());
    }

    @Override
    public Include createInclude() {
        return new IncludeImpl(getModel());
    }

    @Override
    public TypeId createTypeId() {
        return new TypeIdImpl(getModel());
    }

    @Override
    public ClassName createClassName() {
        return new ClassNameImpl(getModel());
    }

    @Override
    public SerializerType createSerializerType() {
        return new  SerializerTypeImpl(getModel());
    }

    @Override
    public Serializer createSerializer() {
        return new SerializerImpl(getModel());
    }

    @Override
    public DefaultSerializer createDefaultSerializer() {
        return new DefaultSerializerImpl(getModel());
    }

    @Override
    public InitParams createInitParams() {
        return new InitParamsImpl(getModel());
    }

    @Override
    public InitParam createInitParam() {
        return new InitParamImpl(getModel());
    }

    @Override
    public ParamType createParamType() {
        return new ParamTypeImpl(getModel());
    }

    @Override
    public ParamValue createParamValue() {
        return new ParamValueImpl(getModel());
    }

    @Override
    public AllowInterfaces createAllowInterfaces() {
        return new AllowInterfacesImpl(getModel());
    }

    @Override
    public AllowSubclasses createAllowSubclasses() {
        return new AllowSubclassesImpl(getModel());
    }

    public PofConfigVisitorImpl getVisitor() {
        PofConfigVisitorImpl visitor = getBuilder().get();
        if (visitor == null) {
            visitor = new PofConfigVisitorImpl(getModel());
            getBuilder().set(visitor);
        }
        visitor.init();
        
        return visitor;
    }

}
