/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.project.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.project.libraries.ui.LibrariesCustomizer;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.libraries.LibraryProvider.class)
public class LibrariesStorage extends FileChangeAdapter
implements WritableLibraryProvider<LibraryImplementation>, ChangeListener {

    private static final String NB_HOME_PROPERTY = "netbeans.home";  //NOI18N
    private static final String LIBRARIES_REPOSITORY = "org-netbeans-api-project-libraries/Libraries";  //NOI18N
    private static final String TIME_STAMPS_FILE = "libraries-timestamps.properties"; //NOI18B
    private static final String XML_EXT = "xml";    //NOI18N

    static final Logger LOG = Logger.getLogger(LibrariesStorage.class.getName());
    
    //Lock to prevent FileAlreadyLocked exception.
    private static final Object TIMESTAMPS_LOCK = new Object ();

    // persistent storage, it may be null for before first library is store into storage
    private FileObject storage = null;

    private Map<String, LibraryImplementation> libraries;

    private Map<String, LibraryImplementation> librariesByFileNames;

    // Library declaraion public ID
    // i18n bundle
    private ResourceBundle bundle;

    private PropertyChangeSupport support;
    
    //Flag if the storage is initialized
    //The storage needs to be lazy initialized, it is in lookup
    private boolean initialized;
    
    private Properties timeStamps;


    /**
     * Create libraries that need to be populated later.
     */
    public LibrariesStorage() {
        this.support = new PropertyChangeSupport(this);
    }
    
    /**
     * Constructor for tests
     */
    LibrariesStorage (FileObject storage) {
        this ();
        this.storage = storage;               
    }



    /**
     * Initialize the default storage.
     * @return new storage or null on I/O error.
     */
    private static final FileObject createStorage () {
        try {
            return FileUtil.createFolder(FileUtil.getConfigRoot(), LIBRARIES_REPOSITORY);
        } catch (IOException e) {
            return null;
        }
    }


    // scans over storage and fetchs it ((fileset persistence files) into memory
    // ... note that providers can read their data during getVolume call
    private void loadFromStorage(
            final Map<? super String, ? super LibraryImplementation> libraries,
            final Map<? super String, ? super LibraryImplementation> librariesByFileNames) {
        // configure parser
        //We are in unit test with no storage
        if (storage == null) {
            return;
        }
        LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
        LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();
        LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,convertor);
        // parse
        for (FileObject descriptorFile : storage.getChildren()) {
            if (XML_EXT.equalsIgnoreCase(descriptorFile.getExt())) {                          
                try {
                    handler.setLibrary (null);
                    readLibrary (descriptorFile, parser);
                    LibraryImplementation impl = handler.getLibrary ();
                    if (impl != null) {
                        LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
                        if (provider == null) {
                            LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                        }
                        else if (libraries.keySet().contains(impl.getName())) {
                                LOG.warning("LibrariesStorage: Library \""
                                    +impl.getName()+"\" is already defined, skeeping the definition from: " 
                                    + FileUtil.getFileDisplayName(descriptorFile));
                        }
                        else {                                                
                            if (!isUpToDate(descriptorFile)) {
                                provider.libraryCreated (impl);
                                updateTimeStamp(descriptorFile);
                            }
                            librariesByFileNames.put(descriptorFile.getPath(),impl);
                            libraries.put (impl.getName(),impl);
                            LibrariesCustomizer.registerSource(impl, descriptorFile);
                        }
                    }
                } catch (SAXException e) {
                    //The library is broken, probably edited by user
                    //just log as warning
                    LOG.warning(String.format("Cannot load library from file %s, reason: %s", FileUtil.getFileDisplayName(descriptorFile), e.getMessage()));
                } catch (ParserConfigurationException e) {
                    Exceptions.printStackTrace(e);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (RuntimeException e) {
                    // Other problem.
                    Exceptions.printStackTrace(e);
                }
            }
        }
        try {
            saveTimeStamps();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }       
    }
    
    private void initStorage () {
        boolean reload;
        synchronized (this) {
            if (!initialized) {
                if (this.storage == null) {
                    this.storage = createStorage();
                }
                if (storage != null) {
                    this.storage.addFileChangeListener (this);
                }
                LibraryTypeRegistry.getDefault().addChangeListener(this);
                initialized = true;
            }
            //For the first time or a new LibraryTypeProvider has been enabled
            reload = libraries == null || LibraryTypeRegistry.getDefault().hasChanged();
        }
        if (reload) {
            final Map<String,LibraryImplementation> libraries = new HashMap<String, LibraryImplementation>();
            final Map<String,LibraryImplementation> librariesByFileNames = new HashMap<String, LibraryImplementation>();
            this.loadFromStorage(libraries, librariesByFileNames);
            synchronized (this) {
                this.libraries = libraries;
                this.librariesByFileNames = librariesByFileNames;
            }
        }
    }

    private static LibraryImplementation readLibrary (FileObject descriptorFile) throws SAXException, ParserConfigurationException, IOException{
        return readLibrary (descriptorFile, (LibraryImplementation) null);
    }
    
    private static LibraryImplementation readLibrary (FileObject descriptorFile, LibraryImplementation impl) throws SAXException, ParserConfigurationException, IOException {
        LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
        LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();
        LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,convertor);
        handler.setLibrary (impl);
        readLibrary (descriptorFile, parser);
        LibrariesCustomizer.registerSource(impl, descriptorFile);
        return handler.getLibrary();
    }

    private static void readLibrary (FileObject descriptorFile, LibraryDeclarationParser parser) throws SAXException, ParserConfigurationException, IOException {
        URL baseURL = descriptorFile.getURL();
        InputSource input = new InputSource(baseURL.toExternalForm());
        input.setByteStream(descriptorFile.getInputStream()); // #33554 workaround
        try {
            parser.parse(input);
        } catch (SAXException e) {
            throw Exceptions.attachMessage(e, "From: " + baseURL);  //NOI18N
        }
    }

    private void writeLibrary (final FileObject storage, final LibraryImplementation library) throws IOException {
        storage.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        String libraryType = library.getType ();
                        LibraryTypeProvider libraryTypeProvider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
                        if (libraryTypeProvider == null) {
                            LOG.warning("LibrariesStorage: Cannot store library, the library type is not recognized by any of installed LibraryTypeProviders.");	//NOI18N
                            return;
                        }
                        FileObject fo = storage.createData (library.getName(),"xml");   //NOI18N
                        writeLibraryDefinition (fo, library, libraryTypeProvider);
                    }
                }
        );
    }

    private static void writeLibraryDefinition (final FileObject definitionFile, final LibraryImplementation library, final LibraryTypeProvider libraryTypeProvider) throws IOException {
        Document doc = XMLUtil.createDocument("library", null,
                "-//NetBeans//DTD Library Declaration 1.0//EN",
                "http://www.netbeans.org/dtds/library-declaration-1_0.dtd"); // NOI18N
        Element libraryE = doc.getDocumentElement();
        libraryE.setAttribute("version", "1.0"); // NOI18N
        libraryE.appendChild(doc.createElement("name")).appendChild(doc.createTextNode(library.getName())); // NOI18N
        libraryE.appendChild(doc.createElement("type")).appendChild(doc.createTextNode(library.getType())); // NOI18N
        String description = library.getDescription();
        if (description != null && description.length() > 0) {
            libraryE.appendChild(doc.createElement("description")).appendChild(doc.createTextNode(description)); // NOI18N
        }
        String localizingBundle = library.getLocalizingBundle();
        if (localizingBundle != null && localizingBundle.length() > 0) {
            libraryE.appendChild(doc.createElement("localizing-bundle")).appendChild(doc.createTextNode(localizingBundle)); // NOI18N
        }
        for (String vtype : libraryTypeProvider.getSupportedVolumeTypes()) {
            Element volumeE = (Element) libraryE.appendChild(doc.createElement("volume")); // NOI18N
            volumeE.appendChild(doc.createElement("type")).appendChild(doc.createTextNode(vtype)); // NOI18N
            List<URL> volume = library.getContent(vtype);
            if (volume != null) {
                //If null -> broken library, repair it.
                for (URL url : volume) {
                    volumeE.appendChild(doc.createElement("resource")).appendChild(doc.createTextNode(url.toString())); // NOI18N
                }
            }
        }
        OutputStream os = definitionFile.getOutputStream();
        try {
            XMLUtil.write(doc, os, "UTF-8"); // NOI18N
        } finally {
            os.close();
        }
    }


    private void fireLibrariesChanged () {
        this.support.firePropertyChange(PROP_LIBRARIES,null,null);
    }


    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }


    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * Return all libraries in memory.
     */
    public final LibraryImplementation[] getLibraries() {
        this.initStorage();
        synchronized (this) {
            assert this.storage != null : "Storage is not initialized";
            assert this.libraries != null;
            //return a snapshot of libraries, maybe even newer
            return libraries.values().toArray(new LibraryImplementation[libraries.size()]);
        }
    } // end getLibraries


    public void addLibrary (LibraryImplementation library) throws IOException {
        this.initStorage();
        assert this.storage != null : "Storage is not initialized";
        writeLibrary(this.storage,library);
    }

    public void removeLibrary (LibraryImplementation library) throws IOException {
        this.initStorage();
        assert this.storage != null : "Storage is not initialized";
        for (String key : librariesByFileNames.keySet()) {
            LibraryImplementation lib = this.librariesByFileNames.get(key);
            if (library.equals (lib)) {
                FileObject fo = this.storage.getFileSystem().findResource (key);
                if (fo != null) {
                    fo.delete();
                    return;
                }
            }
        }
    }

    public void updateLibrary(final LibraryImplementation oldLibrary, final LibraryImplementation newLibrary) throws IOException {
        this.initStorage();
        assert this.storage != null : "Storage is not initialized";
        for (String key : librariesByFileNames.keySet()) {
            LibraryImplementation lib = librariesByFileNames.get(key);
            if (oldLibrary.equals(lib)) {
                final FileObject fo = this.storage.getFileSystem().findResource(key);
                if (fo != null) {
                    String libraryType = newLibrary.getType ();
                    final LibraryTypeProvider libraryTypeProvider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
                    if (libraryTypeProvider == null) {
                        LOG.warning("LibrariesStorageL Cannot store library, the library type is not recognized by any of installed LibraryTypeProviders.");	//NOI18N
                        return;
                    }
                    this.storage.getFileSystem().runAtomicAction(
                            new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    writeLibraryDefinition (fo, newLibrary, libraryTypeProvider);
                                }
                            }
                    );
                }
            }
        }
    }


    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        try {
            final LibraryImplementation impl = readLibrary (fo);
            if (impl != null) {
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                }
                else {
                    synchronized (this) {                        
                        this.libraries.put (impl.getName(), impl);
                        this.librariesByFileNames.put (fo.getPath(), impl);
                    }
                    //Has to be called outside the synchronized block,
                    // The code is provided by LibraryType implementator and can fire events -> may cause deadlocks
                    try {
                        provider.libraryCreated (impl);
                        updateTimeStamp(fo);
                        saveTimeStamps();
                    } catch (RuntimeException e) {
                        String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryCreatedError");
                        Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                    }
                    this.fireLibrariesChanged();
                }
            }            
        } catch (SAXException e) {
            //The library is broken, probably edited by user or unknown provider (FoD), log as warning
            LOG.warning(String.format("Cannot load library from file %s, reason: %s", FileUtil.getFileDisplayName(fo), e.getMessage()));
        } catch (ParserConfigurationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public void fileDeleted(FileEvent fe) {
        String fileName = fe.getFile().getPath();
        LibraryImplementation impl;
        synchronized (this) {            
            impl = this.librariesByFileNames.remove(fileName);
            if (impl != null) {
                this.libraries.remove (impl.getName());
            }
        }
        if (impl != null) {
            LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
            if (provider == null) {
                LOG.warning("LibrariesStorage: Cannot invoke LibraryTypeProvider.libraryDeleted(), the library type provider is unknown.");  //NOI18N
            }
            else {
                //Has to be called outside the synchronized block,
                // The code is provided by LibraryType implementator and can fire events -> may cause deadlocks
                try {
                    provider.libraryDeleted (impl);
                } catch (RuntimeException e) {
                    String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryDeletedError");
                    Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                }
            }
            this.fireLibrariesChanged();
        }
    }

    public void fileChanged(FileEvent fe) {
        FileObject definitionFile = fe.getFile();
        String fileName = definitionFile.getPath();
        LibraryImplementation impl;
        synchronized (this) {
            impl = this.librariesByFileNames.get(fileName);
        }
        if (impl != null) {
            try {
                readLibrary (definitionFile, impl);
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                }
                try {
                    //TODO: LibraryTypeProvider should be extended by libraryUpdated method 
                    provider.libraryCreated (impl);
                    updateTimeStamp(definitionFile);
                    saveTimeStamps();
                } catch (RuntimeException e) {
                    String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryCreatedError");
                    Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                }                
            } catch (SAXException se) {
                //The library is broken, probably edited by user, log as warning
                LOG.warning(String.format("Cannot load library from file %s, reason: %s", FileUtil.getFileDisplayName(definitionFile), se.getMessage()));
            } catch (ParserConfigurationException pce) {
                Exceptions.printStackTrace(pce);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    protected final ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = NbBundle.getBundle(LibrariesStorage.class);
        }
        return bundle;
    }
    
    private boolean isUpToDate (FileObject libraryDefinition) {
        Properties timeStamps = getTimeStamps();
        String ts = (String) timeStamps.get (libraryDefinition.getNameExt());
        return ts == null ? false : Long.parseLong(ts) >= libraryDefinition.lastModified().getTime();
    }

    private void updateTimeStamp (FileObject libraryDefinition) {
        Properties timeStamps = getTimeStamps();
        timeStamps.put(libraryDefinition.getNameExt(), Long.toString(libraryDefinition.lastModified().getTime()));
    }
    
    private void saveTimeStamps () throws IOException {        
        if (this.storage != null) {
            synchronized (TIMESTAMPS_LOCK) {
                Properties timeStamps = getTimeStamps();
                if (timeStamps.get(NB_HOME_PROPERTY) == null) {
                    String currNbLoc = getNBRoots();
                    timeStamps.put(NB_HOME_PROPERTY,currNbLoc);
                }
                FileObject parent = storage.getParent();
                FileObject timeStampFile = parent.getFileObject(TIME_STAMPS_FILE);
                if (timeStampFile == null) {
                    timeStampFile = parent.createData(TIME_STAMPS_FILE);
                }
                FileLock lock = timeStampFile.lock();
                try {
                    OutputStream out = timeStampFile.getOutputStream(lock);
                    try {
                        timeStamps.store (out, null);    
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        }
    }        
    
    private synchronized Properties getTimeStamps () {
        if (this.timeStamps == null) {
            this.timeStamps = new Properties();
            if (this.storage != null) {
                FileObject timeStampFile = storage.getParent().getFileObject(TIME_STAMPS_FILE);
                if (timeStampFile != null) {
                    try {
                        InputStream in = timeStampFile.getInputStream();
                        try {
                            this.timeStamps.load (in);
                        } finally {
                            in.close();
                        }
                        String nbLoc = (String) this.timeStamps.get (NB_HOME_PROPERTY);
                        String currNbLoc = getNBRoots ();                                                
                        if (nbLoc == null || !nbLoc.equals (currNbLoc)) {
                            this.timeStamps.clear();
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        return this.timeStamps;
    }
    
    private static String getNBRoots () {
        Set<String> result = new TreeSet<String>();
        String currentNbLoc = System.getProperty ("netbeans.home");   //NOI18N
        if (currentNbLoc != null) {
            File f = FileUtil.normalizeFile(new File (currentNbLoc));
            if (f.isDirectory()) {
                result.add (f.getAbsolutePath());
            }
        }
        currentNbLoc = System.getProperty ("netbeans.dirs");        //NOI18N
        if (currentNbLoc != null) {
            StringTokenizer tok = new StringTokenizer(currentNbLoc, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                 File f = FileUtil.normalizeFile(new File(tok.nextToken()));
                 result.add(f.getAbsolutePath());
            }
        }
        StringBuffer sb = new StringBuffer ();
        for (Iterator<String> it = result.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(":");  //NOI18N
            }
        }
        return sb.toString();
    }

    public void stateChanged(ChangeEvent e) {        
        fireLibrariesChanged();
    }
}
