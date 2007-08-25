/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.io.IOException;

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
    
    private Lookup context;
    private Project project;

    public RubyFileLocator(Lookup context, Project project) {
        assert project != null;
        this.context = context;
        this.project = project;
    }

    public FileObject find(String file) {
        FileObject[] fos = null;

        SourceGroup[] groups = null;
        Sources sources = project.getLookup().lookup(Sources.class);

        if (sources != null) {
            groups = sources.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        }

        if (groups == null) {
            return null;
        }

        // First check roots and search by relative path.
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            FileObject f = root.getFileObject(file);

            if (f != null) {
                return f;
            }
        }

        // Next try searching the set of source files
        fos = findSources(groups);

        if (fos != null) {
            for (FileObject fo : fos) {
                if (fo.getNameExt().equals(file)) {
                    return fo;
                }
            }
        }

        // Manual search
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();

            // First see if this path is relative to the root
            try {
                File f = new File(FileUtil.toFile(root), file);

                if (f.exists()) {
                    f = f.getCanonicalFile();

                    return FileUtil.toFileObject(f);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            // Search recursively for the given file below the path 
            FileObject fo = findFile(root, file);

            if (fo != null) {
                return fo;
            }
        }

        // Try to resolve relatively to project directory (see e.g. #112254)
        File f = new File(FileUtil.toFile(project.getProjectDirectory()), file);
        if (f.exists()) {
            try {
                f = f.getCanonicalFile();
                return FileUtil.toFileObject(f);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    private FileObject findFile(FileObject fo, String name) {
        if (name.equals(fo.getNameExt())) {
            return fo;
        }

        for (FileObject child : fo.getChildren()) {
            FileObject found = findFile(child, name);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     */
    private FileObject[] findSources(SourceGroup[] groups) {
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            FileObject[] files =
                RubyActionProvider.findSelectedFiles(context, root,
                    RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N

            // TODO - what about RHTML files?
            
            if (files != null) {
                return files;
            }
        }

        return null;
    }
}
