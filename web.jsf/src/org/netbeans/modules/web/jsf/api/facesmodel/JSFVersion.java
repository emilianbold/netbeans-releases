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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.jsf.api.facesmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Pisl, ads, Martin Fousek
 */
public enum JSFVersion {
    JSF_1_0,
    JSF_1_1,
    JSF_1_2,
    JSF_2_0,
    JSF_2_1,
    JSF_2_2;
    
    private static final RequestProcessor RP = new RequestProcessor(JSFVersion.class);

    // caches for holding JSF version and the project CP listeners
    private static final Map<WebModule, JSFVersion> projectVersionCache = new WeakHashMap<WebModule, JSFVersion>();
    private static final Map<WebModule, PropertyChangeListener> projectListenerCache = new WeakHashMap<WebModule, PropertyChangeListener>();

    /**
     * Gets the JSF version supported by the WebModule. It seeks for the JSF only on the classpath.
     *
     * @param webModule WebModule to seek for JSF version
     * @param exact whether the JSF version must be exact and investigated synchronously
     * @return JSF version if any found on the WebModule compile classpath, {@code null} otherwise
     */
    @CheckForNull
    public synchronized static JSFVersion forWebModule(@NonNull final WebModule webModule, boolean exact) {
        JSFVersion version = projectVersionCache.get(webModule);
        if (version == null) {
            if (exact) {
                version = getVersion(webModule);
            }
            RP.submit(new Runnable() {
                @Override
                public void run() {
                    Project project = FileOwnerQuery.getOwner(webModule.getDocumentBase());
                    ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
                    ClassPath compileCP = cpp.findClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
                    PropertyChangeListener listener = WeakListeners.propertyChange(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                                projectVersionCache.put(webModule, getVersion(webModule));
                            }
                        }
                    }, compileCP);
                    projectListenerCache.put(webModule, listener);
                    compileCP.addPropertyChangeListener(listener);
                    projectVersionCache.put(webModule, getVersion(webModule));
                }
            });
        }
        return version;
    }

    /**
     * Says whether the current instance is at least of the same JSF version.
     *
     * @param version version to compare
     * @return {@code true} if the current instance is at least of the given version, {@code false} otherwise
     */
    public boolean isAtLeast(@NonNull JSFVersion version) {
        int thisMajorVersion = Integer.parseInt(this.name().substring(4, 5));
        int thisMinorVersion = Integer.parseInt(this.name().substring(6, 7));
        int compMajorVersion = Integer.parseInt(version.name().substring(4, 5));
        int compMinorVersion = Integer.parseInt(version.name().substring(6, 7));
        return thisMajorVersion > compMajorVersion
                || thisMajorVersion == compMajorVersion && thisMinorVersion >= compMinorVersion;
    }

    private static JSFVersion getVersion(WebModule webModule) {
        if (JSFUtils.isJSF22Plus(webModule)) {
            return JSFVersion.JSF_2_2;
        } else if (JSFUtils.isJSF21Plus(webModule)) {
            return JSFVersion.JSF_2_1;
        } else if (JSFUtils.isJSF20Plus(webModule)) {
            return JSFVersion.JSF_2_0;
        } else if (JSFUtils.isJSF12Plus(webModule)) {
            return JSFVersion.JSF_1_2;
        }
        return null;
    }
}
