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

package org.netbeans.modules.wsdleditorapi.generator;

import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
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
