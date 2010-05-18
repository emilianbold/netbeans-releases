/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.CVSRoot;

import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

/**
 * This admin handler represents virtual reality, it
 * does not touch any disk file. Instead it pretends
 * that it represents empty local folder corresponding
 * to given repository path.
 *
 * <p>It supports only non-recursive commands.
 *
 * @author Petr Kuzel
 */
final class VirtualAdminHandler implements AdminHandler {

    private final CVSRoot root;

    private final String repositoryPath;

    /**
     * Creates AdminHandler for a non-existing local folder that represents
     * root.getRepository() + "/" + repositoryPath repository folder.
     *
     * @param root repository
     * @param repositoryPath local path representation in repository (always <tt>/</tt> separated)
     */
    public VirtualAdminHandler(CVSRoot root, String repositoryPath) {
        this.root = root;
        this.repositoryPath = repositoryPath;
    }

    public void updateAdminData(String localDirectory, String repositoryPath, Entry entry, GlobalOptions globalOptions) throws IOException {
    }

    public boolean exists(File file) {
        return false;
    }

    public Entry getEntry(File file) throws IOException {
        return null;
    }

    public Iterator getEntries(File directory) throws IOException {
        return Collections.EMPTY_SET.iterator();
    }

    public void setEntry(File file, Entry entry) throws IOException {
    }

    public String getRepositoryForDirectory(String directory, String repository) throws IOException {
        String repo = root.getRepository();
        assert repo.equals(repository) : "Mismatch! Expected " + repo + " but got " + repository;  // NOI18N
        return repository + "/" + repositoryPath; // NOI18N
    }

    public void removeEntry(File file) throws IOException {
    }

    public Set getAllFiles(File directory) throws IOException {
        return Collections.EMPTY_SET;
    }

    public String getStickyTagForDirectory(File directory) {
        return null; // trunk
    }
}
