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
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.MailingList;
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
            mProj.setURL(kProj.getWebLocation().toExternalForm());

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
                        updateScm(scm, kf);
                        break;

                    case ISSUES:
                        IssueManagement iMng = mProj.getIssueManagement();
                        if (iMng == null) {
                            iMng = factory.createIssueManagement();
                            mProj.setIssueManagement(iMng);
                        }
                        updateIssueMng(iMng, kf);
                        break;

                    case LISTS:
                        updateLists(mProj, kf, factory, kProj);
                        break;

                }
            }

        } finally {
            model.endTransaction();
        }
    }

    private static final String POM_SCM = "scm";
    private static final String POM_DELIM = ":";
    private static final String POM_HG = "hg";
    private static final String POM_SVN = "svn";
    private static final String POM_EXTERNAL = "???";

    private static void updateScm (Scm scm, KenaiFeature kf) {
        StringBuilder conn = new StringBuilder();
        conn.append(POM_SCM);
        conn.append(POM_DELIM);

        String scmName = kf.getName();
        if (KenaiService.Names.SUBVERSION.equals(scmName)) {
            conn.append(POM_SVN);
        } else if (KenaiService.Names.MERCURIAL.equals(scmName)) {
            conn.append(POM_HG);
        } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(scmName)) {
            conn.append(POM_EXTERNAL);
        }
        conn.append(POM_DELIM);

        conn.append(kf.getLocation());

        String strConn = conn.toString();
        scm.setConnection(strConn);
        scm.setDeveloperConnection(strConn);
        scm.setUrl(kf.getWebLocation().toExternalForm());
    }

    private static void updateIssueMng(IssueManagement iMng, KenaiFeature kf) {
        iMng.setSystem(kf.getService());
        iMng.setUrl(kf.getLocation());
    }

    private static final String KENAI_SITE = ".kenai.com";
    private static final String KENAI_SYMPA = "sympa@";
    private static final String KENAI_SUBSCRIBE = "?subject=sub%20";
    private static final String KENAI_UNSUBSCRIBE = "?subject=unsub%20";

    private static void updateLists(Project mProj, KenaiFeature kf, POMComponentFactory factory,
            KenaiProject kProj) {
        // does mailing list entry already exist in POM?
        List<MailingList> mLists = mProj.getMailingLists();
        MailingList mList = null;
        if (mLists != null) {
            for (MailingList ml : mLists) {
                if (ml.getName() != null && ml.getName().equals(kf.getName())) {
                    mList = ml;
                    break;
                }
            }
        }

        if (mList == null) {
            // no entry in POM, create new one
            mList = factory.createMailingList();
            mProj.addMailingList(mList);
        }

        mList.setName(kf.getName());
        mList.setArchive(kf.getWebLocation().toExternalForm());

        // post
        StringBuilder sb = new StringBuilder();
        sb.append(kf.getName());
        sb.append('@');
        sb.append(kProj.getName());
        sb.append(KENAI_SITE);
        mList.setPost(sb.toString());

        // subscribe, unsubscribe
        sb.delete(0, sb.length());
        sb.append(KENAI_SYMPA);
        sb.append(kProj.getName());
        sb.append(KENAI_SITE);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(sb);
        sb.append(KENAI_SUBSCRIBE);
        sb.append(kf.getName());
        sb2.append(KENAI_UNSUBSCRIBE);
        sb2.append(kf.getName());
        mList.setSubscribe(sb.toString());
        mList.setUnsubscribe(sb2.toString());
    }

}

