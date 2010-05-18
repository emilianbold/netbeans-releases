/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents ofleft this file are subject to the terms of either the GNU
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
package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileBinding;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileMessage;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;
import org.netbeans.modules.wsdlextensions.file.validator.FileComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.utils.WSDLUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.actions.SystemAction;
import org.openide.awt.Mnemonics;

/**
 * FileBindingConfigurationPanel - Panel that allows configuration of
 * properties specifically for File Binding component
 *
 * @author  jalmero
 */
public class FileBindingConfigurationPanel extends javax.swing.JPanel {

    /** the WSDL model to configure **/
    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.file.resources.Bundle");

    /** style document for description area **/
    private StyledDocument mDoc = null;
    private StyledDocument mDocAdv = null;
    private StyledDocument mDocAdvOut = null;
    private String[] mStyles = null;

    /** error buffer **/
    private StringBuffer mErrBuff = null;
    private Icon mCASAImg = null;

    /** mode for inbound only **/
    private boolean mInOnly = false;

    private static final Logger mLogger = Logger.
            getLogger(FileBindingConfigurationPanel.class.getName());

    private MyItemListener mItemListener = null;

    /** Creates new form FileBindingConfigurationPanel */
    public FileBindingConfigurationPanel(QName qName, WSDLComponent component) {
        mComponent = component;
        mQName = qName;
        initComponents();
        initFileChooser();
        populateView(mComponent);
    }

    /**
     * Trims input and returns null, if blank.
     *
     * @param text
     * @return trimmed text, if blank returns null.
     */
    private String trimTextFieldInput(String text) {
        if (text == null) {
            return text;
        }
        String trimmedText = text.trim();
        if (trimmedText.length() == 0) {
            return null;
        }
        return text.trim();
    }

