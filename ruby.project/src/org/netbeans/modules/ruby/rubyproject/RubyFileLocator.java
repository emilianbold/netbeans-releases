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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * @author Tor Norbye
 */
public class RubyFileLocator implements FileLocator {
    
    protected Lookup context;
    protected Project project;

    public RubyFileLocator(Lookup context, Project project) {
        assert project != null;
        this.context = context;
        this.project = project;
    }

    public FileObject find(String path) {
        // Firstly try whether a path is absolute
        File file = new File(path);
        if (file.isAbsolute() && file.isFile()) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(file));
        }
        
        List<FileObject> roots = new ArrayList<FileObject>();
        
        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }
        
        SourceGroup[] rubyGroups = sources.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        if (rubyGroups != null) {
            for (SourceGroup group : rubyGroups) {
                roots.add(group.getRootFolder());
            }
        }
        
        SourceGroup[] generalGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        if (generalGroups != null) {
            for (SourceGroup group : generalGroups) {
                roots.add(group.getRootFolder());
            }
        }
        
        // Try to resolve relatively to project directory (see e.g. #112254)
        for (FileObject root : roots) {
            File relToPrjDir = new File(FileUtil.toFile(root), path);
            if (relToPrjDir.isFile()) {
                try {
                    relToPrjDir = relToPrjDir.getCanonicalFile();
                    return FileUtil.toFileObject(relToPrjDir);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        // Next try searching the set of source files
        FileObject[] fos = findSources(roots);
        String fileName = new File(path).getName();
        if (fos != null) {
            for (FileObject fo : fos) {
                if (fo.getNameExt().equals(fileName)) {
                    return fo;
                }
            }
        }

        // Last try - manual recursive search
        for (FileObject root : roots) {
            FileObject fo = findFile(root, fileName);
            if (fo != null) {
                return fo;
            }
        }

        return null;
    }

    /** Searches recursively for the given file below the path. */
    private FileObject findFile(FileObject fo, String fileName) {
        if (fileName.equals(fo.getNameExt())) {
            return fo;
        }

        for (FileObject child : fo.getChildren()) {
            FileObject found = findFile(child, fileName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Find selected sources, the sources has to be under single source root.
     *
     * @param context the lookup in which files should be found
     */
    protected FileObject[] findSources(List<FileObject> roots) {
        for (FileObject root : roots) {
            FileObject[] files = RubyActionProvider.findSelectedFiles(context, root,
                    RubyInstallation.RUBY_MIME_TYPE, true);

            // TODO - what about RHTML files?
            if (files != null) {
                return files;
            }
        }
        return null;
    }
}
