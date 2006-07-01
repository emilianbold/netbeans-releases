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
package org.netbeans.modules.subversion.config;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.openide.ErrorManager;

/**
 * Handles the Subversion credential files.
 *
 * @author Tomas Stupka
 *
 */
public abstract class SVNCredentialFile extends KVFile {


    /**
     * Creates sa new instance
     */
    protected SVNCredentialFile(File file) {
        super(file);
    }

    /**
     * Returns the filename for a realmString as a MD5 value in hex form.
     */
    protected static String getFileName(String realmString) {
        assert realmString != null;        
        String fileName = ""; // NOI18N
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
            md5.update(realmString.getBytes());
            byte[] md5digest = md5.digest();
            for (int i = 0; i < md5digest.length; i++) {
                String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
                if(hex.length()==1) {
                    hex = "0" + hex; // NOI18N
                }
                fileName += hex;
            }            
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e); // should not happen
        }                        
        
        return fileName;
    }    
    
    protected abstract String getRealmString();    
    protected abstract void setRealmString(String realm);        
}
