/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.editor;

import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
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
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;

public class ExportHtmlAction extends CookieAction {

    private static final String HTML_EXT = ".html";  //NOI18N
    private static final String FILE_PROTOCOL = "file://"; //NOI18N

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
        StyledDocument doc = null;
        try {
            doc = ec.openDocument();
        } catch (IOException ioe) {
        }
        if (doc instanceof BaseDocument) {
            final BaseDocument bdoc = (BaseDocument) doc;
            final JTextComponent jtc = Utilities.getLastActiveComponent();
            Presenter p = new Presenter ();
            p.setFileName (System.getProperty("user.home")+File.separatorChar+              //NOI18N
                    ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile().getName()+HTML_EXT);
            p.setShowLines (((Boolean)SettingsUtil.getValue (bdoc.getKitClass(),SettingsNames.LINE_NUMBER_VISIBLE,
                    Boolean.FALSE)).booleanValue());
            p.setSelectionActive ((jtc != null && jtc.getSelectionStart()!=jtc.getSelectionEnd()));
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(ExportHtmlAction.class, "CTL_ExportHtml"));
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible (true);
            dlg.dispose();
            dlg = null;
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                boolean selection = p.isSelection();
                final String file = p.getFileName();
                final boolean lineNumbers = p.isShowLines();
                final boolean open = p.isOpenHtml();
                final int selectionStart = selection ? jtc.getSelectionStart() : 0;
                final int selectionEnd = selection ? jtc.getSelectionEnd() : bdoc.getLength();
                RequestProcessor.getDefault().post(
                        new Runnable () {
                            public void run () {
                                try {
                                    if (jtc!=null)
                                        this.setCursor (org.openide.util.Utilities.createProgressCursor (jtc));
                                    export (bdoc, file, lineNumbers, selectionStart, selectionEnd);
                                    if (open) {
                                        HtmlBrowser.URLDisplayer.getDefault().showURL (new URL (FILE_PROTOCOL+file));
                                    }
                                } catch (MalformedURLException mue) {
                                        ErrorManager.getDefault().notify (mue);
                                } catch (IOException ioe) {
                                    NotifyDescriptor nd = new NotifyDescriptor.Message (
                                            MessageFormat.format (NbBundle.getMessage(ExportHtmlAction.class,"ERR_IOError"),
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
            ErrorManager.getDefault().log (NbBundle.getMessage(ExportHtmlAction.class,"MSG_DocError."));
        }
    }

    public final String getName() {
        return NbBundle.getMessage (ExportHtmlAction.class, "CTL_ExportHtmlAction");
    }

    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected final boolean asynchronous() {
        return false;
    }

    private void export (final BaseDocument bdoc,  String fileName, boolean lineNumbers, int selectionStart, int selectionEnd) throws IOException {
        Coloring coloring =  SettingsUtil.getColoring (bdoc.getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        Color bgColor = coloring.getBackColor();
        Color fgColor = coloring.getForeColor();
        Font font = coloring.getFont();
        coloring = SettingsUtil.getColoring (bdoc.getKitClass(), SettingsNames.LINE_NUMBER_COLORING, false);
        Color lnbgColor = coloring.getBackColor();
        Color lnfgColor = coloring.getForeColor();
        FileObject fo = ((DataObject)bdoc.getProperty (Document.StreamDescriptionProperty)).getPrimaryFile();
        HtmlPrintContainer htmlPrintContainer = new HtmlPrintContainer();
        htmlPrintContainer.begin (fo, font, fgColor, bgColor,lnfgColor,lnbgColor);
        bdoc.print (htmlPrintContainer,false, lineNumbers, selectionStart, selectionEnd);
        String result = htmlPrintContainer.end();
        PrintWriter out = null;
        try {
            out = new PrintWriter (new FileWriter (fileName));
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

        public final boolean isOpenHtml () {
            return this.openHtml.isSelected();
        }

        public final void setSelectionActive (boolean value) {
            this.selection.setEnabled (value);
        }

        private void initGUI () {
            this.setLayout ( new GridBagLayout ());
            JLabel label = new JLabel (NbBundle.getMessage (ExportHtmlAction.class, "CTL_OutputDir"));
            label.setDisplayedMnemonic (NbBundle.getMessage (ExportHtmlAction.class, "MNE_OutputDir").charAt(0));
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
            JButton button = new JButton (NbBundle.getMessage(ExportHtmlAction.class,"CTL_Select"));
            button.setMnemonic (NbBundle.getMessage (ExportHtmlAction.class, "MNE_Select").charAt(0));
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
            selection = new JCheckBox (NbBundle.getMessage(ExportHtmlAction.class, "CTL_Selection"));
            selection.setMnemonic(NbBundle.getMessage(ExportHtmlAction.class,"MNE_Selection").charAt(0));
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
            showLineNumbers = new JCheckBox (NbBundle.getMessage(ExportHtmlAction.class,"CTL_ShowLineNumbers"));
            showLineNumbers.setMnemonic(NbBundle.getMessage(ExportHtmlAction.class,"MNE_ShowLineNumbers").charAt(0));
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
            openHtml = new JCheckBox (NbBundle.getMessage(ExportHtmlAction.class,"CTL_OpenHTML"));
            openHtml.setMnemonic(NbBundle.getMessage(ExportHtmlAction.class,"MNE_OpenHTML").charAt(0));
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
            chooser.setFileFilter (new javax.swing.filechooser.FileFilter () {
                public boolean accept(File f) {
                    if (f.isFile() && f.getName().endsWith (HTML_EXT) || f.isDirectory()) {
                        return true;
                    }
                    else
                      return false;
                }

                public String getDescription() {
                    return NbBundle.getMessage (ExportHtmlAction.class, "TXT_HTMLFileType");
                }
            });
            chooser.setSelectedFile (new File (this.fileName.getText()));
            if (chooser.showOpenDialog (dlg) == JFileChooser.APPROVE_OPTION) {
                this.fileName.setText (chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

}
