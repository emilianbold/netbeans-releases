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
package org.netbeans.modules.xslt.project.wizard.element;

import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.spi.NameGenerator;
import org.netbeans.modules.xslt.tmap.ui.editors.ServiceParamTreeModel;
import org.netbeans.modules.xslt.tmap.util.Util;

/**
 *
 * @author Vitaly Bychkov
 */
public class XsltServiceConfigurationModel {

    private boolean isRequestReply = true;
    private Project myProject;
    private String myServiceName;
    private Operation myWsdlOperation;
    private Operation myPartnerWsdlOperation;

    public XsltServiceConfigurationModel(Project project) {
        this(project, true);
    }

    public XsltServiceConfigurationModel(Project project, boolean isRequestReply) {
        myProject = project;
        this.isRequestReply = isRequestReply;
        initDefaultValues();
    }

    /**
     * @return the myServiceName
     */
    public String getServiceName() {
        return myServiceName;
    }

    /**
     * @param myServiceName the myServiceName to set
     */
    public void setServiceName(String serviceName) {
        myServiceName = serviceName;
    }

    /**
     * @return the myWsdlOperation
     */
    public Operation getWsdlOperation() {
        return myWsdlOperation;
    }

    /**
     * @param myWsdlOperation the myWsdlOperation to set
     */
    public void setWsdlOperation(Operation wsdlOperation) {
        this.myWsdlOperation = wsdlOperation;
    }

    /**
     * @return the myPartnerWsdlOperation
     */
    public Operation getPartnerWsdlOperation() {
        return myPartnerWsdlOperation;
    }

    /**
     * @param myPartnerWsdlOperation the myPartnerWsdlOperation to set
     */
    public void setPartnerWsdlOperation(Operation partnerWsdlOperation) {
        myPartnerWsdlOperation = partnerWsdlOperation;
    }

    private void initDefaultValues() {
        initDefaultServiceNameValue();
        initDefaultWsdlOperations();
    }

    private void initDefaultWsdlOperations() {
        setWsdlOperation(getDefaultWsdlOperation());
        if (!isRequestReply) {
            Operation op = getWsdlOperation();
            setPartnerWsdlOperation(getDefaultWsdlOperation(getWsdlOperation()));
        }
    }
    
    private Project getProject() {
        return myProject;
    }

    private Operation getDefaultWsdlOperation() {
        return getDefaultWsdlOperation(null);
    }    
    
    private Operation getDefaultWsdlOperation(Operation filterOut) {
        Operation wsdlOperation = null;
        TMapModel model = Util.getTMapModel(getProject());
        if (model != null) {
            wsdlOperation = getFirstOperation(ServiceParamTreeModel.getImportedModels(model), filterOut);
            if (wsdlOperation == null) {
                wsdlOperation = getFirstOperation(ServiceParamTreeModel.getNonImportedModels(model), filterOut);
            }
        }

        return wsdlOperation;
    }

    private Operation getFirstOperation(List<WSDLModel> wsdlModels, Operation filterOut) {
        Operation wsdlOperation = null;
        if (wsdlModels != null) {
            for (WSDLModel wsdlModel : wsdlModels) {
                if (wsdlModel == null) {
                    continue;
                }

                Definitions defs = wsdlModel.getDefinitions();
                if (defs == null) {
                    continue;
                }

                Collection<PortType> pts = defs.getPortTypes();
                if (pts == null) {
                    continue;
                }
                for (PortType pt : pts) {
                    if (pt == null) {
                        continue;
                    }
                    Collection<Operation> ops = pt.getOperations();
                    for (Operation op : ops) {
                        if (op != null && !op.equals(filterOut)) {
                            wsdlOperation = op;
                            break;
                        }
                    }
                    if (wsdlOperation != null) {
                        break;
                    }
                }
                if (wsdlOperation != null) {
                    break;
                }
            }
        }
        
        return wsdlOperation;
    }

    private void initDefaultServiceNameValue() {
        String serviceName = null;
        TMapModel model = Util.getTMapModel(getProject());
        if (model != null) {
            serviceName = NameGenerator.getUniqueName(model.getTransformMap(), Service.class);
        }

        setServiceName(serviceName == null ? XsltproConstants.EMPTY_STRING : serviceName);
    }
}
