/*
 * InboundTransferPanel.java
 *
 * Created on September 19, 2008, 7:28 PM
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
public class InboundTransferPanel extends javax.swing.JPanel implements AncestorListener, PropertyAccessible, PropertyChangeSupport, BindingConfigurationDelegate {

    private MessageTypePanel mMessageTypePanel;
    /** the WSDL model to configure **/
    private WSDLComponent mWsdlComponent;
    /** QName **/
    private QName mQName;
    /**
     * Project associated with this wsdl
     */
    private Project mProject = null;
    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ftp.resources.Bundle");
    private static final Logger mLogger = Logger.getLogger(InboundTransferPanel.class.getName());
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
    private boolean mSolicit;
    private boolean bOneWay;
    private boolean bRequestResponseCorrelate;
    
    /** Creates new form InboundTransferPanel */
    public InboundTransferPanel(QName qName, WSDLComponent component, boolean oneway, boolean b, boolean solicit) {
        this(qName, component, oneway, b, solicit, null);
    }

    public InboundTransferPanel(QName qName, WSDLComponent component, boolean oneway, boolean b, boolean solicit, PropertyChangeSupport proxy) {
        bRequest = b;
        mProxy = proxy;
        mSolicit = solicit;
        bOneWay = oneway;
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
        return NbBundle.getMessage(InboundTransferPanel.class,
                mSolicit ? "InboundTransferPanel.ReceiveTransfer.StepLabel" :
                (bRequest ? "InboundTransferPanel.Request.StepLabel" : "InboundTransferPanel.Response.StepLabel"));
    }

    public void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
        }
    }

    private BindingOperation getBindingOperation(Collection<BindingOperation> bindingOperations) {
        Iterator iter = bindingOperations.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            return bop;
        }
        return null;
    }

    private void initCustomComponents() {
        mMessageTypePanel = new MessageTypePanel(mWsdlComponent, null, null, null, this, true);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        //gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        inboundTransferingConfigPanel.add(mMessageTypePanel, gridBagConstraints);

        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        inboundTransferingConfigPanel.add(tmpPanel, gridBagConstraints);

        /**
         * invisible components used to keep status:
         */
        servicePortComboBox = new JComboBox();
        bindingNameComboBox = new JComboBox();
        portTypeComboBox = new JComboBox();
        operationNameComboBox = new JComboBox();
        partComboBox = new JComboBox();

        /**
         * for solicit hide and disable poll interval
         */
        if ( mSolicit ) {
            pollIntervalLab.setVisible(false);
            pollIntervalLab.setEnabled(false);
            pollIntervalText.setVisible(false);
            pollIntervalText.setEnabled(false);
        }
        
        /**
         * change title depend on the bRequest
         */
        requestLab.setText(getName());
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
                    FTPTransfer pollTransfer = getInputFTPTransfer(binding,
                            bop.getName());
                    updateInputTransferView(binding, pollTransfer);

                }
            }

        }
    }

    private FTPTransfer getInputFTPTransfer(Binding binding,
            String selectedOperation) {
        FTPTransfer pollFTPTransfer = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    List<FTPTransfer> pollFTPTransfers = null;
                    if (bRequest && !mSolicit ) {
                        pollFTPTransfers = bop.getBindingInput().getExtensibilityElements(FTPTransfer.class);
                    } else {
                        pollFTPTransfers = bop.getBindingOutput().getExtensibilityElements(FTPTransfer.class);
                    }
                    if (pollFTPTransfers.size() > 0) {
                        pollFTPTransfer = pollFTPTransfers.get(0);
                        break;
                    }
                }
            }
        }
        return pollFTPTransfer;
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

    private void resetView() {
        pollIntervalText.getDocument().removeDocumentListener(mDocumentListener);
        postReceiveLocText.getDocument().removeDocumentListener(mDocumentListener);
        preReceiveLocText.getDocument().removeDocumentListener(mDocumentListener);
        receiveFromText.getDocument().removeDocumentListener(mDocumentListener);

        postReceiveCmdCombo.removeItemListener(mItemListener);
        postReceiveLocHasPattern.removeItemListener(mItemListener);
        preReceiveCmdCombo.removeItemListener(mItemListener);
        preReceiveLocHasPattern.removeItemListener(mItemListener);
        receiveFromHasRegexCheck.removeItemListener(mItemListener);

        pollIntervalText.removeActionListener(mActionListener);
        postReceiveLocText.removeActionListener(mActionListener);
        preReceiveLocText.removeActionListener(mActionListener);
        receiveFromText.removeActionListener(mActionListener);

        postReceiveCmdCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.POST_CMD_GET.length; i++) {
            postReceiveCmdCombo.addItem(FTPConstants.POST_CMD_GET[i]);
        }
        postReceiveLocHasPattern.setSelected(false);
        preReceiveCmdCombo.removeAllItems();
        for (int i = 0; i < FTPConstants.PRE_CMD_PUT_GET.length; i++) {
            preReceiveCmdCombo.addItem(FTPConstants.PRE_CMD_PUT_GET[i]);
        }
        preReceiveLocHasPattern.setSelected(false);
        receiveFromHasRegexCheck.setSelected(false);

        servicePortComboBox.removeItemListener(mItemListener);
        bindingNameComboBox.removeItemListener(mItemListener);
        portTypeComboBox.removeItemListener(mItemListener);
        operationNameComboBox.removeItemListener(mItemListener);

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

        postReceiveCmdCombo.addItemListener(mItemListener);
        postReceiveLocHasPattern.addItemListener(mItemListener);
        preReceiveCmdCombo.addItemListener(mItemListener);
        preReceiveLocHasPattern.addItemListener(mItemListener);
        receiveFromHasRegexCheck.addItemListener(mItemListener);

        pollIntervalText.addActionListener(mActionListener);
        postReceiveLocText.addActionListener(mActionListener);
        preReceiveLocText.addActionListener(mActionListener);
        receiveFromText.addActionListener(mActionListener);

        pollIntervalText.getDocument().addDocumentListener(mDocumentListener);
        postReceiveLocText.getDocument().addDocumentListener(mDocumentListener);
        preReceiveLocText.getDocument().addDocumentListener(mDocumentListener);
        receiveFromText.getDocument().addDocumentListener(mDocumentListener);

        servicePortComboBox.addItemListener(mItemListener);
        bindingNameComboBox.addItemListener(mItemListener);
        portTypeComboBox.addItemListener(mItemListener);
        operationNameComboBox.addItemListener(mItemListener);

        pollIntervalText.getDocument().addDocumentListener(mDocumentListener);
        postReceiveLocText.getDocument().addDocumentListener(mDocumentListener);
        preReceiveLocText.getDocument().addDocumentListener(mDocumentListener);
        receiveFromText.getDocument().addDocumentListener(mDocumentListener);

    }

    private void updateInputTransferView(Binding binding, FTPTransfer pollTransfer) {
        postReceiveLocText.setToolTipText(mBundle.getString("DESC_Attribute_postReceiveLocation"));
        preReceiveLocText.setToolTipText(mBundle.getString("DESC_Attribute_preReceiveLocation"));
        receiveFromText.setToolTipText(mBundle.getString("DESC_Attribute_receiveFrom"));
        postReceiveCmdCombo.setToolTipText(mBundle.getString("DESC_Attribute_postReceiveCommand"));
        postReceiveLocHasPattern.setToolTipText(mBundle.getString("DESC_Attribute_postReceiveLocationHasPatterns"));
        preReceiveCmdCombo.setToolTipText(mBundle.getString("DESC_Attribute_preReceiveCommand"));
        preReceiveLocHasPattern.setToolTipText(mBundle.getString("DESC_Attribute_preReceiveLocationHasPatterns"));
        receiveFromHasRegexCheck.setToolTipText(mBundle.getString("DESC_Attribute_receiveFromHasRegexs"));
        
        if ( !mSolicit )
            pollIntervalText.setToolTipText(mBundle.getString("DESC_Attribute_pollIntervalMillis"));

        if (pollTransfer != null) {

            postReceiveLocText.setText(pollTransfer.getPostReceiveLocation());
            preReceiveLocText.setText(pollTransfer.getPreReceiveLocation());
            receiveFromText.setText(pollTransfer.getReceiveFrom());
            postReceiveCmdCombo.setSelectedItem(pollTransfer.getPostReceiveCommand());
            postReceiveLocHasPattern.setSelected(pollTransfer.getPostReceiveLocationHasPatterns());
            preReceiveCmdCombo.setSelectedItem(pollTransfer.getPreReceiveCommand());
            preReceiveLocHasPattern.setSelected(pollTransfer.getPreReceiveLocationHasPatterns());
            receiveFromHasRegexCheck.setSelected(pollTransfer.getReceiveFromHasPatterns());
            bRequestResponseCorrelate = pollTransfer.getMessageCorrelateEnabled();
            
            if ( !mSolicit )
                pollIntervalText.setText(pollTransfer.getPollInterval());

            Object obj = pollTransfer.getParent();
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

            // BASED on Message Type selected, need to check if Part selected has a type            
            partComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = pollTransfer.getPart();
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
                NamedComponentReference<Message> transferPoll = null;
                if (bRequest && !mSolicit ) {
                    transferPoll = op.getInput().getMessage();
                } else {
                    transferPoll = op.getOutput().getMessage();
                }
                if (partComboBox.getSelectedItem() != null) {
                    if (part != null) {
                        mPart = Utilities.getMessagePart(part, transferPoll.get());
                    }
                }
            }

            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    pollTransfer, mProject,
                    operationNameComboBox.getSelectedItem() != null ? operationNameComboBox.getSelectedItem().toString() : "");

        } else {
            // null out view
            postReceiveLocText.setText("");
            preReceiveLocText.setText("");
            receiveFromText.setText("");
            postReceiveCmdCombo.setSelectedItem("");
            postReceiveLocHasPattern.setSelected(false);
            preReceiveCmdCombo.setSelectedItem("");
            preReceiveLocHasPattern.setSelected(false);
            receiveFromHasRegexCheck.setSelected(false);
            if ( !mSolicit )
                pollIntervalText.setText("");
            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    pollTransfer, null, null);
        }
        validateMe(true);
    }

    private void updateServiceView(FTPAddress address) {
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
        if (evt.getSource() == preReceiveCmdCombo) {
            handlePreReceiveCmdComboChange(evt);
        } else if (evt.getSource() == postReceiveCmdCombo) {
            handlePostReceiveCmdComboChange();
        }
    }

    private void handlePostReceiveCmdComboChange() {
        if (mProxy != null) {
            ((ValidationProxy) mProxy).validatePlugin();
        } else {
            validateMe(true);
        }
    }

    private void handlePreReceiveCmdComboChange(ItemEvent evt) {
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

        if (evt.getSource() == pollIntervalText) {
            desc = new String[]{"Poll Interval\n\n",
                        mBundle.getString("DESC_Attribute_pollIntervalMillis")
                    };
        } else if (evt.getSource() == postReceiveCmdCombo) {
            desc = new String[]{"Post Receive Operation\n\n",
                        mBundle.getString("DESC_Attribute_postReceiveCommand")
                    };
        } else if (evt.getSource() == postReceiveLocHasPattern) {
            desc = new String[]{"Post Receive Location Has Patterns\n\n",
                        mBundle.getString("DESC_Attribute_postReceiveLocationHasPatterns")
                    };
        } else if (evt.getSource() == postReceiveLocText) {
            desc = new String[]{"Post Receive Location\n\n",
                        mBundle.getString("DESC_Attribute_postReceiveLocation")
                    };
        } else if (evt.getSource() == preReceiveCmdCombo) {
            desc = new String[]{"Pre Receive Operation\n\n",
                        mBundle.getString("DESC_Attribute_preReceiveCommand")
                    };
        } else if (evt.getSource() == preReceiveLocHasPattern) {
            desc = new String[]{"Pre Receive Location Has Patterns\n\n",
                        mBundle.getString("DESC_Attribute_preReceiveLocationHasPatterns")
                    };
        } else if (evt.getSource() == preReceiveLocText) {
            desc = new String[]{"Pre Receive Location\n\n",
                        mBundle.getString("DESC_Attribute_preReceiveLocation")
                    };
        } else if (evt.getSource() == receiveFromHasRegexCheck) {
            desc = new String[]{"Receive Source Has Regular Expressions\n\n",
                        mBundle.getString("DESC_Attribute_receiveFromHasRegexs")
                    };
        } else if (evt.getSource() == receiveFromText) {
            desc = new String[]{"Receive Source\n\n",
                        mBundle.getString("DESC_Attribute_receiveFrom")
                    };
        }

        if (desc != null) {
            descPanel.setText(desc[0], desc[1]);
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
        inboundTransferingConfigPanel = new javax.swing.JPanel();
        pollReqOrRespConfigPanel = new javax.swing.JPanel();
        requestLab = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        receiveFromLab = new javax.swing.JLabel();
        receiveFromText = new javax.swing.JTextField();
        preReceiveLocLab = new javax.swing.JLabel();
        preReceiveLocText = new javax.swing.JTextField();
        postReceiveLocLab = new javax.swing.JLabel();
        postReceiveLocText = new javax.swing.JTextField();
        pollIntervalLab = new javax.swing.JLabel();
        pollIntervalText = new javax.swing.JTextField();
        receiveFromHasRegexCheck = new javax.swing.JCheckBox();
        preReceiveCmdCombo = new javax.swing.JComboBox();
        preReceiveCmdLab = new javax.swing.JLabel();
        postReceiveCmdCombo = new javax.swing.JComboBox();
        postReceiveCmdLab = new javax.swing.JLabel();
        preReceiveLocHasPattern = new javax.swing.JCheckBox();
        postReceiveLocHasPattern = new javax.swing.JCheckBox();
        recvFromImgLab = new javax.swing.JLabel();
        recvHasRegexImgLab = new javax.swing.JLabel();
        preRecvCmdImgLab = new javax.swing.JLabel();
        preRecvLocImgLab = new javax.swing.JLabel();
        preRecvHasPattImgLab = new javax.swing.JLabel();
        postRecvCmdImgLab = new javax.swing.JLabel();
        postRecvLocImgLab = new javax.swing.JLabel();
        postRecvHasPattImgLab = new javax.swing.JLabel();
        pollIntervalImgLab = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(612, 767));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setPreferredSize(new java.awt.Dimension(612, 767));

        inboundTransferingConfigPanel.setMinimumSize(new java.awt.Dimension(610, 690));
        inboundTransferingConfigPanel.setName("inboundTransferingConfigPanel"); // NOI18N
        inboundTransferingConfigPanel.setPreferredSize(new java.awt.Dimension(610, 690));
        inboundTransferingConfigPanel.setLayout(new java.awt.GridBagLayout());

        pollReqOrRespConfigPanel.setMinimumSize(new java.awt.Dimension(610, 430));
        pollReqOrRespConfigPanel.setName("pollReqOrRespConfigPanel"); // NOI18N
        pollReqOrRespConfigPanel.setPreferredSize(new java.awt.Dimension(610, 430));
        pollReqOrRespConfigPanel.setLayout(new java.awt.GridBagLayout());

        requestLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(requestLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.requestLab.text")); // NOI18N
        requestLab.setName("requestLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(requestLab, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(10, 150, 10, 10);
        pollReqOrRespConfigPanel.add(jSeparator1, gridBagConstraints);

        receiveFromLab.setLabelFor(receiveFromText);
        org.openide.awt.Mnemonics.setLocalizedText(receiveFromLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromLab.text")); // NOI18N
        receiveFromLab.setName("receiveFromLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(receiveFromLab, gridBagConstraints);
        receiveFromLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromLab.AccessibleContext.accessibleDescription")); // NOI18N

        receiveFromText.setName("receiveFromText"); // NOI18N
        receiveFromText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                receiveFromTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                receiveFromTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(receiveFromText, gridBagConstraints);
        receiveFromText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromText.AccessibleContext.accessibleName")); // NOI18N
        receiveFromText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromText.AccessibleContext.accessibleDescription")); // NOI18N

        preReceiveLocLab.setLabelFor(preReceiveLocText);
        org.openide.awt.Mnemonics.setLocalizedText(preReceiveLocLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocLab.text")); // NOI18N
        preReceiveLocLab.setName("preReceiveLocLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(preReceiveLocLab, gridBagConstraints);
        preReceiveLocLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocLab.AccessibleContext.accessibleDescription")); // NOI18N

        preReceiveLocText.setName("preReceiveLocText"); // NOI18N
        preReceiveLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preReceiveLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preReceiveLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(preReceiveLocText, gridBagConstraints);
        preReceiveLocText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocText.AccessibleContext.accessibleName")); // NOI18N
        preReceiveLocText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocText.AccessibleContext.accessibleDescription")); // NOI18N

        postReceiveLocLab.setLabelFor(postReceiveLocText);
        org.openide.awt.Mnemonics.setLocalizedText(postReceiveLocLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocLab.text")); // NOI18N
        postReceiveLocLab.setName("postReceiveLocLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(postReceiveLocLab, gridBagConstraints);
        postReceiveLocLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocLab.AccessibleContext.accessibleDescription")); // NOI18N

        postReceiveLocText.setName("postReceiveLocText"); // NOI18N
        postReceiveLocText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postReceiveLocTextActionPerformed(evt);
            }
        });
        postReceiveLocText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postReceiveLocTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postReceiveLocTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(postReceiveLocText, gridBagConstraints);
        postReceiveLocText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocText.AccessibleContext.accessibleName")); // NOI18N
        postReceiveLocText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocText.AccessibleContext.accessibleDescription")); // NOI18N

        pollIntervalLab.setLabelFor(pollIntervalText);
        org.openide.awt.Mnemonics.setLocalizedText(pollIntervalLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalLab.text")); // NOI18N
        pollIntervalLab.setName("pollIntervalLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(pollIntervalLab, gridBagConstraints);
        pollIntervalLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalLab.AccessibleContext.accessibleDescription")); // NOI18N

        pollIntervalText.setName("pollIntervalText"); // NOI18N
        pollIntervalText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pollIntervalTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pollIntervalTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(pollIntervalText, gridBagConstraints);
        pollIntervalText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalText.AccessibleContext.accessibleName")); // NOI18N
        pollIntervalText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(receiveFromHasRegexCheck, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromHasRegexCheck.text")); // NOI18N
        receiveFromHasRegexCheck.setName("receiveFromHasRegexCheck"); // NOI18N
        receiveFromHasRegexCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                receiveFromHasRegexCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                receiveFromHasRegexCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(receiveFromHasRegexCheck, gridBagConstraints);
        receiveFromHasRegexCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.receiveFromHasRegexCheck.AccessibleContext.accessibleDescription")); // NOI18N

        preReceiveCmdCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        preReceiveCmdCombo.setName("preReceiveCmdCombo"); // NOI18N
        preReceiveCmdCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preReceiveCmdComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preReceiveCmdComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(preReceiveCmdCombo, gridBagConstraints);
        preReceiveCmdCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveCmdCombo.AccessibleContext.accessibleName")); // NOI18N
        preReceiveCmdCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveCmdCombo.AccessibleContext.accessibleDescription")); // NOI18N

        preReceiveCmdLab.setLabelFor(preReceiveCmdCombo);
        org.openide.awt.Mnemonics.setLocalizedText(preReceiveCmdLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveCmdLab.text")); // NOI18N
        preReceiveCmdLab.setName("preReceiveCmdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(preReceiveCmdLab, gridBagConstraints);
        preReceiveCmdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveCmdLab.AccessibleContext.accessibleDescription")); // NOI18N

        postReceiveCmdCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        postReceiveCmdCombo.setName("postReceiveCmdCombo"); // NOI18N
        postReceiveCmdCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postReceiveCmdComboFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postReceiveCmdComboFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(postReceiveCmdCombo, gridBagConstraints);
        postReceiveCmdCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveCmdCombo.AccessibleContext.accessibleName")); // NOI18N
        postReceiveCmdCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveCmdCombo.AccessibleContext.accessibleDescription")); // NOI18N

        postReceiveCmdLab.setLabelFor(postReceiveCmdCombo);
        org.openide.awt.Mnemonics.setLocalizedText(postReceiveCmdLab, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveCmdLab.text")); // NOI18N
        postReceiveCmdLab.setName("postReceiveCmdLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(postReceiveCmdLab, gridBagConstraints);
        postReceiveCmdLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveCmdLab.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(preReceiveLocHasPattern, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocHasPattern.text")); // NOI18N
        preReceiveLocHasPattern.setActionCommand(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocHasPattern.actionCommand")); // NOI18N
        preReceiveLocHasPattern.setName("preReceiveLocHasPattern"); // NOI18N
        preReceiveLocHasPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preReceiveLocHasPatternActionPerformed(evt);
            }
        });
        preReceiveLocHasPattern.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                preReceiveLocHasPatternFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                preReceiveLocHasPatternFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(preReceiveLocHasPattern, gridBagConstraints);
        preReceiveLocHasPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preReceiveLocHasPattern.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(postReceiveLocHasPattern, org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocHasPattern.text")); // NOI18N
        postReceiveLocHasPattern.setName("postReceiveLocHasPattern"); // NOI18N
        postReceiveLocHasPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postReceiveLocHasPatternActionPerformed(evt);
            }
        });
        postReceiveLocHasPattern.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                postReceiveLocHasPatternFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                postReceiveLocHasPatternFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(postReceiveLocHasPattern, gridBagConstraints);
        postReceiveLocHasPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postReceiveLocHasPattern.AccessibleContext.accessibleDescription")); // NOI18N

        recvFromImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        recvFromImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.recvFromImgLab.text")); // NOI18N
        recvFromImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.recvFromImgLab.toolTipText")); // NOI18N
        recvFromImgLab.setName("recvFromImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(recvFromImgLab, gridBagConstraints);

        recvHasRegexImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        recvHasRegexImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.recvHasRegexImgLab.text")); // NOI18N
        recvHasRegexImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.recvHasRegexImgLab.toolTipText")); // NOI18N
        recvHasRegexImgLab.setName("recvHasRegexImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(recvHasRegexImgLab, gridBagConstraints);

        preRecvCmdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preRecvCmdImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvCmdImgLab.text")); // NOI18N
        preRecvCmdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvCmdImgLab.toolTipText")); // NOI18N
        preRecvCmdImgLab.setName("preRecvCmdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(preRecvCmdImgLab, gridBagConstraints);

        preRecvLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preRecvLocImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvLocImgLab.text")); // NOI18N
        preRecvLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvLocImgLab.toolTipText")); // NOI18N
        preRecvLocImgLab.setName("preRecvLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(preRecvLocImgLab, gridBagConstraints);

        preRecvHasPattImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        preRecvHasPattImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvHasPattImgLab.text")); // NOI18N
        preRecvHasPattImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.preRecvHasPattImgLab.toolTipText")); // NOI18N
        preRecvHasPattImgLab.setName("preRecvHasPattImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(preRecvHasPattImgLab, gridBagConstraints);

        postRecvCmdImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postRecvCmdImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvCmdImgLab.text")); // NOI18N
        postRecvCmdImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvCmdImgLab.toolTipText")); // NOI18N
        postRecvCmdImgLab.setName("postRecvCmdImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(postRecvCmdImgLab, gridBagConstraints);

        postRecvLocImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postRecvLocImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvLocImgLab.text")); // NOI18N
        postRecvLocImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvLocImgLab.toolTipText")); // NOI18N
        postRecvLocImgLab.setName("postRecvLocImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(postRecvLocImgLab, gridBagConstraints);

        postRecvHasPattImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        postRecvHasPattImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvHasPattImgLab.text")); // NOI18N
        postRecvHasPattImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.postRecvHasPattImgLab.toolTipText")); // NOI18N
        postRecvHasPattImgLab.setName("postRecvHasPattImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(postRecvHasPattImgLab, gridBagConstraints);

        pollIntervalImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        pollIntervalImgLab.setText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalImgLab.text")); // NOI18N
        pollIntervalImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollIntervalImgLab.toolTipText")); // NOI18N
        pollIntervalImgLab.setName("pollIntervalImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(pollIntervalImgLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        inboundTransferingConfigPanel.add(pollReqOrRespConfigPanel, gridBagConstraints);
        pollReqOrRespConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollReqOrRespConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        pollReqOrRespConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.pollReqOrRespConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(inboundTransferingConfigPanel);
        inboundTransferingConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.inboundTransferingConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        inboundTransferingConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.inboundTransferingConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descriptionPanel.setMinimumSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);
        descriptionPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.descriptionPanel.AccessibleContext.accessibleName")); // NOI18N
        descriptionPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundTransferPanel.class, "InboundTransferPanel.descriptionPanel.AccessibleContext.accessibleDescription")); // NOI18N

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void postReceiveLocTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postReceiveLocTextActionPerformed
}//GEN-LAST:event_postReceiveLocTextActionPerformed

private void preReceiveLocHasPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preReceiveLocHasPatternActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_preReceiveLocHasPatternActionPerformed

private void postReceiveLocHasPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postReceiveLocHasPatternActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_postReceiveLocHasPatternActionPerformed

private void receiveFromTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_receiveFromTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_receiveFromTextFocusGained

private void receiveFromTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_receiveFromTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_receiveFromTextFocusLost

private void receiveFromHasRegexCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_receiveFromHasRegexCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_receiveFromHasRegexCheckFocusGained

private void receiveFromHasRegexCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_receiveFromHasRegexCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_receiveFromHasRegexCheckFocusLost

private void preReceiveCmdComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveCmdComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preReceiveCmdComboFocusGained

private void preReceiveCmdComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveCmdComboFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preReceiveCmdComboFocusLost

private void preReceiveLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveLocTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preReceiveLocTextFocusGained

private void preReceiveLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preReceiveLocTextFocusLost

private void preReceiveLocHasPatternFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveLocHasPatternFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_preReceiveLocHasPatternFocusGained

private void preReceiveLocHasPatternFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preReceiveLocHasPatternFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_preReceiveLocHasPatternFocusLost

private void postReceiveCmdComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveCmdComboFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postReceiveCmdComboFocusGained

private void postReceiveCmdComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveCmdComboFocusLost
}//GEN-LAST:event_postReceiveCmdComboFocusLost

private void postReceiveLocTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveLocTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postReceiveLocTextFocusGained

private void postReceiveLocTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveLocTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_postReceiveLocTextFocusLost

private void postReceiveLocHasPatternFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveLocHasPatternFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_postReceiveLocHasPatternFocusGained

private void postReceiveLocHasPatternFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_postReceiveLocHasPatternFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_postReceiveLocHasPatternFocusLost

private void pollIntervalTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollIntervalTextFocusGained

private void pollIntervalTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_pollIntervalTextFocusLost

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
        if (receiveFromText.getText() == null || receiveFromText.getText().trim().length() == 0) {
            error = Utilities.setError(error, "TransferConfiguration.MISSING_RECEIVEFROM");
        } else {
            String op = "NONE";

            if (preReceiveCmdCombo.getSelectedItem() != null) {
                op = preReceiveCmdCombo.getSelectedItem().toString();
            }

            if (!op.equals("NONE") && (preReceiveLocText.getText() == null || preReceiveLocText.getText().trim().length() == 0)) {
                // copy and rename need location
                error = Utilities.setError(error, "TransferConfiguration.MISSING_PRE_OP_LOC", new Object[]{op});
            } else {
                op = "NONE";
                if (postReceiveCmdCombo.getSelectedItem() != null) {
                    op = postReceiveCmdCombo.getSelectedItem().toString();
                }
                if (op.equals("RENAME") && (postReceiveLocText.getText() == null || postReceiveLocText.getText().trim().length() == 0)) {
                    error = Utilities.setError(error, "TransferConfiguration.MISSING_POST_OP_LOC", new Object[]{op});
                } else if (pollIntervalText.getText() != null && pollIntervalText.getText().trim().length() > 0 && Utilities.getInteger(pollIntervalText.getText().trim()) < 0) {
                    if ( !mSolicit ) {
                        error = Utilities.setError(error, "TransferConfiguration.INVALID_POLL_INTERVAL", new Object[]{pollIntervalText.getText().trim()});
                    }
                }
            }
        }

        // if self is ok, check the sub-panel
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

            Port port = (Port) ftpAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = getOperationName();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();

            // only 1
            //
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    // poll message is inside <input> if is for request
                    // poll message is inside <output> if is for response
                    FTPTransfer pollTransfer = null;
                    List<FTPTransfer> pollFTPTransfers = null;
                    List<FTPMessage> pollFTPMessages = null;
                    if (bRequest && !mSolicit) {
                        BindingInput bi = bop.getBindingInput();
                        if (bi != null) {
                            pollFTPTransfers =
                                    bi.getExtensibilityElements(FTPTransfer.class);
                            if (pollFTPTransfers.size() > 0) {
                                pollTransfer = pollFTPTransfers.get(0);
                            } else {
                                // no ftp:transfer, then it might be ftp:message
                                pollFTPMessages = bi.getExtensibilityElements(FTPMessage.class);
                                if (pollFTPMessages != null && pollFTPMessages.size() > 0) {
                                    for (int i = 0; i < pollFTPMessages.size(); i++) {
                                        if (pollFTPMessages.get(i) != null) {
                                            bi.removeExtensibilityElement(pollFTPMessages.get(i));
                                        }
                                    }
                                }
                                bi.addExtensibilityElement((pollTransfer = (FTPTransfer) Utilities.createFTPTransfer(mWsdlComponent)));
                            }
                        }
                    } else {
                        BindingOutput bo = bop.getBindingOutput();
                        if (bo != null) {
                            pollFTPTransfers =
                                    bo.getExtensibilityElements(FTPTransfer.class);
                            if (pollFTPTransfers.size() > 0) {
                                pollTransfer = pollFTPTransfers.get(0);
                            } else {
                                // no ftp:transfer, then it might be ftp:message
                                pollFTPMessages = bo.getExtensibilityElements(FTPMessage.class);
                                if (pollFTPMessages != null && pollFTPMessages.size() > 0) {
                                    for (int i = 0; i < pollFTPMessages.size(); i++) {
                                        if (pollFTPMessages.get(i) != null) {
                                            bo.removeExtensibilityElement(pollFTPMessages.get(i));
                                        }
                                    }
                                }
                                bo.addExtensibilityElement((pollTransfer = (FTPTransfer) Utilities.createFTPTransfer(mWsdlComponent)));
                            }
                        }
                        // if solicit - need to remove ftp:message or ftp:transfer children from <input>
                        // do not strip ext elem when bRequest is false and mSolicit is true
                        if ( mSolicit ) {
                            BindingInput bi = bop.getBindingInput();
                            if ( bi != null ) {
                                List<FTPMessage> inputMsgBindingElems = bi.getExtensibilityElements(FTPMessage.class);
                                if ( inputMsgBindingElems != null ) {
                                    for ( int i = 0; i < inputMsgBindingElems.size(); i++ ) {
                                        bi.removeExtensibilityElement(inputMsgBindingElems.get(i));
                                    }
                                }
                                List<FTPTransfer> inputTransferBindingElems = bi.getExtensibilityElements(FTPTransfer.class);
                                if ( inputTransferBindingElems != null ) {
                                    for ( int i = 0; i < inputTransferBindingElems.size(); i++ ) {
                                        bi.removeExtensibilityElement(inputTransferBindingElems.get(i));
                                    }
                                }
                            }
                        }
                    }
                    commitPollMessage(binding, operationName, pollTransfer);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } //finally {
//            if (wsdlModel.isIntransaction()) {
//               wsdlModel.endTransaction(); 
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
        } //finally {
//            if (wsdlModel.isIntransaction()) {
//               wsdlModel.endTransaction(); 
//            }                        
//            return true;
//        } 

        return true;
    }

    private void commitPollMessage(Binding binding, String opName,
            FTPTransfer pollTransfer) {
        pollTransfer.setReceiveFrom(receiveFromText.getText().trim());
        pollTransfer.setReceiveFromHasPatterns(receiveFromHasRegexCheck.isSelected());
        pollTransfer.setPreReceiveCommand(preReceiveCmdCombo.getSelectedItem() != null ? preReceiveCmdCombo.getSelectedItem().toString() : "NONE");
        pollTransfer.setPreReceiveLocation(preReceiveLocText.getText().trim());
        pollTransfer.setPreReceiveLocationHasPatterns(preReceiveLocHasPattern.isSelected());
        pollTransfer.setPostReceiveCommand(postReceiveCmdCombo.getSelectedItem() != null ? postReceiveCmdCombo.getSelectedItem().toString() : "NONE");
        pollTransfer.setPostReceiveLocation(postReceiveLocText.getText().trim());
        pollTransfer.setPostReceiveLocationHasPatterns(postReceiveLocHasPattern.isSelected());

        Boolean b = null;
        
        if (mWzdProps != null) {
            b = (Boolean) mWzdProps.get(FTPConstants.WSDL_PROP_REQRESPCORRELATE);
        }

        // from prev panel
        pollTransfer.setMessageCorrelateEnabled(mSolicit ? false : (b != null ? b.booleanValue() : true));
        
        if ( bOneWay )
            pollTransfer.setMessageCorrelateEnabled(false);

        pollTransfer.setUse(FTPConstants.LITERAL);
        if (mMessageTypePanel.getInputUseType() != null) {
            if (mMessageTypePanel.getInputUseType().equals(FTPConstants.ENCODED)) {        
                pollTransfer.setEncodingStyle(mMessageTypePanel.getEncodingStyle());
                pollTransfer.setUse(mMessageTypePanel.getInputUseType());
            } else if ((pollTransfer.getUse() != null) &&
                        (mMessageTypePanel.getInputUseType().equals(FTPConstants.LITERAL))) {
                    pollTransfer.setEncodingStyle(null);                   
            }
        } else {
            pollTransfer.setAttribute(FTPTransfer.FTP_ENCODINGSTYLE_PROPERTY, null);
        }

        if ( mMessageTypePanel.getMessageType() == FTPConstants.BINARY_MESSAGE_TYPE )
            pollTransfer.setFileType(FTPConstants.BINARY);
        else {
            pollTransfer.setFileType(FTPConstants.TEXT);
            String enc = mMessageTypePanel.getMessageCharEncoding();
            if ( enc != null && enc.trim().length() > 0 ) {
                pollTransfer.setCharacterEncoding(enc.trim());
            }
        }

        if ( mMessageTypePanel.getForwardAsAttachment() )
            pollTransfer.setForwardAsAttachment(true);

        // set the type of the message part if it is undefined based
        // on the MessageType option from user
        commitMessageType(binding, opName, pollTransfer);

        if ( !mSolicit ) {
            if (pollIntervalText.getText() != null && pollIntervalText.getText().trim().length() > 0) {
                pollTransfer.setPollInterval(pollIntervalText.getText().trim());
            } else {
                pollTransfer.setPollInterval("5000");
            }
        }
        validateMe(true);
    }

    private boolean commitPort(Port port) {
        Collection<FTPAddress> address = port.getExtensibilityElements(FTPAddress.class);
        if (address != null && address.size() > 0) {
            FTPAddress ftpAddress = address.iterator().next();
            return commitAddress(ftpAddress);
        }
        return false;
    }

    private boolean commitMessage(FTPTransfer FTPTransfer) {
        Object parentObj = FTPTransfer.getParent();
        FTPBinding ftpBinding = null;
        BindingOperation parentOp = null;
        if (parentObj instanceof BindingInput) {
            parentOp =
                    (BindingOperation) ((BindingInput) parentObj).getParent();
        } else if (parentObj instanceof BindingOutput) {
            parentOp =
                    (BindingOperation) ((BindingOutput) parentObj).getParent();
        }
        if (parentObj != null) {
            Binding parentBinding = (Binding) parentOp.getParent();
            Collection<FTPBinding> bindings =
                    parentBinding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    private boolean commitOperation(FTPOperation ftpOperation) {
        Object obj = ftpOperation.getParent();
        if (obj instanceof BindingOperation) {
            Binding parentBinding =
                    (Binding) ((BindingOperation) obj).getParent();
            Collection<FTPBinding> bindings =
                    parentBinding.getExtensibilityElements(FTPBinding.class);
            if (!bindings.isEmpty()) {
                return commitBinding(bindings.iterator().next());
            }
        }
        return false;
    }

    private void commitMessageType(Binding binding, String opName,
            FTPTransfer pollTransfer) {
        String partName = getInputPart();
        if (partName != null) {
            Collection parts = null;
            parts = (bRequest && !mSolicit) ? Utilities.getInputParts(binding, opName) : Utilities.getOutputParts(binding, opName);
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
            pollTransfer.setPart(partName);
        } else {
            // need to create a message with part
            Message newMessage = Utilities.createMessage(mWsdlComponent.getModel(), "PollTransfer");
            if (newMessage != null) {
                Part newPart = Utilities.createPart(mWsdlComponent.getModel(),
                        newMessage, "part1");
                if (newPart != null) {
                    Utilities.setPartType(newPart, getMessageType(),
                            getSelectedPartType(), getSelectedElementType());

                    if (Utilities.getMessagePart(newPart.getName(), newMessage) == null) {
                        newMessage.addPart(newPart);
                    }
                    pollTransfer.setPart(newPart.getName());
                }
            }
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
    String getInputPart() {
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
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel inboundTransferingConfigPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel pollIntervalImgLab;
    private javax.swing.JLabel pollIntervalLab;
    private javax.swing.JTextField pollIntervalText;
    private javax.swing.JPanel pollReqOrRespConfigPanel;
    private javax.swing.JComboBox postReceiveCmdCombo;
    private javax.swing.JLabel postReceiveCmdLab;
    private javax.swing.JCheckBox postReceiveLocHasPattern;
    private javax.swing.JLabel postReceiveLocLab;
    private javax.swing.JTextField postReceiveLocText;
    private javax.swing.JLabel postRecvCmdImgLab;
    private javax.swing.JLabel postRecvHasPattImgLab;
    private javax.swing.JLabel postRecvLocImgLab;
    private javax.swing.JComboBox preReceiveCmdCombo;
    private javax.swing.JLabel preReceiveCmdLab;
    private javax.swing.JCheckBox preReceiveLocHasPattern;
    private javax.swing.JLabel preReceiveLocLab;
    private javax.swing.JTextField preReceiveLocText;
    private javax.swing.JLabel preRecvCmdImgLab;
    private javax.swing.JLabel preRecvHasPattImgLab;
    private javax.swing.JLabel preRecvLocImgLab;
    private javax.swing.JCheckBox receiveFromHasRegexCheck;
    private javax.swing.JLabel receiveFromLab;
    private javax.swing.JTextField receiveFromText;
    private javax.swing.JLabel recvFromImgLab;
    private javax.swing.JLabel recvHasRegexImgLab;
    private javax.swing.JLabel requestLab;
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
