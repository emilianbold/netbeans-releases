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
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.RubyFileLocator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Tor Norbye
 */
public class RailsFileLocator extends RubyFileLocator {
    
    public RailsFileLocator(Lookup context, Project project) {
        super(context, project);
    }

    /**
     * Find selected sources, the sources has to be under single source root.
     *
     * @param context the lookup in which files should be found
     */
    protected @Override FileObject[] findSources(List<FileObject> roots) {
        FileObject[] files = super.findSources(roots);
        for (FileObject root : roots) {
            files = RailsActionProvider.findSelectedFiles(context, root,
                    RhtmlTokenId.MIME_TYPE, true);
            if (files != null) {
                return files;
            }
        }
        return null;
    }
}
