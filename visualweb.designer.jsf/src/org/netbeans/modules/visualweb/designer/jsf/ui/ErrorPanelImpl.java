/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.designer.jsf.ui;

import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.Util;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLabelUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import javax.xml.parsers.DocumentBuilder;

import org.netbeans.api.diff.Diff;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.NbBundle;

import org.w3c.tidy.Tidy;

import org.xml.sax.EntityResolver;

import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.models.FacesModel;


// XXX Moved from designer/ErrorPanel.


// XXX Moved from designer/ErrorPanel.
/**
 * Panel which shows an error label, and a listbox containing errors
 *
 * @todo Unify this code with the code in ImportPagePanel such that JSP tag
 *   handling etc. is handled correctly in both places. There's already some
 *   deviation in what these do.
 * @todo Trap the case where conversion fails, and tell the user that it failed,
 *   in addition to graying out the buttons.
 *
 * @author  Tor Norbye
 */
public class ErrorPanelImpl extends JPanel
implements JsfForm.ErrorPanel {
    private ParserAnnotation[] errors;
    private DefaultListModel model;
//    private WebForm webform;
    private final FacesModel facesModel;
    private final JsfForm.ErrorPanelCallback errorPanelCallback;
    private ParserAnnotation activated = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton convertButton;
    private javax.swing.JList errorList;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JButton previewButton;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables

    /** Creates new form ErrorPanel */
    public ErrorPanelImpl(/*WebForm webform,*/FacesModel facesModel, final ParserAnnotation[] errors,
    JsfForm.ErrorPanelCallback errorPanelCallback) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + "()");
//        }
//        if(webform == null) {
//            throw(new IllegalArgumentException("Null webform."));
        if(facesModel == null) {
            throw(new IllegalArgumentException("Null FacesModel."));
        }
        if(errors == null) {
            throw(new IllegalArgumentException("Null errors array."));
        }
        if (errorPanelCallback == null) {
            throw new NullPointerException("Null ErrorPanelCallback!"); // NOI18N
        }
        this.errors = errors;
