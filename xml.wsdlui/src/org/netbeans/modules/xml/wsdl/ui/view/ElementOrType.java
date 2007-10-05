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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.openide.util.NbBundle;

public class ElementOrType {
    GlobalElement mElement;
    GlobalType mType;
    QName mQName;
    boolean isElement;
    Map<String, String> namespaceToPrefixMap;
    String namespacePrefix;
    String localPart;
    
    public ElementOrType(GlobalElement ele, Map<String, String> namespaceToPrefixMap) {
        mElement = ele;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
        namespacePrefix = generateNamespacePrefixIfNotPresent(mElement.getModel().getSchema().getTargetNamespace());
        localPart = mElement.getName();
    }
    


    public ElementOrType(GlobalType type, Map<String, String> namespaceToPrefixMap) {
        mType = type;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
        namespacePrefix = generateNamespacePrefixIfNotPresent(mType.getModel().getSchema().getTargetNamespace());
        localPart = mType.getName();
    }
    
        
    public boolean isElement() {
        return mElement != null;
    }
    
    public GlobalElement getElement() {
        return mElement;
    }
    
    public GlobalType getType() {
        return mType;
    }
    
    @Override
    public String toString() {
        if (namespacePrefix == null) 
            return localPart;
        return namespacePrefix + ":" + localPart;
    }
    
    
    public String generateNamespacePrefix(String optionalPrefixNameString) {
        String prefix = null;
        if(optionalPrefixNameString == null) {
            optionalPrefixNameString = NbBundle.getMessage(NameGenerator.class, "NameGenerator_DEFAULT_PREFIX");
        }
        
        int prefixCounter = 0;
        String prefixStr = optionalPrefixNameString;
        prefix = prefixStr;
        
        while(namespaceToPrefixMap.containsValue(prefix)) {
            prefix = prefixStr + prefixCounter++;
        }
        
        
        
        return prefix;
    }
    
    private String generateNamespacePrefixIfNotPresent(String namespace) {
        String nsPrefix = namespaceToPrefixMap.get(namespace);
        if (nsPrefix == null) {
            nsPrefix = generateNamespacePrefix(null);
            namespaceToPrefixMap.put(namespace, nsPrefix);
        }
        return nsPrefix;
    }
    
//    private String generateNamespacePrefixIfNotPresent(SchemaModel  sModel) {
//        String schemaTNS = sModel.getSchema().getTargetNamespace();
//        FileObject fo = (FileObject) sModel.getModelSource().getLookup().lookup(FileObject.class);
//        String prefixName = fo.getName();
//         if(prefixName.length() > 5) {
//            prefixName = prefixName.substring(0, 5);
//         }
//         String prefix = NameGenerator.getInstance().generateNamespacePrefix(prefixName, def);
//        ((AbstractDocumentComponent) def).addPrefix(prefix, schemaTNS);
//    }
}
