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
package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * TODO - I should just use RubyFileLocator!
 * @author Tor Norbye
 */
public class RailsFileLocator implements FileLocator {
    private Lookup context;
    private RailsProject project;
    private static final String RAILS_ROOT = "#{RAILS_ROOT}/"; // NOI18N

    public RailsFileLocator(Lookup context, RailsProject project) {
        this.context = context;
        this.project = project;
    }

    public FileObject find(String file) {
        if (file.startsWith(RAILS_ROOT)) {
            file = file.substring(RAILS_ROOT.length());
            FileObject dir = project.getProjectDirectory();
            FileObject fo = dir.getFileObject(file);

            if (fo != null) {
                return fo;
            }

            if (file.indexOf('\\') == -1) {
                fo = dir.getFileObject(file.replace('\\', '/')); // getFileObject only accepts /
            }
            
            if (fo != null) {
                return fo;
            }
        }
        
        
        FileObject[] fos = null;

        if (context != Lookup.EMPTY) {
            // First check roots and search by relative path.
            FileObject[] srcPath = project.getSourceRoots().getRoots();

            if (srcPath != null) {
                for (FileObject root : srcPath) {
                    FileObject f = root.getFileObject(file);

                    if (f != null) {
                        return f;
                    }
                }
            }

            // Next try searching the set of source files
            fos = findSources(context);

            if (fos != null) {
                for (FileObject fo : fos) {
                    if (fo.getNameExt().equals(file)) {
                        return fo;
                    }
                }
            }
        }

        // Manual search
        FileObject[] srcPath = project.getSourceRoots().getRoots();

        for (FileObject root : srcPath) {
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

    private FileObject[] findSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = RailsActionProvider.findSelectedFiles(context, srcPath[i], RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
            files = RailsActionProvider.findSelectedFiles(context, srcPath[i], RhtmlTokenId.MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

}
