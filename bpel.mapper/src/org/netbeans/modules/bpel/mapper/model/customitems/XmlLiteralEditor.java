/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.mapper.model.customitems;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.plaf.basic.BasicBorders;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.bpel.model.api.Literal.LiteralForm;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  nk160297
 */
public class XmlLiteralEditor extends EditorLifeCycleAdapter
        implements Validator.Provider, ValidStateManager.Provider {

    private XmlLiteralDataObject mDataObject;
    private DefaultValidator mValidator;
    private ValidStateManager mVSM;

    public XmlLiteralEditor(XmlLiteralDataObject mDataObject, String value) {
        if (mDataObject == null) {
            this.mDataObject = new XmlLiteralDataObject(null, value, LiteralForm.EMPTY);
        } else {
            this.mDataObject = mDataObject;
        }
        initComponents();
        initControls();

        jEditorPane1.setText(value);
//        Action action = jTextArea1.getKeymap().getAction(KeyStroke.getKeyStroke(KeyEvent.VK_F,
//                KeyEvent.SHIFT_DOWN_MASK + KeyEvent.ALT_DOWN_MASK));
//        if (action != null) {
//            action.actionPerformed(new ActionEvent(jTextArea1, 0, "format"));
//        }
    }

    @Override
    public void createContent() {
//        assert EventQueue.isDispatchThread();
//        mMapper = PredicatesMapperFactory.createMapper(mMapperModel);
//        initComponents();
//        //
//        mDlgTitle = NbBundle.getMessage(PredicateEditor.class,
//            "PREDICATE_DLG_TITLE"); // NOI18N
//        //
//        btnGoToContext.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                if (mSContext != null) {
//                    showSchemaContextInSourceTree();
//                }
//            }
//        });
//        SoaUtil.activateInlineMnemonics(this);

        //
        getValidStateManager(true).addValidStateListener(
                new ValidStateListener() {

                    public void stateChanged(ValidStateManager source, boolean isValid) {
                        if (source.isValid()) {
//                          lblErrorMessage.setText("");
                        } else {
//                          lblErrorMessage.setText(source.getHtmlReasons());
//                          lblErrorMessage.setText("stateChanged");
                        }
                    }
                });
    }

    public XmlLiteralDataObject getXmlDataObject() {
        return mDataObject;
    }

    public LiteralForm getCurrentLiteralForm() {
        if (jEditorPane1.getText() == null || jEditorPane1.getText().length() == 0) {
            return LiteralForm.EMPTY;
        }
        if (buttonGroup1.isSelected(rbntText.getModel())) {
            return LiteralForm.TEXT_CONTENT;
        }
        if (buttonGroup1.isSelected(rbtnCData.getModel())) {
            return LiteralForm.CDATA_SUBELEMENT;
        }
        if (buttonGroup1.isSelected(rbtnXmlSubelement.getModel())) {
            return LiteralForm.SUBELEMENT;
        }

        return null;
    }

    @Override
    public boolean initControls() {
        if (mDataObject == null) {
            return true;
        }
        //
//        DefaultValidator validator = getValidator();
        LiteralForm literalForm = mDataObject.getLiteralForm();
        if (literalForm != null) {

            switch (literalForm) {
                case EMPTY:
                    // nothing to do
                    break;
                case CDATA_SUBELEMENT: {
                    buttonGroup1.setSelected(rbtnCData.getModel(), true);
                    jEditorPane1.setContentType("text/xml");
                    break;
                }
                case TEXT_CONTENT: {
                    buttonGroup1.setSelected(rbntText.getModel(), true);
                    jEditorPane1.setContentType("text/plain");
                    break;
                }
                case SUBELEMENT: {
                    buttonGroup1.setSelected(rbtnXmlSubelement.getModel(), true);
                    jEditorPane1.setContentType("text/xml");
//                    jTextArea1.getInputMap();
                    break;
                }
            }

        }
        //      DocumentBuilder b = new DocumentBuilder();

        //
        return true;
    }

    /**
     * Returns true if the user press Ok
     * @param editor
     * @return
     */
    public static boolean showDlg(XmlLiteralEditor editor) {
        String dlgTitle = NbBundle.getMessage(XmlLiteralEditor.class,
                "XML_LITERAL_DLG_TITLE"); // NOI18N
        //
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(editor, dlgTitle);
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUtil.setInitialFocusComponentFor(editor);
        dialog.setVisible(true);
        //
        if (descriptor.isOkHasPressed()) {
            editor.getXmlDataObject().setLiteralForm(editor.getCurrentLiteralForm());
            editor.getXmlDataObject().setTextContent(editor.getXmlText());
        }

        return descriptor.isOkHasPressed();
    }

    public ValidStateManager getValidStateManager(boolean isFast) {
        if (isFast) {
            // Only detailed validation is supported here
            return null;
        }
        if (mVSM == null) {
            mVSM = new DefaultValidStateManager();
        }
        return mVSM;
    }

    public DefaultValidator getValidator() {
        if (mValidator == null) {
            mValidator = new DefaultValidator(
                    (ValidStateManager.Provider) XmlLiteralEditor.this,
                    XmlLiteralEditor.class) {

                public void doFastValidation() {
                }

                @Override
                public void doDetailedValidation() {
                    if (getCurrentLiteralForm() != LiteralForm.SUBELEMENT) {
                        return;
                    }

                    InputSource iSource = new InputSource(new StringReader(getXmlText()));
                    try {
                        DocumentBuilderFactory.newInstance().
                                newDocumentBuilder().parse(iSource);
                    } catch (SAXException ex) {
                        addReasonKey(Severity.ERROR, "XML_LITERAL_ERROR");
//                      lblErrorMessage.setText(ex.getMessage());
                    } catch (IOException ex) {
                        addReasonKey(Severity.ERROR, "XML_LITERAL_ERROR");
//                      lblErrorMessage.setText(ex.getMessage());
                    } catch (ParserConfigurationException ex) {
                        addReasonKey(Severity.ERROR, "XML_LITERAL_ERROR");
//                      lblErrorMessage.setText(ex.getMessage());
                    }
                }
            };
        }
        return mValidator;
    }

    public String getXmlText() {
        return jEditorPane1.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        Action action = new ChangeTypeAction();
        buttonGroup1 = new javax.swing.ButtonGroup();
        rbntText = new javax.swing.JRadioButton(action);
        rbtnCData = new javax.swing.JRadioButton(action);
        rbtnXmlSubelement = new javax.swing.JRadioButton(action);
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        bntLoadFromFile = new javax.swing.JButton(new OpenFileAction());
        lblErrorMessage = new javax.swing.JLabel();

        buttonGroup1.add(rbntText);
        rbntText.setSelected(true);
        rbntText.setText(org.openide.util.NbBundle.getMessage(XmlLiteralEditor.class, "RBNT_Text")); // NOI18N

        buttonGroup1.add(rbtnCData);
        rbtnCData.setText(org.openide.util.NbBundle.getMessage(XmlLiteralEditor.class, "RBTN_CDATA")); // NOI18N

        buttonGroup1.add(rbtnXmlSubelement);
        rbtnXmlSubelement.setText(org.openide.util.NbBundle.getMessage(XmlLiteralEditor.class, "RBTN_XmlElement")); // NOI18N

        jScrollPane1.setViewportView(jEditorPane1);

        bntLoadFromFile.setText(org.openide.util.NbBundle.getMessage(XmlLiteralEditor.class, "BTN_Load")); // NOI18N

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
 
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).
                add(layout.createSequentialGroup().addContainerGap().
                add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).
                add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE).
                add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE).
                add(layout.createSequentialGroup().add(rbntText).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).
                add(rbtnCData).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).
                add(rbtnXmlSubelement).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 71, Short.MAX_VALUE).
                add(bntLoadFromFile))).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).
                add(layout.createSequentialGroup().addContainerGap().
                add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(rbntText).
                add(rbtnCData).add(bntLoadFromFile).add(rbtnXmlSubelement)).
                addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).
                add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE).
                addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).
                add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
    }// </editor-fold>
    // Variables declaration - do not modify
    private javax.swing.JButton bntLoadFromFile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JRadioButton rbntText;
    private javax.swing.JRadioButton rbtnCData;
    private javax.swing.JRadioButton rbtnXmlSubelement;
    // End of variables declaration

    private class ChangeTypeAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == rbntText) {
                String text = jEditorPane1.getText();
                jEditorPane1.setContentType("text/plain");
                jEditorPane1.setText(text);
            }
            if (e.getSource() == rbtnCData) {
                String text = jEditorPane1.getText();
                jEditorPane1.setContentType("text/plain");
                jEditorPane1.setText(text);

            }
            if (e.getSource() == rbtnXmlSubelement) {
                String text = jEditorPane1.getText();
                jEditorPane1.setContentType("text/xml");
                jEditorPane1.setText(text);
            }
            mDataObject.setLiteralForm(getCurrentLiteralForm());
        }
    }

    private class OpenFileAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fChooser = new JFileChooser();
            int res = fChooser.showOpenDialog(XmlLiteralEditor.this);
            if (res == JFileChooser.APPROVE_OPTION) {
                FileReader f;
                StringBuilder buf =new StringBuilder();
                try {
                    f = new FileReader(fChooser.getSelectedFile());
                    BufferedReader bufReader = new BufferedReader(f);
                    String line = null;
                    while ((line = bufReader.readLine()) != null) {
                        buf.append(line);
                        buf.append("\n");
                    }

                    XmlLiteralEditor.this.jEditorPane1.setText(buf.toString());
                    bufReader.close();
                    f.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
