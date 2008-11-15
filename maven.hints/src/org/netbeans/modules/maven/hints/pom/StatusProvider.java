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
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
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
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public final class StatusProvider implements UpToDateStatusProviderFactory {

    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        return new StatusProviderImpl(document);
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private Document document;
        private POMModel model;
        private Project project;

        StatusProviderImpl(Document doc) {
            this.document = doc;
            initializeModel();
            checkHints();
        }


        private boolean checkHints() {
            assert model != null;
            try {
                model.sync();
                model.refresh();
            } catch (IOException ex) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Errror while syncing pom model.", ex);
            }
            if (!model.getState().equals(Model.State.VALID)) {
                Logger.getLogger(StatusProvider.class.getName()).log(Level.INFO, "Pom model document is not valid, is " + model.getState());
                HintsController.setErrors(document, "pom", Collections.<ErrorDescription>emptyList());
                return false;
            }
            Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
            Lookup.Result<POMErrorFixProvider> res = lkp.lookupResult(POMErrorFixProvider.class);
            List<ErrorDescription> err = new ArrayList<ErrorDescription>();
            for (POMErrorFixProvider prov : res.allInstances()) {
               List<ErrorDescription> lst = prov.getErrorsForDocument(model, project);
               if (lst != null) {
                   err.addAll(lst);
               }
            }
            HintsController.setErrors(document, "pom", err);
            return true;
        }

        private void initializeModel() {
            DataObject dobj = NbEditorUtilities.getDataObject(document);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                ModelSource ms = Utilities.createModelSource(fo, true);
                model = POMModelFactory.getDefault().getModel(ms);
                model.setAutoSyncActive(false);
                project = FileOwnerQuery.getOwner(fo);
//                //TODO weak listener
                fo.addFileChangeListener(new FileChangeAdapter() {
                    @Override
                    public void fileChanged(FileEvent fe) {
                        checkHints();
                    }
                });
            }
        }

        @Override
        public UpToDateStatus getUpToDate() {
//            if (!checkHints()) {
//                System.out.println("skipped checking hints");
//                return UpToDateStatus.UP_TO_DATE_DIRTY;
//            }
            //TODO use processing and posting to RP thread.
            return UpToDateStatus.UP_TO_DATE_OK;
        }

    }

}
