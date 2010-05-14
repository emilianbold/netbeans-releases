/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.soa.pojo.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.pojo.model.api.PortTypeMetadata;
import org.netbeans.modules.soa.pojo.model.api.WSDLMetadata;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.modules.soa.pojo.util.WSDLUtil;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class POJOProviderAdvancedPanel extends JPanel {
    private JDialog mParent = null;
    private OperationMethodChooserPanel mPOJOPanel = null;
    private Map mOldValues = new HashMap();
    private Properties mDefaultValues = new Properties();
    private List listeners = new ArrayList();
    private WizardDescriptor mWizard = null;
    /** Creates new form POJOProviderVisualPanel2 */
    public POJOProviderAdvancedPanel() {
        initComponents();
        if ( this.jBoolDefaultValues.isSelected() ) {
            disableWSDLExtract();
        }
    }

    void setPOJOPanel(OperationMethodChooserPanel mPOJOPanel) {
        this.mPOJOPanel = mPOJOPanel;
    }
    void setWizDesc(WizardDescriptor mWizard) {
        this.mWizard = mWizard;
    }

    private void disableWSDLExtract() {
    }

    private void enableWSDLExtract() {
    }


    public void addChangeListener(ChangeListener cl) {
        listeners.add(cl);
    }
    public void removeChangeListener(ChangeListener arg0) {
        listeners.remove(arg0);
    }

    private void fireChange()
    {
        ChangeEvent e = new ChangeEvent(this);
        for(Iterator it = listeners.iterator(); it.hasNext(); ((ChangeListener)it.next()).stateChanged(e));
    }


    @Override
    public String getName() {
        return "Configure Default Values";
    }

    public void setParent(JDialog dialog) {
        mParent = dialog;
    }

    public void rollbackUserEntries() {
            this.txtEndpointName.setText((String)this.mOldValues.get(GeneratorUtil.POJO_ENDPOINT_NAME));
            this.txtInterfaceName.setText((String)mOldValues.get(GeneratorUtil.POJO_INTERFACE_NAME));
            this.txtInterfaceNs.setText((String)mOldValues.get(GeneratorUtil.POJO_INTERFACE_NS));
            this.txtOutMsgType.setText((String)mOldValues.get(GeneratorUtil.POJO_OUTPUT_TYPE));
            this.txtOutMsgTypeNs.setText((String)mOldValues.get(GeneratorUtil.POJO_OUTMSG_TYPE_NS));
            this.txtServiceName.setText((String)mOldValues.get(GeneratorUtil.POJO_SERVICE_NAME));
            this.txtServiceNs.setText((String)mOldValues.get(GeneratorUtil.POJO_SERVICE_NS));
             Boolean isChecked =(Boolean)mOldValues.get(GeneratorUtil.POJO_USE_DEFAULT);
            if (isChecked != null) {
                if (isChecked.equals(Boolean.TRUE) ){
                    this.jBoolDefaultValues.setSelected(true);
                } else {
                    this.jBoolDefaultValues.setSelected(false);
                }
            } else {
                    this.jBoolDefaultValues.setSelected(true);
            }

    }

     public void rollbackUserEntriesToDefault() {
            if (this.mDefaultValues.containsKey(GeneratorUtil.POJO_ENDPOINT_NAME)  ) {
                //this.setEndpointName( );
                this.txtEndpointName.setText(this.mDefaultValues.getProperty(GeneratorUtil.POJO_ENDPOINT_NAME));
                this.txtInterfaceName.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_INTERFACE_NAME));
                this.txtInterfaceNs.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_INTERFACE_NS));
                this.txtOutMsgType.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_OUTPUT_TYPE));
                this.txtOutMsgTypeNs.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS));
                this.txtServiceName.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_SERVICE_NAME));
                this.txtServiceNs.setText(mDefaultValues.getProperty(GeneratorUtil.POJO_SERVICE_NS));
            }

    }

    public void saveUserEntries() {
        mOldValues.put(GeneratorUtil.POJO_ENDPOINT_NAME, this.getEndpointName());
        mOldValues.put(GeneratorUtil.POJO_INTERFACE_NAME, this.getInterfaceName());
        mOldValues.put(GeneratorUtil.POJO_INTERFACE_NS, this.getInterfaceNameNS());
        mOldValues.put(GeneratorUtil.POJO_OUTPUT_TYPE, this.getOutMessageType());
        mOldValues.put(GeneratorUtil.POJO_OUTMSG_TYPE_NS, this.getOutMessageTypeNS());
        mOldValues.put(GeneratorUtil.POJO_SERVICE_NAME, this.getServiceName());
        mOldValues.put(GeneratorUtil.POJO_SERVICE_NS, this.getServiceNameNS());
        mOldValues.put(GeneratorUtil.POJO_USE_DEFAULT, Boolean.valueOf(this.jBoolDefaultValues.isSelected()));

        if (this.mPOJOPanel != null &&  this.getOutMessageType().equals("") ||  this.getOutMessageTypeNS().equals("")) {
            mPOJOPanel.setOutputType(POJOSupportedDataTypes.Void.toString());
        } else {
            mPOJOPanel.enableOutputType();
        }
    }

    public String getEndpointName() {
        return this.txtEndpointName.getText();
    }

    public boolean useDefaultValues() {
        return this.jBoolDefaultValues.isSelected();
    }


    public void setDefaultValues(boolean bool) {
        this.jBoolDefaultValues.setSelected(bool);
    }
    public void setEndpointName( String data) {
        mOldValues.put(GeneratorUtil.POJO_ENDPOINT_NAME, data);
        this.mDefaultValues.put(GeneratorUtil.POJO_ENDPOINT_NAME, data);
        this.txtEndpointName.setText(data);

    }

    public String getInterfaceName() {
        return this.txtInterfaceName.getText();
    }
    public void setInterfaceName( String data) {
        mOldValues.put(GeneratorUtil.POJO_INTERFACE_NAME, data);
        this.mDefaultValues.put(GeneratorUtil.POJO_INTERFACE_NAME, data);
        this.txtInterfaceName.setText(data);
    }

    public String getInterfaceNameNS() {
        return this.txtInterfaceNs.getText();
    }
    public void setInterfaceNameNS( String data) {
        mOldValues.put(GeneratorUtil.POJO_INTERFACE_NS, data);
        mDefaultValues.put(GeneratorUtil.POJO_INTERFACE_NS, data);
        this.txtInterfaceNs.setText(data);
    }

    public String getServiceName() {
        return this.txtServiceName.getText();
    }
    public void setServiceName( String data) {
        mOldValues.put(GeneratorUtil.POJO_SERVICE_NAME, data);
        mDefaultValues.put(GeneratorUtil.POJO_SERVICE_NAME, data);
        this.txtServiceName.setText(data);
    }

    public String getServiceNameNS() {
        return this.txtServiceNs.getText();
    }
    public void setServiceNameNS( String data) {
        mOldValues.put(GeneratorUtil.POJO_SERVICE_NS, data);
        mDefaultValues.put(GeneratorUtil.POJO_SERVICE_NS, data);
        this.txtServiceNs.setText(data);
    }

    public String getOutMessageType() {
        return this.txtOutMsgType.getText();
    }

    public String getOutMessageTypeNS() {
        return this.txtOutMsgTypeNs.getText();
    }


    public void setOutMessageType(String data) {
        mOldValues.put(GeneratorUtil.POJO_OUTPUT_TYPE, data);
        mDefaultValues.put(GeneratorUtil.POJO_OUTPUT_TYPE, data);
        this.txtOutMsgType.setText(data);
    }

    public void setOutMessageTypeNS(String data) {
        mOldValues.put(GeneratorUtil.POJO_OUTMSG_TYPE_NS, data);
        mDefaultValues.put(GeneratorUtil.POJO_OUTMSG_TYPE_NS, data);
        this.txtOutMsgTypeNs.setText(data);
    }



    public boolean isInitialized() {
        return !this.mOldValues.isEmpty();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtEndpointName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtInterfaceName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtInterfaceNs = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtServiceName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtServiceNs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtOutMsgType = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtOutMsgTypeNs = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jBoolDefaultValues = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jOkButton = new javax.swing.JButton();
        jCancelBtn = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(500, 415));
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        jLabel1.setLabelFor(txtEndpointName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel1.toolTipText")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        txtEndpointName.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtEndpointName.text")); // NOI18N
        txtEndpointName.setName("txtEndpointName"); // NOI18N
        txtEndpointName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEndpointNameActionPerformed(evt);
            }
        });
        txtEndpointName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtEndpointNameFocusGained(evt);
            }
        });
        txtEndpointName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtEndpointNameKeyReleased(evt);
            }
        });

        jLabel2.setLabelFor(txtInterfaceName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel2.toolTipText")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtInterfaceName.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtInterfaceName.text")); // NOI18N
        txtInterfaceName.setName("txtInterfaceName"); // NOI18N
        txtInterfaceName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtInterfaceNameFocusGained(evt);
            }
        });
        txtInterfaceName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtInterfaceNameKeyReleased(evt);
            }
        });

        jLabel3.setLabelFor(txtInterfaceNs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel3.toolTipText")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtInterfaceNs.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtInterfaceNs.text")); // NOI18N
        txtInterfaceNs.setName("txtInterfaceNs"); // NOI18N
        txtInterfaceNs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtInterfaceNsFocusGained(evt);
            }
        });
        txtInterfaceNs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtInterfaceNsKeyReleased(evt);
            }
        });

        jLabel4.setLabelFor(txtServiceName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel4.toolTipText")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        txtServiceName.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtServiceName.text")); // NOI18N
        txtServiceName.setName("txtServiceName"); // NOI18N
        txtServiceName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtServiceNameFocusGained(evt);
            }
        });
        txtServiceName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtServiceNameKeyReleased(evt);
            }
        });

        jLabel5.setLabelFor(txtServiceNs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel5.toolTipText")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        txtServiceNs.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtServiceNs.text")); // NOI18N
        txtServiceNs.setName("txtServiceNs"); // NOI18N
        txtServiceNs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtServiceNsFocusGained(evt);
            }
        });
        txtServiceNs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtServiceNsKeyReleased(evt);
            }
        });

        jLabel6.setLabelFor(txtOutMsgType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel6.toolTipText")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        txtOutMsgType.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtOutMsgType.text")); // NOI18N
        txtOutMsgType.setName("txtOutMsgType"); // NOI18N
        txtOutMsgType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtOutMsgTypeFocusGained(evt);
            }
        });
        txtOutMsgType.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtOutMsgTypeKeyReleased(evt);
            }
        });

        jLabel7.setLabelFor(txtOutMsgTypeNs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel7.toolTipText")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        txtOutMsgTypeNs.setText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.txtOutMsgTypeNs.text")); // NOI18N
        txtOutMsgTypeNs.setName("txtOutMsgTypeNs"); // NOI18N
        txtOutMsgTypeNs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtOutMsgTypeNsFocusGained(evt);
            }
        });
        txtOutMsgTypeNs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtOutMsgTypeNsKeyReleased(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "TT_AdvProvider_Help")); // NOI18N
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setFocusable(false);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jTextArea1.setOpaque(false);
        jTextArea1.setRequestFocusEnabled(false);
        jTextArea1.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(jTextArea1);

        jBoolDefaultValues.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jBoolDefaultValues, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jBoolDefaultValues.text")); // NOI18N
        jBoolDefaultValues.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "TT_ProviderAdv_UseDefaults")); // NOI18N
        jBoolDefaultValues.setName("jBoolDefaultValues"); // NOI18N
        jBoolDefaultValues.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jBoolDefaultValuesItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jOkButton, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jOkButton.text")); // NOI18N
        jOkButton.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "TT_AdvProvider_Ok")); // NOI18N
        jOkButton.setName("jOkButton"); // NOI18N
        jOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOkButtonActionPerformed(evt);
            }
        });
        jPanel1.add(jOkButton);

        org.openide.awt.Mnemonics.setLocalizedText(jCancelBtn, org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "POJOProviderAdvancedPanel.jCancelBtn.text")); // NOI18N
        jCancelBtn.setToolTipText(org.openide.util.NbBundle.getMessage(POJOProviderAdvancedPanel.class, "TT_ProviderAdv_Cancel")); // NOI18N
        jCancelBtn.setName("jCancelBtn"); // NOI18N
        jCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelBtnActionPerformed(evt);
            }
        });
        jPanel1.add(jCancelBtn);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel8)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .add(jBoolDefaultValues)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel1)
                                    .add(jLabel6)
                                    .add(jLabel5)
                                    .add(jLabel4)
                                    .add(jLabel3)
                                    .add(jLabel2)
                                    .add(jLabel7))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(txtInterfaceNs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                            .add(txtInterfaceName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                            .add(txtServiceName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                            .add(txtOutMsgTypeNs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                            .add(txtEndpointName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                            .add(txtOutMsgType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(txtServiceNs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)))
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))))
                .add(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel8)
                .add(18, 18, 18)
                .add(jBoolDefaultValues)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtEndpointName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtInterfaceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(4, 4, 4)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtInterfaceNs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(4, 4, 4)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtServiceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtOutMsgType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtOutMsgTypeNs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtServiceNs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void txtEndpointNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEndpointNameActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtEndpointNameActionPerformed

