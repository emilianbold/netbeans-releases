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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.CatalogWSDLSerializer;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.Entry;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.EntryType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xdm.xam.XDMAccessProvider;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
/**
 * This class is a concrete implemenation of the CatalogWSDLSerializer which uses
 * WSDL XDM Model for reading/writing the catalog.wsdl file
 * @see CatalogWSDLSerializer
 * @author chikkala
 */
public class XAMCatalogWSDLSerializer extends CatalogWSDLSerializer {
    /** logger */
    private static final Logger sLogger = Logger.getLogger(XAMCatalogWSDLSerializer.class.getName());
    /** defuult model source provider that is used to load/save xdm model */
    private static ModelSourceProvider sModelSourceProvider = new ModelSourceProvider();
    /** 
     * setter for the static model source provider which will be used to
     * create xdm model.
     * The model source implemenation provided in the retriever package's Utility
     * class is not suitable, a simple model source provider implemenation is 
     * provided with the default one. @see ModelSourceProvider for more details.
     * 
     * @param msProvider ModelSourceProvider
     */
    static void setModelSourceProvider(ModelSourceProvider msProvider) {
        sLogger.fine("new MessageSourceProvider set");
        sModelSourceProvider = msProvider;
    }
    /**
     * return the current model source provider. If the provider is not set
     * a default will be created and returned 
     * @return ModelSourceProvider
     */
    static ModelSourceProvider getModelSourceProvider() {
        if (sModelSourceProvider == null) {
            sModelSourceProvider = new ModelSourceProvider();
        }
        return sModelSourceProvider;
    }
    /**
     * 
     * @param catWSDL
     * @param catFO
     * @throws java.io.IOException
     */
    public void marshall(CatalogWSDL catWSDL, FileObject catFO) throws IOException {
        saveCatalogWSDL(catWSDL, catFO);
    }
    /**
     * 
     * @param catFO
     * @return
     * @throws java.io.IOException
     */
    public CatalogWSDL unmarshall(FileObject catFO) throws IOException {
        CatalogWSDL catWSDL = null;
        Definitions wsdlDef = loadCatalogWSDLDefinitions(catFO);
        catWSDL = loadCatalogWSDL(wsdlDef);
        return catWSDL;
    }
    /**
     * loads the wsdl definition model from the file.
     * @param catalogWsdlFO
     * @return
     */
    private Definitions loadCatalogWSDLDefinitions(FileObject catalogWsdlFO) {
        try {
            ModelSource catModelSource = getModelSourceProvider().getModelSource(catalogWsdlFO, true);
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(catModelSource);
            Definitions wsdlDef = wsdlModel.getDefinitions();
            return wsdlDef;
        } catch (CatalogModelException ex) {
            //TODO: log exception. 
            sLogger.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }
    /**
     * adds xsd entries to the catalog model from the wsdl definition's schema 
     * import elements 
     * @param catalog
     * @param wsdlDef
     */
    private void addXSDEntries(CatalogWSDL catalog, Definitions wsdlDef) {

        Collection<Schema> schemas = wsdlDef.getTypes().getSchemas();
        Schema xsdSchema = null;
        for (Schema schema : schemas) {
            //TODO: check if you have more than one schema. if yes. pick one with catalog tns
            xsdSchema = schema;
            break;
        }
        if (xsdSchema != null) {
            Collection<org.netbeans.modules.xml.schema.model.Import> xsdImports = xsdSchema.getImports();
            for (org.netbeans.modules.xml.schema.model.Import xsdImport : xsdImports) {
                Entry xsdEntry = new Entry(EntryType.XSD);
                xsdEntry.setNamespace(xsdImport.getNamespace());
                xsdEntry.setLocation(xsdImport.getSchemaLocation());
                catalog.addEntry(xsdEntry);
            }
        }
    }
    /**
     * adds wsdl entries to the catalog model from wsdl import elements of the 
     * defintions 
     * @param catalog
     * @param wsdlDef
     */
    private void addWSDLEntries(CatalogWSDL catalog, Definitions wsdlDef) {
        Collection<Import> wsdlImports = wsdlDef.getImports();
        for (Import wsdlImport : wsdlImports) {
            Entry wsdlEntry = new Entry(EntryType.WSDL);
            wsdlEntry.setNamespace(wsdlImport.getNamespace());
            wsdlEntry.setLocation(wsdlImport.getLocation());
            catalog.addEntry(wsdlEntry);
        }
    }
    /**
     * loads the catalog model from the wsdl defintions model
     * @param wsdlDef
     * @return
     * @throws java.io.IOException
     */
    private CatalogWSDL loadCatalogWSDL(Definitions wsdlDef) throws IOException {
        CatalogWSDL catalog = new CatalogWSDL();
        if (!CatalogWSDL.CATALOG_NAME.equalsIgnoreCase(wsdlDef.getName()) ||
                !CatalogWSDL.TNS.equals(wsdlDef.getTargetNamespace())) {
            throw new IOException("Not a CATALOG WSDL File");
        }
        // add wsdl entries
        addWSDLEntries(catalog, wsdlDef);
        // add xsd entries
        addXSDEntries(catalog, wsdlDef);

        return catalog;
    }
    /**
     * add the namespace prefixes to the wsdl model
     * @param wsdlModel
     * @param namespace
     */
    protected void addNamespacePrefix(WSDLModel wsdlModel, String namespace) {
        String prefix = null;
        String prefixPrefix = "ns";
        Map prefixes = null;
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) wsdlModel.getDefinitions();
        prefixes = def.getPrefixes();
        for ( int i=0; i < Integer.MAX_VALUE; ++i ) {
            if (!prefixes.containsKey(prefixPrefix+i)) {
                prefix = prefixPrefix+i;
                def.addPrefix(prefix, namespace);
                break;
            }
        }
    }