    /**
     * Return the directory setting
     * @return String directory path
     */
    String getFileAddress() {
        File file = new File(getDirectoryTextField().getText());
        String absPath = file.getAbsolutePath();
        if ((absPath != null) && (absPath.equals(""))) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * Return the file lock name
     * @return String file name to represent lock
     */
    String getFileLockName() {
        return lockNameTextField.getText();
    }

    /**
     * Return the input file name
     * @return String file name
     */
    String getInputFileName() {
        // since the enhancement that
        // a regular expression can be put
        // as the file matching criteria
        // and a regex can contain chars
        // like "/" or "\" etc. that might be
        // recognized by java.io.File as path separator
        // hence causing the regular expression
        // screwed up
        // refer to open esb issue 2126:
        // https://open-esb.dev.java.net/issues/show_bug.cgi?id=2126
        //
//        File file = new File(inputFileNameTextField.getText());
//        return file.getName();
        return inputFileNameTextField.getText();
    }

    /**
     * Return the output file name
     * @return String file name
     */
    String getOutputFileName() {
        // since the enhancement that
        // a regular expression can be put
        // as the file matching criteria
        // and a regex can contain chars
        // like "/" or "\" etc. that might be
        // recognized by java.io.File as path separator
        // hence causing the regular expression
        // screwed up
        // refer to open esb issue 2126:
        // https://open-esb.dev.java.net/issues/show_bug.cgi?id=2126
        //
//        File file = new File(outputFileNameTextField.getText());
//        return file.getName();
        return outputFileNameTextField.getText();
    }

    /**
     * Return the polling interval
     * @return long polling interval
     */
    long getInputPollingInterval() {
        if (inputPollingIntervalSpinner.getValue() != null) {
            return new Long(inputPollingIntervalSpinner.getValue().toString()).
                    longValue();
        } else {
            return 0;
        }
    }

    /**
     * Return the service port
     * @return String service port
     */
    Port getServicePort() {
        return (Port) servicePortComboBox.getSelectedItem();
    }

    /**
     * Return the binding name used
     * @return String binding name
     */
    String getBinding() {
        if ((bindingNameComboBox.getSelectedItem() != null) &&
                (!bindingNameComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return bindingNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the port type used
     * @return String port type name
     */
    String getPortType() {
        if ((portTypeComboBox.getSelectedItem() != null) &&
                (!portTypeComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return portTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the use type for input message
     * @return String use type
     */
    String getInputUseType() {
        if (inputUseLiteralRadioBtn.isSelected()) {
            return FileConstants.LITERAL;
        } else {
            return FileConstants.ENCODED;
        }
    }

    /**
     * Return the use type for output message
     * @return String use type
     */
    String getOutputUseType() {
        if (outputUseLiteralRadioBtn.isSelected()) {
            return FileConstants.LITERAL;
        } else {
            return FileConstants.ENCODED;
        }
    }

    /**
     * Return the work area value
     * @return String work area
     */
    String getWorkArea() {
        return trimTextFieldInput(workAreaTextField.getText());
    }

    /**
     * Return the sequential name value
     * @return String sequential name
     */
    String getSeqName() {
        return trimTextFieldInput(seqNameTextField.getText());
    }

    /**
     * Return the relative path value
     * @return String relative path
     */
    String getRelativePath() {
        if (isRelativePathBox.isSelected()) {
            return FileConstants.BOOLEAN_TRUE;
        } else {
            return FileConstants.BOOLEAN_FALSE;
        }
    }

    /**
     * Return the path relative to value
     * @return String path relative to name
     */
    String getPathRelativeTo() {
        if ((pathRelativeToComboBox.getSelectedItem() != null) &&
                (!pathRelativeToComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return pathRelativeToComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the operation name
     * @return String operation name
     */
    String getOperationName() {
        if ((operationNameComboBox.getSelectedItem() != null) &&
                (!operationNameComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return operationNameComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the encoding style value
     * @return String encoding style
     */
    String getEncodingStyle() {
        return trimTextFieldInput(inputEncodingStyleTextField.getText());
    }

    /**
     * Return the input file type value
     * @return input file type
     */
    String getInputFileType() {
        if ((inputFileTypeComboBox.getSelectedItem() != null) &&
                (!inputFileTypeComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return inputFileTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the file is pattern value for input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputFileIsPattern() {
        if (inputFileIsPatternBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return the part value for the input message
     * return String part used
     */
    String getInputPart() {
        return trimTextFieldInput((String) inputPartComboBox.getSelectedItem());
    }

    /**
     * Return the remove eol value for the input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputRemoveEOL() {
        if (inputRemoveEOLBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return the remove eol value for the input message
     * @return boolean true if file is pattern; otherwise false
     */
    boolean getInputAddEOL() {
        if (inputRemoveEOLBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return record delimiter for input message
     * @return String record delimiter
     */
    String getInputRecordDelimiter() {
        return trimTextFieldInput(inputRecordDelimiterTextField.getText());
    }

    /**
     * Return multi records per file value for input message
     * @return boolean true if multi records per file is set; otherwise false
     */
    boolean getInputMultiRecordsPerFile() {
        if (inputMultiRecordsPerFileBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return maximum bytes per record value for input message
     * @return long maximum bytes per record
     */
    long getInputMaxBytesPerRecord() {
        if (inputMaxBytesPerRecordSpinner.getValue() != null) {
            return new Long(inputMaxBytesPerRecordSpinner.getValue().
                    toString()).longValue();
        } else {
            return 0;
        }
    }

    /**
     * Return archive value for input message
     * @return boolean true if archive is set; otherwise false
     */
    boolean getInputArchive() {
        if (inputArchiveBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return archive relative value for input message
     * @return boolean true if archive relative is set; otherwise false
     */
    boolean getInputArchiveRelative() {
        if (inputArchiveRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return archive directory value for input message
     * @return String archive directory
     */
    String getInputArchiveDirectory() {
        File file = new File(inputArchiveDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    /**
     * Return protect value for input message
     * @return boolean true if protect is set
     */
    boolean getInputProtect() {
        if (inputProtectBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return protect relative value for input message
     * @return boolean true if protect relative is set
     */
    boolean getInputProtectRelative() {
        if (inputProtectRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return protect directory value for input message
     * @return String protect directory
     */
    String getInputProtectDirectory() {
        File file = new File(inputProtectDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    /**
     * Return stage value for input message
     * @return boolean true if stage is set
     */
    boolean getInputStage() {
        if (inputStageBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return stage relative value for input message
     * @return boolean true if stage relative is set; otherwise false
     */
    boolean getInputStageRelative() {
        if (inputStageRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return stage directory value for input message
     * @return String stage directory
     */
    String getInputStageDirectory() {
        File file = new File(inputStageDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    /**
     * Return encoding style value for output message
     * @return String encoding style
     */
    String getOutputEncodingStyle() {
        return trimTextFieldInput(outputEncodingStyleTextField.getText());
    }

    /**
     * Return file type value for output message
     * @return String file type
     */
    String getOutputFileType() {
        if ((outputFileTypeComboBox.getSelectedItem() != null) &&
                (!outputFileTypeComboBox.getSelectedItem().toString().
                equals(FileConstants.NOT_SET))) {
            return outputFileTypeComboBox.getSelectedItem().toString();
        } else {
            return null;
        }
    }

    /**
     * Return the file is pattern value for output message
     * @return boolean true if file is pattern is set; otherwise false
     */
    boolean getOutputFileIsPattern() {
        if (outputFileIsPatternBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return part value for output message
     * @return String part
     */
    String getOutputPart() {
        return trimTextFieldInput((String) outputPartComboBox.getSelectedItem());
    }

    /**
     * Return the add EOL value for output message
     * @return boolean true if add eol is set; otherwise false
     */
    boolean getOutputAddEOL() {
        if (outputAddEOLBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return record delimiter value for output message
     * @return String record delimiter
     */
    String getOutputRecordDelimiter() {
        return trimTextFieldInput(outputRecordDelimiterTextField.getText());
    }

    /**
     * Return the multiple records per file value for output message
     * @return boolean true if multiple records per file is set; otherwise false
     */
    boolean getOutputMultiRecordsPerFile() {
        if (outputMultiRecordsPerFileBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return the maximum bytes per record value for output message
     * @return long maximum bytes per record
     */
    long getOutputMaxBytesPerRecord() {
        if (outputMaxBytesPerRecordSpinner.getValue() != null) {
            return new Long(outputMaxBytesPerRecordSpinner.getValue().
                    toString()).longValue();
        } else {
            return 0;
        }
    }

    /**
     * Return archive value for output message
     * @return boolean true if protect is set
     */
    boolean getOutputArchive() {
        if (outputArchiveBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return archive relative value for output message
     * @return boolean true if protect relative is set
     */
    boolean getOutputArchiveRelative() {
        if (outputArchiveIsRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return archive directory value for output message
     * @return String protect directory
     */
    String getOutputArchiveDirectory() {
        File file = new File(outputArchiveDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    /**
     * Return protect value for output message
     * @return boolean true if protect is set
     */
    boolean getOutputProtect() {
        if (outputProtectBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return protect relative value for output message
     * @return boolean true if protect relative is set
     */
    boolean getOutputProtectRelative() {
        if (outputProtectIsRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return protect directory value for output message
     * @return String protect directory
     */
    String getOutputProtectDirectory() {
        File file = new File(outputProtectDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    /**
     * Return stage value for output message
     * @return boolean true if stage is set
     */
    boolean getOutputStage() {
        if (outputStageBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return stage relative value for output message
     * @return boolean true if stage relative is set; otherwise false
     */
    boolean getOutputStageRelative() {
        if (outputStageIsRelativeBox.isSelected()) {
            return FileConstants.stringValueIsTrue("true");
        } else {
            return false;
        }
    }

    /**
     * Return stage directory value for output message
     * @return String stage directory
     */
    String getOutputStageDirectory() {
        File file = new File(outputStageDirectoryTextField.getText());
        return file.getAbsolutePath();
    }

    int getReadWriteMode() {
        String modeSelected = (String) directionModeBox.getSelectedItem();
        if (modeSelected != null) {
            if (directionModeBox.isVisible() && modeSelected.equals(FileConstants.READ_STR)) {
                return FileConstants.READ;
            } else if (directionModeBox.isVisible() && modeSelected.equals(FileConstants.WRITE_STR)) {
                return FileConstants.WRITE;
            } else if (directionModeBox.isVisible() && modeSelected.equals(FileConstants.READ_WRITE_STR)) {
                return FileConstants.READ_WRITE;
            }
        }
        return -1;
    }

    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        // do FileBC-specific validation first

        boolean ok = validateMe();
        if (!ok) {
            return ok;
        }

        ValidationResult results = new FileComponentValidator().
                validate(mComponent.getModel(), null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }

    }

    public boolean validateMe() {
        boolean ok = true;
        String directory = getFileAddress();
        if ((getPathRelativeTo() != null) && (directory == null) ||
                (directory.equals(""))) {
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.FileDirectoryMustBeSet"));
            return false;
        }

        return ok;
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

        directoryFileChooser = new javax.swing.JFileChooser();
        readRBtn = new javax.swing.JRadioButton();
        writeRBtn = new javax.swing.JRadioButton();
        readWriteRBtn = new javax.swing.JRadioButton();
        mInputUseBtnGrp = new javax.swing.ButtonGroup();
        mOutputUseBtnGrp = new javax.swing.ButtonGroup();
        mReadWriteBtnGrp = new javax.swing.ButtonGroup();
        inputAdvMessageTabPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        inputScrollAdvPanel = new javax.swing.JPanel();
        inputAdvScrollPane = new javax.swing.JScrollPane();
        inputAdvDescriptionArea = new javax.swing.JTextPane();
        mDocAdv = inputAdvDescriptionArea.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style defAdv = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regularAdv = mDocAdv.addStyle("regular", defAdv);
        Style sAdv = mDocAdv.addStyle("bold", regularAdv);
        StyleConstants.setBold(sAdv, true);
        inputMessageTabPanelTop = new javax.swing.JPanel();
        inputMessageTabPanel = new javax.swing.JPanel();
        inputRecordDelimiterLabel = new javax.swing.JLabel();
        inputPartLabel = new javax.swing.JLabel();
        inputMaxBytesPerReLabel = new javax.swing.JLabel();
        inputPartComboBox = new javax.swing.JComboBox();
        inputArchDirectoryLabel = new javax.swing.JLabel();
        inputArchiveDirectoryTextField = new javax.swing.JTextField();
        inputArchiveButton = new javax.swing.JButton();
        inputFixedLengthBox = new javax.swing.JCheckBox();
        inputRemoveEOLBox = new javax.swing.JCheckBox();
        inputMultiRecordsPerFileBox = new javax.swing.JCheckBox();
        inputMaxBytesPerRecordSpinner = new javax.swing.JSpinner();
        inputArchiveBox = new javax.swing.JCheckBox();
        inputArchiveRelativeBox = new javax.swing.JCheckBox();
        inputPostProcessingLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        inputRecordDelimiterTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        inputProtectDirectoryLabel = new javax.swing.JLabel();
        inputProtectDirectoryTextField = new javax.swing.JTextField();
        inputProtectRelativeBox = new javax.swing.JCheckBox();
        inputProtectButton = new javax.swing.JButton();
        inputAddEOLBox = new javax.swing.JCheckBox();
        inputStageBox = new javax.swing.JCheckBox();
        inputStageDirectoryLabel = new javax.swing.JLabel();
        inputStageDirectoryTextField = new javax.swing.JTextField();
        inputStageRelativeBox = new javax.swing.JCheckBox();
        inputStageButton = new javax.swing.JButton();
        inputProtectBox = new javax.swing.JCheckBox();
        outputAdvMessageTabPanel = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        outputMessageTabPanel = new javax.swing.JPanel();
        outputReocrdDelimLabel = new javax.swing.JLabel();
        outputPartLabel = new javax.swing.JLabel();
        outputMaxBytesPerReLabel = new javax.swing.JLabel();
        outputRecordDelimiterTextField = new javax.swing.JTextField();
        outputPartComboBox = new javax.swing.JComboBox();
        outputProtectDirLabel = new javax.swing.JLabel();
        outputProtectDirectoryTextField = new javax.swing.JTextField();
        outputProtectButton = new javax.swing.JButton();
        outputStageDirLabel = new javax.swing.JLabel();
        outputStageDirectoryTextField = new javax.swing.JTextField();
        outputStageButton = new javax.swing.JButton();
        outputMaxBytesPerRecordSpinner = new javax.swing.JSpinner();
        outputAddEOLBox = new javax.swing.JCheckBox();
        outputMultiRecordsPerFileBox = new javax.swing.JCheckBox();
        outputFixedLengthBox = new javax.swing.JCheckBox();
        outputStageIsRelativeBox = new javax.swing.JCheckBox();
        outputStageBox = new javax.swing.JCheckBox();
        outputProtectIsRelativeBox = new javax.swing.JCheckBox();
        outputProtectBox = new javax.swing.JCheckBox();
        outputRecordLabel = new javax.swing.JLabel();
        outputPostProcessingLab = new javax.swing.JLabel();
        outputArchiveDirLabel = new javax.swing.JLabel();
        outputArchiveBox = new javax.swing.JCheckBox();
        outputArchiveDirectoryTextField = new javax.swing.JTextField();
        outputArchiveIsRelativeBox = new javax.swing.JCheckBox();
        outputArchiveButton = new javax.swing.JButton();
        outputRemoveEOLBox = new javax.swing.JCheckBox();
        outputScrollAdvPanel = new javax.swing.JPanel();
        outputAdvScrollPane = new javax.swing.JScrollPane();
        outputAdvDescriptionArea = new javax.swing.JTextPane();
        mDocAdvOut = outputAdvDescriptionArea.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style regularAdvOut = mDocAdv.addStyle("regular", defAdv);
        Style sAdvOut = mDocAdvOut.addStyle("bold", regularAdvOut);
        StyleConstants.setBold(sAdvOut, true);
        splitPane = new javax.swing.JSplitPane();
        bindingConfigurationPanel = new javax.swing.JTabbedPane();
        generalTabPanel = new javax.swing.JPanel();
        servicesGeneralPanel = new javax.swing.JPanel();
        bindingNameLabel = new javax.swing.JLabel();
        bindingNameComboBox = new javax.swing.JComboBox();
        portTypeLabel = new javax.swing.JLabel();
        portTypeComboBox = new javax.swing.JComboBox();
        portNameLabel = new javax.swing.JLabel();
        servicePortComboBox = new javax.swing.JComboBox();
        servicesGeneralLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        rwGeneralPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        directoryPropertiesSep = new javax.swing.JSeparator();
        directoryLabel = new javax.swing.JLabel();
        directoryLabImage = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        serviceBindingOperationLabel = new javax.swing.JLabel();
        operationNameLabel = new javax.swing.JLabel();
        readPropertiesGeneralLabel = new javax.swing.JLabel();
        inputFileNameLabel = new javax.swing.JLabel();
        writePropertiesGeneralLabel = new javax.swing.JLabel();
        outputFileNameLabel = new javax.swing.JLabel();
        outputFileNameTextField = new javax.swing.JTextField();
        sep3General = new javax.swing.JSeparator();
        inputFileNameTextField = new javax.swing.JTextField();
        operationNameComboBox = new javax.swing.JComboBox();
        sep1General = new javax.swing.JSeparator();
        inputFileIsPatternBox = new javax.swing.JCheckBox();
        outputFileIsPatternBox = new javax.swing.JCheckBox();
        sep2General = new javax.swing.JSeparator();
        advanceTabPanel = new javax.swing.JPanel();
        servicesAdvancePanel = new javax.swing.JPanel();
        servicesAdvancedLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pathRelativeToComboBox = new javax.swing.JComboBox();
        sep1Advanced = new javax.swing.JSeparator();
        pathRelativeToLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lockNameLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        workAreaLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        seqNameLabel = new javax.swing.JLabel();
        seqNameTextField = new javax.swing.JTextField();
        workAreaTextField = new javax.swing.JTextField();
        lockNameTextField = new javax.swing.JTextField();
        isRelPathPanel = new javax.swing.JPanel();
        isRelativePathCasaLabel = new javax.swing.JLabel();
        isRelativePathBox = new javax.swing.JCheckBox();
        directionAdvancePanel = new javax.swing.JPanel();
        directionLab = new javax.swing.JLabel();
        directionSep = new javax.swing.JSeparator();
        modeLab = new javax.swing.JLabel();
        directionModeBox = new javax.swing.JComboBox();
        rAdvancePanel = new javax.swing.JPanel();
        readPropertiesAdvLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        inputUseLabel = new javax.swing.JLabel();
        inputEncodingStyleLabel = new javax.swing.JLabel();
        inputPollingIntervalLabel = new javax.swing.JLabel();
        inputPollingIntervalSpinner = new javax.swing.JSpinner();
        inputEncodingStyleTextField = new javax.swing.JTextField();
        inputUseLiteralRadioBtn = new javax.swing.JRadioButton();
        inputUseEncodedRadioBtn = new javax.swing.JRadioButton();
        inputFileTypeLabel = new javax.swing.JLabel();
        inputFileTypeComboBox = new javax.swing.JComboBox();
        inputDetailsBtn = new javax.swing.JButton();
        wAdvancePanel = new javax.swing.JPanel();
        writePropertiesAdvLabel = new javax.swing.JLabel();
        outputUseLabel = new javax.swing.JLabel();
        outputEncodingStyleLabel = new javax.swing.JLabel();
        outputEncodingStyleTextField = new javax.swing.JTextField();
        outputUseLiteralRadioBtn = new javax.swing.JRadioButton();
        outputUseEncodedRadioBtn = new javax.swing.JRadioButton();
        jSeparator3 = new javax.swing.JSeparator();
        outputFileTypeComboBox = new javax.swing.JComboBox();
        outputFileTypeLabel = new javax.swing.JLabel();
        outputDetailsBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        scrollTabPanel = new javax.swing.JPanel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();
        mDoc = descriptionTextPane.getStyledDocument();
        mStyles = new String[]{"bold", "regular"};
        Style def = StyleContext.getDefaultStyleContext().
        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = mDoc.addStyle("regular", def);
        Style s = mDoc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        directoryFileChooser.setName("directoryFileChooser"); // NOI18N

        mReadWriteBtnGrp.add(readRBtn);
        readRBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.readRBtn.text")); // NOI18N
        readRBtn.setMargin(new java.awt.Insets(0, 0, 2, 2));
        readRBtn.setName("readRBtn"); // NOI18N
        readRBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readRBtnActionPerformed(evt);
            }
        });

        mReadWriteBtnGrp.add(writeRBtn);
        writeRBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.writeRBtn.text")); // NOI18N
        writeRBtn.setMargin(new java.awt.Insets(0, 2, 2, 2));
        writeRBtn.setName("writeRBtn"); // NOI18N
        writeRBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeRBtnActionPerformed(evt);
            }
        });

        mReadWriteBtnGrp.add(readWriteRBtn);
        readWriteRBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.readWriteRBtn.text")); // NOI18N
        readWriteRBtn.setMargin(new java.awt.Insets(0, 2, 2, 2));
        readWriteRBtn.setName("readWriteRBtn"); // NOI18N
        readWriteRBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readWriteRBtnActionPerformed(evt);
            }
        });

        inputAdvMessageTabPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputAdvMessageTabPanel.setName("inputAdvMessageTabPanel"); // NOI18N
        inputAdvMessageTabPanel.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        inputScrollAdvPanel.setName("inputScrollAdvPanel"); // NOI18N
        inputScrollAdvPanel.setLayout(new java.awt.BorderLayout());

        inputAdvScrollPane.setMinimumSize(new java.awt.Dimension(300, 50));
        inputAdvScrollPane.setName("inputAdvScrollPane"); // NOI18N

        inputAdvDescriptionArea.setBackground(inputAdvScrollPane.getBackground());
        inputAdvDescriptionArea.setEditable(false);
        inputAdvDescriptionArea.setName("inputAdvDescriptionArea"); // NOI18N
        inputAdvDescriptionArea.setPreferredSize(new java.awt.Dimension(300, 50));
        inputAdvScrollPane.setViewportView(inputAdvDescriptionArea);

        inputScrollAdvPanel.add(inputAdvScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setBottomComponent(inputScrollAdvPanel);

        inputMessageTabPanelTop.setName("inputMessageTabPanelTop"); // NOI18N
        inputMessageTabPanelTop.setLayout(new java.awt.BorderLayout());

        inputMessageTabPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createEmptyBorder(1, 1, 20, 1)));
        inputMessageTabPanel.setName("inputMessageTabPanel"); // NOI18N
        inputMessageTabPanel.setLayout(new java.awt.GridBagLayout());

        inputRecordDelimiterLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputRecordDelimiterLabel.text")); // NOI18N
        inputRecordDelimiterLabel.setName("inputRecordDelimiterLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        inputMessageTabPanel.add(inputRecordDelimiterLabel, gridBagConstraints);

        inputPartLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputPartLabel.text")); // NOI18N
        inputPartLabel.setName("inputPartLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(inputPartLabel, gridBagConstraints);

        inputMaxBytesPerReLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputMaxBytesPerReLabel.text")); // NOI18N
        inputMaxBytesPerReLabel.setName("inputMaxBytesPerReLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(inputMaxBytesPerReLabel, gridBagConstraints);

        inputPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputPartComboBox.setName("inputPartComboBox"); // NOI18N
        inputPartComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        inputMessageTabPanel.add(inputPartComboBox, gridBagConstraints);

        inputArchDirectoryLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputArchDirectoryLabel.text")); // NOI18N
        inputArchDirectoryLabel.setName("inputArchDirectoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 10, 0);
        inputMessageTabPanel.add(inputArchDirectoryLabel, gridBagConstraints);

        inputArchiveDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputArchiveDirectoryTextField.text")); // NOI18N
        inputArchiveDirectoryTextField.setName("inputArchiveDirectoryTextField"); // NOI18N
        inputArchiveDirectoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputArchiveDirectoryTextFieldActionPerformed(evt);
            }
        });
        inputArchiveDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 75, 0, 0);
        inputMessageTabPanel.add(inputArchiveDirectoryTextField, gridBagConstraints);

        inputArchiveButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputArchiveButton.text")); // NOI18N
        inputArchiveButton.setName("inputArchiveButton"); // NOI18N
        inputArchiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputArchiveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        inputMessageTabPanel.add(inputArchiveButton, gridBagConstraints);

        inputFixedLengthBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputFixedLengthBox.text")); // NOI18N
        inputFixedLengthBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputFixedLengthBox.setName("inputFixedLengthBox"); // NOI18N
        inputFixedLengthBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputFixedLengthBoxItemStateChanged(evt);
            }
        });
        inputFixedLengthBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        inputMessageTabPanel.add(inputFixedLengthBox, gridBagConstraints);

        inputRemoveEOLBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputRemoveEOLBox.text")); // NOI18N
        inputRemoveEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputRemoveEOLBox.setName("inputRemoveEOLBox"); // NOI18N
        inputRemoveEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(inputRemoveEOLBox, gridBagConstraints);

        inputMultiRecordsPerFileBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputMultiRecordsPerFileBox.text")); // NOI18N
        inputMultiRecordsPerFileBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputMultiRecordsPerFileBox.setName("inputMultiRecordsPerFileBox"); // NOI18N
        inputMultiRecordsPerFileBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputMultiRecordsPerFileBoxItemStateChanged(evt);
            }
        });
        inputMultiRecordsPerFileBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        inputMessageTabPanel.add(inputMultiRecordsPerFileBox, gridBagConstraints);

        inputMaxBytesPerRecordSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        inputMaxBytesPerRecordSpinner.setName("inputMaxBytesPerRecordSpinner"); // NOI18N
        inputMaxBytesPerRecordSpinner.setRequestFocusEnabled(false);
        inputMaxBytesPerRecordSpinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        inputMessageTabPanel.add(inputMaxBytesPerRecordSpinner, gridBagConstraints);

        inputArchiveBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputArchiveBox.text")); // NOI18N
        inputArchiveBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputArchiveBox.setName("inputArchiveBox"); // NOI18N
        inputArchiveBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        inputArchiveBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        inputMessageTabPanel.add(inputArchiveBox, gridBagConstraints);

        inputArchiveRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputArchiveRelativeBox.text")); // NOI18N
        inputArchiveRelativeBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputArchiveRelativeBox.setName("inputArchiveRelativeBox"); // NOI18N
        inputArchiveRelativeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputArchiveRelativeBoxItemStateChanged(evt);
            }
        });
        inputArchiveRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(inputArchiveRelativeBox, gridBagConstraints);

        inputPostProcessingLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputPostProcessingLabel.text")); // NOI18N
        inputPostProcessingLabel.setName("inputPostProcessingLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(inputPostProcessingLabel, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel1.text_2")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        inputMessageTabPanel.add(jLabel1, gridBagConstraints);

        inputRecordDelimiterTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputRecordDelimiterTextField.text")); // NOI18N
        inputRecordDelimiterTextField.setName("inputRecordDelimiterTextField"); // NOI18N
        inputRecordDelimiterTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 100, 0, 10);
        inputMessageTabPanel.add(inputRecordDelimiterTextField, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel2.text_2")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        inputMessageTabPanel.add(jLabel2, gridBagConstraints);

        inputProtectDirectoryLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputProtectDirectoryLabel.text")); // NOI18N
        inputProtectDirectoryLabel.setName("inputProtectDirectoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
        inputMessageTabPanel.add(inputProtectDirectoryLabel, gridBagConstraints);

        inputProtectDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputProtectDirectoryTextField.text")); // NOI18N
        inputProtectDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        inputProtectDirectoryTextField.setName("inputProtectDirectoryTextField"); // NOI18N
        inputProtectDirectoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputProtectDirectoryTextFieldActionPerformed(evt);
            }
        });
        inputProtectDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 75, 10, 0);
        inputMessageTabPanel.add(inputProtectDirectoryTextField, gridBagConstraints);

        inputProtectRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputProtectRelativeBox.text")); // NOI18N
        inputProtectRelativeBox.setToolTipText(mBundle.getString("DESC_Attribute_protectDirIsRelative"));
        inputProtectRelativeBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputProtectRelativeBox.setName("inputProtectRelativeBox"); // NOI18N
        inputProtectRelativeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputProtectRelativeBoxActionPerformed(evt);
            }
        });
        inputProtectRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        inputMessageTabPanel.add(inputProtectRelativeBox, gridBagConstraints);

        inputProtectButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputProtectButton.text")); // NOI18N
        inputProtectButton.setName("inputProtectButton"); // NOI18N
        inputProtectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputProtectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        inputMessageTabPanel.add(inputProtectButton, gridBagConstraints);

        inputAddEOLBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputAddEOLBox.text")); // NOI18N
        inputAddEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputAddEOLBox.setName("inputAddEOLBox"); // NOI18N
        inputAddEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputAddEOLBoxparameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputAddEOLBoxparameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 100, 0, 0);
        inputMessageTabPanel.add(inputAddEOLBox, gridBagConstraints);

        inputStageBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputStageBox.text")); // NOI18N
        inputStageBox.setToolTipText(mBundle.getString("DESC_Attribute_stage"));
        inputStageBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputStageBox.setName("inputStageBox"); // NOI18N
        inputStageBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        inputStageBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        inputMessageTabPanel.add(inputStageBox, gridBagConstraints);

        inputStageDirectoryLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputStageDirectoryLabel.text")); // NOI18N
        inputStageDirectoryLabel.setName("inputStageDirectoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
        inputMessageTabPanel.add(inputStageDirectoryLabel, gridBagConstraints);

        inputStageDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputStageDirectoryTextField.text")); // NOI18N
        inputStageDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_stageDirectory"));
        inputStageDirectoryTextField.setName("inputStageDirectoryTextField"); // NOI18N
        inputStageDirectoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputStageDirectoryTextFieldActionPerformed(evt);
            }
        });
        inputStageDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 75, 10, 0);
        inputMessageTabPanel.add(inputStageDirectoryTextField, gridBagConstraints);

        inputStageRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputStageRelativeBox.text")); // NOI18N
        inputStageRelativeBox.setToolTipText(mBundle.getString("DESC_Attribute_stageDirIsRelative"));
        inputStageRelativeBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputStageRelativeBox.setName("inputStageRelativeBox"); // NOI18N
        inputStageRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        inputMessageTabPanel.add(inputStageRelativeBox, gridBagConstraints);

        inputStageButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputStageButton.text")); // NOI18N
        inputStageButton.setName("inputStageButton"); // NOI18N
        inputStageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputStageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        inputMessageTabPanel.add(inputStageButton, gridBagConstraints);

        inputProtectBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputProtectBox.text")); // NOI18N
        inputProtectBox.setToolTipText(mBundle.getString("DESC_Attribute_protect"));
        inputProtectBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputProtectBox.setName("inputProtectBox"); // NOI18N
        inputProtectBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        inputProtectBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        inputMessageTabPanel.add(inputProtectBox, gridBagConstraints);

        inputMessageTabPanelTop.add(inputMessageTabPanel, java.awt.BorderLayout.CENTER);

        jSplitPane1.setTopComponent(inputMessageTabPanelTop);

        inputAdvMessageTabPanel.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        outputAdvMessageTabPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        outputAdvMessageTabPanel.setName("outputAdvMessageTabPanel"); // NOI18N
        outputAdvMessageTabPanel.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        outputMessageTabPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        outputMessageTabPanel.setName("outputMessageTabPanel"); // NOI18N

        outputReocrdDelimLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputReocrdDelimLabel.text")); // NOI18N
        outputReocrdDelimLabel.setToolTipText(mBundle.getString("DESC_Attribute_recordDelimiter"));
        outputReocrdDelimLabel.setName("outputReocrdDelimLabel"); // NOI18N

        outputPartLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputPartLabel.text")); // NOI18N
        outputPartLabel.setToolTipText(mBundle.getString("DESC_Attribute_part"));
        outputPartLabel.setName("outputPartLabel"); // NOI18N

        outputMaxBytesPerReLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputMaxBytesPerReLabel.text")); // NOI18N
        outputMaxBytesPerReLabel.setToolTipText(mBundle.getString("DESC_Attribute_maxBytesPerRecord"));
        outputMaxBytesPerReLabel.setName("outputMaxBytesPerReLabel"); // NOI18N

        outputRecordDelimiterTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputRecordDelimiterTextField.text")); // NOI18N
        outputRecordDelimiterTextField.setToolTipText(mBundle.getString("DESC_Attribute_recordDelimiter"));
        outputRecordDelimiterTextField.setName("outputRecordDelimiterTextField"); // NOI18N
        outputRecordDelimiterTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputPartComboBox.setToolTipText(mBundle.getString("DESC_Attribute_part"));
        outputPartComboBox.setName("outputPartComboBox"); // NOI18N
        outputPartComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputProtectDirLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputProtectDirLabel.text")); // NOI18N
        outputProtectDirLabel.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        outputProtectDirLabel.setName("outputProtectDirLabel"); // NOI18N

        outputProtectDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputProtectDirectoryTextField.text")); // NOI18N
        outputProtectDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        outputProtectDirectoryTextField.setName("outputProtectDirectoryTextField"); // NOI18N
        outputProtectDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputProtectButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputProtectButton.text")); // NOI18N
        outputProtectButton.setName("outputProtectButton"); // NOI18N
        outputProtectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputProtectButtonActionPerformed(evt);
            }
        });

        outputStageDirLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputStageDirLabel.text")); // NOI18N
        outputStageDirLabel.setToolTipText(mBundle.getString("DESC_Attribute_stageDirectory"));
        outputStageDirLabel.setName("outputStageDirLabel"); // NOI18N

        outputStageDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputStageDirectoryTextField.text")); // NOI18N
        outputStageDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_stageDirectory"));
        outputStageDirectoryTextField.setName("outputStageDirectoryTextField"); // NOI18N
        outputStageDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputStageButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputStageButton.text")); // NOI18N
        outputStageButton.setName("outputStageButton"); // NOI18N
        outputStageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputStageButtonActionPerformed(evt);
            }
        });

        outputMaxBytesPerRecordSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        outputMaxBytesPerRecordSpinner.setName("outputMaxBytesPerRecordSpinner"); // NOI18N
        outputMaxBytesPerRecordSpinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputAddEOLBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputAddEOLBox.text")); // NOI18N
        outputAddEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputAddEOLBox.setName("outputAddEOLBox"); // NOI18N
        outputAddEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputMultiRecordsPerFileBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputMultiRecordsPerFileBox.text")); // NOI18N
        outputMultiRecordsPerFileBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputMultiRecordsPerFileBox.setName("outputMultiRecordsPerFileBox"); // NOI18N
        outputMultiRecordsPerFileBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputMultiRecordsPerFileBoxActionPerformed(evt);
            }
        });
        outputMultiRecordsPerFileBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputFixedLengthBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputFixedLengthBox.text")); // NOI18N
        outputFixedLengthBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputFixedLengthBox.setName("outputFixedLengthBox"); // NOI18N

        outputStageIsRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputStageIsRelativeBox.text")); // NOI18N
        outputStageIsRelativeBox.setName("outputStageIsRelativeBox"); // NOI18N
        outputStageIsRelativeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputStageIsRelativeBoxItemStateChanged(evt);
            }
        });
        outputStageIsRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputStageBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputStageBox.text")); // NOI18N
        outputStageBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputStageBox.setName("outputStageBox"); // NOI18N
        outputStageBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        outputStageBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputProtectIsRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputProtectIsRelativeBox.text")); // NOI18N
        outputProtectIsRelativeBox.setName("outputProtectIsRelativeBox"); // NOI18N
        outputProtectIsRelativeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputProtectIsRelativeBoxItemStateChanged(evt);
            }
        });
        outputProtectIsRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputProtectBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputProtectBox.text")); // NOI18N
        outputProtectBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputProtectBox.setName("outputProtectBox"); // NOI18N
        outputProtectBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        outputProtectBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputRecordLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputRecordLabel.text_2")); // NOI18N
        outputRecordLabel.setName("outputRecordLabel"); // NOI18N

        outputPostProcessingLab.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputPostProcessingLab.text")); // NOI18N
        outputPostProcessingLab.setToolTipText(mBundle.getString("DESC_Attribute_protect"));
        outputPostProcessingLab.setName("outputPostProcessingLab"); // NOI18N

        outputArchiveDirLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputArchiveDirLabel.text")); // NOI18N
        outputArchiveDirLabel.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        outputArchiveDirLabel.setName("outputArchiveDirLabel"); // NOI18N

        outputArchiveBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputArchiveBox.text")); // NOI18N
        outputArchiveBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputArchiveBox.setName("outputArchiveBox"); // NOI18N
        outputArchiveBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateArchiveProtectStageBoxItemStateChanged(evt);
            }
        });
        outputArchiveBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputArchiveDirectoryTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputArchiveDirectoryTextField.text")); // NOI18N
        outputArchiveDirectoryTextField.setToolTipText(mBundle.getString("DESC_Attribute_protectDirectory"));
        outputArchiveDirectoryTextField.setName("outputArchiveDirectoryTextField"); // NOI18N
        outputArchiveDirectoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputArchiveIsRelativeBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputArchiveIsRelativeBox.text")); // NOI18N
        outputArchiveIsRelativeBox.setName("outputArchiveIsRelativeBox"); // NOI18N
        outputArchiveIsRelativeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputArchiveIsRelativeBoxItemStateChanged(evt);
            }
        });
        outputArchiveIsRelativeBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        outputArchiveButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputArchiveButton.text")); // NOI18N
        outputArchiveButton.setName("outputArchiveButton"); // NOI18N
        outputArchiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputArchiveButtonActionPerformed(evt);
            }
        });

        outputRemoveEOLBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputRemoveEOLBox.text")); // NOI18N
        outputRemoveEOLBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputRemoveEOLBox.setName("outputRemoveEOLBox"); // NOI18N
        outputRemoveEOLBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout outputMessageTabPanelLayout = new org.jdesktop.layout.GroupLayout(outputMessageTabPanel);
        outputMessageTabPanel.setLayout(outputMessageTabPanelLayout);
        outputMessageTabPanelLayout.setHorizontalGroup(
            outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(outputPartLabel)
                    .add(outputRecordLabel)
                    .add(outputMaxBytesPerReLabel)
                    .add(outputPostProcessingLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(outputStageDirLabel)
                    .add(outputArchiveDirLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outputProtectDirLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(outputArchiveDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .add(outputProtectDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .add(outputStageDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, outputStageButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, outputProtectButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, outputArchiveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .add(134, 134, 134)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(outputArchiveBox)
                    .add(outputProtectBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 3, Short.MAX_VALUE)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(outputArchiveIsRelativeBox)
                    .add(outputProtectIsRelativeBox))
                .add(99, 99, 99))
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .add(134, 134, 134)
                .add(outputStageBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, Short.MAX_VALUE)
                .add(outputStageIsRelativeBox)
                .add(99, 99, 99))
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .add(134, 134, 134)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputMaxBytesPerRecordSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputPartComboBox, 0, 127, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputMultiRecordsPerFileBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                .add(18, 18, 18)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(outputMessageTabPanelLayout.createSequentialGroup()
                        .add(outputReocrdDelimLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(outputRecordDelimiterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                    .add(outputMessageTabPanelLayout.createSequentialGroup()
                        .add(outputAddEOLBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, Short.MAX_VALUE)
                        .add(outputRemoveEOLBox)))
                .addContainerGap(12, Short.MAX_VALUE))
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .add(134, 134, 134)
                .add(outputFixedLengthBox)
                .addContainerGap(223, Short.MAX_VALUE))
        );
        outputMessageTabPanelLayout.setVerticalGroup(
            outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputMessageTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputPartLabel)
                    .add(outputPartComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outputAddEOLBox)
                    .add(outputRemoveEOLBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputRecordDelimiterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outputRecordLabel)
                    .add(outputMultiRecordsPerFileBox)
                    .add(outputReocrdDelimLabel))
                .add(2, 2, 2)
                .add(outputFixedLengthBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputMaxBytesPerReLabel)
                    .add(outputMaxBytesPerRecordSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputArchiveIsRelativeBox)
                    .add(outputArchiveBox)
                    .add(outputPostProcessingLab))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputArchiveButton)
                    .add(outputArchiveDirLabel)
                    .add(outputArchiveDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputProtectIsRelativeBox)
                    .add(outputProtectBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputProtectButton)
                    .add(outputProtectDirLabel)
                    .add(outputProtectDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputStageIsRelativeBox)
                    .add(outputStageBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessageTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputStageDirLabel)
                    .add(outputStageButton)
                    .add(outputStageDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(31, 31, 31))
        );

        jSplitPane2.setTopComponent(outputMessageTabPanel);

        outputScrollAdvPanel.setName("outputScrollAdvPanel"); // NOI18N
        outputScrollAdvPanel.setLayout(new java.awt.BorderLayout());

        outputAdvScrollPane.setMinimumSize(new java.awt.Dimension(150, 100));
        outputAdvScrollPane.setName("outputAdvScrollPane"); // NOI18N

        outputAdvDescriptionArea.setBackground(inputAdvScrollPane.getBackground());
        outputAdvDescriptionArea.setEditable(false);
        outputAdvDescriptionArea.setName("outputAdvDescriptionArea"); // NOI18N
        outputAdvDescriptionArea.setPreferredSize(new java.awt.Dimension(300, 50));
        outputAdvScrollPane.setViewportView(outputAdvDescriptionArea);

        outputScrollAdvPanel.add(outputAdvScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane2.setBottomComponent(outputScrollAdvPanel);

        outputAdvMessageTabPanel.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setName("Configure File Binding Parameters"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setName("splitPane"); // NOI18N

        bindingConfigurationPanel.setName("bindingConfigurationPanel"); // NOI18N
        bindingConfigurationPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                bindingConfigurationPanelComponentShown(evt);
            }
        });

        generalTabPanel.setName("generalTabPanel"); // NOI18N
        generalTabPanel.setLayout(new java.awt.GridBagLayout());

        servicesGeneralPanel.setName("servicesGeneralPanel"); // NOI18N
        servicesGeneralPanel.setLayout(new java.awt.GridBagLayout());

        bindingNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.bindingNameLabel.text")); // NOI18N
        bindingNameLabel.setName("bindingNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        servicesGeneralPanel.add(bindingNameLabel, gridBagConstraints);

        bindingNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingNameComboBox.setName("bindingNameComboBox"); // NOI18N
        bindingNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bindingNameComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        servicesGeneralPanel.add(bindingNameComboBox, gridBagConstraints);

        portTypeLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.portTypeLabel.text_1")); // NOI18N
        portTypeLabel.setName("portTypeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        servicesGeneralPanel.add(portTypeLabel, gridBagConstraints);

        portTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        portTypeComboBox.setName("portTypeComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        servicesGeneralPanel.add(portTypeComboBox, gridBagConstraints);

        portNameLabel.setLabelFor(servicePortComboBox);
        portNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.portNameLabel.text_1")); // NOI18N
        portNameLabel.setName("portNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 5);
        servicesGeneralPanel.add(portNameLabel, gridBagConstraints);

        servicePortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        servicePortComboBox.setName("servicePortComboBox"); // NOI18N
        servicePortComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                servicePortComboBoxItemStateChanged(evt);
            }
        });
        servicePortComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                servicePortComboBoxFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        servicesGeneralPanel.add(servicePortComboBox, gridBagConstraints);

        servicesGeneralLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        servicesGeneralLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.servicesGeneralLabel.text_2")); // NOI18N
        servicesGeneralLabel.setName("servicesGeneralLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        servicesGeneralPanel.add(servicesGeneralLabel, gridBagConstraints);

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 65, 0, 10);
        servicesGeneralPanel.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        generalTabPanel.add(servicesGeneralPanel, gridBagConstraints);

        rwGeneralPanel.setName("rwGeneralPanel"); // NOI18N
        rwGeneralPanel.setLayout(new java.awt.GridBagLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel8.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        rwGeneralPanel.add(jLabel8, gridBagConstraints);

        directoryTextField.setName("directoryTextField"); // NOI18N
        directoryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryTextFieldActionPerformed(evt);
            }
        });
        directoryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rwGeneralPanel.add(directoryTextField, gridBagConstraints);

        directoryPropertiesSep.setName("directoryPropertiesSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 10);
        rwGeneralPanel.add(directoryPropertiesSep, gridBagConstraints);

        directoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        directoryLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.directoryLabel.text_1")); // NOI18N
        directoryLabel.setName("directoryLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 10);
        rwGeneralPanel.add(directoryLabel, gridBagConstraints);

        directoryLabImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        directoryLabImage.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.directoryLabImage.text")); // NOI18N
        directoryLabImage.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.CASA_EDITED")); // NOI18N
        directoryLabImage.setName("directoryLabImage"); // NOI18N
        directoryLabImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                directoryLabImageMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rwGeneralPanel.add(directoryLabImage, gridBagConstraints);

        browseButton.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.browseButton.text_1")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        rwGeneralPanel.add(browseButton, gridBagConstraints);

        serviceBindingOperationLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        serviceBindingOperationLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.serviceBindingOperationLabel.text")); // NOI18N
        serviceBindingOperationLabel.setName("serviceBindingOperationLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        rwGeneralPanel.add(serviceBindingOperationLabel, gridBagConstraints);

        operationNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.operationNameLabel.text")); // NOI18N
        operationNameLabel.setName("operationNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rwGeneralPanel.add(operationNameLabel, gridBagConstraints);

        readPropertiesGeneralLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        readPropertiesGeneralLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.readPropertiesGeneralLabel.text")); // NOI18N
        readPropertiesGeneralLabel.setName("readPropertiesGeneralLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        rwGeneralPanel.add(readPropertiesGeneralLabel, gridBagConstraints);

        inputFileNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputFileNameLabel.text")); // NOI18N
        inputFileNameLabel.setName("inputFileNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rwGeneralPanel.add(inputFileNameLabel, gridBagConstraints);

        writePropertiesGeneralLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        writePropertiesGeneralLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.writePropertiesGeneralLabel.text")); // NOI18N
        writePropertiesGeneralLabel.setName("writePropertiesGeneralLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        rwGeneralPanel.add(writePropertiesGeneralLabel, gridBagConstraints);

        outputFileNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputFileNameLabel.text")); // NOI18N
        outputFileNameLabel.setToolTipText(mBundle.getString("DESC_Attribute_fileName"));
        outputFileNameLabel.setName("outputFileNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rwGeneralPanel.add(outputFileNameLabel, gridBagConstraints);

        outputFileNameTextField.setToolTipText(mBundle.getString("DESC_Attribute_fileName"));
        outputFileNameTextField.setName("outputFileNameTextField"); // NOI18N
        outputFileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rwGeneralPanel.add(outputFileNameTextField, gridBagConstraints);

        sep3General.setName("sep3General"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 110, 0, 10);
        rwGeneralPanel.add(sep3General, gridBagConstraints);

        inputFileNameTextField.setName("inputFileNameTextField"); // NOI18N
        inputFileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rwGeneralPanel.add(inputFileNameTextField, gridBagConstraints);

        operationNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        operationNameComboBox.setName("operationNameComboBox"); // NOI18N
        operationNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                operationNameComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rwGeneralPanel.add(operationNameComboBox, gridBagConstraints);

        sep1General.setName("sep1General"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 50, 0, 10);
        rwGeneralPanel.add(sep1General, gridBagConstraints);

        inputFileIsPatternBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputFileIsPatternBox.text")); // NOI18N
        inputFileIsPatternBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputFileIsPatternBox.setName("inputFileIsPatternBox"); // NOI18N
        inputFileIsPatternBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputFileIsPatternBoxActionPerformed(evt);
            }
        });
        inputFileIsPatternBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        rwGeneralPanel.add(inputFileIsPatternBox, gridBagConstraints);

        outputFileIsPatternBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputFileIsPatternBox.text")); // NOI18N
        outputFileIsPatternBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputFileIsPatternBox.setName("outputFileIsPatternBox"); // NOI18N
        outputFileIsPatternBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileIsPatternBoxActionPerformed(evt);
            }
        });
        outputFileIsPatternBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        rwGeneralPanel.add(outputFileIsPatternBox, gridBagConstraints);

        sep2General.setName("sep2General"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 110, 0, 10);
        rwGeneralPanel.add(sep2General, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        generalTabPanel.add(rwGeneralPanel, gridBagConstraints);

        bindingConfigurationPanel.addTab(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.generalTabPanel.TabConstraints.tabTitle"), generalTabPanel); // NOI18N

        advanceTabPanel.setName("advanceTabPanel"); // NOI18N
        advanceTabPanel.setLayout(new java.awt.GridBagLayout());

        servicesAdvancePanel.setName("servicesAdvancePanel"); // NOI18N
        servicesAdvancePanel.setLayout(new java.awt.GridBagLayout());

        servicesAdvancedLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        servicesAdvancedLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.servicesAdvancedLabel.text_2")); // NOI18N
        servicesAdvancedLabel.setName("servicesAdvancedLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        servicesAdvancePanel.add(servicesAdvancedLabel, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        jLabel4.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel4.toolTipText")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        servicesAdvancePanel.add(jLabel4, gridBagConstraints);

        pathRelativeToComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pathRelativeToComboBox.setName("pathRelativeToComboBox"); // NOI18N
        pathRelativeToComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                pathRelativeToComboBoxItemStateChanged(evt);
            }
        });
        pathRelativeToComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        servicesAdvancePanel.add(pathRelativeToComboBox, gridBagConstraints);

        sep1Advanced.setName("sep1Advanced"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 65, 0, 0);
        servicesAdvancePanel.add(sep1Advanced, gridBagConstraints);

        pathRelativeToLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.pathRelativeToLabel.text_2")); // NOI18N
        pathRelativeToLabel.setName("pathRelativeToLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        servicesAdvancePanel.add(pathRelativeToLabel, gridBagConstraints);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel5.toolTipText")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        servicesAdvancePanel.add(jLabel5, gridBagConstraints);

        lockNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.lockNameLabel.text")); // NOI18N
        lockNameLabel.setName("lockNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        servicesAdvancePanel.add(lockNameLabel, gridBagConstraints);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel6.toolTipText")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        servicesAdvancePanel.add(jLabel6, gridBagConstraints);

        workAreaLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.workAreaLabel.text")); // NOI18N
        workAreaLabel.setName("workAreaLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        servicesAdvancePanel.add(workAreaLabel, gridBagConstraints);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        jLabel7.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel7.toolTipText")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        servicesAdvancePanel.add(jLabel7, gridBagConstraints);

        seqNameLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.seqNameLabel.text")); // NOI18N
        seqNameLabel.setName("seqNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 10);
        servicesAdvancePanel.add(seqNameLabel, gridBagConstraints);

        seqNameTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.seqNameTextField.text")); // NOI18N
        seqNameTextField.setName("seqNameTextField"); // NOI18N
        seqNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        servicesAdvancePanel.add(seqNameTextField, gridBagConstraints);

        workAreaTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.workAreaTextField.text")); // NOI18N
        workAreaTextField.setName("workAreaTextField"); // NOI18N
        workAreaTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        servicesAdvancePanel.add(workAreaTextField, gridBagConstraints);

        lockNameTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.lockNameTextField.text")); // NOI18N
        lockNameTextField.setName("lockNameTextField"); // NOI18N
        lockNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        servicesAdvancePanel.add(lockNameTextField, gridBagConstraints);

        isRelPathPanel.setName("isRelPathPanel"); // NOI18N
        isRelPathPanel.setLayout(new java.awt.GridBagLayout());

        isRelativePathCasaLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/file/resources/service_composition_16.png"))); // NOI18N
        isRelativePathCasaLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.isRelativePathCasaLabel.text")); // NOI18N
        isRelativePathCasaLabel.setToolTipText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.CASA_EDITED")); // NOI18N
        isRelativePathCasaLabel.setName("isRelativePathCasaLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        isRelPathPanel.add(isRelativePathCasaLabel, gridBagConstraints);

        isRelativePathBox.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.isRelativePathBox.text")); // NOI18N
        isRelativePathBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        isRelativePathBox.setName("isRelativePathBox"); // NOI18N
        isRelativePathBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isRelativePathBoxActionPerformed(evt);
            }
        });
        isRelativePathBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 0);
        isRelPathPanel.add(isRelativePathBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        servicesAdvancePanel.add(isRelPathPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        advanceTabPanel.add(servicesAdvancePanel, gridBagConstraints);

        directionAdvancePanel.setName("directionAdvancePanel"); // NOI18N
        directionAdvancePanel.setLayout(new java.awt.GridBagLayout());

        directionLab.setFont(new java.awt.Font("Tahoma", 1, 11));
        directionLab.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.directionLab.text")); // NOI18N
        directionLab.setName("directionLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        directionAdvancePanel.add(directionLab, gridBagConstraints);

        directionSep.setName("directionSep"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 70, 0, 0);
        directionAdvancePanel.add(directionSep, gridBagConstraints);

        modeLab.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.modeLab.text")); // NOI18N
        modeLab.setName("modeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        directionAdvancePanel.add(modeLab, gridBagConstraints);

        directionModeBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        directionModeBox.setName("directionModeBox"); // NOI18N
        directionModeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                directionModeBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        directionAdvancePanel.add(directionModeBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        advanceTabPanel.add(directionAdvancePanel, gridBagConstraints);

        rAdvancePanel.setName("rAdvancePanel"); // NOI18N
        rAdvancePanel.setLayout(new java.awt.GridBagLayout());

        readPropertiesAdvLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        readPropertiesAdvLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.readPropertiesAdvLabel.text")); // NOI18N
        readPropertiesAdvLabel.setName("readPropertiesAdvLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        rAdvancePanel.add(readPropertiesAdvLabel, gridBagConstraints);

        jSeparator2.setName("jSeparator2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
        rAdvancePanel.add(jSeparator2, gridBagConstraints);

        inputUseLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputUseLabel.text")); // NOI18N
        inputUseLabel.setName("inputUseLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rAdvancePanel.add(inputUseLabel, gridBagConstraints);

        inputEncodingStyleLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputEncodingStyleLabel.text")); // NOI18N
        inputEncodingStyleLabel.setName("inputEncodingStyleLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rAdvancePanel.add(inputEncodingStyleLabel, gridBagConstraints);

        inputPollingIntervalLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputPollingIntervalLabel.text")); // NOI18N
        inputPollingIntervalLabel.setName("inputPollingIntervalLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rAdvancePanel.add(inputPollingIntervalLabel, gridBagConstraints);

        inputPollingIntervalSpinner.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        inputPollingIntervalSpinner.setName("inputPollingIntervalSpinner"); // NOI18N
        inputPollingIntervalSpinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rAdvancePanel.add(inputPollingIntervalSpinner, gridBagConstraints);

        inputEncodingStyleTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputEncodingStyleTextField.text")); // NOI18N
        inputEncodingStyleTextField.setName("inputEncodingStyleTextField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rAdvancePanel.add(inputEncodingStyleTextField, gridBagConstraints);

        mInputUseBtnGrp.add(inputUseLiteralRadioBtn);
        inputUseLiteralRadioBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputUseLiteralRadioBtn.text")); // NOI18N
        inputUseLiteralRadioBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        inputUseLiteralRadioBtn.setName("inputUseLiteralRadioBtn"); // NOI18N
        inputUseLiteralRadioBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputUseLiteralRadioBtnItemStateChanged(evt);
            }
        });
        inputUseLiteralRadioBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputUseLiteralRadioBtnFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputUseLiteralRadioBtnFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rAdvancePanel.add(inputUseLiteralRadioBtn, gridBagConstraints);

        mInputUseBtnGrp.add(inputUseEncodedRadioBtn);
        inputUseEncodedRadioBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputUseEncodedRadioBtn.text")); // NOI18N
        inputUseEncodedRadioBtn.setName("inputUseEncodedRadioBtn"); // NOI18N
        inputUseEncodedRadioBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputUseLiteralRadioBtnItemStateChanged(evt);
            }
        });
        inputUseEncodedRadioBtn.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputUseEncodedRadioBtnFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                inputUseEncodedRadioBtnFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 50, 0, 0);
        rAdvancePanel.add(inputUseEncodedRadioBtn, gridBagConstraints);

        inputFileTypeLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputFileTypeLabel.text")); // NOI18N
        inputFileTypeLabel.setName("inputFileTypeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        rAdvancePanel.add(inputFileTypeLabel, gridBagConstraints);

        inputFileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputFileTypeComboBox.setName("inputFileTypeComboBox"); // NOI18N
        inputFileTypeComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 75, 0, 0);
        rAdvancePanel.add(inputFileTypeComboBox, gridBagConstraints);

        inputDetailsBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.inputDetailsBtn.text")); // NOI18N
        inputDetailsBtn.setName("inputDetailsBtn"); // NOI18N
        inputDetailsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputDetailsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        rAdvancePanel.add(inputDetailsBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 10);
        advanceTabPanel.add(rAdvancePanel, gridBagConstraints);

        wAdvancePanel.setName("wAdvancePanel"); // NOI18N
        wAdvancePanel.setLayout(new java.awt.GridBagLayout());

        writePropertiesAdvLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        writePropertiesAdvLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.writePropertiesAdvLabel.text")); // NOI18N
        writePropertiesAdvLabel.setName("writePropertiesAdvLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        wAdvancePanel.add(writePropertiesAdvLabel, gridBagConstraints);

        outputUseLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputUseLabel.text")); // NOI18N
        outputUseLabel.setName("outputUseLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        wAdvancePanel.add(outputUseLabel, gridBagConstraints);

        outputEncodingStyleLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputEncodingStyleLabel.text")); // NOI18N
        outputEncodingStyleLabel.setName("outputEncodingStyleLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        wAdvancePanel.add(outputEncodingStyleLabel, gridBagConstraints);

        outputEncodingStyleTextField.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputEncodingStyleTextField.text")); // NOI18N
        outputEncodingStyleTextField.setName("outputEncodingStyleTextField"); // NOI18N
        outputEncodingStyleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputEncodingStyleTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        wAdvancePanel.add(outputEncodingStyleTextField, gridBagConstraints);

        mOutputUseBtnGrp.add(outputUseLiteralRadioBtn);
        outputUseLiteralRadioBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputUseLiteralRadioBtn.text")); // NOI18N
        outputUseLiteralRadioBtn.setMargin(new java.awt.Insets(2, 0, 2, 2));
        outputUseLiteralRadioBtn.setName("outputUseLiteralRadioBtn"); // NOI18N
        outputUseLiteralRadioBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputUseLiteralRadioBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        wAdvancePanel.add(outputUseLiteralRadioBtn, gridBagConstraints);

        mOutputUseBtnGrp.add(outputUseEncodedRadioBtn);
        outputUseEncodedRadioBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputUseEncodedRadioBtn.text")); // NOI18N
        outputUseEncodedRadioBtn.setName("outputUseEncodedRadioBtn"); // NOI18N
        outputUseEncodedRadioBtn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputUseLiteralRadioBtnItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 50, 0, 0);
        wAdvancePanel.add(outputUseEncodedRadioBtn, gridBagConstraints);

        jSeparator3.setName("jSeparator3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
        wAdvancePanel.add(jSeparator3, gridBagConstraints);

        outputFileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputFileTypeComboBox.setName("outputFileTypeComboBox"); // NOI18N
        outputFileTypeComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 75, 0, 0);
        wAdvancePanel.add(outputFileTypeComboBox, gridBagConstraints);

        outputFileTypeLabel.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputFileTypeLabel.text")); // NOI18N
        outputFileTypeLabel.setToolTipText(mBundle.getString("DESC_Attribute_fileType"));
        outputFileTypeLabel.setName("outputFileTypeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        wAdvancePanel.add(outputFileTypeLabel, gridBagConstraints);

        outputDetailsBtn.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.outputDetailsBtn.text")); // NOI18N
        outputDetailsBtn.setName("outputDetailsBtn"); // NOI18N
        outputDetailsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputDetailsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        wAdvancePanel.add(outputDetailsBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 10, 10);
        advanceTabPanel.add(wAdvancePanel, gridBagConstraints);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        advanceTabPanel.add(jPanel1, gridBagConstraints);

        bindingConfigurationPanel.addTab(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "FileBindingConfigurationPanel.advanceTabPanel.TabConstraints.tabTitle"), advanceTabPanel); // NOI18N

        splitPane.setTopComponent(bindingConfigurationPanel);

        scrollTabPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        scrollTabPanel.setName("scrollTabPanel"); // NOI18N
        scrollTabPanel.setLayout(new java.awt.BorderLayout());

        descriptionScrollPane.setMinimumSize(new java.awt.Dimension(200, 50));
        descriptionScrollPane.setName("descriptionScrollPane"); // NOI18N

        descriptionTextPane.setBackground(scrollTabPanel.getBackground());
        descriptionTextPane.setEditable(false);
        descriptionTextPane.setName("descriptionTextPane"); // NOI18N
        descriptionScrollPane.setViewportView(descriptionTextPane);

        scrollTabPanel.add(descriptionScrollPane, java.awt.BorderLayout.CENTER);

        splitPane.setBottomComponent(scrollTabPanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
private void outputProtectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputProtectButtonActionPerformed
// TODO add your handling code here:
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = directoryFileChooser.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                outputProtectDirectoryTextField.setText(directoryFileChooser.
                        getSelectedFile().getAbsolutePath());
                validateFilePath(outputProtectDirectoryTextField.getText());
            }
        }
    });
}//GEN-LAST:event_outputProtectButtonActionPerformed
private void outputStageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputStageButtonActionPerformed
// TODO add your handling code here:
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = directoryFileChooser.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                outputStageDirectoryTextField.setText(directoryFileChooser.
                        getSelectedFile().getAbsolutePath());
                validateFilePath(outputStageDirectoryTextField.getText());
            }
        }
    });
}//GEN-LAST:event_outputStageButtonActionPerformed
private void inputArchiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputArchiveButtonActionPerformed
// TODO add your handling code here:
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = directoryFileChooser.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                inputArchiveDirectoryTextField.setText(directoryFileChooser.
                        getSelectedFile().getAbsolutePath());
                validateFilePath(inputArchiveDirectoryTextField.getText());
            }
        }
    });
}//GEN-LAST:event_inputArchiveButtonActionPerformed
private void inputArchiveDirectoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputArchiveDirectoryTextFieldActionPerformed
// TODO add your handling code here:
    validateFilePath(inputArchiveDirectoryTextField.getText());
}//GEN-LAST:event_inputArchiveDirectoryTextFieldActionPerformed
private void parameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_parameterFocusGained

