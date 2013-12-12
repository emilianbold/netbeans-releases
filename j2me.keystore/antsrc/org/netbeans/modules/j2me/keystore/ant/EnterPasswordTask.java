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

package org.netbeans.modules.j2me.keystore.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.j2me.keystore.ui.EnterPasswordPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class EnterPasswordTask extends Task {

    /**
     * Holds value of property keyStore.
     */
    private File keyStore;

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

    /**
     * Creates a new instance of EnterPasswordTask
     */
    public EnterPasswordTask() {
    }

    @Override
    public void execute() throws BuildException {
        if (passwordProperty == null) {
            throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_PasswordPropertyrequired.")); //NOI18N
        }
        boolean a = keyStore != null, b = keyFile != null, c = connectionId != null, d = userNameProperty != null;
        if (a ? b || c : b && c) {
            throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_JustOneOfTheAttributes")); //NOI18N
        }
        if (d && (a || b)) {
            throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_InvalidUsernameUsage")); //NOI18N
        }
        String password = null;
        if (a) {
            if (!keyStore.isFile()) {
                throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_InvalidKeystore")); //NOI18N
            }
            password = keyAlias == null ? EnterPasswordPanel.getKeystorePassword(keyStore.getAbsolutePath()) : EnterPasswordPanel.getAliasPassword(keyStore.getAbsolutePath(), keyAlias);
        } else if (b) {
            password = EnterPasswordPanel.getKeyfilePassword(keyFile);
        } else if (c) {
            if (d) {
                String[] s = EnterPasswordPanel.getConnectionUsernameAndPassword(connectionId, userName);
                if (s != null) {
                    getProject().setProperty(userNameProperty, s[0]);
                    password = s[1];
                }
            } else {
                password = EnterPasswordPanel.getConnectionPassword(connectionId, userName);
            }
        } else {
            throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_RequiredAttribute")); //NOI18N
        }
        if (password == null) {
            throw new BuildException(NbBundle.getMessage(EnterPasswordTask.class, "ERR_Cancelled")); //NOI18N
        }
        getProject().setProperty(passwordProperty, password);
    }

    /**
     * Setter for property keyStore.
     *
     * @param keystore New value of property keyStore.
     */
    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Setter for property keyAlias.
     *
     * @param keyAlias New value of property keyAlias.
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Setter for property keyFile.
     *
     * @param keyFile New value of property keyFile.
     */
    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    /**
     * Setter for property connectionId.
     *
     * @param connectionId New value of property connectionId.
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Setter for property userName.
     *
     * @param userName New value of property userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Setter for property userNameProperty.
     *
     * @param userNameProperty New value of property userNameProperty.
     */
    public void setUserNameProperty(String userNameProperty) {
        this.userNameProperty = userNameProperty;
    }

    /**
     * Setter for property passwordProperty.
     *
     * @param passwordProperty New value of property passwordProperty.
     */
    public void setPasswordProperty(String passwordProperty) {
        this.passwordProperty = passwordProperty;
    }

}
