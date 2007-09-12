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

package org.netbeans.modules.bpel.properties;

import javax.swing.text.Utilities;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.xml.xam.Reference;

/**
 * This class is intended to keep one of 3 possible types:
 * WSDL message, schema global element and schema global type.
 *
 * @author nk160297
 */
public class TypeContainer {

    Object myType;
    VariableStereotype myStereotype;
    String myRefString;

    public TypeContainer(Message message) {
        myType = message;
    }
   
    public TypeContainer(GlobalElement gElement) {
        myType = gElement;
    }
   
    public TypeContainer(GlobalType gType) {
        myType = gType;
    }
    
    public TypeContainer(Reference typeRef) {
        assert typeRef != null;
        myType = typeRef.get();
        myRefString = typeRef.getRefString();
    }
   
    public Object getType() {
        return myType;
    }
    
    public synchronized VariableStereotype getStereotype() {
        if (myStereotype == null) {
            myStereotype = VariableStereotype.recognizeStereotype(myType);
        }
        return myStereotype;
    }
    
    public Message getMessage() {
        if (myType instanceof Message) {
            return (Message)myType;
        }
        return null;
    }

    public GlobalElement getGlobalElement() {
        if (myType instanceof GlobalElement) {
            return (GlobalElement)myType;
        }
        return null;
    }

    public GlobalType getGlobalType() {
        if (myType instanceof GlobalType) {
            return (GlobalType)myType;
        }
        return null;
    }
    
    public String getTypeName() {
        if (myType instanceof Message) {
            return ((Message)myType).getName();
        } else if (myType instanceof GlobalElement) {
            return ((GlobalElement)myType).getName();
        } else if (myType instanceof GlobalType) {
            return ((GlobalType)myType).getName();
        } else {
            return null;
        }
    }
    
    public QName getTypeQName() {
        if (myType instanceof Message) {
            String targetNamespace = ((Message)myType).getModel().
                    getDefinitions().getTargetNamespace();
            String localPart = ((Message)myType).getName();
            return new QName(targetNamespace, localPart);
        } else if (myType instanceof GlobalElement) {
            String targetNamespace = ((GlobalElement)myType).getModel().
                    getSchema().getTargetNamespace();
            String localPart = ((GlobalElement)myType).getName();
            return new QName(targetNamespace, localPart);
        } else if (myType instanceof GlobalType) {
            String targetNamespace = ((GlobalType)myType).getModel().
                    getSchema().getTargetNamespace();
            String localPart = ((GlobalType)myType).getName();
            return new QName(targetNamespace, localPart);
        } else {
            return null;
        }
    }
    
    public String getRefString() {
        if (myRefString == null || myRefString.length() == 0) {
            return ResolverUtility.qName2DisplayText(getTypeQName());
        } else {
            return myRefString;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof TypeContainer) {
            TypeContainer otherType = (TypeContainer)obj;
            return this.myType.equals(otherType.getType());
        }
        return false;
    }
    
    
}
