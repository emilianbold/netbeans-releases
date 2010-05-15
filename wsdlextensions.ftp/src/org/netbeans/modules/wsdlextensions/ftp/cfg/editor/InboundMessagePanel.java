/*
 * InboundMessagePanel.java
 *
 * Created on September 19, 2008, 7:25 PM
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
public class InboundMessagePanel extends javax.swing.JPanel implements AncestorListener, PropertyAccessible, PropertyChangeSupport, BindingConfigurationDelegate {

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
    private static final Logger mLogger = Logger.getLogger(InboundMessagePanel.class.getName());
    private DescriptionPanel descPanel = null;
    private boolean bRequest;
    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
    private Part mPart = null;
    private boolean bRequestResponseCorrelate;
    /**
     * invisible components used to keep status:
     */
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JComboBox partComboBox;
    private Map<String, Object> mWzdProps;
    private PropertyChangeSupport mProxy;
    private boolean mSolicit;
    private boolean mOneWay;

    /** Creates new form InboundMessagePanel */
    public InboundMessagePanel(QName qName, WSDLComponent component, boolean oneway, boolean b, boolean solicit) {
        this(qName, component, oneway, b, solicit, null);
    }

    public InboundMessagePanel(QName qName, WSDLComponent component, boolean oneway, boolean b, boolean solicit, PropertyChangeSupport proxy) {
        mOneWay = oneway;
        bRequest = b;
        mProxy = proxy;
        mSolicit = solicit;
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
        return NbBundle.getMessage(InboundMessagePanel.class,
                mSolicit ? "InboundMessagePanel.GetMessage.StepLabel" : (bRequest ? "InboundMessagePanel.Request.StepLabel" : "InboundMessagePanel.Response.StepLabel"));
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

    public void setOperationName(String opName) {
        if (opName != null) {
            operationNameComboBox.setSelectedItem(opName);
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
        inboundMessagingConfigPanel.add(mMessageTypePanel, gridBagConstraints);

        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.0;
        inboundMessagingConfigPanel.add(tmpPanel, gridBagConstraints);
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
        if (mSolicit) {
            pollIntervalImgLab.setVisible(false);
            pollIntervalImgLab.setEnabled(false);
            pollIntervalLab.setVisible(false);
            pollIntervalLab.setEnabled(false);
            pollIntervalText.setVisible(false);
            pollIntervalText.setEnabled(false);
        }

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
                    FTPMessage pollMessage = getPollFTPMessage(binding,
                            bop.getName());
                    updatePollMessageView(binding, pollMessage);
                }
            }

        }
    }

    private FTPMessage getPollFTPMessage(Binding binding,
            String selectedOperation) {
        FTPMessage pollFTPMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    List<FTPMessage> pollFTPMessages = null;

                    if (bRequest && !mSolicit) {
                        pollFTPMessages = bop.getBindingInput().getExtensibilityElements(FTPMessage.class);
                    } else {
                        pollFTPMessages = bop.getBindingOutput().getExtensibilityElements(FTPMessage.class);
                    }

                    if (pollFTPMessages.size() > 0) {
                        pollFTPMessage = pollFTPMessages.get(0);
                        break;
                    }
                }
            }
        }
        return pollFTPMessage;
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
        messageNamePrefixText.getDocument().removeDocumentListener(mDocumentListener);
        messageNameText.getDocument().removeDocumentListener(mDocumentListener);
        messageRepoText.getDocument().removeDocumentListener(mDocumentListener);
        pollIntervalText.getDocument().removeDocumentListener(mDocumentListener);

        messageNamePrefixText.removeActionListener(mActionListener);
        messageNameText.removeActionListener(mActionListener);
        messageRepoText.removeActionListener(mActionListener);
        pollIntervalText.removeActionListener(mActionListener);

        messageNamePrefixText.setText("");
        messageNameText.setText("");
        messageRepoText.setText("");

        if (!mSolicit) {
            pollIntervalText.setText("5000");
        }
        
        archiveCheck.setSelected(true);
        stageCheck.setSelected(true);

        servicePortComboBox.removeItemListener(mItemListener);
        bindingNameComboBox.removeItemListener(mItemListener);
        portTypeComboBox.removeItemListener(mItemListener);
        operationNameComboBox.removeItemListener(mItemListener);

        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
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
        messageNamePrefixText.addActionListener(mActionListener);
        messageNameText.addActionListener(mActionListener);
        messageRepoText.addActionListener(mActionListener);
        pollIntervalText.addActionListener(mActionListener);

        servicePortComboBox.addItemListener(mItemListener);
        bindingNameComboBox.addItemListener(mItemListener);
        portTypeComboBox.addItemListener(mItemListener);
        operationNameComboBox.addItemListener(mItemListener);

        messageNamePrefixText.getDocument().addDocumentListener(mDocumentListener);
        messageNameText.getDocument().addDocumentListener(mDocumentListener);
        messageRepoText.getDocument().addDocumentListener(mDocumentListener);
        pollIntervalText.getDocument().addDocumentListener(mDocumentListener);
    }

    private void updatePollMessageView(Binding binding, FTPMessage pollMessage) {
        messageNamePrefixText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageNamePrefixIB"));
        messageNameText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageName"));
        messageRepoText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageRepository"));
        if (!mSolicit) {
            pollIntervalText.setToolTipText(mBundle.getString("DESC_Attribute_Message_pollIntervalMillis"));
        }

        archiveCheck.setToolTipText(mBundle.getString("DESC_Attribute_Message_archive"));
        stageCheck.setToolTipText(mBundle.getString("DESC_Attribute_Message_stage"));
        
        if (pollMessage != null) {
            messageNamePrefixText.setText(pollMessage.getMessageNamePrefixIB());
            messageNameText.setText(pollMessage.getMessageName());
            messageRepoText.setText(pollMessage.getMessageRepository());
            bRequestResponseCorrelate = pollMessage.getMessageCorrelateEnabled();

            if (!mSolicit) {
                pollIntervalText.setText(pollMessage.getPollInterval());
            }

            archiveCheck.setSelected(pollMessage.getArchiveEnabled());
            stageCheck.setSelected(pollMessage.getStagingEnabled());
            
            Object obj = pollMessage.getParent();
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
            String part = pollMessage.getPart();
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
                NamedComponentReference<Message> messagePoll = null;
                if (bRequest && !mSolicit) {
                    messagePoll = op.getInput().getMessage();
                } else {
                    messagePoll = op.getOutput().getMessage();
                }
                if (partComboBox.getSelectedItem() != null) {
                    if (part != null) {
                        mPart = Utilities.getMessagePart(part, messagePoll.get());
                    }
                }
            }

            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    pollMessage, mProject,
                    operationNameComboBox.getSelectedItem() != null ? operationNameComboBox.getSelectedItem().toString() : "");

        } else {
            // null out view
            messageNamePrefixText.setText("");
            messageNameText.setText("");
            messageRepoText.setText("");

            if (!mSolicit) {
                pollIntervalText.setText("");
            }
            archiveCheck.setSelected(true);
            stageCheck.setSelected(true);
            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    pollMessage, null, null);
        }
        // lastly, populate the encoding info if message type is text
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

    private void updateView(Map<String, Object> mWzdProps) {
        String repo = (String) mWzdProps.get(FTPConstants.WSDL_PROP_MSGREPO);
        if (repo != null && repo.trim().length() > 0) {
            if (messageRepoText.getText() == null || messageRepoText.getText().trim().length() == 0) {
                // over write only if there is no value typed in at this panel
                messageRepoText.setText(repo.trim());
            }
        }
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
    }

    private void handleActionPerformed(ActionEvent evt) {
    }

    private void updateDescriptionArea(FocusEvent evt) {
        descPanel.setText("");
        String[] desc = null;

        if (evt.getSource() == messageNamePrefixText) {
            desc = new String[]{"Message Name Prefix\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageNamePrefixIB")
                    };
        } else if (evt.getSource() == messageNameText) {
            desc = new String[]{"Message Name\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageName")
                    };
        } else if (evt.getSource() == messageRepoText) {
            desc = new String[]{"Messaging Repository\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageRepository")
                    };
        } else if (evt.getSource() == pollIntervalText) {
            desc = new String[]{"Poll Interval in milli-seconds\n\n",
                        mBundle.getString("DESC_Attribute_Message_pollIntervalMillis")
                    };
        } else if (evt.getSource() == archiveCheck) {
            desc = new String[]{"Enable Archive Polled Message\n\n",
                        mBundle.getString("DESC_Attribute_Message_archive")
                    };
        } else if (evt.getSource() == stageCheck) {
            desc = new String[]{"Enable Stage Polled Message\n\n",
                        mBundle.getString("DESC_Attribute_Message_stage")
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
        inboundMessagingConfigPanel = new javax.swing.JPanel();
        pollReqOrRespConfigPanel = new javax.swing.JPanel();
        requestLab = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        messageRepoLab = new javax.swing.JLabel();
        messageRepoText = new javax.swing.JTextField();
        messageNameLab = new javax.swing.JLabel();
        messageNameText = new javax.swing.JTextField();
        messagePreifxLab = new javax.swing.JLabel();
        messageNamePrefixText = new javax.swing.JTextField();
        pollIntervalLab = new javax.swing.JLabel();
        pollIntervalText = new javax.swing.JTextField();
        archiveCheck = new javax.swing.JCheckBox();
        msgRepoImgLab = new javax.swing.JLabel();
        msgNameImgLab = new javax.swing.JLabel();
        msgPrefixImgLab = new javax.swing.JLabel();
        pollIntervalImgLab = new javax.swing.JLabel();
        archivePolledImgLab = new javax.swing.JLabel();
        stageCheck = new javax.swing.JCheckBox();
        stagePolledImgLab = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(612, 620));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(612, 620));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(612, 620));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setPreferredSize(new java.awt.Dimension(612, 620));

        inboundMessagingConfigPanel.setMinimumSize(new java.awt.Dimension(610, 560));
        inboundMessagingConfigPanel.setName("inboundMessagingConfigPanel"); // NOI18N
        inboundMessagingConfigPanel.setPreferredSize(new java.awt.Dimension(610, 560));
        inboundMessagingConfigPanel.setLayout(new java.awt.GridBagLayout());

        pollReqOrRespConfigPanel.setMinimumSize(new java.awt.Dimension(610, 300));
        pollReqOrRespConfigPanel.setName("pollReqOrRespConfigPanel"); // NOI18N
        pollReqOrRespConfigPanel.setPreferredSize(new java.awt.Dimension(610, 300));
        pollReqOrRespConfigPanel.setLayout(new java.awt.GridBagLayout());

        requestLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(requestLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.requestLab.text_1")); // NOI18N
        requestLab.setName("requestLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(requestLab, gridBagConstraints);

        jSeparator1.setMinimumSize(new java.awt.Dimension(0, 1));
        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 150, 0, 10);
        pollReqOrRespConfigPanel.add(jSeparator1, gridBagConstraints);

        messageRepoLab.setLabelFor(messageRepoText);
        org.openide.awt.Mnemonics.setLocalizedText(messageRepoLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageRepoLab.text_1")); // NOI18N
        messageRepoLab.setName("messageRepoLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(messageRepoLab, gridBagConstraints);
        messageRepoLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageRepoLab.AccessibleContext.accessibleDescription")); // NOI18N

        messageRepoText.setName("messageRepoText"); // NOI18N
        messageRepoText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageRepoTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageRepoTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(messageRepoText, gridBagConstraints);
        messageRepoText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageRepoText.AccessibleContext.accessibleName")); // NOI18N
        messageRepoText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageRepoText.AccessibleContext.accessibleDescription")); // NOI18N

        messageNameLab.setLabelFor(messageNameText);
        org.openide.awt.Mnemonics.setLocalizedText(messageNameLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNameLab.text_1")); // NOI18N
        messageNameLab.setName("messageNameLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(messageNameLab, gridBagConstraints);
        messageNameLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNameLab.AccessibleContext.accessibleDescription")); // NOI18N

        messageNameText.setName("messageNameText"); // NOI18N
        messageNameText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageNameTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageNameTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(messageNameText, gridBagConstraints);
        messageNameText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNameText.AccessibleContext.accessibleName")); // NOI18N
        messageNameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNameText.AccessibleContext.accessibleDescription")); // NOI18N

        messagePreifxLab.setLabelFor(messageNamePrefixText);
        org.openide.awt.Mnemonics.setLocalizedText(messagePreifxLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messagePreifxLab.text_1")); // NOI18N
        messagePreifxLab.setName("messagePreifxLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(messagePreifxLab, gridBagConstraints);
        messagePreifxLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messagePreifxLab.AccessibleContext.accessibleDescription")); // NOI18N

        messageNamePrefixText.setName("messageNamePrefixText"); // NOI18N
        messageNamePrefixText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                messageNamePrefixTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                messageNamePrefixTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(messageNamePrefixText, gridBagConstraints);
        messageNamePrefixText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNamePrefixText.AccessibleContext.accessibleName")); // NOI18N
        messageNamePrefixText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.messageNamePrefixText.AccessibleContext.accessibleDescription")); // NOI18N

        pollIntervalLab.setLabelFor(pollIntervalText);
        org.openide.awt.Mnemonics.setLocalizedText(pollIntervalLab, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalLab.text_1")); // NOI18N
        pollIntervalLab.setName("pollIntervalLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(pollIntervalLab, gridBagConstraints);
        pollIntervalLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalLab.AccessibleContext.accessibleDescription")); // NOI18N

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
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(pollIntervalText, gridBagConstraints);
        pollIntervalText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalText.AccessibleContext.accessibleName")); // NOI18N
        pollIntervalText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(archiveCheck, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archiveCheck.text")); // NOI18N
        archiveCheck.setName("archiveCheck"); // NOI18N
        archiveCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                archiveCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                archiveCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(archiveCheck, gridBagConstraints);
        archiveCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archiveCheck.AccessibleContext.accessibleDescription")); // NOI18N

        msgRepoImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgRepoImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgRepoImgLab.text")); // NOI18N
        msgRepoImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgRepoImgLab.toolTipText")); // NOI18N
        msgRepoImgLab.setName("msgRepoImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(msgRepoImgLab, gridBagConstraints);

        msgNameImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgNameImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgNameImgLab.text")); // NOI18N
        msgNameImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgNameImgLab.toolTipText")); // NOI18N
        msgNameImgLab.setName("msgNameImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(msgNameImgLab, gridBagConstraints);

        msgPrefixImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgPrefixImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgPrefixImgLab.text")); // NOI18N
        msgPrefixImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.msgPrefixImgLab.toolTipText")); // NOI18N
        msgPrefixImgLab.setName("msgPrefixImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(msgPrefixImgLab, gridBagConstraints);

        pollIntervalImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        pollIntervalImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalImgLab.text")); // NOI18N
        pollIntervalImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollIntervalImgLab.toolTipText")); // NOI18N
        pollIntervalImgLab.setName("pollIntervalImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        pollReqOrRespConfigPanel.add(pollIntervalImgLab, gridBagConstraints);

        archivePolledImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        archivePolledImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archivePolledImgLab.text")); // NOI18N
        archivePolledImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.archivePolledImgLab.toolTipText")); // NOI18N
        archivePolledImgLab.setName("archivePolledImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(archivePolledImgLab, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(stageCheck, org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.stageCheck.text")); // NOI18N
        stageCheck.setActionCommand(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.stageCheck.actionCommand")); // NOI18N
        stageCheck.setName("stageCheck"); // NOI18N
        stageCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stageCheckActionPerformed(evt);
            }
        });
        stageCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                stageCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                stageCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        pollReqOrRespConfigPanel.add(stageCheck, gridBagConstraints);
        stageCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.stageCheck.AccessibleContext.accessibleDescription")); // NOI18N

        stagePolledImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        stagePolledImgLab.setText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.stagePolledImgLab.text")); // NOI18N
        stagePolledImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.stagePolledImgLab.toolTipText")); // NOI18N
        stagePolledImgLab.setName("stagePolledImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        pollReqOrRespConfigPanel.add(stagePolledImgLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        inboundMessagingConfigPanel.add(pollReqOrRespConfigPanel, gridBagConstraints);
        pollReqOrRespConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollReqOrRespConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        pollReqOrRespConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.pollReqOrRespConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(inboundMessagingConfigPanel);
        inboundMessagingConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inboundMessagingConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        inboundMessagingConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.inboundMessagingConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descriptionPanel.setMinimumSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);
        descriptionPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.descriptionPanel.AccessibleContext.accessibleName")); // NOI18N
        descriptionPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundMessagePanel.class, "InboundMessagePanel.descriptionPanel.AccessibleContext.accessibleDescription")); // NOI18N

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void messageRepoTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageRepoTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageRepoTextFocusGained

private void messageRepoTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageRepoTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageRepoTextFocusLost

private void messageNameTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageNameTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageNameTextFocusGained

private void messageNameTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageNameTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageNameTextFocusLost

private void messageNamePrefixTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageNamePrefixTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_messageNamePrefixTextFocusGained

private void messageNamePrefixTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_messageNamePrefixTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_messageNamePrefixTextFocusLost

private void pollIntervalTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_pollIntervalTextFocusGained

private void pollIntervalTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pollIntervalTextFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_pollIntervalTextFocusLost

private void archiveCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_archiveCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_archiveCheckFocusGained

private void archiveCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_archiveCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_archiveCheckFocusLost

private void stageCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stageCheckActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_stageCheckActionPerformed

private void stageCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stageCheckFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_stageCheckFocusGained

private void stageCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stageCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_stageCheckFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox archiveCheck;
    private javax.swing.JLabel archivePolledImgLab;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel inboundMessagingConfigPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel messageNameLab;
    private javax.swing.JTextField messageNamePrefixText;
    private javax.swing.JTextField messageNameText;
    private javax.swing.JLabel messagePreifxLab;
    private javax.swing.JLabel messageRepoLab;
    private javax.swing.JTextField messageRepoText;
    private javax.swing.JLabel msgNameImgLab;
    private javax.swing.JLabel msgPrefixImgLab;
    private javax.swing.JLabel msgRepoImgLab;
    private javax.swing.JLabel pollIntervalImgLab;
    private javax.swing.JLabel pollIntervalLab;
    private javax.swing.JTextField pollIntervalText;
    private javax.swing.JPanel pollReqOrRespConfigPanel;
    private javax.swing.JLabel requestLab;
    private javax.swing.JCheckBox stageCheck;
    private javax.swing.JLabel stagePolledImgLab;
    // End of variables declaration//GEN-END:variables
    
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
        } else if (mWsdlComponent instanceof FTPMessage) {
            result = commitMessage((FTPMessage) mWsdlComponent);
        } else if (mWsdlComponent instanceof FTPTransfer) {
            result = commitTransfer((FTPTransfer) mWsdlComponent);
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

        // repo required
        // poll interval must be valid number
        // prefix can be blank
        // msg name can be blank
        if (messageRepoText.getText() == null || messageRepoText.getText().trim().length() == 0) {
            error = Utilities.setError(error, "MessageConfiguration.MISSING_MSG_REPO");
        } else if (pollIntervalText.getText() != null && pollIntervalText.getText().trim().length() > 0 && Utilities.getInteger(pollIntervalText.getText().trim()) < 0) {
            if (!mSolicit) {
                error = Utilities.setError(error, "MessageConfiguration.INVALID_POLL_INTERVAL", new Object[]{pollIntervalText.getText()});
            }
        }

        // if self is ok, check sub-panel
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
                    FTPMessage pollFTPMessage = null;
                    List<FTPMessage> pollFTPMessages = null;
                    List<FTPTransfer> pollFTPTransfers = null;
                    if (bRequest && !mSolicit) {
                        BindingInput bi = bop.getBindingInput();
                        if (bi != null) {
                            pollFTPMessages =
                                    bi.getExtensibilityElements(FTPMessage.class);
                            if (pollFTPMessages.size() > 0) {
                                pollFTPMessage = pollFTPMessages.get(0);
                            } else {
                                // create ftp:message and populated and commit
                                // a work around when the framework does not
                                // provide a initial ftp:message here
                                // we need to remove the ftp:transfer
                                // and plant our ftp:message
                                pollFTPTransfers =
                                        bi.getExtensibilityElements(FTPTransfer.class);
                                if (pollFTPTransfers != null && pollFTPTransfers.size() > 0) {
                                    // wrong binding with binding scheme ftp:transfer
                                    for (int i = 0; i < pollFTPTransfers.size(); i++) {
                                        if (pollFTPTransfers.get(i) != null) {
                                            bi.removeExtensibilityElement(pollFTPTransfers.get(i));
                                        }
                                    }
                                }
                                bi.addExtensibilityElement((pollFTPMessage = (FTPMessage) Utilities.createFTPMessage(mWsdlComponent)));
                            }
                        }
                    } else {
                        BindingOutput bo = bop.getBindingOutput();
                        if (bo != null) {
                            pollFTPMessages = bo.getExtensibilityElements(FTPMessage.class);
                            if (pollFTPMessages.size() > 0) {
                                pollFTPMessage = pollFTPMessages.get(0);
                            } else {
                                // create ftp:message and populated and commit
                                // a work around when the framework does not
                                // provide a initial ftp:message here
                                // we need to remove the ftp:transfer
                                // and plant our ftp:message
                                pollFTPTransfers =
                                        bo.getExtensibilityElements(FTPTransfer.class);
                                if (pollFTPTransfers != null && pollFTPTransfers.size() > 0) {
                                    // wrong binding with binding scheme ftp:transfer
                                    for (int i = 0; i < pollFTPTransfers.size(); i++) {
                                        if (pollFTPTransfers.get(i) != null) {
                                            bo.removeExtensibilityElement(pollFTPTransfers.get(i));
                                        }
                                    }
                                }
                                bo.addExtensibilityElement((pollFTPMessage = (FTPMessage) Utilities.createFTPMessage(mWsdlComponent)));
                            }
                        }
                        // if solicit - need to remove ftp:message or ftp:transfer children from <input>
                        // do not strip ext elem when bRequest is false and mSolicit is true
                        if (mSolicit) {
                            BindingInput bi = bop.getBindingInput();
                            if (bi != null) {
                                List<FTPMessage> inputMsgBindingElems = bi.getExtensibilityElements(FTPMessage.class);
                                if (inputMsgBindingElems != null) {
                                    for (int i = 0; i < inputMsgBindingElems.size(); i++) {
                                        bi.removeExtensibilityElement(inputMsgBindingElems.get(i));
                                    }
                                }
                                List<FTPTransfer> inputTransferBindingElems = bi.getExtensibilityElements(FTPTransfer.class);
                                if (inputTransferBindingElems != null) {
                                    for (int i = 0; i < inputTransferBindingElems.size(); i++) {
                                        bi.removeExtensibilityElement(inputTransferBindingElems.get(i));
                                    }
                                }
                            }
                        }
                    }
                    if (binding != null && operationName != null && pollFTPMessage != null) {
                        commitPollMessage(binding, operationName,
                                pollFTPMessage);
                    } else {
                        // should report error
                    }
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
            FTPMessage pollFTPMessage) {
        pollFTPMessage.setMessageName(messageNameText.getText());
        pollFTPMessage.setMessageNamePrefixIB(messageNamePrefixText.getText());
        pollFTPMessage.setMessageRepository(messageRepoText.getText());

        pollFTPMessage.setArchiveEnabled(archiveCheck.isSelected());
        pollFTPMessage.setStagingEnabled(stageCheck.isSelected());
        
        String s = null;
        if (mWzdProps != null) {
            s = (String) mWzdProps.get(FTPConstants.WSDL_PROP_MSGREPO);
        }

        // if user set a repo at previous panel, see which one win
        if (s != null && s.trim().length() > 0) {
            if (messageRepoText.getText() == null || messageRepoText.getText().trim().length() == 0) {
                messageRepoText.setText(s.trim());
            }
        }

        Boolean b = null;
        if (mWzdProps != null) {
            b = (Boolean) mWzdProps.get(FTPConstants.WSDL_PROP_REQRESPCORRELATE);
        }

        // from prev panel
        pollFTPMessage.setMessageCorrelateEnabled(mOneWay ? false : (b != null ? b.booleanValue() : true));

        pollFTPMessage.setUse(FTPConstants.LITERAL);
        if (mMessageTypePanel.getInputUseType() != null) {
            if (mMessageTypePanel.getInputUseType().equals(FTPConstants.ENCODED)) {        
                pollFTPMessage.setEncodingStyle(mMessageTypePanel.getEncodingStyle());
                pollFTPMessage.setUse(mMessageTypePanel.getInputUseType());
            } else if ((pollFTPMessage.getUse() != null) &&
                        (mMessageTypePanel.getInputUseType().equals(FTPConstants.LITERAL))) {
                    pollFTPMessage.setEncodingStyle(null);                   
            }
        } else {
            pollFTPMessage.setAttribute(FTPMessage.FTP_ENCODINGSTYLE_PROPERTY, null);
        }
        
        if ( mMessageTypePanel.getMessageType() == FTPConstants.BINARY_MESSAGE_TYPE )
            pollFTPMessage.setFileType(FTPConstants.BINARY);
        else {
            pollFTPMessage.setFileType(FTPConstants.TEXT);
            String enc = mMessageTypePanel.getMessageCharEncoding();
            if ( enc != null && enc.trim().length() > 0 ) {
                pollFTPMessage.setCharacterEncoding(enc.trim());
            }
        }
        
        if ( mMessageTypePanel.getForwardAsAttachment() )
            pollFTPMessage.setForwardAsAttachment(true);
        
        // set the type of the message part if it is undefined based
        // on the MessageType option from user
        commitMessageType(binding, opName, pollFTPMessage);

        if (!mSolicit) {
            if (pollIntervalText.getText() != null && pollIntervalText.getText().trim().length() > 0) {
                pollFTPMessage.setPollInterval(pollIntervalText.getText().trim());
            } else {
                pollFTPMessage.setPollInterval("5000");
            }
        }
    }

    private boolean commitPort(Port port) {
        Collection<FTPAddress> address = port.getExtensibilityElements(FTPAddress.class);
        if (address != null && address.size() > 0) {
            FTPAddress ftpAddress = address.iterator().next();
            return commitAddress(ftpAddress);
        }
        return false;
    }

    private boolean commitMessage(FTPMessage ftpMessage) {
        Object parentObj = ftpMessage.getParent();
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

    private boolean commitTransfer(FTPTransfer ftpTransfer) {
        Object parentObj = ftpTransfer.getParent();
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
            FTPMessage pollFTPMessage) {
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
            pollFTPMessage.setPart(partName);
        } else {
            // need to create a message with part
            Message newMessage = Utilities.createMessage(mWsdlComponent.getModel(), "PollMessage");
            if (newMessage != null) {
                Part newPart = Utilities.createPart(mWsdlComponent.getModel(),
                        newMessage, "part1");
                if (newPart != null) {
                    Utilities.setPartType(newPart, getMessageType(),
                            getSelectedPartType(), getSelectedElementType());

                    if (Utilities.getMessagePart(newPart.getName(), newMessage) == null) {
                        newMessage.addPart(newPart);
                    }
                    pollFTPMessage.setPart(newPart.getName());
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

    public boolean getRequestResponseCorrelate() {
        return bRequestResponseCorrelate;
    }

    public Map exportProperties() {
        // nothing to export - but pass it on if any
        return mWzdProps;
    }

    public void importProperties(Map p) {
        mWzdProps = p;
        if (mWzdProps != null && mWzdProps.size() > 0) {
            updateView(mWzdProps);
        }
    }

    public void ancestorAdded(AncestorEvent event) {
        // check if me become visible
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
