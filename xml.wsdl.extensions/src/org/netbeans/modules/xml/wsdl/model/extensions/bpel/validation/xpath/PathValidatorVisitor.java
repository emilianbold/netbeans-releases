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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathModelTracerVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 * This visitor is intended to validate semantics of single XPath.
 * It can check reference integrity of variables, parts and types.
 *
 * @author nk160297
 */
public class PathValidatorVisitor extends XPathModelTracerVisitor {
    
    private PathValidationContext myContext;
    
    public PathValidatorVisitor(PathValidationContext context) {
        myContext = context;
    }
    
    //========================================================
    
    @Override
    public void visit(XPathLocationPath locationPath) {
        //
        if (locationPath.getAbsolute()) {
            //
            // Process the first step of an absolute location path.
            //
            SchemaComponent rootComp = myContext.getSchemaContextComponent();
            //
            if (rootComp instanceof GlobalType) {
                // issue #90323
                // Error. The location path must not be absolute if the global type is used.
                myContext.addResultItem(ResultType.ERROR, "ABSOLUTE_XPATH_WITH_TYPE"); // NOI18N
            } else {
                assert rootComp instanceof GlobalElement : 
                    "Root component type is " + rootComp.getComponentType().getName() + 
                    " but it has to be a GlobalElement"; // NOI18N
                //
                // Check if the root element equals to the element of the first step
                checkFirstStepType(locationPath, (GlobalElement)rootComp);
            }
        }
        //
        // The following check is intended specially for Property Aliases.
        // It check if the type of the last element of the Query is the same as
        // the type of the correlation property.
        GlobalType propType = null;
        //
        WSDLComponent comp = myContext.getWsdlContext();

        if (comp instanceof PropertyAlias) {
            PropertyAlias pa = (PropertyAlias)comp;
            propType = getPropertyType(pa);
        }
        //
        if (propType != null) {
            //
            // Take the schema component which corresponds to the last step.
            Set<GlobalType> lastStepTypes = getLastStepSchemaTypeSet(locationPath);
            if (lastStepTypes == null || lastStepTypes.isEmpty()) {
                return;
            } 
            if (lastStepTypes.size() == 1) {
                GlobalType gType = lastStepTypes.iterator().next();
                // Check if the type of the last element of the XPath
//System.out.println();
//System.out.println("1: " + getBasedSimpleType(propType));
//System.out.println("2: " + getBasedSimpleType(gType));
                if (XAMUtils.getBasedSimpleType(propType) != XAMUtils.getBasedSimpleType(gType)) {
                    myContext.addResultItem(ResultType.WARNING, "QUERY_INCONSISTENT_TYPE", 
                        XAMUtils.getTypeName(gType), XAMUtils.getTypeName(propType)); // NOI18N
                }
                else {
                // # 83335
//System.out.println();
//System.out.println("TYPE IS: " + gType.getClass().getName());
//System.out.println("TYPE IS: " + XAMUtils.getBasedSimpleType(gType).getClass().getName());
//System.out.println();
                  // # 148447
                  if ( !(XAMUtils.getBasedSimpleType(gType) instanceof SimpleType)) {
                    myContext.addResultItem(ResultType.ERROR, "TYPE_MUST_BE_SIMPLE"); // NOI18N
                  }
                }
            } 
            else {
                boolean hasConsistentType = false; 
                for (GlobalType gType : lastStepTypes) {
                    if (propType.equals(gType)) {
                        hasConsistentType = true;
                    }
                }
                //
                if (hasConsistentType) {
                    // Error. The type of the last XPath element differ from the type
                    // of the correlaton property.
                    myContext.addResultItem(ResultType.WARNING, 
                            "QUERY_AMBIGUOUS_TYPE",
                            propType.getName()); // NOI18N
                } else {
                    // Error. The type of the last XPath element differ from the type
                    // of the correlaton property.
                    myContext.addResultItem(ResultType.ERROR, 
                            "QUERY_CANNOTBE_CONSISTENT_TYPE",
                            propType.getName()); // NOI18N
                }
            }
        }
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
        myContext.addResultItem(ResultType.ERROR, "UNSUPPORTED_VARIABLE_EXPRESSION"); // NOI18N
    }
    
