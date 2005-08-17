/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.openide.ErrorManager;
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
    private ProxyDescriptor factory_proxy;

    /** Creates Kit that servers as Client.Factory */
    private Kit(CVSRoot factory_cvsRoot, ProxyDescriptor factory_proxy) {
        this.factory_cvsRoot = factory_cvsRoot;
        this.factory_proxy = factory_proxy;
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
            checkoutFolder = tmp;
        } catch (IOException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Can not create temporary folder.");
            err.notify(e);
        }
        return checkoutFolder;
    }

    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File next = files[i];
                deleteRecursively(next);  // RECURSION
            }
            file.delete();
        }
    }

    public static Client createClient(CVSRoot cvsRoot, ProxyDescriptor proxy) {
        Connection connection = ClientRuntime.setupConnection(cvsRoot, proxy);
        Client client = new Client(connection, new StandardAdminHandler());
        return client;
    }

    public static Client.Factory createClientFactory(CVSRoot cvsRoot, ProxyDescriptor proxy) {
        return new Kit(cvsRoot, proxy);
    }

    public Client createClient() {
        return createClient(factory_cvsRoot, factory_proxy);
    }
}
