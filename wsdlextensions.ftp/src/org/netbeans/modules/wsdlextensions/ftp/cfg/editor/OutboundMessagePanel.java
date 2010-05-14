/*
 * OutboundMessagePanel.java
 *
 * Created on September 20, 2008, 9:25 AM
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
public class OutboundMessagePanel extends javax.swing.JPanel implements AncestorListener, PropertyAccessible, PropertyChangeSupport, BindingConfigurationDelegate {

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
    private static final Logger mLogger = Logger.getLogger(OutboundMessagePanel.class.getName());
    private DescriptionPanel descPanel = null;
    private MyItemListener mItemListener = null;
    private MyActionListener mActionListener = null;
    private MyDocumentListener mDocumentListener = null;
    private boolean bRequest;
    private Part mPart = null;
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
    private boolean bRequestResponseCorrelate;
    private boolean mOneWay;

    /** Creates new form OutboundMessagePanel */
    public OutboundMessagePanel(QName qName, WSDLComponent component, boolean oneway, boolean b) {
        this(qName, component, oneway, b, null);
    }

    public OutboundMessagePanel(QName qName, WSDLComponent component, boolean oneway, boolean b, PropertyChangeSupport proxy) {
        mOneWay = oneway;
        bRequest = b;
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
        return NbBundle.getMessage(OutboundMessagePanel.class,
                bRequest ? "OutboundMessagePanel.Request.StepLabel" : "OutboundMessagePanel.Response.StepLabel");
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
        outboundMessagingConfigPanel.add(mMessageTypePanel, gridBagConstraints);

        javax.swing.JPanel tmpPanel = new javax.swing.JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        outboundMessagingConfigPanel.add(tmpPanel, gridBagConstraints);
        /**
         * invisible components used to keep status:
         */
        servicePortComboBox = new JComboBox();
        bindingNameComboBox = new JComboBox();
        portTypeComboBox = new JComboBox();
        operationNameComboBox = new JComboBox();
        partComboBox = new JComboBox();

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
                    FTPMessage putMessage = getPutFTPMessage(binding,
                            bop.getName());
                    updatePutMessageView(binding, putMessage);
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

    private FTPMessage getPutFTPMessage(Binding binding,
            String selectedOperation) {
        FTPMessage putFTPMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    List<FTPMessage> putFTPMessages = null;
                    if (bRequest) {
                        putFTPMessages = bop.getBindingInput().getExtensibilityElements(FTPMessage.class);
                    } else {
                        putFTPMessages = bop.getBindingOutput().getExtensibilityElements(FTPMessage.class);
                    }
                    if (putFTPMessages.size() > 0) {
                        putFTPMessage = putFTPMessages.get(0);
                        break;
                    }
                }
            }
        }
        return putFTPMessage;
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

    private void updatePutMessageView(Binding binding, FTPMessage putMessage) {
        messageNamePrefixText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageNamePrefixOB"));
        messageNameText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageName"));
        messageRepoText.setToolTipText(mBundle.getString("DESC_Attribute_Message_messageRepository"));
        protectCheck.setToolTipText(mBundle.getString("DESC_Attribute_Message_protect"));
        stagingCheck.setToolTipText(mBundle.getString("DESC_Attribute_Message_stage"));
        
        if (putMessage != null) {
            messageNamePrefixText.setText(putMessage.getMessageNamePrefixOB());
            messageNameText.setText(putMessage.getMessageName());
            messageRepoText.setText(putMessage.getMessageRepository());
            bRequestResponseCorrelate = putMessage.getMessageCorrelateEnabled();

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

            protectCheck.setSelected(putMessage.getProtectEnabled());
            stagingCheck.setSelected(putMessage.getStagingEnabled());

            Object obj = putMessage.getParent();
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
            String part = putMessage.getPart();
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
                    putMessage, mProject,
                    operationNameComboBox.getSelectedItem() != null ? operationNameComboBox.getSelectedItem().toString() : "");

        } else {
            // null out view
            messageNamePrefixText.setText("");
            messageNameText.setText("");
            messageRepoText.setText("");
            protectCheck.setSelected(true);
            stagingCheck.setSelected(true);
            mMessageTypePanel.populateView(mWsdlComponent, mPart,
                    putMessage, null, null);

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
        messageNamePrefixText.getDocument().removeDocumentListener(mDocumentListener);
        messageNameText.getDocument().removeDocumentListener(mDocumentListener);
        messageRepoText.getDocument().removeDocumentListener(mDocumentListener);

        messageNamePrefixText.removeActionListener(mActionListener);
        messageNameText.removeActionListener(mActionListener);
        messageRepoText.removeActionListener(mActionListener);

        messageNamePrefixText.setText("");
        messageNameText.setText("");
        messageRepoText.setText("");
        stagingCheck.setSelected(true);
        protectCheck.setSelected(true);
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

        messageNamePrefixText.getDocument().addDocumentListener(mDocumentListener);
        messageNameText.getDocument().addDocumentListener(mDocumentListener);
        messageRepoText.getDocument().addDocumentListener(mDocumentListener);
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
                        mBundle.getString("DESC_Attribute_Message_messageNamePrefixOB")
                    };
        } else if (evt.getSource() == messageNameText) {
            desc = new String[]{"Message Name\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageName")
                    };
        } else if (evt.getSource() == messageRepoText) {
            desc = new String[]{"Messaging Repository\n\n",
                        mBundle.getString("DESC_Attribute_Message_messageRepository")
                    };
        } else if (evt.getSource() == protectCheck) {
            desc = new String[]{"Enable Overwrite Protect\n\n",
                        mBundle.getString("DESC_Attribute_Message_protect")
                    };
        } else if (evt.getSource() == stagingCheck) {
            desc = new String[]{"Enable Staging When Put Message\n\n",
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
        outboundMessagingConfigPanel = new javax.swing.JPanel();
        putReqOrRespConfigPanel = new javax.swing.JPanel();
        requestLab = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        messageRepoLab = new javax.swing.JLabel();
        messageRepoText = new javax.swing.JTextField();
        messageNameLab = new javax.swing.JLabel();
        messageNameText = new javax.swing.JTextField();
        messagePreifxLab = new javax.swing.JLabel();
        messageNamePrefixText = new javax.swing.JTextField();
        protectCheck = new javax.swing.JCheckBox();
        stagingCheck = new javax.swing.JCheckBox();
        msgRepoImgLab = new javax.swing.JLabel();
        msgNameImgLab = new javax.swing.JLabel();
        msgPrefixImgLab = new javax.swing.JLabel();
        overwriteProtectImgLab = new javax.swing.JLabel();
        stagingImgLab = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(612, 650));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setPreferredSize(new java.awt.Dimension(612, 650));

        outboundMessagingConfigPanel.setMinimumSize(new java.awt.Dimension(610, 520));
        outboundMessagingConfigPanel.setName("outboundMessagingConfigPanel"); // NOI18N
        outboundMessagingConfigPanel.setPreferredSize(new java.awt.Dimension(610, 520));
        outboundMessagingConfigPanel.setLayout(new java.awt.GridBagLayout());

        putReqOrRespConfigPanel.setMinimumSize(new java.awt.Dimension(610, 260));
        putReqOrRespConfigPanel.setName("putReqOrRespConfigPanel"); // NOI18N
        putReqOrRespConfigPanel.setPreferredSize(new java.awt.Dimension(610, 260));
        putReqOrRespConfigPanel.setLayout(new java.awt.GridBagLayout());

        requestLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(requestLab, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.requestLab.text")); // NOI18N
        requestLab.setName("requestLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        putReqOrRespConfigPanel.add(requestLab, gridBagConstraints);

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

        messageRepoLab.setLabelFor(messageRepoText);
        org.openide.awt.Mnemonics.setLocalizedText(messageRepoLab, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageRepoLab.text")); // NOI18N
        messageRepoLab.setName("messageRepoLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(messageRepoLab, gridBagConstraints);
        messageRepoLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageRepoLab.AccessibleContext.accessibleDescription")); // NOI18N

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
        putReqOrRespConfigPanel.add(messageRepoText, gridBagConstraints);
        messageRepoText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageRepoText.AccessibleContext.accessibleName")); // NOI18N
        messageRepoText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageRepoText.AccessibleContext.accessibleDescription")); // NOI18N

        messageNameLab.setLabelFor(messageNameText);
        org.openide.awt.Mnemonics.setLocalizedText(messageNameLab, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNameLab.text")); // NOI18N
        messageNameLab.setName("messageNameLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(messageNameLab, gridBagConstraints);
        messageNameLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNameLab.AccessibleContext.accessibleDescription")); // NOI18N

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
        putReqOrRespConfigPanel.add(messageNameText, gridBagConstraints);
        messageNameText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNameText.AccessibleContext.accessibleName")); // NOI18N
        messageNameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNameText.AccessibleContext.accessibleDescription")); // NOI18N

        messagePreifxLab.setLabelFor(messageNamePrefixText);
        org.openide.awt.Mnemonics.setLocalizedText(messagePreifxLab, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messagePreifxLab.text")); // NOI18N
        messagePreifxLab.setName("messagePreifxLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(messagePreifxLab, gridBagConstraints);
        messagePreifxLab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messagePreifxLab.AccessibleContext.accessibleDescription")); // NOI18N

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
        putReqOrRespConfigPanel.add(messageNamePrefixText, gridBagConstraints);
        messageNamePrefixText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNamePrefixText.AccessibleContext.accessibleName")); // NOI18N
        messageNamePrefixText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.messageNamePrefixText.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(protectCheck, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.protectCheck.text")); // NOI18N
        protectCheck.setName("protectCheck"); // NOI18N
        protectCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                protectCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                protectCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(protectCheck, gridBagConstraints);
        protectCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.protectCheck.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stagingCheck, org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.stagingCheck.text")); // NOI18N
        stagingCheck.setName("stagingCheck"); // NOI18N
        stagingCheck.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                stagingCheckFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                stagingCheckFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 10, 10);
        putReqOrRespConfigPanel.add(stagingCheck, gridBagConstraints);
        stagingCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.stagingCheck.AccessibleContext.accessibleDescription")); // NOI18N

        msgRepoImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgRepoImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgRepoImgLab.text")); // NOI18N
        msgRepoImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgRepoImgLab.toolTipText")); // NOI18N
        msgRepoImgLab.setName("msgRepoImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(msgRepoImgLab, gridBagConstraints);

        msgNameImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgNameImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgNameImgLab.text")); // NOI18N
        msgNameImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgNameImgLab.toolTipText")); // NOI18N
        msgNameImgLab.setName("msgNameImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(msgNameImgLab, gridBagConstraints);

        msgPrefixImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        msgPrefixImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgPrefixImgLab.text")); // NOI18N
        msgPrefixImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.msgPrefixImgLab.toolTipText")); // NOI18N
        msgPrefixImgLab.setName("msgPrefixImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(msgPrefixImgLab, gridBagConstraints);

        overwriteProtectImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        overwriteProtectImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.overwriteProtectImgLab.text")); // NOI18N
        overwriteProtectImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.overwriteProtectImgLab.toolTipText")); // NOI18N
        overwriteProtectImgLab.setName("overwriteProtectImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(overwriteProtectImgLab, gridBagConstraints);

        stagingImgLab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/service_composition_16.png"))); // NOI18N
        stagingImgLab.setText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.stagingImgLab.text")); // NOI18N
        stagingImgLab.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.stagingImgLab.toolTipText")); // NOI18N
        stagingImgLab.setName("stagingImgLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        putReqOrRespConfigPanel.add(stagingImgLab, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        outboundMessagingConfigPanel.add(putReqOrRespConfigPanel, gridBagConstraints);
        putReqOrRespConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.putReqOrRespConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        putReqOrRespConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.putReqOrRespConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(outboundMessagingConfigPanel);
        outboundMessagingConfigPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.outboundMessagingConfigPanel.AccessibleContext.accessibleName")); // NOI18N
        outboundMessagingConfigPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.outboundMessagingConfigPanel.AccessibleContext.accessibleDescription")); // NOI18N

        descriptionPanel.setMinimumSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(610, 60));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);
        descriptionPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.descriptionPanel.AccessibleContext.accessibleName")); // NOI18N
        descriptionPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundMessagePanel.class, "OutboundMessagePanel.descriptionPanel.AccessibleContext.accessibleDescription")); // NOI18N

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

private void protectCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_protectCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_protectCheckFocusGained

private void protectCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_protectCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_protectCheckFocusLost

private void stagingCheckFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stagingCheckFocusGained
    updateDescriptionArea(evt);
}//GEN-LAST:event_stagingCheckFocusGained

private void stagingCheckFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stagingCheckFocusLost
// TODO add your handling code here:
}//GEN-LAST:event_stagingCheckFocusLost

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
        } else if (mWsdlComponent instanceof FTPMessage) {
            result = commitMessage((FTPMessage) mWsdlComponent);
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
        }

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
        if ( mProxy != null )
            mProxy.doFirePropertyChange(name, oldValue, newValue);
        else
            firePropertyChange(name, oldValue,
                newValue);
    }

    private boolean commitAddress(FTPAddress ftpAddress) {
        WSDLModel wsdlModel = ftpAddress.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            // bRequest = true
            //
            // put req
            //
            // bRequest = false
            //
            // put resp
            //
            Port port = (Port) ftpAddress.getParent();
            Binding binding = port.getBinding().get();
            String operationName = getOperationName();
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();

            // only 1
            // 
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(operationName)) {
                    // put message is inside <input> if is for request
                    // put message is inside <output> if is for response
                    FTPMessage putFTPMessage = null;
                    List<FTPMessage> putFTPMessages = null;
                    List<FTPTransfer> putFTPTransfers = null;
                    if (bRequest) {
                        BindingInput bi = bop.getBindingInput();
                        if (bi != null) {
                            putFTPMessages = bi.getExtensibilityElements(FTPMessage.class);
                            if (putFTPMessages.size() > 0) {
                                putFTPMessage = putFTPMessages.get(0);
//                                commitPutMessage(binding, operationName, putFTPMessage);
                            }
                            else {
                                // create ftp:message and populated and commit
                                // a work around when the framework does not
                                // provide a initial ftp:message here
                                // we need to remove the ftp:transfer
                                // and plant our ftp:message
                                putFTPTransfers =
                                        bi.getExtensibilityElements(FTPTransfer.class);
                                if ( putFTPTransfers != null && putFTPTransfers.size() > 0) {
                                    // wrong binding with binding scheme ftp:transfer
                                    for (int i = 0; i < putFTPTransfers.size(); i++) {
                                        if ( putFTPTransfers.get(i) != null )
                                        bi.removeExtensibilityElement(putFTPTransfers.get(i));
                                    }
                                }
                                bi.addExtensibilityElement((putFTPMessage = (FTPMessage)Utilities.createFTPMessage(mWsdlComponent)));
                            }
                        }
                    } else {
                        BindingOutput bo = bop.getBindingOutput();
                        if (bo != null) {
                            putFTPMessages =
                                    bo.getExtensibilityElements(FTPMessage.class);
                            if (putFTPMessages.size() > 0) {
                                putFTPMessage = putFTPMessages.get(0);
                            }
                            else {
                                // create ftp:message and populated and commit
                                // a work around when the framework does not
                                // provide a initial ftp:message here
                                // we need to remove the ftp:transfer
                                // and plant our ftp:message
                                putFTPTransfers =
                                        bo.getExtensibilityElements(FTPTransfer.class);
                                if ( putFTPTransfers != null && putFTPTransfers.size() > 0) {
                                    // wrong binding with binding scheme ftp:transfer
                                    for (int i = 0; i < putFTPTransfers.size(); i++) {
                                        if ( putFTPTransfers.get(i) != null )
                                        bo.removeExtensibilityElement(putFTPTransfers.get(i));
                                    }
                                }
                                bo.addExtensibilityElement((putFTPMessage = (FTPMessage)Utilities.createFTPMessage(mWsdlComponent)));
                            }
                        }
                    }
                    if ( binding != null &&operationName != null && putFTPMessage != null ) {
                        commitPutMessage(binding, operationName, putFTPMessage);
                    }
                    else {
                        // should report error
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        } //finally {
//            if (wsdlModel.isIntransaction()) {
//                wsdlModel.endTransaction();
//            }
        return true;
    }

    private boolean commitBinding(FTPBinding ftpBinding) {
        WSDLModel wsdlModel = ftpBinding.getModel();
        try {
            if (!wsdlModel.isIntransaction()) {
                wsdlModel.startTransaction();
            }
            Binding binding = (Binding) ftpBinding.getParent();

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
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

    private void commitPutMessage(Binding binding, String opName,
            FTPMessage putFTPMessage) {
        putFTPMessage.setMessageName(messageNameText.getText());
        putFTPMessage.setMessageNamePrefixOB(messageNamePrefixText.getText());
        putFTPMessage.setMessageRepository(messageRepoText.getText());

        Boolean b = null;

        if ( mWzdProps != null ) {
            b = (Boolean)mWzdProps.get(FTPConstants.WSDL_PROP_REQRESPCORRELATE);
        }
        
        putFTPMessage.setMessageCorrelateEnabled(mOneWay ? false : (b != null ? b.booleanValue() : true));

        putFTPMessage.setUse(FTPConstants.LITERAL);
        if (mMessageTypePanel.getInputUseType() != null) {
            if (mMessageTypePanel.getInputUseType().equals(FTPConstants.ENCODED)) {        
                putFTPMessage.setEncodingStyle(mMessageTypePanel.getEncodingStyle());
                putFTPMessage.setUse(mMessageTypePanel.getInputUseType());
            } else if ((putFTPMessage.getUse() != null) &&
                        (mMessageTypePanel.getInputUseType().equals(FTPConstants.LITERAL))) {
                    putFTPMessage.setEncodingStyle(null);                   
            }
        } else {
            putFTPMessage.setAttribute(FTPMessage.FTP_ENCODINGSTYLE_PROPERTY, null);
        }
        
        if ( mMessageTypePanel.getMessageType() == FTPConstants.BINARY_MESSAGE_TYPE )
            putFTPMessage.setFileType(FTPConstants.BINARY);
        else {
            putFTPMessage.setFileType(FTPConstants.TEXT);
            String enc = mMessageTypePanel.getMessageCharEncoding();
            if ( enc != null && enc.trim().length() > 0 ) {
                putFTPMessage.setCharacterEncoding(enc.trim());
            }
        }

        // set the type of the message part if it is undefined based
        // on the MessageType option from user
        commitMessageType(binding, opName, putFTPMessage);

        putFTPMessage.setAttribute(FTPMessage.FTP_POLLINTERVAL_PROPERTY, null);
    }

    private boolean commitPort(Port port) {
        Collection<FTPAddress> address = port.getExtensibilityElements(FTPAddress.class);
        if ( address != null && address.size() > 0 ) {
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
            FTPMessage putFTPMessage) {
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
            Message newMessage = Utilities.createMessage(mWsdlComponent.getModel(), "PutMessage");
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
    private javax.swing.JPanel descriptionPanel;
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
    private javax.swing.JPanel outboundMessagingConfigPanel;
    private javax.swing.JLabel overwriteProtectImgLab;
    private javax.swing.JCheckBox protectCheck;
    private javax.swing.JPanel putReqOrRespConfigPanel;
    private javax.swing.JLabel requestLab;
    private javax.swing.JCheckBox stagingCheck;
    private javax.swing.JLabel stagingImgLab;
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
        if ( mWzdProps != null && mWzdProps.size() > 0 ) {
            updateView(mWzdProps);
        }
    }

    public void ancestorAdded(AncestorEvent event) {
        if (event.getSource() == this) {
            if ( mProxy != null ) {
                // embedded in CASA plugin
                // need to validate all the tabbed panels
                ((ValidationProxy)mProxy).validatePlugin();
            }
            else {
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
        if ( desc != null && desc.getErrorMode() != null && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            result = false;
        }
        return result;
    }
}
