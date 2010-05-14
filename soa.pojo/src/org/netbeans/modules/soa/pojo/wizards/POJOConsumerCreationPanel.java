/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Dimension;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.swing.ComboBoxEditor;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.model.api.OperationMetadata;
import org.netbeans.modules.soa.pojo.model.api.PortTypeMetadata;
import org.netbeans.modules.soa.pojo.model.api.WSDLMetadata;
import org.netbeans.modules.soa.pojo.util.ExtensionFileFilter;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.modules.soa.pojo.util.WSDLUtil;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class POJOConsumerCreationPanel extends JPanel {

    private WizardDescriptor mWizDesc = null;
    private WSDLModel mWsdlModel = null;
    private POJOConsumerPalleteAdvancedPanel mAdvancedPanel;
    private Map<String, ExecutableElement> mMapMethSignToExecElem = null;
    private List listeners = new ArrayList();
    

    /** Creates new form POJOConsumerPalleteVisualPanel1 */
    public POJOConsumerCreationPanel() {
        initComponents();
        // Due to JCombo bug - start.

        jCmbIntfName.getEditor().getEditorComponent().addFocusListener( new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbIntfNameFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                jCmbIntfNameFocusLost(evt);
            }
        });

        jCmbOpnName.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbOpnNameFocusGained(evt);
            }
        });

        // - end.
        this.jCmbInvkRetType.setModel(new javax.swing.DefaultComboBoxModel(GeneratorUtil.POJO_OUT_TYPES));
        this.jcmbInvokeInputType.setModel(new javax.swing.DefaultComboBoxModel(GeneratorUtil.POJO_IN_TYPES));
        this.enableWSDLBased(false);
        this.jBtnAdvanced.setVisible(false);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_POJO_Binding_Consumer_Name");
    }

    public void setWizardDescriptor(WizardDescriptor wiz) {
        this.mWizDesc = wiz;
        setExistingMethods((Map<String, ExecutableElement>) wiz.getProperty(GeneratorUtil.POJO_GET_METHOD_LIST));
        ExecutableElement selectedMethod = (ExecutableElement) wiz.getProperty(GeneratorUtil.POJO_SELECTED_METHOD);
        if (selectedMethod != null) {
            this.jCmbInvokeFromMethod.setSelectedItem(selectedMethod.toString());
        }

        Boolean boolObj = (Boolean) mWizDesc.getProperty(GeneratorUtil.HIDE_ADVANCED);
        if (boolObj != null) {
            this.jBtnAdvanced.setVisible(!boolObj.booleanValue());
        }

        Boolean bInvokeType = (Boolean) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_INVOKE_TYPE);
        if (bInvokeType != null) {
            this.jCheckSynch.setSelected(bInvokeType == Boolean.TRUE);
        }

        POJOSupportedDataTypes inType = (POJOSupportedDataTypes) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE);
        POJOSupportedDataTypes outType = (POJOSupportedDataTypes) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE);
        if (inType != null) {
            this.jcmbInvokeInputType.setSelectedItem(inType.toString());
        }

        if (outType != null) {
            this.jCmbInvkRetType.setSelectedItem(inType.toString());
        }

        String interfaceName = (String) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NAME);
        String interfaceNameNs = (String) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NS);

        String opnName = (String) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_OPERATION_NAME);

        if (opnName != null) {
            this.jCmbOpnName.addItem(opnName);
            this.jCmbOpnName.setSelectedItem(opnName);
        }

        if (interfaceName != null) {

            this.jCmbIntfName.addItem(interfaceName);
            this.jCmbIntfName.setSelectedItem(interfaceName);

            this.jTxtIntfNS.setText(interfaceNameNs);
            this.jTxtOpnNS.setText(interfaceNameNs);
        }

        Boolean bConsumerDrop = (Boolean) mWizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_DROP);
        if (bConsumerDrop != null && bConsumerDrop.equals(Boolean.TRUE)) {
            this.disableWSDL(true);
            this.jCmbIntfName.setEditable(false);
            this.jCmbOpnName.setEditable(false);
            this.jTxtIntfNS.setText(interfaceNameNs);
            this.jTxtOpnNS.setText(interfaceNameNs);
            this.jBtnAdvanced.setVisible(false);
            this.jBtnBrowse.setEnabled(false);
            this.jRdBtWsdlFromFile.setEnabled(false);
            this.jRdBtWsdlFromURL.setEnabled(false);
            this.jTxtWSDLFileLoc.setEditable(false);
            this.jTxtWSDLFileLocURL.setEditable(false);
            this.jTxtWSDLFileLoc.setEditable(false);

        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jcbConsumerFromWSDL = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jRdBtWsdlFromFile = new javax.swing.JRadioButton();
        jTxtWSDLFileLoc = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jRdBtWsdlFromURL = new javax.swing.JRadioButton();
        jTxtWSDLFileLocURL = new javax.swing.JTextField();
        lblInterfaceName = new javax.swing.JLabel();
        jCmbIntfName = new javax.swing.JComboBox();
        lblInterfaceNs = new javax.swing.JLabel();
        jTxtIntfNS = new javax.swing.JTextField();
        lblOpName = new javax.swing.JLabel();
        jCmbOpnName = new javax.swing.JComboBox();
        lblOpNameNs = new javax.swing.JLabel();
        jTxtOpnNS = new javax.swing.JTextField();
        lblInvokePattern = new javax.swing.JLabel();
        jCheckSynch = new javax.swing.JRadioButton();
        jCheckAsynch = new javax.swing.JRadioButton();
        lblInvokeInputType = new javax.swing.JLabel();
        jcmbInvokeInputType = new javax.swing.JComboBox();
        lblInvokeReturnType = new javax.swing.JLabel();
        jCmbInvkRetType = new javax.swing.JComboBox();
        lblInvokeMethod = new javax.swing.JLabel();
        jCmbInvokeFromMethod = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jDesc = new javax.swing.JTextArea();
        jBtnAdvanced = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jcbConsumerFromWSDL, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jcbConsumerFromWSDL.text")); // NOI18N
        jcbConsumerFromWSDL.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_chkbx_use_wsdl")); // NOI18N
        jcbConsumerFromWSDL.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jcbConsumerFromWSDL.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jcbConsumerFromWSDLItemStateChanged(evt);
            }
        });
        jcbConsumerFromWSDL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbConsumerFromWSDLActionPerformed(evt);
            }
        });
        jcbConsumerFromWSDL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jcbConsumerFromWSDLFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jcbConsumerFromWSDL, gridBagConstraints);

        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel1MouseMoved(evt);
            }
        });
        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });
        jPanel1.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(jRdBtWsdlFromFile);
        jRdBtWsdlFromFile.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRdBtWsdlFromFile, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jRdBtWsdlFromFile.text")); // NOI18N
        jRdBtWsdlFromFile.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Select_WSDL_from_File")); // NOI18N
        jRdBtWsdlFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRdBtWsdlFromFileActionPerformed(evt);
            }
        });
        jRdBtWsdlFromFile.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jRdBtWsdlFromFileFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jRdBtWsdlFromFile, gridBagConstraints);

        jTxtWSDLFileLoc.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Select_WSDL_from_File")); // NOI18N
        jTxtWSDLFileLoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtWSDLFileLocFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTxtWSDLFileLocFocusLost(evt);
            }
        });
        jTxtWSDLFileLoc.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                jTxtWSDLFileLocCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTxtWSDLFileLocInputMethodTextChanged(evt);
            }
        });
        jTxtWSDLFileLoc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTxtWSDLFileLocKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jTxtWSDLFileLoc, gridBagConstraints);
        jTxtWSDLFileLoc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_txt_WSDL_File")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jBtnBrowse.text")); // NOI18N
        jBtnBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_browsr_select_WSDL")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });
        jBtnBrowse.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jBtnBrowseFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanel1.add(jBtnBrowse, gridBagConstraints);

        buttonGroup1.add(jRdBtWsdlFromURL);
        org.openide.awt.Mnemonics.setLocalizedText(jRdBtWsdlFromURL, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jRdBtWsdlFromURL.text")); // NOI18N
        jRdBtWsdlFromURL.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_SelectWSDL_From_URL")); // NOI18N
        jRdBtWsdlFromURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRdBtWsdlFromURLActionPerformed(evt);
            }
        });
        jRdBtWsdlFromURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jRdBtWsdlFromURLFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jRdBtWsdlFromURL, gridBagConstraints);

        jTxtWSDLFileLocURL.setEditable(false);
        jTxtWSDLFileLocURL.setText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jTxtWSDLFileLocURL.text")); // NOI18N
        jTxtWSDLFileLocURL.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_SelectWSDL_From_URL")); // NOI18N
        jTxtWSDLFileLocURL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtWSDLFileLocURLFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTxtWSDLFileLocURLFocusLost(evt);
            }
        });
        jTxtWSDLFileLocURL.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTxtWSDLFileLocURLInputMethodTextChanged(evt);
            }
        });
        jTxtWSDLFileLocURL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTxtWSDLFileLocURLKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jTxtWSDLFileLocURL, gridBagConstraints);
        jTxtWSDLFileLocURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_TXT_WSDL_URL")); // NOI18N

        lblInterfaceName.setLabelFor(jCmbIntfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblInterfaceName, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInterfaceName.text")); // NOI18N
        lblInterfaceName.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "tt_interface_name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInterfaceName, gridBagConstraints);

        jCmbIntfName.setEditable(true);
        jCmbIntfName.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "tt_interface_name")); // NOI18N
        jCmbIntfName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCmbIntfNameActionPerformed(evt);
            }
        });
        jCmbIntfName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbIntfNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jCmbIntfNameFocusLost(evt);
            }
        });
        jCmbIntfName.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jCmbIntfNameInputMethodTextChanged(evt);
            }
        });
        jCmbIntfName.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jCmbIntfNamePropertyChange(evt);
            }
        });
        jCmbIntfName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jCmbIntfNameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jCmbIntfNameKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jCmbIntfNameKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCmbIntfName, gridBagConstraints);
        jCmbIntfName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_Interface_Name")); // NOI18N

        lblInterfaceNs.setLabelFor(jTxtIntfNS);
        org.openide.awt.Mnemonics.setLocalizedText(lblInterfaceNs, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInterfaceNs.text")); // NOI18N
        lblInterfaceNs.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_interface_ns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInterfaceNs, gridBagConstraints);

        jTxtIntfNS.setText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jTxtIntfNS.text")); // NOI18N
        jTxtIntfNS.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtIntfNSFocusGained(evt);
            }
        });
        jTxtIntfNS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTxtIntfNSKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtIntfNSKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtIntfNSKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jTxtIntfNS, gridBagConstraints);

        lblOpName.setLabelFor(jCmbOpnName);
        org.openide.awt.Mnemonics.setLocalizedText(lblOpName, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblOpName.text")); // NOI18N
        lblOpName.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Oper")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblOpName, gridBagConstraints);

        jCmbOpnName.setEditable(true);
        jCmbOpnName.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Oper")); // NOI18N
        jCmbOpnName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCmbOpnNameActionPerformed(evt);
            }
        });
        jCmbOpnName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbOpnNameFocusGained(evt);
            }
        });
        jCmbOpnName.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jCmbOpnNameInputMethodTextChanged(evt);
            }
        });
        jCmbOpnName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jCmbOpnNameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jCmbOpnNameKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jCmbOpnNameKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCmbOpnName, gridBagConstraints);

        lblOpNameNs.setLabelFor(jTxtOpnNS);
        org.openide.awt.Mnemonics.setLocalizedText(lblOpNameNs, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblOpNameNs.text")); // NOI18N
        lblOpNameNs.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_OpNS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblOpNameNs, gridBagConstraints);

        jTxtOpnNS.setText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jTxtOpnNS.text")); // NOI18N
        jTxtOpnNS.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_OpNS")); // NOI18N
        jTxtOpnNS.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTxtOpnNSFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTxtOpnNSFocusLost(evt);
            }
        });
        jTxtOpnNS.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTxtOpnNSInputMethodTextChanged(evt);
            }
        });
        jTxtOpnNS.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTxtOpnNSPropertyChange(evt);
            }
        });
        jTxtOpnNS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTxtOpnNSKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtOpnNSKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtOpnNSKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jTxtOpnNS, gridBagConstraints);

        lblInvokePattern.setLabelFor(jCheckSynch);
        org.openide.awt.Mnemonics.setLocalizedText(lblInvokePattern, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInvokePattern.text")); // NOI18N
        lblInvokePattern.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_invoke_pattern")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInvokePattern, gridBagConstraints);

        buttonGroup2.add(jCheckSynch);
        jCheckSynch.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckSynch, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jCheckSynch.text")); // NOI18N
        jCheckSynch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCheckSynchFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCheckSynch, gridBagConstraints);

        buttonGroup2.add(jCheckAsynch);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckAsynch, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jCheckAsynch.text")); // NOI18N
        jCheckAsynch.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_rbtn_Asynch")); // NOI18N
        jCheckAsynch.setEnabled(false);
        jCheckAsynch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCheckAsynchFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCheckAsynch, gridBagConstraints);

        lblInvokeInputType.setLabelFor(jcmbInvokeInputType);
        org.openide.awt.Mnemonics.setLocalizedText(lblInvokeInputType, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInvokeInputType.text")); // NOI18N
        lblInvokeInputType.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_inputType_java")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInvokeInputType, gridBagConstraints);

        jcmbInvokeInputType.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_inputType_java")); // NOI18N
        jcmbInvokeInputType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jcmbInvokeInputTypeItemStateChanged(evt);
            }
        });
        jcmbInvokeInputType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jcmbInvokeInputTypeFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jcmbInvokeInputType, gridBagConstraints);

        lblInvokeReturnType.setLabelFor(jCmbInvkRetType);
        org.openide.awt.Mnemonics.setLocalizedText(lblInvokeReturnType, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInvokeReturnType.text")); // NOI18N
        lblInvokeReturnType.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_invoke_return_type_java")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInvokeReturnType, gridBagConstraints);

        jCmbInvkRetType.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_invoke_return_type_java")); // NOI18N
        jCmbInvkRetType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbInvkRetTypeFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCmbInvkRetType, gridBagConstraints);

        lblInvokeMethod.setLabelFor(jCmbInvokeFromMethod);
        org.openide.awt.Mnemonics.setLocalizedText(lblInvokeMethod, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.lblInvokeMethod.text")); // NOI18N
        lblInvokeMethod.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Invoke_from_method")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(lblInvokeMethod, gridBagConstraints);

        jCmbInvokeFromMethod.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_Invoke_from_method")); // NOI18N
        jCmbInvokeFromMethod.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCmbInvokeFromMethodFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jCmbInvokeFromMethod, gridBagConstraints);

        jSeparator1.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "tt_simple_separator")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jSeparator1, gridBagConstraints);
        jSeparator1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jSeparator1.AccessibleContext.accessibleName")); // NOI18N
        jSeparator1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jSeparator1.AccessibleContext.accessibleDescription")); // NOI18N

        jDesc.setColumns(20);
        jDesc.setEditable(false);
        jDesc.setLineWrap(true);
        jDesc.setRows(5);
        jDesc.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_helptext_area")); // NOI18N
        jDesc.setWrapStyleWord(true);
        jDesc.setOpaque(false);
        jScrollPane1.setViewportView(jDesc);
        jDesc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_helptext_area")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_helptext_scrollpane")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_helptext_scrollpane")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnAdvanced, org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "POJOConsumerCreationPanel.jBtnAdvanced.text")); // NOI18N
        jBtnAdvanced.setToolTipText(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "TT_btn_consumer_advance")); // NOI18N
        jBtnAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAdvancedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel1.add(jBtnAdvanced, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_consumer_panel")); // NOI18N
        jPanel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AD_consumer_panel")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AN_Consumer_Form")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(POJOConsumerCreationPanel.class, "AD_Consumer_Form")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jBtnAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAdvancedActionPerformed
    if (mAdvancedPanel == null) {
        mAdvancedPanel = new POJOConsumerPalleteAdvancedPanel();
    }

    if (mWizDesc != null) {
        if (mAdvancedPanel.useDefaultValues()) {
            setAdvancedDefaultOptions();
            mAdvancedPanel.enableEdit(false);
        }
    }

    final POJOConsumerPalleteAdvancedPanel advncPanel = mAdvancedPanel;
    final POJOConsumerCreationPanel thisPanel = this;
    java.awt.EventQueue.invokeLater(new Runnable() {

        public void run() {
            /*                if (! advncPanel.isSaved()) {
            advncPanel.rollbackUserEntries();
            }
             */

            advncPanel.setPreferredSize(thisPanel.getPreferredSize());
            advncPanel.setMaximumSize(thisPanel.getMaximumSize());
            advncPanel.setMinimumSize(thisPanel.getMinimumSize());


            JDialog dialog = new JDialog();
            dialog.setLocation(thisPanel.getX(), thisPanel.getY());
            Dimension dim = new Dimension();
            dim.setSize(thisPanel.getPreferredSize().getWidth(), thisPanel.getPreferredSize().getHeight());
            dialog.setPreferredSize(dim);
            dim = new Dimension();
            dim.setSize(thisPanel.getMinimumSize().getWidth(), thisPanel.getMinimumSize().getHeight());
            dialog.setMinimumSize(dim);
            dim = new Dimension();
            dim.setSize(thisPanel.getMaximumSize().getWidth(), thisPanel.getMaximumSize().getHeight());
            dialog.setMaximumSize(dim);
            dialog.setAlwaysOnTop(true);
            dialog.setResizable(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(thisPanel.getSize());
            dialog.getContentPane().add(advncPanel);
            dialog.setModal(true);
            dialog.enableInputMethods(true);
            dialog.setLocationRelativeTo(null);
            String title = NbBundle.getMessage(OperationMethodChooserPanel.class, "LBL_ConsumerAdvanced_Dialog");
            dialog.setTitle(title);
            dialog.getAccessibleContext().setAccessibleName(title);
            dialog.getAccessibleContext().setAccessibleDescription(title);
            dialog.pack();
            dialog.setResizable(true);
            dialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jBtnAdvancedActionPerformed

    private void setAdvancedDefaultOptions() {
        if (mAdvancedPanel == null) {
            mAdvancedPanel = new POJOConsumerPalleteAdvancedPanel();
        }

        mAdvancedPanel.setDoneMethodName(GeneratorUtil.POJO_DONE_METHOD_NAME);
        mAdvancedPanel.setReplyMethodName(GeneratorUtil.POJO_REPLY_METHOD_NAME);
        Object opnObject = this.jCmbOpnName.getSelectedItem();
        if (opnObject instanceof String) {
            mAdvancedPanel.setInputMessageType(opnObject + GeneratorUtil.POJO_IN_MESSAGE_SUFFIX);
            mAdvancedPanel.setInputMessageType(this.getInterfaceNS());
        } else {
            OperationMetadata opnMd = (OperationMetadata) opnObject;
            if (opnMd != null) {
                mAdvancedPanel.setInputMessageType(opnMd.getInputMessageName().getLocalPart());
                mAdvancedPanel.setInputMessageTypeNS(opnMd.getInputMessageName().getNamespaceURI());
            }
        }
    }

private void jCmbOpnNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCmbOpnNameActionPerformed
    fireChange();
    this.jCmbOpnNameFocusGained(null);
}//GEN-LAST:event_jCmbOpnNameActionPerformed

    private void populateOperationAndInterface(String url, File wsdlFile) {
        WSDLUtil wsdlUtil = new WSDLUtil();

        if ((url == null) && (wsdlFile == null)) {
            return;
        }
        
        if (wsdlFile != null) {
            FileObject fo = FileUtil.toFileObject(wsdlFile);
            if ( fo != null) {
                mWsdlModel = wsdlUtil.getWSDLModel(fo);
            } else {
                return;
            }
        } else {
            mWsdlModel = wsdlUtil.getWSDLModel(url, (Project) mWizDesc.getProperty(GeneratorUtil.PROJECT_INSTANCE), this.mWizDesc);
        }

        WSDLMetadata wsdlMetadata = WSDLUtil.getInterfaceNames(mWsdlModel);
        List<PortTypeMetadata> listOfPT = wsdlMetadata.getPortTypeMetadaList();
        PortTypeMetadata firstPT = listOfPT.get(0);
        jCmbIntfName.removeAllItems();
        for (PortTypeMetadata pt : listOfPT) {
            System.err.println("-- " + pt.toString());
            this.jCmbIntfName.addItem(pt);
        }
        jCmbIntfName.updateUI();

        if (firstPT != null) {
            this.jCmbIntfName.setSelectedItem(firstPT);
            String ns = firstPT.getPortType().getNamespaceURI();
            this.jTxtOpnNS.setText(ns);
            this.jTxtIntfNS.setText(ns);

        }
    }

    public String getInterfaceName() {
        Object ptmObj = this.jCmbIntfName.getSelectedItem();
        if (ptmObj == null) {
            ComboBoxEditor cbx = jCmbIntfName.getEditor();
            if ( cbx == null ) {
                return null;
            }
            ptmObj = this.jCmbIntfName.getEditor().getItem();
        }
        String intfName = null;
        if (ptmObj instanceof PortTypeMetadata) {
            intfName = ((PortTypeMetadata) this.jCmbIntfName.getSelectedItem()).getPortType().getLocalPart();
        } else {
            intfName = (String) ptmObj;
        }
        return intfName;
    }

    public String getInterfaceNS() {
        return this.jTxtIntfNS.getText();
    }

    public String getOperation() {
        Object ptmObj = this.jCmbOpnName.getSelectedItem();
        if (ptmObj == null) {
            ptmObj = this.jCmbOpnName.getEditor().getItem();
        }


        String opnName = null;
        if (ptmObj instanceof OperationMetadata) {
            opnName = ((OperationMetadata) this.jCmbOpnName.getSelectedItem()).getOperationName();
        } else {
            opnName = (String) ptmObj;
        }
        return opnName;
    }

    public String getOperationNS() {
        return this.jTxtOpnNS.getText();
    }

    public boolean isSynchronousInvoke() {
        return this.jCheckSynch.isSelected();
    }

    public POJOSupportedDataTypes getInputType() {
        return GeneratorUtil.POJO_IN_TYPES[this.jcmbInvokeInputType.getSelectedIndex()];
    }

    public POJOSupportedDataTypes getOutputType() {
        return GeneratorUtil.POJO_OUT_TYPES[this.jCmbInvkRetType.getSelectedIndex()];
    }

    public ExecutableElement getInvokeFrom() {
        return this.mMapMethSignToExecElem.get((String) this.jCmbInvokeFromMethod.getSelectedItem());
    }

    public void setExistingMethods(Map<String, ExecutableElement> map) {
        mMapMethSignToExecElem = map;
        jCmbInvokeFromMethod.setEditable(true);
        jCmbInvokeFromMethod.setModel(new javax.swing.DefaultComboBoxModel(map.keySet().toArray(new String[0])));
        jCmbInvokeFromMethod.setEditable(false);

    }

    public POJOConsumerPalleteAdvancedPanel getAdvanced() {
        if (mAdvancedPanel == null) {
            setAdvancedDefaultOptions();
        }
        return this.mAdvancedPanel;
    }

private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed
// TODO add your handling code here:
    String baseLoc = this.jTxtWSDLFileLoc.getText();//".";
    File baseLocFile = (baseLoc != null ?new File( baseLoc):null);
    
    if  ( baseLocFile != null ) {
        if (!  baseLocFile.exists()) {
            File parentFile =  baseLocFile.getParentFile();
            if ( parentFile != null && parentFile.exists()) {
                baseLoc = parentFile.getAbsolutePath();
            }else {
                baseLoc = ".";
            }
        }
    } else {
        baseLoc = ".";
    }            
            
    JFileChooser jfc = new JFileChooser(baseLoc);
    javax.swing.filechooser.FileFilter filter1 = new ExtensionFileFilter("*.wsdl", new String[]{"wsdl", "WSDL"});//NOI18N
    jfc.setFileFilter(filter1);
    if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        File wsdlFile = jfc.getSelectedFile();
        if ((wsdlFile != null) && (wsdlFile.isFile() && (wsdlFile.exists()))){
            this.jTxtWSDLFileLoc.setText(wsdlFile.getAbsolutePath());
            this.populateOperationAndInterface(null, wsdlFile);
        }
    }
}//GEN-LAST:event_jBtnBrowseActionPerformed

private void jCmbIntfNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCmbIntfNameActionPerformed
// TODO add your handling code here:
    fireChange();
    this.jCmbIntfNameFocusGained(null);
    if (jCmbIntfName.getItemCount() > 0) {
        if (this.jCmbIntfName.getSelectedItem() instanceof PortTypeMetadata) {
            PortTypeMetadata pt = (PortTypeMetadata) this.jCmbIntfName.getSelectedItem();
            List<OperationMetadata> listOP = pt.getOperationMetadataList();
            this.jCmbOpnName.removeAllItems();
            this.jCmbIntfName.removeAllItems();
            for (OperationMetadata opn : listOP) {
                this.jCmbOpnName.addItem(opn);
            }
            if (listOP.size() > 0) {
                this.jCmbOpnName.setSelectedItem(listOP.get(0));
            }
        }
    }
}//GEN-LAST:event_jCmbIntfNameActionPerformed

private void jTxtWSDLFileLocURLInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocURLInputMethodTextChanged
// TODO add your handling code here:
}//GEN-LAST:event_jTxtWSDLFileLocURLInputMethodTextChanged

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
    private void clearFields() {
        this.jTxtIntfNS.setText("");
        this.jTxtOpnNS.setText("");
        this.jCmbIntfName.removeAllItems();
        this.jCmbOpnName.removeAllItems();
        //this.jTxtDesc.setText("");
        this.jTxtWSDLFileLoc.setText("");
    }

private void jRdBtWsdlFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRdBtWsdlFromFileActionPerformed
// TODO add your handling code here:
    if (jRdBtWsdlFromFile.isSelected() && !this.jTxtWSDLFileLoc.isEnabled()) {
        clearFields();
    }

    this.jTxtWSDLFileLoc.setEditable(true);
    this.jTxtWSDLFileLoc.setEnabled(true);
    this.jTxtWSDLFileLocURL.setEditable(false);
    this.jTxtWSDLFileLocURL.setEnabled(false);

}//GEN-LAST:event_jRdBtWsdlFromFileActionPerformed

private void jRdBtWsdlFromURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRdBtWsdlFromURLActionPerformed
// TODO add your handling code here:
    if (jRdBtWsdlFromURL.isSelected() && !this.jTxtWSDLFileLoc.isEditable()) {
        clearFields();
    }

    this.jTxtWSDLFileLoc.setEditable(false);
    this.jTxtWSDLFileLoc.setEnabled(true);

    this.jTxtWSDLFileLocURL.setEnabled(true);
    this.jTxtWSDLFileLocURL.setEditable(true);

}//GEN-LAST:event_jRdBtWsdlFromURLActionPerformed



