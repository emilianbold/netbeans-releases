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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xslt.validation.transformmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.TMapConstants;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitorAdapter;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.validation.ValidatorUtil;
import org.netbeans.modules.xslt.validation.core.TMapValidator;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.07
 */
public final class Validator extends TMapValidator {
    public TMapVisitor getVisitor() { 
        return (new VisitorAdapterImpl());
    }
    // ==========================================================
    private class VisitorAdapterImpl extends TMapVisitorAdapter {
        private List<WsdlDataHolder> wsdlHolders;
        private List<String> xsltGlobalParamNameList;

        private List<ServiceVariableCollector> listServiceVariableCollectors = 
            new ArrayList<ServiceVariableCollector>();
        private OperationVariableCollector currentOperationVariableCollector;
        
        private Set<String> 
            setServiceNames = new HashSet<String>(),
            setInvokeNames = new HashSet<String>(),
            setTransformNames = new HashSet<String>(),
            setParamNames = new HashSet<String>(),
            setInputVariableNames = new HashSet<String>(),
            setOutputVariableNames = new HashSet<String>();
        
        private String currentServicePortTypeQName, currentOperationOpName;
        
        @Override
        public void visit(TransformMap transformMap) {
            setServiceNames.clear();
            setInvokeNames.clear();
            listServiceVariableCollectors.clear();
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules]
            getImportedWsdlList(transformMap);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SIE002
            checkImportedWsdlNamespace(transformMap);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SSW001
            checkServicePortTypeMultidefined(transformMap);
            
            visitChildren(transformMap);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SIE001, P2 Group/rules STE003, STE004,
            // P3 Group/the rule SPE004
            checkTransformVariablesValid();
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/the rule SIE003
            checkAmbiguousWsdlDefinitions(transformMap);
        }

        @Override
        public void visit(Service service) {
            listServiceVariableCollectors.add(new ServiceVariableCollector(
                service, new ArrayList<OperationVariableCollector>()));

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SIE001, P2 Group/rules SSE002, SINE002
            currentServicePortTypeQName = checkWsdlContainsPortType(service);
            currentOperationOpName = null;
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SSE001
            checkServiceNameUnique(service);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SOW001
            checkOperationOpNameMultidefined(service);
        }

        @Override
        public void visit(Transform transform) {
            setParamNames.clear();
            
            checkVariableReference(transform.getSource(), transform);
            checkVariableReference(transform.getResult(), transform);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule STE001
            checkTransformNameUnique(transform);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/the rule STE007
            String fileName = transform.getFile();
            if (fileName == null) return;
            if (fileName.trim().length() < 1) {
                addError("FIX_STE007_TRANSFORM_XSL_FILE_NOT_DEFINED_PROPERLY", transform);
                return;
            }
            FileObject transformFileObject = ValidatorUtil.getXsltFileObject(transform);
            if (transformFileObject == null) {
                addError("FIX_STE007_TRANSFORM_XSL_FILE_NOT_FOUND", transform, fileName);
                return;
            }
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SPE005
            XslModel transformXsltModel = ValidatorUtil.getXslModel(transformFileObject);
            xsltGlobalParamNameList = ValidatorUtil.getXsltGlobalParamNameList(
                transformXsltModel);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/the rule STE005
            checkVariablesDependOnXslFile(transform);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/the rule STE006
            checkParamsDependOnXslFile(transform);
        }

        @Override
        public void visit(Param param) {
            //out();
            //out("VISIT param");
            //out();
            checkVariableReference(param, param.getVariableReference());

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SPE001
            checkParamNameUnique(param);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SPE005
            checkParamMatchesXsltGlobalParameter(param);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SPE002
            checkCorrectValueParamType(param);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/rules SPE006, SPE007
            checkParamTypeIsLiteral(param);
        }

