/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.c2c.api;

import com.tasktop.c2c.server.profile.domain.project.Profile;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;
import javax.swing.Icon;
import org.netbeans.modules.team.c2c.client.api.ClientFactory;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.c2c.client.api.CloudException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Vrabec
 */
public final class CloudServer {
    
    /**
     * fired when user logs in/out
     * getOldValue() returns old PasswordAuthentication or null
     * getNewValue() returns new PasswordAuthentication or null
     */
    public static final String PROP_LOGIN = "login";
    
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final URL url;
    private final String displayName;
    private Icon icon;
    private PasswordAuthentication auth;
    private Profile currentProfile;

    private CloudServer (String displayName, String url) throws MalformedURLException {
        while (url.endsWith("/")) { //NOI18N
            url = url.substring(0, url.length() - 1);
        }
        this.displayName = displayName;
        this.url = new URL(url);
    }

    static CloudServer createInstance (String displayName, String url) throws MalformedURLException {
        return new CloudServer(displayName, url);
    }
    
    /**
     * Adds listener to Kenai instance
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to Kenai instance
     * @param name 
     * @param l
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * Removes listener from Kenai instance
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from Kenai instance
     * @param name
     * @param l
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(name, l);
    }

    public URL getUrl () {
        return url;
    }

    public String getDisplayName () {
        return displayName;
    }

    public Icon getIcon () {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/team/c2c/resources/server.png", false); //NOI18N
        }
        return icon;
    }

    public void logout () {
        PasswordAuthentication old = auth;
        synchronized(this) {
            auth = null;
            currentProfile = null;
        }
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, PROP_LOGIN, old, auth);
        firePropertyChange(propertyChangeEvent);
    }

    public boolean isLoggedIn () {
        return auth != null;
    }

    public PasswordAuthentication getPasswordAuthentication () {
        return auth;
    }

    public void login (String username, char[] password) throws CloudException {
        CloudClient createClient = ClientFactory.getInstance().createClient(getUrl().toString(), new PasswordAuthentication(username, password.clone()));
        currentProfile = createClient.getCurrentProfile();
        auth = new PasswordAuthentication(username, password.clone());
        Arrays.fill(password, '\0');
    }

    private void firePropertyChange (PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
        CloudServerManager.getDefault().propertyChangeSupport.firePropertyChange(event);
    }
}
