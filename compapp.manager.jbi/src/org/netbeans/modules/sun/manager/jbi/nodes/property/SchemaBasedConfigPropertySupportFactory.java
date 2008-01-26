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
package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.xml.namespace.QName;
import org.netbeans.modules.sun.manager.jbi.editors.ComboBoxPropertyEditor;
import org.netbeans.modules.sun.manager.jbi.editors.PasswordEditor;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.netbeans.modules.sun.manager.jbi.management.ConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtNode;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Extension;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.MinInclusive;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class to provide schema-based property support for JBI Component
 * configuration.
 * 
 * The schema that is currently supported is nothing fancy. There is no 
 * include/import, no entity, etc.
 * 
 * @author jqian
 */
public class SchemaBasedConfigPropertySupportFactory {
        
    private static MyCatalogModel catalogModel = new MyCatalogModel();
         
    public static PropertySupport getPropertySupport(
            String schemaText, 
            String compName,
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr, 
            final MBeanAttributeInfo info) {          
        Schema schema = getSchema(schemaText, compName);         
        return getPropertySupport(schema, componentNode, attr, info);
    }
    
    /*private*/ static PropertySupport getPropertySupport(
            Schema schema, 
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        String elementName = attr.getName();      
        String simpleTypeName = getGlobalSimpleTypeName(schema, elementName);
        //System.out.println("elementName: " + elementName + " simpleTypeName: " + simpleTypeName);
        
        if (simpleTypeName == null) {
            return null;
        } else if (simpleTypeName.endsWith("boolean")) { // NOI18N  // ignore ns
            return getBooleanPropertySupport(componentNode, attr, info);
        } else if (simpleTypeName.endsWith("int")) { // NOI18N
            return getIntegerPropertySupport(componentNode, attr, info, null);
        } else if (simpleTypeName.endsWith("string")) { // NOI18N  
            return getStringPropertySupport(componentNode, attr, info, null);
        }
        
        for (GlobalSimpleType gst : schema.getSimpleTypes()) {
            if (simpleTypeName.endsWith(gst.getName())) { 
                SimpleTypeRestriction restriction = 
                        gst.getChildren(SimpleTypeRestriction.class).get(0);
                GlobalSimpleType baseSimpleType = restriction.getBase().get();
                String baseSimpleTypeName = baseSimpleType.getName();
                
                if (baseSimpleTypeName.equals("int")) { // NOI18N                    
                    return getIntegerPropertySupport(
                            componentNode, attr, info, restriction);                    
                } else if (baseSimpleTypeName.equals("string")) { // NOI18N  
                     return getStringPropertySupport(
                            componentNode, attr, info, restriction);
                } else {
                    throw new RuntimeException("TODO: Unsupported schema base type: " + // NOI18N
                            baseSimpleTypeName + ". Pleae file a IssueZilla ticket.");
                }
            } 
        }
        
        return null;
    }
    
    private static PropertySupport getBooleanPropertySupport(
            AppserverJBIMgmtNode componentNode,
            Attribute attr,
            MBeanAttributeInfo info) {        
        
        return new SchemaBasedConfigPropertySupport<Boolean>(
                componentNode, Boolean.class, attr, info);
    }
          
    private static PropertySupport getStringPropertySupport(
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr, 
            final MBeanAttributeInfo info,
            final SimpleTypeRestriction restriction) {
        
        if (restriction == null)  {
            return getSimpleStringPropertySupport(componentNode, attr, info);
        } else {
            Collection enumerations = restriction.getEnumerations();
            if (enumerations.size() == 0) {
                return getSimpleStringPropertySupport(componentNode, attr, info);
            } else {
                final String[] validValues = new String[enumerations.size()];
                int i = 0;
                for (Enumeration enumeration : restriction.getEnumerations()) {
                    validValues[i++] = enumeration.getValue();
                }
                return getEnumeratedStringPropertySupport(
                        componentNode, attr, info, validValues);
            }
        }
    }
    
    private static PropertySupport getSimpleStringPropertySupport(
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        return new SchemaBasedConfigPropertySupport<String>(
                componentNode, String.class, attr, info) {
            @Override
            public PropertyEditor getPropertyEditor() {
                if (info instanceof ConfigurationMBeanAttributeInfo && 
                        ((ConfigurationMBeanAttributeInfo)info).isPassword()) {
                    return new PasswordEditor();
                } else {
                    return super.getPropertyEditor();
                }
            }
        };
    }
    
    public static PropertySupport getEnumeratedStringPropertySupport(
            final AppserverJBIMgmtNode componentNode, 
            final Attribute attr,
            final MBeanAttributeInfo info, 
            final String[] validValues) {
        
        return new SchemaBasedConfigPropertySupport<String>(
                componentNode, String.class, attr, info) {            
            @Override
            public PropertyEditor getPropertyEditor() {
                return new ComboBoxPropertyEditor(validValues);
            }
        };
    }      
    
    public static PropertySupport getIntegerPropertySupport(
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr,
            final MBeanAttributeInfo info, 
            final int minInclusiveValue, 
            final int maxInclusiveValue) {
        
        return new SchemaBasedConfigPropertySupport<Integer>(
                componentNode, Integer.class, attr, info) {            
                        
            @Override
            public Integer getValue() {
                // friendly reminder for now
                if (attr.getValue() instanceof String) {
                    String msg = "The component's configuration schema indicates this attribute is of type 'int'." + // NOI18N
                            " However, the MBean attribute is of type 'string'. Please fix the component."; // NOI18N
                    throw new ClassCastException(msg);
                }
                return super.getValue();
            }
            
            @Override
            protected boolean validate(Integer val) {
                int value = Integer.parseInt(val.toString());
                if (value < minInclusiveValue || value > maxInclusiveValue) {
                    String errMsg = NbBundle.getMessage(getClass(), 
                            "MSG_INVALID_INTEGER", value, // NOI18N
                            minInclusiveValue, maxInclusiveValue);
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            errMsg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                    return false;
                }
                
                return true;
            }
        };
    }
    
