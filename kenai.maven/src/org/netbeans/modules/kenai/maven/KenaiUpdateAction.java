/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.maven;

import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class KenaiUpdateAction extends AbstractAction implements ContextAwareAction {

    private Lookup context;
    private String uri;

    public KenaiUpdateAction() {
        putValue(Action.NAME, NbBundle.getMessage(KenaiUpdateAction.class, "CTL_KenaiUpdateAction"));
    }

    public KenaiUpdateAction(Lookup context) {
        this();
        this.context = context;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        FileObject fo = context.lookup(FileObject.class);
        if (fo == null) {
            return false;
        }
        Object attr = fo.getAttribute("ProvidedExtensions.RemoteLocation");
        if (attr == null || !(attr instanceof String)) {
            return false;
        }
        uri = (String)attr;

        return isKenai(uri);
    }

    public void actionPerformed(ActionEvent arg0) {
        FileObject fo = context.lookup(FileObject.class);
        /*Project prj = actionContext.lookup(Project.class);
        if (prj == null) {
            if (fo != null) {
                prj = FileOwnerQuery.getOwner(fo);
            }
        }*/

        ModelSource ms = Utilities.createModelSource(fo);
        POMModel model = null;
        if (ms.isEditable()) {
            model = POMModelFactory.getDefault().getModel(ms);
        }

        KenaiProject project = null;
        try {
            project = KenaiProject.forRepository(uri);
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (model != null && project != null) {
            try {
                performUpdate(model, project);
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new KenaiUpdateAction(actionContext);
    }


    private static Pattern repositoryPattern = Pattern.compile("(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)");

    private static boolean isKenai (String uri) {
        return repositoryPattern.matcher(uri).matches();
    }

    static void performUpdate (POMModel model, KenaiProject kProj) throws KenaiException {
        try {
            model.startTransaction();
            Project mProj = model.getProject();
            POMComponentFactory factory = model.getFactory();

            mProj.setName(kProj.getDisplayName());
            mProj.setDescription(kProj.getDescription());

            KenaiFeature[] kfs = kProj.getFeatures();

            for (int i = 0; i < kfs.length; i++) {
                KenaiFeature kf = kfs[i];

                switch (kf.getType()) {
                    case SOURCE:
                        Scm scm = mProj.getScm();
                        if (scm == null) {
                            scm = factory.createScm();
                            mProj.setScm(scm);
                        }
                        scm.setUrl(kf.getLocation());
                }
            }



        } finally {
            model.endTransaction();
        }
    }

}