    /**
     * This method is pertinent only for the first step of an absolute location path
     */ 
    private boolean checkFirstStepType(XPathLocationPath locationPath, 
            GlobalElement requiredElement) {
        //
        Set<SchemaCompPair> scPairSet = null;
        LocationStep[] stepArr = locationPath.getSteps();
        if (stepArr != null && stepArr.length != 0) {
            LocationStep firstStep = stepArr[0];
            if (firstStep != null) {
                XPathSchemaContext sContext = firstStep.getSchemaContext();
                if (sContext != null) {
                    scPairSet = sContext.getSchemaCompPairs();
                }
            }
        }
        //
        if (scPairSet == null || scPairSet.isEmpty()) {
            // The schema type was not resolved.
            // It is Ok, but there is nothing to check
            return true; 
        }
        //
        if (scPairSet.size() == 1) {
            SchemaCompPair scPair = scPairSet.iterator().next();
            if (scPair != null) {
                SchemaComponent firstStepComp = 
                        scPair.getCompHolder().getSchemaComponent();
                if (firstStepComp.equals(requiredElement)) {
                    return true;
                }
            }
        }
        //
        String elName = requiredElement.getName();
        String elNsUri = requiredElement.getModel().
                getEffectiveNamespace(requiredElement);
        String requiredElementName = elName;
        if (elNsUri != null) {
            String nsPrefix = myContext.getNsContext().getPrefix(elNsUri);
            if (nsPrefix != null && nsPrefix.length() != 0) {
                requiredElementName = nsPrefix + ":" + elName;
            }
        }
        //
        myContext.addResultItem(ResultType.ERROR, "WRONG_START_ELEMENT_ABSOLUTE",
                requiredElementName); // NOI18N
        //
        return false;
    }
    
    /**
     * Returns a set of possible Schema types, which corresponds to 
     * the last step of the location path. Only global types make sense.
     */ 
    private Set<GlobalType> getLastStepSchemaTypeSet(
            XPathLocationPath locationPath) {
        //
        Set<SchemaCompPair> scPairSet = null;
        //
        LocationStep[] stepArr = locationPath.getSteps();
        if (stepArr != null && stepArr.length > 0) {
            LocationStep lastStep = stepArr[stepArr.length - 1];
            if (lastStep != null) {
                XPathSchemaContext sContext = lastStep.getSchemaContext();
                if (sContext != null) {
                    scPairSet = sContext.getSchemaCompPairs();
                }
            }
        }
        //
        if (scPairSet == null || scPairSet.isEmpty()) {
            // The schema type was not resolved.
            // It is Ok, but there is nothing to check
            return null; 
        }
        //
        if (scPairSet.size() == 1) {
            SchemaCompPair scPair = scPairSet.iterator().next();
            SchemaCompHolder sCompHolder = scPair.getCompHolder();
            GlobalType type = getComponentType(sCompHolder.getSchemaComponent());
            if (type == null) {
                // Error. A global type has to be specified for the last element (attribute)
                // of the Location path.
                String lastElementName = sCompHolder.getName();
//                   myContext.addResultItem(ResultType.ERROR, "QUERY_TAIL_NOT_GLOBAL_TYPE", lastElementName); // NOI18N
                return null;
            } else {
                return Collections.singleton(type);
            }
        } else {
            boolean allTailsAreGlobal = true;
            boolean hasOneGlobalTail = false;
            SchemaComponent sComp = null;
            HashSet<GlobalType> result = new HashSet<GlobalType>();
            for (SchemaCompPair scPair : scPairSet) {
                sComp = scPair.getCompHolder().getSchemaComponent();
                GlobalType type = getComponentType(sComp);
                if (type != null) {
                    result.add(type);
                    hasOneGlobalTail = true;
                } else {
                    allTailsAreGlobal = false;
                }
            }
            //
            if (!hasOneGlobalTail) {
                // Error. The set of possible schema components for the tail 
                // of location path doesn't contain any object with global type. 
                String lastElementName = ((Named)sComp).getName();
//                   myContext.addResultItem(ResultType.ERROR, "QUERY_TAIL_NOT_GLOBAL_TYPE", lastElementName); // NOI18N
                return null;
            } else if (!allTailsAreGlobal) {
                // Error. The set of possible schema components for the tail 
                // of location path contains some objects with not global type. 
                String lastElementName = ((Named)sComp).getName();
                myContext.addResultItem(ResultType.WARNING, 
                        "QUERY_TAIL_CANBE_NOT_GLOBAL_TYPE",
                        lastElementName); // NOI18N
            }
            //
            return result; 
        }
    }
    