static boolean exists(String URLName){
  try {
    //HttpURLConnection.setFollowRedirects(false);
    // note : you may also need
    //        HttpURLConnection.setInstanceFollowRedirects(false)
        String urlN = URLName.toLowerCase();
        if ( urlN.startsWith("http:")) {
            HttpURLConnection con =
               (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("GET");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } 
    }
  catch (Exception e) {
       e.printStackTrace();
       return false;
       }
  return false;
  }



private void jTxtWSDLFileLocURLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocURLFocusLost
// TODO add your handling code here:
    if (this.jTxtWSDLFileLocURL.isEditable() && this.jTxtWSDLFileLocURL.getText().length() > 7) {
        try {

            if  (exists(jTxtWSDLFileLocURL.getText())) {
                this.populateOperationAndInterface(this.jTxtWSDLFileLocURL.getText(), null);
            }
        } catch (Exception ex) {
        }
    }
}//GEN-LAST:event_jTxtWSDLFileLocURLFocusLost

private void jTxtWSDLFileLocKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocKeyPressed
// TODO add your handling code here:
    //if ( evt.getKeyCode()  == java.awt.event.KeyEvent.VK_TAB) {
    if (this.jTxtWSDLFileLoc.isEditable()) {
        String text = this.jTxtWSDLFileLoc.getText();
        if (text != null && (text.toLowerCase().endsWith(".wsd") &&  (evt != null &&  evt.getKeyChar() == 'l' || evt.getKeyChar() == 'L')) || (text.toLowerCase().endsWith(".wsdl")) ) {
            File file = new File(text);
            if (file.exists()) {
                this.populateOperationAndInterface(null, new File(jTxtWSDLFileLoc.getText()));
            } else {
                this.jTxtIntfNS.setText("");
                this.jTxtOpnNS.setText("");
                this.jCmbIntfName.removeAllItems();
                this.jCmbOpnName.removeAllItems();
            }
        } else {
            this.jTxtIntfNS.setText("");
            this.jTxtOpnNS.setText("");
            this.jCmbIntfName.removeAllItems();
            this.jCmbOpnName.removeAllItems();
        }
    }
