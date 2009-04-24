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

package org.netbeans.modules.maven.hints.pom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public final class StatusProvider implements UpToDateStatusProviderFactory {

    private static final String LAYER_POM = "pom"; //NOI18N
    private static final String LAYER_POM_SELECTION = "pom-selection"; //NOI18N

    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        FileObject fo = NbEditorUtilities.getFileObject(document);
        if (fo != null && "text/x-maven-pom+xml".equals(fo.getMIMEType())) { //NOI18N
            //workaround for wrong mimetype registration.
            return new StatusProviderImpl(document);
        }
        return null;
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private Document document;
        private POMModel model;
        private Project project;
        private FileChangeListener listener;

        StatusProviderImpl(Document doc) {
            this.document = doc;
            listener = new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            checkHints();
                        }
                    });
                }
            };
            initializeModel();
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    checkHints();
                }
            });
        }


        private void checkHints() {
            HintsController.setErrors(document, LAYER_POM, findHints(model, project));
        }

        static List<ErrorDescription> findHints(POMModel model, Project project) {
            if (!model.getModelSource().isEditable()) {
                return new ArrayList<ErrorDescription>();
            }
            assert model != null;
            try {
                model.sync();
                model.refresh();
            } catch (IOException ex) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Errror while syncing pom model.", ex);
            }
            List<ErrorDescription> err = new ArrayList<ErrorDescription>();
            if (!model.getState().equals(Model.State.VALID)) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Pom model document is not valid, is " + model.getState());
                return err;
            }
            if (model.getProject() == null) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Pom model root element missing");
                return err;
            }
            Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
            Lookup.Result<POMErrorFixProvider> res = lkp.lookupResult(POMErrorFixProvider.class);
            for (POMErrorFixProvider prov : res.allInstances()) {
                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                    continue;
                }
               List<ErrorDescription> lst = prov.getErrorsForDocument(model, project);
               if (lst != null) {
                   err.addAll(lst);
               }
            }
            return err;
        }

        private void initializeModel() {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo != null) {
                ModelSource ms = Utilities.createModelSource(fo);
                model = POMModelFactory.getDefault().getModel(ms);
                model.setAutoSyncActive(false);
                project = FileOwnerQuery.getOwner(fo);
                fo.addFileChangeListener(FileUtil.weakFileChangeListener(listener, fo));
            }
        }

        static List<ErrorDescription> findHints(POMModel model, Project project, int selectionStart, int selectionEnd) {
            if (!model.getModelSource().isEditable()) {
                return new ArrayList<ErrorDescription>();
            }
            try {
                model.sync();
                model.refresh();
            } catch (IOException ex) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Errror while syncing pom model.", ex);
            }
            List<ErrorDescription> err = new ArrayList<ErrorDescription>();
            if (!model.getState().equals(Model.State.VALID)) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Pom model document is not valid, is " + model.getState());
                return err;
            }
            if (model.getProject() == null) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Pom model root element missing");
                return err;
            }

            Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
            Lookup.Result<SelectionPOMFixProvider> res = lkp.lookupResult(SelectionPOMFixProvider.class);
            for (SelectionPOMFixProvider prov : res.allInstances()) {
                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                    continue;
                }
                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project, selectionStart, selectionEnd);
                if (lst != null) {
                    err.addAll(lst);
                }
            }
            return err;
        }


        @Override
        public UpToDateStatus getUpToDate() {
//            if (!checkHints()) {
//                System.out.println("skipped checking hints");
//                return UpToDateStatus.UP_TO_DATE_DIRTY;
//            }

            //this condition is important in order not to break any running hints
            //the model sync+refresh renders any existing POMComponents people
            // might be holding useless
            if (!model.isIntransaction()) {
                FileObject fo = NbEditorUtilities.getFileObject(document);
                boolean ok = false;
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie ed = dobj.getCookie(EditorCookie.class);
                    if (ed != null) {
                        JEditorPane[] panes = ed.getOpenedPanes();
                        if (panes != null && panes.length > 0 && panes[0].getSelectionStart() != panes[0].getSelectionEnd()) {
                            HintsController.setErrors(document, LAYER_POM_SELECTION, findHints(model, project, panes[0].getSelectionStart(), panes[0].getSelectionEnd()));
                            ok = true;
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (!ok) {
                        HintsController.setErrors(document, LAYER_POM_SELECTION, Collections.<ErrorDescription>emptyList());
                    }
                }
            }
            //TODO use processing and posting to RP thread.
            return UpToDateStatus.UP_TO_DATE_OK;
        }

    }

}
