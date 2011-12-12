/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.filesystems;

import java.awt.Image;
import java.io.IOException;
import java.util.*;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusListener;

/**
 * Work in progress - summarizes the current communication between VCS and masterfs 
 * 
 * @author Tomas Stupka
 */
public final class VCSFilesystemInterceptor {
    
    // ==================================================================================================
    // ANNOTATIONS
    // ==================================================================================================

    /** Listeners are held weakly, and can GC if nobody else holds them */
    public static void registerFileStatusListener(FileStatusListener listener) {}
    
    public static Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return null;
    }
    
    public static String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return null;
    }
    
    public static Action[] actions(Set<? extends FileObject> files) {
        return null;
    }

    
    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Determines if the given file is writable
     * @param file
     * @return 
     */
    public static boolean canWrite(VCSFileProxy file) {
        return false;
    }

    /**
     * Returns the given files files attribute
     * @param file
     * @param attrName
     * @return 
     */
    public static Object getAttribute(VCSFileProxy file, String attrName) {
        return null;
    }
    
    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    public static void beforeChange(VCSFileProxy file) {}
    
    public static void fileChanged(VCSFileProxy file) {}

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    public static DeleteHandler getDeleteHandler(VCSFileProxy file) {
        return null;
    }

    public static void deleteSuccess(VCSFileProxy file) {}

    public static void deletedExternally(VCSFileProxy file) {}
   
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    public static void beforeCreate(VCSFileProxy parent, String name, boolean isFolder) {}

    public static void createFailure(VCSFileProxy parent, String name, boolean isFolder) {}

    public static void createSuccess(VCSFileProxy file) {}

    public static void createdExternally(VCSFileProxy file) {}

    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public static IOHandler getMoveHandler(VCSFileProxy from, VCSFileProxy to) {
        return null;
    }

    public static IOHandler getRenameHandler(VCSFileProxy from, String newName) {
        return null;
    }

    public static void afterMove(VCSFileProxy from, VCSFileProxy to) {}

    // ==================================================================================================
    // COPY
    // ==================================================================================================

    public static IOHandler getCopyHandler(VCSFileProxy from, VCSFileProxy to) {
        return null;
    }

    public static void beforeCopy(VCSFileProxy from, VCSFileProxy to) {}
    
    public static void copySuccess(VCSFileProxy from, VCSFileProxy to) {}

    // ==================================================================================================
    // MISC
    // ==================================================================================================    
    
    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a VCSFileProxy
     */
    public static void fileLocked(VCSFileProxy fo) {}

    public static long listFiles(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        return -1;
    }
    
    // ==================================================================================================
    // HANDLERS
    // ==================================================================================================
    
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
    
    public interface DeleteHandler {
        /**
         * Deletes the file or directory denoted by this abstract pathname.  If
         * this pathname denotes a directory, then the directory must be empty in
         * order to be deleted.
         *
         * @return  <code>true</code> if and only if the file or directory is
         *          successfully deleted; <code>false</code> otherwise
         */
        boolean delete(VCSFileProxy file); // XXX IOException ?
    }    

}