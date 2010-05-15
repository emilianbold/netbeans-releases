/*
 * MessageTypePanel.java
 *
 * Created on September 25, 2008, 11:17 AM
 */
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ftp.FTPComponentEncodable;
import org.netbeans.modules.wsdlextensions.ftp.FTPConstants;
import org.netbeans.modules.wsdlextensions.ftp.FTPMessage;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  jfu
 */
public class MessageTypePanel extends javax.swing.JPanel {
    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;
    private FTPComponentEncodable mFTPEncodable;
    private String mOpName;
    private Part mPart;
    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ftp.resources.Bundle");
    private static final Logger mLogger = Logger.getLogger(MessageTypePanel.class.getName());
    private DescriptionPanel descPanel = null;
    private Project mProject = null;
    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
    private GlobalType mType = null;
    private GlobalElement mElement = null;
    private PropertyChangeSupport mPropertySupport;
    private boolean bEnableForwardAsAttach;
    
    /** Creates new form MessageTypePanel */
    public MessageTypePanel(WSDLComponent component, Part part,
            FTPMessage ftpMessage, String operationName, PropertyChangeSupport topContainer, boolean forwardAsAttach) {
        bEnableForwardAsAttach = forwardAsAttach;
        mPropertySupport = topContainer;
        initComponents();
        populateView(component, part, ftpMessage, null, operationName);
    }

    /**
     * Populate the view with the given the model component
     * @param component
     * @param part
     * @param fileMessage
     * @param operationName
     */
    public void populateView(WSDLComponent component, Part part,
            FTPComponentEncodable ftpMessage, Project project, String operationName) {
        cleanUp();
        mWsdlComponent = component;
        mPart = part;
        mFTPEncodable = ftpMessage;
        mProject = project;
        mOpName = operationName;
        if (mOpName == null) {
            mOpName = "";
        }
        resetView();
        populateView();
        initListeners();
    }

    /**
     * Return message type.  Options are FileConstants.XML_MESSAGE_TYPE,
     * FileConstants.TEXT_MESSAGE_TYPE, FileConstants.ENCODED_MESSAGE_TYPE
     * 
     * @return
     */
    public int getMessageType() {
        // default is text
        Object typeObj = messageTypeCombo.getSelectedItem();
        int msgType = FTPConstants.TEXT_MESSAGE_TYPE;
        if ( typeObj != null ) {
            if ( FTPConstants.TEXT.equals(typeObj.toString())) {
                msgType = FTPConstants.TEXT_MESSAGE_TYPE;
            } else if (FTPConstants.XML.equals(typeObj.toString())) {
                msgType = FTPConstants.XML_MESSAGE_TYPE;
            } else if (FTPConstants.BINARY.equals(typeObj.toString())) {
                msgType = FTPConstants.BINARY_MESSAGE_TYPE;
            } else if (FTPConstants.ENCODED_DATA.equals(typeObj.toString())) {
                return FTPConstants.ENCODED_MESSAGE_TYPE;
            }
        }
        return msgType;
    }     
    /**
     * get charset for the message type selected
     * must be "text"
     * @return
     */
    public String getMessageCharEncoding() {
        return messageCharEncodingComboBox.getSelectedItem() != null ?  messageCharEncodingComboBox.getSelectedItem().toString() : null;
    }     

    /**
     * Return the encoding style value
     * @return String encoding style
     */
    public String getEncodingStyle() {
        return inputEncodedTypeTfld.getText() != null ? inputEncodedTypeTfld.getText().trim() : null;
    }

    /**
     * Return the use type for input message
     * @return String use type
     */
    public String getInputUseType() {
        Object typeObj = messageTypeCombo.getSelectedItem();
        if (typeObj != null && typeObj.equals(FTPConstants.ENCODED_DATA)) {
            return FTPConstants.ENCODED;
        } else {
            return FTPConstants.LITERAL;
        }
    }

    public GlobalType getSelectedPartType() {
        return mType;
    }
    
    public GlobalElement getSelectedElementType() {
        return mElement;
    }

    public void setDescriptionPanel(DescriptionPanel dPanel) {
        descPanel = dPanel;
    }

    public void setProject(Project project) {
        mProject = project;
    }

