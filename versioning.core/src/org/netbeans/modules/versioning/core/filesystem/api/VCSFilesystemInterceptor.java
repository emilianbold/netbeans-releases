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
package org.netbeans.modules.versioning.core.filesystem.api;

import org.netbeans.modules.versioning.core.spi.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.versioning.fileproxy.api.VCSFileProxy;

/**
 *
 * @author Tomas Stupka
 */
public final class VCSFilesystemInterceptor {

    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Determines if the given file is writable
     * @param file
     * @return 
     */
    public boolean canWrite(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the given files files attribute
     * @param file
     * @param attrName
     * @return 
     */
    public Object getAttribute(VCSFileProxy file, String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    public void beforeChange(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void fileChanged(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    public DeleteHandler getDeleteHandler(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteSuccess(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deletedExternally(VCSFileProxy file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    public void beforeCreate(VCSFileProxy parent, String name, boolean isFolder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createFailure(VCSFileProxy parent, String name, boolean isFolder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createSuccess(VCSFileProxy fo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createdExternally(VCSFileProxy fo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public IOHandler getMoveHandler(VCSFileProxy from, VCSFileProxy to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IOHandler getRenameHandler(VCSFileProxy from, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void afterMove(VCSFileProxy from, VCSFileProxy to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ==================================================================================================
    // COPY
    // ==================================================================================================

    public IOHandler getCopyHandler(VCSFileProxy from, VCSFileProxy to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beforeCopy(VCSFileProxy from, VCSFileProxy to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void copySuccess(VCSFileProxy from, VCSFileProxy to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ==================================================================================================
    // MISC
    // ==================================================================================================    
    
    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a VCSFileProxy
     */
    public void fileLocked(VCSFileProxy fo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        throw new UnsupportedOperationException("Not supported yet.");
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