// }
}//GEN-LAST:event_jTxtWSDLFileLocKeyPressed

private void jTxtWSDLFileLocURLKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocURLKeyPressed
}//GEN-LAST:event_jTxtWSDLFileLocURLKeyPressed

private void jcbConsumerFromWSDLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jcbConsumerFromWSDLItemStateChanged
// TODO add your handling code here:
    if (this.jcbConsumerFromWSDL.isSelected()) {
        enableWSDLBased(true);
    } else {
        enableWSDLBased(false);
    }
}//GEN-LAST:event_jcbConsumerFromWSDLItemStateChanged

    private void disableWSDL(boolean bool) {
        this.jRdBtWsdlFromFile.setEnabled(bool);
        this.jRdBtWsdlFromURL.setEnabled(bool);
        this.jTxtWSDLFileLoc.setEnabled(bool);
        this.jTxtWSDLFileLocURL.setEnabled(bool);
        this.jBtnBrowse.setEnabled(bool);

        this.jCmbIntfName.setEnabled(false);
        this.jCmbOpnName.setEnabled(false);
        this.jTxtIntfNS.setEnabled(false);
        this.jTxtOpnNS.setEnabled(false);
        this.jcbConsumerFromWSDL.setEnabled(false);
    }

    private void enableWSDLBased(boolean bool) {
        this.jRdBtWsdlFromFile.setEnabled(bool);
        this.jRdBtWsdlFromURL.setEnabled(bool);
        this.jTxtWSDLFileLoc.setEnabled(bool);
        this.jTxtWSDLFileLocURL.setEnabled(bool);
        this.jBtnBrowse.setEnabled(bool);

        if (!bool) {
            this.jCmbOpnName.removeAllItems();
            this.jCmbIntfName.removeAllItems();
            this.jTxtIntfNS.setText("");
            this.jTxtOpnNS.setText("");
            this.jTxtWSDLFileLoc.setText("");
            this.jTxtWSDLFileLocURL.setText(GeneratorUtil.POJO_WSDL_LOC_PREFIX);
        }
    }

    public void populateFromWSDLModel(WSDLModel wsdlModel, boolean editable) {
        Definitions wsdlDef = wsdlModel.getDefinitions();
        String namespace = wsdlDef.getTargetNamespace();
        PortType pt = wsdlDef.getPortTypes().iterator().next();
        String interfaceName = pt.getName();
        Operation op = pt.getOperations().iterator().next();
        String operationName = op.getName();
        Input inMsg = op.getInput();
        String inputTypeName = null;
        String inputTypeNameNS = null;

        Output opOut = op.getOutput();
        if (mAdvancedPanel == null) {
            mAdvancedPanel = new POJOConsumerPalleteAdvancedPanel();
        }

        if (inMsg != null) {
            inputTypeName = inMsg.getMessage().getQName().getLocalPart();
            inputTypeNameNS = inMsg.getMessage().getQName().getNamespaceURI();

            mAdvancedPanel.setInputMessageType(inputTypeName);
            mAdvancedPanel.setInputMessageTypeNS(inputTypeNameNS);


        } else {
            jcmbInvokeInputType.setSelectedItem(POJOSupportedDataTypes.Void);//NOI18N
            jcmbInvokeInputType.setEnabled(false);
        }

        if (opOut == null) {
            this.jCmbInvkRetType.setSelectedItem(POJOSupportedDataTypes.Void);//NOI18N
            this.jCmbInvkRetType.setEnabled(false);


        }


        this.jTxtIntfNS.setText(namespace);
        this.jTxtOpnNS.setText(namespace);
        this.jCmbIntfName.getEditor().setItem(interfaceName);
        this.jCmbOpnName.getEditor().setItem(operationName);

        this.jCheckSynch.setSelected(true);
        this.disableWSDL(false);

    }

private void jcbConsumerFromWSDLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbConsumerFromWSDLActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jcbConsumerFromWSDLActionPerformed

private void jcmbInvokeInputTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jcmbInvokeInputTypeItemStateChanged
// TODO add your handling code here:
    Object pjDtObj = this.jcmbInvokeInputType.getSelectedItem();
    if (pjDtObj.equals(POJOSupportedDataTypes.MessageExchange)) {
        jCmbInvkRetType.setSelectedItem(POJOSupportedDataTypes.Void);//NOI18N
        jCmbInvkRetType.setEnabled(false);
    } else {
        if (!jCmbInvkRetType.isEnabled()) {
            jCmbInvkRetType.setEnabled(true);
        }
    }

}//GEN-LAST:event_jcmbInvokeInputTypeItemStateChanged

private void jTxtWSDLFileLocInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocInputMethodTextChanged
// TODO add your handling code here:
    // this.jTxtWSDLFileLocKeyPressed(null);
}//GEN-LAST:event_jTxtWSDLFileLocInputMethodTextChanged

private void jTxtWSDLFileLocCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocCaretPositionChanged
// TODO add your handling code here:
    //this.jTxtWSDLFileLocKeyPressed(null);
}//GEN-LAST:event_jTxtWSDLFileLocCaretPositionChanged

private void jTxtWSDLFileLocFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_Type_File")); //NOI18N
    this.jTxtWSDLFileLocKeyPressed(null);
}//GEN-LAST:event_jTxtWSDLFileLocFocusGained

private void jTxtWSDLFileLocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocFocusLost
// TODO add your handling code here:
    this.jTxtWSDLFileLocKeyPressed(null);
}//GEN-LAST:event_jTxtWSDLFileLocFocusLost

private void jcbConsumerFromWSDLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jcbConsumerFromWSDLFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_CheckBox")); //NOI18N
}//GEN-LAST:event_jcbConsumerFromWSDLFocusGained

private void jRdBtWsdlFromFileFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jRdBtWsdlFromFileFocusGained
// TODO add your handling code here:
     this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_Select_File")); //NOI18N
}//GEN-LAST:event_jRdBtWsdlFromFileFocusGained

private void jBtnBrowseFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jBtnBrowseFocusGained
// TODO add your handling code here:
     this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_Select_File_Browse")); //NOI18N
}//GEN-LAST:event_jBtnBrowseFocusGained

private void jRdBtWsdlFromURLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jRdBtWsdlFromURLFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_URL_CheckBox")); //NOI18N
}//GEN-LAST:event_jRdBtWsdlFromURLFocusGained

private void jTxtWSDLFileLocURLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtWSDLFileLocURLFocusGained
// TODO add your handling code here:
// TODO add your handling code here://GEN-LAST:event_jTxtWSDLFileLocURLFocusGained
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_WSDL_URL_Txt")); //NOI18N

}

private void jCmbIntfNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbIntfNameFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Interface_Txt")); //NOI18N
}//GEN-LAST:event_jCmbIntfNameFocusGained

private void jTxtIntfNSFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtIntfNSFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_IntfNs_Txt")); //NOI18N
}//GEN-LAST:event_jTxtIntfNSFocusGained

private void jCmbOpnNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbOpnNameFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Operation_Txt")); //NOI18N
}//GEN-LAST:event_jCmbOpnNameFocusGained

private void jTxtOpnNSFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtOpnNSFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_OpnNs_Txt")); //NOI18N
}//GEN-LAST:event_jTxtOpnNSFocusGained

private void jCheckSynchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCheckSynchFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Invoke_Synch_Txt")); //NOI18N
}//GEN-LAST:event_jCheckSynchFocusGained

private void jCheckAsynchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCheckAsynchFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Invoke_ASynch_Txt")); //NOI18N
}//GEN-LAST:event_jCheckAsynchFocusGained

private void jcmbInvokeInputTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jcmbInvokeInputTypeFocusGained
// TODO add your handling code here:
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Invoke_InputType_Txt")); //NOI18N
}//GEN-LAST:event_jcmbInvokeInputTypeFocusGained

