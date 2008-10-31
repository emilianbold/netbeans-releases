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

package org.netbeans.modules.maven.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Utility class to create ModelSource (environment) for the
 * to be created models.
 *
 * copied from xml.retriever and customized.
 * @author mkleint
 */
public class Utilities {
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    
    
    public static Document getDocument(FileObject modelSourceFileObject) {
        Document result = null;
        try {
            DataObject dObject = DataObject.find(modelSourceFileObject);
            EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            if(doc instanceof BaseDocument)
                return doc;
            
            
//            result = new org.netbeans.editor.BaseDocument(
//                    org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
//            String str = doc.getText(0, doc.getLength());
//            result.insertString(0,str,null);
            
        } catch (Exception dObjEx) {
            return null;
        }
        return result;
    }
    
    private static Document _getDocument(DataObject modelSourceDataObject)
    throws IOException {
        Document result = null;
        if (modelSourceDataObject != null && modelSourceDataObject.isValid()) {
            EditorCookie ec = (EditorCookie)
            modelSourceDataObject.getCookie(EditorCookie.class);
            assert ec != null : "Data object "+modelSourceDataObject.getPrimaryFile().getPath()+" has no editor cookies.";
            Document doc = null;
            try {
                doc = ec.openDocument();
            } catch (UserQuestionException uce) {
                // this exception is thrown if the document is to large
                // lets just confirm that it is ok
                uce.confirmed();
                doc = ec.openDocument();
            }
//            assert(doc instanceof BaseDocument) : "instance of " + doc.getClass();
            result = doc;
        }
        return result;
    }
    
    /**
     * This method must be overridden by the Unit testcase to return a special
     * Document object for a FileObject.
     */
    protected static Document _getDocument(FileObject modelSourceFileObject)
    throws DataObjectNotFoundException, IOException {
        DataObject dObject = DataObject.find(modelSourceFileObject);
        return _getDocument(dObject);
    }
    
    
    public static FileObject getFileObject(ModelSource ms) {
        return (FileObject) ms.getLookup().lookup(FileObject.class);
    }
    
    public static CatalogModel getCatalogModel(ModelSource ms) throws CatalogModelException{
        return CatalogModelFactory.getDefault().getCatalogModel(ms);
    }
    
    public static ModelSource getModelSource(FileObject bindingHandlerFO, boolean editable){
        return createModelSource(bindingHandlerFO, editable);
    }
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    public static ModelSource createModelSource(FileObject thisFileObj,
            boolean editable) {
        assert thisFileObj != null : "Null file object.";


        DataObject dobj = null;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            //does this ever happen?
        }
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        if (dobj != null) {
            ic.add(dobj);
        }
        ic.add(thisFileObj);
        ic.add(FileUtil.toFile(thisFileObj));

        ModelSource ms = new ModelSource(lookup, editable);
        final CatalogModel catalogModel;
        try {
            catalogModel = CatalogModelFactory.getDefault().getCatalogModel(ms);
            assert catalogModel != null;
            if (catalogModel != null) {
                ic.add(catalogModel);
            }
        } catch (CatalogModelException ex) {
            Exceptions.printStackTrace(ex);
        }
        Document document = null;
        try {
            document = _getDocument(dobj);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage());
        }
        if (document != null) {
            ic.add(document);
        }
        return ms;
    }

    /**
     * attempts to save the document model to disk.
     * if model is in transaction, the transaction is ended first,
     * then dataobject's SaveCookie is called.
     *
     * @param model
     * @throws java.io.IOException if saving fails.
     */
    public static void saveChanges(AbstractDocumentModel model) throws IOException {
        if (model.isIntransaction()) {
            model.endTransaction();
        }
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
        if (save != null) {
            save.save();
        } else {
            //not changed?
        }
    }

    /**
     * performs model modifying operations on top of the model loaded. After modifications,
     * the model is persisted to file.
     * @param pomFileObject
     * @param operations
     */
    public static void performPOMModelOperations(FileObject pomFileObject, List<ModelOperation<POMModel>> operations) {
        assert pomFileObject != null;
        assert operations != null;
        ModelSource source = Utilities.createModelSource(pomFileObject, true);
        POMModel model = POMModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.startTransaction();
                for (ModelOperation<POMModel> op : operations) {
                    op.performOperation(model);
                }
                model.endTransaction();
                Utilities.saveChanges(model);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (model.isIntransaction()) {
                    model.rollbackTransaction();
                }
            }
        } else {
            //TODO report error.. what is the error?
        }
    }
}
