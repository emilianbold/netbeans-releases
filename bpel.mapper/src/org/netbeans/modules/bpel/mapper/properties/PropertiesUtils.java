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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class PropertiesUtils {
    public static String getNSPreffix(CorrelationProperty property, 
            BpelDesignContext designContext)
    {
        return getNSPreffix(property, designContext.getSelectedEntity());
    }
    
    public static String getNSPreffix(CorrelationProperty property, 
            BpelEntity contextBpelEntity)
    {
        WSDLModel wsdlModel = property.getModel();
        if (wsdlModel == null) {
            return null;
        }
        
        Definitions definitions = wsdlModel.getDefinitions();
        if (definitions == null) {
            return null;
        }
        
        String targetNamespace = definitions.getTargetNamespace();
        if (targetNamespace == null) {
            return null;
        }

        ExNamespaceContext namespaceContext = contextBpelEntity
                .getNamespaceContext();

        //Iterator<String> preffixesIrerator = namespaceContext.getPrefixes();
        
        String nsPreffix = null;
        
        try {
            nsPreffix = namespaceContext.addNamespace(targetNamespace);
        } catch (InvalidNamespaceException ex) {}
        
//        if (preffixesIrerator == null || !preffixesIrerator.hasNext()) {
//            try {
//                namespaceContext.addNamespace(targetNamespace);
//                preffixesIrerator = namespaceContext.getPrefixes();
//                if (preffixesIrerator != null && preffixesIrerator.hasNext()) {
//                    nsPreffix = preffixesIrerator.next();
//                }
//            } catch (InvalidNamespaceException ex) {}
//        } else {
//            nsPreffix = preffixesIrerator.next();
//        }
        
        return nsPreffix;
    }
    
    public static String getQName(CorrelationProperty property, 
            BpelDesignContext designContext)
    {
        return getQName(property, designContext.getSelectedEntity());
    }
    
    public static String getQName(CorrelationProperty property, 
            BpelEntity contextBpelEntity) 
    {
        String nsPreffix = getNSPreffix(property, contextBpelEntity);
        
        boolean preffixOk = (nsPreffix != null) 
                && (nsPreffix.trim().length() > 0);

        String propertyName = property.getName();
        
        return (preffixOk) 
                ? (nsPreffix + ":" + propertyName) 
                : propertyName; // NOI18N
    }

    public static boolean isEqual(String propertyQName, 
            CorrelationProperty property, BpelDesignContext designContext)
    {
        return isEqual(propertyQName, property, designContext
                .getSelectedEntity());
    }
    
    public static boolean isEqual(String propertyQName, 
            CorrelationProperty property, BpelEntity contextBpelEntity)
    {
        if (propertyQName == null) {
            return false;
        }
        
        String propertyName = property.getName();
        if (propertyName == null) {
            return false;
        }
        
        if (!propertyQName.endsWith(":" + propertyName)) {
            return false;
        }
        
        String qName = getQName(property, contextBpelEntity);
        
        return (qName != null) && qName.equals(propertyQName);
    }
    
    public static List<Object> loadChildren(FileObject fileObject) {
        FileObject[] children = fileObject.getChildren();
        List<Object> result = null;
        
        if (children != null && children.length > 0) {
            result = new ArrayList<Object>(children.length);
            for (FileObject child : children) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    public static String getDisplayName(FileObject fileObject) {
        return (String) fileObject.getAttribute("display-name");
    }
    
    public static boolean isNMProperty(Object o) {
        return (o instanceof FileObject) && !((FileObject) o).isFolder();
    }
    
    public static String getToolTip(FileObject fileObject) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>"); // NOI18N;
        
        String title = NbBundle.getMessage(PropertiesUtils.class, 
                (fileObject.isFolder()) 
                        ? "NM_PROPERTIES_GROUP" // NOI18N
                        : "NM_PROPERTY"); // NOI18N
        
        builder.append("<p align=center><b>"); // NOI18N
        builder.append(title); // NOI18N
        builder.append("</b></p>"); // NOI18N
        
        Object toolTipAttr = fileObject
                .getAttribute(PropertiesConstants.TOOLTIP_ATTR);
        Object displayNameAttr = fileObject
                .getAttribute(PropertiesConstants.DISPLAY_NAME_ATTR);
        
        String toolTip = null;
        if (toolTipAttr instanceof String) {
            toolTip = (String) toolTipAttr;
        } else if (displayNameAttr instanceof String) {
            toolTip = (String) displayNameAttr;
        }
        
        String nmProperty = null;
        if (!fileObject.isFolder()) {
            Object nmPropertyAttr = fileObject.getAttribute(PropertiesConstants
                    .NM_PROPERTY_ATTR);
            if (nmPropertyAttr instanceof String) {
                nmProperty = (String) nmPropertyAttr;
            }
        }
        
        if (toolTip != null || nmProperty != null) {
            builder.append("<hr>"); // NOI18N
            if (toolTip != null) {
                builder.append(toolTip);
            }
            
            if (nmProperty != null) {
                if (toolTip != null) {
                    builder.append("<hr>"); // NOI18N
                }
                builder.append(nmProperty);
            }
        }
        
        builder.append("</body></html>"); // NOI18N;
        return builder.toString();
    }
    
    public static String getToolTip(NMProperty nmProperty) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>"); // NOI18N;
        
        String title = NbBundle.getMessage(PropertiesUtils.class, 
                "NM_PROPERTY"); // NOI18N
        
        builder.append("<p align=center><b>"); // NOI18N
        builder.append(title); // NOI18N
        builder.append("</b></p>"); // NOI18N
        
        String displayName = nmProperty.getDisplayName();
        String nmPropertyName = nmProperty.getNMProperty();
        
        if (displayName != null) {
            displayName = displayName.trim();
            if (displayName.length() == 0) {
                displayName = null;
            }
        }
        
        if (nmPropertyName != null) {
            nmPropertyName = nmPropertyName.trim();
            if (nmPropertyName.length() == 0) {
                nmPropertyName = null;
            }
        }
        
        if (displayName != null || nmPropertyName != null) {
            builder.append("<hr>"); // NOI18N
            if (displayName != null) {
                builder.append(displayName);
            }
            
            if (nmPropertyName != null) {
                if (displayName != null) {
                    builder.append("<hr>"); // NOI18N
                }
                
                builder.append(nmPropertyName);
            }
        }
        
        builder.append("</body></html>"); // NOI18N;
        return builder.toString();
    }
    
    public static String getToolTip(CorrelationProperty correlationProperty) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>"); // NOI18N;
        
        String title = NbBundle.getMessage(PropertiesUtils.class, 
                "CORRELATION_PROPERTY"); // NOI18N
        
        builder.append("<p align=center><b>"); // NOI18N
        builder.append(title); // NOI18N
        builder.append("</b></p>"); // NOI18N
        
        builder.append("</body></html>"); // NOI18N;
        return builder.toString();
    }
}