private void parameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterFocusLost
    // TODO add your handling code here:
//    clearDescriptionArea();
}//GEN-LAST:event_parameterFocusLost

private void bindingConfigurationPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_bindingConfigurationPanelComponentShown
    SwingUtilities.getWindowAncestor(this).pack();
}//GEN-LAST:event_bindingConfigurationPanelComponentShown
private void directoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
//    validateFilePath(getDirectoryTextField().getText());
    if ((directoryTextField.getText() == null) ||
            directoryTextField.getText().equals("")) {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT, null,
                NbBundle.getMessage(FileBindingConfigurationPanel.class,
                "FileBindingConfiguratonnPanel.FILEDIR_EMPTY"));
    } else {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }
}
private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
//    FileNameStepWizardAction action = (FileNameStepWizardAction) SystemAction.
//            get(FileNameStepWizardAction.class);
//    action.performAction(mQName, mComponent);
//    if (false) {
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            int retVal = directoryFileChooser.showDialog(null, "Select");
            if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                getDirectoryTextField().setText(directoryFileChooser.
                        getSelectedFile().getAbsolutePath());
                // per BC developer, no need to validate directory
                //validateFilePath(getDirectoryTextField().getText());
            }
        }
    });
//    }
}//GEN-LAST:event_browseButtonActionPerformed
private void operationNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_operationNameComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedOperation = (String) operationNameComboBox.
                getSelectedItem();
        if (mComponent != null)  {
            Binding binding = null;
            if (mComponent instanceof FileAddress) {
                Port port = (Port) ((FileAddress) mComponent).getParent();
                binding = port.getBinding().get();
            } else if (mComponent instanceof FileBinding) {
                binding = (Binding) ((FileBinding) mComponent).getParent();
            } else if (mComponent instanceof FileMessage) {
                Object obj = ((FileMessage)mComponent).getParent();
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    binding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation)
                            ((BindingOutput) obj).getParent();
                    binding = (Binding) parentOp.getParent();
                }
            } else if (mComponent instanceof FileOperation) {
                Object obj = ((FileOperation)mComponent).getParent();
                if (obj instanceof BindingOperation) {
                    binding = (Binding) ((BindingOperation)obj).getParent();
                }
            }
            if (binding != null) {
                FileMessage inputMessage = getInputFileMessage(binding,
                        selectedOperation);
                updateInputMessageView(inputMessage);
                FileMessage outputMessage = getOutputFileMessage(binding,
                        selectedOperation);
                updateOutputMessageView(outputMessage);
                if (outputMessage == null) {
                    updateOutputMessageViewFromInput(inputMessage);
                }
            }
        }
    }
}//GEN-LAST:event_operationNameComboBoxItemStateChanged
private void inputFileIsPatternBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputFileIsPatternBoxActionPerformed
    // TODO add your handling code here:
    if (!isInputFileValid()) {
        // just a warning and should not prevent from proceeding
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_NORMAL_MESSAGE_EVT, null, NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel.FileNameMissing"));
    } else if (isOutputFileValid()) {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }
}//GEN-LAST:event_inputFileIsPatternBoxActionPerformed
private void outputFileIsPatternBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileIsPatternBoxActionPerformed
    // TODO add your handling code here:
    if (!isOutputFileValid()) {
        // just a warning and should not prevent from proceeding
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_NORMAL_MESSAGE_EVT, null, NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel.FileNameMissing"));
    } else if (isInputFileValid()) {
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }
}//GEN-LAST:event_outputFileIsPatternBoxActionPerformed
    private void inputArchiveRelativeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputArchiveRelativeBoxItemStateChanged
        // TODO add your handling code here:
        if (!isDirectoryRelative(inputArchiveRelativeBox)) {
            // make sure directory is filled in and must be relative
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.inputArchiveDirectoryMustBeRelative.text"));
        } else if (isDirectoryRelative(outputProtectIsRelativeBox)&&
                isDirectoryRelative(outputStageIsRelativeBox) &&
                isDirectoryRelative(inputProtectRelativeBox)&&
                isDirectoryRelative(inputStageRelativeBox) &&
                isDirectoryRelative(inputArchiveRelativeBox)) {
            firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
    }//GEN-LAST:event_inputArchiveRelativeBoxItemStateChanged
    private void outputProtectIsRelativeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputProtectIsRelativeBoxItemStateChanged
        // TODO add your handling code here:
        if (!isDirectoryRelative(outputProtectIsRelativeBox)) {
            // make sure directory is filled in and must be relative
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.outputProtectDirectoryMustBeRelative.text"));
        } else if (isDirectoryRelative(outputArchiveIsRelativeBox)&&
                isDirectoryRelative(outputStageIsRelativeBox) &&
                isDirectoryRelative(inputProtectRelativeBox)&&
                isDirectoryRelative(inputStageRelativeBox) &&
                isDirectoryRelative(inputArchiveRelativeBox)) {
            firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
    }//GEN-LAST:event_outputProtectIsRelativeBoxItemStateChanged
    private void outputStageIsRelativeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputStageIsRelativeBoxItemStateChanged
        // TODO add your handling code here:
        if (!isDirectoryRelative(outputStageIsRelativeBox)) {
            // make sure directory is filled in and must be relative
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.inputStageDirectoryMustBeRelative.text"));
        } else if (isDirectoryRelative(outputArchiveIsRelativeBox)&&
                isDirectoryRelative(outputProtectIsRelativeBox) &&
                isDirectoryRelative(inputProtectRelativeBox)&&
                isDirectoryRelative(inputStageRelativeBox) &&
                isDirectoryRelative(inputArchiveRelativeBox)) {
            firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
    }//GEN-LAST:event_outputStageIsRelativeBoxItemStateChanged
    private void updateArchiveProtectStageBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_updateArchiveProtectStageBoxItemStateChanged
        // TODO add your handling code here:
        updateArchiveProtectStage(evt.getSource());
    }//GEN-LAST:event_updateArchiveProtectStageBoxItemStateChanged
    private void inputMultiRecordsPerFileBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputMultiRecordsPerFileBoxItemStateChanged
        // TODO add your handling code here:
        if (inputMultiRecordsPerFileBox.isSelected()) {
            inputFixedLengthBox.setEnabled(true);
            inputRecordDelimiterLabel.setEnabled(true);
            inputRecordDelimiterTextField.setEnabled(true);
        } else {
            inputFixedLengthBox.setEnabled(false);
            inputRecordDelimiterLabel.setEnabled(false);
            inputRecordDelimiterTextField.setEnabled(false);
        }
    }//GEN-LAST:event_inputMultiRecordsPerFileBoxItemStateChanged
    private void inputFixedLengthBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputFixedLengthBoxItemStateChanged
        // TODO add your handling code here:

        // max bytes per record must have a non-zero value
        if (inputFixedLengthBox.isSelected()) {
            if (getInputMaxBytesPerRecord() == 0) {
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null,
                        NbBundle.getMessage(FileBindingConfigurationPanel.class,
                        "FileBindingConfigurationPanel.MAX_BYTES_MUST_BE_SPECIFIED"));
                return;
            }
        }
        firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null,
                    "");

    }//GEN-LAST:event_inputFixedLengthBoxItemStateChanged
    private void outputMultiRecordsPerFileBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputMultiRecordsPerFileBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_outputMultiRecordsPerFileBoxActionPerformed
    private void outputArchiveIsRelativeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputArchiveIsRelativeBoxItemStateChanged
        // TODO add your handling code here:
        if (!isDirectoryRelative(outputArchiveIsRelativeBox)) {
            // make sure directory is filled in and must be relative
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.outputArchiveDirectoryMustBeRelative.text"));
        } else if (isDirectoryRelative(outputStageIsRelativeBox)&&
                isDirectoryRelative(outputProtectIsRelativeBox) &&
                isDirectoryRelative(inputProtectRelativeBox)&&
                isDirectoryRelative(inputStageRelativeBox) &&
                isDirectoryRelative(inputArchiveRelativeBox)) {
            firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
}//GEN-LAST:event_outputArchiveIsRelativeBoxItemStateChanged
    private void outputArchiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputArchiveButtonActionPerformed
        // TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(null, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    outputArchiveDirectoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    validateFilePath(outputArchiveDirectoryTextField.getText());
                }
            }
        });
}//GEN-LAST:event_outputArchiveButtonActionPerformed
private void readWriteRBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readWriteRBtnActionPerformed
    // TODO add your handling code here:
    if (readWriteRBtn.isSelected()) {
        //        updateReadInfo(readWriteRBtn.isSelected());
        //        updateWriteInfo(readWriteRBtn.isSelected());
        updateReadWriteInfo();
    }
}//GEN-LAST:event_readWriteRBtnActionPerformed

