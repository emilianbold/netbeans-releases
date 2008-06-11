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
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.NbBundle;

/**
 * The implementation of the TreeItemInfoProvider for the schema related objects.
 * 
 * @author nk160297
 */
public class SchemaTreeInfoProvider implements TreeItemInfoProvider {

    public static final String ANY_ELEMENT = 
            NbBundle.getMessage(SchemaTreeInfoProvider.class, "ANY_ELEMENT"); // NOI18N
    
    public static final String ANY_ATTRIBUTE = 
            NbBundle.getMessage(SchemaTreeInfoProvider.class, "ANY_ATTRIBUTE"); // NOI18N
    
    
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
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof ElementReference) {
            NamedComponentReference<GlobalElement> elementRef = 
                    ((ElementReference)dataObject).getRef();
            QName qName = elementRef.getQName();
            return qName.getLocalPart();
        }
        //
        if (dataObject instanceof Named) {
            return ((Named)dataObject).getName();
        }
        //
        if (dataObject instanceof AnyElement) {
            return ANY_ELEMENT;
        }
        //
        if (dataObject instanceof AnyAttribute) {
            return ANY_ATTRIBUTE;
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof SchemaComponent) {
            if (dataObject instanceof Element) {
                Element element = (Element)dataObject;
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
            if (dataObject instanceof Attribute) {
                 Attribute attribute = (Attribute)dataObject;
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
            if (dataObject instanceof GlobalComplexType) {
                return SchemaIcons.COMPLEX_TYPE.getIcon();
            } 
            if (dataObject instanceof GlobalSimpleType) {
                return SchemaIcons.SIMPLE_TYPE.getIcon();
            } 
            //
            if (dataObject instanceof AnyElement) {
                AnyElement anyElement = (AnyElement)dataObject;
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
            if (dataObject instanceof AnyAttribute) {
                // The Any Attribute doesn't have multiplisity parameters
                return SchemaIcons.ATTRIBUTE.getIcon();
            }
        } 
        //
        if (dataObject instanceof SchemaModel || dataObject instanceof Schema) {
            return SchemaIcons.SCHEMA_FILE.getIcon();
        }
        //
        if (dataObject instanceof WSDLModel || dataObject instanceof Definitions) {
            return SchemaIcons.WSDL_FILE.getIcon();
        }
        //
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        String name = getDisplayName(treeItem);
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof SchemaComponent) {
            String nameSpase = ((SchemaComponent) dataObject).getModel().
                    getEffectiveNamespace((SchemaComponent) dataObject);
            //
            String type = null;

            if (dataObject instanceof GlobalElement) {
                if (((GlobalElement) dataObject).getType() != null) {
                    type = ((GlobalElement) dataObject).getType().getRefString();
                    return getColorTooltip(name, type, nameSpase);
                }
            }

            if (dataObject instanceof LocalElement) {
                if (((LocalElement) dataObject).getType() != null) {
                    return getColorTooltip(name, ((LocalElement) dataObject).
                            getType().getRefString(), nameSpase);
                }
            }

            if (dataObject instanceof LocalAttribute) {
                if (((LocalAttribute) dataObject).getType() != null) {
                    return getColorTooltip(name, ((LocalAttribute) dataObject).
                            getType().getRefString(), nameSpase);
                }
            }

            if (dataObject instanceof GlobalAttribute) {
                if (((GlobalAttribute) dataObject).getType() != null) {
                    return getColorTooltip(name, ((GlobalAttribute) dataObject).
                            getType().getRefString(), nameSpase);
                }
            }

            if (dataObject instanceof GlobalType) {
                return getColorTooltip(name, ((GlobalType) dataObject).getName(), nameSpase);
            }

            if (dataObject instanceof AnyElement || dataObject instanceof AnyAttribute) {
                return getColorTooltip(name, "ANY_TYPE", nameSpase);
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
    
    private String getColorTooltip(String name, String type, String nameSpace) {
        String result;
        result = "<html><body>";
        if (name != null) {
            result = result + name;
        }

        if (type != null) {
            result = result + "<b><font color=#7C0000>" + " " + type + "</font></b>";
        }
        if (nameSpace != null) {
            result = result + "<hr>" + "Namespace: " + nameSpace;
        }
        result = result + "</body>";

        return result;
    }
    
}
