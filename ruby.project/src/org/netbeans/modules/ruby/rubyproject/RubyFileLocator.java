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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    public FileObject find(String file) {
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
            File relToPrjDir = new File(FileUtil.toFile(root), file);
            if (relToPrjDir.exists()) {
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
        String fileName = new File(file).getName();
        if (fos != null) {
            for (FileObject fo : fos) {
                if (fo.getNameExt().equals(fileName)) {
                    return fo;
                }
            }
        }

        // Manual search
        for (SourceGroup group : rubyGroups) {
            FileObject root = group.getRootFolder();

            // First see if this path is relative to the root
            try {
                File f = new File(FileUtil.toFile(root), fileName);
                if (f.exists()) {
                    f = f.getCanonicalFile();

                    return FileUtil.toFileObject(f);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            // Search recursively for the given file below the path 
            FileObject fo = findFile(root, fileName);
            if (fo != null) {
                return fo;
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