private void writeRBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeRBtnActionPerformed
    // TODO add your handling code here:
    updateWriteInfo(writeRBtn.isSelected());
    //    if (writeRBtn.isSelected()) {
    //        updateReadInfo(false);
    //    }
}//GEN-LAST:event_writeRBtnActionPerformed

private void readRBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readRBtnActionPerformed
    // TODO add your handling code here:
    updateReadInfo(readRBtn.isSelected());

}//GEN-LAST:event_readRBtnActionPerformed

private void servicePortComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_servicePortComboBoxFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_servicePortComboBoxFocusGained

private void servicePortComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_servicePortComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        Object selObj = servicePortComboBox.getSelectedItem();
        String selBindingName = "";
        if (bindingNameComboBox.getSelectedItem() != null) {
            selBindingName = bindingNameComboBox.getSelectedItem().toString();
        }
        if ((selObj != null) && (mComponent != null)) {
            Port selServicePort = (Port) selObj;
            if (selServicePort.getBinding() != null) {
                Binding binding = selServicePort.getBinding().get();
                if ((binding != null) && (binding.getName().
                        equals(selBindingName))) {
                    Iterator<FileAddress> fileAddresses = selServicePort.
                            getExtensibilityElements(FileAddress.class).
                            iterator();
                    // 1 fileaddress for 1 binding
                    while (fileAddresses.hasNext()) {
                        updateServiceView(fileAddresses.next());
                        break;
                    }
                }
            }
        }
    }
}//GEN-LAST:event_servicePortComboBoxItemStateChanged

