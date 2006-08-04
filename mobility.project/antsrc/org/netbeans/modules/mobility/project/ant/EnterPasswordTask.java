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

/*
 * EnterPasswordTask.java
 *
 * Created on 11. leden 2005, 12:10
 */
package org.netbeans.modules.mobility.project.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.mobility.project.ui.security.EnterPasswordPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class EnterPasswordTask extends Task {
    
    /**
     * Holds value of property keyStore.
     */
    private String keyStore;
    
    /**
     * Holds value of property keyAlias.
     */
    private String keyAlias;
    
    /**
     * Holds value of property keyFile.
     */
    private String keyFile;
    
    /**
     * Holds value of property connectionId.
     */
    private String connectionId;
    
    /**
     * Holds value of property userName.
     */
    private String userName;
    
    /**
     * Holds value of property userNameProperty.
     */
    private String userNameProperty;
    
    /**
     * Holds value of property passwordProperty.
     */
    private String passwordProperty;    
    
    /** Creates a new instance of EnterPasswordTask */
    public EnterPasswordTask() {
    }
    
    public void execute() throws BuildException {
        if (passwordProperty == null) throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_PasswordPropertyrequired.")); //NOI18N
        boolean a = keyStore != null, b = keyFile != null, c = connectionId != null, d = userNameProperty != null;
        if (a ? b || c : b && c) throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_JustOneOfTheAttributes")); //NOI18N
        if (d && (a || b)) throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_InvalidUsernameUsage")); //NOI18N
        String password = null;
        if (a) {
            if (!new File(keyStore).isFile()) throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_InvalidKeystore")); //NOI18N
            password = keyAlias == null ? EnterPasswordPanel.getKeystorePassword(keyStore) : EnterPasswordPanel.getAliasPassword(keyStore, keyAlias);
        } else if (b) {
            password = EnterPasswordPanel.getKeyfilePassword(keyFile);
        } else if (c) {
            if (d) {
                String[] s  = EnterPasswordPanel.getConnectionUsernameAndPassword(connectionId, userName);
                if (s != null) {
                    getProject().setProperty(userNameProperty, s[0]);
                    password = s[1];
                }
            } else {
                password = EnterPasswordPanel.getConnectionPassword(connectionId, userName);
            }
        } else throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_RequiredAttribute")); //NOI18N
        if (password == null) throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_Cancelled")); //NOI18N
        getProject().setProperty(passwordProperty, password);
    }
    
    /**
     * Setter for property keyStore.
     * @param keystore New value of property keyStore.
     */
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }
    
    /**
     * Setter for property keyAlias.
     * @param keyAlias New value of property keyAlias.
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
    
    /**
     * Setter for property keyFile.
     * @param keyFile New value of property keyFile.
     */
    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }
    
    /**
     * Setter for property connectionId.
     * @param connectionId New value of property connectionId.
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * Setter for property userName.
     * @param userName New value of property userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Setter for property userNameProperty.
     * @param userNameProperty New value of property userNameProperty.
     */
    public void setUserNameProperty(String userNameProperty) {
        this.userNameProperty = userNameProperty;
    }
    
    /**
     * Setter for property passwordProperty.
     * @param passwordProperty New value of property passwordProperty.
     */
    public void setPasswordProperty(String passwordProperty) {
        this.passwordProperty = passwordProperty;
    }
    
}
