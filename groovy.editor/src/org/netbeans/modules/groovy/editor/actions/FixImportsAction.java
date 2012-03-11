/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm, Martin Janicek
 */
@EditorActionRegistration(
    name             = FixImportsAction.ACTION_NAME,
    mimeType         = "text/x-groovy",
    shortDescription = "Fixes import statements.",
    popupText        = "Fix Imports..."
)
public class FixImportsAction extends BaseAction {

    protected static final String ACTION_NAME = "fix-groovy-imports"; //NOI18N
    private static final Logger LOG = Logger.getLogger(FixImportsAction.class.getName());

    private final List<String> missingNames;
    private final AtomicBoolean cancel;
    
    private DataObject dob;
    private FileObject fo;
    private Source source;
    

    public FixImportsAction() {
        super(MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        
        missingNames = new ArrayList<String>();
        cancel = new AtomicBoolean();
    }

    @Override
    public boolean isEnabled() {
        // here should go all the logic whether there are in fact missing
        // imports we're able to fix.
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        init(target.getDocument());

        try {
            ParserManager.parse(Collections.singleton(source), new CollectMissingImportsTask());
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        // go over list of missing imports, fix it - if there is only one candidate
        // or populate choosers input list if there is more than one candidate.

        final List<String> singleCandidates = new ArrayList<String>();
        final Map<String, List<ImportCandidate>> multipleCandidates = new HashMap<String, List<ImportCandidate>>();

        for (String name : missingNames) {
            List<ImportCandidate> importCandidates = FixImportsHelper.getImportCandidate(fo, name);

            switch (importCandidates.size()) {
                case 0: continue;
                case 1: singleCandidates.add(importCandidates.get(0).getFqnName()); break;
                default: multipleCandidates.put(name, importCandidates);
            }
        }

        // do we have multiple candidate? In this case we need to present a chooser

        if (!multipleCandidates.isEmpty()) {
            List<String> choosenCandidates = showFixImportChooser(multipleCandidates);
            singleCandidates.addAll(choosenCandidates);
        }

        if (!singleCandidates.isEmpty()) {
            Collections.sort(singleCandidates);
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    FixImportsHelper.doImports(fo, singleCandidates);
                }
            }, "Fix All Imports", cancel, false);
        }
    }

    private void init(Document document) {
        dob = NbEditorUtilities.getDataObject(document);
        fo = dob.getPrimaryFile();
        source = Source.create(fo);
    }

    private class CollectMissingImportsTask extends UserTask {

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult result = AstUtilities.getParseResult(resultIterator.getParserResult());
            if (result != null) {
                ErrorCollector errorCollector = result.getErrorCollector();
                if (errorCollector == null) {
                    return;
                }
                List errors = errorCollector.getErrors();
                if (errors == null) {
                    return;
                }

                collectMissingImports(errors);
            }
        }

        private void collectMissingImports(List errors) {
            for (Object error : errors) {
                if (error instanceof SyntaxErrorMessage) {
                    SyntaxException se = ((SyntaxErrorMessage) error).getCause();

                    if (se != null) {
                        String missingClassName = FixImportsHelper.getMissingClassName(se.getMessage());

                        if (missingClassName != null) {
                            if (!missingNames.contains(missingClassName)) {
                                missingNames.add(missingClassName);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<String> showFixImportChooser(Map<String, List<ImportCandidate>> multipleCandidates) {
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
}