private void bindingNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bindingNameComboBoxItemStateChanged
// TODO add your handling code here:
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String selectedBinding = (String) bindingNameComboBox.
                getSelectedItem();
        // if binding name is changed, update the selected port type
        if (mComponent != null)  {
            if (mComponent instanceof FileAddress) {
                PortType portType = getPortType(selectedBinding,
                        (FileAddress) mComponent);
                if (portType != null)  {
                    portTypeComboBox.setSelectedItem(portType.getName());
                }
            } else if (mComponent instanceof FileBinding) {
                Binding parentBinding = (Binding)
                        ((FileBinding) mComponent).getParent();
                NamedComponentReference<PortType> portType =
                        parentBinding.getType();
                if ((portType != null) && (portType.get() != null)) {
                    portTypeComboBox.setSelectedItem(portType.get().getName());
                }
            } else if (mComponent instanceof FileMessage) {
                Object obj = ((FileMessage)mComponent).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation)
                            ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if ((parentBinding != null) &&
                        (parentBinding.getType() != null) &&
                        (parentBinding.getType().get() == null)) {
                    NamedComponentReference<PortType> portType =
                            parentBinding.getType();
                    if (parentBinding.getName().equals(selectedBinding)) {
                        portTypeComboBox.setSelectedItem(portType.get().getName());
                    }
                }
            } else if (mComponent instanceof FileOperation) {
                Object obj = ((FileOperation)mComponent).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<FileBinding> bindings = parentBinding.
                            getExtensibilityElements(FileBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFileBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(
                                parentBinding.getName());
                    }
                }
            }
        }
    }
}//GEN-LAST:event_bindingNameComboBoxItemStateChanged

private void inputUseLiteralRadioBtnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputUseLiteralRadioBtnItemStateChanged
    // TODO add your handling code here:
        updateEncodingInfo(evt.getSource());

}//GEN-LAST:event_inputUseLiteralRadioBtnItemStateChanged

private void inputDetailsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputDetailsBtnActionPerformed
    // if mode is read/write, then need to bring up right panel contents
    if (outputMessageTabPanel != null) {
        if (getReadWriteMode() == FileConstants.READ_WRITE) {
            // if mode is read/write, enable both read/write params
            inputRemoveEOLBox.setVisible(true);
            inputArchiveBox.setVisible(true);
            inputArchiveRelativeBox.setVisible(true);
            inputArchiveDirectoryTextField.setVisible(true);
            inputArchDirectoryLabel.setVisible(true);
            inputArchiveButton.setVisible(true);
            inputProtectBox.setVisible(true);
            inputProtectRelativeBox.setVisible(true);
            inputProtectDirectoryLabel.setVisible(true);
            inputProtectDirectoryTextField.setVisible(true);
            inputProtectButton.setVisible(true);
            inputStageBox.setVisible(true);
            inputStageRelativeBox.setVisible(true);
            inputStageDirectoryLabel.setVisible(true);
            inputStageDirectoryTextField.setVisible(true);
            inputStageButton.setVisible(true);
        } else if (getReadWriteMode() == FileConstants.WRITE) {
            // remove read-specific parameters
            inputRemoveEOLBox.setVisible(false);
            inputArchiveBox.setVisible(false);
            inputArchiveRelativeBox.setVisible(false);
            inputArchiveDirectoryTextField.setVisible(false);
            inputArchDirectoryLabel.setVisible(false);
            inputArchiveButton.setVisible(false);
            inputProtectBox.setVisible(true);
            inputProtectRelativeBox.setVisible(true);
            inputProtectDirectoryLabel.setVisible(true);
            inputProtectDirectoryTextField.setVisible(true);
            inputProtectButton.setVisible(true);
            inputStageBox.setVisible(true);
            inputStageRelativeBox.setVisible(true);
            inputStageDirectoryLabel.setVisible(true);
            inputStageDirectoryTextField.setVisible(true);
            inputStageButton.setVisible(true);
        } else if ((getReadWriteMode() == FileConstants.READ) ||
                (getReadWriteMode() == -1)) {
            // remove write-specific parameters
            inputRemoveEOLBox.setVisible(true);
            inputArchiveBox.setVisible(true);
            inputArchiveRelativeBox.setVisible(true);
            inputArchiveDirectoryTextField.setVisible(true);
            inputArchDirectoryLabel.setVisible(true);
            inputArchiveButton.setVisible(true);
            inputAddEOLBox.setVisible(false);
            inputProtectBox.setVisible(false);
            inputProtectRelativeBox.setVisible(false);
            inputProtectDirectoryLabel.setVisible(false);
            inputProtectDirectoryTextField.setVisible(false);
            inputProtectButton.setVisible(false);
            inputStageBox.setVisible(false);
            inputStageRelativeBox.setVisible(false);
            inputStageDirectoryLabel.setVisible(false);
            inputStageDirectoryTextField.setVisible(false);
            inputStageButton.setVisible(false);
        }
        DialogDescriptor descriptor = new DialogDescriptor(
                inputAdvMessageTabPanel,
                NbBundle.getMessage(FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel.inputAdvanceTitlePanel"),
                true, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
    }
}//GEN-LAST:event_inputDetailsBtnActionPerformed

private void inputUseLiteralRadioBtnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputUseLiteralRadioBtnFocusLost
    // TODO add your handling code here:
    clearDescriptionArea();
}//GEN-LAST:event_inputUseLiteralRadioBtnFocusLost

private void inputUseLiteralRadioBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputUseLiteralRadioBtnFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputUseLiteralRadioBtnFocusGained

private void isRelativePathBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isRelativePathBoxActionPerformed
    // TODO add your handling code here:
    updateRelativePathInfo();
}//GEN-LAST:event_isRelativePathBoxActionPerformed

private void pathRelativeToComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_pathRelativeToComboBoxItemStateChanged
    // TODO add your handling code here:
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        String directory = getFileAddress();
        if ((getPathRelativeTo() != null) && (directory == null) ||
                (directory.equals(""))) {
            firePropertyChange(
                    ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                    FileBindingConfigurationPanel.class,
                    "FileBindingConfigurationPanel.FileDirectoryMustBeSet"));
        }
    }
}//GEN-LAST:event_pathRelativeToComboBoxItemStateChanged

private void inputUseEncodedRadioBtnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputUseEncodedRadioBtnFocusLost
    // TODO add your handling code here:
    clearDescriptionArea();
}//GEN-LAST:event_inputUseEncodedRadioBtnFocusLost

private void inputUseEncodedRadioBtnFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputUseEncodedRadioBtnFocusGained
    // TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_inputUseEncodedRadioBtnFocusGained

private void directionModeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_directionModeBoxItemStateChanged
    // TODO add your handling code here:
    if (mInOnly) {
        if (directionModeBox.getSelectedItem() != null) {
            if (directionModeBox.getSelectedItem().toString().equals(FileConstants.READ_STR)) {
                updateReadInfo(true);
            } else if (directionModeBox.getSelectedItem().toString().equals(FileConstants.WRITE_STR)) {
                updateWriteInfo(true);
            } else if (directionModeBox.getSelectedItem().toString().equals(FileConstants.READ_WRITE_STR)) {
                updateReadWriteInfo();
            }
        }
    }
}//GEN-LAST:event_directionModeBoxItemStateChanged

private void outputDetailsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputDetailsBtnActionPerformed
    // TODO add your handling code here:
    if (outputMessageTabPanel != null) {
        if (getReadWriteMode() == FileConstants.READ_WRITE) {
            // if mode is write, remove read-specific parameters
            outputRemoveEOLBox.setVisible(true);
            outputArchiveBox.setVisible(true);
            outputArchiveIsRelativeBox.setVisible(true);
            outputArchiveDirectoryTextField.setVisible(true);
            outputArchiveDirLabel.setVisible(true);
            outputArchiveButton.setVisible(true);
        } else {
            outputRemoveEOLBox.setVisible(false);
            outputArchiveBox.setVisible(false);
            outputArchiveIsRelativeBox.setVisible(false);
            outputArchiveDirectoryTextField.setVisible(false);
            outputArchiveDirLabel.setVisible(false);
            outputArchiveButton.setVisible(false);
        }
        DialogDescriptor descriptor = new DialogDescriptor(
                outputAdvMessageTabPanel,
                NbBundle.getMessage(FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel.outputAdvanceTitlePanel"),
                true, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
        } else {
        }
    }
}//GEN-LAST:event_outputDetailsBtnActionPerformed

private void outputEncodingStyleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputEncodingStyleTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_outputEncodingStyleTextFieldActionPerformed
    private void inputAddEOLBoxparameterFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputAddEOLBoxparameterFocusGained
        // TODO add your handling code here:
}//GEN-LAST:event_inputAddEOLBoxparameterFocusGained
    private void inputAddEOLBoxparameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputAddEOLBoxparameterFocusLost
        // TODO add your handling code here:
}//GEN-LAST:event_inputAddEOLBoxparameterFocusLost

    private void inputStageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputStageButtonActionPerformed
        // TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(null, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    inputStageDirectoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    validateFilePath(inputStageDirectoryTextField.getText());
                }
            }
        });
    }//GEN-LAST:event_inputStageButtonActionPerformed

    private void inputStageDirectoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputStageDirectoryTextFieldActionPerformed
        // TODO add your handling code here:
        validateFilePath(inputStageDirectoryTextField.getText());
    }//GEN-LAST:event_inputStageDirectoryTextFieldActionPerformed

    private void inputProtectDirectoryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputProtectDirectoryTextFieldActionPerformed
        // TODO add your handling code here:
        validateFilePath(inputProtectDirectoryTextField.getText());
    }//GEN-LAST:event_inputProtectDirectoryTextFieldActionPerformed

    private void inputProtectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputProtectButtonActionPerformed
        // TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int retVal = directoryFileChooser.showDialog(null, "Select");
                if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                    inputProtectDirectoryTextField.setText(directoryFileChooser.
                            getSelectedFile().getAbsolutePath());
                    validateFilePath(inputProtectDirectoryTextField.getText());
                }
            }
        });
    }//GEN-LAST:event_inputProtectButtonActionPerformed

    private void inputProtectRelativeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputProtectRelativeBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputProtectRelativeBoxActionPerformed

    private void directoryLabImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_directoryLabImageMouseClicked
        // TODO add your handling code here:
