package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wsdlextensions.email.editor.panels.InboundMessagePanel;
import org.netbeans.modules.wsdlextensions.email.editor.EmailError;
import org.netbeans.modules.wsdlextensions.email.editor.Form;
import org.netbeans.modules.wsdlextensions.email.editor.Form.FormModel;

/**
 * IMAP/POP3 Binding
 *
 */
public class InboundBindingConfigurationEditorForm extends InboundMessagePanel {

    private final Model model;

    private boolean mEnablePayloadProcessing = true;

    public InboundBindingConfigurationEditorForm(Model model) {
        super();
        this.model = model;
        init();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                mEmailServerTextField.requestFocusInWindow();
            }
        });

    }

    private void init() {
    }

    /**
     * Signal for the form to reread its data model into its view, in effect
     * discarding uncommitted changes made thru the view.
     */
    public void refresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("refresh()", new Runnable() {

                public void run() {
                    refresh();
                }
            });
            return;
        }
        // Email Address
        mEmailServerTextField.setText(model.getEmailServer());
        mPortTextField.setText(model.getPort());
        mUserNameTextField.setText(model.getUserName());
        mPasswordField.setText(model.getPassword());
        mUseSSL.setSelected(model.getUseSSL());
        mMailFolderTextField.setText(model.getMailFolder());
        mMaxMessageCountTextField.setText(model.getMaxMessageCount());
        mMessageAckModeComboBox.setSelectedItem(model.getMessageAckMode());
        mMessageAckOperationComboBox.setSelectedItem(model.getMessageAckOperation());
        mPollingIntervalTextField.setText(model.getPollingInterval());

        // Email Message
        mSaveAttDirTextField.setText(model.getSaveAttachmentsToDir());
        // mHandleNMAttComboBox.setSelectedItem(model.getHandleNMAttachments());

    }

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    public void commit() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("commit()", new Runnable() {

                public void run() {
                    commit();
                }
            });
            return;
        }

        // Email Address
        model.setEmailServer(mEmailServerTextField.getText());
        model.setPort(mPortTextField.getText());
        model.setUserName(mUserNameTextField.getText());
        model.setPassword(new StringBuilder().append(mPasswordField.getPassword()).toString());
        model.setUseSSL(mUseSSL.isSelected());
        model.setMailFolder(mMailFolderTextField.getText());
        model.setMaxMessageCount(mMaxMessageCountTextField.getText());
        model.setMessageAckMode(mMessageAckModeComboBox.getSelectedItem().toString());
        model.setMessageAckOperation(mMessageAckOperationComboBox.getSelectedItem().toString());
        model.setPollingInterval(mPollingIntervalTextField.getText());

        // Email Message
        model.setSaveAttachmentsToDir(mSaveAttDirTextField.getText());
        //model.setHandleNMAttachments(Boolean.parseBoolean(mHandleNMAttComboBox.getSelectedItem().toString()));

    }

    /**
     * Validate the changes before the form update its data model
     *
     *
     * @return boolean.
     */
    private boolean requiredInformationExists() {
        boolean success = false;
        success = !Utils.safeString(mEmailServerTextField.getText()).equals("");
        return success;
    }

    /**
     * Populate the form's internal data model with the information provided. If
     * the supplied model is not a type that is recognized or meaningful, it is
     * disregarded.
     *
     * @param model A supported FormModel instance.
     */
    public void loadModel(FormModel model) {
    }

    /**
     * Returns the form's own data model.
     *
     * @return Form data model
     */
    public FormModel getModel() {
        return model;
    }

    public void setEnablePayloadProcessing(boolean enable) {
        this.mEnablePayloadProcessing = enable;
    }

    /**
     * The Swing component that represents the form's visual representation.
     *
     * @return The form's view.
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * Validate the changes before commiting the data
     *
     *
     * @return EmailError.
     */
    public EmailError validateMe() {
        EmailError emailError = new EmailError();
        return emailError;
    }

    public static void syncToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException {
        if (!(destModel instanceof InboundBindingConfigurationEditorForm.Model)) {
            return;
        }
        if (!(srcModel instanceof InboundBindingConfigurationEditorForm.Model)) {
            return;
        }
        InboundBindingConfigurationEditorForm.Model dest =
                (InboundBindingConfigurationEditorForm.Model) destModel;
        InboundBindingConfigurationEditorForm.Model src =
                (InboundBindingConfigurationEditorForm.Model) srcModel;

        // Email Address
        dest.setEmailServer(src.getEmailServer());
        dest.setPort(src.getPort());
        dest.setUserName(src.getUserName());
        dest.setPassword(src.getPassword());
        dest.setUseSSL(src.getUseSSL());
        dest.setMailFolder(src.getMailFolder());
        dest.setMaxMessageCount(src.getMaxMessageCount());
        dest.setMessageAckMode(src.getMessageAckMode());
        dest.setMessageAckOperation(src.getMessageAckOperation());
        dest.setPollingInterval(src.getPollingInterval());

        // Email Message
        dest.setSaveAttachmentsToDir(src.getSaveAttachmentsToDir());
        //dest.setHandleNMAttachments(src.getHandleNMAttachments());

    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        public String getEmailServer();

        public void setEmailServer(String emailServer);

        public String getPort();

        public void setPort(String port);

        public String getUserName();

        public void setUserName(String userName);

        public String getPassword();

        public void setPassword(String password);

        public boolean getUseSSL();

        public void setUseSSL(boolean useSSL);

        public String getMailFolder();

        public void setMailFolder(String mailFolder);

        public String getMaxMessageCount();

        public void setMaxMessageCount(String maxMsgCount);

        public String getMessageAckMode();

        public void setMessageAckMode(String msgAckMode);

        public String getMessageAckOperation();

        public void setMessageAckOperation(String msgAckOperation);

        public String getPollingInterval();

        public void setPollingInterval(String pollingInterval);

        public String getSaveAttachmentsToDir();

        public void setSaveAttachmentsToDir(String saveAttDir);
        //public boolean getHandleNMAttachments();
        //public void setHandleNMAttachments(boolean handleNMAtt);
    }
}