//        this.webform = webform;
        this.facesModel = facesModel;
        this.errorPanelCallback = errorPanelCallback;
        initComponents();
        updateErrors();

        // XXX #100175 Do not hardcode font sizes.
        // But how to provide larger font nicely?
        Font titleFont = jLabel3.getFont();
        if (titleFont != null) {
            int size = titleFont.getSize();
            float newSize = 2 * size;
            Font newFont = titleFont.deriveFont(newSize);
            jLabel3.setFont(newFont);
        }
        
        convertButton.setText(NbBundle.getMessage(ErrorPanelImpl.class, "Convert"));
        previewButton.setText(NbBundle.getMessage(ErrorPanelImpl.class, "Preview"));
        convertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                    convertToXHTML(ErrorPanel.this.webform);
                    convertToXHTML(ErrorPanelImpl.this.facesModel);
                    // XXX See uncommented line at the end of convertToXHTML.
                    ErrorPanelImpl.this.errorPanelCallback.updateTopComponentForErrors();
                }
            });
        previewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                    preview(ErrorPanel.this.webform);
                    preview(ErrorPanelImpl.this.facesModel);
                }
            });

        // XXX what about previewButton?
        textArea.setEnabled(false);

        //textArea.setColor(Color.BLACK);
        textArea.setDisabledTextColor(Color.BLACK);
        textArea.setFont((Font)UIManager.getDefaults().get("Label.font"));

        listScrollPane.setBorder(null); // no border around the error box
        errorList.setCellRenderer(new ErrorCellRenderer());
        errorList.addMouseListener(new MouseAdapter() {
                private ParserAnnotation getSelected(MouseEvent e) {
                    Point p = e.getPoint();
                    int index = errorList.locationToIndex(p);
                    Rectangle bounds = errorList.getCellBounds(index, index);

                    if ((bounds != null) && bounds.contains(p)) {
                        return errors[index];
                    } else {
                        return null;
                    }
                }

                public void mousePressed(MouseEvent e) {
                    // "Activate" hyperlink - make it render highlighted
                    activated = getSelected(e);
                    errorList.repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    // "Deactivate" hyperlink
                    activated = null;
                    errorList.repaint();
                }

                public void mouseClicked(MouseEvent e) {
                    ParserAnnotation selected = getSelected(e);

                    if (selected == null) {
                        return;
                    }

                    FileObject fo = selected.getFileObject();
                    DataObject dobj;

                    try {
                        dobj = DataObject.find(fo);
                    } catch (DataObjectNotFoundException ex) {
                        return;
                    }

                    /* This workaround is hopefully not necessary
                       anymore now that the window system natively
                       supports multiview. This was necessary with
                       our own window manager to ensure that the
                       Source tab was shown.
                    WebForm webform = Utilities.getWebForm(dobj, true);
                    if (webform != null) {
                        TopComponent tc = webform.getSourceView();
                        if (tc != null) {
                            tc.requestActive();
                        }
                    }
                    */
                    LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);

                    if (lc != null) {
                        Line.Set ls = lc.getLineSet();

                        if (ls != null) {
                            // -1: convert line numbers to be zero-based
                            Line line = ls.getCurrent(selected.getLine() - 1);
                            line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                        }
                    }
                }
            });
    }

    public void updateErrors() {
        textArea.setText(NbBundle.getMessage(ErrorPanelImpl.class, "ErrorDescription"));

//        FacesModel model = webform.getModel();
        FacesModel model = facesModel;
        errors = model.getErrors();
        DefaultListModel listModel = new DefaultListModel();

        boolean haveJspError = false;
        for (int i = 0; i < errors.length; i++) {
            listModel.addElement(errors[i]);
            if (errors[i].getFileObject() != null) {
                String extension = errors[i].getFileObject().getExt();
                if (extension != null && extension.indexOf("jsp") != -1) { // includes jspf
                    haveJspError = true;
                }
            }
        }
        convertButton.setEnabled(haveJspError);
        previewButton.setEnabled(haveJspError);

        this.model = listModel;
        errorList.setModel(this.model);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the NetBeans Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        textArea = new javax.swing.JTextArea();
        convertButton = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        listScrollPane = new javax.swing.JScrollPane();
        errorList = new javax.swing.JList();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(java.awt.Color.red);

        jLabel3.setForeground(java.awt.Color.white);
        jLabel3.setText(NbBundle.getMessage(ErrorPanelImpl.class, "SourceFileError")); // NOI18N
        jPanel2.add(jLabel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(textArea, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 11);
        add(convertButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 11);
        add(previewButton, gridBagConstraints);

        jPanel1.setBackground(java.awt.Color.white);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        listScrollPane.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        listScrollPane.setViewportView(errorList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(listScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private static void convertToXHTML(/*WebForm webform*/FacesModel facesModel) {
//        FacesModel model = webform.getModel();
        FacesModel model = facesModel;
        model.flush();

        StyledDocument doc = model.getMarkupUnit().getSourceDocument();

        if (doc == null) {
            return;
        }

        Tidy tidy = getTidy();

        /*
        boolean confirmed = confirmConvert(tidy, doc, webform.getDataObject());
        if (!confirmed) {
            return;
        }
        */
        String rewritten = rewrite(tidy, doc);

        if ((rewritten == null) || (rewritten.length() == 0)) {
            return;
        }

        try {
            // Lock the document atomically
            // TODO: BaseDocument.replace() should do this automatically;
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).atomicLock();
            }

            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).replace(0, doc.getLength(), rewritten, null);
            } else {
                doc.remove(0, doc.getLength());
                doc.insertString(0, rewritten, null);
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            // Lock the document atomically
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).atomicUnlock();
            }
        }

        model.sync();
        
        // XXX See after the only usage of this method.