private void txtEndpointNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEndpointNameFocusGained
// TODO add your handling code here:
   this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Endpoint_Info"));
}//GEN-LAST:event_txtEndpointNameFocusGained

private void txtInterfaceNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInterfaceNameFocusGained
// TODO add your handling code here:
    this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Interface_Name_Info"));
}//GEN-LAST:event_txtInterfaceNameFocusGained

private void txtInterfaceNsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInterfaceNsFocusGained
// TODO add your handling code here:
    this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Interface_NS_Info"));
}//GEN-LAST:event_txtInterfaceNsFocusGained

private void txtServiceNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtServiceNameFocusGained
// TODO add your handling code here:
    this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Service_Name_Info"));
}//GEN-LAST:event_txtServiceNameFocusGained

private void txtServiceNsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtServiceNsFocusGained
// TODO add your handling code here:
this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Service_NS_Info"));
}//GEN-LAST:event_txtServiceNsFocusGained

private void txtOutMsgTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtOutMsgTypeFocusGained
// TODO add your handling code here: LBL_OutputType_Name_Info
this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_OutputType_Name_Info"));
}//GEN-LAST:event_txtOutMsgTypeFocusGained

private void txtOutMsgTypeNsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtOutMsgTypeNsFocusGained
// TODO add your handling code here:
this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_OutputType_NS_Info"));
}//GEN-LAST:event_txtOutMsgTypeNsFocusGained

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
// TODO add your handling code here:
}//GEN-LAST:event_formFocusGained

