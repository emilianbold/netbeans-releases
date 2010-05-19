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
package org.netbeans.modules.wsdlextensions.jdbc.configeditor;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCAddress;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCBinding;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperation;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationInput;
import org.netbeans.modules.wsdlextensions.jdbc.configeditor.panels.DBBindingConfigurationPanel;
import org.netbeans.modules.wsdlextensions.jdbc.impl.JDBCAddressImpl;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * 
 * @author Naveen K
 */
public class DBConfigurationEditorComponent implements ExtensibilityElementConfigurationEditorComponent {

    DBBindingConfigurationPanel dbbindingConfigPanel;
    private WSDLComponent wsdlComponent;
    private QName qName;
    private Operation operation;

    /**
     * 
     * @param qname
     * @param component
     */
    public DBConfigurationEditorComponent(QName qname, WSDLComponent component) {
        if (dbbindingConfigPanel == null) {
            dbbindingConfigPanel = new DBBindingConfigurationPanel(qname, component);
        } 
        dbbindingConfigPanel.resetView();
        dbbindingConfigPanel.populateView(qname, component);
        this.qName = qname;
        this.wsdlComponent = component;
    }

    /**
     * 
     * @return JPanel
     */
    public JPanel getEditorPanel() {
        return dbbindingConfigPanel;
    }

    /**
     * 
     * @return The title of this component
     */
    public String getTitle() {
        return NbBundle.getMessage(this.getClass(), "DBConfigurationEditorComponent.Title");
    }

    /**
     * 
     * @return for now the default help context.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public ActionListener getActionListener() {
        return null;
    }

    public boolean commit() {
        boolean commit = false;
        if (!dbbindingConfigPanel.isValid()) {
            return false;
        }
        if (wsdlComponent instanceof JDBCAddress) {
            return commitAddress((JDBCAddress) wsdlComponent);
        } else if (wsdlComponent instanceof JDBCBinding) {
            return commitBinding((JDBCBinding) wsdlComponent);
        } else if (wsdlComponent instanceof Port) {
            return commitPort((Port) wsdlComponent);
        } else if (wsdlComponent instanceof JDBCOperationInput) {
            return commitInputMessage((JDBCOperationInput) wsdlComponent);
        } else if (wsdlComponent instanceof JDBCOperation) {
            return commitOperation((JDBCOperation) wsdlComponent);
        }

        return false;
    }

    public boolean commitAddress(JDBCAddress jdbcAddress) {
        WSDLModel wsdlModel = jdbcAddress.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            jdbcAddress.setJDBCURL(dbbindingConfigPanel.getJNDIName());
            Port port = (Port) jdbcAddress.getParent();
            Binding binding = port.getBinding().get();
            Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
            String operationName = dbbindingConfigPanel.getOperation();
            for (BindingOperation bop : bindingOperations) {
                String localOpName = bop.getName();
                if (localOpName.equals(getOperationName())) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<JDBCOperationInput> jdbcOperationInputs = bi
                                .getExtensibilityElements(JDBCOperationInput.class);
                        if (jdbcOperationInputs.size() > 0) {
                            JDBCOperationInput jdbcOperationInput = jdbcOperationInputs.get(0);
                            commitInputMessage(jdbcOperationInput);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
            if (wsdlModel.isIntransaction()) {
                wsdlModel.endTransaction();
            }
            return true;
        }
    }

    public boolean commitBinding(JDBCBinding jdbcBinding) {

        WSDLModel wsdlModel = jdbcBinding.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            JDBCAddress jdbcAddress = new JDBCAddressImpl(wsdlModel);
            jdbcAddress.setJDBCURL(dbbindingConfigPanel.getJNDIName());

            Binding binding = (Binding) jdbcBinding.getParent();

            Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
            String operationName = dbbindingConfigPanel.getOperation();
            for (BindingOperation bop : bindingOperations) {
                if (bop.equals((BindingOperation) getOperation())) {
                    BindingInput bi = bop.getBindingInput();
                    if (bi != null) {
                        List<JDBCOperationInput> jdbcOperationInputs = bi
                                .getExtensibilityElements(JDBCOperationInput.class);
                        if (jdbcOperationInputs.size() > 0) {
                            JDBCOperationInput jdbcOperationInput = jdbcOperationInputs.get(0);
                            commitInputMessage(jdbcOperationInput);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
            if (wsdlModel.isIntransaction()) {
                wsdlModel.endTransaction();
            }
            return true;
        }
    }

    private boolean commitInputMessage(JDBCOperationInput jdbcOpInput) {
        jdbcOpInput.setOperationType(dbbindingConfigPanel.getOperation());
        jdbcOpInput.setMarkColumnName(dbbindingConfigPanel.getMarkColumnName());
        jdbcOpInput.setMarkColumnValue(dbbindingConfigPanel.getMarkColumnValue());
        jdbcOpInput.setMoveRowToTableName(dbbindingConfigPanel.getMoveRowToTableName());
        jdbcOpInput.setNumberOfRecords(dbbindingConfigPanel.getNoOfRecords());
        jdbcOpInput.setPKName(dbbindingConfigPanel.getPKName());
        jdbcOpInput.setPollingPostProcessing(dbbindingConfigPanel.getPollingPostProcessing());
        jdbcOpInput.setTableName(dbbindingConfigPanel.getTableName());
        jdbcOpInput.setTransaction(dbbindingConfigPanel.getTransaction());
        jdbcOpInput.setSql(dbbindingConfigPanel.getSQLStatement());
        jdbcOpInput.setParamOrder(dbbindingConfigPanel.getParamOrder());
        return true;
    }

    private boolean commitPort(Port port) {
        Collection<JDBCAddress> address = port.getExtensibilityElements(JDBCAddress.class);
        JDBCAddress jdbcAddress = address.iterator().next();
        return commitAddress(jdbcAddress);
    }

    private boolean commitOperation(JDBCOperation jdbcOperation) {
        Object obj = jdbcOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
            Collection<JDBCBinding> bindings = parentBinding.getExtensibilityElements(JDBCBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    public boolean rollback() {
        return true;
    }

    public boolean isValid() {
        return true;
    }

    public void setOperation(Operation op) {
        this.operation = op;
        dbbindingConfigPanel.setOperation(op.getName());
    }

    public Operation getOperation() {
        return this.operation;
    }

    public String getOperationName() {
        return this.operation.getName();
    }
}
