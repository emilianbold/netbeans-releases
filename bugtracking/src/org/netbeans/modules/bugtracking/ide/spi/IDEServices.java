/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ide.spi;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Stupka
 */
public interface IDEServices {
    
    public boolean providesOpenDocument();
    public void openDocument(String resourcePath, int offset);
    
    public boolean providesFindFile();
    // XXX to be clarified if FileObject or if URL would be eventually better.
    // used when opening search history for a file given by a stacktrace.
    // Note, that io.File wouldn't work for VCS on remote filesystems 
    public FileObject findFile(String resourcePath);
        
    public boolean providesJumpTo();
    public void jumpTo(String label, String resource);

    public boolean providesPluginUpdate();
    
    public Plugin getPluginUpdates(String cnb, String pluginName);

    public boolean providesPatchUtils();

    /**
     * Applies the given patch file.
     * 
     * @param patchFile the patch files
     * @param context the context on which the patch should be applied
     * @throws PatchException
     * @throws IOException - the patch is invalid or cannot be applied
     */
    public void applyPatch(File patchFile, File context) throws IOException;

    /**
     * Determines whether the given file is in a recognized patch format.
     * 
     * @param file
     * @return true in case the file is a patch, otherwise false
     * @throws IOException in case something is wrong with the file
     */
    public boolean isPatch(File file) throws IOException;

    /**
     * Open a chooser providing a way to select a file somehow related to the IDE 
     * e.g. an expandable list of projects relevant to the what is currently 
     * opened in the IDE, so that the context for a patch action might be determined.
     * 
     * @return 
     */
    public File selectFileContext();

    public boolean providesSearchHistory(File file);
    
    /**
     * Meant to open a VCS history view where:
     * - it is possible to traverse the given files history entries 
     * - a diff view is provided, showing the selected revision compared against 
     * it's parent and positioned on the given line.
     *
     * @param file Must be a versioned file (not a folder), otherwise false is returned 
     * and the panel won't be opened
     * @param lineNumber requested line number to lock on
     * @return true if parameters are valid and the search history panel is opened, otherwise false
     */
    public boolean searchHistory(File file, int line);
    
    /**
     * Provides access to a downloadable plugin - e.g. from the NetBeans UC
     */
    public interface Plugin {
        String getDescription();
        boolean openInstallWizard();
    }
}
