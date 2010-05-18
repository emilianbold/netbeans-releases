/*
 * OutboundTransferPanel.java
 *
 * Created on September 20, 2008, 9:31 AM
 */
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ftp.FTPAddress;
import org.netbeans.modules.wsdlextensions.ftp.FTPBinding;
import org.netbeans.modules.wsdlextensions.ftp.FTPConstants;
import org.netbeans.modules.wsdlextensions.ftp.FTPMessage;
import org.netbeans.modules.wsdlextensions.ftp.FTPOperation;
import org.netbeans.modules.wsdlextensions.ftp.FTPTransfer;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.utils.WSDLUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.NbBundle;

/**
 *
 * @author  jfu
 */
public class OutboundTransferPanel extends javax.swing.JPanel implements AncestorListener, PropertyAccessible, PropertyChangeSupport, BindingConfigurationDelegate {

    private MessageTypePanel mMessageTypePanel;
    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;
    /** QName **/
    private QName mQName;
    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;
    /** resource bundle for ftp bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ftp.resources.Bundle");
    private static final Logger mLogger = Logger.getLogger(OutboundTransferPanel.class.getName());
    private DescriptionPanel descPanel = null;
    private boolean bRequest;
    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
    /**
     * invisible components used to keep status:
     */
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JComboBox partComboBox;
    private Part mPart = null;
    private Map<String, Object> mWzdProps;
    private PropertyChangeSupport mProxy;
    private boolean bOneWay;
    private boolean bRequestResponseCorrelate;

    /** Creates new form OutboundTransferPanel */
    public OutboundTransferPanel(QName qName, WSDLComponent component, boolean oneway, boolean b) {
        this(qName, component, oneway, b, null);
    }

    public OutboundTransferPanel(QName qName, WSDLComponent component, boolean oneway, boolean b, PropertyChangeSupport proxy) {
        bRequest = b;
        bOneWay = oneway;
        mProxy = proxy;
        initComponents();
        initCustomComponents();
        populateView(qName, component);
    }

