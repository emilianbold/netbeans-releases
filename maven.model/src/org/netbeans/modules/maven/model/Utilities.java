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

package org.netbeans.modules.maven.model;

import hidden.org.codehaus.plexus.util.IOUtil;
import hidden.org.codehaus.plexus.util.StringInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesModelFactory;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 * Utility class to create ModelSource (environment) for the
 * to be created models.
 *
 * copied from xml.retriever and customized.
 * @author mkleint
 */
public class Utilities {
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    private static final String PROFILES_SKELETON =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //NO18N
"<profilesXml xmlns=\"http://maven.apache.org/PROFILES/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +//NO18N
"  xsi:schemaLocation=\"http://maven.apache.org/PROFILES/1.0.0 http://maven.apache.org/xsd/profiles-1.0.0.xsd\">\n" +//NO18N
"</profilesXml>";//NO18N
    //a copy is in CustomizerProviderImpl

    /**
     * 
     * @param file
     * @param editable
     * @param skeleton current content of the document
     * @param mimeType
     * @return
     */
    public static ModelSource createModelSourceForMissingFile(File file, boolean editable, String skeleton, String mimeType) {
        try {
            BaseDocument doc = new BaseDocument(false, mimeType);
            doc.insertString(0, skeleton, null);
            InstanceContent ic = new InstanceContent();
            Lookup lookup = new AbstractLookup(ic);
            ic.add(file);
            ic.add(doc);
            ModelSource ms = new ModelSource(lookup, editable);
//            ic.add(new DummyCatalogModel());
//            final CatalogModel catalogModel;
//            try {
//                catalogModel = CatalogModelFactory.getDefault().getCatalogModel(ms);
//                assert catalogModel != null;
//                if (catalogModel != null) {
//                    ic.add(catalogModel);
//                }
//            } catch (CatalogModelException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            return ms;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        assert false : "Failed to load the model for non-existing file";
        return null;
    }
    
    
    public static Document getDocument(FileObject modelSourceFileObject) {
        Document result = null;
        try {
            DataObject dObject = DataObject.find(modelSourceFileObject);
            EditorCookie ec = dObject.getCookie(EditorCookie.class);
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
            EditorCookie ec = modelSourceDataObject.getCookie(EditorCookie.class);
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
        return ms.getLookup().lookup(FileObject.class);
    }
    
    public static CatalogModel getCatalogModel(ModelSource ms) throws CatalogModelException{
        return CatalogModelFactory.getDefault().getCatalogModel(ms);
    }
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    public static ModelSource createModelSource(FileObject thisFileObj) {
        assert thisFileObj != null : "Null file object.";

        final FileObject fobj = thisFileObj;

        DataObject tempdobj;
        try {
            tempdobj = DataObject.find(fobj);
        } catch (DataObjectNotFoundException ex) {
            tempdobj = null;
        }
        final DataObject dobj = tempdobj;

        final File fl = FileUtil.toFile(fobj);
        boolean editable = (fl != null);

        Lookup proxyLookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                Document document = null;
                try {
                    document = _getDocument(dobj);
                    if (document != null) {
                        return Lookups.fixed(new Object[] {
                            fobj,
                            dobj,
                            fl,
                            document
                        });
                    } else {
                        return Lookups.fixed(new Object[] {
                            fobj,
                            dobj,
                            fl
                        });
                    }
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, ioe.getMessage());
                    return Lookups.fixed(new Object[] {
                        fobj,
                        dobj,
                        fl
                    });
                }
            }
        });

        return new ModelSource(proxyLookup, editable);
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
        model.sync();
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        if (dobj == null) {
            final Document doc = model.getModelSource().getLookup().lookup(Document.class);
            final File file = model.getModelSource().getLookup().lookup(File.class);
            File parent = file.getParentFile();
            FileObject parentFo = FileUtil.toFileObject(parent);
            if (parentFo == null) {
                parent.mkdirs();
                FileUtil.refreshFor(parent);
                parentFo = FileUtil.toFileObject(parent);
            }
            final FileObject fParentFo = parentFo;
            if (fParentFo != null) {
                FileSystem fs = parentFo.getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileObject fo = fParentFo.getFileObject(file.getName());
                        if (fo == null) {
                            fo = fParentFo.createData(file.getName());
                        }
                        OutputStream out = null;
                        try {
                            String text = doc.getText(0, doc.getLength());
                            //TODO how is encoding handled??
                            StringInputStream in = new StringInputStream(text);
                            out = fo.getOutputStream();
                            FileUtil.copy(in, out);
                            out.close();
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            IOUtil.close(out);
                        }
                    }
                });
            } else {
                //TODO report
            }
        } else {
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                save.save();
            } else {
                //not changed?
            }
        }
    }

    /**
     * performs model modifying operations on top of the POM model. After modifications,
     * the model is persisted to file.
     * @param pomFileObject
     * @param operations
     */
    public static void performPOMModelOperations(FileObject pomFileObject, List<ModelOperation<POMModel>> operations) {
        assert pomFileObject != null;
        assert operations != null;
        ModelSource source = Utilities.createModelSource(pomFileObject);
        POMModel model = POMModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.sync();
                if (Model.State.VALID != model.getState()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_POM", NbBundle.getMessage(Utilities.class,"ERR_INVALID_MODEL")), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                    return;
                }
                model.startTransaction();
                for (ModelOperation<POMModel> op : operations) {
                    op.performOperation(model);
                }
                model.endTransaction();
                Utilities.saveChanges(model);
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_POM", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, "Canot write POM", ex);
//                Exceptions.printStackTrace(ex);
            } finally {
                if (model.isIntransaction()) {
                    model.rollbackTransaction();
                }
            }
        } else {
            Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Cannot create model from current content of " + pomFileObject);
        }
    }

    /**
     * performs model modifying operations on top of the profiles.xml model. After modifications,
     * the model is persisted to file.
     * @param profilesFileObject
     * @param operations
     */
    public static void performProfilesModelOperations(FileObject profilesFileObject, List<ModelOperation<ProfilesModel>> operations) {
        assert profilesFileObject != null;
        assert operations != null;
        ModelSource source = profilesFileObject.getSize() == 0 ?
            Utilities.createModelSourceForMissingFile(FileUtil.toFile(profilesFileObject), true, PROFILES_SKELETON, "text/x-maven-profile+xml") :
            Utilities.createModelSource(profilesFileObject);
        ProfilesModel model = ProfilesModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.sync();
                if (Model.State.VALID != model.getState()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_PROFILE", NbBundle.getMessage(Utilities.class,"ERR_INVALID_MODEL")), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                    return;
                }
                model.startTransaction();
                for (ModelOperation<ProfilesModel> op : operations) {
                    op.performOperation(model);
                }
                model.endTransaction();
                Utilities.saveChanges(model);
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_PROFILE", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, "Cannot write profiles.xml", ex);
            } finally {
                if (model.isIntransaction()) {
                    model.rollbackTransaction();
                }
            }
        } else {
            //TODO report error.. what is the error?
        }
    }


    /**
     * performs model modifying operations on top of the settings.xml model. After modifications,
     * the model is persisted to file.
     * @param profilesFileObject
     * @param operations
     */
    public static void performSettingsModelOperations(FileObject settingsFileObject, List<ModelOperation<SettingsModel>> operations) {
        assert settingsFileObject != null;
        assert operations != null;
        ModelSource source = Utilities.createModelSource(settingsFileObject);
        SettingsModel model = SettingsModelFactory.getDefault().getModel(source);
        if (model != null) {
            try {
                model.sync();
                if (Model.State.VALID != model.getState()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_PROFILE", NbBundle.getMessage(Utilities.class,"ERR_INVALID_MODEL")), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                    return;
                }
                model.startTransaction();
                for (ModelOperation<SettingsModel> op : operations) {
                    op.performOperation(model);
                }
                model.endTransaction();
                Utilities.saveChanges(model);
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Utilities.class, "ERR_SETTINGS", ex.getLocalizedMessage()), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT).clear(10000);
                Logger.getLogger(Utilities.class.getName()).log(Level.INFO, "Cannot write settings.xml", ex);
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