    /**
     * adds the schema import elements to the wsdl definitions model correponding
     * to the xsd entries of the catalog model.
     * Must call this between WSDLModel.startTransaction and WSDLModel.endTransaction.
     * @param wsdlDef wsdl definition model
     * @param entry wsdl catalog entry
     */
    private void addEntryAsSchemaImport(Definitions wsdlDef, Entry xsdEntry) {
        WSDLModel wsdlModel = wsdlDef.getModel();
        Types types = wsdlDef.getTypes();
        if (types == null) {
            // no types, create one
            types = wsdlModel.getFactory().createTypes();
            wsdlDef.setTypes(types);
        }
        Schema xsdSchema = null;
        Collection<Schema> schemas = types.getSchemas();
        if (schemas == null || schemas.size() == 0) {
            // no schema, create one
            WSDLSchema wsdlSchema = wsdlModel.getFactory().createWSDLSchema();
            types.addExtensibilityElement(wsdlSchema);
            xsdSchema = wsdlSchema.getSchemaModel().getSchema();
            xsdSchema.setTargetNamespace(CatalogWSDL.TNS);
        } else {
            // get the first one as the schema
            for (Schema s : schemas) {
                //TODO: check if you have more than one schema. if yes. pick one with catalog tns
                xsdSchema = s;
                break;
            }
        }
        // create import schema
        org.netbeans.modules.xml.schema.model.Import schemaImport =
                xsdSchema.getModel().getFactory().createImport();
        // init
        schemaImport.setNamespace(xsdEntry.getNamesapce());
        schemaImport.setSchemaLocation(xsdEntry.getLocation());
        // add
        xsdSchema.addExternalReference(schemaImport);
        addNamespacePrefix(wsdlModel, xsdEntry.getNamesapce());
    }

