/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import com.sun.faces.config.ConfigManager;
import com.sun.faces.config.configprovider.MetaInfFaceletTaglibraryConfigProvider;
import com.sun.faces.facelets.tag.AbstractTagLibrary;
import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.facelets.tag.jsf.core.CoreLibrary;
import com.sun.faces.facelets.tag.jsf.html.HtmlLibrary;
import com.sun.faces.facelets.tag.jstl.core.JstlCoreLibrary;
import com.sun.faces.facelets.tag.jstl.fn.JstlFunction;
import com.sun.faces.facelets.tag.ui.UILibrary;
import com.sun.faces.facelets.util.FunctionLibrary;
import com.sun.faces.spi.ConfigurationResourceProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;

/**
 *
 * @author marekfukala
 */
public class FaceletsLibrarySupport implements PropertyChangeListener {

    private JsfSupport jsfSupport;
    private Map<String, FaceletsLibrary> faceletsLibraries;

    public FaceletsLibrarySupport(JsfSupport jspSupport) {
        this.jsfSupport = jspSupport;
        jspSupport.getClassPath().addPropertyChangeListener(this);
    }

    public JsfSupport getJsfSupport() {
        return jsfSupport;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        faceletsLibraries = null;
    }

    /** @return URI -> library map */
    public synchronized Map<String, FaceletsLibrary> getLibraries() {
        if (faceletsLibraries == null) {
            faceletsLibraries = findLibraries();
            debugLibraries();
        }
        
        updateUndeclaredCompositeLibraries(faceletsLibraries);

        return faceletsLibraries;
    }

    private void updateUndeclaredCompositeLibraries(Map<String, FaceletsLibrary> faceletsLibraries) {
        //process default undeclared composite libraries
        List<String> libraryNames = new ArrayList<String>(jsfSupport.getIndex().getAllCompositeLibraryNames());
        //check if the libraries have been declared
        for(FaceletsLibrary lib : faceletsLibraries.values()) {
            if(lib instanceof CompositeComponentLibrary) {
                String libraryName = ((CompositeComponentLibrary)lib).getLibraryName();
                libraryNames.remove(libraryName);
            }
        }

        //create libraries for the rest of undeclared libs
        for(String libraryName : libraryNames) {
            CompositeComponentLibrary ccl = new CompositeComponentLibrary(this, libraryName);
            faceletsLibraries.put(ccl.getNamespace(), ccl);
        }

    }

