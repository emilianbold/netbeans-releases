/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.IgnoreRule;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public abstract class IgnoreUnignoreCommand extends GitCommand {

    public static final String DOT_GIT_IGNORE = ".gitignore";
    public static final Charset CHARSET = Charset.forName("UTF-8");
    protected final VCSFileProxy[] files;
    private final ProgressMonitor monitor;
    private final Set<VCSFileProxy> ignoreFiles;
    private final FileListener listener;
    protected final static Logger LOG = Logger.getLogger(IgnoreUnignoreCommand.class.getName());
    
    public IgnoreUnignoreCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.files = files;
        this.monitor = monitor;
        this.ignoreFiles = new LinkedHashSet<VCSFileProxy>();
        this.listener = listener;
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            String message = null;
            if (files.length == 0) {
                message = "Files to ignore must not be empty.";
            } else if (Arrays.asList(files).contains(getRepository().getLocation())) {
                message = "Cannot ignore working tree root.";
            }
            if (message != null) {
                monitor.preparationsFailed(message);
                throw new GitException(message);
            }
        }
        return retval;
    }

    @Override
    protected void run () {
        VCSFileProxy workTree = getRepository().getLocation();
        for (int i = 0; i < files.length && !monitor.isCanceled(); ++i) {
            VCSFileProxy f = files[i];
            try {
                changeIgnoreStatus(f);
                listener.notifyFile(f, Utils.getRelativePath(workTree, f));
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                monitor.notifyError(ex.getLocalizedMessage());
            }
        }
    }
    
    private void changeIgnoreStatus (VCSFileProxy f) throws IOException {
        VCSFileProxy parent = f;
        boolean isDirectory = f.isDirectory();
        StringBuilder sb = new StringBuilder('/');
        if (isDirectory) {
            sb.append('/');
        }
        boolean cont = true;
        while (cont) {
            sb.insert(0, parent.getName()).insert(0, '/');
            parent = parent.getParentFile();
            String path = sb.toString();
            if (parent.equals(getRepository().getLocation())) {
                if (addStatement(VCSFileProxy.createFileProxy(parent, DOT_GIT_IGNORE), path, isDirectory, false) && handleAdditionalIgnores(path, isDirectory)) {
                    addStatement(VCSFileProxy.createFileProxy(parent, DOT_GIT_IGNORE), path, isDirectory, true);
                }
                cont = false;
            } else {
                cont = addStatement(VCSFileProxy.createFileProxy(parent, DOT_GIT_IGNORE), path, isDirectory, false);
            }
        }
    }
    
    private boolean addStatement (VCSFileProxy gitIgnore, String path, boolean isDirectory, boolean forceWrite) throws IOException {
        List<IgnoreRule> ignoreRules = parse(gitIgnore);
        return addStatement(ignoreRules, gitIgnore, path, isDirectory, forceWrite, true) == MatchResult.CHECK_PARENT;
    }

    protected final void save (VCSFileProxy gitIgnore, List<IgnoreRule> ignoreRules) throws IOException {
        BufferedWriter bw = null;
        VCSFileProxy tmpFile = VCSFileProxySupport.generateTemporaryFile(gitIgnore.getParentFile(), DOT_GIT_IGNORE+"tmp");
        try {
            bw = new BufferedWriter(new OutputStreamWriter(VCSFileProxySupport.getOutputStream(tmpFile), CHARSET));
            for (ListIterator<IgnoreRule> it = ignoreRules.listIterator(); it.hasNext(); ) {
                String s = it.next().getPattern(false);
                bw.write(s, 0, s.length());
                if (it.hasNext()) {
                    bw.newLine();
                }
            }
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) { }
            }
            if (!VCSFileProxySupport.renameTo(tmpFile, gitIgnore)) {
                // cannot rename directly, try backup and delete te original .gitignore
                VCSFileProxy tmpCopy = generateTempFile(DOT_GIT_IGNORE, gitIgnore.getParentFile()); //NOI18N
                boolean success = false;
                if (VCSFileProxySupport.renameTo(gitIgnore, tmpCopy)) {
                    // and try to rename again
                    success = VCSFileProxySupport.renameTo(tmpFile, gitIgnore);
                    if (!success) {
                        // restore te original .gitignore file
                        VCSFileProxySupport.renameTo(tmpCopy, gitIgnore);
                    }
                    VCSFileProxySupport.delete(tmpCopy);
                }
                if (!success) {
                    VCSFileProxySupport.delete(tmpFile);
                    throw new IOException("Cannot write to " + gitIgnore.getPath());
                }
            }

        }
        ignoreFiles.add(gitIgnore);
    }

    private List<IgnoreRule> parse (VCSFileProxy gitIgnore) throws IOException {
        List<IgnoreRule> rules = new LinkedList<IgnoreRule>();
        if (gitIgnore.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(gitIgnore.getInputStream(false), CHARSET));
                String txt;
                while ((txt = br.readLine()) != null) {
                    rules.add(new IgnoreRule(txt));
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) { }
                }
            }
        }
        return rules;
    }

    public VCSFileProxy[] getModifiedIgnoreFiles () {
        return ignoreFiles.toArray(new VCSFileProxy[ignoreFiles.size()]);
    }

    protected abstract MatchResult addStatement (List<IgnoreRule> ignoreRules, VCSFileProxy gitIgnore, String path, boolean isDirectory, boolean forceWrite, boolean writable) throws IOException;

    protected static String escapeChars (String path) {
        return path.replace("[", "[[]").replace("*", "[*]").replace("?", "[?]"); //NOI18N
    }

    protected final MatchResult checkExcludeFile (String path, boolean isDirectory) throws IOException {
        VCSFileProxy excludeFile = VCSFileProxy.createFileProxy(getRepository().getMetadataLocation(), "info/exclude");
        List<IgnoreRule> ignoreRules = parse(excludeFile);
        return addStatement(ignoreRules, excludeFile, path, isDirectory, false, true);
    }

    protected final MatchResult checkGlobalExcludeFile (String path, boolean directory) throws IOException {
        VCSFileProxy excludeFile = null;
        //VCSFileProxy excludeFile = getGlobalExcludeFile();
        if (excludeFile != null && VCSFileProxySupport.canRead(excludeFile)) {
            List<IgnoreRule> ignoreRules = parse(excludeFile);
            return addStatement(ignoreRules, excludeFile, path, directory, false, false);
        }
        return MatchResult.NOT_IGNORED;
    }

    private VCSFileProxy generateTempFile (String basename, VCSFileProxy parent) {
        VCSFileProxy tempFile = VCSFileProxy.createFileProxy(parent, basename);
        while (tempFile.exists()) {
            tempFile = VCSFileProxy.createFileProxy(parent, basename + Long.toString(System.currentTimeMillis()));
        }
        return tempFile;
    }

//    private VCSFileProxy getGlobalExcludeFile () {
//        Repository repository = getRepository().getRepository();
//        String path = repository.getConfig().get(CoreConfig.KEY).getExcludesFile();
//        VCSFileProxy excludesfile = null;
//        if (path != null) {
//            VCSFileProxy location = getRepository().getLocation();
//            if (path.startsWith("~/")) {
//                VCSFileProxy home = VCSFileProxySupport.getHome(location);
//                excludesfile = VCSFileProxy.createFileProxy(home, path.substring(2));
//            } else if (path.startsWith("/")) {
//                excludesfile = VCSFileProxySupport.getResource(location, path);
//            } else {
//                excludesfile = VCSFileProxy.createFileProxy(location, path);
//            }
//        }
//        return excludesfile;
//    }

    protected abstract boolean handleAdditionalIgnores (String path, boolean directory) throws IOException;

    public static enum MatchResult {
            NOT_IGNORED,
            IGNORED,
            CHECK_PARENT;
    }
}
