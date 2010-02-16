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

import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.modules.web.jsf.editor.facelets.mojarra.FaceletsTaglibConfigProcessor;
import com.sun.faces.config.DocumentInfo;
import com.sun.faces.spi.ConfigurationResourceProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.facelets.mojarra.ConfigManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class FaceletsLibrarySupport implements PropertyChangeListener {

    private JsfSupport jsfSupport;

    /**
     * Library's namespace to library instance map.
     *
     * A composite library can be mapped to two namespaces,
     * the default and the declared one when
     * there is a tag library descriptor for the composite library
     */
    private Map<String, FaceletsLibrary> faceletsLibraries;

    private static final Logger LOGGER = Logger.getLogger(FaceletsLibrarySupport.class.getSimpleName());

    private FileChangeListener DDLISTENER = new FileChangeAdapter() {

        @Override
        public void fileChanged(FileEvent fe) {
            ddChanged();
        }

    };
    
    private static final String DD_FILE_NAME = "web.xml"; //NOI18N

    public FaceletsLibrarySupport(JsfSupport jspSupport) {
        this.jsfSupport = jspSupport;

        //listen on classpath and refresh the libraries when changed
        jspSupport.getClassPath().addPropertyChangeListener(this);

        //listen on /WEB-INF/web.xml changes - <param-name>javax.faces.FACELETS_LIBRARIES</param-name>
        //may change and redefine the libraries
        final FileObject dd = jsfSupport.getWebModule().getDeploymentDescriptor();
        if (dd != null) {
            dd.addFileChangeListener(DDLISTENER);
        }

        //listen on the /WEB-INF folder since the dd is arbitrary and may
        //be created later
        FileObject webInf = jsfSupport.getWebModule().getWebInf();
        if (webInf != null) {
            webInf.addFileChangeListener(new FileChangeAdapter() {

                @Override
                public void fileDataCreated(FileEvent fe) {
                    FileObject file = fe.getFile();
                    if (file.getNameExt().equalsIgnoreCase(DD_FILE_NAME)) {
                        file.addFileChangeListener(DDLISTENER);
                    }
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    FileObject file = fe.getFile();
                    if (file.getNameExt().equalsIgnoreCase(DD_FILE_NAME)) {
                        file.removeFileChangeListener(DDLISTENER);
                    }
                }
            });
        }
    }

    //TODO cache the context-param entry value and check if has changed,
    //now we invalidate libs on each change
    private synchronized void ddChanged() {
        faceletsLibraries = null; //invalidate libraries, force refresh
    }

    public JsfSupport getJsfSupport() {
        return jsfSupport;
    }

    //TODO: the method is supposed to refresh JUST the given library
    public synchronized void libraryChanged(FaceletsLibrary library) {
        faceletsLibraries = null; //refresh all
    }

    //called by JsfIndexer when scanning finishes
    public synchronized void librariesChanged(Collection<String> librariesNamespaces) {
	//just check if the library already exist and if not, refresh all libs

	//if faceletsLibraries are null, we cannot call getLibraries() which could
	//call back to the indexing
	if(faceletsLibraries != null && !getLibraries().keySet().containsAll(librariesNamespaces)) {
	    faceletsLibraries = null;
	}
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        //classpath changed, rescan libraries
        faceletsLibraries = null;
    }

    /** @return URI -> library map */
    public synchronized Map<String, FaceletsLibrary> getLibraries() {
        if (faceletsLibraries == null) {
            faceletsLibraries = findLibraries();

            if (faceletsLibraries == null) {
                //an error when scanning libraries, return no libraries, but give it a next try
                return Collections.emptyMap();
            }
            updateCompositeLibraries(faceletsLibraries);
        }


        return faceletsLibraries;
    }

    // This method creates a library instances for the composite libraries without
    // a library descriptor and also adds the default composite library
    // namespace as a new key to the libraries map.
    private void updateCompositeLibraries(Map<String, FaceletsLibrary> faceletsLibraries) {
        List<String> libraryNames = new ArrayList<String>(jsfSupport.getIndex().getAllCompositeLibraryNames());
        //go through all the declared libraries, filter composite libraries
        //and add default namespace to the libraries map
        Map<String, FaceletsLibrary> cclibsMap = new HashMap<String, FaceletsLibrary>();
        for (FaceletsLibrary lib : faceletsLibraries.values()) {
            if (lib instanceof CompositeComponentLibrary) {
                CompositeComponentLibrary cclib = (CompositeComponentLibrary)lib;
                //add default namespace to the map
                cclibsMap.put(cclib.getDefaultNamespace(), cclib);

                String libraryName = cclib.getLibraryName();
                libraryNames.remove(libraryName);
            }
        }

        faceletsLibraries.putAll(cclibsMap);

        //create libraries for the rest of undeclared libs
        for (String libraryName : libraryNames) {
            CompositeComponentLibrary ccl = new CompositeComponentLibrary(this, libraryName);
            //map the library only to the default namespace, it has no declaration
            faceletsLibraries.put(ccl.getDefaultNamespace(), ccl);
        }

    }

    //handle progress
    private Map<String, FaceletsLibrary> findLibraries() {
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(FaceletsLibrarySupport.class, "MSG_ParsingFaceletsLibraries")); //NOI18N
        progress.start();
        progress.switchToIndeterminate();
        try {
            return _findLibraries();
        } finally {
            progress.finish();
        }
    }

    private Map<String, FaceletsLibrary> _findLibraries() {
        //use this module classloader
        ClassLoader originalLoader = this.getClass().getClassLoader();
        LOGGER.log(Level.FINE, "Scanning facelets libraries, current classloader class=" +
                originalLoader.getClass().getName() +
                ", the used URLClassLoader will also contain following roots:"); //NOI18N

        Collection<URL> urlsToLoad = new ArrayList<URL>();
        for (FileObject cpRoot : getJsfSupport().getClassPath().getRoots()) {
            try {
                //exclude the jsf jars from the classpath, if jsf20 library is available,
                //we'll use the jars from the netbeans library instead
                String fsName = cpRoot.getFileSystem().getDisplayName(); //any better way?
                if(!(fsName.endsWith("jsf-impl.jar") || fsName.endsWith("jsf-api.jar"))) { //NOI18N
                    urlsToLoad.add(URLMapper.findURL(cpRoot, URLMapper.INTERNAL));
                    LOGGER.log(Level.FINE, "+++" + cpRoot); //NOI18N
                } else {
                    LOGGER.log(Level.FINE, "---" + cpRoot); //NOI18N
                }

            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        ClassLoader proxyLoader = new URLClassLoader(urlsToLoad.toArray(new URL[]{}), originalLoader) {

	    //prevent services loading from mojarra's sources
	    @Override
	    public URL findResource(String name) {
		return name.startsWith("META-INF/services") ? null : super.findResource(name); //NOI18N
	    }

	    @Override
	    public Enumeration<URL> findResources(String name) throws IOException {
		if(name.startsWith("META-INF/services")) { //NOI18N
		    return Collections.enumeration(Collections.<URL>emptyList());
		} else {
		    return super.findResources(name);
		}
	    }
	    
	};

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
        // initialize the resource providers for facelet-taglib documents
        List<ConfigurationResourceProvider> faceletTaglibProviders =
                new ArrayList<ConfigurationResourceProvider>();

        //1. first add provider which looks for libraries defined in web-inf.xml
        //WEB-INF/web.xml <param-name>javax.faces.FACELETS_LIBRARIES</param-name> context param provider
        faceletTaglibProviders.add(new WebFaceletTaglibResourceProvider(getJsfSupport().getWebModule()));

        //searches source classpath for .taglib.xml files
        ClassPath sourceClasspath = ClassPath.getClassPath(getJsfSupport().getWebModule().getDocumentBase(), ClassPath.SOURCE);
        faceletTaglibProviders.add(new FaceletTaglibraryConfigProvider(sourceClasspath));

        //2. second add provider which looks for libs in the jars on the project classpath
        //we already identified the library descriptors during indexing, no need
        //to scan again, just use the files from index
        final Collection<URL> urls = new ArrayList<URL>();
        for (FileObject file : getJsfSupport().getIndex().getAllFaceletsLibraryDescriptors()) {
            urls.add(URLMapper.findURL(file, URLMapper.EXTERNAL));
        }
        faceletTaglibProviders.add(new ConfigurationResourceProvider() {

            @Override
            public Collection<URL> getResources(ServletContext sc) {
                return urls;
            }
        });

        //3. last add a provider for default jsf libs
        //
        //Add a facelet taglib provider which provides the libraries from
        //netbeans jsf2.0 library
        //
        //This is needed for the standart JSF 2.0 libraries since it may
        //happen that there is no jsf-impl.jar with the .taglib.xml files
        //on the compile classpath and we still want the features like code
        //completion work. This happens for example in Maven web projects.
        //
        //The provider is last in the list so the provided libraries will
        //be overridden if the descriptors are found in any of the jars
        //on compile classpath.
        Collection<FileObject> libraryDescriptorFiles = DefaultFaceletLibraries.getInstance().getLibrariesDescriptorsFiles();
        final Collection<URL> libraryURLs = new ArrayList<URL>();
        for(FileObject fo : libraryDescriptorFiles) {
            try {
                libraryURLs.add(fo.getURL());
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        faceletTaglibProviders.add(new ConfigurationResourceProvider() {
            @Override
            public Collection<URL> getResources(ServletContext sc) {
                return libraryURLs;
            }
        });

        //parse the libraries
        DocumentInfo[] documents = ConfigManager.getConfigDocuments(null, faceletTaglibProviders, null, true);
        if (documents == null) {
            return null; //error????
        }

        //process the found documents
        FaceletsTaglibConfigProcessor processor = new FaceletsTaglibConfigProcessor(this);
        processor.process(null, documents);

        Map<String, FaceletsLibrary> libsMap = new HashMap<String, FaceletsLibrary>();
        for (FaceletsLibrary lib : processor.compiler.libraries) {
            libsMap.put(lib.getNamespace(), lib);
        }

        return libsMap;

    }


    private void debugLibraries() {
        System.out.println("Facelets Libraries:");  //NOI18N
        System.out.println("====================");  //NOI18N
        for (FaceletsLibrary lib : faceletsLibraries.values()) {
            System.out.println("Library: " + lib.getNamespace());  //NOI18N
            System.out.println("----------------------------------------------------");  //NOI18N
            for (FaceletsLibrary.NamedComponent comp : lib.getComponents()) {
                System.out.println(comp.getName() + "(" + comp.getClass().getSimpleName() + ")");  //NOI18N
            }
            System.out.println();
        }
    }

    public static class Compiler {

        private Collection<FaceletsLibrary> libraries = new HashSet<FaceletsLibrary>();

        //FaceletsTaglibConfigProcessor puts the libraries here and since the
        //equals on the libraries is defined by comparing the namespaces,
        //the first library with a namespace will be preserved, the other
        //will be ignored
        public void addTagLibrary(FaceletsLibrary lib) {
            libraries.add(lib);
        }
    }
}