private void jCmbInvkRetTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbInvkRetTypeFocusGained
// TODO add your handling code here:
    
    this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Invoke_OutputType_Txt")); //NOI18N
}//GEN-LAST:event_jCmbInvkRetTypeFocusGained

private void jCmbInvokeFromMethodFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbInvokeFromMethodFocusGained
// TODO add your handling code here:
     this.jDesc.setText(NbBundle.getMessage(this.getClass(), "LBL_Consumer_Invoke_FromMethod_Txt")); //NOI18N
}//GEN-LAST:event_jCmbInvokeFromMethodFocusGained

private void jCmbIntfNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbIntfNameKeyPressed
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNameKeyPressed

private void jTxtIntfNSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtIntfNSKeyPressed
// TODO add your handling code here:
    this.fireChange();//GEN-LAST:event_jTxtIntfNSKeyPressed
}

private void jCmbOpnNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbOpnNameKeyPressed
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbOpnNameKeyPressed

private void jTxtOpnNSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtOpnNSKeyPressed
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSKeyPressed

private void jCmbIntfNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCmbIntfNameFocusLost
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNameFocusLost

private void jCmbIntfNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbIntfNameKeyTyped
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNameKeyTyped

private void jCmbIntfNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbIntfNameKeyReleased
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNameKeyReleased

private void jCmbIntfNameInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jCmbIntfNameInputMethodTextChanged
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNameInputMethodTextChanged

private void jTxtIntfNSKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtIntfNSKeyReleased
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtIntfNSKeyReleased

private void jTxtIntfNSKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtIntfNSKeyTyped
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtIntfNSKeyTyped

private void jCmbOpnNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbOpnNameKeyReleased
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbOpnNameKeyReleased

private void jCmbOpnNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jCmbOpnNameKeyTyped
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbOpnNameKeyTyped

private void jCmbOpnNameInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jCmbOpnNameInputMethodTextChanged
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbOpnNameInputMethodTextChanged

private void jTxtOpnNSKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtOpnNSKeyReleased
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSKeyReleased

private void jTxtOpnNSKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtOpnNSKeyTyped
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSKeyTyped

private void jTxtOpnNSInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTxtOpnNSInputMethodTextChanged
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSInputMethodTextChanged

private void jCmbIntfNamePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jCmbIntfNamePropertyChange
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jCmbIntfNamePropertyChange

private void jTxtOpnNSFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTxtOpnNSFocusLost
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSFocusLost

private void jTxtOpnNSPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTxtOpnNSPropertyChange
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jTxtOpnNSPropertyChange

private void jPanel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jPanel1KeyPressed

private void jPanel1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseMoved
// TODO add your handling code here:
    this.fireChange();
}//GEN-LAST:event_jPanel1MouseMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jBtnAdvanced;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JRadioButton jCheckAsynch;
    private javax.swing.JRadioButton jCheckSynch;
    private javax.swing.JComboBox jCmbIntfName;
    private javax.swing.JComboBox jCmbInvkRetType;
    private javax.swing.JComboBox jCmbInvokeFromMethod;
    private javax.swing.JComboBox jCmbOpnName;
    private javax.swing.JTextArea jDesc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRdBtWsdlFromFile;
    private javax.swing.JRadioButton jRdBtWsdlFromURL;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTxtIntfNS;
    private javax.swing.JTextField jTxtOpnNS;
    private javax.swing.JTextField jTxtWSDLFileLoc;
    private javax.swing.JTextField jTxtWSDLFileLocURL;
    private javax.swing.JCheckBox jcbConsumerFromWSDL;
    private javax.swing.JComboBox jcmbInvokeInputType;
    private javax.swing.JLabel lblInterfaceName;
    private javax.swing.JLabel lblInterfaceNs;
    private javax.swing.JLabel lblInvokeInputType;
    private javax.swing.JLabel lblInvokeMethod;
    private javax.swing.JLabel lblInvokePattern;
    private javax.swing.JLabel lblInvokeReturnType;
    private javax.swing.JLabel lblOpName;
    private javax.swing.JLabel lblOpNameNs;
    // End of variables declaration//GEN-END:variables
}

