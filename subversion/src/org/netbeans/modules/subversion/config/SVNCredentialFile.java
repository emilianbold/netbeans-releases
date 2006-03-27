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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;

/**
 *
 * @author Tomas Stupka
 *
 */
public abstract class SVNCredentialFile extends KVFile {
        

    protected SVNCredentialFile(File file) {
        super(file);
    }        

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
            e.printStackTrace(); // should not happen
        }                        
        
        return fileName;
    }    
    
    protected abstract String getRealmString();
    protected abstract void setRealmString(String realm);    

    // XXX serializable
    public static class CertificateFile extends SVNCredentialFile {
        
        private final static Key CERT = new Key(0, "ascii_cert");
        private final static Key FAILURES = new Key(1, "failures");        
        private final static Key REALMSTRING = new Key(2, "svn:realmstring");
    
        public CertificateFile(String realmString) {
            super(new File(SvnConfigFiles.getNBConfigDir() + "auth/svn.ssl.server/" + getFileName(realmString)));            
        }

        // XXX the setters do not override the already present value 
        protected void setRealmString(String realm) {
            setValue(REALMSTRING, realm);
        }

        protected String getRealmString() {
            return new String(getValue(REALMSTRING));
        }        
    
        public void setCert(byte[] cert) {
            setValue(CERT, cert);
        }
        
        public void setFailures(String failures) {
            setValue(FAILURES, failures);
        }        
        
        public void deleteOnExit() {
            getFile().deleteOnExit();
        }
        
    }
}
