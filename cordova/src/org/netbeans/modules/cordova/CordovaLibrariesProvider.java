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
package org.netbeans.modules.cordova;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Jan Becicka
 */
@ServiceProvider(service = org.netbeans.spi.project.libraries.LibraryProvider.class)
public class CordovaLibrariesProvider implements LibraryProvider<LibraryImplementation>, PropertyChangeListener {

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public CordovaLibrariesProvider() {
        CordovaPlatform.getDefault().addPropertyChangeListener(this);
    }
    
    
    
    @Override
    public LibraryImplementation[] getLibraries() {
        if (!CordovaPlatform.getDefault().isReady()) {
            return new LibraryImplementation[0];
        }
        LibraryImplementation3 lib = (LibraryImplementation3) LibrariesSupport.createLibraryImplementation("javascript",new String[]{"regular", "documented", "minified"}); // NOI18N
            
            lib.setName("Cordova"); // NOI18N
            lib.setDisplayName("Cordova"); // NOI18N
            Map<String, String> p = new HashMap<String, String>();
            final String version = CordovaPlatform.getDefault().getVersion().toString();
            p.put("version", version);//NOI18N
            p.put("name", "Cordova"); //NOI18N
            p.put("displayname", "Cordova"); //NOI18N
            p.put("cdn", "Phonegap"); // NOI18N
            p.put("site", "http://phonegap.com");//NOI18N
            lib.setProperties(p);
        try {
            final String sdkLocation = CordovaPlatform.getDefault().getSdkLocation();
            final List<URL> libs = Collections.singletonList(new URL("file://" + sdkLocation + "/lib/android/cordova-"+version +".js")); //NOI18N
            lib.setContent("regular", libs);//NOI18N
            lib.setContent("minified", libs);//NOI18N
            lib.setContent("documented", libs);//NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
            
        return new LibraryImplementation[]{lib};
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(PROP_LIBRARIES, null, getLibraries());
    }
}
