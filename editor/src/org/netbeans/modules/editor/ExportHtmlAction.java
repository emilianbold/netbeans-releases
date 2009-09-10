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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.awt.HtmlBrowser;
import org.netbeans.editor.*;

import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.prefs.Preferences;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.awt.Mnemonics;

public class ExportHtmlAction extends CookieAction {

    private static final String HTML_EXT = ".html";  //NOI18N
    private static final String OPEN_HTML_HIST = "ExportHtmlAction_open_html_history"; //NOI18N
    private static final String SHOW_LINES_HIST = "ExportHtmlAction_show_lines_history"; //NOI18N
    private static final String SELECTION_HIST = "ExportHtmlAction_selection_history"; //NOI18N
    private static final String FOLDER_NAME_HIST = "ExportHtmlAction_folder_name_history"; //NOI18N
    private static final String CHARSET = "UTF-8"; //NOI18N

    private Dialog dlg;

    public ExportHtmlAction () {
    }

    protected final int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }



    protected final Class[] cookieClasses() {
        return new Class[] {EditorCookie.class};
    }

    protected final void performAction(Node[] activatedNodes) {
        EditorCookie ec = (EditorCookie) activatedNodes[0].getCookie (EditorCookie.class);
        if (ec==null) return;
        StyledDocument doc = null;
        try {
            doc = ec.openDocument();
        } catch (IOException ioe) {
        }
        if (doc instanceof BaseDocument) {
            final BaseDocument bdoc = (BaseDocument) doc;
            final JTextComponent jtc = Utilities.getLastActiveComponent();
            Presenter p = new Presenter ();
            String folderName = (String)EditorState.get(FOLDER_NAME_HIST);
            if (folderName == null)
                folderName = System.getProperty("user.home"); //NOI18N
            p.setFileName (folderName+File.separatorChar+
                    ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile().getName()+HTML_EXT);
            
            MimePath mimePath = jtc == null ? MimePath.EMPTY : MimePath.parse(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc));
            Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            
            Boolean bool = (Boolean)EditorState.get(SHOW_LINES_HIST);            
            boolean showLineNumbers = bool != null ? bool : prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
            p.setShowLines (showLineNumbers);
            
            p.setSelectionActive (jtc != null && jtc.getSelectionStart()!=jtc.getSelectionEnd());

            bool = (Boolean)EditorState.get(SELECTION_HIST);
            boolean selection = (jtc != null && jtc.getSelectionStart()!=jtc.getSelectionEnd()) && (bool != null ? bool.booleanValue() : true);
            p.setSelection (selection);
            
            bool = (Boolean)EditorState.get(OPEN_HTML_HIST);
            boolean setOpen = bool != null ? bool.booleanValue() : false;
            p.setOpenHtml(setOpen);
            
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(ExportHtmlAction.class, "CTL_ExportHtml"));
            boolean overwrite = true;
            dlg = DialogDisplayer.getDefault().createDialog (dd);            
            do{
                dlg.setVisible (true);
                overwrite = true;
                if ( dd.getValue() == DialogDescriptor.OK_OPTION && new File(p.getFileName()).exists()){
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage( org.netbeans.modules.editor.ExportHtmlAction.class, "MSG_FileExists", p.getFileName()),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
                    );

                    org.openide.DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.getValue()!=NotifyDescriptor.YES_OPTION){
                        overwrite = false;
                    }
                }
            }while(!overwrite);
            
            dlg.dispose();
            dlg = null;
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                if (selection != p.isSelection()) {
                    selection = p.isSelection();
                    EditorState.put(SELECTION_HIST, selection ? Boolean.TRUE : Boolean.FALSE);
                }
                final String file = p.getFileName();
                int idx = file.lastIndexOf(File.separatorChar);
                if (idx != -1)
                    EditorState.put(FOLDER_NAME_HIST, file.substring(0, idx));
                final boolean lineNumbers = p.isShowLines();
                if (showLineNumbers != lineNumbers) {
                    EditorState.put(SHOW_LINES_HIST, lineNumbers ? Boolean.TRUE : Boolean.FALSE);
                }
                final boolean open = p.isOpenHtml();
                if (setOpen != open) {
                    EditorState.put(OPEN_HTML_HIST, open ? Boolean.TRUE : Boolean.FALSE);
                }
                final int selectionStart = selection ? jtc.getSelectionStart() : 0;
                final int selectionEnd = selection ? jtc.getSelectionEnd() : bdoc.getLength();
                RequestProcessor.getDefault().post(
                        new Runnable () {
                            public void run () {
                                try {
                                    if (jtc!=null)
                                        this.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    export (bdoc, file, lineNumbers, selectionStart, selectionEnd);
                                    if (open) {
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(new File(file).toURI().toURL());
                                    }
                                } catch (MalformedURLException mue) {
                                        Exceptions.printStackTrace (mue);
                                } catch (IOException ioe) {
                                    NotifyDescriptor nd = new NotifyDescriptor.Message (
                                            NbBundle.getMessage(ExportHtmlAction.class,"ERR_IOError",
                                                    new Object[]{((DataObject)bdoc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile().getNameExt()
                                            +HTML_EXT,file}),    //NOI18N
                                            NotifyDescriptor.ERROR_MESSAGE);
                                    DialogDisplayer.getDefault().notify (nd);
                                    return;
                                }
                                finally {
                                    if (jtc != null) {
                                        this.setCursor (null);
                                    }
                                }
                            }


                            private void setCursor (final Cursor c) {
                                SwingUtilities.invokeLater (new Runnable () {
                                        public void run() {
                                            jtc.setCursor (c);
                                        }
                                    });
                            }
                        }
                );
            }
        }
        else {
            Logger.getLogger("global").log (Level.FINE,NbBundle.getMessage(ExportHtmlAction.class,"MSG_DocError"));
        }
    }

    public final String getName() {
        return NbBundle.getMessage (ExportHtmlAction.class, "CTL_ExportHtmlAction");
    }

    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected @Override final boolean asynchronous() {
        return false;
    }

    private void export (final BaseDocument bdoc,  String fileName, boolean lineNumbers, int selectionStart, int selectionEnd) throws IOException {
        MimePath mimePath = MimePath.parse((String)bdoc.getProperty(BaseDocument.MIME_TYPE_PROP));
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        
        AttributeSet defaultAttribs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Coloring coloring =  Coloring.fromAttributeSet(defaultAttribs);
        Color bgColor = coloring.getBackColor();
        Color fgColor = coloring.getForeColor();
        Font font = coloring.getFont();
        
        AttributeSet lineNumberAttribs = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
        Coloring lineNumberColoring = Coloring.fromAttributeSet(lineNumberAttribs);
        Color lnbgColor = lineNumberColoring.getBackColor();
        Color lnfgColor = lineNumberColoring.getForeColor();
        
        FileObject fo = ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile();
        HtmlPrintContainer htmlPrintContainer = new HtmlPrintContainer();
        htmlPrintContainer.begin (fo, font, fgColor, bgColor,lnfgColor,lnbgColor, mimePath, CHARSET);
        bdoc.print (htmlPrintContainer,false, Boolean.valueOf(lineNumbers), selectionStart, selectionEnd);
        String result = htmlPrintContainer.end();
        PrintWriter out = null;
        try {
            out = new PrintWriter (new OutputStreamWriter (new FileOutputStream (fileName), CHARSET));
            out.print (result);
        } finally {
            if (out != null)
                out.close();
        }
    }


    private class Presenter extends JPanel {

        private JTextField fileName;
        private JCheckBox showLineNumbers;
        private JCheckBox openHtml;
        private JCheckBox selection;

        public Presenter () {
            this.initGUI ();
        }

        public final String getFileName () {
            return this.fileName.getText();
        }

        public final void setFileName (String name) {
            this.fileName.setText (name);
        }

        public final boolean isShowLines () {
            return this.showLineNumbers.isSelected();
        }

        public final void setShowLines (boolean value) {
            this.showLineNumbers.setSelected (value);
        }

        public final boolean isSelection () {
            return this.selection.isSelected();
        }

        public final void setSelection(boolean value) {
            this.selection.setSelected(value);
        }

        public final boolean isOpenHtml () {
            return this.openHtml.isSelected();
        }

        public final void setOpenHtml (boolean value) {
            this.openHtml.setSelected (value);
        }

        public final void setSelectionActive (boolean value) {
            this.selection.setEnabled (value);
        }

        private void initGUI () {
            this.setLayout ( new GridBagLayout ());
            getAccessibleContext().setAccessibleName(NbBundle.getMessage (ExportHtmlAction.class, "ACSN_ExportToHTML")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ExportHtmlAction.class, "ACSD_ExportToHTML")); // NOI18N
            
            JLabel label = new JLabel ();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (ExportHtmlAction.class, "CTL_OutputDir"));
            label.getAccessibleContext().setAccessibleName(NbBundle.getMessage (ExportHtmlAction.class, "AN_OutputDir"));
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ExportHtmlAction.class, "AD_OutputDir"));
            GridBagConstraints c = new GridBagConstraints ();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12,12,6,6);
            ((GridBagLayout)this.getLayout()).setConstraints (label, c);
            this.add (label);
            fileName = new JTextField ();
            fileName.setColumns (25);
            c = new GridBagConstraints ();
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12,6,6,6);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (fileName, c);
            this.add (this.fileName);
            label.setLabelFor (this.fileName);
            JButton button = new JButton ();
            Mnemonics.setLocalizedText(button, NbBundle.getMessage(ExportHtmlAction.class,"CTL_Select"));
            button.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_Select"));
            button.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(ExportHtmlAction.class,"AD_Select"));
            button.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    selectFile ();
                }
            });
            c = new GridBagConstraints ();
            c.gridx = 2;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (12,6,6,12);
            ((GridBagLayout)this.getLayout()).setConstraints (button,c);
            this.add (button);
            selection = new JCheckBox ();
            Mnemonics.setLocalizedText(selection, NbBundle.getMessage(ExportHtmlAction.class, "CTL_Selection"));
            selection.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_Selection"));
            selection.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_Selection"));
            c = new GridBagConstraints ();
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,6,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.selection,c);
            this.add (this.selection);
            showLineNumbers = new JCheckBox ();
            Mnemonics.setLocalizedText(showLineNumbers, NbBundle.getMessage(ExportHtmlAction.class,"CTL_ShowLineNumbers"));
            showLineNumbers.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_ShowLineNumbers"));
            showLineNumbers.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_ShowLineNumbers"));
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill   = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,6,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.showLineNumbers,c);
            this.add (this.showLineNumbers);
            openHtml = new JCheckBox ();
            Mnemonics.setLocalizedText(openHtml, NbBundle.getMessage(ExportHtmlAction.class,"CTL_OpenHTML"));
            openHtml.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class,"AN_OpenHTML"));
            openHtml.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class,"AD_OpenHTML"));
            c = new GridBagConstraints ();
            c.gridx = 1;
            c.gridy = 3;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets (6,6,12,12);
            c.weightx = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints (this.openHtml,c);
            this.add (this.openHtml);
        }


        private void selectFile () {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(ExportHtmlAction.class, "CTL_Browse_Dialog_Title")); // NOI18N
            chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportHtmlAction.class, "ACD_Browse_Dialog")); // NOI18N
            chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportHtmlAction.class, "ACN_Browse_Dialog")); // NOI18N
            chooser.setFileFilter (new javax.swing.filechooser.FileFilter () {
                public boolean accept(File f) {
                    if (f.isFile() && f.getName().endsWith (HTML_EXT) || f.isDirectory()) {
                        return true;
                    }
                    else
                      return false;
                }

                public String getDescription() {
                    return NbBundle.getMessage (ExportHtmlAction.class, "TXT_HTMLFileType"); // NOI18N
                }
            });
            chooser.setSelectedFile (new File (this.fileName.getText()));
            if (chooser.showDialog (dlg, NbBundle.getMessage(ExportHtmlAction.class, "CTL_Approve_Label")) == JFileChooser.APPROVE_OPTION) { // NOI18N
                this.fileName.setText (chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

}
