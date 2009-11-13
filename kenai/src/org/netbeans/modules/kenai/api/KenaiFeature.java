/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.api.KenaiService.Type;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiFeature {

    private FeatureData featureData;
    private URL webL;
    private String loc;

    /**
     * getSource() returns KenaiFeature <br>
     * getOldValue() returns null<br>
     * getNewValue() returns instance of KenaiNotification
     */
    public static final String PROP_FEATURE_CHANGED = "feature_change";

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    KenaiFeature(FeatureData data) {
        this.featureData = data;
        this.loc = featureData.url;
        try {
            this.webL = featureData.web_url==null?null:new URL(featureData.web_url);
        } catch (MalformedURLException malformedURLException) {
            Logger.getLogger(KenaiFeature.class.getName()).log(Level.FINE, malformedURLException.getMessage(), malformedURLException);
        }
    }

    /**
     * Getter for feature name
     * @return name of feature
     */
    public String getName() {
        return featureData.name;
    }

    /**
     * getter for feature type
     * @return type of feature
     */
    public Type getType() {
        return Type.forId(featureData.type);
    }

    /**
     * extended type.
     * @return type of scm repository. Possible values are
     * "mercurial", "git", "cvs", "subversion" and "other".
     */
    public String getExtendedType() {
        return featureData.repository_type;
    }

    /**
     * getter for service name
     * @return name of service
     * @see KenaiService.Names
     */
    public String getService() {
        return featureData.service;
    }

    /**
     * getter for location of this feature
     * @return location of feature
     */
    public String getLocation() {
        return loc;
    }

    /**
     * getter for web location of this feature
     * @return web location of feature
     */
    public URL getWebLocation() {
        return webL;
    }

    /**
     * Getter for diplay name of this feature
     * @return display name
     */
    public String getDisplayName() {
        return featureData.display_name;
    }

    @Override
    public String toString() {
        return "KenaiFeature " + getName() + ", url=" + getLocation() ;
    }

}
