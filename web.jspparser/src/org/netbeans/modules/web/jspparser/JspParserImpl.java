/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.TldChangeListener;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

// PENDING - need to call reinitOptions when something changes (taglib, jar, web.xml)
// PENDING - separate to two classes, have a per-application instance of one of them

/**
 * @author Petr Jiricka
 */
public class JspParserImpl implements JspParserAPI {
    
    // @GuardedBy(this)
    final Map<WebModule, WebAppParseProxy> parseSupports = new WeakHashMap<WebModule, WebAppParseProxy>();
    private final TldChangeSupport tldChangeSupport;
    
    private static final Logger LOGGER = Logger.getLogger(JspParserImpl.class.getName());
    private static Constructor webAppParserImplConstructor;
    
    private static final JspParserAPI.JspOpenInfo DEFAULT_OPENINFO =
            new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"); // NOI18N
    
    /** Constructs a new Parser API implementation.
     */
    public JspParserImpl() {
        // PENDING - we are preventing the garbage collection of
        // Project-s and FileObject-s (wmRoots)
        tldChangeSupport = new TldChangeSupport(this);
    }
    
    private static void initReflection() {
        if (webAppParserImplConstructor == null) {
            File[] files = new File[5];
            files[0] = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false); // NOI18N
            files[1] = InstalledFileLocator.getDefault().locate("modules/ext/glassfish-jspparser-2.0.jar", null, false); // NOI18N
            //files[2] = InstalledFileLocator.getDefault().locate("modules/ext/glassfish-logging.jar", null, false); // NOI18N
            files[2] = InstalledFileLocator.getDefault().locate("modules/ext/jsp-parser-ext.jar", null, false); // NOI18N
            files[3] = InstalledFileLocator.getDefault().locate("modules/ext/servlet2.5-jsp2.1-api.jar", null, false); // NOI18N
            //Glassfish V2
            files[4] = InstalledFileLocator.getDefault().locate("ant/lib/ant-launcher.jar", null, false); // NOI18N
            
            try {
                URL[] urls = new URL[files.length];
                for (int i = 0; i < files.length; i++) {
                    urls[i] = files[i].toURI().toURL();
                }
                ExtClassLoader urlCL = new ExtClassLoader(urls, JspParserImpl.class.getClassLoader());
                Class<?> cl = urlCL.loadClass("org.netbeans.modules.web.jspparser_ext.WebAppParseSupport"); // NOI18N
                webAppParserImplConstructor = cl.getDeclaredConstructor(new Class[] {JspParserImpl.class, WebModule.class});
            } catch (NoSuchMethodException e) {
                LOGGER.log(Level.INFO, null, e);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.INFO, null, e);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }
    
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm, boolean useEditor) {
        //try to fast create openinfo
        
        //detects encoding even if there is not webmodule (null) or deployment descriptor doesn't exist
        FastOpenInfoParser fastOIP = FastOpenInfoParser.get(wm);
        if (fastOIP != null) {
            JspParserAPI.JspOpenInfo jspOI = fastOIP.getJspOpenInfo(jspFile, useEditor);
            if (jspOI != null) {
                return jspOI;
            }
        }
        
        //no encoding found in the file or the deployment descriptor contains encoding declarations
        if (wm != null) {
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null) {
                WebAppParseProxy pp = getParseProxy(wm);
                if (pp != null) {
                    return pp.getJspOpenInfo(jspFile, useEditor);
                }
            }
        }
        return DEFAULT_OPENINFO;
    }
    
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm, int errorReportingMode) {
        if (wm == null) {
            return getNoWebModuleResult(jspFile, null);
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        WebAppParseProxy pp = getParseProxy(wm);
        if (pp == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        return pp.analyzePage(jspFile, errorReportingMode);
    }
    
    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library.
     * @param wmRoot the web module for which to return the map
     * @return Map which maps global tag library URI to the location
     * (resource path) of its tld. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    public Map<String, String[]> getTaglibMap(WebModule wm) throws IOException {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            throw new IOException();
        }
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getTaglibMap(true);
    }
    
    private synchronized WebAppParseProxy getParseProxy(WebModule wm) {
        WebAppParseProxy pp = parseSupports.get(wm);
        // #67785 - the document root fileobject has to be valid
        FileObject wmRoot = wm.getDocumentBase();
        if (!wmRoot.isValid()) {
            pp = null;
        }
        if (pp == null) {
            pp = createParseProxy(wm);
            parseSupports.put(wm, pp);
        }
        return pp;
    }
    
    private WebAppParseProxy createParseProxy(WebModule wm) {
        // PENDING - do caching for individual JSPs
        try {
            initReflection();
            return (WebAppParseProxy) webAppParserImplConstructor.newInstance(new Object[] {this, wm});
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return null;
    }
    
    public URLClassLoader getModuleClassLoader(WebModule wm) {
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getWAClassLoader();
    }
    
    private JspParserAPI.ParseResult getNoWebModuleResult(FileObject jspFile, WebModule wm) {
        JspParserAPI.ErrorDescriptor error = new JspParserAPI.ErrorDescriptor(null, jspFile, -1, -1,
                NbBundle.getMessage(JspParserImpl.class, "MSG_webModuleNotFound", jspFile.getNameExt()), ""); // NOI18N
        return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
    }

    public void addTldChangeListener(TldChangeListener listener) {
        tldChangeSupport.addTldChangeListener(listener);
    }

    public void removeTldChangeListener(TldChangeListener listener) {
        tldChangeSupport.removeTldChangeListener(listener);
    }

    public void fireChange(WebModule webModule) {
        tldChangeSupport.fireChange(webModule);
    }

    private static class ExtClassLoader extends URLClassLoader {
        
        private static final AllPermission ALL_PERM = new AllPermission();
        
        public ExtClassLoader(URL[] classLoadingURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
        }
        
        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // this is because debugger adds ant on cp but this classloader needs ant as well
            //  - so let this class loader find the class
            if (name.startsWith("org.apache.tools.ant.")) { // NOI18N
                Class<?> clazz = findClass(name);
                return clazz;
            }
            return super.loadClass(name, resolve);
        }
    }
}
