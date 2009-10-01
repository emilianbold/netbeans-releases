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
package org.netbeans.modules.compapp.projects.common;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class represents the in memory model that holds the entries of the wsdl/xsd
 * imports used in resolving the implicit references in the xml files. It defines 
 * the interface to serialize/deserialize the entries to/from catalog.wsdl file to
 * represent the model on disk as a wsdl model.
 * 
 * @see CatalogWSDLModelSerializer
 * @see ImplicitCatalogSupport
 * 
 * @author chikkala
 */
public class CatalogWSDL {

    /** catalog wsdl namespace */
    public static final String TNS = "http://soa.netbeans.org/wsdl/catalog";
    /** file name for the catalog wsdl */
    public static final String CATALOG_WSDL_FILE = "catalog.wsdl";
    /** name of the wsdl defintion */
    public static final String CATALOG_NAME = "catalog";
    /** logger */
    private static final Logger sLogger = Logger.getLogger(CatalogWSDL.class.getName());
    /** serializer interface to read/write the catalog wsdl file */
    private static CatalogWSDLSerializer sSerializer = new XAMCatalogWSDLSerializer();
    /** entries in the catalog wsdl */
    private List<Entry> mEntries;

    /**
     * default constructor
     */
    public CatalogWSDL() {
        this.mEntries = new ArrayList<Entry>();
    }

