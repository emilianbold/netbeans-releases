/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.groovy.editor.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.api.EditorAction;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.groovy.editor.NbUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.groovy.editor.actions.FixImportsHelper.ImportCandidate;
import org.netbeans.modules.groovy.editor.parser.GroovyParser;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;

/**
 *
 * @author schmidtm
 */
public class FixImportsAction extends AbstractAction implements EditorAction, Runnable {

    private final Logger LOG = Logger.getLogger(FixImportsAction.class.getName());
    String MENU_NAME = NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString");
    Document doc = null;
    private FixImportsHelper helper = new FixImportsHelper();

    public FixImportsAction() {
        super(NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString"));
        putValue("PopupMenuText", MENU_NAME);
    // LOG.setLevel(Level.FINEST);
    }

    @Override
    public boolean isEnabled() {
        // here should go all the logic whether there are in fact missing 
        // imports we're able to fix.
        return true;
    }

    void actionPerformed(final JTextComponent comp) {
        LOG.log(Level.FINEST, "actionPerformed(final JTextComponent comp)");

        assert comp != null;
        doc = comp.getDocument();

        if (doc != null) {
            RequestProcessor.getDefault().post(this);
        }
    }

    GroovyParserResult getParserResult(final FileObject fo) {

        DefaultParseListener listener = new DefaultParseListener();
        TranslatedSource translatedSource = null;
        ParserFile parserFile = new DefaultParserFile(fo, null, false);
        List<ParserFile> files = Collections.singletonList(parserFile);

        SourceFileReader reader =
                new SourceFileReader() {

                    public CharSequence read(ParserFile file)
                            throws IOException {
                        Document doc = NbUtilities.getBaseDocument(fo, true);

                        if (doc == null) {
                            return "";
                        }

                        try {
                            return doc.getText(0, doc.getLength());
                        } catch (BadLocationException ble) {
                            IOException ioe = new IOException();
                            ioe.initCause(ble);
                            throw ioe;
                        }
                    }

                    public int getCaretOffset(ParserFile fileObject) {
                        return -1;
                    }
                };


        Parser.Job job = new Parser.Job(files, listener, reader, translatedSource);
        new GroovyParser().parseFiles(job);

        return (GroovyParserResult) listener.getParserResult();

    }

    public void run() {
        DataObject dob = NbEditorUtilities.getDataObject(doc);

        if (dob == null) {
            LOG.log(Level.FINEST, "Could not get DataObject for document");
            return;
        }

        FileObject fo = dob.getPrimaryFile();
        GroovyParserResult result = getParserResult(fo);

        if (result == null) {
            LOG.log(Level.FINEST, "Could not get GroovyParserResult");
            return;
        }

        ErrorCollector errorCollector = result.getErrorCollector();
        List errList = errorCollector.getErrors();

        if (errList == null) {
            LOG.log(Level.FINEST, "Could not get list of errors");
            return;
        }

        List<String> missingNames = new ArrayList<String>();

        // loop over the list of errors, remove duplicates and 
        // populate list of missing imports.

        for (Object error : errList) {
            if (error instanceof SyntaxErrorMessage) {
                SyntaxException se = ((SyntaxErrorMessage) error).getCause();
                if (se != null) {
                    String missingClassName = helper.getMissingClassName(se.getMessage());

                    if (missingClassName != null) {
                        if (!missingNames.contains(missingClassName)) {
                            missingNames.add(missingClassName);
                        }
                    }
                }
            }
        }

        // go over list of missing imports, fix it - if there is only one 
        // candidate or populate choosers input list.

        Map<String, List> multipleCandidates = new HashMap<String, List>();

        for (String name : missingNames) {
            List<ImportCandidate> importCandidates = helper.getImportCandidate(fo, name);

            if (importCandidates.isEmpty()) {
                // nothing to import
                return;
            }

            int size = importCandidates.size();

            if (size == 1) {
                helper.doImport(fo, importCandidates.get(0).getFqnName());
            } else {
                LOG.log(Level.FINEST, "Adding to multipleCandidates: " + name);
                multipleCandidates.put(name, importCandidates);
            }
        }

        // do we have multiple candidate? In this case we need to present a
        // chooser

        List<String> listToFix = null;

        if (!multipleCandidates.isEmpty()) {
            LOG.log(Level.FINEST, "multipleCandidates.size(): " + multipleCandidates.size());
            listToFix = presentChooser(multipleCandidates);
        }

        if (listToFix != null && !listToFix.isEmpty()) {
            LOG.log(Level.FINEST, "listToFix.size(): " + listToFix.size());
            for (String fqn : listToFix) {
                helper.doImport(fo, fqn);
            }
        }

        return;
    }

    private List<String> presentChooser(Map<String, List> multipleCandidates) {
        LOG.log(Level.FINEST, "presentChooser()");
        List<String> result = new ArrayList<String>();
        ImportChooserInnerPanel panel = new ImportChooserInnerPanel();

        panel.initPanel(multipleCandidates);

        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(FixImportsAction.class, "FixImportsDialogTitle")); //NOI18N
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.setVisible(true);
        d.setVisible(false);
        d.dispose();

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            result = panel.getSelections();
        }

        return result;
    }

    public void actionPerformed(ActionEvent e) {
        LOG.log(Level.FINEST, "actionPerformed(ActionEvent e)");

        JTextComponent pane = NbUtilities.getOpenPane();

        LOG.log(Level.FINEST, "NAME               : " + NAME + " : " + getValue(NAME));
        LOG.log(Level.FINEST, "ACCELERATOR_KEY    : " + ACCELERATOR_KEY + " : " + getValue(ACCELERATOR_KEY));
        LOG.log(Level.FINEST, "ACTION_COMMAND_KEY : " + ACTION_COMMAND_KEY + " : " + getValue(ACTION_COMMAND_KEY));

        if (pane != null) {
            actionPerformed(pane);
        }

        return;
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        LOG.log(Level.FINEST, "actionPerformed(ActionEvent evt, JTextComponent target)");
        return;
    }

    public String getActionName() {
        return NbBundle.getMessage(FixImportsAction.class, "FixImportsActionName");
    }

    public Class getShortDescriptionBundleClass() {
        return FixImportsAction.class;
    }
}
