/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.pull;

import java.net.URISyntaxException;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.DialogDescriptor;
import static org.netbeans.modules.mercurial.util.HgUtils.isNullOrEmpty;
import static org.openide.DialogDescriptor.INFORMATION_MESSAGE;

/**
 * Pull action for mercurial:
 * hg pull - pull changes from the specified source
 *
 * @author John Rice
 */
public class PullAction extends ContextAction {
    private static final String CHANGESET_FILES_PREFIX = "files:"; //NOI18N
    
    public enum PullType {

        LOCAL, OTHER
    }
    {
    }

    private final VCSContext context;

    public PullAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public void performAction(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) {
            OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(PullAction.class,"MSG_PULL_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(PullAction.class,"MSG_PULL_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(PullAction.class, "MSG_PULL_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(PullAction.class, "MSG_PULL_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(PullAction.class, "MSG_PULL_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            logger.closeLog();
            return;
        }
        pull(context);
    }

    public static boolean confirmWithLocalChanges(File rootFile, Class bundleLocation, String title, String query, 
            List<String> listIncoming, OutputLogger logger) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] roots = new File[1];
        roots[0] = rootFile;
        File[] localModNewFiles = cache.listFiles(roots, 
                FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
                FileInformation.STATUS_VERSIONED_CONFLICT | 
                FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        List<String> listIncomingAndLocalMod = new ArrayList<String>();
        Set<String> setFiles = new HashSet<String>();
        String filesStr;
        String[] aFileStr;
        String root = rootFile.getAbsolutePath();
        
        for(String s: listIncoming){
            if(s.indexOf(CHANGESET_FILES_PREFIX) == 0){
                filesStr = (s.substring(CHANGESET_FILES_PREFIX.length())).trim();
                aFileStr = filesStr.split(" ");
                for(String fileStr: aFileStr){
                    setFiles.add(root + File.separator + fileStr);
                    break;
                }
            }
        }
        for(File f : localModNewFiles){
            for(String s : setFiles){
                if( s.equals(f.getAbsolutePath())){
                    listIncomingAndLocalMod.add(s);
                }
            }
        }

        if (listIncomingAndLocalMod != null && listIncomingAndLocalMod.size() > 0) {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_OVERWRITE_LOCAL")); // NOI18N
            logger.output(listIncomingAndLocalMod);
            int response = JOptionPane.showOptionDialog(null, 
                    NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (response == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        return true;
    }


    static void annotateChangeSets(List<String> list, Class bundleLocation, String title) {
        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        OutputWriter outRed = io.getErr();
        outRed.println(NbBundle.getMessage(bundleLocation, title));
        for (String s : list) {
            if (s.indexOf(Mercurial.CHANGESET_STR) == 0) {
                outRed.println(s);
            } else if (!s.equals("")) {
                out.println(s);
            }
        }
        out.println("");
        out.close();
        outRed.close();
    }

    public static void pull(final VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        if (root == null) return;

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { getDefaultAndPerformPull(ctx, root, this.getLogger()); } };

        support.start(rp, root, org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")); // NOI18N
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    }

    static void getDefaultAndPerformPull(VCSContext ctx, File root, OutputLogger logger) {
        final String pullSourceString = HgRepositoryContextCache.getInstance().getPullDefault(ctx);
        // If the repository has no default pull path then inform user
        if (isNullOrEmpty(pullSourceString)) {
            notifyDefaultPullUrlNotSpecified(logger);
            return;
        }

        HgURL pullSource;
        try {
            pullSource = new HgURL(pullSourceString);
        } catch (URISyntaxException ex) {
            notifyDefaultPullUrlInvalid(pullSourceString, ex.getReason(), logger);
            return;
        }

        // We assume that if fromPrjName is null that it is a remote pull.
        // This is not true as a project which is in a subdirectory of a
        // repository will report a project name of null. This does no harm.
        String fromPrjName;
        PullType pullType;

        if (pullSource.isFile()) {
            fromPrjName = HgProjectUtils.getProjectName(new File(pullSource.getPath()));
            pullType = (fromPrjName != null) ? PullType.LOCAL
                                             : PullType.OTHER;
        } else {
            fromPrjName = null;
            pullType = PullType.OTHER;
        }
        Project proj = HgUtils.getProject(ctx);
        final String toPrjName = HgProjectUtils.getProjectName(proj);
        performPull(pullType, ctx, root, pullSource, fromPrjName, toPrjName, logger);
    }

    private static void notifyDefaultPullUrlNotSpecified(OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (getMessage("MSG_NO_DEFAULT_PULL_SET_MSG"));  //NOI18N
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(
                        getMessage("MSG_NO_DEFAULT_PULL_SET")));        //NOI18N
    }

    private static void notifyDefaultPullUrlInvalid(String pullUrl,
                                                    String reason,
                                                    OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N
        String msg = getMessage("MSG_DEFAULT_PULL_INVALID", pullUrl);   //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (msg);
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(msg));
    }

    static void performPull(PullType type, VCSContext ctx, File root, HgURL pullSource, String fromPrjName, String toPrjName, OutputLogger logger) {
        if(root == null || pullSource == null) return;
        File bundleFile = null; 
        
        try {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE_SEP")); // NOI18N

            if (fromPrjName != null) {
                logger.outputInRed(NbBundle.getMessage(
                        PullAction.class,
                        "MSG_PULLING_FROM",                             //NOI18N
                        fromPrjName,
                        HgUtils.stripDoubleSlash(pullSource.toString())));
            } else {
                logger.outputInRed(NbBundle.getMessage(
                        PullAction.class,
                        "MSG_PULLING_FROM_NONAME",                      //NOI18N
                        HgUtils.stripDoubleSlash(pullSource.toString())));
            }

            List<String> listIncoming;
            if(type == PullType.LOCAL){
                listIncoming = HgCommand.doIncoming(root, logger);
            }else{
                for (int i = 0; i < 10000; i++) {
                    if (!new File(root.getParentFile(), root.getName() + "_bundle" + i).exists()) { // NOI18N
                        bundleFile = new File(root.getParentFile(), root.getName() + "_bundle" + i); // NOI18N
                        break;
                    }
                }
                listIncoming = HgCommand.doIncoming(root, pullSource, bundleFile, logger, false);
            }
            if (listIncoming == null || listIncoming.isEmpty()) return;
            
            boolean bNoChanges = HgCommand.isNoChanges(listIncoming.get(listIncoming.size() - 1));

            // Warn User when there are Local Changes present that Pull will overwrite
            if (!bNoChanges && !confirmWithLocalChanges(root, PullAction.class, "MSG_PULL_LOCALMODS_CONFIRM_TITLE", "MSG_PULL_LOCALMODS_CONFIRM_QUERY", listIncoming, logger)) { // NOI18N
                logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_LOCALMODS_CANCEL")); // NOI18N
                logger.output(""); // NOI18N
                return;
            }

            // Do Pull if there are changes to be pulled
            List<String> list;
            if (bNoChanges) {
                list = listIncoming;
            } else {
                if(type == PullType.LOCAL){
                    list = HgCommand.doPull(root, logger);
                }else{
                    list = HgCommand.doUnbundle(root, bundleFile, logger);
                }
            }            
                       
            if (list != null && !list.isEmpty()) {

                if (!bNoChanges) {
                    annotateChangeSets(HgUtils.replaceHttpPassword(listIncoming), PullAction.class, "MSG_CHANGESETS_TO_PULL"); // NOI18N
                }

                logger.output(HgUtils.replaceHttpPassword(list));
                if (fromPrjName != null) {
                    logger.outputInRed(NbBundle.getMessage(
                            PullAction.class, "MSG_PULL_FROM", fromPrjName, HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
                } else {
                    logger.outputInRed(NbBundle.getMessage(
                            PullAction.class, "MSG_PULL_FROM_NONAME", HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
                }
                if (toPrjName != null) {
                    logger.outputInRed(NbBundle.getMessage(
                            PullAction.class, "MSG_PULL_TO", toPrjName, root)); // NOI18N
                } else {
                    logger.outputInRed(NbBundle.getMessage(
                            PullAction.class, "MSG_PULL_TO_NONAME", root)); // NOI18N
                }

                // Handle Merge - both automatic and merge with conflicts
                boolean bMergeNeededDueToPull = HgCommand.isMergeNeededMsg(list.get(list.size() - 1));
                boolean bConfirmMerge = false;
                if(bMergeNeededDueToPull){
                    bConfirmMerge = HgUtils.confirmDialog(
                        PullAction.class, "MSG_PULL_MERGE_CONFIRM_TITLE", "MSG_PULL_MERGE_CONFIRM_QUERY"); // NOI18N
                } else {
                    boolean bOutStandingUncommittedMerges = HgCommand.isMergeAbortUncommittedMsg(list.get(list.size() - 1));
                    if(bOutStandingUncommittedMerges){
                        bConfirmMerge = HgUtils.confirmDialog(
                            PullAction.class, "MSG_PULL_MERGE_CONFIRM_TITLE", "MSG_PULL_MERGE_UNCOMMITTED_CONFIRM_QUERY"); // NOI18N
                    }
                }
                if (bConfirmMerge) {
                    logger.output(""); // NOI18N
                    logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_MERGE_DO")); // NOI18N
                    MergeAction.doMergeAction(root, null, logger);
                } else {
                    List<String> headRevList = HgCommand.getHeadRevisions(root);
                    if (headRevList != null && headRevList.size() > 1){
                        MergeAction.printMergeWarning(headRevList, logger);
                    }
                }
            }

            if (!bNoChanges) {
                PushAction.notifyUpdatedFiles(root, list);
                HgUtils.forceStatusRefreshProject(ctx);
                // refresh filesystem to take account of deleted files.
                FileObject rootObj = FileUtil.toFileObject(root);
                try {
                    rootObj.getFileSystem().refresh(true);
                } catch (java.lang.Exception ex) {
                }
            }
            
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            if (bundleFile != null) {
                bundleFile.delete();
            }
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }

    private static String getMessage(String msgKey, String... args) {
        return NbBundle.getMessage(PullAction.class, msgKey, args);
    }

}
