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

package org.netbeans.libs.git.jgit.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.IgnoreRule;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public abstract class IgnoreUnignoreCommand extends GitCommand {

    protected final File[] files;
    private final ProgressMonitor monitor;
    private final Set<File> ignoreFiles;
    private final FileListener listener;
    protected final static Logger LOG = Logger.getLogger(IgnoreUnignoreCommand.class.getName());
    
    public IgnoreUnignoreCommand (Repository repository, File[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, monitor);
        this.files = files;
        this.monitor = monitor;
        this.ignoreFiles = new LinkedHashSet<File>();
        this.listener = listener;
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            String message = null;
            if (files.length == 0) {
                message = "Files to ignore must not be empty.";
            } else if (Arrays.asList(files).contains(getRepository().getWorkTree())) {
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
        File workTree = getRepository().getWorkTree();
        for (int i = 0; i < files.length && !monitor.isCanceled(); ++i) {
            File f = files[i];
            try {
                changeIgnoreStatus(f);
                listener.notifyFile(f, Utils.getRelativePath(workTree, f));
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                monitor.notifyError(ex.getLocalizedMessage());
            }
        }
    }
    
    private void changeIgnoreStatus (File f) throws IOException {
        File parent = f;
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
            if (parent.equals(getRepository().getWorkTree())) {
                cont = addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, false)
                        && checkExcludeFile(path, isDirectory) 
                        && addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, true);
            } else {
                cont = addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, false);
            }
        }
    }
    
    private boolean addStatement (File gitIgnore, String path, boolean isDirectory, boolean rootIgnore) throws IOException {
        List<IgnoreRule> ignoreRules = parse(gitIgnore);
        return addStatement(ignoreRules, gitIgnore, path, isDirectory, rootIgnore) == MatchResult.CHECK_PARENT && !rootIgnore;
    }

    protected final void save (File gitIgnore, List<IgnoreRule> ignoreRules) throws IOException {
        BufferedWriter bw = null;
        File tmpFile = File.createTempFile(Constants.DOT_GIT_IGNORE, "tmp", gitIgnore.getParentFile()); //NOI18N
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), Constants.CHARSET));
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
            if (!tmpFile.renameTo(gitIgnore)) {
                // cannot rename directly, try backup and delete te original .gitignore
                File tmpCopy = generateTempFile(Constants.DOT_GIT_IGNORE, gitIgnore.getParentFile()); //NOI18N
                boolean success = false;
                if (gitIgnore.renameTo(tmpCopy)) {
                    // and try to rename again
                    success = tmpFile.renameTo(gitIgnore);
                    if (!success) {
                        // restore te original .gitignore file
                        tmpCopy.renameTo(gitIgnore);
                    }
                    tmpCopy.delete();
                }
                if (!success) {
                    tmpFile.delete();
                    throw new IOException("Cannot write to " + gitIgnore.getAbsolutePath());
                }
            }

        }
        ignoreFiles.add(gitIgnore);
    }

    private List<IgnoreRule> parse (File gitIgnore) throws IOException {
        List<IgnoreRule> rules = new LinkedList<IgnoreRule>();
        if (gitIgnore.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(gitIgnore), Constants.CHARSET));
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

    public File[] getModifiedIgnoreFiles () {
        return ignoreFiles.toArray(new File[ignoreFiles.size()]);
    }

    protected abstract MatchResult addStatement (List<IgnoreRule> ignoreRules, File gitIgnore, String path, boolean isDirectory, boolean rootIgnore) throws IOException;

    protected static String escapeChars (String path) {
        return path.replace("[", "[[]").replace("*", "[*]").replace("?", "[?]"); //NOI18N
    }

    private boolean checkExcludeFile (String path, boolean isDirectory) throws IOException {
        File excludeFile = new File(getRepository().getDirectory(), "info/exclude");
        List<IgnoreRule> ignoreRules = parse(excludeFile);
        return addStatement(ignoreRules, excludeFile, path, isDirectory, false) == MatchResult.CHECK_PARENT;
    }

    private File generateTempFile (String basename, File parent) {
        File tempFile = new File(parent, basename);
        while (tempFile.exists()) {
            tempFile = new File(parent, basename + Long.toString(System.currentTimeMillis()));
        }
        return tempFile;
    }
}
