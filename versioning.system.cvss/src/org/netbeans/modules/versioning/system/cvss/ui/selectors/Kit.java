/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;

import java.io.File;
import java.io.IOException;

/**
 * Utilities
 *
 * @author Petr Kuzel
 */
public final class Kit implements Client.Factory {

    private CVSRoot factory_cvsRoot;

    /** Creates Kit that servers as Client.Factory */
    private Kit(CVSRoot factory_cvsRoot) {
        this.factory_cvsRoot = factory_cvsRoot;
    }

    public static File createTmpFolder() {
        String tmpDir = System.getProperty("java.io.tmpdir");  // NOI18N
        File tmpFolder = new File(tmpDir);
        File checkoutFolder = null;
        try {
            // generate unique name for checkout folder
            File tmp = File.createTempFile("checkout", "", tmpFolder);  // NOI18N
            if (tmp.delete() == false) {
                return checkoutFolder;
            }
            if (tmp.mkdirs() == false) {
                return checkoutFolder;
            }
            tmp.deleteOnExit();
            checkoutFolder = FileUtil.normalizeFile(tmp);
        } catch (IOException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, org.openide.util.NbBundle.getMessage(Kit.class, "BK2018"));
            err.notify(e);
        }
        return checkoutFolder;
    }

    public static void deleteRecursively(File file) {
        if (file == null) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File next = files[i];
                deleteRecursively(next);  // RECURSION
            }
            file.delete();
        }
    }

    /** Creates client suitable for read only operations */
    public static Client createClient(CVSRoot cvsRoot) {
        Connection connection = ClientRuntime.setupConnection(cvsRoot);
        Client client = new Client(connection, new StandardAdminHandler());
        return client;
    }

    public static Client.Factory createClientFactory(CVSRoot cvsRoot) {
        return new Kit(cvsRoot);
    }

    public Client createClient() {
        return createClient(factory_cvsRoot);
    }
}
