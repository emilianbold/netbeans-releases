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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.kenai.FeatureData;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiProjectFeature {

    private FeatureData featureData;
    private URL webL;
    private URL loc;
    
    KenaiProjectFeature(FeatureData data) {
        this.featureData = data;
        try {
            this.loc = featureData.url==null?null:new URL(featureData.url);
            this.webL = featureData.web_url==null?null:new URL(featureData.web_url);
        } catch (MalformedURLException malformedURLException) {
            try {
                this.webL = featureData.web_url == null ? null : new URL(System.getProperty("kenai.com.url") + "/" + featureData.web_url);
            } catch (MalformedURLException ex) {
                Logger.getLogger(KenaiProjectFeature.class.getName()).log(Level.SEVERE, malformedURLException.getMessage(), ex);
            }
            Logger.getLogger(KenaiProjectFeature.class.getName()).log(Level.FINE, malformedURLException.getMessage(), malformedURLException);
        }
    }

    public String getName() {
        return featureData.name;
    }

    public KenaiFeature getType() {
        return KenaiFeature.forId(featureData.type);
    }

    public String getService() {
        return featureData.service;
    }

    public URL getLocation() {
        return loc;
    }

    public URL getWebLocation() {
        return webL;
    }

    public String getDisplayName() {
        return featureData.display_name;
    }

    @Override
    public String toString() {
        return "KenaiProjectFeature " + getName() + ", url=" + getLocation() ;
    }

}