void disableDefault(boolean bool) {
    this.jBoolDefaultValues.setSelected(!bool);
    this.jBoolDefaultValues.setVisible(!bool);
}
public void disableEdit(boolean bool) {
        if ( bool ) {
            this.rollbackUserEntriesToDefault();
        }
        this.txtEndpointName.setEditable( !bool);
        this.txtInterfaceName.setEnabled(!bool);
       // this.jCbExtractFromWSDL.setSelected(false);
      //  this.txtEndpointName.setOpaque(bool);

        this.txtInterfaceName.setEditable(!bool);
        this.txtInterfaceName.setEnabled(!bool);
        //this.txtInterfaceName.setOpaque(bool);

        this.txtInterfaceNs.setEditable(!bool);
        this.txtInterfaceNs.setEnabled(!bool);
        //this.txtInterfaceNs.setOpaque(bool);

        this.txtOutMsgType.setEditable(!bool);
        this.txtOutMsgType.setEnabled(!bool);
        //this.txtOutMsgType.setOpaque(bool);

        this.txtOutMsgTypeNs.setEditable(!bool);
        this.txtOutMsgTypeNs.setEnabled(!bool);
        //this.txtOutMsgTypeNs.setOpaque(bool);

        this.txtServiceName.setEditable(!bool);
        this.txtServiceName.setEnabled(!bool);
        //this.txtServiceName.setOpaque(bool);

        this.txtServiceNs.setEditable(!bool);
        this.txtServiceNs.setEnabled(!bool);
        //this.txtServiceNs.setOpaque(bool);

        this.updateUI();

}

