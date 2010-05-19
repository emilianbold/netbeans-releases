/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.xpath;

import javax.xml.namespace.QName;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The special BPEL variable container is intended to be used by XPath model. 
 * 
 * @author nk160297
 */
public final class XPathWlmVariable implements XPathVariable {

    private VariableDeclaration myVarDecl;
    private Part myPart;

    /**
     * The part can be null.
     */ 
    public XPathWlmVariable(VariableDeclaration var, Part part) {
        assert var != null;
        myVarDecl = var;
        myPart = part;
    }
    
    public VariableDeclaration getVarDecl() {
        return myVarDecl;
    }
    
    public Part getPart() {
        return myPart;
    }

    public QName getName() {
        return constructXPathName();
    }
    
    public ReferenceableSchemaComponent getType() {
        //
        NamedComponentReference ref = null;
        Part part = getPart();
        if (part != null) {
            ref = part.getElement();
            if (ref == null) {
                ref = part.getType();
            }
        } else {
            VariableDeclaration varDecl = getVarDecl();
            ref = varDecl.getTypeRef();
        }
        //
        Referenceable result = ref.get();
        if (result instanceof ReferenceableSchemaComponent) {
            return ReferenceableSchemaComponent.class.cast(result);
        }
        //
        return null;
    }
    
    public QName constructXPathName() {
        String partName = null;
        if (myPart != null) {
            partName = myPart.getName();
        }
        //
        String totalVarName;
        if (partName != null && partName.length() != 0) {
            totalVarName = myVarDecl.getVariableName() + "." + partName;
        } else {
            totalVarName = myVarDecl.getVariableName();
        }
        //
        // It looks like a prefix is not required for BPEL variables
        return new QName(totalVarName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XPathWlmVariable)) {
            return false;
        }
        //
        XPathWlmVariable other = (XPathWlmVariable)obj;
        //
        if (!(myVarDecl.equals(other.myVarDecl))) {
            return false;
        }
        //
        if (myPart != null && other.myPart == null) {
            return false;
        }
        if (myPart == null && other.myPart != null) {
            return false;
        }
        if (myPart != null && other.myPart != null) {
            if (!(myPart.equals(other.myPart))) {
                return false;
            }
        }
        //
        return true;
    }
    
    @Override
    public String toString() {
        return getExpressionString();
    }

    public String getExpressionString() {
        String varName = myVarDecl.getVariableName();
        if (myPart == null) {
            return "$" + varName;
        } else {
            return "$" + varName + "." + myPart.getName();
        }
    }
}
