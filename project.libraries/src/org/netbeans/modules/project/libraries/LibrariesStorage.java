/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries;

import java.util.*;
import java.net.URL;
import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.xml.EntityCatalog;
import org.openide.filesystems.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;


public class LibrariesStorage extends FileChangeAdapter implements WriteableLibraryProvider {

    private static final String LIBRARIES_REPOSITORY = "org-netbeans-api-project-libraries/Libraries";

    // persistent storage, it may be null for before first library is store into storage
    private FileObject storage = null;

    private Map libraries;

    private Map librariesByFileNames;

    // Library declaraion public ID
    // i18n bundle
    private ResourceBundle bundle;

    private PropertyChangeSupport support;


    /**
     * Create libraries that need to be populated later.
     */
    public LibrariesStorage() {
        this.initStorage();   //Create folder, in future we will allow to create libraries
        if (storage != null) {
            this.loadFromStorage();
            this.storage.addFileChangeListener (this);
        } else {
            // Storage broken. May happen e.g. inside unit tests.
            libraries = Collections.EMPTY_MAP;
            librariesByFileNames = Collections.EMPTY_MAP;
        }
        this.support = new PropertyChangeSupport(this);
    }



    /**
     * Lazily initialize storage.
     * @return new storage or null on I/O error.
     */
    private final FileObject initStorage () {
        FileSystem storageFS = Repository.getDefault().getDefaultFileSystem();
        if (storage == null) {
            try {
                storage = FileUtil.createFolder(storageFS.getRoot(), LIBRARIES_REPOSITORY);
            } catch (IOException e) {
                // storage remains null
            }
        }
        return storage;
    }


    // scans over storage and fetchs it ((fileset persistence files) into memory
    // ... note that providers can read their data during getVolume call
    private void loadFromStorage() {
        // configure parser
        libraries = new HashMap();
        librariesByFileNames = new HashMap();
        LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
        EntityResolver resolver = EntityCatalog.getDefault();
        LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();
        LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,resolver,convertor);
        // parse
        FileObject libraryDefinitions[] = storage.getChildren();
        for (int i = 0; i < libraryDefinitions.length; i++) {
            FileObject descriptorFile = libraryDefinitions[i];
            try {
                readLibrary (descriptorFile, parser);
                LibraryImplementation impl = handler.getLibrary ();
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    ErrorManager.getDefault().log("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                }
                else {
                    provider.libraryCreated (impl);
                    librariesByFileNames.put(descriptorFile.getPath(),impl);
                    libraries.put (impl.getName(),impl);
                }
            } catch (SAXException e) {
                ErrorManager.getDefault().notify (e);
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                ErrorManager.getDefault().notify (e);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify (e);
            } catch (RuntimeException e) {
                // Other problem.
                ErrorManager.getDefault().notify (e);
            }
        }
    }