        @Override
        public void visit(Operation operation) {
            setTransformNames.clear();
            setInputVariableNames.clear();
            setOutputVariableNames.clear();
            
            currentOperationVariableCollector = new OperationVariableCollector(operation);
            try {
                ServiceVariableCollector serviceVariableCollector = 
                    listServiceVariableCollectors.get(
                    listServiceVariableCollectors.size() - 1);
                List<OperationVariableCollector> listOperationVariableCollectors =
                    serviceVariableCollector.getListOperationVariableCollectors();
                listOperationVariableCollectors.add(currentOperationVariableCollector);
            } catch(Exception e) {
                Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
                    e.getMessage(), e);
            }

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SOE001
            currentOperationOpName = checkWsdlContainsOperation(operation);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SOE002
            checkVariableNameUnique(operation);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SOE003
            checkOutputVariablesInitialization(operation);
        }

        @Override
        public void visit(Invoke invoke) {
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SIE001, P2 Group/rules SSE002, SINE002
            String invokePortTypeQName = checkWsdlContainsPortType(invoke);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SINE003
            String invokeOperationOpName= checkWsdlContainsOperation(invoke);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P1 Group/the rule SINE001
            checkInvokeNameUnique(invoke);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SINE004
            checkVariableNameUnique(invoke);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule SINE005
            checkInvokeInputVariableInitialization(invoke);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P3 Group/the rule SINE006
            checkInvokePortTypeAndOperationName(invoke, invokePortTypeQName,
                invokeOperationOpName);
        }

        private void visitChildren(TMapComponent root) {
            Collection<TMapComponent> children = root.getChildren(TMapComponent.class);
            //out("children: " + children);

            for (TMapComponent component : children) {
                component.accept(this);
                visitChildren(component);
            }
        }

        private void checkVariableReference(Param param, VariableReference reference) {
            if (param == null) return;
            if (! ParamType.PART.equals(param.getType())) return;
            
            if (ParamType.PART.equals(param.getType()) && reference == null) {
                addError("FIX_MissedParamPartValue", param); // NOI18N
                return;
            }
            checkVariableReference(reference, param);
        }
        
        private void checkVariableReference(VariableReference reference, 
            TMapComponent tmapComponent) {
            if (! ((tmapComponent instanceof Transform) || 
                   (tmapComponent instanceof Param))) return;
            
            // for example, if transform doesn't contain attributes 
            // "source"/"result", variable reference will be null
            if (reference == null) return;
            
            checkReference(tmapComponent, reference);
            
            String variableFullName = reference.getRefString();
            if (variableFullName.endsWith(".")) { // NOI18N
                addError("FIX_Wrong_Variable_Name", tmapComponent, reference.toString()); // NOI18N
            }
            if (! variableFullName.startsWith(TMapConstants.DOLLAR_SIGN)) {
                addWarning("FIX_Deprecated_Variable_Format", tmapComponent, variableFullName); // NOI18N
            }
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/rules STE002, SPE003
            checkVariableNameMatchesPattern(variableFullName, tmapComponent);

            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule STE003
            currentOperationVariableCollector.addNewUsedVariable(variableFullName, 
                tmapComponent);
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules]
         */
        private void getImportedWsdlList(TransformMap transformMap) {
            wsdlHolders = WsdlDataHolder.getImportedWsdlList(transformMap.getModel());
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SIE002
         */
        private void checkImportedWsdlNamespace(TransformMap transformMap) {
            if ((wsdlHolders == null) || (wsdlHolders.isEmpty())) return;
            
            for (WsdlDataHolder wsdlHolder : wsdlHolders) {
                String 
                    wsdlTargetNamespace = ValidatorUtil.getWsdlTargetNamespace(wsdlHolder),
                    importedWsdlNamespace = wsdlHolder.getNamespace();
                if ((wsdlTargetNamespace != null) && (importedWsdlNamespace != null) && 
                    (! importedWsdlNamespace.equals(wsdlTargetNamespace))) {
                    addError("FIX_SIE002_DIFFERENT_WSDL_NAMESPACE_TARGET_NAMESPACE", 
                        transformMap, importedWsdlNamespace, wsdlTargetNamespace, wsdlHolder.getLocation());
                }
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/rules STE002, SPE003
         */
        private void checkVariableNameMatchesPattern(String variableFullName, 
            TMapComponent tmapComponent) {
            if (! ((tmapComponent instanceof Transform) || 
                   (tmapComponent instanceof Param))) return;

            if (! ValidatorUtil.isVariableNameCorrect(variableFullName, tmapComponent)) {
                String strPattern = (tmapComponent instanceof Transform ? 
                    ValidatorUtil.TRANSFORM_VAR_NAME_PATTERN : 
                    ValidatorUtil.PARAM_VAR_NAME_PATTERN);
                addError("FIX_STE002_VARIABLE_NAME_DOES_NO_MATCH_PATTERN", 
                    tmapComponent, variableFullName, strPattern);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SIE001, P2 Group/rules SSE002, SINE002
         * 
         * Returns a string with port type QName.
         */
        private String checkWsdlContainsPortType(TMapComponent component) {
            String portTypeQName = ValidatorUtil.getPortTypeQName(component);
            if (component instanceof Service) currentServicePortTypeQName = portTypeQName;

            if (portTypeQName == null) return null;
                
            String portTypeQNamePrefix = ValidatorUtil.getQNamePrefix(portTypeQName);
            WsdlDataHolder wsdlHolder = WsdlDataHolder.findWsdlByQNamePrefix(
                portTypeQNamePrefix, wsdlHolders);
            if (wsdlHolder == null) return portTypeQName;
            
            String portTypeName = ValidatorUtil.getQNameLocalPart(portTypeQName);
            if (! ValidatorUtil.checkWsdlContainsPortType(portTypeName, wsdlHolder)) {
                addError("FIX_SIE001_INVALID_VARIABLE_PORT_TYPE", component, 
                    portTypeName, wsdlHolder.getLocation());
            }
            return portTypeQName;
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SSE001
         */
        private void checkServiceNameUnique(Service service) {
            String serviceName = service.getName();
            if ((serviceName == null) || (serviceName.length() < 1)) return;
            
            if (setServiceNames.contains(serviceName)) {
                addError("FIX_SSE001_SERVICE_NAME_NOT_UNIQUE", service, serviceName);
            } else {
                setServiceNames.add(serviceName);
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SSW001
         */
        private void checkServicePortTypeMultidefined(TransformMap transformMap) {
            if (transformMap == null) return;
            List<Service> services = transformMap.getServices();
            if ((services == null) || (services.isEmpty())) return;
            
            List<ServicePortTypeWrapper> listServicePortTypeWrappers = 
                new ArrayList<ServicePortTypeWrapper>();
            for (Service service : services) {
                listServicePortTypeWrappers.add(new ServicePortTypeWrapper(service));
            }            
            while (! listServicePortTypeWrappers.isEmpty()) {
                ServicePortTypeWrapper wrapper = listServicePortTypeWrappers.get(0);
                listServicePortTypeWrappers.remove(0);
                
                int lastIndex = listServicePortTypeWrappers.lastIndexOf(wrapper);
                if (lastIndex > -1) {
                    wrapper = listServicePortTypeWrappers.get(lastIndex);
                    addWarning("FIX_SSW001_SEVERAL_SERVICES_WITH_SAME_PORTTYPE", 
                        wrapper.getService(), wrapper.getServicePortTypeName());
                    
                    while (lastIndex > -1) {
                        listServicePortTypeWrappers.remove(lastIndex--);
                        lastIndex = listServicePortTypeWrappers.lastIndexOf(wrapper);
                    }
                }
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SIE001, P2 Group/rules STE003, STE004,
         * P3 Group/the rule SPE004
         */
        private void checkTransformVariablesValid() {
            if (listServiceVariableCollectors == null) return;
            
            for (ServiceVariableCollector serviceVarCollector : listServiceVariableCollectors) {
                if (serviceVarCollector == null) continue;
                
                List<OperationVariableCollector> operationVariableCollectors = 
                    serviceVarCollector.getListOperationVariableCollectors();
                if ((serviceVarCollector.getService() == null) || 
                    (operationVariableCollectors == null)) {
                    continue;
                }
                for (OperationVariableCollector varCollector : operationVariableCollectors) {
                    if (varCollector == null) continue;
                    
                    List<String> 
                        definedVariables = varCollector.getDefinedVariables(),
                        usedVariables = varCollector.getUsedVariables();

                    if (definedVariables.isEmpty() || usedVariables.isEmpty()) continue;

                    for (String usedVariableName : usedVariables) {
                        if ((usedVariableName == null) || (usedVariableName.length() < 1)) 
                            continue;

                        TMapComponent usedVariableHolder = // Transform or Param
                            varCollector.getUsedVariableHolder(usedVariableName);
                        String simpleVariableName = ValidatorUtil.getVariableSimpleName(
                            usedVariableName);
                        
                        // [Validation rules], P2 Group/ the rule STE003,
                        // P3 Group/the rule SPE004
                        if (! definedVariables.contains(simpleVariableName)) {
                            addError(usedVariableHolder instanceof Transform ?
                                "FIX_STE003_UNDEFINED_TRANSFORM_VARIABLE" :
                                "FIX_SPE004_UNDEFINED_PARAM_VARIABLE", 
                                usedVariableHolder, usedVariableName);
                        } else {
                            // [Validation rules], P2 Group/ the rule STE004,
                            // P3 Group/the rule SPE004
                            VariableDeclarator varDeclarator = 
                                varCollector.getVariableDeclarator(simpleVariableName);
                            checkWsdlContainsVariablePartName(varDeclarator, 
                                usedVariableHolder, usedVariableName);
                        }
                    }
                }
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SIE001, P2 Group/the rule STE004
         * P3 Group/the rule SPE004
         */
        private void checkWsdlContainsVariablePartName(VariableDeclarator varDeclarator, 
            TMapComponent tmapComponent, String variableName) {
            if ((varDeclarator == null) || (tmapComponent == null) ||
                (wsdlHolders == null) || (variableName == null) || 
                (variableName.length() < 1)) 
                return;
            
            String partName = ValidatorUtil.getVariablePartName(variableName);
            if ((partName == null) || 
                (partName.equals(TMapConstants.PART_NAME_STATUS))) return;
            
            String portTypeQName = (varDeclarator instanceof Operation ? 
                ValidatorUtil.getPortTypeQName(tmapComponent) : 
                ValidatorUtil.getPortTypeQName(varDeclarator));
            if (portTypeQName == null) return;
                
            String portTypeQNamePrefix = ValidatorUtil.getQNamePrefix(portTypeQName);
            WsdlDataHolder wsdlHolder = WsdlDataHolder.findWsdlByQNamePrefix(
                portTypeQNamePrefix, wsdlHolders);
            if (wsdlHolder == null) return;
            
            if (ValidatorUtil.checkWsdlContainsVariablePartName(partName, wsdlHolder)) 
                return;
            
            // [Validation rules], P1 Group/the rule SIE001, P2 Group/the rule STE004
            // P3 Group/the rule SPE004
            addError("FIX_SIE001_INVALID_VARIABLE_PART_NAME", tmapComponent, 
                variableName, partName, wsdlHolder.getLocation());
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule SIE003
         */
        private void checkAmbiguousWsdlDefinitions(TransformMap transformMap) {
            if ((transformMap == null) || (wsdlHolders == null) || 
                (wsdlHolders.isEmpty())) return;
            
            for (int mainIdx = 0; mainIdx < wsdlHolders.size() - 1; ++mainIdx) {
                WsdlDataHolder mainWsdlHolder = wsdlHolders.get(mainIdx);
                String mainQNamePrefix = mainWsdlHolder.getQNamePrefix();
                if ((mainQNamePrefix == null) || (mainQNamePrefix.length() < 1)) 
                    continue;
                
                for (int otherIdx = mainIdx + 1; otherIdx < wsdlHolders.size(); ++otherIdx) {
                    WsdlDataHolder otherWsdlHolder = wsdlHolders.get(otherIdx);
                    
                    String otherQNamePrefix = otherWsdlHolder.getQNamePrefix();
                    if ((otherQNamePrefix == null) || (otherQNamePrefix.length() < 1))
                        continue;
                        
                    if (mainQNamePrefix.equals(otherQNamePrefix)) {
                        checkAmbiguousOperaionDefinitions(transformMap, mainWsdlHolder, 
                            otherWsdlHolder);
                        checkAmbiguousPortTypeDefinitions(transformMap, mainWsdlHolder, 
                            otherWsdlHolder);
                        checkAmbiguousPartNameDefinitions(transformMap, mainWsdlHolder, 
                            otherWsdlHolder);
                    }
                }
            }
        }
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule SIE003
         */
        private void checkAmbiguousOperaionDefinitions(TransformMap transformMap,
            WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
            if ((transformMap == null) || (mainWsdlHolder == null) || 
                (otherWsdlHolder == null)) return;
            
            List<String> ambiguousOperationNames = ValidatorUtil.getEqualOperationNames(
                mainWsdlHolder, otherWsdlHolder);
            if (ambiguousOperationNames != null) {
                String mainWsdlLocation = mainWsdlHolder.getLocation(),
                       otherWsdlLocation = otherWsdlHolder.getLocation();
                for (String operationName : ambiguousOperationNames) {
                    if (operationName == null) continue;

                    addError("FIX_SIE003_AMBIGUOUS_OPERATION_DEFINITION", 
                        transformMap, operationName, mainWsdlLocation, otherWsdlLocation);
                }
            }
        }
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule SIE003
         */
        private void checkAmbiguousPortTypeDefinitions(TransformMap transformMap,
            WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
            if ((transformMap == null) || (mainWsdlHolder == null) || 
                (otherWsdlHolder == null)) return;
            
            List<String> ambiguousPortTypes = ValidatorUtil.getEqualPortTypes(
                mainWsdlHolder, otherWsdlHolder);
            if (ambiguousPortTypes != null) {
                String mainWsdlLocation = mainWsdlHolder.getLocation(),
                       otherWsdlLocation = otherWsdlHolder.getLocation();
                for (String portType : ambiguousPortTypes) {
                    if (portType == null) continue;

                    addError("FIX_SIE003_AMBIGUOUS_POPT_TYPE_DEFINITION", 
                        transformMap, portType, mainWsdlLocation, otherWsdlLocation);
                }
            }
        }
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule SIE003
         */
        private void checkAmbiguousPartNameDefinitions(TransformMap transformMap,
            WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
            if ((transformMap == null) || (mainWsdlHolder == null) || 
                (otherWsdlHolder == null)) return;
            
            List<String> ambiguousPartNames = ValidatorUtil.getEqualPortTypes(
                mainWsdlHolder, otherWsdlHolder);
            if (ambiguousPartNames != null) {
                String mainWsdlLocation = mainWsdlHolder.getLocation(),
                       otherWsdlLocation = otherWsdlHolder.getLocation();
                for (String partName : ambiguousPartNames) {
                    if (partName == null) continue;

                    addError("FIX_SIE003_AMBIGUOUS_PART_NAME_DEFINITION", 
                        transformMap, partName, mainWsdlLocation, otherWsdlLocation);
                }
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule STE001
         */
        private void checkTransformNameUnique(Transform transform) {
            String name = transform.getName();
            if ((name == null) || (name.length() < 1)) return;
            
            if (setTransformNames.contains(name)) {
                addError("FIX_STE001_TRANSFORM_NAME_NOT_UNIQUE", transform, name);
            } else {
                setTransformNames.add(name);
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule STE005
         */
        private void checkVariablesDependOnXslFile(Transform transform) {
            if (transform == null) return;
            
            String xsltFile = transform.getFile();
            boolean isXsltFileDefined = (xsltFile != null) && (xsltFile.length() > 0);
            
            if (! isXsltFileDefined) {
                VariableReference sourceRef = transform.getSource(),
                                  resultRef = transform.getResult();
                if ((sourceRef == null) || (resultRef == null)) return;
                if (! ValidatorUtil.isSourceResultTheSameDataType(sourceRef, resultRef)) {
                    addError("FIX_STE005_NO_XSL_FILE_BUT_SOURCE_RESULT_DIFFERENT_DATA_TYPE", 
                        transform);
                }
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule STE006
         */
        private void checkParamsDependOnXslFile(Transform transform) {
            if (transform == null) return;
            
            String xsltFile = transform.getFile();
            boolean isXsltFileDefined = (xsltFile != null) && (xsltFile.length() > 0);
            List<Param> params = transform.getParams();
            
            if ((! isXsltFileDefined) && (params != null) && (! params.isEmpty())) {
                addError("FIX_STE006_NO_XSL_FILE_BUT_PARAM_LIST_NOT_EMPTY", transform);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SINE001
         */
        private void checkInvokeNameUnique(Invoke invoke) {
            String name = invoke.getName();
            if ((name == null) || (name.length() < 1)) return;
            
            if (setInvokeNames.contains(name)) {
                addError("FIX_SINE001_INVOKE_NAME_NOT_UNIQUE", invoke, name);
            } else {
                setInvokeNames.add(name);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SPE001
         */
        private void checkParamNameUnique(Param param) {
            String name = param.getName();
            if ((name == null) || (name.length() < 1)) return;
            
            if (setParamNames.contains(name)) {
                addError("FIX_SPE001_PARAM_NAME_NOT_UNIQUE", param, name);
            } else {
                setParamNames.add(name);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SPE005
         */
        private void checkParamMatchesXsltGlobalParameter(Param param) {
            if (param == null) return;
            String paramName = param.getName();
            if ((paramName == null) || (paramName.length() < 1)) return;
            
            if ((xsltGlobalParamNameList == null) || 
                (! xsltGlobalParamNameList.contains(paramName))) {
                String xsltFileName = "";
                
                TMapComponent parentComponent = param.getParent();
                if (parentComponent instanceof Transform) {
                    xsltFileName = ((Transform) parentComponent).getFile();
                }
                if ((xsltFileName == null) || (xsltFileName.length() < 1)) return;
                
                addError("FIX_SPE005_PARAM_DOES_NOT_MATCH_XSLT_GLOBAL_PARAM", 
                    param, paramName, xsltFileName);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SPE002
         */
        private void checkCorrectValueParamType(Param param) {
            if (param == null) return;
            ParamType paramType = param.getType();
            
            if ((paramType == null) || ((! paramType.equals(ParamType.PART)) && 
                (! paramType.equals(ParamType.LITERAL)))) {
                addError("FIX_SPE002_WRONG_PARAM_TYPE_VALUE", 
                    param, ParamType.LITERAL.getStringValue(), 
                    ParamType.PART.getStringValue());
            }
        }
            
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/rules SPE006, SPE007
         */
        private void checkParamTypeIsLiteral(Param param) {
            if (param == null) return;
            ParamType paramType = param.getType();
            if (paramType == null) return;
                
            String paramValue = param.getValue();
            
            if (paramType.equals(ParamType.LITERAL)) {
                int xmlContentItemCount = checkParamTypeLiteralXmlContent(param, 
                    paramType, paramValue);
                if (xmlContentItemCount < 0) return;
                
                if ((paramValue != null) && (xmlContentItemCount > 0)) {
                    addError("FIX_SPE006_PARAM_TYPE_LITERAL_BUT_VALUE_AND_XML_CONTENT_PRESENTED", 
                        param);
                }
                if ((paramValue == null) && (xmlContentItemCount == 0)) {
                    addError("FIX_SPE006_PARAM_TYPE_LITERAL_BUT_VALUE_AND_XML_CONTENT_NOT_PRESENTED", 
                        param);
                }
            }
        }
            
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/rules SPE006, SPE007
         */
        private int checkParamTypeLiteralXmlContent(Param param, 
            ParamType paramType, String paramValue) {
            if ((param == null) || (paramType == null)) return (-1);

            if (paramType.equals(ParamType.LITERAL)) {
                Element peerElement = param.getPeer();
                if (peerElement == null) return (-1);
                
                NodeList childNodes = peerElement.getChildNodes();
                if (childNodes == null) return (0);
                
                int childAmount = childNodes.getLength(), notEmptyNodeIndex = 0;
                for (int i = 0; i < childAmount; ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode == null) continue;
                    
                    int nodeType = childNode.getNodeType();
                    
                    if (nodeType == Node.TEXT_NODE) {
                        String text = ((Text) childNode).getNodeValue();
                        // skip lines with white spaces only
                        if ((text != null) && (text.trim().length() < 1)) {
                            continue;
                        }
                    }
                    ++notEmptyNodeIndex;                    
                    
                    if ((paramValue == null) && 
                        (nodeType != Node.ELEMENT_NODE) && (nodeType != Node.TEXT_NODE)) {
                        addError("FIX_SPE007_PARAM_TYPE_LITERAL_BUT_WRONG_CHILD_NODE_TYPE", 
                            param, String.valueOf(notEmptyNodeIndex));
                    }
                }
                if ((paramValue == null) && (notEmptyNodeIndex > 1)) {
                    addError("FIX_SPE007_PARAM_TYPE_LITERAL_BUT_TOO_MANY_CHILD_NODES", 
                        param, String.valueOf(notEmptyNodeIndex));
                }
                return notEmptyNodeIndex;
            }
            return (-1);
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SOE002, P2 Group/the rule SINE004
         */
        private void checkVariableNameUnique(TMapComponent tmapComponent) {
            if (! ((tmapComponent instanceof Operation) || (tmapComponent instanceof Invoke)))
                return;
            
            Variable inputVar = ((VariableDeclarator) tmapComponent).getInputVariable(),
                     outputVar = ((VariableDeclarator) tmapComponent).getOutputVariable();
            String inputVariableName = null, outputVariableName = null;

            if (inputVar != null) inputVariableName = inputVar.getName();
            if (inputVariableName == null) {
                // variableName = ValidatorUtil.getOperationInputMessageName(tmapComponent);
                inputVar = ((VariableDeclarator) tmapComponent).getDefaultInputVariable();
                inputVariableName = (inputVar == null) ? null : inputVar.getName();
            } 
            
            if (outputVar != null) outputVariableName = outputVar.getName();
            if (outputVariableName == null) { 
                // variableName = ValidatorUtil.getOperationOutputMessageName(tmapComponent);
                outputVar = ((VariableDeclarator) tmapComponent).getDefaultOutputVariable();
                outputVariableName = (outputVar == null) ? null : outputVar.getName();
            }
            
            if ((inputVariableName != null) && (outputVariableName != null) &&
                (inputVariableName.equals(outputVariableName))) {
                addError("FIX_SOE002_INPUT_OUTPUT_VARIABLE_NAME_EQUAL", tmapComponent, 
                    inputVariableName);
            }
            
            checkVariableNameUnique(tmapComponent, inputVariableName, 
                VariableDeclarator.INPUT_VARIABLE, setInputVariableNames, 
                VariableDeclarator.INPUT_VARIABLE);
            checkVariableNameUnique(tmapComponent, inputVariableName, 
                VariableDeclarator.INPUT_VARIABLE, setOutputVariableNames, 
                VariableDeclarator.OUTPUT_VARIABLE);
            checkVariableNameUnique(tmapComponent, outputVariableName, 
                VariableDeclarator.OUTPUT_VARIABLE, setOutputVariableNames, 
                VariableDeclarator.OUTPUT_VARIABLE);
            checkVariableNameUnique(tmapComponent, outputVariableName, 
                VariableDeclarator.OUTPUT_VARIABLE, setInputVariableNames, 
                VariableDeclarator.INPUT_VARIABLE);

            if (inputVariableName != null)  setInputVariableNames.add(inputVariableName);
            if (outputVariableName != null) setOutputVariableNames.add(outputVariableName);
            
            // http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
            // [Validation rules], P2 Group/the rule STE003
            currentOperationVariableCollector.addNewDefinedVariable(
                (VariableDeclarator) tmapComponent, inputVariableName);
            currentOperationVariableCollector.addNewDefinedVariable(
                (VariableDeclarator) tmapComponent, outputVariableName);
        }
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SINE004
         */
        private void checkVariableNameUnique(TMapComponent tmapComponent, String variableName, 
            String attributeName, Set<String> setVariableNames, String comparedAttributeName) {
            if ((variableName == null) || (setVariableNames == null) || 
                (attributeName == null) || (comparedAttributeName == null)) return;
            
            if ((variableName == null) || (variableName.length() < 1)) return;
            
            if (setVariableNames.contains(variableName)) {
                addError("FIX_SINE004_VARIABLE_NAME_NOT_UNIQUE", tmapComponent, 
                    variableName, attributeName, comparedAttributeName);
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SOE001, P2 Group/the rule SINE003
         * 
         * Returns a string with operation name.
         */
        private String checkWsdlContainsOperation(TMapComponent tmapComponent) {
            if (! ((tmapComponent instanceof Operation) || (tmapComponent instanceof Invoke))) 
                return null;
            
            String operationName = (tmapComponent instanceof Operation) ?
                ((Operation) tmapComponent).getOperation().getRefString() :
                ((Invoke) tmapComponent).getOperation().getRefString();

            String portTypeQName = ValidatorUtil.getPortTypeQName(tmapComponent);
            if (portTypeQName == null) return operationName;
                
            String portTypeQNamePrefix = ValidatorUtil.getQNamePrefix(portTypeQName);
            WsdlDataHolder wsdlHolder = WsdlDataHolder.findWsdlByQNamePrefix(
                portTypeQNamePrefix, wsdlHolders);
            if (wsdlHolder == null) return operationName;
            
            if (! ValidatorUtil.checkWsdlContainsOperation(operationName, wsdlHolder)) {
                addError("FIX_SOE001_INVALID_OPERATION_NAME", tmapComponent, operationName, 
                    ValidatorUtil.getQNameLocalPart(portTypeQName), wsdlHolder.getLocation());
            }
            return operationName;
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P1 Group/the rule SOE003
         */
        private void checkOutputVariablesInitialization(Operation operation) {
            Set<String> setWsdlPartNames = ValidatorUtil.getOperationOutputPartNameSet(
                operation);
            if ((setWsdlPartNames == null) || (setWsdlPartNames.isEmpty())) return;
            
            List<Transform> transforms = operation.getTransforms();
            Set<String> setUsedPartNames = 
                ValidatorUtil.getTransformsOutputPartNameSet(transforms);
            if ((setUsedPartNames != null) && 
                (! setUsedPartNames.containsAll(setWsdlPartNames))) {
                // for error message only those WSDL part names should be stored, 
                // which are not used in result variables of elements "transform"
                setWsdlPartNames.removeAll(setUsedPartNames);
                String operationName = operation.getOperation().getRefString();

                addError("FIX_SOE003_WRONG_INITIALIZATION_OUTPUT_VARIABLES", 
                    operation, operationName, setWsdlPartNames.toString());
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SOW001
         */
        private void checkOperationOpNameMultidefined(Service service) {
            if (service == null) return;
            List<Operation> operations = service.getOperations();
            if ((operations == null) || (operations.isEmpty())) return;
            
            List<OperationOpNameWrapper> listOperationOpNameWrappers = 
                new ArrayList<OperationOpNameWrapper>();
            for (Operation operation : operations) {
                listOperationOpNameWrappers.add(new OperationOpNameWrapper(operation));
            }
            while (! listOperationOpNameWrappers.isEmpty()) {
                OperationOpNameWrapper wrapper = listOperationOpNameWrappers.get(0);
                listOperationOpNameWrappers.remove(0);
                
                int lastIndex = listOperationOpNameWrappers.lastIndexOf(wrapper);
                if (lastIndex > -1) {
                    wrapper = listOperationOpNameWrappers.get(lastIndex);
                    addWarning("FIX_SOW001_SEVERAL_OPERATIONS_WITH_SAME_OPNAME", 
                        wrapper.getOperation(), wrapper.getOperationOpName());
                    
                    while (lastIndex > -1) {
                        listOperationOpNameWrappers.remove(lastIndex);
                        lastIndex = listOperationOpNameWrappers.lastIndexOf(wrapper);
                    }
                }
            }
        }

        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P2 Group/the rule SINE005
         */
        private void checkInvokeInputVariableInitialization(Invoke invoke) {
            if (invoke == null) return;
            
            TMapComponent parentComponent = invoke.getParent();
            if (! (parentComponent instanceof Operation)) return;
                
            List<TMapComponent> operationChildren = parentComponent.getChildren();
            if (operationChildren == null) return;
            
            List<Transform> transforms = new ArrayList<Transform>();
            // get all "transforms", located above than the given "invoke"
            for (TMapComponent operationChild : operationChildren) {
                if (operationChild == invoke) break;
                if (operationChild instanceof Transform) 
                    transforms.add((Transform) operationChild);
            }
            Set<String> setUsedPartNames = 
                ValidatorUtil.getTransformsOutputPartNameSet(transforms);
            
            Set<String> setWsdlPartNames = ValidatorUtil.getOperationInputPartNameSet(invoke);
            if ((setWsdlPartNames == null) || (setWsdlPartNames.isEmpty())) return;

            if ((setUsedPartNames != null) && 
                (! setUsedPartNames.containsAll(setWsdlPartNames))) {
                // for error message only those WSDL part names should be stored, 
                // which are not used in result variables of elements "transform"
                setWsdlPartNames.removeAll(setUsedPartNames);
                String operationName = invoke.getOperation().getRefString();

                addError("FIX_SINE005_WRONG_INITIALIZATION_INPUT_VARIABLES", 
                    invoke, operationName, setWsdlPartNames.toString());
            }
        }
        
        /**
         * http://wikihome.sfbay.sun.com/SOA-BI/Wiki.jsp?page=TransformmapEditor
         * [Validation rules], P3 Group/the rule SINE006
         */
        private void checkInvokePortTypeAndOperationName(Invoke invoke, 
            String invokePortTypeQName, String invokeOperationOpName) {
            if ((invoke == null) || 
                (invokePortTypeQName == null) || (invokeOperationOpName == null) ||
                (currentServicePortTypeQName == null) || (currentOperationOpName == null))
                return;
            
            if ((invokePortTypeQName.equals(currentServicePortTypeQName)) && 
                (invokeOperationOpName.equals(currentOperationOpName))) {
                addError("FIX_SINE006_INVOKE_WRONG_COMBINATION_PORTTYPE_OPERATION_NAME", 
                    invoke, invokePortTypeQName, invokeOperationOpName);
            }
        }
    } // private class
}
class ServicePortTypeWrapper implements Comparable {
    private Service service;

    public ServicePortTypeWrapper(Service service) {
        this.service = service;
    }

    public Service getService() {return service;}
    public String getServicePortTypeName() {return toString();}
    
    @Override
    public String toString() {
        try {
            String servicePortTypeName = service.getPortType().getRefString();
            return servicePortTypeName;
        } catch(Exception e) {
            return null;
        }
    }

    public int compareTo(Object obj) {
        if (! (obj instanceof ServicePortTypeWrapper)) return -1;
        ServicePortTypeWrapper anotherWrapper = (ServicePortTypeWrapper) obj;
        
        String portTypeName = getServicePortTypeName(),
               anotherPortTypeName = anotherWrapper.getServicePortTypeName();
        if ((portTypeName == null) && (anotherPortTypeName == null)) return 0;
        if ((portTypeName == null) && (anotherPortTypeName != null)) return -1;
        if ((portTypeName != null) && (anotherPortTypeName == null)) return 1;
        
        return (portTypeName.compareTo(anotherPortTypeName));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ServicePortTypeWrapper)) return false;
        ServicePortTypeWrapper anotherWrapper = (ServicePortTypeWrapper) obj;
        
        String portTypeName = getServicePortTypeName(),
               anotherPortTypeName = anotherWrapper.getServicePortTypeName();
        if ((portTypeName == null) && (anotherPortTypeName == null)) return true;
        if ((portTypeName == null) || (anotherPortTypeName == null)) return false;
        
        return (portTypeName.equals(anotherPortTypeName));
    }

    @Override
    public int hashCode() {
        String portTypeName = getServicePortTypeName();
        int hash = 7;
        hash = 59 * hash + (portTypeName != null ? portTypeName.hashCode() : 0);
        return hash;
    }
}

class OperationOpNameWrapper implements Comparable {
    private Operation operation;

    public OperationOpNameWrapper(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperation() {return operation;}
    public String getOperationOpName() {return toString();}
    
    @Override
    public String toString() {
        try {
            String operationOpName = operation.getOperation().getRefString();
            return operationOpName;
        } catch(Exception e) {
            return null;
        }
    }

    public int compareTo(Object obj) {
        if (! (obj instanceof OperationOpNameWrapper)) return -1;
        OperationOpNameWrapper anotherWrapper = (OperationOpNameWrapper) obj;
        
        String operationOpName = getOperationOpName(),
               anotherOpName = anotherWrapper.getOperationOpName();
        if ((operationOpName == null) && (anotherOpName == null)) return 0;
        if ((operationOpName == null) && (anotherOpName != null)) return -1;
        if ((operationOpName != null) && (anotherOpName == null)) return 1;
        
        return (operationOpName.compareTo(anotherOpName));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof OperationOpNameWrapper)) return false;
        OperationOpNameWrapper anotherWrapper = (OperationOpNameWrapper) obj;
        
        String operationOpName = getOperationOpName(),
               anotherOpName = anotherWrapper.getOperationOpName();
        if ((operationOpName == null) && (anotherOpName == null)) return true;
        if ((operationOpName == null) || (anotherOpName == null)) return false;
        
        return (operationOpName.equals(anotherOpName));
    }

    @Override
    public int hashCode() {
        String operationOpName = getOperationOpName();
        int hash = 7;
        hash = 59 * hash + (operationOpName != null ? operationOpName.hashCode() : 0);
        return hash;
    }
}

class ServiceVariableCollector {
    private Service service;
    private List<OperationVariableCollector> listOperationVariableCollectors;

    public ServiceVariableCollector(Service service, 
        List<OperationVariableCollector> lisOperationVariableCollectors) {
        this.service = service;
        this.listOperationVariableCollectors = lisOperationVariableCollectors;
    }

    public Service getService() {return service;}
    public List<OperationVariableCollector> getListOperationVariableCollectors() {
        return listOperationVariableCollectors;
    }
}

class OperationVariableCollector {
    private Operation operation;
    
    // <operation inputVariable="<var_name>" outputVariable="<var_name>" ...>,
    // <invoke inputVariable="<var_name>" outputVariable="<var_name>" ...>
    // Map<key, value>: <input/output variable name, operation/invoke>
    private Map<String, VariableDeclarator> mapDefinedVariables = 
        new HashMap<String, VariableDeclarator>();
    
    // <transform source="$<var_name>.<part_name>" result="$<var_name>.<part_name>" ...>
    // <param name="param1" type="part" value="$<var_name>.<part_name>"/>
    // Map<key, value>: <source/result name, transform/param>
    private Map<String, TMapComponent> mapUsedVariables = 
        new HashMap<String, TMapComponent>();
            
    public OperationVariableCollector(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperation() {return operation;}
    public List<String> getDefinedVariables() {
        return (new ArrayList<String>(mapDefinedVariables.keySet()));
    }
    public VariableDeclarator getVariableDeclarator(String definedVariableName) {
        return (mapDefinedVariables.get(definedVariableName));
    }
    public List<String> getUsedVariables() {
        return (new ArrayList<String>(mapUsedVariables.keySet()));
    }
    /**
     * 
     * @param usedVariableName
     * @return Transform or Param
     */
    public TMapComponent getUsedVariableHolder(String usedVariableName) {
        return (mapUsedVariables.get(usedVariableName));
    }
    
    public boolean addNewDefinedVariable(VariableDeclarator varDeclarator, 
        String variableName) {
        if ((varDeclarator == null) || 
            (variableName == null) || (variableName.length() < 1)) return false;
        
        if (! mapDefinedVariables.containsKey(variableName)) {
            mapDefinedVariables.put(variableName, varDeclarator);
        }
        return (mapDefinedVariables.containsKey(variableName));
    }
    
    /**
     * 
     * @param variableName
     * @param tmapComponent Transform or Param
     */
    public void addNewUsedVariable(String variableName, TMapComponent tmapComponent) {
        if (! ((tmapComponent instanceof Transform) || 
               (tmapComponent instanceof Param))) return;
        if ((variableName == null) || (variableName.length() < 1)) return;
        
        if (! mapUsedVariables.containsKey(variableName)) {
            mapUsedVariables.put(variableName, tmapComponent);
        }
    }
}