    /**
     * Obtains the type of the schema component. 
     * It works only with components which can have a type. 
     */ 
    private GlobalType getComponentType(SchemaComponent comp) {
        NamedComponentReference<? extends GlobalType> gTypeRef = null;

        if (comp instanceof TypeContainer) {
            gTypeRef = ((TypeContainer)comp).getType();
        } else if (comp instanceof LocalAttribute) {
            gTypeRef = ((LocalAttribute)comp).getType();
        } else if (comp instanceof GlobalAttribute) {
            gTypeRef = ((GlobalAttribute)comp).getType();
        } else if (comp instanceof ElementReference) {
            NamedComponentReference<GlobalElement> gELementRef = 
                    ((ElementReference)comp).getRef();
            if (gELementRef != null) {
                GlobalElement gElement = gELementRef.get();
                if (gElement != null) {
                    gTypeRef = gElement.getType();
                }
            }
        }
        //
        if (gTypeRef != null) {
            GlobalType gType = gTypeRef.get();
            if (gType == null) {
                // Error. Can not resolve the global type
                myContext.addResultItem(ResultType.ERROR, "UNRESOLVED_GLOBAL_TYPE",
                        gTypeRef.getRefString()); // NOI18N
            }
            return gType;
        }
        //
        return null;
    }
    
    /**
     * Takes the reference to the correlation property which is used by 
     * the specified property alias and obtains the schema type of that property. 
     */ 
    private GlobalType getPropertyType(PropertyAlias pa) {
        if (pa == null) {
          return null;
        }
        NamedComponentReference<CorrelationProperty> cPropRef =
                pa.getPropertyName();
        if (cPropRef == null) {
            // Warning. The property has not specified yet.
            myContext.addResultItem(ResultType.WARNING, "CPROP_NOT_SPECIFIED"); // NOI18N
            return null;
        } else {
            CorrelationProperty cProp = cPropRef.get();
            if (cProp == null) {
                if (myContext != null) {
                    myContext.addResultItem(ResultType.ERROR, "UNRESOLVED_CPROP"); // NOI18N
                }
                return null;
            }
            //
            GlobalType result = null;
            //
            NamedComponentReference<GlobalType> propTypeRef = cProp.getType();
            if (propTypeRef != null) {
                result = propTypeRef.get();
            } else {
                NamedComponentReference<GlobalElement> propElementRef =
                        cProp.getElement();
                if (propElementRef != null) {
                    GlobalElement propElement = propElementRef.get();
                    if (propElement != null) {
                        NamedComponentReference<? extends GlobalType> typeRef =
                                propElement.getType();
                        if (typeRef != null) {
                            result = typeRef.get();
                        }
                    }
                }
            }
            //
            if (result == null) {
                // Error. Can not resolve the type of Correlation Property
                String type = propTypeRef == null ? "" : propTypeRef.getRefString();
                myContext.addResultItem(ResultType.ERROR, "UNRESOLVED_CPROP_TYPE",
                        type, cProp.getName()); // NOI18N
            }
            return result;
        }
    }
}
