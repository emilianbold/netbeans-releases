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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.coherence.xml.pof.ClassName;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponent;
import org.netbeans.modules.coherence.xml.pof.PofConfigVisitor;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.TypeId;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.w3c.dom.Element;

/**
 *
 */
public class UserTypeImpl extends PofConfigComponentImpl implements UserType {

    public UserTypeImpl(PofConfigModelImpl model, Element e) {
        super(model, e);
    }

    public UserTypeImpl(PofConfigModelImpl model) {
        super(model, createNewElement(XML_TAG_NAME, model));
    }

    @Override
    public String getTagName() {
        return UserType.XML_TAG_NAME;
    }

    @Override
    public void accept(PofConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Class<? extends PofConfigComponent> getComponentType() {
        return UserType.class;
    }

    @Override
    public TypeId getTypeId() {
        List<TypeId> elements = getChildren(TypeId.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setTypeId(TypeId element) {
        TypeId child = getTypeId();
        if (child != null) {
            removeChild(TypeId.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(ClassNameImpl.class);
            typeList.add(SerializerImpl.class);
            addBefore(TypeId.XML_TAG_NAME, element, typeList);
        }
    }

    @Override
    public ClassName getClassName() {
        List<ClassName> elements = getChildren(ClassName.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setClassName(ClassName element) {
        ClassName child = getClassName();
        if (child != null) {
            removeChild(ClassName.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(TypeIdImpl.class);
            addAfter(ClassName.XML_TAG_NAME, element, typeList);
        }
    }

    @Override
    public Serializer getSerializer() {
        List<Serializer> elements = getChildren(Serializer.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setSerializer(Serializer element) {
        Serializer child = getSerializer();
        if (child != null) {
            removeChild(Serializer.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(TypeIdImpl.class);
            typeList.add(ClassNameImpl.class);
            addAfter(Serializer.XML_TAG_NAME, element, typeList);
        }
    }
}
