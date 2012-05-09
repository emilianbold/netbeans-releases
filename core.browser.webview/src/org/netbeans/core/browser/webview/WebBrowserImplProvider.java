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
package org.netbeans.core.browser.webview;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.browser.api.WebBrowser;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author petrjiricka
 */
public class WebBrowserImplProvider {
    
    public interface JFXRuntimePathProvider {
        
        /** Returns the installation path of the JavaFX runtime; must contain file lib/jfxrt.jar.
         */
        public String getJFXRuntimePath();
        
    }

    private static URLClassLoader browserCL;
    private static final Logger log = Logger.getLogger(WebBrowserImplProvider.class.getName());
    
    static WebBrowser createBrowser() {
        ClassLoader cl = getBrowserClassLoader();
        try {
            if (cl != null) {
                // test that JavaFX has latest required APIs:
                cl.loadClass("com.sun.javafx.scene.web.Debugger");
            }
        } catch(ClassNotFoundException ex)  {
            log.log(Level.WARNING, "It looks that latest JavaFX runtime (>=2.2.0) "
                    + "which contains support for WebKit Remote Debugging is not available. "
                    + "Please upgrade your JavaFX runtime to newer version. ", ex);
            return new NoWebBrowserImpl();
        }
        try {
            if (cl != null) {
                 //return new WebBrowserImpl();
                Class impl = cl.loadClass("org.netbeans.core.browser.webview.ext.WebBrowserImpl");
                Constructor c = impl.getConstructor(new Class[] {});
                return (WebBrowser)c.newInstance(new Object[] {});
            }
        } catch (InstantiationException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (SecurityException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
        }
        return new NoWebBrowserImpl();
    }

    
    
    private static String[] getFXClassPath() {
        JFXRuntimePathProvider rtPathProvider = Lookup.getDefault().lookup(JFXRuntimePathProvider.class);
        if (rtPathProvider == null) {
            return null;
        }
        String rtPath = rtPathProvider.getJFXRuntimePath();
        if (rtPath == null) {
            return null;
        }
        return new String[] {
            rtPath + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"
        };
    }
    
    
    private static ClassLoader getBrowserClassLoader() {
        synchronized (WebBrowserImplProvider.class) {
            if (browserCL != null) return browserCL;
            File extjar = InstalledFileLocator.getDefault().locate("modules/ext/core.browser.webview-ext.jar", "org.netbeans.core.browser.webview", false); // NOI18N
            if (extjar == null) {
                log.log(Level.INFO, "core.browser.webview-ext.jar not found"); // NOI18N
                return null;
            }
            String[] fxpath = getFXClassPath();
            if (fxpath == null) return null;
            List<URL> urls = new ArrayList<URL>();
            try {
                urls.add(extjar.toURI().toURL());
                for (String fx : fxpath) {
                    urls.add(new File(fx).toURI().toURL());
                }
                browserCL = new URLClassLoader(urls.toArray(new URL[] {}), 
                        WebBrowserImplProvider.class.getClassLoader());
                return browserCL;
            } catch (MalformedURLException m) {
                log.log(Level.INFO, m.getMessage(), m);
                return null;
            }
        }
    }
        
}
