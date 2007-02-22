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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
