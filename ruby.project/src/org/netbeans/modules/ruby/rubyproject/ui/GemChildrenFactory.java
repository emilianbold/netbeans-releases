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
package org.netbeans.modules.ruby.rubyproject.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * A ChildFactory for gem nodes.
 * 
 * @author Erno Mononen
 */
public class GemChildrenFactory extends ChildFactory<FileObject> {

    private final RubyPlatform platform;
    private static final String GEMS_DIR = "gems"; //NOI18N
    private static final FileFilter DIRS = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };
    private static final Comparator<FileObject> NAME_COMPARATOR = new Comparator<FileObject>() {

        public int compare(FileObject o1, FileObject o2) {
            return o1.getNameExt().compareToIgnoreCase(o2.getNameExt());
        }
    };
    /**
     * Listener for changes in gem repos.
     */
    private final FileChangeListener fileListener = new FileChangeListener() {

        public void fileFolderCreated(FileEvent fe) {
            refresh(false);
        }

        public void fileDataCreated(FileEvent fe) {
            refresh(false);
        }

        public void fileChanged(FileEvent fe) {
            refresh(false);
        }

        public void fileDeleted(FileEvent fe) {
            refresh(false);
        }

        public void fileRenamed(FileRenameEvent fe) {
            refresh(false);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            refresh(false);
        }
    };

    public static GemChildrenFactory create(RubyBaseProject project) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        GemChildrenFactory result = new GemChildrenFactory(platform);
        return result;
    }

    private GemChildrenFactory(RubyPlatform platform) {
        this.platform = platform;
    }

    @Override
    protected Node createNodeForKey(FileObject key) {
        return new GemNode(key);
    }

    @Override
    protected boolean createKeys(List<FileObject> toPopulate) {
        GemManager gemManager = platform.getGemManager();
        if (gemManager == null) {
            return true;
        }
        for (File repo : gemManager.getRepositories()) {
            File gemsDir = new File(repo, GEMS_DIR); //NOI18N
            if (!gemsDir.exists() || !gemsDir.isDirectory()) {
                continue;
            }
            try {
                FileUtil.addFileChangeListener(fileListener, gemsDir);
            } catch (IllegalArgumentException iae) {
                // see #33162
            }
            for (File gem : gemsDir.listFiles(DIRS)) {
                toPopulate.add(FileUtil.toFileObject(gem));
            }
        }
        Collections.sort(toPopulate, NAME_COMPARATOR);
        return true;
    }
}