//        webform.getTopComponent().updateErrors();
    }

    private static String rewrite(Tidy tidy, Document doc) {
        InputStream input = null;

        try {
            String text = doc.getText(0, doc.getLength());
            input = new StringBufferInputStream(text);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);

            return "";
        }

        StringBuffer sb = new StringBuffer(doc.getLength() + 500);

        //OutputStream output = new StringBufferOutputStream(sb);
        OutputStream output = new StringBufferOutputStream(sb);

        // For some reason, just setting the output writer here
        // causes the output window to open! It should be delayed until
        // there's output activity!
        // Write the error message to the output tab:
        //    Add to bundle:
        //    HtmlParseError=Parse Errors
        //InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(ErrorPanel.class, "HtmlParseError"), false);
        //OutputWriter errors = io.getOut();
        //tidy.setErrout(errors);
        // Unfortunately we can't just do Tidy.parseDOM, then call the
        // XMLSerializer on this DOM because the serializer chokes - I haven't
        // debugged it but I suspect the Tidy dom has issues. I noticed the
        // same thing when trying to call importNode on tidy nodes (see
        // ImportPagePanel for details)
        //org.w3c.dom.Document document = tidy.parseDOM(input, output);
        //if (document == null) {
        //    return "";
        //} else {
        // So instead we tidy out to text, then reparse with xerces and
        // serialize that!
        boolean escape =
            tidy.getConfiguration().outputJspMode && !tidy.getConfiguration().inputJspMode;
        tidy.parse(new Tidy.EntityWrapperInputStream(input),
            new Tidy.EntityWrapperOutputStream(output, escape));

        String tidied = sb.toString();

        if (tidied.length() == 0) {
            return tidied;
        } else {
            org.w3c.dom.Document xercesDom = null;

            try {
                org.xml.sax.InputSource is2 = new org.xml.sax.InputSource(new StringReader(tidied));

                // We won't keep this DOM so there's no reason to enable
                // CSS on it
                boolean css = false;
                DocumentBuilder parser = MarkupService.createRaveSourceDocumentBuilder(css);

                parser.setEntityResolver(new EntityResolver() {
                        public org.xml.sax.InputSource resolveEntity(String pubid, String sysid) {
                            return new org.xml.sax.InputSource(new ByteArrayInputStream(new byte[0]));
                        }
                    });

                xercesDom = parser.parse(is2);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

                return null;
            } catch (org.xml.sax.SAXException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

                return null;
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

                return null;
            }

            if (xercesDom == null) {
                return "";
            }

//            String result = InSyncService.getProvider().getHtmlStream(xercesDom);
            String result = Util.getHtmlStream(xercesDom);

            return result;
        }
    }

    // XXX Copy from ImportPagePanel (now in project/importpage module).
    private static class StringBufferOutputStream extends OutputStream {
        private StringBuffer sb;

        public StringBufferOutputStream(StringBuffer sb) {
            this.sb = sb;
        }

        public void write(int b) {
            sb.append((char)b);
        }
    }
    
    
    private static void preview(/*WebForm webform*/FacesModel facesModel) {
//        FacesModel model = webform.getModel();
        FacesModel model = facesModel;
        model.flush();

        StyledDocument doc = model.getMarkupUnit().getSourceDocument();

        if (doc == null) {
            return;
        }

        Tidy tidy = getTidy();
//        diff(tidy, doc, webform.getDataObject());
        // See DomProviderImpl.getJspDataObject();
        FileObject file = model.getMarkupFile();
        DataObject dobj;
        try {
            dobj = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            dobj = null;
        }
        diff(tidy, doc, dobj);
    }

    /** Preview the converted source, and return true if the user
     * wants to continue, false otherwise. */

    //private boolean confirmConvert(Tidy tidy, Document doc, DataObject dobj) {
    private static boolean diff(Tidy tidy, Document doc, DataObject dobj) {
        String before;

        try {
            before = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);

            return false;
        }

        String after = rewrite(tidy, doc);
        String mime = dobj.getPrimaryFile().getMIMEType();

        if ((after == null) || (after.length() == 0)) {
            ErrorManager.getDefault().log("Somehow got empty diff output");

            return false;
        }

        Diff diff = Diff.getDefault();

        if (diff == null) {
            // TODO Check for this condition and hide the Diff button
            // if this is the case
            return false;
        }

        String beforeDesc = NbBundle.getMessage(ErrorPanelImpl.class, "DiffBefore"); // NOI18N
        String afterDesc = NbBundle.getMessage(ErrorPanelImpl.class, "DiffAfter"); // NOI18N
        String beforeTitle = beforeDesc;
        String afterTitle = afterDesc;

        Component tp = null;

        try {
            tp = diff.createDiff(beforeDesc, beforeTitle, new StringReader(before), afterDesc,
                    afterTitle, new StringReader(after), mime);
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);

            return false;
        }

        if (tp == null) {
            return false;
        }

        //NotifyDescriptor d =
        // new NotifyDescriptor.Message("Hello...", NotifyDescriptor.INFORMATION_MESSAGE);
        // TopManager.getDefault().notify(d);
        DialogDescriptor d =
            new DialogDescriptor(tp, NbBundle.getMessage(ErrorPanelImpl.class, "TITLE_diff")); // NOI18N
        d.setModal(true);
        d.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(d);
        dlg.pack();
