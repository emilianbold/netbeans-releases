/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.awt.EventQueue;
import java.io.File;
import java.util.HashSet;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Keeps some information about working copies as:
 * 1) working copy is incompatible with the current client
 *
 * @author Ondrej Vrabec
 */
public final class WorkingCopyAttributesCache {
    private static WorkingCopyAttributesCache instance;
    
    private HashSet<String> unsupportedWorkingCopies;

    /**
     * Returns (and creates if needed) an instance.
     * @return an instance of this class
     */
    public static WorkingCopyAttributesCache getInstance () {
        if (instance == null) {
            instance = new WorkingCopyAttributesCache();
            instance.init();
        }
        return instance;
    }

    private WorkingCopyAttributesCache () {
        unsupportedWorkingCopies = new HashSet<String>(5);
    }

    private void init () {

    }

    /**
     * Logs the unsupported working copy exception if the file's working copy has not been stored yet.
     * The file's topmost managed parent is saved so next time the exception is not logged again.
     * The exception is thrown again.
     * @param ex
     * @param file
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    public void logUnsupportedWC(final SVNClientException ex, File file) throws SVNClientException {
        String fileName = file.getAbsolutePath();
        if (!isInUnsupportedWorkingCopies(fileName)) {
            File topManaged = Subversion.getInstance().getTopmostManagedAncestor(file);
            unsupportedWorkingCopies.add(topManaged.getAbsolutePath());
            EventQueue.invokeLater(new Runnable() {
            public void run() {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                }
            });
        }
        throw ex;
    }

    /**
     * Checks if a file denoted by fileName or some of its parents is in a working copy which has already been identified as unsupported
     * @param fileName
     * @return
     */
    public boolean isInUnsupportedWorkingCopies (String fileName) {
        for (String unsupported : unsupportedWorkingCopies) {
            if (fileName.startsWith(unsupported)) {
                return true;
            }
        }
        return false;
    }

}
