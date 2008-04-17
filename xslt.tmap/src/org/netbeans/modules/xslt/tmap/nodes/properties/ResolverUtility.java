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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.openide.util.NbBundle;

/**
 * The utility class containing auxiliary methods to work with WSDL
 *
 * ATTENTION! Many methods contains the lookup as patamether.
 * It is implied that it contains the BpelModel instance.
 *
 * @author Vitaly Bychkov
 * @author nk160297
 */
public final class ResolverUtility {
    
    /**
     * Calculate a String to display from a QName.
     */
    public static String qName2DisplayText(QName qValue) {
        return qName2DisplayText(qValue, null);
    }
    
    /**
     * Calculate a String to display from a QName.
     * The relativeTo parameter is used to calculate the prefix if it isn't specified.
     */
    public static String qName2DisplayText(QName qValue, TMapComponent relativeTo) {
        if (qValue == null) {
            return "";
        }
        //
        String prefix = qValue.getPrefix();
        String namespace = null;
        //
        if (prefix == null || prefix.length() == 0) {
            namespace = qValue.getNamespaceURI();
            if (relativeTo != null && namespace != null && namespace.length() != 0) {
                prefix = relativeTo.getNamespaceContext().getPrefix(namespace);
            }
        }
        //
        if (prefix == null || prefix.length() == 0) {
            if (namespace != null && namespace.length() != 0) {
                String retValue = qValue.getLocalPart() + "{" + namespace + "}"; // NOI18N
                return retValue;
            } else {
                prefix = "";
            }
        } else {
            prefix = prefix + ":"; // NOI18N
        }
        String retValue = prefix + qValue.getLocalPart();
        return retValue;
    }

    public static String getNameByRef(Reference ref)  {
        if (ref == null) {
            return null;
        }
        String result = null;
        //
        Object obj = null;
        try {
            obj = ref.get();
        } catch(IllegalStateException ex) {
            //This exception may happen if referenced object was removed from model
            //A kind of workaround required to work with cached references.
        }
        
        
        if (obj != null) {
            if (obj instanceof VariableReference) {
                result = ((VariableReference)obj).getReferencedVariable().getName();
            } else if (obj instanceof Named) {
                result = ((Named)obj).getName();
            }
        } else {
            result = ref.getRefString();
        }
        //
        return result;
    }
}
