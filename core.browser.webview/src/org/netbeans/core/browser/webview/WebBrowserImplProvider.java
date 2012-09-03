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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
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
        return createBrowser(null);
    }

    static WebBrowser createBrowser( File runtimePath ) {
        ClassLoader cl = getBrowserClassLoader( runtimePath );
        try {
            if (cl != null) {
                // test that JavaFX has latest required APIs:
                cl.loadClass("com.sun.javafx.scene.web.Debugger");
                Class platform = cl.loadClass("javafx.application.Platform");
                Method m = platform.getMethod("setImplicitExit", boolean.class);
            }
        } catch(Throwable ex)  {
            log.log(Level.INFO, "JavaFX runtime is too old - "
                    + "minimum version required is 2.2.0b20", ex);
            return new NoWebBrowserImpl("JavaFX runtime is too old - "
                    + "minimum version required is 2.2.0b20");
        }
        try {
            if (cl != null) {
                 //return new WebBrowserImpl();
                Class impl = cl.loadClass("org.netbeans.core.browser.webview.ext.WebBrowserImpl");
                Constructor c = impl.getConstructor(new Class[] {});
                return (WebBrowser)c.newInstance(new Object[] {});
            }
        } catch (Throwable ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
            return new NoWebBrowserImpl(ex.getMessage());
        }
        return new NoWebBrowserImpl(new RuntimePathPanel());
    }

    static void reset() {
        synchronized( WebBrowserImplProvider.class ) {
            browserCL = null;
        }
    }
    
    private static String[] getFXClassPath() {
        Collection<? extends JFXRuntimePathProvider> pathProviders = Lookup.getDefault().lookupAll( JFXRuntimePathProvider.class );
        
        for( JFXRuntimePathProvider rtPathProvider : pathProviders ) {
            String rtPath = rtPathProvider.getJFXRuntimePath();
            if (rtPath == null) {
                continue;
            }
            return new String[] {
                rtPath + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar" //NOI18N
            };
        }
        return null;
    }
    
    private static ClassLoader getBrowserClassLoader(File runtimePath) {
        synchronized (WebBrowserImplProvider.class) {
            if( null != runtimePath )
                return createBrowserClassLoader( runtimePath );
            
            if (browserCL != null)
                return browserCL;
            browserCL = createBrowserClassLoader( null );
            return browserCL;
        }
    }
        
    private static URLClassLoader createBrowserClassLoader(File runtimePath) {
        synchronized (WebBrowserImplProvider.class) {
            File extjar = InstalledFileLocator.getDefault().locate("modules/ext/core.browser.webview-ext.jar", "org.netbeans.core.browser.webview", false); // NOI18N
            if (extjar == null) {
                log.log(Level.INFO, "core.browser.webview-ext.jar not found"); // NOI18N
                return null;
            }
            String[] fxpath = null;
            if( null == runtimePath ) {
                fxpath = getFXClassPath();
            } else {
                fxpath = new String[] { new File(new File(runtimePath, "lib"), "jfxrt.jar").getAbsolutePath() }; //NOI18N
            }
            if (fxpath == null) return null;
            List<URL> urls = new ArrayList<URL>();
            try {
                urls.add(extjar.toURI().toURL());
                for (String fx : fxpath) {
                    urls.add(new File(fx).toURI().toURL());
                }
                return new URLClassLoader(urls.toArray(new URL[] {}),
                        WebBrowserImplProvider.class.getClassLoader());
            } catch (MalformedURLException m) {
                log.log(Level.INFO, m.getMessage(), m);
                return null;
            }
        }
    }
}
