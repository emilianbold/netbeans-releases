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
package org.netbeans.modules.mercurial.ui.status;

import java.io.File;
import java.util.Map;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;

/**
 * Status action for mercurial: 
 * hg status - show changed files in the working directory
 * 
 * @author John Rice
 */
public class StatusAction extends ContextAction {

    
    private final VCSContext context;

    public StatusAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void performAction(ActionEvent ev) {
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return;
                
        final HgVersioningTopComponent stc = HgVersioningTopComponent.findInstance();
        stc.setContentTitle(Utils.getContextDisplayName(context)); 
        stc.setContext(context);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }
    
    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    } 

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final VCSContext context, HgProgressSupport support) {

        if (context == null || context.getRootFiles().size() == 0) {
            return;
        }
        File repository = HgUtils.getRootFile(context);
        if (repository == null) {
            return;
        }

        try {
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            Calendar start = Calendar.getInstance();
            cache.refreshCached(context);
            Calendar end = Calendar.getInstance();
            Mercurial.LOG.log(Level.FINE, "executeStatus: refreshCached took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N

            for (File root :  context.getRootFiles()) {
                refreshFile(root, repository, support, cache);
                if (support.isCanceled()) {
                    return;
                }
            }
        } catch (HgException ex) {
            support.annotate(ex);
        }
    }

    public static void refreshFile(File root, File repository, HgProgressSupport support, FileStatusCache cache) throws HgException {
        if (support != null && support.isCanceled()) {
            return;
        }
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        if (root.isDirectory()) {
            Map<File, FileInformation> interestingFiles;
            interestingFiles = HgCommand.getInterestingStatus(repository, root);
            if (!interestingFiles.isEmpty()) {
                Collection<File> files = interestingFiles.keySet();
                Map<File, Map<File, FileInformation>> interestingDirs = HgUtils.getInterestingDirs(interestingFiles, files);
                start = Calendar.getInstance();
                for (File file : files) {
                    if (support != null && support.isCanceled()) {
                        return;
                    }
                    FileInformation fi = interestingFiles.get(file);
                    cache.refreshFileStatus(file, fi, interestingDirs.get(file.isDirectory() ? file : file.getParentFile()));
                }
                end = Calendar.getInstance();
                Mercurial.LOG.log(Level.FINE, "executeStatus: process interesting files took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
            }
        } else {
            cache.refresh(root, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
}
