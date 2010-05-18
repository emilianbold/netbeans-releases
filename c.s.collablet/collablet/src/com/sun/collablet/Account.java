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
package com.sun.collablet;

import java.beans.*;

import java.io.*;


/**
 * An account descriptor. Should be generic enough to cover important IM systems
 * and connection methods.
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class Account extends Object implements Cloneable, Serializable {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final int PROXY_NONE = 0;
    public static final int PROXY_HTTPS = 1;
    public static final int PROXY_SOCKS_5 = 2;
    public static final String PROP_USER_NAME = "userName"; // NOI18N
    public static final String PROP_PASSWORD = "password"; // NOI18N
    public static final String PROP_FIRST_NAME = "firstName"; // NOI18N
    public static final String PROP_LAST_NAME = "lastName"; // NOI18N
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    public static final String PROP_EMAIL = "email"; // NOI18N
    public static final String PROP_SERVER = "server"; // NOI18N
    public static final String PROP_PROXY_TYPE = "proxyType"; // NOI18N
    public static final String PROP_PROXY_SERVER = "proxyServer"; // NOI18N
    public static final String PROP_PROXY_USER_NAME = "proxyUserName"; // NOI18N
    public static final String PROP_PROXY_PASSWORD = "proxyPassword"; // NOI18N
    public static final String PROP_VALID = "valid"; // NOI18N
    public static final String PROP_AUTO_LOGIN = "autoLogin"; // NOI18N
    public static final int EXISTING_ACCOUNT = 0;
    public static final int NEW_ACCOUNT = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String persistentID;
    private String displayName;
    private String userName;
    private String server;
    private String password;
    private transient String firstName;
    private transient String lastName;
    private transient String email;
    private int proxyType;
    private String proxyServer;
    private String proxyUserName;
    private String proxyPassword;
    private transient int accountType;
    private boolean autoLogin;
    private transient PropertyChangeSupport changeSupport;

    /**
     *
     *
     */
    public Account() {
        super();

        changeSupport = new PropertyChangeSupport(this);

        // Generate a random ID
        persistentID = "" + hashCode() + "" + Math.round(Math.random() * Integer.MAX_VALUE);
    }

    /** Fixup after deserialization */
    private Object readResolve() throws ObjectStreamException {
        changeSupport = new PropertyChangeSupport(this);

        return this;
    }

    /**
     * Returns a reasonable unique value that can be used to identify this
     * instance across serializations.  This value is constant throughout the
     * serialized lifetime of this object.
     *
     */
    public String getInstanceID() {
        // Backup for existing serialized instances
        if (persistentID == null) {
            // Generate a random ID
            persistentID = "" + hashCode() + "" + Math.round(Math.random() * Integer.MAX_VALUE);
            firePropertyChange(PROP_VALID, new Boolean(false), new Boolean(true));
        }

        return persistentID;
    }

    /**
     *
     *
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Cannot happen
            assert false : "This exception should not be possible: " + e;

            return null;
        }
    }

    /**
     *
     *
     */
    public String toString() {
        return getDisplayName() + "<" + getUserName() + "@" + // NOI18N
        getServer() + ">"; // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property accessors & mutators
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     *
     */
    public void setUserName(String value) {
        boolean isValid = isValid();

        Object oldValue = userName;
        userName = value;
        firePropertyChange(PROP_USER_NAME, oldValue, value);

        fireValidityChange(isValid);
    }

    /**
     *
     *
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     *
     */
    public void setFirstName(String value) {
        Object oldValue = firstName;
        firstName = value;
        firePropertyChange(PROP_FIRST_NAME, oldValue, value);
    }

    /**
     *
     *
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     *
     */
    public void setLastName(String value) {
        Object oldValue = lastName;
        lastName = value;
        firePropertyChange(PROP_LAST_NAME, oldValue, value);
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     *
     */
    public void setDisplayName(String value) {
        Object oldValue = displayName;
        displayName = value;
        firePropertyChange(PROP_DISPLAY_NAME, oldValue, value);
    }

    /**
     *
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     *
     */
    public void setEmail(String value) {
        Object oldValue = email;
        email = value;
        firePropertyChange(PROP_EMAIL, oldValue, value);
    }

    /**
     *
     *
     */
    public String getServer() {
        return server;
    }

    /**
     *
     *
     */
    public void setServer(String value) {
        boolean isValid = isValid();

        Object oldValue = server;
        server = value;
        firePropertyChange(PROP_SERVER, oldValue, value);

        fireValidityChange(isValid);
    }

    /**
     *
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     *
     */
    public void setPassword(String value) {
        Object oldValue = password;
        password = value;
        firePropertyChange(PROP_PASSWORD, oldValue, value);
    }

    /**
     *
     *
     */
    public int getProxyType() {
        return proxyType;
    }

    /**
     *
     *
     */
    public void setProxyType(int value) {
        boolean isValid = isValid();

        int oldValue = proxyType;
        proxyType = value;
        firePropertyChange(PROP_PROXY_TYPE, new Integer(oldValue), new Integer(value));

        fireValidityChange(isValid);
    }

    /**
     *
     *
     */
    public String getProxyServer() {
        return proxyServer;
    }

    /**
     *
     *
     */
    public void setProxyServer(String value) {
        boolean isValid = isValid();

        Object oldValue = proxyServer;
        proxyServer = value;
        firePropertyChange(PROP_PROXY_SERVER, oldValue, value);

        fireValidityChange(isValid);
    }

    /**
     *
     *
     */
    public String getProxyUserName() {
        return proxyUserName;
    }

    /**
     *
     *
     */
    public void setProxyUserName(String value) {
        boolean isValid = isValid();

        Object oldValue = proxyUserName;
        proxyUserName = value;
        firePropertyChange(PROP_PROXY_USER_NAME, oldValue, value);

        fireValidityChange(isValid);
    }

    /**
     *
     *
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     *
     *
     */
    public void setProxyPassword(String value) {
        Object oldValue = proxyPassword;
        proxyPassword = value;
        firePropertyChange(PROP_PROXY_PASSWORD, oldValue, value);
    }

    /**
     *
     *
     */
    public int getAccountType() {
        return accountType;
    }

    /**
     *
     *
     */
    public void setAccountType(int value) {
        accountType = value;
    }

    /**
     *
     *
     */
    public boolean getAutoLogin() {
        return autoLogin;
    }

    /**
     *
     *
     */
    public void setAutoLogin(boolean value) {
        Object oldValue = new Boolean(autoLogin);
        autoLogin = value;
        firePropertyChange(PROP_AUTO_LOGIN, oldValue, new Boolean(value));
    }

    /**
     *
     *
     */
    public boolean isValid() {
        if ((getUserName() == null) || (getUserName().trim().length() == 0)) {
            return false;
        }

        if ((getServer() == null) || (getServer().trim().length() == 0)) {
            return false;
        }

        if (getProxyType() == PROXY_NONE) {
            return true;
        }

        // If proxy is set to something besides none, but proxy server is not
        // set, return false
        if ((getProxyServer() == null) || (getProxyServer().trim().length() == 0)) {
            return false;
        }

        // If either auth field has text, then the other must also
        boolean userNameHasText = (getProxyUserName() != null) && (getProxyUserName().trim().length() > 0);
        boolean neitherHaveText = ((getProxyUserName() == null) || (getProxyUserName().trim().length() == 0)) &&
            ((getProxyPassword() == null) || (getProxyPassword().trim().length() == 0));

        return neitherHaveText || userNameHasText;
    }

    /**
     *
     *
     */
    private void fireValidityChange(boolean previousState) {
        if (isValid() != previousState) {
            firePropertyChange(PROP_VALID, new Boolean(previousState), new Boolean(isValid()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
