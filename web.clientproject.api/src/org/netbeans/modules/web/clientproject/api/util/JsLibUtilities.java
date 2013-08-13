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
package org.netbeans.modules.web.clientproject.api.util;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.api.jslibs.JavaScriptLibrarySelectionPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Utility methods related to JS libraries.
 * @since 1.20
 */
public final class JsLibUtilities {

    private static final Logger LOGGER = Logger.getLogger(JsLibUtilities.class.getName());


    private JsLibUtilities() {
    }

    /**
     * Add JS libraries (<b>{@link JavaScriptLibrarySelectionPanel.SelectedLibrary#isDefault() non-default} only!</b>) to the given
     * site root, underneath the given JS libraries folder.
     * <p>
     * This method must be run in a background thread and stops if the current thread is interrupted.
     * @param selectedLibraries JS libraries to be added
     * @param jsLibFolder JS libraries folder, can be empty string
     * @param siteRootDir site root
     * @param handle progress handle, can be {@code null}
     * @return list of libraries that cannot be downloaded
     * @throws IOException if any error occurs
     */
    @NbBundle.Messages({
        "# {0} - library name",
        "JsLibUtilities.msg.addingJsLib=Adding {0}"
    })
    @CheckReturnValue
    public static List<JavaScriptLibrarySelectionPanel.SelectedLibrary> applyJsLibraries(List<JavaScriptLibrarySelectionPanel.SelectedLibrary> selectedLibraries,
            String jsLibFolder, FileObject siteRootDir, @NullAllowed ProgressHandle handle) throws IOException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Must be run in a background thread");
        }
        List<JavaScriptLibrarySelectionPanel.SelectedLibrary> failed = new ArrayList<JavaScriptLibrarySelectionPanel.SelectedLibrary>(selectedLibraries.size());
        FileObject librariesRoot = null;
        for (JavaScriptLibrarySelectionPanel.SelectedLibrary selectedLibrary : selectedLibraries) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (selectedLibrary.isDefault()) {
                // ignore default js lib (they are already applied)
                continue;
            }
            if (librariesRoot == null) {
                librariesRoot = FileUtil.createFolder(siteRootDir, jsLibFolder);
            }
            JavaScriptLibrarySelectionPanel.LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            assert libraryVersion != null;
            Library library = libraryVersion.getLibrary();
            if (handle != null) {
                handle.progress(Bundle.JsLibUtilities_msg_addingJsLib(library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME)));
            }
            try {
                WebClientLibraryManager.getDefault().addLibraries(new Library[]{library}, librariesRoot, libraryVersion.getType());
            } catch (MissingLibResourceException e) {
                LOGGER.log(Level.FINE, null, e);
                failed.add(selectedLibrary);
            }
        }
        // possible cleanup
        if (!failed.isEmpty()) {
            assert librariesRoot != null;
            FileObject fo = librariesRoot;
            while (!siteRootDir.equals(fo)) {
                if (fo.getChildren().length == 0) {
                    fo.delete();
                }
                fo = fo.getParent();
                assert fo != null;
            }
        }
        return failed;
    }

}
