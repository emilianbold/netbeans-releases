/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.compapp.casaeditor.model.casa.validator.visitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnections;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnectionState;
import org.netbeans.modules.compapp.casaeditor.model.casa.validator.CasaSemanticValidator;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class CasaSemanticValidationVisitor extends CasaComponentVisitor.Deep {

    private ValidateSupport mValidateSupport;
    public List<ResultItem> mResultItems;
    private Validation mValidation;
    private List<Model> mValidatedModels;
    private Validator mValidator;
    /** Validate configuration singleton. */
    private static ValidateConfiguration mValConfig;

    public CasaSemanticValidationVisitor(CasaSemanticValidator validator, 
            Validation validation,
            List<Model> validatedModels) {

        synchronized (this.getClass()) {
            mValConfig = new ValidateConfiguration();
            mResultItems = new Vector<ResultItem>();
        }

        mValidator = validator;
        mValidation = validation;
        mValidatedModels = validatedModels;

        getValidateSupport().setValidator(mValidator);
        getValidateSupport().setResultItems(mResultItems);
    }

    public List<ResultItem> getResultItems() {
        return mResultItems;
    }

    // CSVR #1: Make sure no two connections share the same consumes endpoint.
    @Override
    public void visit(CasaConnections connections) {
        Set<String> consumerNames = new HashSet<String>();

        for (CasaConnection connection : connections.getConnections()) {
            // ignore deleted connections
            if (connection.getState().equals(CasaConnectionState.DELETED.getState())) {
                continue;
            }
            
            String consumerName = connection.getConsumer().getRefString();
            if (consumerNames.contains(consumerName)) {
                CasaWrapperModel model = (CasaWrapperModel) connections.getModel();
                CasaEndpointRef consumes = model.getCasaEndpointRef(connection, true);
                
                CasaEndpoint cEndpoint = connection.getConsumer().get();
                
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, consumes,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "ONE_CONSUMER_MULTIPLE_PROVIDERS", // NOI18N
                        cEndpoint.toString()),
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "FIX_ONE_CONSUMER_MULTIPLE_PROVIDERS")); // NOI18N
            } else {
                consumerNames.add(consumerName);
            }
        }
    }
    
    // CSVR #2: Make sure the two endpoints of a connection have the same interface.
    // CSVR #3: Make sure there is no self connection.
    // CSVR #4: Make sure there is no connection connecting two external endpoints.
    @Override
    public void visit(CasaConnection connection) {
        // ignore deleted connections
        if (connection.getState().equals(CasaConnectionState.DELETED.getState())) {
            return;
        }
            
        CasaWrapperModel model = (CasaWrapperModel) connection.getModel();
        
        NamedComponentReference<CasaEndpoint> consumer = connection.getConsumer();
        NamedComponentReference<CasaEndpoint> provider = connection.getProvider();
        
        if (consumer != null && provider != null) {
            CasaEndpoint cEndpoint = consumer.get();
            CasaEndpoint pEndpoint = provider.get();
            
            CasaEndpointRef consumes = model.getCasaEndpointRef(connection, true);
            CasaEndpointRef provides = model.getCasaEndpointRef(connection, false);

            if (consumer.getRefString().equals(provider.getRefString())) {
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, consumes,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "SELF_CONNECTION"),  // NOI18N
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "FIX_SELF_CONNECTION")); // NOI18N
            }

            QName cInterfaceQName = cEndpoint.getInterfaceQName();
            QName pInterfaceQName = pEndpoint.getInterfaceQName();
            if (! cInterfaceQName.equals(pInterfaceQName)) {
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, consumes,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "ENDPOINTS_WITH_DIFFERENT_INTERFACES",  // NOI18N
                        cInterfaceQName, pInterfaceQName),
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "FIX_ENDPOINTS_WITH_DIFFERENT_INTERFACES")); // NOI18N
            } 
            
            CasaServiceEngineServiceUnit cSESU = model.getCasaEngineServiceUnit(consumes);
            CasaServiceEngineServiceUnit pSESU = model.getCasaEngineServiceUnit(provides);
            if (cSESU != null && pSESU != null &&
                    !cSESU.isInternal() && !pSESU.isInternal()) {
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, consumes,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "CONNECTING_TWO_EXTERNAL_ENDPOINTS",  // NOI18N
                        cInterfaceQName, pInterfaceQName),
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "FIX_CONNECTING_TWO_EXTERNAL_ENDPOINTS")); // NOI18N
            }
        }      
    }

    // CSVR #6: Make sure SE SU's supporting WSDL is available.
    @Override
    public void visit(CasaServiceEngineServiceUnit sesu) {
        CasaWrapperModel model = (CasaWrapperModel) sesu.getModel();
        
        for (CasaEndpointRef endpointRef : sesu.getEndpoints()) {
            CasaEndpoint endpoint = endpointRef.getEndpoint().get();
            QName interfaceQName = endpoint.getInterfaceQName();
            PortType portType = model.getPortType(interfaceQName);
            if (portType == null) {
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, sesu,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class,
                        "SERVICE_ENGINE_SERVICE_UNIT_WSDL_NOT_AVAILABLE", // NOI18N
                        endpoint.toString()),
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class,
                        "FIX_SERVICE_ENGINE_SERVICE_UNIT_WSDL_NOT_AVAILABLE")); // NOI18N
            }
        }        
    }
    
    // CSVR #5: Make sure the linked WSDL component is available.
    @Override
    public void visit(CasaLink casaLink) {
        CasaWrapperModel model = (CasaWrapperModel) casaLink.getModel();
        
        String linkHref = casaLink.getHref();
        try {
            Port port = model.getWSDLComponentFromXLinkHref(linkHref, Port.class);

            if (port == null) {
                getValidateSupport().fireToDo(Validator.ResultType.ERROR, casaLink,
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "WSDL_COMPONENT_NOT_AVAILABLE", linkHref), // NOI18N
                        NbBundle.getMessage(CasaSemanticValidationVisitor.class, 
                        "FIX_WSDL_COMPONENT_NOT_AVAILABLE")); // NOI18N
            }
        } catch (Exception e) {
            //???
        }
    }

    /** Gets the validate visitor support.
     * @return  Visitor support.
     */
    public ValidateSupport getValidateSupport() {
        if (null == mValidateSupport) {
            mValidateSupport = new ValidateSupport(mValConfig);
        }
        return mValidateSupport;
    }
    
    public Validation getValidation() {
        return mValidation;
    }
    
    public void setValidation(Validation validation) {
        this.mValidation = validation;
    }
    
}