private void jBoolDefaultValuesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jBoolDefaultValuesItemStateChanged
// TODO add your handling code here:
    if ( evt.getStateChange() == evt.SELECTED) {
        this.disableWSDLExtract();
        disableEdit(true);
      //  this.mOldValues.put(GeneratorUtil.POJO_USE_DEFAULT, Boolean.TRUE);
    }else {
        disableEdit(false);
        this.mOldValues.put(GeneratorUtil.POJO_USE_DEFAULT, Boolean.FALSE);
    }
}//GEN-LAST:event_jBoolDefaultValuesItemStateChanged

private void jOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOkButtonActionPerformed
// TODO add your handling code here:
    this.saveUserEntries();
    this.mParent.setVisible(false);
}//GEN-LAST:event_jOkButtonActionPerformed

private void jCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelBtnActionPerformed
// TODO add your handling code here:
   this.rollbackUserEntries();
   this.mParent.setVisible(false);
}//GEN-LAST:event_jCancelBtnActionPerformed

private void populateWSDLRelatedData(WSDLUtil wsdlUtil, WSDLModel wsdlModel) {
        WSDLMetadata wsdlMetadata = wsdlUtil.getInterfaceNames(wsdlModel);
        List<PortTypeMetadata> ptList = wsdlMetadata.getPortTypeMetadaList();

}