//        FileNameStepWizardAction action = (FileNameStepWizardAction) SystemAction.get(FileNameStepWizardAction.class);
//        action.performAction(mQName, mComponent);       
    }//GEN-LAST:event_directoryLabImageMouseClicked

    private void validateFilePath(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if ((file == null) || (!file.exists())) {
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_ERROR_EVT, null,
                        NbBundle.getMessage(FileBindingConfigurationPanel.class,
                        "FileBindingConfiguratonnPanel.FILE_NOT_FOUND"));
            } else {
                firePropertyChange(
                        ExtensibilityElementConfigurationEditorComponent.
                        PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            }
        }
    }

    private void initFileChooser() {
        if (this.directoryFileChooser != null) {
            this.directoryFileChooser.setFileSelectionMode(JFileChooser.
                    DIRECTORIES_ONLY);
            this.directoryFileChooser.setAcceptAllFileFilterUsed(false);
        }
    }

    private JTextField getDirectoryTextField() {
        return directoryTextField;
    }

    private void initListeners() {
        if (mItemListener == null)  {
            mItemListener = new MyItemListener();
        }

        inputProtectRelativeBox.addItemListener(mItemListener);
        inputStageRelativeBox.addItemListener(mItemListener);
    }

    private void resetView() {
        inputProtectRelativeBox.removeItemListener(mItemListener);
        inputArchiveRelativeBox.removeItemListener(mItemListener);
        String url = NbBundle.getMessage(FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel.IMG_CASA");
        Image img = org.openide.util.Utilities.loadImage(url);
        mInOnly = false;
        mCASAImg = new ImageIcon(img);
        servicePortComboBox.setEnabled(false);
        portTypeComboBox.setEditable(false);
        servicePortComboBox.removeAllItems();
        bindingNameComboBox.removeAllItems();
        portTypeComboBox.removeAllItems();
        operationNameComboBox.removeAllItems();
        pathRelativeToComboBox.removeAllItems();
        inputFileTypeComboBox.removeAllItems();
        outputFileTypeComboBox.removeAllItems();
        inputPartComboBox.removeAllItems();
        outputPartComboBox.removeAllItems();
        pathRelativeToComboBox.addItem(FileConstants.USER_HOME);
        pathRelativeToComboBox.addItem(FileConstants.CURRENT_WORKING_DIR);
        pathRelativeToComboBox.addItem(FileConstants.DEFAULT_SYSTEM_TEMP_DIR);
        inputFileTypeComboBox.addItem(FileConstants.TEXT);
        readRBtn.setSelected(false);
        writeRBtn.setSelected(false);
        readWriteRBtn.setSelected(false);
        directionModeBox.removeAllItems();
        directionModeBox.addItem(FileConstants.READ_STR);
        directionModeBox.addItem(FileConstants.WRITE_STR);
        directionModeBox.addItem(FileConstants.READ_WRITE_STR);
        outputFileTypeComboBox.addItem(FileConstants.TEXT);
//        outputFileTypeComboBox.addItem(FileConstants.BINARY);
        if (mErrBuff == null) {
            mErrBuff = new StringBuffer();
        }

        // enable general tab sections
        readPropertiesGeneralLabel.setVisible(true);
        inputFileNameLabel.setVisible(true);
        inputFileNameTextField.setVisible(true);
        inputFileIsPatternBox.setVisible(true);
        sep2General.setVisible(true);
        writePropertiesGeneralLabel.setVisible(true);
        outputFileNameLabel.setVisible(true);
        outputFileNameTextField.setVisible(true);
        outputFileIsPatternBox.setVisible(true);
        sep3General.setVisible(true);

        // enable services sections
        servicesGeneralPanel.setVisible(true);
        servicesAdvancePanel.setVisible(true);

        // enable read and write sections
        enableReadInfo();
        enableWriteInfo();

        updateSectionsVisibility();
        bindingConfigurationPanel.setSelectedIndex(0);
    }

    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        cleanUp();
        mQName = qName;
        mComponent = component;
        populateView(component);
        initListeners();        
    }

    private void populateView(WSDLComponent component) {
        resetView();
        if (component != null) {
            if (component instanceof FileAddress) {
                populateFileAddress((FileAddress) component);
            } else if (component instanceof FileBinding) {
                populateFileBinding((FileBinding) component, null);
            } else if (component instanceof Port) {
                Collection<FileAddress> address = ((Port) component).
                        getExtensibilityElements(FileAddress.class);
                if (!address.isEmpty()) {
                    populateFileAddress(address.iterator().next());
                }
            } else if (component instanceof FileMessage) {
                Object obj = ((FileMessage)component).getParent();
                Binding parentBinding = null;
                if (obj instanceof BindingInput) {
                    BindingOperation parentOp =
                            (BindingOperation) ((BindingInput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                } else if (obj instanceof BindingOutput) {
                    BindingOperation parentOp = (BindingOperation)
                            ((BindingOutput) obj).getParent();
                    parentBinding = (Binding) parentOp.getParent();
                }
                if (parentBinding != null) {
                    Collection<FileBinding> bindings = parentBinding.
                            getExtensibilityElements(FileBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFileBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.
                                setSelectedItem(parentBinding.getName());
                    }
                }
            } else if (component instanceof FileOperation) {
                Object obj = ((FileOperation)component).getParent();
                if (obj instanceof BindingOperation) {
                    Binding parentBinding = (Binding)
                            ((BindingOperation)obj).getParent();
                    Collection<FileBinding> bindings = parentBinding.
                            getExtensibilityElements(FileBinding.class);
                    if (!bindings.isEmpty()) {
                        populateFileBinding(bindings.iterator().next(), null);
                        bindingNameComboBox.setSelectedItem(
                                parentBinding.getName());
                    }
                }
            }
            populateDescriptionAndTooltip();
        }
    }

    private void populateFileAddress(FileAddress fileAddress) {
        Port port = (Port) fileAddress.getParent();
        if (port.getBinding() != null) {
            Binding binding = port.getBinding().get();
            Collection<FileBinding> bindings = binding.
                    getExtensibilityElements(FileBinding.class);
            servicePortComboBox.setEnabled(false);
            servicePortComboBox.setSelectedItem(port.getName());
            if (!bindings.isEmpty()) {
                populateFileBinding(bindings.iterator().next(), fileAddress);
                bindingNameComboBox.setSelectedItem(binding.getName());
            }
            // from Port, need to disable binding box as 1:1 relationship
            bindingNameComboBox.setEditable(false);
            bindingNameComboBox.setEnabled(false);
        }
    }

    private void populateFileBinding(FileBinding fileBinding,
            FileAddress fileAddress) {
        if (fileAddress == null) {
            servicePortComboBox.setEnabled(true);
            fileAddress = getFileAddress(fileBinding);
        }
        if (fileAddress == null) {
            return;
        }
        Port port = (Port) fileAddress.getParent();

        // need to populate with all service ports that uses this binding
        populateListOfPorts(fileBinding);
        servicePortComboBox.setSelectedItem(port);

        // from Binding, need to allow changing of Port
        bindingNameComboBox.setEditable(false);
        bindingNameComboBox.setEnabled(false);

        updateServiceView(fileAddress);
        if (fileBinding != null) {
            populateListOfBindings(fileBinding);
            populateListOfPortTypes(fileBinding);
            Binding binding = (Binding) fileBinding.getParent();
            bindingNameComboBox.setSelectedItem(binding.getName());
            NamedComponentReference<PortType> pType = binding.getType();
            PortType portType = pType.get();
            portTypeComboBox.setSelectedItem(portType.getName());

            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            populateOperations(bindingOperations);

//            operationNameComboBox.addItemListener(new ItemListener() {
//                public void itemStateChanged(ItemEvent evt) {
//                    // based on selected operation, populate messages
//                    operationNameComboBoxItemStateChanged(evt);
//                }
//            });
            // select the 1st item since this is not a configurable param
            if (operationNameComboBox.getItemCount() > 0) {
                operationNameComboBox.setSelectedIndex(0);
            }

            if (operationNameComboBox.getItemCount() == 1) {
                // need to implicitly call update on messages because above
                // listener will not change selection if only 1 item
                if (binding != null) {
                    FileMessage inputMessage = getInputFileMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateInputMessageView(inputMessage);

                    FileMessage outputMessage = getOutputFileMessage(binding,
                            operationNameComboBox.getSelectedItem().toString());
                    updateOutputMessageView(outputMessage);
                    if (outputMessage == null) {
                        updateOutputMessageViewFromInput(inputMessage);
                    }
                }
            }

        }

    }

    private void populateListOfPortTypes(FileBinding fileBinding) {
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<PortType> portTypes = defs.getPortTypes().iterator();
            List<PortType> filePortTypes = null;
            while (portTypes.hasNext()) {
                PortType portType = portTypes.next();
                if (!portTypeExists(portType.getName())) {
                    portTypeComboBox.addItem(portType.getName());
                }
            }
        }
    }

    private boolean portTypeExists(String portTypeName) {
        boolean exists = false;
        int count = portTypeComboBox.getItemCount();
        for (int i = 0; i < count; i++) {
            String name = (String) portTypeComboBox.getItemAt(i);
            if (portTypeName.equals(name)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    private PortType getPortType(String bindingName, FileAddress fileAddress) {
        if ((fileAddress != null) && (fileAddress.getParent() != null)) {
            Port parentPort = (Port) fileAddress.getParent();
            Service parentService = (Service) parentPort.getParent();
            Definitions defs = (Definitions) parentService.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<FileBinding> fileBindings = null;
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }
                NamedComponentReference<PortType> portType = binding.getType();
                if (binding.getName().equals(bindingName)) {
                    return portType.get();
                }
            }
        }
        return null;
    }

    private void populateListOfBindings(FileBinding fileBinding) {
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Binding> bindings = defs.getBindings().iterator();
            List<FileBinding> fileBindings = null;

            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                if (binding.getType() == null
                        || binding.getType().get() == null) {
                    continue;
                }

                fileBindings = binding.
                        getExtensibilityElements(FileBinding.class);
                if (fileBindings != null) {
                    Iterator iter = fileBindings.iterator();
                    while (iter.hasNext()) {
                        FileBinding b = (FileBinding) iter.next();
                        Binding fBinding = (Binding) b.getParent();
                        bindingNameComboBox.addItem(fBinding.getName());
                    }
                }
            }
        }
    }

    private void populateListOfPorts(FileBinding fileBinding) {
            Vector<Port> portV = new Vector<Port>();

        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            portV.add(port);
                        }
                    }
                }
            }
        }
        servicePortComboBox.setModel(new DefaultComboBoxModel(portV));
        servicePortComboBox.setRenderer(new PortCellRenderer());

    }

    private void populateOperations(Collection bindingOps) {
        Iterator iter = bindingOps.iterator();
        while (iter.hasNext()) {
            BindingOperation bop = (BindingOperation) iter.next();
            operationNameComboBox.addItem(bop.getName());
        }
    }

    private FileMessage getInputFileMessage(Binding binding,
            String selectedOperation) {
        FileMessage inputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingInput bi = bop.getBindingInput();
                    List<FileMessage> inputFileMessages =
                            bi.getExtensibilityElements(FileMessage.class);
                    if (inputFileMessages.size() > 0) {
                        inputFileMessage = inputFileMessages.get(0);
                        break;
                    }
                }
            }
        }
        return inputFileMessage;
    }

    private FileMessage getOutputFileMessage(Binding binding,
            String selectedOperation) {
        FileMessage outputFileMessage = null;
        if (binding != null) {
            Collection<BindingOperation> bindingOperations =
                    binding.getBindingOperations();
            for (BindingOperation bop : bindingOperations) {
                if (bop.getName().equals(selectedOperation)) {
                    BindingOutput bo = bop.getBindingOutput();
                    if (bo != null) {
                        List<FileMessage> outputFileMessages =
                                bo.getExtensibilityElements(FileMessage.class);
                        if (outputFileMessages.size() > 0) {
                            outputFileMessage = outputFileMessages.get(0);
                            break;
                        }
                    }
                }
            }
        }
        return outputFileMessage;
    }

    private void updateInputMessageView(FileMessage inputFileMessage) {
        if (inputFileMessage != null) {
            inputFileNameTextField.setText(inputFileMessage.getFileName());
            inputFileNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));      //NOI18N
            inputFileNameLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));      //NOI18N

            inputEncodingStyleTextField.setText(inputFileMessage.
                    getFileEncodingStyle()); // did not find resource for doc

            Long poll = inputFileMessage.getPollingInterval();
            if (poll != null) {
                inputPollingIntervalSpinner.setValue(inputFileMessage.
                    getPollingInterval().longValue());
            }
            inputPollingIntervalSpinner.setToolTipText(
                    mBundle.getString("DESC_Attribute_pollingInterval"));//NOI18N
            inputPollingIntervalLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_pollingInterval"));//NOI18N

            if ((inputFileMessage.getFileUseType() != null) &&
                    (inputFileMessage.getFileUseType().equals(
                    FileConstants.ENCODED))) {
                inputUseEncodedRadioBtn.setSelected(true);
            } else {
                inputUseLiteralRadioBtn.setSelected(true);
            }
            updateEncodingInfo(inputUseLiteralRadioBtn);
            if ((inputFileMessage.getFileType() == null) &&
                    (inputFileTypeComboBox.getItemCount() > 0)) {
                inputFileTypeComboBox.setSelectedIndex(0);
            } else {
                inputFileTypeComboBox.setSelectedItem(inputFileMessage.
                        getFileType());
            }
            inputFileTypeComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileType"));      //NOI18N
            inputFileTypeLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileType"));      //NOI18N

            inputFileIsPatternBox.setSelected(inputFileMessage.
                    getFileNameIsPattern());
            inputFileIsPatternBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileNameIsPattern"));      //NOI18N

            inputRemoveEOLBox.setSelected(inputFileMessage.getRemoveEOL());

            inputRemoveEOLBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_removeEOL"));     //NOI18N

            if (inputFileMessage.getRecordDelimiter() != null) {
                inputRecordDelimiterTextField.setText(inputFileMessage.
                        getRecordDelimiter());
            } else {
                inputRecordDelimiterTextField.setText("");
            }
            inputRecordDelimiterTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_recordDelimiter"));//NOI18N
            inputRecordDelimiterLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_recordDelimiter"));//NOI18N

            if (inputFileMessage.getMultipleRecordsPerFile()) {
                inputMultiRecordsPerFileBox.setSelected(true);
            } else {
                inputMultiRecordsPerFileBox.setSelected(false);
            }
            inputMultiRecordsPerFileBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_multipleRecordsPerFile"));      //NOI18N//NOI18N

            Long val = inputFileMessage.getMaxBytesPerRecord();
            if (val != null) {
                inputMaxBytesPerRecordSpinner.setValue(inputFileMessage.
                    getMaxBytesPerRecord().longValue());
            }
            inputMaxBytesPerRecordSpinner.setToolTipText(
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord"));      //NOI18N
            inputMaxBytesPerReLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord"));      //NOI18N

            if (inputFileMessage.getArchiveEnabled()) {
                inputArchiveBox.setSelected(true);
            } else {
                inputArchiveBox.setSelected(false);
            }
            updateArchiveProtectStage(inputArchiveBox);

            inputArchiveBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_archive"));       //NOI18N

            if (inputFileMessage.getArchiveDirIsRelative()) {
                inputArchiveRelativeBox.setSelected(true);
            } else {
                inputArchiveRelativeBox.setSelected(false);
            }
            inputArchiveRelativeBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_archiveDirIsRelative"));//NOI18N

            inputArchiveDirectoryTextField.setText(inputFileMessage.
                    getArchiveDirectory());
            inputArchiveDirectoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_archiveDirectory"));//NOI18N
            inputArchDirectoryLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_archiveDirectory"));//NOI18N

            BindingInput input = (BindingInput) inputFileMessage.getParent();
            Collection<Part> parts = WSDLUtils.getParts(input);
            Vector<String> vect = new Vector<String>();
            //vect.add("");
            for (Part part : parts) {
                vect.add(part.getName());
            }
            inputPartComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = inputFileMessage.getPart();
            if (part == null) {
                // per BC developer, will preselect 1st item
                if (inputPartComboBox.getItemCount() > 0) {
                    inputPartComboBox.setSelectedIndex(0);
                }
            } else {
                inputPartComboBox.setSelectedItem(part);
            }
            inputPartComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_part"));          //NOI18N
            inputPartLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_part"));          //NOI18N

        } else {
            // null out view
            inputFileNameTextField.setText("");
            inputEncodingStyleTextField.setText("");
            inputFileTypeComboBox.setSelectedItem("");
            inputFileIsPatternBox.setSelected(false);
            inputRemoveEOLBox.setSelected(false);
            inputRecordDelimiterTextField.setText("");
            inputMultiRecordsPerFileBox.setSelected(false);
            inputMaxBytesPerRecordSpinner.setValue(0);
            inputArchiveBox.setSelected(false);
            inputArchiveRelativeBox.setSelected(true);
            inputArchiveDirectoryTextField.setText("");

        }
    }

    private void updateOutputMessageView(FileMessage outputFileMessage) {
        if (outputFileMessage != null) {
            outputFileNameTextField.setText(outputFileMessage.getFileName());
            outputEncodingStyleTextField.setText(outputFileMessage.
                    getFileEncodingStyle());
            if ((outputFileMessage.getFileUseType() != null) &&
                    (outputFileMessage.getFileUseType().equals(FileConstants.ENCODED))) {
                outputUseEncodedRadioBtn.setSelected(true);
            } else {
                outputUseLiteralRadioBtn.setSelected(true);
            }
            updateEncodingInfo(outputUseLiteralRadioBtn);
            if ((outputFileMessage.getFileType() == null) &&
                    (outputFileTypeComboBox.getItemCount() > 0)) {
                outputFileTypeComboBox.setSelectedIndex(0);
            } else {
                outputFileTypeComboBox.setSelectedItem(outputFileMessage.
                        getFileType());
            }
            outputFileIsPatternBox.setSelected(outputFileMessage.
                    getFileNameIsPattern());

            outputAddEOLBox.setSelected(outputFileMessage.getAddEOL());
            if (outputFileMessage.getRecordDelimiter() != null) {
                outputRecordDelimiterTextField.setText(outputFileMessage.
                        getRecordDelimiter());
            } else {
                outputRecordDelimiterTextField.setText("");
            }
            outputMultiRecordsPerFileBox.setSelected(outputFileMessage.
                        getMultipleRecordsPerFile());
            Long val = outputFileMessage.getMaxBytesPerRecord();
            if (val != null) {
                outputMaxBytesPerRecordSpinner.setValue(outputFileMessage.
                    getMaxBytesPerRecord().longValue());
            }

            outputProtectBox.setSelected(outputFileMessage.getProtectEnabled());
            outputProtectIsRelativeBox.setSelected(outputFileMessage.
                    getProtectDirIsRelative());
            updateArchiveProtectStage(outputProtectBox);
            outputProtectDirectoryTextField.setText(outputFileMessage.
                    getProtectDirectory());

            outputStageBox.setSelected(outputFileMessage.getStagingEnabled());
            updateArchiveProtectStage(outputStageBox);
            outputStageIsRelativeBox.setSelected(outputFileMessage.
                    getStagingDirIsRelative());

            outputStageDirectoryTextField.setText(outputFileMessage.
                    getStagingDirectory());

            BindingOutput output = (BindingOutput) outputFileMessage.getParent();
            Collection<Part> parts = WSDLUtils.getParts(output);
            Vector<String> vect = new Vector<String>();
            //vect.add("");
            for (Part part : parts) {
                vect.add(part.getName());
            }
            outputPartComboBox.setModel(new DefaultComboBoxModel(vect));
            String part = outputFileMessage.getPart();
            if (part == null) {
                // per BC developer, will preselect 1st item
                if (outputPartComboBox.getItemCount() > 0) {
                    outputPartComboBox.setSelectedIndex(0);
                }
            } else {
                outputPartComboBox.setSelectedItem(part);
            }
        } else {
            // null out view
            outputFileNameTextField.setText("");
            outputEncodingStyleTextField.setText("");
            outputFileTypeComboBox.setSelectedItem("");
            outputFileIsPatternBox.setSelected(false);
            outputAddEOLBox.setSelected(false);
            outputRecordDelimiterTextField.setText("");
            outputMultiRecordsPerFileBox.setSelected(false);
            outputMaxBytesPerRecordSpinner.setValue(0);
            outputProtectBox.setSelected(false);
            outputProtectIsRelativeBox.setSelected(false);
            outputProtectDirectoryTextField.setText("");
            outputStageBox.setSelected(false);
            outputStageIsRelativeBox.setSelected(false);
            outputStageDirectoryTextField.setText("");

            // since we are using the input message view for the in/out,
            // need to set default to input view instead for the out params
            inputAddEOLBox.setSelected(false);
            inputProtectBox.setSelected(false);
            updateArchiveProtectStage(inputProtectBox);
            inputProtectRelativeBox.setSelected(true);
            inputProtectDirectoryTextField.setText("");
            inputStageBox.setSelected(false);
            updateArchiveProtectStage(inputStageBox);
            inputStageRelativeBox.setSelected(true);
            inputStageDirectoryTextField.setText("");

        }
        updateDirectionMode(outputFileMessage);
    }

    private void updateOutputMessageViewFromInput(FileMessage inMessage) {
        if (inMessage != null) {
            inputAddEOLBox.setSelected(inMessage.getAddEOL());

            inputProtectBox.setSelected(inMessage.getProtectEnabled());
            inputProtectRelativeBox.setSelected(inMessage.
                    getProtectDirIsRelative());
            updateArchiveProtectStage(inputProtectBox);
            inputProtectDirectoryTextField.setText(inMessage.
                    getProtectDirectory());

            inputStageBox.setSelected(inMessage.getStagingEnabled());
            updateArchiveProtectStage(inputStageBox);
            inputStageRelativeBox.setSelected(inMessage.
                    getStagingDirIsRelative());

            inputStageDirectoryTextField.setText(inMessage.
                    getStagingDirectory());
        }
    }

    private void updateDirectionMode(FileMessage fileMessage) {
        if (fileMessage != null) {

            // no need to prompt for direction as it is both
            writeRBtn.setVisible(false);
            readRBtn.setVisible(false);
            readWriteRBtn.setVisible(false);
//            directionModeBox.setVisible(false);
//            modeLab.setVisible(false);
//            directionSep.setVisible(false);
//            directionLab.setVisible(false);
            directionAdvancePanel.setVisible(false);
            enableReadInfo();
            enableWriteInfo();
        } else {
            mInOnly = true;
//            directionLab.setVisible(true);
//            modeLab.setVisible(true);
//            directionModeBox.setVisible(true);
//            directionSep.setVisible(true);
            directionAdvancePanel.setVisible(true);
            updateReadInfo(true);
        }

    }

    private void updateSectionsVisibility() {
        if (mQName != null) {
            String prefix = mQName.getPrefix();
            String localPart = mQName.getLocalPart();
            if (prefix.equals("file") && localPart.equals("binding")) {
                // remove services section
                servicesGeneralPanel.setVisible(false);
                servicesAdvancePanel.setVisible(false);
            } else if (prefix.equals("") && localPart.equals("address")) {
                // from wizard, do not show port name and op name
                servicesGeneralPanel.setVisible(false);
            } else if (prefix.equals("file") && localPart.equals("address")) {
                // from wsdleditor, do not show port name
                servicesGeneralPanel.setVisible(false);
            }
        }
    }

    private void updateServiceView(FileAddress fileAddress) {
        if (fileAddress != null) {
            directoryTextField.setText(fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_ADDRESS));
            directoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N
            directoryLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N

            lockNameTextField.setText(fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_LOCK_NAME));
            lockNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_lockName")); //NOI18N
            lockNameLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_lockName")); //NOI18N

            workAreaTextField.setText(fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_WORK_AREA));
            workAreaTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_lockName")); //NOI18N
            workAreaLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_lockName")); //NOI18N

            seqNameTextField.setText(fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_SEQ_NAME));
            seqNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_seqName")); //NOI18N
            seqNameLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_seqName")); //NOI18N

            String val = fileAddress.
                    getAttribute(FileAddress.ATTR_FILE_RELATIVE_PATH);
            if ((val != null) && (val.equals("true"))) {
                isRelativePathBox.setSelected(true);
            } else {
                isRelativePathBox.setSelected(false);
            }
            updateRelativePathInfo();
            isRelativePathBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_relativePath"));  //NOI18N

            if (fileAddress.getAttribute(FileAddress.ATTR_FILE_PATH_RELATIVE_TO)
                    == null) {
                pathRelativeToComboBox.setSelectedItem(FileConstants.USER_HOME);
            } else {
                pathRelativeToComboBox.setSelectedItem(fileAddress.
                        getAttribute(FileAddress.ATTR_FILE_PATH_RELATIVE_TO));
            }
            pathRelativeToComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_pathRelativeTo"));//NOI18N
            pathRelativeToLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_pathRelativeTo"));//NOI18N
        }
    }

    FileAddress getFileAddressPerSelectedPort() {
        FileAddress address = null;
        Port selectedServicePort = (Port) servicePortComboBox.getSelectedItem();
        if (selectedServicePort != null) {
            Binding binding = selectedServicePort.getBinding().get();
            String selBindingName = bindingNameComboBox.
                    getSelectedItem().toString();
            if ((binding != null) && (binding.getName().
                    equals(selBindingName))) {
                Iterator<FileAddress> fileAddresses = selectedServicePort.
                        getExtensibilityElements(FileAddress.class).
                        iterator();
                // 1 fileaddress for 1 binding
                while (fileAddresses.hasNext()) {
                    return fileAddresses.next();
                }
            }
        }
        return address;
    }

    FileAddress getFileAddress(FileBinding fileBinding) {
        FileAddress fileAddress = null;
        if ((fileBinding != null) && (fileBinding.getParent() != null)) {
            Binding parentBinding = (Binding) fileBinding.getParent();
            Definitions defs = (Definitions) parentBinding.getParent();
            Iterator<Service> services = defs.getServices().iterator();
            String bindingName = parentBinding.getName();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();

                        if ((binding != null) && (binding.getName().
                                equals(bindingName))) {
                            Iterator<FileAddress> fileAddresses = port.
                                    getExtensibilityElements(FileAddress.class).
                                    iterator();
                            // 1 fileaddress for 1 binding
                            while (fileAddresses.hasNext()) {
                                return fileAddresses.next();
                            }
                        }
                    }
                }
            }
        }
        return fileAddress;
    }

    private void populateDescriptionAndTooltip() {
        if (mBundle != null) {
            directoryTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N
            directoryLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileDirectory")); //NOI18N

//            relativePathLabel.setToolTipText(
//                    mBundle.getString("DESC_Attribute_relativePath")); //NOI18N
            isRelativePathBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_relativePath")); //NOI18N

            inputFileTypeLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileType"));       //NOI18N
            inputFileTypeComboBox.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileType"));       //NOI18N

            inputFileNameLabel.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));       //NOI18N
            inputFileNameTextField.setToolTipText(
                    mBundle.getString("DESC_Attribute_fileName"));       //NOI18N

        }
    }

    private void cleanUp() {
        // clean up listeners TODO
        // null out data TODO
        mQName = null;
        mComponent = null;
    }

    private void clearDescriptionArea() {
        descriptionTextPane.setText("");
        inputAdvDescriptionArea.setText("");
        outputAdvDescriptionArea.setText("");
    }

    private void updateDescriptionArea(FocusEvent evt) {
        descriptionTextPane.setText("");
        inputAdvDescriptionArea.setText("");
        outputAdvDescriptionArea.setText("");

        // The image must first be wrapped in a style
        Style style = mDoc.addStyle("StyleName", null);
        StyleConstants.setIcon(style, mCASAImg);
        String[] desc = null;
        boolean casaEdited = false;

        if (evt.getSource() == directoryTextField) {
            desc = new String[]{"File Directory\n\n",
                    mBundle.getString("DESC_Attribute_fileDirectory")};
            casaEdited = true;
        } else if (evt.getSource() == inputFileNameTextField) {
            desc = new String[]{"File Name\n\n",
                    mBundle.getString("DESC_Attribute_fileName")};
        } else if (evt.getSource() == isRelativePathBox) {
            desc = new String[]{"Is Relative Path\n\n",
                    mBundle.getString("DESC_Attribute_relativePath")};
            casaEdited = true;
        } else if (evt.getSource() == pathRelativeToComboBox) {
            desc = new String[]{"Path Relative To\n\n",
                    mBundle.getString("DESC_Attribute_pathRelativeTo")};
            casaEdited = true;
        } else if (evt.getSource() == lockNameTextField) {
            desc = new String[]{"Lock Name\n\n",
                    mBundle.getString("DESC_Attribute_lockName")};
            casaEdited = true;
        } else if (evt.getSource() == outputFileNameTextField) {
            desc = new String[]{"File Name\n\n",
                    mBundle.getString("DESC_Attribute_fileName")};
        } else if (evt.getSource() == workAreaTextField) {
            desc = new String[]{"Processing Area\n\n",
                    mBundle.getString("DESC_Attribute_workArea")};
            casaEdited = true;
        } else if (evt.getSource() == seqNameTextField) {
            desc = new String[]{"Sequential Number FileName\n\n",
                    mBundle.getString("DESC_Attribute_seqName")};
            casaEdited = true;
        } else if (evt.getSource() == inputFileIsPatternBox) {
            desc = new String[]{"Input File Is Pattern\n\n",
                    mBundle.getString("DESC_Attribute_fileNameIsPattern")};
        } else if (evt.getSource() == outputFileIsPatternBox) {
            desc = new String[]{"Output File Is Pattern\n\n",
                    mBundle.getString("DESC_Attribute_fileNameIsPattern")};
        } else if (evt.getSource() == inputPollingIntervalSpinner) {
            desc = new String[]{"Polling Interval\n\n",
                    mBundle.getString("DESC_Attribute_pollingInterval")};
        } else if (evt.getSource() == inputFileTypeComboBox) {
            desc = new String[]{"File Type\n\n",
                    mBundle.getString("DESC_Attribute_fileType")};
        } else if (evt.getSource() == outputFileTypeComboBox) {
            desc = new String[]{"File Type\n\n",
                    mBundle.getString("DESC_Attribute_fileType")};
        }
        if (desc != null) {
            try {
                mDoc.insertString(mDoc.getLength(), desc[0],
                        mDoc.getStyle(mStyles[0]));
                mDoc.insertString(mDoc.getLength(), desc[1],
                        mDoc.getStyle(mStyles[1]));

                // Insert the image
                if (casaEdited) {
                    mDoc.insertString(mDoc.getLength(), "\n",
                            mDoc.getStyle(mStyles[1]));
                    mDoc.insertString(mDoc.getLength(), "ignored text", style);
                    mDoc.insertString(mDoc.getLength(), "  " + NbBundle.
                            getMessage(FileBindingConfigurationPanel.class,
                            "FileBindingConfigurationPanel.CASA_EDITED"),
                            mDoc.getStyle(mStyles[1]));
                }

                descriptionTextPane.setCaretPosition(0);
            } catch(BadLocationException ble) {
                mLogger.log(Level.FINER, ble.getMessage());
            }
            return;
        }

        // if coming from advanced popup
        if (evt.getSource() == inputPartComboBox) {
            desc = new String[]{"Part\n\n",
                    mBundle.getString("DESC_Attribute_part")};
        } else if (evt.getSource() == inputPartComboBox) {
            desc = new String[]{"Part\n\n",
                    mBundle.getString("DESC_Attribute_part")};
        } else if (evt.getSource() == inputRecordDelimiterTextField) {
            desc = new String[]{"Record Delimiter\n\n",
                    mBundle.getString("DESC_Attribute_recordDelimiter")};
        } else if (evt.getSource() == inputMaxBytesPerRecordSpinner) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        } else if (evt.getSource() == inputRemoveEOLBox) {
            desc = new String[]{"RemoveEOL\n\n",
                    mBundle.getString("DESC_Attribute_removeEOL")};
        } else if (evt.getSource() == inputMultiRecordsPerFileBox) {
            desc = new String[]{"Multi Records per  File\n\n",
                    mBundle.getString("DESC_Attribute_multipleRecordsPerFile")};
        } else if (evt.getSource() == inputArchiveBox) {
            desc = new String[]{"Is Archive\n\n",
                    mBundle.getString("DESC_Attribute_archive")};
        } else if (evt.getSource() == inputArchiveDirectoryTextField) {
            desc = new String[]{"Archive Directory\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirectory")};
        } else if (evt.getSource() == inputArchiveRelativeBox) {
            desc = new String[]{"Archive Directory Is Relative\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirIsRelative")};
        } else if (evt.getSource() == inputProtectBox) {
            desc = new String[]{"Is Protect\n\n",
                    mBundle.getString("DESC_Attribute_protect")};
        } else if (evt.getSource() == inputProtectDirectoryTextField) {
            desc = new String[]{"Protect Directory\n\n",
                    mBundle.getString("DESC_Attribute_protectDirectory")};
        } else if (evt.getSource() == inputProtectRelativeBox) {
            desc = new String[]{"Protect Directory is Relative\n\n",
                    mBundle.getString("DESC_Attribute_protectDirIsRelative")};
        } else if (evt.getSource() == inputStageBox) {
            desc = new String[]{"Is Stage\n\n",
                    mBundle.getString("DESC_Attribute_stage")};
        } else if (evt.getSource() == inputStageDirectoryTextField) {
            desc = new String[]{"Stage Directory\n\n",
                    mBundle.getString("DESC_Attribute_stageDirectory")};
        } else if (evt.getSource() == inputStageRelativeBox) {
            desc = new String[]{"Stage Directory is Relative\n\n",
                    mBundle.getString("DESC_Attribute_stageDirIsRelative")};
        }

        if (desc != null) {
            try {
                for (int i = 0; i < desc.length; i++) {
                    mDocAdv.insertString(mDocAdv.getLength(), desc[i],
                            mDocAdv.getStyle(mStyles[i]));
                }
                inputAdvDescriptionArea.setCaretPosition(0);
            } catch(BadLocationException ble) {
            }
            return;
        }

        // if coming from advanced popup
        if (evt.getSource() == outputPartComboBox) {
            desc = new String[]{"Part\n\n",
                    mBundle.getString("DESC_Attribute_part")};
        } else if (evt.getSource() == outputRecordDelimiterTextField) {
            desc = new String[]{"Record Delimiter\n\n",
                    mBundle.getString("DESC_Attribute_recordDelimiter")};
        } else if (evt.getSource() == outputMaxBytesPerRecordSpinner) {
            desc = new String[]{"Max Bytes Per Record\n\n",
                    mBundle.getString("DESC_Attribute_maxBytesPerRecord")};
        } else if (evt.getSource() == outputAddEOLBox) {
            desc = new String[]{"AddEOL\n\n",
                    mBundle.getString("DESC_Attribute_addEOL")};
        } else if (evt.getSource() == outputMultiRecordsPerFileBox) {
            desc = new String[]{"Multi Records per  File\n\n",
                    mBundle.getString("DESC_Attribute_multipleRecordsPerFile")};
        } else if (evt.getSource() == outputArchiveBox) {
            desc = new String[]{"Is Archive\n\n",
                    mBundle.getString("DESC_Attribute_archive")};
        } else if (evt.getSource() == outputArchiveDirectoryTextField) {
            desc = new String[]{"Archive Directory\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirectory")};
        } else if (evt.getSource() == outputArchiveIsRelativeBox) {
            desc = new String[]{"Archive Directory Is Relative\n\n",
                    mBundle.getString("DESC_Attribute_archiveDirIsRelative")};
        } else if (evt.getSource() == outputProtectBox) {
            desc = new String[]{"Is Protect\n\n",
                    mBundle.getString("DESC_Attribute_protect")};
        } else if (evt.getSource() == outputProtectDirectoryTextField) {
            desc = new String[]{"Protect Directory\n\n",
                    mBundle.getString("DESC_Attribute_protectDirectory")};
        } else if (evt.getSource() == outputProtectIsRelativeBox) {
            desc = new String[]{"Protect Directory is Relative\n\n",
                    mBundle.getString("DESC_Attribute_protectDirIsRelative")};
        } else if (evt.getSource() == outputStageBox) {
            desc = new String[]{"Is Stage\n\n",
                    mBundle.getString("DESC_Attribute_stage")};
        } else if (evt.getSource() == outputStageDirectoryTextField) {
            desc = new String[]{"Stage Directory\n\n",
                    mBundle.getString("DESC_Attribute_stageDirectory")};
        } else if (evt.getSource() == outputStageIsRelativeBox) {
            desc = new String[]{"Stage Directory is Relative\n\n",
                    mBundle.getString("DESC_Attribute_stageDirIsRelative")};
        }

        if (desc != null) {
            try {
                for (int i = 0; i < desc.length; i++) {
                    mDocAdvOut.insertString(mDocAdvOut.getLength(), desc[i],
                            mDocAdvOut.getStyle(mStyles[i]));
                }
                outputAdvDescriptionArea.setCaretPosition(0);
            } catch(BadLocationException ble) {
                mLogger.log(Level.FINER, ble.getMessage());
            }
        }
    }

    private void updateRelativePathInfo() {
        if (isRelativePathBox.isSelected()) {
            pathRelativeToLabel.setEnabled(true);
            pathRelativeToComboBox.setEnabled(true);
        } else {
            // grey out root directory (ie pathRelativeTo)
            pathRelativeToLabel.setEnabled(false);
            pathRelativeToComboBox.setEnabled(false);
        }
    }

    private void updateEncodingInfo(Object source) {
        if ((source == inputUseLiteralRadioBtn) ||
                (source == inputUseEncodedRadioBtn)) {
            if (inputUseEncodedRadioBtn.isSelected()) {
                inputEncodingStyleLabel.setEnabled(true);
                inputEncodingStyleTextField.setEnabled(true);
            } else {
                inputEncodingStyleLabel.setEnabled(false);
                inputEncodingStyleTextField.setEnabled(false);
            }
        } else if ((source == outputUseEncodedRadioBtn) ||
                (source == outputUseLiteralRadioBtn)) {
            if (outputUseEncodedRadioBtn.isSelected()) {
                outputEncodingStyleLabel.setEnabled(true);
                outputEncodingStyleTextField.setEnabled(true);
            } else {
                outputEncodingStyleLabel.setEnabled(false);
                outputEncodingStyleTextField.setEnabled(false);
            }
        }
    }

    private void updateReadInfo(boolean editable) {
        rAdvancePanel.setVisible(editable);
        updateEncodingInfo(inputUseLiteralRadioBtn);

        // from general tab, enable read section
        readPropertiesGeneralLabel.setVisible(editable);
        inputFileNameLabel.setVisible(editable);
        inputFileNameTextField.setVisible(editable);
        inputFileIsPatternBox.setVisible(editable);
        sep2General.setVisible(editable);

        // change label to indicate both a Read/Write
        readPropertiesGeneralLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readPropertiesGeneralLabel.text")); //NOI18N
        readPropertiesAdvLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readPropertiesAdvLabel.text")); //NOI18N

        // update advance propery for read-specific props
        inputRemoveEOLBox.setVisible(editable);

        inputProtectBox.setVisible(!editable);
        inputProtectRelativeBox.setEnabled(!editable);
        inputProtectDirectoryLabel.setEnabled(!editable);
        inputProtectDirectoryTextField.setEnabled(!editable);
        inputProtectButton.setEnabled(!editable);

        inputStageBox.setVisible(!editable);
        inputStageRelativeBox.setEnabled(!editable);
        inputStageDirectoryLabel.setEnabled(!editable);
        inputStageDirectoryTextField.setEnabled(!editable);
        inputStageButton.setEnabled(!editable);

        // from advanced tab, disable write
        wAdvancePanel.setVisible(false);

        // from general tab, disable write section
        writePropertiesGeneralLabel.setVisible(false);
        outputFileNameLabel.setVisible(false);
        outputFileNameTextField.setVisible(false);
        outputFileIsPatternBox.setVisible(false);
        sep3General.setVisible(false);

    }

    private void updateWriteInfo(boolean editable) {
        // from advanced tab, enable read since contains superset of parameters
        // just remove polling parameter
        rAdvancePanel.setVisible(true);
        inputPollingIntervalLabel.setVisible(false);
        inputPollingIntervalSpinner.setVisible(false);

        // from advanced tab, disable write
        wAdvancePanel.setVisible(false);

        // from general tab, enable read section
        readPropertiesGeneralLabel.setVisible(true);
        inputFileNameLabel.setVisible(true);
        inputFileNameTextField.setVisible(true);
        inputFileIsPatternBox.setVisible(true);
        sep2General.setVisible(true);

        // change label to indicate Write
        readPropertiesGeneralLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "writePropertiesGeneralLabel.text")); //NOI18N
        readPropertiesAdvLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "writePropertiesGeneralLabel.text")); //NOI18N

        // from general tab, disable write section
        writePropertiesGeneralLabel.setVisible(false);
        outputFileNameLabel.setVisible(false);
        outputFileNameTextField.setVisible(false);
        outputFileIsPatternBox.setVisible(false);
        sep3General.setVisible(false);

        // from advanced props,enable write-related params
        inputAddEOLBox.setVisible(editable);

        inputProtectBox.setVisible(editable);
        inputProtectRelativeBox.setEnabled(editable);
        inputProtectDirectoryLabel.setEnabled(editable);
        inputProtectDirectoryTextField.setEnabled(editable);
        inputProtectButton.setEnabled(editable);

        inputStageBox.setVisible(editable);
        inputStageRelativeBox.setEnabled(editable);
        inputStageDirectoryLabel.setEnabled(editable);
        inputStageDirectoryTextField.setEnabled(editable);
        inputStageButton.setEnabled(editable);
    }

    private void updateReadWriteInfo() {

        // from advanced tab, enable read since contains superset of parameters
        rAdvancePanel.setVisible(true);
        inputPollingIntervalLabel.setVisible(true);
        inputPollingIntervalSpinner.setVisible(true);

        // from advanced tab, disable write
        wAdvancePanel.setVisible(false);

        // from general tab, enable read section
        readPropertiesGeneralLabel.setVisible(true);
        inputFileNameLabel.setVisible(true);
        inputFileNameTextField.setVisible(true);
        inputFileIsPatternBox.setVisible(true);
        sep2General.setVisible(true);

        // change label to indicate both a Read/Write
        readPropertiesGeneralLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readWritePropertiesGeneralLabel.text")); //NOI18N
        readPropertiesAdvLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readWritePropertiesGeneralLabel.text")); //NOI18N

        // from general tab, disable write section
        writePropertiesGeneralLabel.setVisible(false);
        outputFileNameLabel.setVisible(false);
        outputFileNameTextField.setVisible(false);
        outputFileIsPatternBox.setVisible(false);
        sep3General.setVisible(false);

        // from advanced props,enable write-related params
        inputAddEOLBox.setVisible(true);

        inputProtectBox.setVisible(true);
        inputProtectRelativeBox.setEnabled(true);
        inputProtectDirectoryLabel.setEnabled(true);
        inputProtectDirectoryTextField.setEnabled(true);
        inputProtectButton.setEnabled(true);
        updateArchiveProtectStage(inputProtectBox);

        inputStageBox.setVisible(true);
        inputStageRelativeBox.setEnabled(true);
        inputStageDirectoryLabel.setEnabled(true);
        inputStageDirectoryTextField.setEnabled(true);
        inputStageButton.setEnabled(true);
        updateArchiveProtectStage(inputStageBox);
    }

    private void enableReadInfo() {
        rAdvancePanel.setVisible(true);
        updateEncodingInfo(inputUseLiteralRadioBtn);

        // from general tab, enable read section
        readPropertiesGeneralLabel.setVisible(true);
        inputFileNameLabel.setVisible(true);
        inputFileNameTextField.setVisible(true);
        inputFileIsPatternBox.setVisible(true);
        sep2General.setVisible(true);

        // change label to indicate both a Read
        readPropertiesGeneralLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readPropertiesGeneralLabel.text")); //NOI18N
        readPropertiesAdvLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "readPropertiesAdvLabel.text")); //NOI18N

        // update advance propery for read-specific props
        inputRemoveEOLBox.setVisible(true);
    }

    private void enableWriteInfo() {

        wAdvancePanel.setVisible(true);

        // change label to indicate Write
        writePropertiesGeneralLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "writePropertiesGeneralLabel.text")); //NOI18N
        writePropertiesAdvLabel.setText(NbBundle.getMessage(
                FileBindingConfigurationPanel.class,
                "FileBindingConfigurationPanel." +
                "writePropertiesGeneralLabel.text")); //NOI18N

        // from general tab, disable write section
        writePropertiesGeneralLabel.setVisible(true);
        outputFileNameLabel.setVisible(true);
        outputFileNameTextField.setVisible(true);
        outputFileIsPatternBox.setVisible(true);
        sep3General.setVisible(true);

        // from advanced props,enable write-related params
        inputAddEOLBox.setVisible(true);

        inputProtectBox.setVisible(true);
        inputProtectRelativeBox.setEnabled(true);
        inputProtectDirectoryLabel.setEnabled(true);
        inputProtectDirectoryTextField.setEnabled(true);
        inputProtectButton.setEnabled(true);

        inputStageBox.setVisible(true);
        inputStageRelativeBox.setEnabled(true);
        inputStageDirectoryLabel.setEnabled(true);
        inputStageDirectoryTextField.setEnabled(true);
        inputStageButton.setEnabled(true);
    }

    private boolean isInputFileValid() {
        boolean valid = true;
        if (inputFileIsPatternBox.isSelected()) {
            String inputFileName = getInputFileName();
            if ((inputFileIsPatternBox.isSelected()) &&
                    ((inputFileName == null) || (inputFileName.equals("")) ||
                    ((inputFileName.indexOf("%d") == -1) &&
                     (inputFileName.indexOf("%t") == -1) &&
                     (inputFileName.indexOf("%u") == -1)) )) {
                return false;
            }
        }
        return valid;
    }

    private boolean isOutputFileValid() {
        boolean valid = true;
        if (outputFileIsPatternBox.isSelected()) {
            String outputFileName = getOutputFileName();
            if ((outputFileIsPatternBox.isSelected()) &&
                    ((outputFileName == null) || (outputFileName.equals("")) ||
                    ((outputFileName.indexOf("%d") == -1) &&
                     (outputFileName.indexOf("%t") == -1) &&
                     (outputFileName.indexOf("%u") == -1)) )) {
                return false;
            }
        }
        return valid;
    }

    private void updateArchiveProtectStage(Object source) {
        if (source == inputArchiveBox) {
            inputArchiveRelativeBox.setEnabled(inputArchiveBox.isSelected());
            inputArchDirectoryLabel.setEnabled(inputArchiveBox.isSelected());
            inputArchiveDirectoryTextField.setEnabled(
                    inputArchiveBox.isSelected());
            inputArchiveButton.setEnabled(inputArchiveBox.isSelected());
        }  else if (source == inputProtectBox) {
            inputProtectRelativeBox.setEnabled(inputProtectBox.isSelected());
            inputProtectDirectoryLabel.setEnabled(inputProtectBox.isSelected());
            inputProtectDirectoryTextField.setEnabled(inputProtectBox.isSelected());
            inputProtectButton.setEnabled(inputProtectBox.isSelected());
        } else if (source == inputStageBox) {
            inputStageRelativeBox.setEnabled(inputStageBox.isSelected());
            inputStageDirectoryLabel.setEnabled(inputStageBox.isSelected());
            inputStageDirectoryTextField.setEnabled(inputStageBox.isSelected());
            inputStageButton.setEnabled(inputStageBox.isSelected());
        } else if (source == outputArchiveBox) {
            if (outputArchiveBox.isSelected()) {
                outputArchiveIsRelativeBox.setEnabled(true);
                outputArchiveDirLabel.setEnabled(true);
                outputArchiveDirectoryTextField.setEnabled(true);
                outputArchiveButton.setEnabled(true);
            } else {
                outputArchiveIsRelativeBox.setEnabled(false);
                outputArchiveDirLabel.setEnabled(false);
                outputArchiveDirectoryTextField.setEnabled(false);
                outputArchiveButton.setEnabled(false);
            }
        } else if (source == outputProtectBox) {
            if (outputProtectBox.isSelected()) {
                outputProtectIsRelativeBox.setEnabled(true);
                outputProtectDirLabel.setEnabled(true);
                outputProtectDirectoryTextField.setEnabled(true);
                outputProtectButton.setEnabled(true);
            } else {
                outputProtectIsRelativeBox.setEnabled(false);
                outputProtectDirLabel.setEnabled(false);
                outputProtectDirectoryTextField.setEnabled(false);
                outputProtectButton.setEnabled(false);
            }
        } else if (source == outputStageBox) {
            if (outputStageBox.isSelected()) {
                outputStageIsRelativeBox.setEnabled(true);
                outputStageDirLabel.setEnabled(true);
                outputStageDirectoryTextField.setEnabled(true);
                outputStageButton.setEnabled(true);
            } else {
                outputStageIsRelativeBox.setEnabled(false);
                outputStageDirLabel.setEnabled(false);
                outputStageDirectoryTextField.setEnabled(false);
                outputStageButton.setEnabled(false);
            }
        }
    }

    private boolean isDirectoryRelative(Object source) {
        boolean valid = true;
        if (source == inputArchiveRelativeBox) {
            if (inputArchiveRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getInputArchiveDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == inputProtectRelativeBox) {
            if (inputProtectRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getInputProtectDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == inputStageRelativeBox) {
            if (inputStageRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getInputStageDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == outputArchiveIsRelativeBox) {
            if (outputArchiveIsRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getOutputArchiveDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == outputProtectIsRelativeBox) {
            if (outputProtectIsRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getOutputProtectDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        } else if (source == outputStageIsRelativeBox) {
            if (outputStageIsRelativeBox.isSelected()) {
                // make sure directory is filled in and must be relative
                String dir = getOutputStageDirectory();
                File fileDir = new File(dir);
                if ((dir == null) || (fileDir.isAbsolute())) {
                    return false;
                }
            }
        }
        return valid;
    }

    public class PortCellRenderer extends JLabel
            implements javax.swing.ListCellRenderer {

        public PortCellRenderer() {
            super();
            setOpaque(true);
        }

        public Component getListCellRendererComponent(javax.swing.JList list,
                Object value, int index, boolean isSelected,
                boolean isFocused) {
            if ((value != null) && (value instanceof Port)) {
                setText(((Port) value).getName());
                setBackground(isSelected ?
                    list.getSelectionBackground() : list.getBackground());
                setForeground(isSelected ?
                    list.getSelectionForeground() : list.getForeground());
            }
            return this;
        }
    }

    public class MyItemListener implements ItemListener {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            handleItemStateChanged(evt);
        }
    }
    
    private void handleItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getSource() == inputProtectRelativeBox) {
                if (!isDirectoryRelative(inputStageRelativeBox)) {
                    // make sure directory is filled in and must be relative
                    firePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                            FileBindingConfigurationPanel.class,
                            "FileBindingConfigurationPanel.inputStageDirectoryMustBeRelative.text"));
                } else if (isDirectoryRelative(outputStageIsRelativeBox)&&
                        isDirectoryRelative(outputProtectIsRelativeBox) &&
                        isDirectoryRelative(outputArchiveIsRelativeBox)&&
                        isDirectoryRelative(inputProtectRelativeBox) &&
                        isDirectoryRelative(inputArchiveRelativeBox)) {
                    firePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_CLEAR_MESSAGES_EVT, null, "");
                }
                return;
            } else if (evt.getSource() == inputStageRelativeBox) {
                if (!isDirectoryRelative(inputStageRelativeBox)) {
                    // make sure directory is filled in and must be relative
                    firePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT, null, NbBundle.getMessage(
                            FileBindingConfigurationPanel.class,
                            "FileBindingConfigurationPanel.inputStageDirectoryMustBeRelative.text"));
                } else if (isDirectoryRelative(outputStageIsRelativeBox)&&
                        isDirectoryRelative(outputProtectIsRelativeBox) &&
                        isDirectoryRelative(outputArchiveIsRelativeBox)&&
                        isDirectoryRelative(inputProtectRelativeBox) &&
                        isDirectoryRelative(inputArchiveRelativeBox)) {
                    firePropertyChange(
                            ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_CLEAR_MESSAGES_EVT, null, "");
                }
                return;
            }
        }
        firePropertyChange(
                ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT, null, "");
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advanceTabPanel;
    private javax.swing.JTabbedPane bindingConfigurationPanel;
    private javax.swing.JComboBox bindingNameComboBox;
    private javax.swing.JLabel bindingNameLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JPanel directionAdvancePanel;
    private javax.swing.JLabel directionLab;
    private javax.swing.JComboBox directionModeBox;
    private javax.swing.JSeparator directionSep;
    private javax.swing.JFileChooser directoryFileChooser;
    private javax.swing.JLabel directoryLabImage;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JSeparator directoryPropertiesSep;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JPanel generalTabPanel;
    private javax.swing.JCheckBox inputAddEOLBox;
    private javax.swing.JTextPane inputAdvDescriptionArea;
    private javax.swing.JPanel inputAdvMessageTabPanel;
    private javax.swing.JScrollPane inputAdvScrollPane;
    private javax.swing.JLabel inputArchDirectoryLabel;
    private javax.swing.JCheckBox inputArchiveBox;
    private javax.swing.JButton inputArchiveButton;
    private javax.swing.JTextField inputArchiveDirectoryTextField;
    private javax.swing.JCheckBox inputArchiveRelativeBox;
    private javax.swing.JButton inputDetailsBtn;
    private javax.swing.JLabel inputEncodingStyleLabel;
    private javax.swing.JTextField inputEncodingStyleTextField;
    private javax.swing.JCheckBox inputFileIsPatternBox;
    private javax.swing.JLabel inputFileNameLabel;
    private javax.swing.JTextField inputFileNameTextField;
    private javax.swing.JComboBox inputFileTypeComboBox;
    private javax.swing.JLabel inputFileTypeLabel;
    private javax.swing.JCheckBox inputFixedLengthBox;
    private javax.swing.JLabel inputMaxBytesPerReLabel;
    private javax.swing.JSpinner inputMaxBytesPerRecordSpinner;
    private javax.swing.JPanel inputMessageTabPanel;
    private javax.swing.JPanel inputMessageTabPanelTop;
    private javax.swing.JCheckBox inputMultiRecordsPerFileBox;
    private javax.swing.JComboBox inputPartComboBox;
    private javax.swing.JLabel inputPartLabel;
    private javax.swing.JLabel inputPollingIntervalLabel;
    private javax.swing.JSpinner inputPollingIntervalSpinner;
    private javax.swing.JLabel inputPostProcessingLabel;
    private javax.swing.JCheckBox inputProtectBox;
    private javax.swing.JButton inputProtectButton;
    private javax.swing.JLabel inputProtectDirectoryLabel;
    private javax.swing.JTextField inputProtectDirectoryTextField;
    private javax.swing.JCheckBox inputProtectRelativeBox;
    private javax.swing.JLabel inputRecordDelimiterLabel;
    private javax.swing.JTextField inputRecordDelimiterTextField;
    private javax.swing.JCheckBox inputRemoveEOLBox;
    private javax.swing.JPanel inputScrollAdvPanel;
    private javax.swing.JCheckBox inputStageBox;
    private javax.swing.JButton inputStageButton;
    private javax.swing.JLabel inputStageDirectoryLabel;
    private javax.swing.JTextField inputStageDirectoryTextField;
    private javax.swing.JCheckBox inputStageRelativeBox;
    private javax.swing.JRadioButton inputUseEncodedRadioBtn;
    private javax.swing.JLabel inputUseLabel;
    private javax.swing.JRadioButton inputUseLiteralRadioBtn;
    private javax.swing.JPanel isRelPathPanel;
    private javax.swing.JCheckBox isRelativePathBox;
    private javax.swing.JLabel isRelativePathCasaLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JLabel lockNameLabel;
    private javax.swing.JTextField lockNameTextField;
    private javax.swing.ButtonGroup mInputUseBtnGrp;
    private javax.swing.ButtonGroup mOutputUseBtnGrp;
    private javax.swing.ButtonGroup mReadWriteBtnGrp;
    private javax.swing.JLabel modeLab;
    private javax.swing.JComboBox operationNameComboBox;
    private javax.swing.JLabel operationNameLabel;
    private javax.swing.JCheckBox outputAddEOLBox;
    private javax.swing.JTextPane outputAdvDescriptionArea;
    private javax.swing.JPanel outputAdvMessageTabPanel;
    private javax.swing.JScrollPane outputAdvScrollPane;
    private javax.swing.JCheckBox outputArchiveBox;
    private javax.swing.JButton outputArchiveButton;
    private javax.swing.JLabel outputArchiveDirLabel;
    private javax.swing.JTextField outputArchiveDirectoryTextField;
    private javax.swing.JCheckBox outputArchiveIsRelativeBox;
    private javax.swing.JButton outputDetailsBtn;
    private javax.swing.JLabel outputEncodingStyleLabel;
    private javax.swing.JTextField outputEncodingStyleTextField;
    private javax.swing.JCheckBox outputFileIsPatternBox;
    private javax.swing.JLabel outputFileNameLabel;
    private javax.swing.JTextField outputFileNameTextField;
    private javax.swing.JComboBox outputFileTypeComboBox;
    private javax.swing.JLabel outputFileTypeLabel;
    private javax.swing.JCheckBox outputFixedLengthBox;
    private javax.swing.JLabel outputMaxBytesPerReLabel;
    private javax.swing.JSpinner outputMaxBytesPerRecordSpinner;
    private javax.swing.JPanel outputMessageTabPanel;
    private javax.swing.JCheckBox outputMultiRecordsPerFileBox;
    private javax.swing.JComboBox outputPartComboBox;
    private javax.swing.JLabel outputPartLabel;
    private javax.swing.JLabel outputPostProcessingLab;
    private javax.swing.JCheckBox outputProtectBox;
    private javax.swing.JButton outputProtectButton;
    private javax.swing.JLabel outputProtectDirLabel;
    private javax.swing.JTextField outputProtectDirectoryTextField;
    private javax.swing.JCheckBox outputProtectIsRelativeBox;
    private javax.swing.JTextField outputRecordDelimiterTextField;
    private javax.swing.JLabel outputRecordLabel;
    private javax.swing.JCheckBox outputRemoveEOLBox;
    private javax.swing.JLabel outputReocrdDelimLabel;
    private javax.swing.JPanel outputScrollAdvPanel;
    private javax.swing.JCheckBox outputStageBox;
    private javax.swing.JButton outputStageButton;
    private javax.swing.JLabel outputStageDirLabel;
    private javax.swing.JTextField outputStageDirectoryTextField;
    private javax.swing.JCheckBox outputStageIsRelativeBox;
    private javax.swing.JRadioButton outputUseEncodedRadioBtn;
    private javax.swing.JLabel outputUseLabel;
    private javax.swing.JRadioButton outputUseLiteralRadioBtn;
    private javax.swing.JComboBox pathRelativeToComboBox;
    private javax.swing.JLabel pathRelativeToLabel;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JComboBox portTypeComboBox;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JPanel rAdvancePanel;
    private javax.swing.JLabel readPropertiesAdvLabel;
    private javax.swing.JLabel readPropertiesGeneralLabel;
    private javax.swing.JRadioButton readRBtn;
    private javax.swing.JRadioButton readWriteRBtn;
    private javax.swing.JPanel rwGeneralPanel;
    private javax.swing.JPanel scrollTabPanel;
    private javax.swing.JSeparator sep1Advanced;
    private javax.swing.JSeparator sep1General;
    private javax.swing.JSeparator sep2General;
    private javax.swing.JSeparator sep3General;
    private javax.swing.JLabel seqNameLabel;
    private javax.swing.JTextField seqNameTextField;
    private javax.swing.JLabel serviceBindingOperationLabel;
    private javax.swing.JComboBox servicePortComboBox;
    private javax.swing.JPanel servicesAdvancePanel;
    private javax.swing.JLabel servicesAdvancedLabel;
    private javax.swing.JLabel servicesGeneralLabel;
    private javax.swing.JPanel servicesGeneralPanel;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel wAdvancePanel;
    private javax.swing.JLabel workAreaLabel;
    private javax.swing.JTextField workAreaTextField;
    private javax.swing.JLabel writePropertiesAdvLabel;
    private javax.swing.JLabel writePropertiesGeneralLabel;
    private javax.swing.JRadioButton writeRBtn;
    // End of variables declaration//GEN-END:variables

}
