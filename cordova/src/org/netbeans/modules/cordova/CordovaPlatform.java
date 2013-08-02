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

import java.io.*;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPlatform {
    
    private static CordovaPlatform instance;
    
    private static String CORDOVA_SDK_ROOT_PREF = "cordova.home";//NOI18N

    private Version version;

    private transient final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private CordovaPlatform() {
    }

    public static synchronized CordovaPlatform getDefault() {
        if (instance == null) {
            instance = new CordovaPlatform();
        }
        return instance;
    }
    
    private static Pattern versionPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");
    
    public Version getVersion() {
        if (version == null) {
            try {
                String v = ProcessUtilities.callProcess(Utilities.isWindows()?"cordova.cmd":"cordova", true, 60*1000, "-v");
                if (versionPattern.matcher(v.trim()).matches()) {
                    version = new Version(v);
                }
            } catch (IOException ex) {
            }
        }
        return version;
    }
    

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }
    
    public boolean isReady() {
        return getVersion() != null;
    }
    
    public static boolean isCordovaProject(Project project) {
        final FileObject root = project.getProjectDirectory();
        root.refresh();
        return root.getFileObject(".cordova") != null; // NOI18N
    }

    public static class Version implements Comparable<Version> {

        String version;
        
        public Version(String version) {
            this.version = version;
        }


        @Override
        public int compareTo(Version o) {
            return version.compareTo(o.version);
        }

        @Override
        public String toString() {
            return version;
        }
        
        public boolean isSupported() {
            return compareTo(new Version(("3.0")))>0; // NOI18N
        }
    }
}

