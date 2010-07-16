/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.ui.tester.impl;

import com.sun.encoder.EncoderConfigurationException;
import com.sun.encoder.EncoderException;
import com.sun.encoder.EncoderType;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.prefs.BackingStoreException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.encoder.ui.basic.Utils;
import org.netbeans.modules.encoder.ui.tester.EncoderTestPerformer;
import org.netbeans.modules.encoder.ui.tester.EncoderTestTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.xml.sax.SAXException;

/**
 * The encoder test performer
 *
 * @author Cannis Meng
 */
public class EncoderTestPerformerImpl implements EncoderTestPerformer, ActionListener {

    private static final ResourceBundle _bundle =
        ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/tester/impl/Bundle"); //NOI18N
    private static final String PROCESS = _bundle.getString("test_performer.lbl.process"); //NOI18N
    private static final String PROCESS_DESC = _bundle.getString("test_performer.process.description"); //NOI18N
    private static final String CANCEL = _bundle.getString("test_performer.lbl.cancel"); //NOI18N
    private static final String CANCEL_DESC = _bundle.getString("test_performer.cancel.description"); //NOI18N
    public static final String ENCODE = "Encode";  //NOI18N
    public static final String DECODE = "Decode";  //NOI18N

    public static final QName TOP_PROPERTY_ELEMENT = new QName(EncodingConst.URI, EncodingConst.TOP_FLAG);
    private static final XmlBoolean XML_BOOLEAN_TRUE = XmlBoolean.Factory.newValue(Boolean.TRUE);
    private static final String DEBUG_PKG_NAME = "com.sun.encoder";  //NOI18N
    private TesterPanel testerPanel;
    private DialogDescriptor dialogDescriptor;
    private Dialog dialog;
    private EncoderTestTask mEncoderTestTask;
    private File metaFile;
    private EncoderType mEncoderType;

    public void performTest(File xsdFile, EncoderType encoderType) {
        metaFile = xsdFile;
        mEncoderType = encoderType;
        if (mEncoderTestTask == null) {
            mEncoderTestTask = new EncoderTestTaskImpl();
        }
        showDialog();
    }

