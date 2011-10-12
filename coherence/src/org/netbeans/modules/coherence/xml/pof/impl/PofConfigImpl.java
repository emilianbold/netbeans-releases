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
import org.netbeans.modules.coherence.xml.pof.AllowInterfaces;
import org.netbeans.modules.coherence.xml.pof.AllowSubclasses;
import org.netbeans.modules.coherence.xml.pof.DefaultSerializer;
import org.netbeans.modules.coherence.xml.pof.PofConfig;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponent;
import org.netbeans.modules.coherence.xml.pof.PofConfigVisitor;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;
import org.w3c.dom.Element;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class PofConfigImpl extends PofConfigComponentImpl implements PofConfig {

    PofConfigImpl(PofConfigModelImpl model, Element e) {
        super(model, e);
    }

    PofConfigImpl(PofConfigModelImpl model) {
        this(model, createNewElement(XML_TAG_NAME, model));
    }

    @Override
    public String getTagName() {
        return PofConfig.XML_TAG_NAME;
    }

    @Override
    public void accept(PofConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Class<? extends PofConfigComponent> getComponentType() {
        return PofConfig.class;
    }

    @Override
    public UserTypeList getUserTypeList() {
        List<UserTypeList> elements = getChildren(UserTypeList.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setUserTypeList(UserTypeList element) {
        UserTypeList child = getUserTypeList();
        if (child != null) {
            removeChild(UserTypeList.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(AllowInterfacesImpl.class);
            typeList.add(AllowSubclassesImpl.class);
            typeList.add(DefaultSerializerImpl.class);
            addBefore(UserTypeList.XML_TAG_NAME, element, typeList);
        }
    }

    @Override
    public AllowInterfaces getAllowInterfaces() {
        List<AllowInterfaces> elements = getChildren(AllowInterfaces.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setAllowInterfaces(AllowInterfaces element) {
        AllowInterfaces child = getAllowInterfaces();
        if (child != null) {
            removeChild(AllowInterfaces.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(UserTypeListImpl.class);
            addAfter(AllowInterfaces.XML_TAG_NAME, element, typeList);
        }
    }

    @Override
    public AllowSubclasses getAllowSubclasses() {
        List<AllowSubclasses> elements = getChildren(AllowSubclasses.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setAllowSubclasses(AllowSubclasses element) {
        AllowSubclasses child = getAllowSubclasses();
        if (child != null) {
            removeChild(AllowSubclasses.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(UserTypeListImpl.class);
            typeList.add(AllowInterfacesImpl.class);
            addAfter(AllowSubclasses.XML_TAG_NAME, element, typeList);
        }
    }

    @Override
    public DefaultSerializer getDefaultSerializer() {
        List<DefaultSerializer> elements = getChildren(DefaultSerializer.class);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public void setDefaultSerializer(DefaultSerializer element) {
        DefaultSerializer child = getDefaultSerializer();
        if (child != null) {
            removeChild(DefaultSerializer.XML_TAG_NAME, child);
        }
        if (element != null) {
            Collection typeList = new ArrayList();
            typeList.add(UserTypeListImpl.class);
            typeList.add(AllowInterfacesImpl.class);
            typeList.add(AllowSubclassesImpl.class);
            addAfter(DefaultSerializer.XML_TAG_NAME, element, typeList);
        }
    }
}
