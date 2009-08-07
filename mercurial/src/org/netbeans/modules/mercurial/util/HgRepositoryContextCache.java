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
package org.netbeans.modules.mercurial.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * A class to encapsulate a Repository and allow us to cache some values
 *
 * @author John Rice
 */
public class HgRepositoryContextCache {
    private boolean hasHistory;
    private String pushDefault;
    private String pullDefault;
    private File root;

    private VCSContext rootCtx;
    private Set<File> historyCtxRootFiles;
    private Set<File> pushCtxRootFiles;
    private Set<File> pullCtxRootFiles;

    private Map<File, DefaultPaths> rootToDefaultPaths;

    private static HgRepositoryContextCache instance;

    private HgRepositoryContextCache() {
    }

    public static HgRepositoryContextCache getInstance() {
        if(instance == null) {
            instance = new HgRepositoryContextCache();
        }
        return instance;
    }

    public boolean hasHistory(VCSContext ctx) {
        Set<File> files;
        if(ctx == null) return false;
        files = ctx.getRootFiles();

        if(files.equals(historyCtxRootFiles)){
            return hasHistory;
        }else{
            root = getRoot(ctx);
            hasHistory = HgCommand.hasHistory(root);
            historyCtxRootFiles = ctx.getRootFiles();
            return hasHistory;
        }

    }

    public void setHasHistory(VCSContext ctx) {
        historyCtxRootFiles = ctx.getRootFiles();
        hasHistory = true;
    }

    public synchronized String getPullDefault(VCSContext ctx) {
        Set<File> files;
        if(ctx == null) return null;
        files = ctx.getRootFiles();

        if(files.equals(pullCtxRootFiles)){
            return pullDefault;
        }else{
            root = getRoot(ctx);
            pullDefault = new HgConfigFiles(root).getDefaultPull(true);
            pullCtxRootFiles = ctx.getRootFiles();
            return pullDefault;
        }
    }

    public synchronized void reset() {
        pushCtxRootFiles = null;
        pullCtxRootFiles = null;
        getRootToDefaultPaths().clear();
    }

    public synchronized String getPushDefault(VCSContext ctx) {
        Set<File> files;
        if(ctx == null) return null;
        files = ctx.getRootFiles();

        if(files.equals(pushCtxRootFiles)){
            return pushDefault;
        }else{
            root = getRoot(ctx);
            pushDefault = new HgConfigFiles(root).getDefaultPush(true);
            pushCtxRootFiles = ctx.getRootFiles();
            return pushDefault;
        }
    }

    private File getRoot(VCSContext ctx){
        if(ctx == rootCtx && root != null) {
            return root;
        } else {
            root = HgUtils.getRootFile(ctx);
            rootCtx = ctx;
            return root;
        }
    }

    public synchronized String getPullDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        DefaultPaths paths = getDefaultPaths(repoRoot);
        return paths.pull;
    }

    public synchronized String getPushDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        DefaultPaths paths = getDefaultPaths(repoRoot);
        return paths.push;
    }

    private DefaultPaths getDefaultPaths(File repoRoot) {
        Map<File, DefaultPaths> map = getRootToDefaultPaths();
        DefaultPaths paths = map.get(repoRoot);
        if (paths == null) {
            HgConfigFiles config = new HgConfigFiles(repoRoot);
            String pull = config.getDefaultPull(true);
            String push = config.getDefaultPush(true);
            paths = new DefaultPaths(pull, push);
            map.put(root, paths);
        }
        return paths;
    }

    private Map<File, DefaultPaths> getRootToDefaultPaths() {
        if(rootToDefaultPaths == null) {
            rootToDefaultPaths = new HashMap<File, DefaultPaths>();
        }
        return rootToDefaultPaths;
    }

    private static class DefaultPaths {
        public DefaultPaths(String pull, String push) {
            this.pull = pull;
            this.push = push;
        }
        String pull;
        String push;
    }
}

