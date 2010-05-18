/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.rest.wadl.model.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.WadlVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Ayub Khan
 */
public class GenericExtensibilityElement extends WadlComponentBase implements ExtensibilityElement.ParentSelector {
    
    /** Creates a new instance of GenericExtensibilityElement */
    public GenericExtensibilityElement(WadlModel model, Element e) {
        super(model, e);
    }
    
    public GenericExtensibilityElement(WadlModel model, QName qname){
        this(model, createNewElement(qname, model));
    }
    
    public void accept(WadlVisitor visitor) {
        visitor.visit(this);
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        public Class getType() { return String.class; }
        public String getName() { return name; }
        public Class getMemberType() { return null; }
    }
    
    public String getAttribute(String attribute) {
        return getAttribute(new StringAttribute(attribute));
    }
    public void setAttribute(String attribute, String value) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_FRAGMENT_PROPERTY, text);
    }

    public void addAnyElement(ExtensibilityElement anyElement, int index) {
        List<WadlComponent> all = getChildren();
        if (index > all.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        insertAtIndex(EXTENSIBILITY_ELEMENT_PROPERTY, anyElement, index);
    }

    public void removeAnyElement(ExtensibilityElement any) {
//        super.removeExtensibilityElement(any);
    }

    public List<ExtensibilityElement> getAnyElements() {
        List<ExtensibilityElement> result = new ArrayList<ExtensibilityElement>();
//        List<ExtensibilityElement> allEEs = super.getExtensibilityElements();
//        for (ExtensibilityElement ee : allEEs) {
//            if (! ee.getModel().getQNames().contains(ee.getQName())) {
//                result.add(ee);
//            }
//        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Generic extensibility by default can be added to any Wadl component.
     */
    public boolean canBeAddedTo(Component target) {
        if (target instanceof WadlComponent) {
            return true;
        }
        return false;
    }
}
