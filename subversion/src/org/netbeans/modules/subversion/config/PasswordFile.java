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
import java.io.IOException;
import org.netbeans.modules.subversion.config.KVFile.Key;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a file holding the username and password credentials for a realmstring.
 *
 * @author Tomas Stupka
 */
public class PasswordFile extends SVNCredentialFile {

    private static final long serialVersionUID = 1L;

    private final static Key PASSTYPE = new Key(0, "passtype");
    private final static Key PASSWORD = new Key(1, "password");
    private final static Key REALMSTRING = new Key(2, "svn:realmstring");
    private final static Key USERNAME = new Key(3, "username");

    private final static String PASSTYPE_SIMPLE = "simple";
        
    public PasswordFile (String realmString) {
        super(getFile(realmString));
    }

    private PasswordFile (File file) {
        super(file);
    }

    /**
     * Goes through the Netbeans Subversion modules configuration directory and looks
     * for a file holding the username and password for the givenurl.
     *
     * @param svnUrl the url 
     * @return the file holding the username and password for the givenurl or null 
     *         if nothing was found    
     */
    public static PasswordFile findFileForUrl(SVNUrl svnUrl) {
        // create our own realmstring  -
        String realmString = "<" + svnUrl.getProtocol() + "://" + svnUrl.getHost() + ">";
        PasswordFile nbPasswordFile = new PasswordFile(realmString);
        
        if(!nbPasswordFile.getFile().exists()) {

            File configDir = new File(SvnConfigFiles.getUserConfigPath() + "/auth/svn.simple");
            File[] files = configDir.listFiles();
            if(files==null) {
                return null;
            }
            for (int i = 0; i < files.length; i++) {
                PasswordFile passwordFile = new PasswordFile(files[i]);
                if(passwordFile.acceptSvnUrl(svnUrl) &&
                   passwordFile.getPasstype().equals(PASSTYPE_SIMPLE)) // windows likes to use wincryp, but we can accept only plain text
                {
                    // XXX overwrites the value given by svn with our own -> could potentialy cause a conflict
                    passwordFile.setRealmString(realmString); 
                    return passwordFile;
                }
            }
            
            // no password file - let's create an empty one then...
            nbPasswordFile.setRealmString(realmString);
            nbPasswordFile.setPasstype(PASSTYPE_SIMPLE);
            nbPasswordFile.setPassword("");
            nbPasswordFile.setUsername("");            
            return nbPasswordFile;
            
        } else {
            return nbPasswordFile;
        }        
    }

    public void store() throws IOException {
        store(getFile(getRealmString()));
    }

    public String getPassword() {
        return new String(getValue(PASSWORD));
    }

    public String getUsername() {
        return new String(getValue(USERNAME));
    }

    public void setPassword(String password) {
        setValue(PASSWORD, password);
    }

    public void setUsername(String username) {
        setValue(USERNAME, username);
    }
    
    protected String getRealmString() {
        return new String(getValue(REALMSTRING));
    }

    protected void setRealmString(String realm) {
        setValue(REALMSTRING, realm.getBytes());
    }

    private void setPasstype(String passtype) {
        setValue(PASSTYPE, passtype);
    }

    private String getPasstype() {
        return new String(getValue(PASSTYPE));
    }

    private boolean acceptSvnUrl(SVNUrl svnUrl) {
        if(svnUrl==null) {
            return false;
        }        
        String realmStrig = getRealmString();
        if(realmStrig==null || realmStrig.length() < 6 ) {
            // at least 'svn://'
            return false;
        }
        return realmStrig.substring(1).startsWith(svnUrl.getProtocol() + "://" + svnUrl.getHost());         
    }
    
    private static File getFile(String realmString) {
        return new File(SvnConfigFiles.getNBConfigPath() + "auth/svn.simple/" + getFileName(realmString));
    }
    
}