    private static LibraryImplementation readLibrary (FileObject descriptorFile) throws SAXException, javax.xml.parsers.ParserConfigurationException, IOException {
        LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
        EntityResolver resolver = EntityCatalog.getDefault();
        LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();
        LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,resolver,convertor);
        readLibrary (descriptorFile, parser);
        return handler.getLibrary();
    }

    private static void readLibrary (FileObject descriptorFile, LibraryDeclarationParser parser) throws SAXException, javax.xml.parsers.ParserConfigurationException, IOException {
        URL baseURL = descriptorFile.getURL();
        InputSource input = new InputSource(baseURL.toExternalForm());
        input.setByteStream(descriptorFile.getInputStream()); // #33554 workaround
        try {
            parser.parse(input);
        } catch (SAXException e) {
            ErrorManager.getDefault().annotate(e, ErrorManager.UNKNOWN, "From " + baseURL, null, null, null);
            throw e;
        }
    }

    private static void writeLibrary (final FileObject storage, final LibraryImplementation library) throws IOException {
        storage.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction () {
                    public void run() throws IOException {
                        String libraryType = library.getType ();
                        LibraryTypeProvider libraryTypeProvider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
			if (libraryTypeProvider == null) {
                            ErrorManager.getDefault().log ("Can not store library, the library type is not recognized by any of the LibraryTypeProviders.");	//NOI18N
                            return;
			}
                        FileObject fo = storage.createData (library.getName(),"xml");   //NOI18N
                        FileLock lock = null;
                        PrintWriter out = null;
                        try {
                            lock = fo.lock();
                            out = new PrintWriter(new OutputStreamWriter(fo.getOutputStream (lock)));							
                            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");      //NOI18N
                            out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">"); //NOI18N
                            out.println("<library version=\"1.0\">");       			//NOI18N
                            out.println("\t<name>"+library.getName()+"</name>");        //NOI18N
							out.println("\t<type>"+libraryType+"</type>");
                            String description = library.getDescription();
                            if (description != null && description.length() > 0) {
                                out.println("\t<description>"+description+"</description>");   //NOI18N
                            }
                            String[] volumeTypes = libraryTypeProvider.getSupportedVolumeTypes ();
                            for (int i = 0; i < volumeTypes.length; i++) {
                                out.println("\t<volume>");      //NOI18N
                                out.println ("\t\t<type>"+volumeTypes[i]+"</type>");   //NOI18N
                                List volume = library.getContent (volumeTypes[i]);
                                if (volume != null) {
                                    //If null -> broken library, repair it.
                                    for (Iterator eit = volume.iterator(); eit.hasNext();) {
                                        URL url = (URL) eit.next ();
                                        out.println("\t\t<resource>"+url+"</resource>"); //NOI18N
                                    }
                                }
                                out.println("\t</volume>");     //NOI18N
                            }
                            out.println("</library>");  //NOI18N
                        } finally {
                            if (out !=  null)
                                out.close();
                            if (lock != null)
                                lock.releaseLock();
                        }
                    }
                }
        );
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
    public synchronized final LibraryImplementation[] getLibraries() {
        assert this.storage != null : "Storage is not initialized";
        return (LibraryImplementation[]) libraries.values().toArray(new LibraryImplementation[libraries.size()]);
    } // end getLibraries


    public void addLibrary (LibraryImplementation library) throws IOException {
        assert this.storage != null : "Storage is not initialized";
        writeLibrary(this.storage,library);
    }

    public void removeLibrary (LibraryImplementation library) throws IOException {
        assert this.storage != null : "Storage is not initialized";
        for (Iterator jt = this.librariesByFileNames.keySet().iterator(); jt.hasNext();) {
            String key = (String) jt.next ();
            LibraryImplementation lib = (LibraryImplementation) this.librariesByFileNames.get(key);
            if (library.equals (lib)) {
                FileObject fo = this.storage.getFileSystem().findResource (key);
                if (fo != null) {
                    fo.delete();
                    return;
                }
            }
        }
    }


    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        try {
            final LibraryImplementation impl = readLibrary (fo);
            LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
            if (provider == null) {
                ErrorManager.getDefault().log("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
            }
            else {
                synchronized (this) {
                    provider.libraryCreated (impl);
                    this.libraries.put (impl.getName(), impl);
                    this.librariesByFileNames.put (fo.getPath(), impl);
                }
                this.fireLibrariesChanged();
            }
        } catch (SAXException e) {
            ErrorManager.getDefault().notify (e);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            ErrorManager.getDefault().notify (e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify (e);
        }
    }

    public void fileDeleted(FileEvent fe) {
        String fileName = fe.getFile().getPath();
        LibraryImplementation impl;
        synchronized (this) {
            impl = (LibraryImplementation) this.librariesByFileNames.remove (fileName);
            if (impl != null) {
                this.libraries.remove (impl.getName());
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    ErrorManager.getDefault().log("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryDeleted(), the library type provider is unknown.");  //NOI18N
                }
                else {
                    provider.libraryDeleted (impl);
                }                
            }
        }
        if (impl != null) {
            this.fireLibrariesChanged();
        }
    }

    protected final ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = NbBundle.getBundle(LibrariesStorage.class);
        }
        return bundle;
    }


} // end LibrariesStorage