    public void setProject(Project project) {
        mProject = project;
        mMessageTypePanel.setProject(project);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OutboundTransferPanel.class,
                bRequest ? "OutboundTransferPanel.Request.StepLabel" : "OutboundTransferPanel.Response.StepLabel");
    }

    public void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }

    private boolean commitTransfer(FTPTransfer fTPTransfer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initCustomComponents() {
        mMessageTypePanel = new MessageTypePanel(mWsdlComponent, null, null, null, this, false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        //gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        outboundTransferingConfigPanel.add(mMessageTypePanel, gridBagConstraints);

        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        outboundTransferingConfigPanel.add(tmpPanel, gridBagConstraints);

        /**
         * invisible components used to keep status:
         */
        servicePortComboBox = new JComboBox();
        bindingNameComboBox = new JComboBox();
        portTypeComboBox = new JComboBox();
        operationNameComboBox = new JComboBox();
        partComboBox = new JComboBox();

        titleLab.setText(getName());
        this.addAncestorListener(this);

    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mWsdlComponent = component;
        resetView();
        populateView(mWsdlComponent);
        mMessageTypePanel.setDescriptionPanel(descPanel);
        initListeners();
    }

    private void cleanUp() {
        mQName = null;
        mWsdlComponent = null;
    }

    private void populateFTPAddress(FTPAddress fTPAddress) {
        Port port = (Port) fTPAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<FTPBinding> bindings = binding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                populateFTPBinding(bindings.iterator().next(), fTPAddress);
            }
        }
    }

    private void populateFTPBinding(FTPBinding fTPBinding, FTPAddress address) {
        if (address == null) {
            address = getFTPAddress(fTPBinding);
        }
        if (fTPBinding == null) {
            return;
        }
        Port port = (Port) address.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(fTPBinding);

        updateServiceView(address);
        if (fTPBinding != null) {
            populateListOfBindings(fTPBinding);
            populateListOfPortTypes(fTPBinding);
            Binding binding = (Binding) fTPBinding.getParent();

            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.setSelectedItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();

            populateOperations(bindingOperations);

            if (operationNameComboBox.getItemCount() > 0) {
                operationNameComboBox.setSelectedIndex(0);
            }

            if ((bindingOperations != null) && (bindingOperations.size() > 0)) {

                BindingOperation bop = getBindingOperation(bindingOperations);
                if (binding != null) {
                    FTPTransfer putTransfer = getPutFTPTransfer(binding,
                            bop.getName());
                    updatePutTransferView(binding, putTransfer);
                }
            }

        }
    }

    private BindingOperation getBindingOperation(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            return bop;
        }
        return null;
    }

    private FTPTransfer getPutFTPTransfer(Binding binding,
            String selectedOperation) {
        FTPTransfer putFTPTransfer = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    List<FTPTransfer> putFTPTransfers = null;
                    if (bRequest) {
                        putFTPTransfers = bop.getBindingInput().getExtensibilityElements(FTPTransfer.class);
                    } else {
                        putFTPTransfers = bop.getBindingOutput().getExtensibilityElements(FTPTransfer.class);
                    }
                    if (putFTPTransfers.size() > 0) {
                        putFTPTransfer = putFTPTransfers.get(0);
                        break;
                    }
                }
            }
        }
        return putFTPTransfer;
    }

    private FTPAddress getFTPAddress(FTPBinding ftpBinding) {
        FTPAddress ftpAddress = null;
        if ((ftpBinding != null) && (ftpBinding.getParent() != null)) {
            Binding parentBinding = (Binding) ftpBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<FTPAddress> ftpAddresses = port.getExtensibilityElements(FTPAddress.class).
                                    iterator();
                            // 1 address for 1 binding
                            while (ftpAddresses.hasNext()) {
                                return ftpAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return ftpAddress;
    }

    private void populateListOfBindings(FTPBinding fTPBinding) {
        if ((fTPBinding != null) && (fTPBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fTPBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<FTPBinding> fileBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null || binding.getType().get() == null) {
                    continue;
                }

                fileBindings = binding.getExtensibilityElements(FTPBinding.class);
                if (fileBindings != null) {
                    Iterator iter = fileBindings.iterator();
                    while (iter.hasNext()) {
                        FTPBinding b = (FTPBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
// TODO
//                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    private void populateListOfPortTypes(FTPBinding fTPBinding) {
        if ((fTPBinding != null) && (fTPBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fTPBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> ftpPortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
// TODO
//                if (!portTypeExists(portType.getName())) {
//
//                    portTypeComboBox.addItem(portType.getName());
//                }
            }
        }
    }

    private void populateListOfPorts(FTPBinding fTPBinding) {
        Vector<Port> portV = new Vector<Port>();
        if ((fTPBinding != null) && (fTPBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fTPBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if (port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            portV.add(port);
                        }
                    }
                }
            }
        }
    }

    private void populateOperations(Collection<BindingOperation> bindingOperations) {
        Iterator iter = bindingOperations.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    private void updatePutTransferView(Binding binding, FTPTransfer putTransfer) {
        postSendLocText.setToolTipText(mBundle.getString("DESC_Attribute_postSendLocation"));
        preSendLocText.setToolTipText(mBundle.getString("DESC_Attribute_preSendLocation"));
        sendToText.setToolTipText(mBundle.getString("DESC_Attribute_sendTo"));
        postSendCmdCombo.setToolTipText(mBundle.getString("DESC_Attribute_postSendCommand"));
        postSendLocHasPattern.setToolTipText(mBundle.getString("DESC_Attribute_postSendLocationHasPatterns"));
        preSendCmdCombo.setToolTipText(mBundle.getString("DESC_Attribute_preSendCommand"));
        preSendLocHasPattern.setToolTipText(mBundle.getString("DESC_Attribute_preSendLocationHasPatterns"));
        sendToHasPatternCheck.setToolTipText(mBundle.getString("DESC_Attribute_sendToHasPatterns"));
        appendCheck.setToolTipText(mBundle.getString("DESC_Attribute_append"));

        if (putTransfer != null) {
            postSendLocText.setText(putTransfer.getPostSendLocation());
            preSendLocText.setText(putTransfer.getPreSendLocation());
            sendToText.setText(putTransfer.getSendTo());
            postSendCmdCombo.setSelectedItem(putTransfer.getPostSendCommand());
            postSendLocHasPattern.setSelected(putTransfer.getPostSendLocationHasPatterns());
            preSendCmdCombo.setSelectedItem(putTransfer.getPreSendCommand());
            preSendLocHasPattern.setSelected(putTransfer.getPreSendLocationHasPatterns());
            sendToHasPatternCheck.setSelected(putTransfer.getSendToHasPatterns());
            bRequestResponseCorrelate = putTransfer.getMessageCorrelateEnabled();
            
            Object obj = putTransfer.getParent();
            Collection<Part> parts = null;

            if (obj instanceof BindingInput) {
                parts = WSDLUtils.getParts((BindingInput) obj);
            } else {
                // BindingOutput
                parts = WSDLUtils.getParts((BindingOutput) obj);
            }

            Vector<String> vect = new Vector<String>();
            //vect.add("");
            for (Part part : parts) {
                vect.add(part.getName());
            }

            appendCheck.setSelected(putTransfer.getAppend());

            // BASED on Message Type selected, need to check if Part selected has a type            
            partComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = putTransfer.getPart();
            if (part == null) {
                // per BC developer, will preselect 1st item
                if (partComboBox.getItemCount() > 0) {
                    partComboBox.setSelectedIndex(0);
                    part = (String) partComboBox.getSelectedItem();
                }
            } else {
                partComboBox.setSelectedItem(part);
            }

            // check if Part selected has a type and set correct msg type toggle
            // get the Message
            Operation op = Utilities.getOperation(binding,
                    operationNameComboBox.getSelectedItem() != null ? operationNameComboBox.getSelectedItem().toString() : "");
            if (op != null) {
                NamedComponentReference<Message> messagePut = null;
                if (bRequest) {
                    messagePut = op.getInput().getMessage();
                } else {
                    messagePut = op.getOutput().getMessage();
                }
                if (partComboBox.getSelectedItem() != null) {
                    if (part != null) {
                        mPart = Utilities.getMessagePart(part, messagePut.get());
                    }
                }
            }

            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    putTransfer, mProject,
                    operationNameComboBox.getSelectedItem() != null ? operationNameComboBox.getSelectedItem().toString() : "");

        } else {
            // null out view
            postSendLocText.setText("");
            preSendLocText.setText("");
            sendToText.setText("");
            postSendCmdCombo.setSelectedItem("");
            postSendLocHasPattern.setSelected(false);
            preSendCmdCombo.setSelectedItem("");
            preSendLocHasPattern.setSelected(false);
            sendToHasPatternCheck.setSelected(false);
            appendCheck.setSelected(false);
            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    putTransfer, null, null);
        }
        validateMe(true);
    }

    private void updateServiceView(FTPAddress address) {
        if (address != null) {
//            directoryTextField.setText(fileAddress.
//                    getAttribute(FileAddress.ATTR_FILE_ADDRESS));
//            directoryTextField.setToolTipText(
//                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N
//
//            if (fileAddress.getPathRelativeTo() == null) {
//                pathRelativeToComboBox.setSelectedItem(FileConstants.NOT_SET);
//            } else {
//                pathRelativeToComboBox.setSelectedItem(fileAddress.
//                        getPathRelativeTo());
//            }
//            pathRelativeToComboBox.setToolTipText(
//                    mBundle.getString("DESC_Attribute_pathRelativeTo"));//NOI18N
        }
    }

    private void resetView() {
        postSendCmdCombo.removeItemListener(mItemListener);
        postSendLocHasPattern.removeItemListener(mItemListener);
        preSendCmdCombo.removeItemListener(mItemListener);
        preSendLocHasPattern.removeItemListener(mItemListener);
        sendToHasPatternCheck.removeItemListener(mItemListener);

        postSendLocText.removeActionListener(mActionListener);
        preSendLocText.removeActionListener(mActionListener);
        sendToText.removeActionListener(mActionListener);

        postSendLocText.getDocument().removeDocumentListener(mDocumentListener);
        preSendLocText.getDocument().removeDocumentListener(mDocumentListener);
        sendToText.getDocument().removeDocumentListener(mDocumentListener);

        postSendCmdCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.POST_CMD_PUT.length; i++) {
            postSendCmdCombo.addItem(FTPConstants.POST_CMD_PUT[i]);
        }

        postSendLocHasPattern.setSelected(false);

        preSendCmdCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.PRE_CMD_PUT_GET.length; i++) {
            preSendCmdCombo.addItem(FTPConstants.PRE_CMD_PUT_GET[i]);
        }

        preSendLocHasPattern.setSelected(false);
        sendToHasPatternCheck.setSelected(false);
    }

    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof FTPAddress) {
                populateFTPAddress((FTPAddress) component);
            } else if (component instanceof FTPBinding) {
                populateFTPBinding((FTPBinding) component, null);
            } else if (component instanceof Port) {
                Collection<FTPAddress> address = ((Port) component).getExtensibilityElements(FTPAddress.class);
                if (!address.isEmpty()) {
                    populateFTPAddress(address.iterator().next());
                }
            } else if (component instanceof FTPMessage) {
                Object obj = ((FTPMessage) component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            } else if (component instanceof FTPTransfer) {
                Object obj = ((FTPTransfer) component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation) ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            } else if (component instanceof FTPOperation) {
                Object obj = ((FTPOperation) component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
                    Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFTPBinding(bindings.iterator().next(), null);
                    }
                }
            }
        }
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

        postSendCmdCombo.addItemListener(mItemListener);
        postSendLocHasPattern.addItemListener(mItemListener);
        preSendCmdCombo.addItemListener(mItemListener);
        preSendLocHasPattern.addItemListener(mItemListener);
        sendToHasPatternCheck.addItemListener(mItemListener);

        postSendLocText.addActionListener(mActionListener);
        preSendLocText.addActionListener(mActionListener);
        sendToText.addActionListener(mActionListener);

        postSendLocText.getDocument().addDocumentListener(mDocumentListener);
        preSendLocText.getDocument().addDocumentListener(mDocumentListener);
        sendToText.getDocument().addDocumentListener(mDocumentListener);
    }

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
            if (mProxy != null) {
                ((ValidationProxy) mProxy).validatePlugin();
            } else {
                validateMe(true);
            }
        }

        // Handle deletions	from the text field
        public void removeUpdate(DocumentEvent event) {
            if (mProxy != null) {
                ((ValidationProxy) mProxy).validatePlugin();
            } else {
                validateMe(true);
            }
        }

        // Handle changes to the text field
        public void changedUpdate(DocumentEvent event) {
            // empty
        }
    }

    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getSource() == preSendCmdCombo) {
            handlePreSendCmdComboChange(evt);
        } else if (evt.getSource() == postSendCmdCombo) {
            handlePostSendCmdComboChange();
        }
    }

    private void handlePostSendCmdComboChange() {
        if (mProxy != null) {
            ((ValidationProxy) mProxy).validatePlugin();
        } else {
            validateMe(true);
        }
    }

    private void handlePreSendCmdComboChange(ItemEvent evt) {
        if (mProxy != null) {
            ((ValidationProxy) mProxy).validatePlugin();
        } else {
            validateMe(true);
        }
    }

    private void handleActionPerformed(ActionEvent evt) {
    }

    private void updateDescriptionArea(FocusEvent evt) {
        descPanel.setText("");
        String[] desc = null;
        if (evt.getSource() == appendCheck) {
            desc = new String[]{"Append Payload To Target File\n\n",
                        mBundle.getString("DESC_Attribute_append")
                    };
        } else if (evt.getSource() == postSendCmdCombo) {
            desc = new String[]{"Post Send Operation\n\n",
                        mBundle.getString("DESC_Attribute_postSendCommand")
                    };
        } else if (evt.getSource() == postSendLocHasPattern) {
            desc = new String[]{"Post Send Location Has Patterns\n\n",
                        mBundle.getString("DESC_Attribute_postSendLocationHasPatterns")
                    };
        } else if (evt.getSource() == postSendLocText) {
            desc = new String[]{"Post Send Location\n\n",
                        mBundle.getString("DESC_Attribute_postSendLocation")
                    };
        } else if (evt.getSource() == preSendCmdCombo) {
            desc = new String[]{"Pre Send Operation\n\n",
                        mBundle.getString("DESC_Attribute_preSendCommand")
                    };
        } else if (evt.getSource() == preSendLocHasPattern) {
            desc = new String[]{"Pre Send Location Has Patterns\n\n",
                        mBundle.getString("DESC_Attribute_preSendLocationHasPatterns")
                    };
        } else if (evt.getSource() == preSendLocText) {
            desc = new String[]{"Pre Send Location\n\n",
                        mBundle.getString("DESC_Attribute_preSendLocation")
                    };
        } else if (evt.getSource() == sendToHasPatternCheck) {
            desc = new String[]{"Send Destination has patterns\n\n",
                        mBundle.getString("DESC_Attribute_sendToHasPatterns")
                    };
        } else if (evt.getSource() == sendToText) {
            desc = new String[]{"Send Destination\n\n",
                        mBundle.getString("DESC_Attribute_sendTo")
                    };
        }

        if (desc != null) {
            descPanel.setText(desc[0], desc[1]);
        }
    }

    /**
     * Return the operation name
     * @return String operation name
     */
    public String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(FTPConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
        if (mMessageTypePanel != null) {
            mMessageTypePanel.enablePayloadProcessing(enable);
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

        jSplitPane1 = new javax.swing.JSplitPane();
        outboundTransferingConfigPanel = new javax.swing.JPanel();
        putReqOrRespConfigPanel = new javax.swing.JPanel();
        titleLab = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        sendToLab = new javax.swing.JLabel();
        sendToText = new javax.swing.JTextField();
        preSendLocLab = new javax.swing.JLabel();
        preSendLocText = new javax.swing.JTextField();
        postSendLocLab = new javax.swing.JLabel();
        postSendLocText = new javax.swing.JTextField();
        sendToHasPatternCheck = new javax.swing.JCheckBox();
        preSendCmdCombo = new javax.swing.JComboBox();
        preSendCmdLab = new javax.swing.JLabel();
        postSendCmdCombo = new javax.swing.JComboBox();
        postSendCmdLab = new javax.swing.JLabel();
        preSendLocHasPattern = new javax.swing.JCheckBox();
        postSendLocHasPattern = new javax.swing.JCheckBox();
        appendCheck = new javax.swing.JCheckBox();
        sendToImgLab = new javax.swing.JLabel();
        sendToPattImgLab = new javax.swing.JLabel();
        preSendCmdImgLab = new javax.swing.JLabel();
        preSendLocImgLab = new javax.swing.JLabel();
        preSendHasPattImgLab = new javax.swing.JLabel();
        postSendCmdImgLab = new javax.swing.JLabel();
        postSendLocImgLab = new javax.swing.JLabel();
        postSendHasPattImgLab = new javax.swing.JLabel();
        appendImgLab = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(612, 730));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setPreferredSize(new java.awt.Dimension(612, 730));

        outboundTransferingConfigPanel.setMinimumSize(new java.awt.Dimension(610, 690));
        outboundTransferingConfigPanel.setName("outboundTransferingConfigPanel"); // NOI18N
        outboundTransferingConfigPanel.setPreferredSize(new java.awt.Dimension(610, 690));
        outboundTransferingConfigPanel.setLayout(new java.awt.GridBagLayout());

        putReqOrRespConfigPanel.setMinimumSize(new java.awt.Dimension(610, 430));
        putReqOrRespConfigPanel.setName("putReqOrRespConfigPanel"); // NOI18N
        putReqOrRespConfigPanel.setPreferredSize(new java.awt.Dimension(610, 430));
        putReqOrRespConfigPanel.setLayout(new java.awt.GridBagLayout());

        titleLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(titleLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.titleLab.text")); // NOI18N
        titleLab.setName("titleLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(titleLab, gridBagConstraints);

        jSeparator1.setMinimumSize(new java.awt.Dimension(0, 1));
        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 100, 0, 10);
        putReqOrRespConfigPanel.add(jSeparator1, gridBagConstraints);

        sendToLab.setLabelFor(sendToText);
        org.openide.awt.Mnemonics.setLocalizedText(sendToLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToLab.text")); // NOI18N
        sendToLab.setName("sendToLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(sendToLab, gridBagConstraints);
        sendToLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToLab.AccessibleContext.accessibleDescription")); // NOI18N

        sendToText.setName("sendToText"); // NOI18N
        sendToText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sendToTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sendToTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(sendToText, gridBagConstraints);
        sendToText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToText.AccessibleContext.accessibleName")); // NOI18N
        sendToText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToText.AccessibleContext.accessibleDescription")); // NOI18N

        preSendLocLab.setLabelFor(preSendLocText);
        org.openide.awt.Mnemonics.setLocalizedText(preSendLocLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocLab.text")); // NOI18N
        preSendLocLab.setName("preSendLocLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(preSendLocLab, gridBagConstraints);
        preSendLocLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocLab.AccessibleContext.accessibleDescription")); // NOI18N

        preSendLocText.setName("preSendLocText"); // NOI18N
        preSendLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preSendLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preSendLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(preSendLocText, gridBagConstraints);
        preSendLocText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocText.AccessibleContext.accessibleName")); // NOI18N
        preSendLocText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocText.AccessibleContext.accessibleDescription")); // NOI18N

        postSendLocLab.setLabelFor(postSendLocText);
        org.openide.awt.Mnemonics.setLocalizedText(postSendLocLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocLab.text")); // NOI18N
        postSendLocLab.setName("postSendLocLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(postSendLocLab, gridBagConstraints);
        postSendLocLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocLab.AccessibleContext.accessibleDescription")); // NOI18N

        postSendLocText.setName("postSendLocText"); // NOI18N
        postSendLocText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postSendLocTextActionPerformed(evt);
            }
        });
        postSendLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postSendLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postSendLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(postSendLocText, gridBagConstraints);
        postSendLocText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocText.AccessibleContext.accessibleName")); // NOI18N
        postSendLocText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sendToHasPatternCheck, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToHasPatternCheck.text")); // NOI18N
        sendToHasPatternCheck.setName("sendToHasPatternCheck"); // NOI18N
        sendToHasPatternCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sendToHasPatternCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sendToHasPatternCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(sendToHasPatternCheck, gridBagConstraints);
        sendToHasPatternCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToHasPatternCheck.AccessibleContext.accessibleDescription")); // NOI18N

        preSendCmdCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        preSendCmdCombo.setName("preSendCmdCombo"); // NOI18N
        preSendCmdCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preSendCmdComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preSendCmdComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(preSendCmdCombo, gridBagConstraints);
        preSendCmdCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdCombo.AccessibleContext.accessibleName")); // NOI18N
        preSendCmdCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdCombo.AccessibleContext.accessibleDescription")); // NOI18N

        preSendCmdLab.setLabelFor(preSendCmdCombo);
        org.openide.awt.Mnemonics.setLocalizedText(preSendCmdLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdLab.text")); // NOI18N
        preSendCmdLab.setName("preSendCmdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(preSendCmdLab, gridBagConstraints);
        preSendCmdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdLab.AccessibleContext.accessibleDescription")); // NOI18N

        postSendCmdCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        postSendCmdCombo.setName("postSendCmdCombo"); // NOI18N
        postSendCmdCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postSendCmdComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postSendCmdComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(postSendCmdCombo, gridBagConstraints);
        postSendCmdCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdCombo.AccessibleContext.accessibleName")); // NOI18N
        postSendCmdCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdCombo.AccessibleContext.accessibleDescription")); // NOI18N

        postSendCmdLab.setLabelFor(postSendCmdCombo);
        org.openide.awt.Mnemonics.setLocalizedText(postSendCmdLab, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdLab.text")); // NOI18N
        postSendCmdLab.setName("postSendCmdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(postSendCmdLab, gridBagConstraints);
        postSendCmdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdLab.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(preSendLocHasPattern, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocHasPattern.text")); // NOI18N
        preSendLocHasPattern.setName("preSendLocHasPattern"); // NOI18N
        preSendLocHasPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preSendLocHasPatternActionPerformed(evt);
            }
        });
        preSendLocHasPattern.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preSendLocHasPatternFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preSendLocHasPatternFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(preSendLocHasPattern, gridBagConstraints);
        preSendLocHasPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocHasPattern.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(postSendLocHasPattern, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocHasPattern.text")); // NOI18N
        postSendLocHasPattern.setName("postSendLocHasPattern"); // NOI18N
        postSendLocHasPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postSendLocHasPatternActionPerformed(evt);
            }
        });
        postSendLocHasPattern.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postSendLocHasPatternFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postSendLocHasPatternFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(postSendLocHasPattern, gridBagConstraints);
        postSendLocHasPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocHasPattern.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(appendCheck, org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.appendCheck.text")); // NOI18N
        appendCheck.setName("appendCheck"); // NOI18N
        appendCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                appendCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                appendCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(appendCheck, gridBagConstraints);
        appendCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.appendCheck.AccessibleContext.accessibleDescription")); // NOI18N

        sendToImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        sendToImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToImgLab.text")); // NOI18N
        sendToImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToImgLab.toolTipText")); // NOI18N
        sendToImgLab.setName("sendToImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(sendToImgLab, gridBagConstraints);

        sendToPattImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        sendToPattImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToPattImgLab.text")); // NOI18N
        sendToPattImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.sendToPattImgLab.toolTipText")); // NOI18N
        sendToPattImgLab.setName("sendToPattImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(sendToPattImgLab, gridBagConstraints);

        preSendCmdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preSendCmdImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdImgLab.text")); // NOI18N
        preSendCmdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendCmdImgLab.toolTipText")); // NOI18N
        preSendCmdImgLab.setName("preSendCmdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(preSendCmdImgLab, gridBagConstraints);

        preSendLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preSendLocImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocImgLab.text")); // NOI18N
        preSendLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendLocImgLab.toolTipText")); // NOI18N
        preSendLocImgLab.setName("preSendLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(preSendLocImgLab, gridBagConstraints);

        preSendHasPattImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preSendHasPattImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendHasPattImgLab.text")); // NOI18N
        preSendHasPattImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.preSendHasPattImgLab.toolTipText")); // NOI18N
        preSendHasPattImgLab.setName("preSendHasPattImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(preSendHasPattImgLab, gridBagConstraints);

        postSendCmdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postSendCmdImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdImgLab.text")); // NOI18N
        postSendCmdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendCmdImgLab.toolTipText")); // NOI18N
        postSendCmdImgLab.setName("postSendCmdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(postSendCmdImgLab, gridBagConstraints);

        postSendLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postSendLocImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocImgLab.text")); // NOI18N
        postSendLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendLocImgLab.toolTipText")); // NOI18N
        postSendLocImgLab.setName("postSendLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(postSendLocImgLab, gridBagConstraints);

        postSendHasPattImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postSendHasPattImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendHasPattImgLab.text")); // NOI18N
        postSendHasPattImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.postSendHasPattImgLab.toolTipText")); // NOI18N
        postSendHasPattImgLab.setName("postSendHasPattImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(postSendHasPattImgLab, gridBagConstraints);

        appendImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        appendImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.appendImgLab.text")); // NOI18N
        appendImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.appendImgLab.toolTipText")); // NOI18N
        appendImgLab.setName("appendImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(appendImgLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        outboundTransferingConfigPanel.add(putReqOrRespConfigPanel, gridBagConstraints);
        putReqOrRespConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.putReqOrRespConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        putReqOrRespConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.putReqOrRespConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(outboundTransferingConfigPanel);
        outboundTransferingConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.outboundTransferingConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        outboundTransferingConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundTransferPanel.class, "OutboundTransferPanel.outboundTransferingConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descriptionPanel.setMinimumSize(new java.awt.Dimension(610, 40));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(610, 40));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void postSendLocTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postSendLocTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_postSendLocTextActionPerformed

private void preSendLocHasPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preSendLocHasPatternActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_preSendLocHasPatternActionPerformed

private void postSendLocHasPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postSendLocHasPatternActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_postSendLocHasPatternActionPerformed

private void sendToTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sendToTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_sendToTextFocusGained

private void sendToTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sendToTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_sendToTextFocusLost

private void sendToHasPatternCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sendToHasPatternCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_sendToHasPatternCheckFocusGained

private void sendToHasPatternCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sendToHasPatternCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_sendToHasPatternCheckFocusLost

private void preSendCmdComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendCmdComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preSendCmdComboFocusGained

private void preSendCmdComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendCmdComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preSendCmdComboFocusLost

private void preSendLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendLocTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preSendLocTextFocusGained

private void preSendLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preSendLocTextFocusLost

private void preSendLocHasPatternFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendLocHasPatternFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preSendLocHasPatternFocusGained

private void preSendLocHasPatternFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preSendLocHasPatternFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preSendLocHasPatternFocusLost

private void postSendCmdComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendCmdComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postSendCmdComboFocusGained

private void postSendCmdComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendCmdComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_postSendCmdComboFocusLost

private void postSendLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendLocTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postSendLocTextFocusGained

private void postSendLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_postSendLocTextFocusLost

private void postSendLocHasPatternFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendLocHasPatternFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postSendLocHasPatternFocusGained

private void postSendLocHasPatternFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postSendLocHasPatternFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_postSendLocHasPatternFocusLost

private void appendCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_appendCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_appendCheckFocusGained

private void appendCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_appendCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_appendCheckFocusLost

    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        boolean result = false;
        ErrorDescription error = validateMe();
        if (error != null && ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT.equals(error.getErrorMode())) {
        } else if (mWsdlComponent instanceof FTPAddress) {
            result = commitAddress((FTPAddress) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPBinding) {
            result = commitBinding((FTPBinding) mWsdlComponent);
        } else if (mWsdlComponent instanceof Port) {
            result = commitPort((Port) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPTransfer) {
            result = commitMessage((FTPTransfer) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPOperation) {
            result = commitOperation((FTPOperation) mWsdlComponent);
        }
        return result;
    }

    public ErrorDescription validateMe() {
        return validateMe(false);
    }

    public ErrorDescription validateMe(boolean fireEvt) {
        ErrorDescription error = null;
        // sendTo must be specified
        // if pre/post send operation is not NONE
        // then the corresponding location is required
        // 
        if (sendToText.getText() == null || sendToText.getText().trim().length() == 0) {
            error = Utilities.setError(error, "TransferConfiguration.MISSING_SENDTO");
        } else {
            String op = "NONE";

            if (preSendCmdCombo.getSelectedItem() != null) {
                op = preSendCmdCombo.getSelectedItem().toString();
            }

            if (!op.equals("NONE") && (preSendLocText.getText() == null || preSendLocText.getText().trim().length() == 0)) {
                // copy and rename need location
                error = Utilities.setError(error, "TransferConfiguration.MISSING_PRE_OP_LOC", new Object[]{op});
            } else {
                op = "NONE";
                if (postSendCmdCombo.getSelectedItem() != null) {
                    op = postSendCmdCombo.getSelectedItem().toString();
                }
                if (op.equals("RENAME") && (postSendLocText.getText() == null || postSendLocText.getText().trim().length() == 0)) {
                    error = Utilities.setError(error, "TransferConfiguration.MISSING_POST_OP_LOC", new Object[]{op});
                }
            }
        }

        // self ok, check the sub-panel
        if (error == null || error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT)) {
            error = mMessageTypePanel.validateMe(fireEvt, true);
        }

        if (fireEvt) {
            if (error != null && error.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                doFirePropertyChange(error.getErrorMode(), null, error.getErrorMessage());
            } else {
                doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT,
                        null, "");
            }
        }

        return error;
    }

    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {
        if (mProxy != null) {
            mProxy.doFirePropertyChange(name, oldValue, newValue);
        } else {
            firePropertyChange(name, oldValue,
                    newValue);
        }
    }

    private boolean commitAddress(FTPAddress ftpAddress) {
        WSDLModel wsdlModel = ftpAddress.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            // directory path service info will be filled in by inbound

            Port port = (Port) ftpAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = getOperationName();

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            // only 1
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    // put message is inside <input> if is for request
                    // put message is inside <output> if is for response
                    FTPTransfer putTransfer = null;
                    List<FTPTransfer> putFTPTransfers = null;
                    List<FTPMessage> putFTPMessages = null;
                    if (bRequest) {
                        BindingInput bi = bop.getBindingInput();
                        if (bi != null) {
                            putFTPTransfers =
                                    bi.getExtensibilityElements(FTPTransfer.class);
                            if (putFTPTransfers.size() > 0) {
                                putTransfer = putFTPTransfers.get(0);
                            } else {
                                // no ftp:transfer, then it might be ftp:message
                                putFTPMessages = bi.getExtensibilityElements(FTPMessage.class);
                                if (putFTPMessages != null && putFTPMessages.size() > 0) {
                                    for (int i = 0; i < putFTPMessages.size(); i++) {
                                        if (putFTPMessages.get(i) != null) {
                                            bi.removeExtensibilityElement(putFTPMessages.get(i));
                                        }
                                    }
                                }
                                bi.addExtensibilityElement((putTransfer = (FTPTransfer) Utilities.createFTPTransfer(mWsdlComponent)));
                            }
                        }
                    } else {
                        BindingOutput bo = bop.getBindingOutput();
                        if (bo != null) {
                            putFTPTransfers = bo.getExtensibilityElements(FTPTransfer.class);
                            if (putFTPTransfers.size() > 0) {
                                putTransfer = putFTPTransfers.get(0);
                            } else {
                                // no ftp:transfer, then it might be ftp:message
                                putFTPMessages = bo.getExtensibilityElements(FTPMessage.class);
                                if (putFTPMessages != null && putFTPMessages.size() > 0) {
                                    for (int i = 0; i < putFTPMessages.size(); i++) {
                                        if (putFTPMessages.get(i) != null) {
                                            bo.removeExtensibilityElement(putFTPMessages.get(i));
                                        }
                                    }
                                }
                                bo.addExtensibilityElement((putTransfer = (FTPTransfer) Utilities.createFTPTransfer(mWsdlComponent)));
                            }
                        }
                    }
                    commitPutTransfer(binding, operationName, putTransfer);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
//        finally {
//            if (wsdlModel.isIntransaction()) {
//                wsdlModel.endTransaction();
//            }
//            return true;
//        }
        return true;
    }

    private boolean commitBinding(FTPBinding ftpBinding) {
        WSDLModel wsdlModel = ftpBinding.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } finally {
//            if (wsdlModel.isIntransaction()) {
//                wsdlModel.endTransaction();
//            }
            return true;
        }
    }

    private void commitPutTransfer(Binding binding, String opName,
            FTPTransfer putTransfer) {
        putTransfer.setSendTo(sendToText.getText().trim());
        putTransfer.setSendToHasPatterns(sendToHasPatternCheck.isSelected());
        putTransfer.setPreSendCommand(preSendCmdCombo.getSelectedItem() != null ? preSendCmdCombo.getSelectedItem().toString() : "NONE");
        putTransfer.setPreSendLocation(preSendLocText.getText().trim());
        putTransfer.setPreSendLocationHasPatterns(preSendLocHasPattern.isSelected());
        putTransfer.setPostSendCommand(postSendCmdCombo.getSelectedItem() != null ? postSendCmdCombo.getSelectedItem().toString() : "NONE");
        putTransfer.setPostSendLocation(postSendLocText.getText().trim());
        putTransfer.setPostSendLocationHasPatterns(postSendLocHasPattern.isSelected());

        putTransfer.setAppend(appendCheck.isSelected());
        
        Boolean b = null;
        if (mWzdProps != null) {
            b = (Boolean) mWzdProps.get(FTPConstants.WSDL_PROP_REQRESPCORRELATE);
        }

        // from prev panel
        putTransfer.setMessageCorrelateEnabled(b != null ? b.booleanValue() : true);

        if ( bOneWay )
            putTransfer.setMessageCorrelateEnabled(false);

        putTransfer.setUse(FTPConstants.LITERAL);
        if (mMessageTypePanel.getInputUseType() != null) {
            if (mMessageTypePanel.getInputUseType().equals(FTPConstants.ENCODED)) {        
                putTransfer.setEncodingStyle(mMessageTypePanel.getEncodingStyle());
                putTransfer.setUse(mMessageTypePanel.getInputUseType());
            } else if ((putTransfer.getUse() != null) &&
                        (mMessageTypePanel.getInputUseType().equals(FTPConstants.LITERAL))) {
                    putTransfer.setEncodingStyle(null);                   
            }
        } else {
            putTransfer.setAttribute(FTPMessage.FTP_ENCODINGSTYLE_PROPERTY, null);
        }
        
        if ( mMessageTypePanel.getMessageType() == FTPConstants.BINARY_MESSAGE_TYPE )
            putTransfer.setFileType(FTPConstants.BINARY);
        else {
            putTransfer.setFileType(FTPConstants.TEXT);
            String enc = mMessageTypePanel.getMessageCharEncoding();
            if ( enc != null && enc.trim().length() > 0 ) {
                putTransfer.setCharacterEncoding(enc.trim());
            }
        }
        // set the type of the message part if it is undefined based
        // on the MessageType option from user
        commitMessageType(binding, opName, putTransfer);
    }

    private boolean commitPort(Port port) {
        Collection<FTPAddress> address = port.getExtensibilityElements(FTPAddress.class);
        if (address != null && address.size() > 0) {
            FTPAddress ftpAddress = address.iterator().next();
            return commitAddress(ftpAddress);
        }
        return false;
    }

    private boolean commitMessage(FTPTransfer ftpTransfer) {
        Object parentObj = ftpTransfer.getParent();
        FTPBinding ftpBinding = null;
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp = (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof BindingOutput) {
            parentOp = (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    private boolean commitOperation(FTPOperation fileOperation) {
        Object obj = fileOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding = (Binding) ((BindingOperation) obj).getParent();
            Collection<FTPBinding> bindings = parentBinding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    private void commitMessageType(Binding binding, String opName,
            FTPTransfer putFTPMessage) {
        String partName = getOutputPart();
        if (partName != null) {
            Collection parts = null;
            parts = bRequest ? Utilities.getInputParts(binding, opName) : Utilities.getOutputParts(binding, opName);
            if ((parts != null) && (parts.size() > 0)) {
                Iterator iter = parts.iterator();
                while (iter.hasNext()) {
                    Part partEntry = (Part) iter.next();
                    if (partEntry.getName().equals(partName)) {
                        Utilities.setPartType(partEntry, getMessageType(),
                                getSelectedPartType(), getSelectedElementType());
                    }
                }
            }
            putFTPMessage.setPart(partName);
        } else {
            // need to create a message with part
            Message newMessage = Utilities.createMessage(mWsdlComponent.getModel(), "PutTransfer");
            if (newMessage != null) {
                Part newPart = Utilities.createPart(mWsdlComponent.getModel(),
                        newMessage, "part1");
                if (newPart != null) {
                    Utilities.setPartType(newPart, getMessageType(),
                            getSelectedPartType(), getSelectedElementType());

                    if (Utilities.getMessagePart(newPart.getName(), newMessage) == null) {
                        newMessage.addPart(newPart);
                    }
                    putFTPMessage.setPart(newPart.getName());
                }
            }
        }
    }

    /**
     * Returns the selected part 
     * @return
     */
    public GlobalType getSelectedPartType() {
        return mMessageTypePanel.getSelectedPartType();
    }

    /**
     * Returns the selected element
     * @return
     */
    public GlobalElement getSelectedElementType() {
        return mMessageTypePanel.getSelectedElementType();
    }

    /**
     * Return message type.  Options are FileConstants.XML_MESSAGE_TYPE,
     * FileConstants.TEXT_MESSAGE_TYPE, FileConstants.ENCODED_MESSAGE_TYPE
     * 
     * @return
     */
    int getMessageType() {
        return mMessageTypePanel.getMessageType();
    }

    /**
     * Return the part value for the input message
     * return String part used
     */
    String getOutputPart() {
        String part = (String) partComboBox.getSelectedItem();
        if (part != null) {
            part = part.trim();
        }
        return part;
    }

    /**
     * Return the use type for input message
     * @return String use type
     */
    String getInputUseType() {
        return mMessageTypePanel.getInputUseType();
    }

    /**
     * Return the encoding style value
     * @return String encoding style
     */
    String getEncodingStyle() {
        return mMessageTypePanel.getEncodingStyle();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox appendCheck;
    private javax.swing.JLabel appendImgLab;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel outboundTransferingConfigPanel;
    private javax.swing.JComboBox postSendCmdCombo;
    private javax.swing.JLabel postSendCmdImgLab;
    private javax.swing.JLabel postSendCmdLab;
    private javax.swing.JLabel postSendHasPattImgLab;
    private javax.swing.JCheckBox postSendLocHasPattern;
    private javax.swing.JLabel postSendLocImgLab;
    private javax.swing.JLabel postSendLocLab;
    private javax.swing.JTextField postSendLocText;
    private javax.swing.JComboBox preSendCmdCombo;
    private javax.swing.JLabel preSendCmdImgLab;
    private javax.swing.JLabel preSendCmdLab;
    private javax.swing.JLabel preSendHasPattImgLab;
    private javax.swing.JCheckBox preSendLocHasPattern;
    private javax.swing.JLabel preSendLocImgLab;
    private javax.swing.JLabel preSendLocLab;
    private javax.swing.JTextField preSendLocText;
    private javax.swing.JPanel putReqOrRespConfigPanel;
    private javax.swing.JCheckBox sendToHasPatternCheck;
    private javax.swing.JLabel sendToImgLab;
    private javax.swing.JLabel sendToLab;
    private javax.swing.JLabel sendToPattImgLab;
    private javax.swing.JTextField sendToText;
    private javax.swing.JLabel titleLab;
    // End of variables declaration//GEN-END:variables

    
    public boolean getRequestResponseCorrelate() {
        return bRequestResponseCorrelate;
    }

    public Map exportProperties() {
        // nothing to export - but pass it on if any
        return mWzdProps;
    }

    public void importProperties(Map p) {
        mWzdProps = p;
    }

    public void ancestorAdded(AncestorEvent event) {
        if (event.getSource() == this) {
            if (mProxy != null) {
                // embedded in CASA plugin
                // need to validate all the tabbed panels
                ((ValidationProxy) mProxy).validatePlugin();
            } else {
                validateMe(true);
            }
        }
    }

    public void ancestorRemoved(AncestorEvent event) {
        // not interested
    }

    public void ancestorMoved(AncestorEvent event) {
        // not interested
    }

    public boolean isValidConfiguration() {
        boolean result = true;
        ErrorDescription desc = validateMe();
        if (desc != null && desc.getErrorMode() != null && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            result = false;
        }
        return result;
    }
}
