/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        String fileName = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");            
            md5.update(realmString.getBytes());
            byte[] md5digest = md5.digest();
            for (int i = 0; i < md5digest.length; i++) {
                String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
                if(hex.length()==1) {
                    hex = "0" + hex;
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