private void jCmbWSDLOpnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbWSDLOpnFocusGained
// TODO add your handling code here:
this.jTextArea1.setText(NbBundle.getMessage (POJOProviderAdvancedPanel.class, "LBL_Select_Operation_From_Combo"));//GEN-LAST:event_jCmbWSDLOpnFocusGained

}
private void txtEndpointNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEndpointNameKeyReleased
// TODO add your handling code here:
    fireChange();
}//GEN-LAST:event_txtEndpointNameKeyReleased

private void txtInterfaceNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInterfaceNameKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtInterfaceNameKeyReleased

private void txtServiceNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtServiceNameKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtServiceNameKeyReleased

private void txtInterfaceNsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInterfaceNsKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtInterfaceNsKeyReleased

private void txtServiceNsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtServiceNsKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtServiceNsKeyReleased

private void txtOutMsgTypeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOutMsgTypeKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtOutMsgTypeKeyReleased

private void txtOutMsgTypeNsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtOutMsgTypeNsKeyReleased
// TODO add your handling code here:
     fireChange();
}//GEN-LAST:event_txtOutMsgTypeNsKeyReleased
private void jCmbWSDLOpnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCmbWSDLOpnItemStateChanged
// TODO add your handling code here://GEN-LAST:event_jCmbWSDLOpnItemStateChanged
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jBoolDefaultValues;
    private javax.swing.JButton jCancelBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JButton jOkButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField txtEndpointName;
    private javax.swing.JTextField txtInterfaceName;
    private javax.swing.JTextField txtInterfaceNs;
    private javax.swing.JTextField txtOutMsgType;
    private javax.swing.JTextField txtOutMsgTypeNs;
    private javax.swing.JTextField txtServiceName;
    private javax.swing.JTextField txtServiceNs;
    // End of variables declaration//GEN-END:variables
}