    private void initListeners() {
        if (mItemListener == null) {
            mItemListener = new MyItemListener();
        }

        if (mActionListener == null) {
            mActionListener = new MyActionListener();
        }

        if (mDocumentListener == null) {
            mDocumentListener = new MyDocumentListener();
        }
        
        messageTypeCombo.addItemListener(mItemListener);
        messageCharEncodingComboBox.getEditor().getEditorComponent().addFocusListener(
            new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageCharEncodingComboBoxFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageCharEncodingComboBoxFocusLost(evt);
            }}
        );
        
        inputXmlDetailsBtn.addActionListener(mActionListener);
        inputEncodedTypeTfld.getDocument().addDocumentListener(mDocumentListener);
        inputXMLTfld.getDocument().addDocumentListener(mDocumentListener);
    }

    private void populateView() {
        if (mFTPEncodable != null) {
            boolean encodedOn = false;
            if ((mFTPEncodable.getUse() != null) &&
                    (mFTPEncodable.getUse().equals(
                    FTPConstants.ENCODED))) {
                messageTypeCombo.setSelectedItem(FTPConstants.ENCODED_DATA);
                encodedOn = true;
                if (mFTPEncodable.getEncodingStyle() != null) {
                    inputEncodedTypeTfld.setText(mFTPEncodable.getEncodingStyle());
                } else {
                    inputEncodedTypeTfld.setText("");
                }
            } else {
                inputEncodedTypeTfld.setText("");
            }

            if ( mFTPEncodable.getFileType() != null && mFTPEncodable.getFileType().trim().length() > 0 ) {
                messageTypeCombo.setSelectedItem(mProject);
            }
            
            if ( bEnableForwardAsAttach ) {
                chkAttach.setVisible(true);
                chkAttach.setSelected(mFTPEncodable.getForwardAsAttachment());
            }
            
            if (mPart != null) {
                String ptStr = Utilities.getPartTypeOrElementString(mPart);

                if (FTPConstants.XSD_STRING.equals(ptStr)) {
                    messageTypeCombo.setSelectedItem(FTPConstants.TEXT);
                } else if (FTPConstants.XSD_BASE64_BINARY.equals(ptStr) && (!encodedOn)) {
                    messageTypeCombo.setSelectedItem(FTPConstants.BINARY);
                } else {
                    if (encodedOn) {
                        inputXMLTfld.setText(ptStr);
                    } else {
                        messageTypeCombo.setSelectedItem(FTPConstants.XML);
                        inputXMLTfld.setText(ptStr);
                    }
                }
            }

        } else {
            // null out view
            inputXMLTfld.setText("");
            inputEncodedTypeTfld.setText("");
        }
        handleMessageTypeRecord();
    }

    private void cleanUp() {
        mWsdlComponent = null;
        mFTPEncodable = null;
    }

    private void resetView() {
        messageTypeCombo.removeItemListener(mItemListener);
        messageTypeCombo.removeAllItems();
        messageTypeCombo.addItem(FTPConstants.TEXT);
        messageTypeCombo.addItem(FTPConstants.BINARY);
        messageTypeCombo.addItem(FTPConstants.XML);
        messageTypeCombo.addItem(FTPConstants.ENCODED_DATA);      
    
        messageCharEncodingComboBox.removeAllItems();
        messageCharEncodingComboBox.addItem("");
        SortedMap<String, Charset> cs = Charset.availableCharsets();
        if ( cs != null ) {
            Iterator it = cs.keySet().iterator();
            while ( it.hasNext() ) {
                messageCharEncodingComboBox.addItem(cs.get(it.next()).name());
            }
        }

        messageCharEncodingComboBox.setEnabled(true);
        msgCharEncodingLab.setEnabled(true);

        chkAttach.setSelected(false);
        chkAttach.setVisible(bEnableForwardAsAttach);
        inputXmlDetailsBtn.removeActionListener(mActionListener);

        inputXMLTfld.setText("");
        inputEncodedTypeTfld.setText("");

        mType = null;
        mElement = null;
    }

    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
            String[] desc = null;

            if (evt.getSource() == inputXMLTfld) {
                desc = new String[]{"XSD Schema\n\n",
                            mBundle.getString("DESC_Attribute_PayloadProcessing_Xml_schema")
                        };
            } else if (evt.getSource() == inputEncodedTypeTfld) {
                desc = new String[]{"Encoder Name\n\n",
                            mBundle.getString("DESC_Attribute_PayloadProcessing_Encoded_EncoderName")
                        };
            } else if (evt.getSource() == messageTypeCombo ) {
                desc = new String[]{"Message Type\n\n",
                        messageTypeCombo.getToolTipText()};
            } else if (evt.getSource() == messageCharEncodingComboBox.getEditor().getEditorComponent() ) {
                desc = new String[]{"Message Content Char Encoding\n\n",
                        messageCharEncodingComboBox.getToolTipText()};
            } else if (evt.getSource() == chkAttach) {
                desc = new String[]{"Forward as Attachment\n\n",
                   chkAttach.getToolTipText()}; 
            }

            if (desc != null) {
                descPanel.setText(desc[0], desc[1]);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        messageTypePanel = new javax.swing.JPanel();
        payloadProcessingSectionLab = new javax.swing.JLabel();
        inputMessageTypeSep = new javax.swing.JSeparator();
        inputEncodedTypeTfld = new javax.swing.JTextField();
        inputXMLTfld = new javax.swing.JTextField();
        inputXmlDetailsBtn = new javax.swing.JButton();
        inputEncodedTypeLab = new javax.swing.JLabel();
        chkAttach = new javax.swing.JCheckBox();
        messageTypeLab = new javax.swing.JLabel();
        messageTypeCombo = new javax.swing.JComboBox();
        xmlElemTypeLab = new javax.swing.JLabel();
        msgCharEncodingLab = new javax.swing.JLabel();
        messageCharEncodingComboBox = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(610, 245));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(610, 245));
        setLayout(new java.awt.GridBagLayout());

        messageTypePanel.setMinimumSize(new java.awt.Dimension(610, 260));
        messageTypePanel.setName("messageTypePanel"); // NOI18N
        messageTypePanel.setPreferredSize(new java.awt.Dimension(610, 260));
        messageTypePanel.setLayout(new java.awt.GridBagLayout());

        payloadProcessingSectionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        payloadProcessingSectionLab.setText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.payloadProcessingSectionLab.text")); // NOI18N
        payloadProcessingSectionLab.setName("payloadProcessingSectionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(payloadProcessingSectionLab, gridBagConstraints);

        inputMessageTypeSep.setName("inputMessageTypeSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 136, 0, 10);
        messageTypePanel.add(inputMessageTypeSep, gridBagConstraints);

        inputEncodedTypeTfld.setToolTipText(mBundle.getString("DESC_Attribute_encodingStyle"));
        inputEncodedTypeTfld.setName("inputEncodedTypeTfld"); // NOI18N
        inputEncodedTypeTfld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputEncodedTypeTfldActionPerformed(evt);
            }
        });
        inputEncodedTypeTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputEncodedTypeTfldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputEncodedTypeTfldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 100, 10, 10);
        messageTypePanel.add(inputEncodedTypeTfld, gridBagConstraints);
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedTypeTfld.AccessibleContext.accessibleName")); // NOI18N
        inputEncodedTypeTfld.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedTypeTfld.AccessibleContext.accessibleDescription")); // NOI18N

        inputXMLTfld.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xmlElemTypeTxtFld.tooltip")); // NOI18N
        inputXMLTfld.setName("inputXMLTfld"); // NOI18N
        inputXMLTfld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputXMLTfldActionPerformed(evt);
            }
        });
        inputXMLTfld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputXMLTfldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputXMLTfldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(inputXMLTfld, gridBagConstraints);

        inputXmlDetailsBtn.setText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputXmlDetailsBtn.text")); // NOI18N
        inputXmlDetailsBtn.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputXmlDetailsBtn.tooltip")); // NOI18N
        inputXmlDetailsBtn.setName("inputXmlDetailsBtn"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(inputXmlDetailsBtn, gridBagConstraints);
        inputXmlDetailsBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputXmlDetailsBtn.AccessibleContext.accessibleName")); // NOI18N

        inputEncodedTypeLab.setLabelFor(inputEncodedTypeTfld);
        org.openide.awt.Mnemonics.setLocalizedText(inputEncodedTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedTypeLab.text")); // NOI18N
        inputEncodedTypeLab.setName("inputEncodedTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        messageTypePanel.add(inputEncodedTypeLab, gridBagConstraints);
        inputEncodedTypeLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.inputEncodedTypeLab.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkAttach, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.chkAttach.text")); // NOI18N
        chkAttach.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.chkAttach.tooltip.text")); // NOI18N
        chkAttach.setName("chkAttach"); // NOI18N
        chkAttach.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAttachActionPerformed(evt);
            }
        });
        chkAttach.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attachCheckFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(chkAttach, gridBagConstraints);

        messageTypeLab.setLabelFor(messageTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(messageTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeLab.text")); // NOI18N
        messageTypeLab.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeCombo.tooltip")); // NOI18N
        messageTypeLab.setName("messageTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(messageTypeLab, gridBagConstraints);

        messageTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        messageTypeCombo.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeCombo.tooltip")); // NOI18N
        messageTypeCombo.setName("messageTypeCombo"); // NOI18N
        messageTypeCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageTypeComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageTypeComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(messageTypeCombo, gridBagConstraints);
        messageTypeCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypeCombo.AccessibleContext.accessibleName")); // NOI18N

        xmlElemTypeLab.setLabelFor(inputXMLTfld);
        org.openide.awt.Mnemonics.setLocalizedText(xmlElemTypeLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xmlElemTypeLab.text")); // NOI18N
        xmlElemTypeLab.setName("xmlElemTypeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(xmlElemTypeLab, gridBagConstraints);
        xmlElemTypeLab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xmlElemTypeLab.AccessibleContext.accessibleName")); // NOI18N
        xmlElemTypeLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.xmlElemTypeLab.AccessibleContext.accessibleDescription")); // NOI18N

        msgCharEncodingLab.setLabelFor(messageCharEncodingComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(msgCharEncodingLab, org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.msgCharEncodingLab.text")); // NOI18N
        msgCharEncodingLab.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.msgCharEncodingLab.tooltip")); // NOI18N
        msgCharEncodingLab.setName("msgCharEncodingLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(msgCharEncodingLab, gridBagConstraints);

        messageCharEncodingComboBox.setEditable(true);
        messageCharEncodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        messageCharEncodingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageCharEncodingComboBox.tooltip")); // NOI18N
        messageCharEncodingComboBox.setName("messageCharEncodingComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        messageTypePanel.add(messageCharEncodingComboBox, gridBagConstraints);
        messageCharEncodingComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageCharEncodingComboBox.AccessibleContext.accessibleName")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(messageTypePanel, gridBagConstraints);
        messageTypePanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypePanel.AccessibleContext.accessibleName")); // NOI18N
        messageTypePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageTypePanel.class, "MessageTypePanel.messageTypePanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void inputEncodedTypeTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedTypeTfldFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputEncodedTypeTfldFocusGained

private void inputXMLTfldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputXMLTfldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_inputXMLTfldActionPerformed

private void inputXMLTfldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputXMLTfldFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputXMLTfldFocusGained

private void inputXMLTfldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputXMLTfldFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_inputXMLTfldFocusLost

private void inputEncodedTypeTfldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputEncodedTypeTfldFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_inputEncodedTypeTfldFocusLost

private void inputEncodedTypeTfldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputEncodedTypeTfldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_inputEncodedTypeTfldActionPerformed

private void chkAttachActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAttachActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chkAttachActionPerformed

private void attachCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attachCheckFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);//GEN-LAST:event_attachCheckFocusGained
}

private void messageTypeComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageTypeComboFocusGained
// TODO add your handling code here:
        updateDescriptionArea(evt);                                       
}//GEN-LAST:event_messageTypeComboFocusGained

private void messageTypeComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageTypeComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageTypeComboFocusLost

private void messageCharEncodingComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageCharEncodingComboBoxFocusGained
// TODO add your handling code here:
        updateDescriptionArea(evt);//GEN-LAST:event_messageCharEncodingComboBoxFocusGained
}

private void messageCharEncodingComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageCharEncodingComboBoxFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageCharEncodingComboBoxFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkAttach;
    private javax.swing.JLabel inputEncodedTypeLab;
    private javax.swing.JTextField inputEncodedTypeTfld;
    private javax.swing.JSeparator inputMessageTypeSep;
    private javax.swing.JTextField inputXMLTfld;
    private javax.swing.JButton inputXmlDetailsBtn;
    private javax.swing.JComboBox messageCharEncodingComboBox;
    private javax.swing.JComboBox messageTypeCombo;
    private javax.swing.JLabel messageTypeLab;
    private javax.swing.JPanel messageTypePanel;
    private javax.swing.JLabel msgCharEncodingLab;
    private javax.swing.JLabel payloadProcessingSectionLab;
    private javax.swing.JLabel xmlElemTypeLab;
    // End of variables declaration//GEN-END:variables

    public class MyItemListener implements ItemListener {

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            handleItemStateChanged(evt);
        }
    }

    public class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            handleActionPerformed(evt);
        }
    }

    public class MyDocumentListener implements DocumentListener {
        // Handle insertions into the text field

        public void insertUpdate(DocumentEvent event) {
            validateMe(true);
        }

        // Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            validateMe(true);
        }

        // Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }
    }

    private void handleItemStateChanged(ItemEvent evt) {
        if ( evt.getSource() == messageTypeCombo ) {
            // possible message type selection
            handleMessageTypeRecord();
        }
    }

    private void handleActionPerformed(ActionEvent evt) {
        if (evt.getSource() == inputXmlDetailsBtn) {
            showEncodedTypeDialog();
        }
    }

    public ErrorDescription validateMe() {
        return validateMe(false);
    }
    
    public ErrorDescription validateMe(boolean fireEvent) {
        return validateMe(fireEvent, false);
    }

    public ErrorDescription validateMe(boolean fireEvent, boolean selfOnly) {
        boolean valid = true;
        ErrorDescription error = new ErrorDescription();

        if ((isXMLPayload()) && (messageTypeCombo.isEnabled())) {
            if (inputXMLTfld.getText() == null || inputXMLTfld.getText().trim().length() == 0) {
                valid = false;
                String msg = NbBundle.getMessage(MessageTypePanel.class, 
                        "MessageTypePanel.XMLElementIncomplete");
                mLogger.finest(msg);
                error.setErrorMessage(msg);
                error.setErrorMode(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);
                if (fireEvent) {
                    mPropertySupport.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                                PROPERTY_ERROR_EVT, null, msg);                
                }
            }
        } else if (isEncodedPayload()) {
            if ((inputEncodedTypeTfld.getText().trim().length() == 0) ||
                    (inputXMLTfld.getText().trim().length() == 0)) {
                valid = false;
            }
            if (!valid) {                
                String msg = NbBundle.getMessage(MessageTypePanel.class, 
                        "MessageTypePanel.EncodedDataIncomplete");  
                error.setErrorMessage(msg);
                error.setErrorMode(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT);   
                if (fireEvent) {
                    mPropertySupport.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                                PROPERTY_ERROR_EVT, null, msg);                
                }
            }
        }

        if (valid) {
            if (!selfOnly) {
                // when this sub-panel is valid, further check the containing panel
                ErrorDescription outer_error = mPropertySupport.validateMe(fireEvent);
                error.setErrorMessage(outer_error != null && outer_error.getErrorMessage() != null ? outer_error.getErrorMessage() : "");
                error.setErrorMode(outer_error != null && outer_error.getErrorMode() != null ? outer_error.getErrorMode() : ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT);
            }
            else {
                error.setErrorMessage("");
                error.setErrorMode(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT);
                if (fireEvent) {
                    mPropertySupport.doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_CLEAR_MESSAGES_EVT, null, "");
                }
            }
        }
        
        return error;
    }

    private void showEncodedTypeDialog() {
        WSDLModel wsdlModel = mWsdlComponent.getModel();
        SchemaComponent schemaComponent = null;
        if (mPart != null) {
            schemaComponent = mPart.getElement() == null ? null : mPart.getElement().get();
            if (schemaComponent == null) {
                schemaComponent = mPart.getType() == null ? null : mPart.getType().get();
            }
        }

        if (mProject != null) {
            boolean ok = BindingComponentUtils.browseForElementOrType(mProject,
                    wsdlModel, schemaComponent);
            if (ok) {
                mType = BindingComponentUtils.getElementOrType();
                mElement = BindingComponentUtils.getSchemaComponent();
                String partTypeStr = BindingComponentUtils.getPrefixNameSpace();
                if (isEncodedPayload()) {
                    if (mElement == null) {
                        // encoded must be of element type only
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                NbBundle.getMessage(MessageTypePanel.class,
                                "MessageTypePanel.invalidType"));
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                } 
                inputXMLTfld.setText(partTypeStr);
            }
        } else {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(MessageTypePanel.class,
                    "MessageTypePanel.UnknownProject"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
    }

    public void enablePayloadProcessing(boolean enable) {
        messageTypeCombo.setEnabled(enable);
        inputEncodedTypeTfld.setEnabled(enable);
        inputXMLTfld.setEnabled(enable);
        inputXmlDetailsBtn.setEnabled(enable);
        inputEncodedTypeLab.setEnabled(enable);
    }

    /**
     * Enables the xml message payload
     * @param enable
     */
    public void enableXMLPayloadProcessing(boolean enable) {
        messageTypeCombo.setEnabled(true);
        validateMe(true);
    }

    private void handleMessageTypeRecord() {
        Object typeObj = messageTypeCombo.getSelectedItem();
        if ( typeObj != null ) {
            if (FTPConstants.XML.equals(typeObj.toString())) {
                // XML
                xmlElemTypeLab.setEnabled(true);
                inputEncodedTypeTfld.setEnabled(false);
                inputEncodedTypeLab.setEnabled(false);
                inputXMLTfld.setEnabled(true);
                inputXmlDetailsBtn.setEnabled(true);
                // XML message content char set should come from xml declaration
                // not from user through this GUI
                messageCharEncodingComboBox.setSelectedItem("");
                messageCharEncodingComboBox.setEnabled(false);
                msgCharEncodingLab.setEnabled(false);
            } else if (FTPConstants.TEXT.equals(typeObj.toString()) ) {
                // TEXT
                xmlElemTypeLab.setEnabled(false);
                inputXMLTfld.setEnabled(false);
                inputXmlDetailsBtn.setEnabled(false);
                inputEncodedTypeTfld.setEnabled(false);
                inputEncodedTypeLab.setEnabled(false);
                messageCharEncodingComboBox.setSelectedItem("");
                messageCharEncodingComboBox.setEnabled(true);
                msgCharEncodingLab.setEnabled(true);
            } else if ( FTPConstants.BINARY.equals(typeObj.toString()) ) {
                // BINARY
                xmlElemTypeLab.setEnabled(false);
                inputXMLTfld.setEnabled(false);
                inputXmlDetailsBtn.setEnabled(false);
                inputEncodedTypeTfld.setEnabled(false);
                inputEncodedTypeLab.setEnabled(false);
                messageCharEncodingComboBox.setSelectedItem("");
                messageCharEncodingComboBox.setEnabled(false);
                msgCharEncodingLab.setEnabled(false);
            } else if (FTPConstants.ENCODED_DATA.equals(typeObj.toString())) {
                // ENCODED - for encoded use, there is a xsd with anotation
                // in it, and the customencoder take the charset info from
                // the xsd also, not from the user through this GUI
                xmlElemTypeLab.setEnabled(true);
                inputXMLTfld.setEnabled(true);
                inputXmlDetailsBtn.setEnabled(true);
                inputEncodedTypeTfld.setEnabled(true);
                inputEncodedTypeLab.setEnabled(true);
                messageCharEncodingComboBox.setSelectedItem("");
                messageCharEncodingComboBox.setEnabled(false);
                msgCharEncodingLab.setEnabled(false);
            }
        }
        else {
            // default to TEXT
            xmlElemTypeLab.setEnabled(false);
            inputXMLTfld.setEnabled(false);
            inputXmlDetailsBtn.setEnabled(false);
            inputEncodedTypeTfld.setEnabled(false);
            inputEncodedTypeLab.setEnabled(false);
            messageCharEncodingComboBox.setSelectedItem("");
            messageCharEncodingComboBox.setEnabled(true);
            msgCharEncodingLab.setEnabled(true);
        }
        validateMe(true);
    }

    /**
     * Checks if the xml payload toggle is turned on
     * @return boolean true if the XML payload is selected
     */
    public boolean isXMLPayload() {
        Object typeObj = messageTypeCombo.getSelectedItem();
        if (typeObj != null && FTPConstants.XML.equals(typeObj.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the encoded payload is selected
     * @return boolean true if the encoded payload is selected
     */
    public boolean isEncodedPayload() {
        Object typeObj = messageTypeCombo.getSelectedItem();
        if (typeObj != null && FTPConstants.ENCODED_DATA.equals(typeObj.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return true if payload is to be forwarded as an attachment
     * @return boolean 
     */
    public boolean getForwardAsAttachment() {
        return chkAttach.isSelected();
    }
}
