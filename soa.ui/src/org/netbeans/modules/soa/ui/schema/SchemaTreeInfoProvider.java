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
package org.netbeans.modules.soa.ui.schema;

import javax.swing.Icon;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * The implementation of the TreeItemInfoProvider for the schema related objects.
 * 
 * @author nk160297
 */
public class SchemaTreeInfoProvider implements TreeItemInfoProvider {

    public static enum ToolTipTitles {
        ANY_ELEMENT, 
        ANY_ATTRIBUTE, 
        EMBEDED_SCHEMA,
        IMPORTED_SCHEMA, 
        INCLUDED_SCHEMA, 
        PRIMITIVE_TYPES, 
        GLOBAL_ELEMENT, 
        LOCAL_ELEMENT, 
        GLOBAL_ATTRIBUTE, 
        LOCAL_ATTRIBUTE, 
        GLOBAL_TYPE,
        ;
    
        public String getName() {
            String title = NbBundle.getMessage(
                    SchemaTreeInfoProvider.class, this.toString());
            return title;
        }
    
    }
    
    private static SchemaTreeInfoProvider singleton = new SchemaTreeInfoProvider();
    
    public static SchemaTreeInfoProvider getInstance() {
        return singleton;
    }

    public static GlobalType getGlobalType(SchemaComponent sComp) {
        if (sComp == null) {
            return null;
        }
        //
        GlobalType gType = null;
        //
        if (sComp instanceof GlobalType) {
            gType = (GlobalType)sComp;
        } else if (sComp instanceof TypeContainer) {
            TypeContainer typeContainer = (TypeContainer)sComp;
            NamedComponentReference<? extends GlobalType> typeRef = 
                    typeContainer.getType();
            if (typeRef != null) {
                gType = typeRef.get();
            }
        } else {
            if (sComp instanceof LocalAttribute) {
                NamedComponentReference<GlobalSimpleType> gTypeRef = 
                        ((LocalAttribute)sComp).getType();
                if (gTypeRef != null) {
                    gType = gTypeRef.get();
                }
            } else if (sComp instanceof GlobalAttribute) {
                NamedComponentReference<GlobalSimpleType> gTypeRef = 
                        ((GlobalAttribute)sComp).getType();
                if (gTypeRef != null) {
                    gType = gTypeRef.get();
                }
            }
        }
        //
        return gType;
    }
    
    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        if (dataObj instanceof Schema) {
            Schema schema = (Schema)dataObj;
            //
            Object parent = treeItem.getParent().getDataObject();
            if (parent instanceof WSDLModel) {
                return schema.getModel().getEffectiveNamespace(schema);
            } else {
                return getDisplayName(schema.getModel());
            }
        } 
        //
        return getDisplayByDataObj(dataObj);
    }
    
    public String getDisplayByDataObj(Object dataObj) {
        //
        if (dataObj instanceof Import) {
            try {
                SchemaModel sModel = ((Import)dataObj).resolveReferencedModel();
                if (sModel != null) {
                    return getDisplayName(sModel);
                }
            } catch (CatalogModelException ex) {
                // the import cannot be resolved 
            }
        } 
        //
        if (dataObj instanceof Include) {
            try {
                SchemaModel sModel = ((Include)dataObj).resolveReferencedModel();
                if (sModel != null) {
                    return getDisplayName(sModel);
                }
            } catch (CatalogModelException ex) {
                // the import cannot be resolved 
            }
        }
        //
        if (dataObj instanceof ElementReference) {
            NamedComponentReference<GlobalElement> elementRef = 
                    ((ElementReference)dataObj).getRef();
            QName qName = elementRef.getQName();
            return qName.getLocalPart();
        }
        //
        if (dataObj instanceof Named) {
            return ((Named)dataObj).getName();
        }
        //
        if (dataObj instanceof AnyElement) {
            return ToolTipTitles.ANY_ELEMENT.getName();
        }
        //
        if (dataObj instanceof AnyAttribute) {
            return ToolTipTitles.ANY_ATTRIBUTE.getName();
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getIconByDataObj(dataObj);
    }
    
    public Icon getIconByDataObj(Object dataObj) {
        if (dataObj instanceof SchemaComponent) {
            if (dataObj instanceof Element) {
                Element element = (Element)dataObj;
                boolean isOptional = false;
                boolean isRepeating = false;
                String maxOccoursStr = null;
                if (element instanceof GlobalElement) {
                    return SchemaIcons.ELEMENT.getIcon();
                } else if (element instanceof LocalElement) {
                    LocalElement lElement = (LocalElement)element;
                    isOptional = lElement.getMinOccursEffective() < 1;
                    //
                    maxOccoursStr = lElement.getMaxOccursEffective();
                } else if (element instanceof ElementReference) {
                    ElementReference elementRef = (ElementReference)element;
                    isOptional = elementRef.getMinOccursEffective() < 1;  
                    //
                    maxOccoursStr = elementRef.getMaxOccursEffective();
                }
                //
                if (maxOccoursStr != null) {
                    try {
                        int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                        isRepeating = maxOccoursInt > 1;  
                    } catch (NumberFormatException ex) {
                        // Do Nothing
                        isRepeating = true;
                    }
                }
                //
                if (isOptional) {
                    if (isRepeating) {
                        return SchemaIcons.ELEMENT_OPTIONAL_REPEATING.getIcon();
                    } else {
                        return SchemaIcons.ELEMENT_OPTIONAL.getIcon();
                    }
                } else {
                    if (isRepeating) {
                        return SchemaIcons.ELEMENT_REPEATING.getIcon();
                    } else {
                        return SchemaIcons.ELEMENT.getIcon();
                    }
                }
            } 
            //
            if (dataObj instanceof Attribute) {
                 Attribute attribute = (Attribute)dataObj;
                if (attribute instanceof LocalAttribute) {
                    Use use = ((LocalAttribute)attribute).getUseEffective();
                    if (use == Use.OPTIONAL) {
                        return SchemaIcons.ATTRIBUTE_OPTIONAL.getIcon();
                    } else {
                        return SchemaIcons.ATTRIBUTE.getIcon();
                    }
                } else {
                    return SchemaIcons.ATTRIBUTE.getIcon();
                }
            } 
            //
            if (dataObj instanceof GlobalComplexType) {
                return SchemaIcons.COMPLEX_TYPE.getIcon();
            } 
            if (dataObj instanceof GlobalSimpleType) {
                return SchemaIcons.SIMPLE_TYPE.getIcon();
            } 
            //
            if (dataObj instanceof AnyElement) {
                AnyElement anyElement = (AnyElement)dataObj;
                boolean isOptional = anyElement.getMinOccursEffective() < 1;
                String maxOccoursStr = anyElement.getMaxOccursEffective();
                //
                boolean isRepeating = false;
                //
                if (maxOccoursStr != null) {
                    try {
                        int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                        isRepeating = maxOccoursInt > 1;  
                    } catch (NumberFormatException ex) {
                        // Do Nothing
                        isRepeating = true;
                    }
                }
                //
                if (isOptional) {
                    if (isRepeating) {
                        return SchemaIcons.ELEMENT_OPTIONAL_REPEATING.getIcon();
                    } else {
                        return SchemaIcons.ELEMENT_OPTIONAL.getIcon();
                    }
                } else {
                    if (isRepeating) {
                        return SchemaIcons.ELEMENT_REPEATING.getIcon();
                    } else {
                        return SchemaIcons.ELEMENT.getIcon();
                    }
                }
            }
            //
            if (dataObj instanceof AnyAttribute) {
                // The Any Attribute doesn't have multiplisity parameters
                return SchemaIcons.ATTRIBUTE.getIcon();
            }
            //
            if (dataObj instanceof Import) {
                return SchemaIcons.SCHEMA_FILE.getIcon();
            }
            //
            if (dataObj instanceof Include) {
                return SchemaIcons.SCHEMA_FILE.getIcon();
            }    
            //
        } 
        //
        if (dataObj instanceof SchemaModel || dataObj instanceof Schema) {
            return SchemaIcons.SCHEMA_FILE.getIcon();
        }
        //
        if (dataObj instanceof WSDLModel || dataObj instanceof Definitions) {
            return SchemaIcons.WSDL_FILE.getIcon();
        }
        //
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        if (dataObj instanceof Schema) {
            Schema schema = (Schema)dataObj;
            //
            Object parent = treeItem.getParent().getDataObject();
            if (parent instanceof WSDLModel) {
                String ns = schema.getModel().getEffectiveNamespace(schema);
                return getColorTooltip(ToolTipTitles.EMBEDED_SCHEMA.getName(), 
                        null, null, ns);
            } else {
                return getDisplayName(schema.getModel());
            }
        }
        //
        String name = getDisplayName(treeItem);
        return getToolTipTextByDataObj(dataObj, name);
    }

    public String getToolTipTextByDataObj(Object dataObj, String name) {
        if (dataObj instanceof SchemaComponent) {
            String nameSpace = ((SchemaComponent) dataObj).getModel().
                    getEffectiveNamespace((SchemaComponent) dataObj);
            //
            if (dataObj instanceof GlobalElement) {
                if (((GlobalElement) dataObj).getType() != null) {
                    return getColorTooltip(
                            ToolTipTitles.GLOBAL_ELEMENT.getName(), 
                            name, 
                            ((GlobalElement) dataObj).getType().getRefString(), 
                            nameSpace);
                }
            }

            if (dataObj instanceof LocalElement) {
                if (((LocalElement) dataObj).getType() != null) {
                    return getColorTooltip(ToolTipTitles.LOCAL_ELEMENT.getName(), 
                            name, 
                            ((LocalElement) dataObj).getType().getRefString(), 
                            nameSpace);
                }
            }

            if (dataObj instanceof LocalAttribute) {
                if (((LocalAttribute) dataObj).getType() != null) {
                    return getColorTooltip(ToolTipTitles.LOCAL_ATTRIBUTE.getName(), 
                            name, 
                            ((LocalAttribute) dataObj).getType().getRefString(), 
                            nameSpace);
                }
            }

            if (dataObj instanceof GlobalAttribute) {
                if (((GlobalAttribute) dataObj).getType() != null) {
                    return getColorTooltip(ToolTipTitles.GLOBAL_ATTRIBUTE.getName(), 
                            name, 
                            ((GlobalAttribute) dataObj).getType().getRefString(), 
                            nameSpace);
                }
            }

            if (dataObj instanceof GlobalType) {
                return getColorTooltip(ToolTipTitles.GLOBAL_TYPE.getName(), name, 
                        ((GlobalType) dataObj).getName(), nameSpace);
            }

            if (dataObj instanceof AnyElement || dataObj instanceof AnyAttribute) {
                return getColorTooltip(name, null, null, null);
            }
            //
            if (dataObj instanceof Import) {
                try {
                    SchemaModel sModel = ((Import)dataObj).resolveReferencedModel();
                    if (sModel != null) {
                        String ns = sModel.getEffectiveNamespace(sModel.getSchema());
                        return getColorTooltip(ToolTipTitles.IMPORTED_SCHEMA.getName(), 
                                name, null, ns);
                    }
                } catch (CatalogModelException ex) {
                    // the import cannot be resolved 
                }
            } 
            //
            if (dataObj instanceof Include) {
                try {
                    SchemaModel sModel = ((Include)dataObj).resolveReferencedModel();
                    if (sModel != null) {
                        String ns = sModel.getEffectiveNamespace(sModel.getSchema());
                        return getColorTooltip(ToolTipTitles.INCLUDED_SCHEMA.getName(), 
                                name, null, ns);
                    }
                } catch (CatalogModelException ex) {
                    // the import cannot be resolved 
                }
            }
            
            //
            String notNamedTypeLbl = NbBundle.getMessage(
                    SchemaTreeInfoProvider.class, "NOT_NAMED_TYPE"); // NOI18N

            return new String("<html><body>" + name +
                    "<b><font color=#7C0000>" + " " + notNamedTypeLbl +
                    "</font></b> <hr> Localy define type, this type does not have name" +
                    "</body>");
        // } else if (dataObject instanceof SchemaModel) {
        }
        //
        return null;
    }
    
    public static Project safeGetProject(Model model) {
        FileObject fo = SoaUtil.getFileObjectByModel(model);
        if (fo != null && fo.isValid()) {
            return FileOwnerQuery.getOwner(fo);
        } else {
            return null;
        }
    }

    public static String getDisplayName(SchemaModel schemaModel) {
        Project ownerProject = safeGetProject(schemaModel);
        if (ownerProject == null) { 
            return schemaModel.getEffectiveNamespace(schemaModel.getSchema());
            // TODO: it can be null
        } else {
            FileObject schemaFo = SoaUtil.getFileObjectByModel(schemaModel);
            //
            String fileExt = schemaFo.getExt();
            if (fileExt.equalsIgnoreCase("xsd")) {
                FileObject projectDir = ownerProject.getProjectDirectory();
                return FileUtil.getRelativePath(projectDir, schemaFo);
            } else {
                // The file doesn't have XSD extension. So it might be not 
                // a Schema file but rather WSDL file and the schema is embedded. 
                // There is a issue 137943 about lack of possibility 
                // to check the embedding state.
                //
                return schemaModel.getEffectiveNamespace(schemaModel.getSchema());
                // TODO: it can be null
            }
        }
    }
    
    public static String getColorTooltip(
            String title, String name, String type, String nameSpace) {
        //
        StringBuilder result = new StringBuilder();
        //
        if (title != null) {
            result.append("<p align=\"center\"><b>" + title + "</b></p>");
        }
        //
        if (name != null && type != null) {
            if (result.length() != 0) {
                result.append("<hr>");
            }
            //
            if (name != null) {
                result.append(name);
            }
            //
            if (type != null) {
                result.append("<b><font color=#7C0000>" + " " + type + "</font></b>");
            }
        }
        //
        if (nameSpace != null) {
            if (result.length() != 0) {
                result.append("<hr>");
            }
            //
            result.append("Namespace: " + nameSpace);
        }
        //
        return "<html><body>" + result.toString() + "</body>";
    }
    
}