    /**
     * adds the wsdl import elements to the wsdl definitions model correponding
     * to the wsdl entries of the catalog model.
     * Must call this between WSDLModel.startTransaction and WSDLModel.endTransaction.
     * @param wsdlDef wsdl definition model
     * @param wsdlEntry wsdl catalog entry
     */
    private void addEntryAsWSDLImport(Definitions wsdlDef, Entry wsdlEntry) {

        WSDLModel wsdlModel = wsdlDef.getModel();
        Import imp = wsdlModel.getFactory().createImport();
        // init import
        imp.setNamespace(wsdlEntry.getNamesapce());
        imp.setLocation(wsdlEntry.getLocation());
        // add 
        wsdlDef.addImport(imp);
        addNamespacePrefix(wsdlModel, wsdlEntry.getNamesapce());
    }
    /**
     * saves the catalog model to the catalog file after translating the 
     * catalog model to the wsdl xdm model.
     * @param catalog
     * @param catalogFO
     * @throws java.io.IOException
     */
    private void saveCatalogWSDL(CatalogWSDL catalog, FileObject catalogFO) throws IOException {
        Definitions wsdlDef = loadCatalogWSDLDefinitions(catalogFO);
        List<Entry> entries = catalog.getEntries();
        WSDLModel wsdlModel = wsdlDef.getModel();
        try {
            wsdlModel.startTransaction();
            for (Entry entry : entries) {
                if (EntryType.XSD.equals(entry.getType())) {
                    // add schema import
                    addEntryAsSchemaImport(wsdlDef, entry);
                } else if (EntryType.WSDL.equals(entry.getType())) {
                    // add wsdl import
                    addEntryAsWSDLImport(wsdlDef, entry);
                } else {
                    sLogger.fine("!!!!!Unsupported CatalogWSDL entry!!!!!");
                }
            }
        } finally {
            if (wsdlModel.isIntransaction()) {
                wsdlModel.endTransaction();
            }
        }

        // make sure that all the open editors are saved to disk and then overwrite it 
        // read the text in the base document.
        String text = null;
        Document doc = wsdlModel.getModelSource().getLookup().lookup(BaseDocument.class);
        if (doc != null) {
            int size = doc.getLength();
            try {
                text = doc.getText(0, size);
            } catch (Exception ex) {
                sLogger.log(Level.FINE, ex.getMessage(), ex);
            }
        }
        if (text != null) {
            // catalog wsdl is not supposed to be edited outside this serializer. So, save all the
            // outside edits and then overwrite them with the acutal modified content.
            // use save cookie or editor cookie to save any open and edited document references
            DataObject dObj = DataObject.find(catalogFO);
            SaveCookie saveCookie = dObj.getCookie(SaveCookie.class);
            EditorCookie editorCookie = (EditorCookie) dObj.getCookie(EditorCookie.class);
            if ( saveCookie != null ) {
                saveCookie.save();  
            }
            if ( editorCookie != null ) {
                editorCookie.saveDocument();
            }
            // save the actual update 
            saveToFileObject(catalogFO, new StringBuffer(text));
        } else {
            sLogger.fine("BASE DOCUMENT TEXT IS NULL. Can not save catalog wsdl");
        }


    }
    /**
     * This is the default implemenation of the ModelSource provider. The retriever
     * modules Utility method Utilities.getModelSource(...) requires a lot of 
     * project and editor api dependencies which is not suitable for the simple
     * serializer and its unit testing, we need this class to provide a simple 
     * implemenation of the model source that uses the swing document to 
     * represent file on the disk.
     * 
     */
    public static class ModelSourceProvider {
        /**
         * creates the ModelSource for a file
         * @param fileObject
         * @param editable
         * @return
         * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
         */
        public ModelSource getModelSource(final FileObject fileObject, boolean editable) throws CatalogModelException {

            BaseDocument document = null;
            document = (BaseDocument) loadSwingDocument(fileObject);
            Lookup lookup = Lookups.fixed(new Object[]{document});
            return new ModelSource(lookup, editable);
        }
        /**
         * loads the file data into swing document that is needed for the 
         * model source 
         * @param fileObject
         * @return
         */
        public Document loadSwingDocument(FileObject fileObject) {
            InputStream in = null;
            Document sd = null;
            try {
                in = fileObject.getInputStream();
                sd = (new XDMAccessProvider()).loadSwingDocument(in);
            } catch (IOException ioEx) {
                sLogger.log(Level.FINE, ioEx.getMessage(), ioEx);
            } catch (BadLocationException locEx) {
                sLogger.log(Level.FINE, locEx.getMessage(), locEx);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    //ignore
                    }
                }
            }
            if (sd == null) {
                sLogger.fine("BaseDocument is NULL in loadSwingDocument");
            }
            return sd;
        }
    }
}
