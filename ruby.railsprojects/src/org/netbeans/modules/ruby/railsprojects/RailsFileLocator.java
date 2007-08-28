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

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.RubyFileLocator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Tor Norbye
 */
public class RailsFileLocator extends RubyFileLocator {
    
    private static final String RAILS_ROOT = "#{RAILS_ROOT}/"; // NOI18N

    public RailsFileLocator(Lookup context, Project project) {
        super(context, project);
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
        return super.find(file);
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
            FileObject[] files = RailsActionProvider.findSelectedFiles(context, root,
                    RubyInstallation.RUBY_MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
            files = RailsActionProvider.findSelectedFiles(context, root,
                    RhtmlTokenId.MIME_TYPE, true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }
}