//        dlg.show();
        dlg.setVisible(true);

        // Do OK/Cancel thingy here
        return d.getValue() == NotifyDescriptor.OK_OPTION;
    }

    private static Tidy getTidy() {
        // Set configuration settings
        Tidy tidy = new Tidy();
        tidy.getConfiguration().outputJspMode = true;
        tidy.getConfiguration().inputJspMode = true;
        tidy.setOnlyErrors(false);
        tidy.setShowWarnings(false);
        tidy.setQuiet(true);

        // XXX Apparently JSP pages (at least those involving
        // JSF) need XML handling in order for JTidy not to choke on them
        //tidy.setXmlTags(true);
        tidy.setXmlTags(false);

        tidy.setXHTML(true); // XXX ?

        //tidy.setMakeClean(panel.getReplace());
        //tidy.setIndentContent(panel.getIndent());
        //tidy.setSmartIndent(panel.getIndent());
        //tidy.setUpperCaseTags(panel.getUpper());
        //tidy.setHideEndTags(panel.getOmit());
        //tidy.setWraplen(panel.getWrapCol());
        return tidy;
    }

    // Give the errors in the list a "hyperlink" look
    private class ErrorCellRenderer extends DefaultListCellRenderer {
        ErrorCellRenderer() {
            setUI(new HyperlinkLabelUI());
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            cellHasFocus = false; // no focus-feedback for the hyperlink

            Component c =
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            assert c == this; // if not, gotta cast and call the below methods on c.

            if (value instanceof ParserAnnotation) {
                ParserAnnotation p = (ParserAnnotation)value;
                String filename = p.getFileObject().getNameExt();
                int baseIndex = filename.lastIndexOf('/');
                if (baseIndex != -1) {
                    filename = filename.substring(baseIndex);
                }
                setText(NbBundle.getMessage(ErrorPanelImpl.class, "LineFormat",
                        filename,
                        Integer.toString(p.getLine()), p.getMessage()));

                if (p.getIcon() != null) {
                    setIcon(p.getIcon());
                }

                if (p == activated) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLUE);
                }

                setBackground(list.getBackground());
            }

            return c;
        }
    }

    // Paint underlined labels
    private static final class HyperlinkLabelUI extends MetalLabelUI {
        protected void paintEnabledText(JLabel l, Graphics g, String text, int textX, int textY) {
            super.paintEnabledText(l, g, text, textX, textY);

            FontMetrics fm = g.getFontMetrics();
            int underlineRectX = textX;
            int underlineRectY = textY;
            int underlineRectWidth = fm.stringWidth(text);
            int underlineRectHeight = 1;
            g.fillRect(underlineRectX, underlineRectY + 1, underlineRectWidth, underlineRectHeight);
        }
    }

    /*
    // Grr... tidy uses input/output stream instead of input/output writer
    private class StringBufferOutputStream extends OutputStream {
        private StringBuffer sb;
        StringBufferOutputStream(StringBuffer sb) {
            this.sb = sb;
        }

        public void write(int b) {
            sb.append((char)b);
        }
    }
    */
}