    private void showDialog() {
        try {
            QName[] qnames = getTopElementDecls(metaFile);
            if (qnames.length == 0) {
                // i.e. no top element(s) are selected in the XSD
                // show dialog to ask user to fix the XSD file before testing
                JOptionPane.showMessageDialog(null,
                        _bundle.getString("test_panel.lbl.no_top_element_in_xsd"));
                return;
            }
            testerPanel = new TesterPanel(metaFile.getAbsolutePath());
            testerPanel.setTopElementDecls(getTopElementDecls(metaFile), null);
        } catch (XmlException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        // "Process" and "Cancel" buttons
        JButton processButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(processButton, PROCESS);
        processButton.setToolTipText(PROCESS_DESC);
        processButton.getAccessibleContext().setAccessibleName(PROCESS_DESC);
        processButton.getAccessibleContext().setAccessibleDescription(PROCESS_DESC);
        JButton cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, CANCEL);
        cancelButton.setToolTipText(CANCEL_DESC);
        cancelButton.getAccessibleContext().setAccessibleName(CANCEL_DESC);
        cancelButton.getAccessibleContext().setAccessibleDescription(CANCEL_DESC);
        // DialogDescriptor object
        dialogDescriptor = new DialogDescriptor(
            testerPanel, // inner component of the dialog
            _bundle.getString("test_performer.lbl.test_encoding"), //NOI18N //title of the dialog
            true, //modal status
            new Object[]{processButton, cancelButton}, //array of custom options
            processButton, //default option from custom option array
            DialogDescriptor.BOTTOM_ALIGN, //specifies where to place options in the dialog
            HelpCtx.DEFAULT_HELP, //help context specifying help page
            this); //listener for the user's button presses
        dialogDescriptor.setClosingOptions(new Object[]{cancelButton});
        dialogDescriptor.setButtonListener(this);
        // the Dialog
        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private static String stripOffMnemonics(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        int mnemonicPosition = Mnemonics.findMnemonicAmpersand(in);
        if (mnemonicPosition < 0) {
            // i.e. no mnemonics found
            return in;
        } else if (mnemonicPosition == 0) {
            // i.e. first char is mnemonics
            return in.substring(1);
        } else if (mnemonicPosition == in.length() - 1) {
            // i.e. last char is mnemonics
            return in.substring(0, mnemonicPosition);
        } else {
            return in.substring(0, mnemonicPosition)
                + in.substring(mnemonicPosition + 1);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(stripOffMnemonics(PROCESS))) {
            try {
                testerPanel.savePreferences();
            } catch (BackingStoreException ex) {
                //Ignore
            }
            process();
        }
    }

    /**
     * Gets the top level element declaractions (the declarations that define
     * messages) from an XSD file.  It only search within the XSD specified,
     * not any of the referenced XSDs.
     *
     * @param xsdFile the XSD file
     */
    private QName[] getTopElementDecls(File xsdFile)
            throws XmlException, IOException {
        SchemaDocument schemaDoc = SchemaDocument.Factory.parse(xsdFile);
        if (schemaDoc.getSchema() == null) {
            return new QName[0];
        }
        String targetNS = schemaDoc.getSchema().getTargetNamespace();
        if (targetNS != null && targetNS.length() == 0) {
            targetNS = null;
        }
        TopLevelElement[] elemDecls = schemaDoc.getSchema().getElementArray();
        if (elemDecls == null || elemDecls.length == 0) {
            return new QName[0];
        }
        List<QName> topElemList = new ArrayList<QName>();
        for (int i = 0; i < elemDecls.length; i++) {
            if (!elemDecls[i].isSetAnnotation()
                    || elemDecls[i].getAnnotation().sizeOfAppinfoArray() == 0) {
                continue;
            }
            final int countAppinfos = elemDecls[i].getAnnotation().sizeOfAppinfoArray();
            for (int j = 0; j < countAppinfos; j++) {
                if (!elemDecls[i].getAnnotation().getAppinfoArray(j).isSetSource()) {
                    continue;
                }
                if (!EncodingConst.URI.equals(
                        elemDecls[i].getAnnotation().getAppinfoArray(j).getSource())) {
                    continue;
                }
                XmlObject xmlObj = elemDecls[i].getAnnotation().getAppinfoArray(j);
                XmlObject[] topProps = xmlObj.selectChildren(TOP_PROPERTY_ELEMENT);
                if (topProps == null || topProps.length == 0) {
                    continue;
                }
                if (XML_BOOLEAN_TRUE.compareValue(topProps[0]) == 0) {
                    if (targetNS == null) {
                        topElemList.add(new QName(elemDecls[i].getName()));
                    } else {
                        topElemList.add(new QName(targetNS, elemDecls[i].getName()));
                    }
                }
            }
        }
        return topElemList.toArray(new QName[0]);
    }

    private void process() {

        int debugLevelIndex = testerPanel.getDebugLevelIndex();
        // if debugLevel is other than "None", then output debug information
        boolean debugging = debugLevelIndex > 0;
        ByteArrayOutputStream byteArrOS = null;
        Handler logHandler = null;
        Logger logger = null;
        Level origLevel = null;
        Level currLevel = null;

        if (debugging) {
            if (debugLevelIndex == 1) {
                currLevel = Level.INFO;
            } else if (debugLevelIndex == 2) {
                currLevel = Level.FINE;
            } else if (debugLevelIndex == 3) {
                currLevel = Level.FINER;
            } else if (debugLevelIndex == 4) {
                currLevel = Level.FINEST;
            }
            logger = Logger.getLogger(DEBUG_PKG_NAME);
            // remember original debug level
            origLevel = logger.getLevel();
            logger.setLevel(currLevel);

            // craete a StreamHandler based on ByteArrayOutputStream
            byteArrOS = new ByteArrayOutputStream();
            logHandler = new StreamHandler(byteArrOS, new LogFormatter());
            logHandler.setLevel(currLevel);
            // add to logger
            logger.addHandler(logHandler);
        }

        //verify the input first
        QName rootElement = testerPanel.getSelectedTopElementDecl();
        if (rootElement == null) {
            JOptionPane.showMessageDialog(testerPanel,
                    _bundle.getString("test_performer.lbl.select_top_element"));
            return;
        }

        String type = testerPanel.getActionType();
        File processFile = new File(testerPanel.getProcessFile());

        if (!processFile.exists()) {
            JOptionPane.showMessageDialog(testerPanel,
                    _bundle.getString("test_performer.lbl.enter_process_file"));
            return;
        }

        String oFile = testerPanel.getOutputFile();
        File outputFile = new File(oFile);
        if (testerPanel.getOutputFileName() == null
                || testerPanel.getOutputFileName().equals("")) {  //NOI18N
            //no outputfile
            JOptionPane.showMessageDialog(testerPanel,
                    _bundle.getString("test_performer.lbl.enter_output_file."));
            return;
        }

        /**
         * if outputFile already exists and "overwrite" is unchecked, then
         * output file name is based on the given base name with "_1" suffixed
         * and with the same file extension.
         */
        if (outputFile.exists()) {
            if (!testerPanel.isOverwrite()) {
                String ext = FileUtil.getExtension(outputFile.getAbsolutePath());
                String parent = outputFile.getParent();
                String name = outputFile.getName().replaceAll("." + ext, "");  //NOI18N
                outputFile = new File(parent + File.separatorChar + name + "_1." + ext);  //NOI18N
            }
        }

        boolean result = true;

        try {
            testerPanel.setCursor(Utilities.createProgressCursor(testerPanel));
            if (type.equals(ENCODE)) {
                mEncoderTestTask.encode(mEncoderType, metaFile, rootElement,
                        processFile, outputFile, testerPanel.getPostencodeCoding(),
                        testerPanel.isToString());
            } else {
                // must be DECODE
                mEncoderTestTask.decode(mEncoderType, metaFile, rootElement,
                        processFile, outputFile, testerPanel.getPredecodeCoding(),
                        testerPanel.isFromString());
            }
        } catch (TransformerConfigurationException ex) {
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (final TransformerException ex) {
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (EncoderException ex) { // thrown by both decode and encode
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (IOException ex) { // thrown by both decode and encode
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (EncoderConfigurationException ex) { // thrown by both decode and encode
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (ParserConfigurationException ex) {
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } catch (SAXException ex) {
            displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
            Utils.notify(ex, true, dialog, JOptionPane.ERROR_MESSAGE);
            result = false;
        } finally {
            testerPanel.setCursor(null);
        }

        if (!result) {
            return;
        }

        // dismiss the dialog panel
        dialog.setVisible(false);
        try {
            DataObject dObj = DataLoaderPool.getDefault().
                            findDataObject(FileUtil.toFileObject(outputFile));
            if (dObj != null) {
                // try to edit it.
                EditCookie edit = (EditCookie) dObj.getCookie(EditCookie.class);
                if (edit != null) {
                    edit.edit();
                } else {
                    // if no edit action given, then try to open it.
                    OpenCookie open = (OpenCookie) dObj.getCookie(OpenCookie.class);
                    if (open != null) {
                        open.open();
                    }
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        displayDebugMsgs(debugging, byteArrOS, logHandler, logger, origLevel);
    }

    private void displayDebugMsgs(boolean debugging,
            ByteArrayOutputStream byteArrOS,
            Handler logHandler,
            Logger logger,
            Level origLevel) {
        if (!debugging || logHandler == null || byteArrOS == null
                || logger == null) {
            return;
        }
        // flush the Handler.
        logHandler.flush();
        String title = NbBundle.getMessage(EncoderTestPerformerImpl.class,
            "test_performer.iooutputpane.title", metaFile.getName()); //NOI18N
        boolean newIO = true;
        InputOutput io = IOProvider.getDefault().getIO(title, newIO);
        // Ensure this I/O output pane is visible.
        io.select();
        // now writes to the output pane.
        OutputWriter writer = io.getOut();
        writer.println(byteArrOS.toString());
        try {
            byteArrOS.close();
        } catch (IOException ex) {
            // ignore
        }

        // make sure to close the writer of I/O output pane.
        writer.close();
        // close and remove the Handler.
        logHandler.close();
        logger.removeHandler(logHandler);
        // reset to its original logging level
        logger.setLevel(origLevel);
    }
}
