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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.netbeans.modules.subversion.config.KVFile.Key;
import org.openide.filesystems.FileUtil;

/**
 * Represents a Subversions file holding a X509Certificate for a realmstring.
 *
 * @author Tomas Stupka
 */
public class CertificateFile extends SVNCredentialFile {
        
    private final static Key CERT = new Key(0, "ascii_cert");
    private final static Key FAILURES = new Key(1, "failures");        
    private final static Key REALMSTRING = new Key(2, "svn:realmstring");
    private static final String NEWLINE = System.getProperty("line.separator"); 
    
    public CertificateFile(X509Certificate cert, String realmString, int failures, boolean temporarily) throws CertificateEncodingException {
        super(getNBCertFile(realmString));
        setCert(cert);
        setFailures(failures);
        if(temporarily) {
            getFile().deleteOnExit();
        }
    }

    private void setCert(X509Certificate cert) throws CertificateEncodingException {
        String encodedCert = new sun.misc.BASE64Encoder().encode(cert.getEncoded());        
        encodedCert = encodedCert.replace(NEWLINE, ""); // XXX where does this come from ????!!!        
        setValue(CERT, encodedCert.getBytes());
    }
        
    protected void setRealmString(String realm) {
        setValue(REALMSTRING, realm);
    }

    protected String getRealmString() {
        return new String(getValue(REALMSTRING));
    }        

    private void setFailures(int failures) {
        setValue(FAILURES, String.valueOf(failures));
    }        

    public static File getSystemCertFile(String realmString) {
        File file = new File(SvnConfigFiles.getUserConfigPath() + "auth/svn.ssl.server/" + getFileName(realmString));
        return FileUtil.normalizeFile(file);
    }

    public static File getNBCertFile(String realmString) {
        File file = new File(SvnConfigFiles.getNBConfigPath() + "auth/svn.ssl.server/" + getFileName(realmString));
        return FileUtil.normalizeFile(file);
    }

}