    private static PropertySupport getIntegerPropertySupport(
            final AppserverJBIMgmtNode componentNode,
            final Attribute attr, 
            final MBeanAttributeInfo info,
            final SimpleTypeRestriction restriction) {
        
        int minInclusiveValue = Integer.MIN_VALUE;
        int maxInclusiveValue = Integer.MAX_VALUE;
        
        if (restriction != null) {
            Collection<MinInclusive> minInclusives = restriction.getMinInclusives();
            if (minInclusives.size() == 1) {
                MinInclusive minInclusive = minInclusives.iterator().next();
                minInclusiveValue = Integer.parseInt(minInclusive.getValue());
            }

            Collection<MinExclusive> minExclusives = restriction.getMinExclusives();
            if (minExclusives.size() == 1) {
                MinExclusive minExclusive = minExclusives.iterator().next();
                minInclusiveValue = Integer.parseInt(minExclusive.getValue()) + 1;
            }

            Collection<MaxInclusive> maxInclusives = restriction.getMaxInclusives();
            if (maxInclusives.size() == 1) {
                MaxInclusive maxInclusive = maxInclusives.iterator().next();
                maxInclusiveValue = Integer.parseInt(maxInclusive.getValue());
            }

            Collection<MaxExclusive> maxExclusives = restriction.getMaxExclusives();
            if (maxExclusives.size() == 1) {
                MaxExclusive maxExclusive = maxExclusives.iterator().next();
                maxInclusiveValue = Integer.parseInt(maxExclusive.getValue()) - 1;
            }
        }
        
        return getIntegerPropertySupport(
                componentNode, attr, info, minInclusiveValue, maxInclusiveValue);
    }
        
    
    static Schema getSchema(String schemaText, String compName) {
        SchemaModelFactory factory = SchemaModelFactory.getDefault();
        ModelSource modelSource = loadModel(schemaText, compName, compName, false);
        SchemaModel schemaModel = factory.getModel(modelSource);
        Schema schema = schemaModel.getSchema(); 
        return schema;
    }    
  
    static String getGlobalSimpleTypeName(Schema schema, String elementName) {
        
        String type = null;
        for (GlobalComplexType gct : schema.getComplexTypes()) {
            if (gct.getName().equals("ConfigurationType")) { // NOI18N  
                ComplexTypeDefinition ctd = gct.getDefinition();
                    for (Element element : ctd.getChildren(Element.class)) {
                        String name = element.getAnyAttribute(new QName("name")); // NOI18N  
                        if (name.equals(elementName)) {
                            type = element.getAnyAttribute(new QName("type"));  // NOI18N  
                            break;
                        }
                    }   
                if (type != null) {
                    break;
                }
            }
        }
        
        if (type == null) {
            return null;
        }
        
        String base = null;        
        for (GlobalComplexType gct : schema.getComplexTypes()) {
            if (type.endsWith(gct.getName())) { // ignore ns
                List<SimpleContent> scList = gct.getChildren(SimpleContent.class);
                if (scList.size() > 0) { // ==1 ?
                    SimpleContent sc = scList.get(0);
                    Extension ext = sc.getChildren(Extension.class).get(0);
                    base = ext.getAnyAttribute(new QName("base")); // NOI18N  
                    break;
                }
                
//                List<Sequence> seqList = gct.getChildren(Sequence.class);
//                if (seqList.size() > 0) { // ==1 ?
//                    Sequence seq = seqList.get(0);
//                    seq.getChildren();
//                    break;
//                }
            }
        }      
        
        return base;
    }
    
    private static ModelSource loadModel(String schemaText, 
            String baseURI, String key, boolean editable) {        
        ModelSource model = null;        
        try {
            Document document = new PlainDocument();
            document.insertString(0, schemaText, null);
            
            File fakeFile = new File(key);
                 
            Lookup lookup = Lookups.fixed(new Object[]{
                document, catalogModel, fakeFile,
                ReadOnlyAccess.Provider.getInstance()});
            
            model = new ModelSource(lookup, editable);
            // System.out.println("Model created: "+baseURI+", "+model);
            
            catalogModel.addModelSource(new URI(baseURI), model);
        } catch(Exception ex) {
            System.out.println(ex);
            model = null;
        }
        
        return model;        
    }
    
    private static class MyCatalogModel implements CatalogModel {
        
        private HashMap<URI, ModelSource> msMap = new HashMap<URI, ModelSource>();
        
        MyCatalogModel() {
            super();
        }
        
        public void addModelSource(URI loc, ModelSource ms) {
            msMap.put(loc, ms);
        }
                
        public ModelSource getModelSource(URI locationURI) 
                throws CatalogModelException {
            return msMap.get(locationURI);
        }
        
        public ModelSource getModelSource(URI locationURI, 
                ModelSource modelSourceOfSourceDocument) 
                throws CatalogModelException {
            return getModelSource(locationURI); 
        }
        
        public InputSource resolveEntity(String publicId, String systemId) 
                throws SAXException, IOException {
            return null;
        }
        
        public LSInput resolveResource(String type, String namespaceURI, 
                String publicId, String systemId, String baseURI) {
            return null;
        }  
    }    
    
}
