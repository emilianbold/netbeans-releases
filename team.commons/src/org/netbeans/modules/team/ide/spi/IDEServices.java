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

package org.netbeans.modules.team.ide.spi;

import java.io.File;
import java.io.IOException;
import javax.swing.Icon;

/**
 *
 * @author Tomas Stupka
 */
public interface IDEServices {
    
    /**
     * Determines whether the functionality to open a document for a resource (file)
     * is available. <br>
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesOpenDocument();
    
    /**
     * Opens a document representing the given resource. 
     * <b>Note</b> that the given path doesn't necessarily have to be a fully qualified path, but 
     * might be in a shorter form as given by e.g. an stacktrace - org/netbeans/modules/bugzilla/Bugzilla.java

     * @param resourcePath
     * @param offset 
     */
    public void openDocument(String resourcePath, int offset);
    
    /**
     * Determines whether the functionality to jump to a resource is available. 
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesJumpTo();
    
    /**
     * 
     * Opens a search/find resource UI prefilled with the given resource. 
     * <br>
     * <b>Note</b> that the given resource doesn't necessarily have to be a be a fully qualified path, but 
     * might be just an arbitrary string potentially identifying e.g. a java type.
     * 
     * @param resource
     * @param title 
     */
    public void jumpTo(String resource, String title);

    /**
     * Determines whether the functionality to download a plugin is available 
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesPluginUpdate();
    
    /**
     * Returns a Plugin with the given code name base in case there is none installed, 
     * or that the currently installed version is lesser than the installed.
     * 
     * @param cnb - the plugins code name base
     * @param pluginName the plugins name - e.g. Bugzilla or Jira
     * @return plugin or null if not available
     */
    public Plugin getPluginUpdates(String cnb, String pluginName);

    /**
     * Determines whether patch relevant functionality is available.
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesPatchUtils();

    /**
     * Applies the given patch file.
     * 
     * @param patchFile the patch files
     */
    public void applyPatch(File patchFile);

    /**
     * Determines whether the given file is in a recognized patch format.
     * 
     * @param patchFile
     * @return true in case the file is a patch, otherwise false
     * @throws IOException in case something is wrong with the file
     */
    public boolean isPatch(File patchFile) throws IOException;

    /**
     * Determines whether the functionality to open the History for a resource (file)
     * is available.
     * 
     * @return <code>true</code> if available, otherwise <code>false</code>
     */
    public boolean providesOpenHistory();

    /**
     * Meant to open a VCS history view where:
     * - it is possible to traverse the given resource history entries 
     * - a diff view is provided, showing the selected revision compared against 
     * it's parent and positioned on the given line.
     *
     * @param resourcePath resourcePath representing a versioned file (not a folder). 
     * <b>Note</b> that the given path doesn't necessarily have to be the full path, but 
     * might be a shorter form as given by e.g. an stacktrace - org/netbeans/modules/bugzilla/Bugzilla.java
     * @param line requested line number to lock on
     * @return true if parameters are valid, the file is versioned and the history view was opened, 
     * otherwise false.
     */
    public boolean openHistory(String resourcePath, int line);
    
    /**
     * Creates an animated busy icon (used e.g. in ProgressLabel) to be shown in
     * UI (like the treelist nodes) that perform some operation (e.g. searching).
     * May return null.
     * 
     * @return <code>BusyIcon</code> implementation of an animated busy icon, or
     *         <code>null</code> if no specific implementation is available
     */
    public BusyIcon createBusyIcon();

    /**
     * Determines whether the capability of opening a directory in a file browse
     * UI (e.g. Favorites window in NetBeans) is available.
     * @return <code>true</code> if can open a directory in a Favorites UI, otherwise <code>false</code>
     */
    public boolean canOpenInFavorites();

    /**
     * Opens given directory in file browser (Favorites).
     * @param workingDir 
     */
    public void openInFavorites(File workingDir);

    /**
     * Determines whether the capability of opening a http proxy configuration UI
     * is available.
     * 
     * @return <code>true</code> if there is a http proxy configuration UI available, otherwise <code>false</code>
     */
    public boolean providesProxyConfiguration();
    
    /**
     * Opens the http proxy configuration UI
     */
    public void openProxyConfiguration();
    
    /**
     * Provides access to a downloadable plugin - e.g. from the NetBeans UC
     */
    public interface Plugin {
        /**
         * Returns the plugins description
         * @return the plugins description
         */
        String getDescription();
        
        /**
         * Install or Update the plugin. 
         * @return <code>true</code> in case it was possible to install the plugin, otherwise <code>false</code> 
         */
        boolean installOrUpdate();
    }

    /**
     * Defines interface for an animated busy icon (used e.g. in ProgressLabel).
     * Implementation can use a specific library, e.g. SwingX.
     */
    public interface BusyIcon extends Icon {
        /**
         * Called by timer (run by ProgressLabel) for next animation step.
         */
        public void tick();
    }
}