    /**
     * return the list of entries in the catalog model
     * @return List of Entry objects
     */
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(this.mEntries);
    }

    /**
     * return the list of entries of type either wsdl or xsd imports
     * @param type WSDL or XSD.
     * @return List of Entry objects
     */
    public List<Entry> getEntries(EntryType type) {
        List<Entry> entries = new ArrayList<Entry>();
        for (Entry entry : this.mEntries) {
            if (entry.getType().equals(type)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * return entries correpsonding to the namespace
     * @param namespace namespace of the entreis
     * @return List of Entry objects that has the namespace.
     */
    public List<Entry> getEntries(String namespace) {
        List<Entry> entries = new ArrayList<Entry>();
        for (Entry entry : this.mEntries) {
            if (entry.getNamesapce().equals(namespace)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * finds the entry in the catalog. 
     * @param type type of the entry. can not be null
     * @param namespace namespace. can not be null
     * @param location location. can be null.
     * @return Entry object
     */
    public Entry getEntry(EntryType type, String namespace, String location) {
        for (Entry entry : this.mEntries) {
            if (entry.getType().equals(type) &&
                    entry.getNamesapce().equals(namespace) &&
                    (location == null || location.equals(entry.getLocation()))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * adds entries 
     * @param list of entries
     * @return true if the entries are added else false.
     */
    public boolean addAllEntries(List<Entry> list) {
        return this.mEntries.addAll(list);
    }

    /**
     * add single entrie
     * @param entry entry to be added
     * @return true if the entries is added else false.
     */
    public boolean addEntry(Entry entry) {
        return this.mEntries.add(entry);
    }

    /**
     * removes the entry from the list
     * @param entry object
     * @return true if the entry is removed successfully
     */
    public boolean removeEntry(Entry entry) {
        return this.mEntries.remove(entry);
    }

    /**
     * clear all entries
     */
    public void clear() {
        this.mEntries.clear();
    }

    /**
     * set the static reference to the serializer that can be used 
     * to serialize/deserialze the model to/from file.
     * @param serializer
     */
    public static void setCatalogWSDLSerializer(CatalogWSDLSerializer serializer) {
        sLogger.fine("new CatalogWSDLSerializer set");
        sSerializer = serializer;
    }

    /**
     * return the current serializer. if not set, creates a default one and return.
     * @return CatalogWSDLSerializer
     */
    public static CatalogWSDLSerializer getCatalogWSDLSerializer() {
        if (sSerializer == null) {
            sSerializer = new XAMCatalogWSDLSerializer();
        }
        return sSerializer;
    }

    /**
     * reads the catalog.xml file from a specified directory and return the 
     * in memory representation of it.
     * @param fromDir directory in which to look for the catalog.wsdl file
     * @return CatalogWSDL model
     * @throws java.io.IOException on error
     */
    public static CatalogWSDL loadCatalogWSDL(FileObject fromDir) throws IOException {
        FileObject catFO = fromDir.getFileObject(CATALOG_WSDL_FILE);
        if (catFO == null) {
            throw new FileNotFoundException("Catalog.wsdl file not found in " + fromDir.getPath());
        }
        return getCatalogWSDLSerializer().unmarshall(catFO);
    }

    /**
     * writes the in memory model to the catalog.wsdl file in a destination directory.
     * creates the file if it is not existing.
     * @param catalog in memory model
     * @param destDir direcotry to save the model
     * @throws java.io.IOException on error
     */
    public static void saveCatalogWSDL(CatalogWSDL catalog, FileObject destDir) throws IOException {
        FileObject catFO = CatalogWSDLSerializer.createCatalogWSDLFile(destDir, true);
        getCatalogWSDLSerializer().marshall(catalog, catFO);
    }

    /**
     * finds the catalog wsdl file in the project and return the corresponding 
     * file object.
     * 
     * @param project project in which the catalog file should be looked up.
     * @return file object correpsonding to the catalog wsdl file in the project
     * @throws java.io.IOException on error
     */
    public static FileObject getCatalogWSDLFile(Project project) throws IOException {
        FileObject catFO =
                CatalogWSDLSerializer.createCatalogWSDLFile(project.getProjectDirectory(), false);
        return catFO;
    }

    /**
     * loads the catalog wsdl model from the catalog.wsdl file in a project
     * 
     * @param project project from which to load the catalog wsdl model
     * @return memory model of the catalog wsdl file
     * @throws java.io.IOException on error.
     */
    public static CatalogWSDL loadCatalogWSDL(Project project) throws IOException {
        return loadCatalogWSDL(project.getProjectDirectory());
    }

    /**
     * saves the memory model of the catalog wsdl a location in a project.
     * @param catalog memory model
     * @param project project to which the model should be saved.
     * @throws java.io.IOException on error.
     */
    public static void saveCatalogWSDL(CatalogWSDL catalog, Project project) throws IOException {
        saveCatalogWSDL(catalog, project.getProjectDirectory());
    }

    /**
     * enumeration that represents the supported entries to the catalog wsdl. only
     * xsd and wsdl entries are supported.
     */
    public static enum EntryType {

        XSD, WSDL
    }

    /**
     * This class represents each entry into the catalog wsdl file in memory that
     * correpsonds to the wsdl or xsd imports.
     * 
     */
    public static class Entry {

        /** type of entry. wsdl or xsd */
        private EntryType mType;
        /** namespce of the entry */
        private String mNamespace;
        /** location of the entry. location in case of wsdl import, schemaLocation
        in case of xsd entry */
        private String mLocation;

        /**
         * constructor for the entry.
         * @param type
         */
        public Entry(EntryType type) {
            this.mType = type;
        }

        /**
         * getter for type
         * @return EntryType
         */
        public EntryType getType() {
            return this.mType;
        }

        /**
         * 
         * @return
         */
        public String getNamesapce() {
            return mNamespace;
        }

        /**
         * 
         * @param namespace
         */
        public void setNamespace(String namespace) {
            this.mNamespace = namespace;
        }

        /**
         * 
         * @return
         */
        public String getLocation() {
            return this.mLocation;
        }

        /**
         * 
         * @param location
         */
        public void setLocation(String location) {
            this.mLocation = location;
        }

        @Override
        public String toString() {
            return " Type     : " + this.mType +
                    "\n Namespace: " + this.mNamespace +
                    "\n Location : " + this.mLocation;
        }

        /**
         * creates a xsd entry 
         * @param namespace
         * @param location
         * @return
         */
        public static Entry createXSDEntry(String namespace, String location) {
            Entry entry = new Entry(EntryType.XSD);
            entry.setNamespace(namespace);
            entry.setLocation(location);
            return entry;
        }

        /**
         * creates a wsdl entry
         * @param namespace
         * @param location
         * @return
         */
        public static Entry createWSDLEntry(String namespace, String location) {
            Entry entry = new Entry(EntryType.WSDL);
            entry.setNamespace(namespace);
            entry.setLocation(location);
            return entry;
        }
    }

    /**
     * This class defines the serialization/deserialization interface that will
     * be used to load/save memory model of the catalog wsdl file to/from the 
     * disk. Concrete implementation can use any technique like xdm model, dom model
     * or other to implement the serialization/deserialization.
     */
    public static abstract class CatalogWSDLSerializer {

        /**
         * called to save the catalog wsdl model to the file
         * @param catWSDL model
         * @param catFO file object
         * @throws java.io.IOException on error
         */
        public abstract void marshall(CatalogWSDL catWSDL, FileObject catFO) throws IOException;

        /**
         * called to load the catalog wsdl model from the file
         * @param catFO file object
         * @return memory model of the catalog wsdl file
         * @throws java.io.IOException on error.
         */
        public abstract CatalogWSDL unmarshall(FileObject catFO) throws IOException;

        /**
         * utility method to write a data to the file. 
         * @param outFO file object 
         * @param srcBuff buffer containing the data.
         */
        public static void saveToFileObject(FileObject outFO, StringBuffer srcBuff) {
            FileLock outLock = null;
            OutputStream outS = null;
            InputStream inS = null;

            try {
                inS = new ByteArrayInputStream(srcBuff.toString().getBytes());
                outLock = outFO.lock();
                outS = outFO.getOutputStream(outLock);
                FileUtil.copy(inS, outS);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (inS != null) {
                    try {
                        inS.close();
                    } catch (IOException ex) {
                        sLogger.log(Level.FINER, ex.getMessage(), ex);
                    }
                }
                if (outS != null) {
                    try {
                        outS.close();
                    } catch (IOException ex) {
                        sLogger.log(Level.FINER, ex.getMessage(), ex);
                    }
                }
                if (outLock != null) {
                    outLock.releaseLock();
                }
            }
        }

        /**
         * utility method that can create the empty catalog wsdl file.
         * 
         * @param destDir directory in which the catalog file will be created.
         * @param overwrite overwrites with empty catalog wsdl defintion if the
         * file existis if set to true.
         * @return file object correpsonding to the catalog.wsdl file
         * @throws java.io.IOException on error.
         */
        public static FileObject createCatalogWSDLFile(FileObject destDir, boolean overwrite) throws IOException {
            FileObject fo = null;
            fo = destDir.getFileObject(CATALOG_WSDL_FILE);

            if (fo != null && !overwrite) {
                return fo;
            }
            if (fo == null) {
                // create a new data
                fo = FileUtil.createData(destDir, CATALOG_WSDL_FILE);
            }
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<definitions name=\"" + CATALOG_NAME + "\" targetNamespace=\"" + TNS + "\"");
            out.println("   xmlns=\"http://schemas.xmlsoap.org/wsdl/\"");
            out.println("   xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"");
            out.println("   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
            out.println("   xmlns:tns=\"" + TNS + "\"");
            out.println(">");
            out.println("   <types/>");
            out.println("</definitions>");
            out.close();
            try {
                writer.close();
            } catch (IOException ioEx) {
            //ignore.
            }
            saveToFileObject(fo, writer.getBuffer());

            return fo;
        }
    }
}
