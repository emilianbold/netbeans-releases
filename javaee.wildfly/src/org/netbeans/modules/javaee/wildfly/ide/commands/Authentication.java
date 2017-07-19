/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import org.openide.util.Exceptions;

/**
 *
 * @author ehugonnet
 */
public class Authentication {

    private final String username;
    private final char[] password;
    private CallbackHandler callBackHandler;

    public Authentication() {
        this.username = "";
        this.password = new char[0];
        this.callBackHandler = createCallBackHandler();
    }

    public Authentication(String username, char[] password) {
        this.username = username;
        this.password = Arrays.copyOf(password, password.length);
        this.callBackHandler = createCallBackHandler();
    }
    
    private CallbackHandler createCallBackHandler(){
        return new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                String realm = null;
                String username = null;
                for (Callback current : callbacks) {
                    if(isOptionalNameCallback(current)) {
                        NameCallback nameCallback = (NameCallback) current;
                        nameCallback.setName(nameCallback.getDefaultName());
                    } else if (current instanceof NameCallback && username != null && !username.isEmpty()) {
                        NameCallback nameCallback = (NameCallback) current;
                        nameCallback.setName(username);
                    } else if (current instanceof PasswordCallback) {
                        PasswordCallback pwdCallback = (PasswordCallback) current;
                        pwdCallback.setPassword(password);
                    } else if (current instanceof RealmCallback) {
                        RealmCallback realmCallback = (RealmCallback) current;
                        realm = realmCallback.getDefaultText();
                        realmCallback.setText(realmCallback.getDefaultText());
                    } else if (isCredentialCallBack(current)) {
                        setPassword(current, realm);
                    } else {
                        throw new UnsupportedCallbackException(current);
                    }
                }
            }
        };
    }

    public CallbackHandler getCallbackHandler() {
        return this.callBackHandler;
    }

    private boolean isOptionalNameCallback(Callback callback) {
        try {
            return callback.getClass().getClassLoader().loadClass("org.wildfly.security.auth.callback.OptionalNameCallback").isAssignableFrom(callback.getClass());
        } catch (ClassNotFoundException ex) {
           return false;
        }
    }

    private boolean isCredentialCallBack(Callback callback) {
        try {
            return callback.getClass().getClassLoader().loadClass("org.wildfly.security.auth.callback.CredentialCallback").isAssignableFrom(callback.getClass());
        } catch (ClassNotFoundException ex) {
           return false;
        }
    }

    private void setPassword(Callback callback, String realm) {
        try {
            ClassLoader cl = callback.getClass().getClassLoader();
            Class passwordCredentialClass = cl.loadClass("org.wildfly.security.credential.PasswordCredential");
            Method isCredentialTypeSupportedMethod = callback.getClass().getDeclaredMethod("isCredentialTypeSupported", new Class[]{Class.class, String.class});
            Boolean isCredentialTypeSupported = (Boolean) isCredentialTypeSupportedMethod.invoke(callback, new Object[]{passwordCredentialClass, "clear"});
            if(isCredentialTypeSupported) {
                Object clearPassword = cl.loadClass("org.wildfly.security.password.interfaces.ClearPassword").getDeclaredMethod("createRaw", new Class[]{String.class, char[].class}).invoke(null, new Object[]{"clear", password});
                Constructor passwordCredentialConstructor = passwordCredentialClass.getConstructor( new Class[]{cl.loadClass("org.wildfly.security.password.Password")});
                Object passwordCredential= passwordCredentialConstructor.newInstance(clearPassword);
                Class credentialClass = cl.loadClass("org.wildfly.security.credential.Credential");
                Method setCredentialMethod = callback.getClass().getDeclaredMethod("setCredential", new Class[]{credentialClass});
                setCredentialMethod.invoke(callback, passwordCredential);
            } else if (realm != null) {
                 isCredentialTypeSupported = (Boolean) isCredentialTypeSupportedMethod.invoke(callback, new Object[]{passwordCredentialClass, "digest-md5"});
                 if(isCredentialTypeSupported) {
                     Class passwordAlgorithmSpecClass = cl.loadClass("org.wildfly.security.password.spec.DigestPasswordAlgorithmSpec");
                     Object algoSpec = passwordAlgorithmSpecClass.getConstructor(new Class[]{String.class, String.class}).newInstance(username, realm);
                     Class encryptPasswordSpecClass = cl.loadClass("org.wildfly.security.password.spec.EncryptablePasswordSpec");
                     Object passwordSpec = encryptPasswordSpecClass.getConstructor(new Class[]{char[].class, passwordAlgorithmSpecClass}).newInstance(this.password, algoSpec);
                     Class passwordFactoryClass = cl.loadClass("org.wildfly.security.password.PasswordFactory");
                     Object passwordFactory = passwordFactoryClass.getDeclaredMethod("getInstance", String.class).invoke(null, "digest-md5");
                     Object passwordCredential= passwordFactoryClass.getDeclaredMethod("generatePassword", encryptPasswordSpecClass).invoke(passwordFactory, passwordSpec);
                    Class credentialClass = cl.loadClass("org.wildfly.security.credential.Credential");
                    Method setCredentialMethod = callback.getClass().getDeclaredMethod("setCredential", new Class[]{credentialClass});
                    setCredentialMethod.invoke(callback, passwordCredential);
                 }
            }

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