    private Map<String, FaceletsLibrary> findLibraries() {
        //create a new classloader loading the classpath roots
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        Collection<URL> urlsToLoad = new ArrayList<URL>();
        for (FileObject cpRoot : getJsfSupport().getClassPath().getRoots()) {
            urlsToLoad.add(URLMapper.findURL(cpRoot, URLMapper.INTERNAL));
        }
        ClassLoader proxyLoader = new URLClassLoader(urlsToLoad.toArray(new URL[]{}), originalLoader);

        try {
            Thread.currentThread().setContextClassLoader(proxyLoader);

            //do the parse
            return parseLibraries();

        } finally {
            //reset the original loader
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    private Map<String, FaceletsLibrary> parseLibraries() {
        ServletContext sc = null;

        // initialize the resource providers for facelet-taglib documents
        List<ConfigurationResourceProvider> faceletTaglibProviders =
                new ArrayList<ConfigurationResourceProvider>();
        //searches in classpath jars for .taglib.xml files
        faceletTaglibProviders.add(new MetaInfFaceletTaglibraryConfigProvider());
        //WEB-INF/web.xml <param-name>javax.faces.FACELETS_LIBRARIES</param-name> context param provider
        faceletTaglibProviders.add(new WebFaceletTaglibResourceProvider(getJsfSupport().getWebModule()));

        //collect all libraries from all providers
        Collection<URL> libraryUrls = new ArrayList<URL>();
        for (ConfigurationResourceProvider provider : faceletTaglibProviders) {
            libraryUrls.addAll(provider.getResources(sc));
        }

        //parse the libraries
        ConfigManager cm = ConfigManager.getInstance();
        Document[] documents = (Document[]) callMethod("getConfigDocuments", ConfigManager.class, cm, null, faceletTaglibProviders, null, true); //NOI18N
        FaceletsTaglibConfigProcessorPatched processor = new FaceletsTaglibConfigProcessorPatched(this);

        //process the found documents
        processor.process(documents);

        Collection<FaceletsLibrary> allLibs = new HashSet<FaceletsLibrary>();

        //get the parsed libraries from the compiler instance
        allLibs.addAll(processor.compiler.libraries);

        //add default jsf 2.0 libraries
        allLibs.addAll(getConvertedDefaultLibraries());

        Map<String, FaceletsLibrary> libsMap = new HashMap<String, FaceletsLibrary>();
        for (FaceletsLibrary lib : allLibs) {
            libsMap.put(lib.getNamespace(), lib);
        }

        return libsMap;

    }

    private Collection<FaceletsLibrary> getConvertedDefaultLibraries() {
        Collection<FaceletsLibrary> converted = new ArrayList<FaceletsLibrary>();
        for (TagLibrary library : getDefaultLibraries()) {
            if (library instanceof AbstractTagLibrary) {
                String namespace = (String) getField("namespace", AbstractTagLibrary.class, library); //NOI18N
                if (namespace == null) {
                    continue; //error, take next
                }
                ClassBasedFaceletsLibrary flib = new ClassBasedFaceletsLibrary(this, namespace);
                Map tagComponents = (Map) getField("factories", AbstractTagLibrary.class, library); //NOI18N
                Collection<FaceletsLibrary.NamedComponent> components = new ArrayList<FaceletsLibrary.NamedComponent>();
                if (tagComponents != null) {
                    for (Object key : tagComponents.keySet()) {
                        String componentName = (String) key;
                        //XXX resolve the component type accoring to the factory instance, ufff
                        //fortunately it looks like we do not need that
                        components.add(flib.createNamedComponent(componentName));
                    }
                }
                flib.setComponents(components);
                converted.add(flib);

            } else if (library instanceof FunctionLibrary) {
                String namespace = (String) getField("namespace", FunctionLibrary.class, library); //NOI18N
                if (namespace == null) {
                    continue; //error, take next
                }
                ClassBasedFaceletsLibrary flib = new ClassBasedFaceletsLibrary(this, namespace);
                Collection<FaceletsLibrary.NamedComponent> components = new ArrayList<FaceletsLibrary.NamedComponent>();
                Map functionComponents = (Map) getField("functions", FunctionLibrary.class, library); //NOI18N
                if (functionComponents != null) {
                    for (Object key : functionComponents.keySet()) {
                        String componentName = (String) key;
                        //XXX resolve the component type accoring to the factory instance, ufff
                        //fortunately it looks like we do not need that
                        components.add(flib.createFunction(componentName, null));
                    }
                }
                flib.setComponents(components);
                converted.add(flib);
            }
        }


        return converted;
    }

    private Collection<TagLibrary> getDefaultLibraries() {
        Collection<TagLibrary> libs = new ArrayList<TagLibrary>();
        libs.add(new CompositeLibrary());
        libs.add(new CoreLibrary());
        libs.add(new HtmlLibrary());
        libs.add(new UILibrary());
        libs.add(new JstlCoreLibrary());
        libs.add(new FunctionLibrary(JstlFunction.class, "http://java.sun.com/jsp/jstl/functions")); //NOI18N

        return libs;

    }

    private static Object getField(String fieldName, Class clazz, Object object) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            try {
                return f.get(object);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }

        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** does search only by method name not params!!! */
    private static Object callMethod(String methodName, Class clazz, Object object, Object... params) {
        try {
            Method m = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (method != null && method.getName().equals(methodName)) {
                    m = method;
                }
            }
            m.setAccessible(true);
            return m.invoke(object, params);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);

        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private void debugLibraries() {
        System.out.println("Facelets Libraries:");
        System.out.println("====================");
        for (FaceletsLibrary lib : faceletsLibraries.values()) {
            System.out.println("Library: " + lib.getNamespace());
            System.out.println("----------------------------------------------------");
            for (FaceletsLibrary.NamedComponent comp : lib.getComponents()) {
                System.out.println(comp.getName() + "(" + comp.getClass().getSimpleName() + ")");
            }
            System.out.println();
        }
    }

    public static class Compiler {

        private List<FaceletsLibrary> libraries = new ArrayList<FaceletsLibrary>();

        public void addTagLibrary(FaceletsLibrary lib) {
            libraries.add(lib);
        }

    }
}
