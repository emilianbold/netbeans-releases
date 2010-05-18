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

import java.security.*;

import java.util.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class CollabPrincipal extends Object implements Principal, Comparable {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////
    public static final String PROP_STATUS = "status";
    public static final String PROP_PROPERTIES = "properties";
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_AWAY = 2;
    public static final int STATUS_IDLE = 3;
    public static final int STATUS_OFFLINE = 4;
    public static final int STATUS_WATCHED = 5;
    public static final int STATUS_PENDING = 6;
    public static final int STATUS_CHAT = 7;
    public static final int STATUS_BUSY = 8;
    public static final int STATUS_INVISIBLE = 9;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String name;
    private String identifier;
    private String displayName;
    private int userStatus;
    private boolean online;
    private boolean hasConversationAdminRole = false;
    private Object statusLock = new Object();
    private Map properties = new HashMap();
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    protected CollabPrincipal() {
        super();
    }

    /**
     *
     *
     */
    protected CollabPrincipal(String identifier, String name, String displayName) {
        super();
        this.identifier = identifier;
        this.name = name;
        this.displayName = displayName;
        this.userStatus = STATUS_UNKNOWN;
    }

    /**
     *
     *
     */
    public String toString() {
        return "id = " + getIdentifier() + "; name = " + getName() + // NOI18N
        "; displayName = " + getDisplayName(); // NOI18N
    }

    /**
     *
     *
     */
    public boolean equals(Object theOther) {
        if (theOther == null) {
            return false;
        }

        if (!(theOther instanceof CollabPrincipal)) {
            return false;
        }

        CollabPrincipal other = (CollabPrincipal) theOther;

        // Test equality only on the JID
        return ((other.getIdentifier() == null) && (getIdentifier() == null)) ||
        ((other.getIdentifier() != null) && other.getIdentifier().equals(getIdentifier()));
    }

    /**
     *
     *
     */
    public int hashCode() {
        if (getIdentifier() != null) {
            return getIdentifier().hashCode();
        } else {
            return super.hashCode();
        }
    }

    /**
     *
     *
     */
    public int compareTo(Object other) {
        CollabPrincipal otherPrincipal = (CollabPrincipal) other;

        String compareString1 = getDisplayName();
        String compareString2 = otherPrincipal.getDisplayName();

        if ((compareString1 == null) || (compareString2 == null)) {
            compareString1 = getName();
            compareString2 = otherPrincipal.getName();
        }

        if ((compareString1 == null) || (compareString2 == null)) {
            compareString1 = getIdentifier();
            compareString2 = otherPrincipal.getIdentifier();
        }

        if ((compareString1 == null) || (compareString2 == null)) {
            return 0; // No way to compare
        } else {
            return compareString1.compareTo(compareString2);
        }
    }

    /**
     *
     *
     */
    public abstract CollabSession getCollabSession();

    /**
     *
     *
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     *
     */
    protected void setIdentifier(String value) {
        identifier = value;
    }

    /**
     *
     *
     */
    public String getName() {
        return name;
    }

    /**
     *
     *
     */
    protected void setName(String value) {
        name = value;
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
        displayName = value;
    }

    /**
     *
     *
     */
    public String[] getPropertyNames() {
        synchronized (properties) {
            return (String[]) properties.keySet().toArray(new String[properties.size()]);
        }
    }

    /**
     *
     *
     */
    public String getProperty(String name) {
        synchronized (properties) {
            return (String) properties.get(name);
        }
    }

    /**
     *
     *
     */
    public void setProperty(String name, String value) {
        synchronized (properties) {
            String currentValue = (String) properties.get(name);

            if ((currentValue == null) || !currentValue.equals(value)) {
                properties.put(name, value);
                changeSupport.firePropertyChange(PROP_PROPERTIES, currentValue, value);
            }
        }
    }

    /**
     *
     *
     */
    public boolean hasConversationAdminRole() {
        return hasConversationAdminRole;
    }

    /**
     *
     *
     */
    public void setConversationAdminRole(boolean value) {
        hasConversationAdminRole = value;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public boolean isOnline()
    //	{
    //		synchronized (onlineLock)
    //		{
    //			return online;
    //		}
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public void setOnline(boolean value)
    //	{
    //		synchronized (onlineLock)
    //		{
    //			if (value!=online)
    //			{
    //				online=value;
    //				changeSupport.firePropertyChange(PROP_STATUS,!online,online);
    //			}
    //		}
    //	}

    /**
     *
     *
     */
    public int getStatus() {
        synchronized (statusLock) {
            return userStatus;
        }
    }

    /**
     *
     *
     */
    public void setStatus(int value) {
        int oldStatus = STATUS_UNKNOWN;

        synchronized (statusLock) {
            oldStatus = userStatus;
            userStatus = value;
        }

        changeSupport.firePropertyChange(PROP_STATUS, oldStatus, value);
    }

    /**
     *
     *
     */
    public abstract void subscribe() throws CollabException;

    /**
     *
     *
     */
    public abstract void unsubscribe() throws CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Property change methods
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
}
