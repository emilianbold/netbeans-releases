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

package org.netbeans.modules.updatecenters.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Jiri Rechtacek
 */
public final class NetBeansKeyStoreProvider implements KeyStoreProvider {
    
    public static final String KS_FILE_PATH = "core" + System.getProperty ("file.separator") + "ide.ks";
    private static final String KS_DEFAULT_PASSWORD = "open4all";
    
    public KeyStore getKeyStore() {
        return getKeyStore (getKeyStoreFile (), getPassword ());
    }
    
    private static File getKeyStoreFile () {
        File ksFileLocated = InstalledFileLocator.getDefault ().locate (KS_FILE_PATH, null, true);
        assert ksFileLocated != null : "File found at " + KS_FILE_PATH;
        return ksFileLocated;
    }

    /** Creates keystore and loads data from file.
    * @param filename - name of the keystore
    * @param password
    */
    private static KeyStore getKeyStore(File file, String password) {
        if (file == null) return null;
        KeyStore keyStore = null;
        InputStream is = null;
        
        try {

            is = new FileInputStream (file);

            keyStore = KeyStore.getInstance (KeyStore.getDefaultType ());
            keyStore.load (is, password.toCharArray ());
            
        } catch (Exception ex) {
            Logger.getLogger ("global").log (Level.INFO, ex.getMessage (), ex);
        } finally {
            try {
                if (is != null) is.close ();
            } catch (IOException ex) {
                assert false : ex;
            }
        }

        return keyStore;
    }
    
    private static String getPassword () {
        String password = KS_DEFAULT_PASSWORD;
        //XXX: read password from bundle
        return password;
    }

}
