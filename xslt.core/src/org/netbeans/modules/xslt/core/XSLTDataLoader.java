/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.core;

import java.io.IOException;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 * Temporary use UniFileLoader until the role 
 * of secondary file becomes clear...
 *
 */
public class XSLTDataLoader extends UniFileLoader {
    private static final long serialVersionUID = 1L;

    public static final String MIME_TYPE = "application/xslt+xml";
    static final String PRIMARY_EXTENSION = "xsl";                 // NOI18N
    static final String PRIMARY_EXTENSION2 = "xslt";              // NOI18N
  
    // TODO m
    static final String TRANSFORM_MAP_FILE = "transformmap.xml";              // NOI18N

    // TODO r | m
//    static final String SECONDARY_EXTENSION = "N/A"; // NOI18N
    public static final String ACTION_CONTEXT = "Loaders/" +       // NOI18N
        MIME_TYPE + "/Actions";                                    // NOI18N
    static final String LOADER_NAME ="LBL_loader_name";             // NOI18N 
    
    public XSLTDataLoader() {
        super("org.netbeans.modules.xslt.core.XSLTDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(XSLTDataLoader.class, LOADER_NAME ); 
    }
    
    
    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject( FileObject primaryFile )
            throws DataObjectExistsException, IOException
    {
        return new XSLTDataObject(primaryFile, this);
    }
    
    protected FileObject findPrimaryFile( FileObject fo ) {
        FileObject primaryFo = null;
        String extension = fo.getExt();
        if (extension.equals(PRIMARY_EXTENSION) 
            || extension.equals(PRIMARY_EXTENSION2)) 
        {
            // recognize xslt file only in context of XSLT project
            primaryFo = isXsltProjectContext(fo) ? fo : null;
        }
        return primaryFo;
    }
    
    private boolean isXsltProjectContext(FileObject fo) {
        boolean isContext = false;
        try {
            if (!fo.getFileSystem().isDefault() && !fo.isFolder()) {
                String extension = fo.getExt();
                if (extension.equals(PRIMARY_EXTENSION)
                    || extension.equals(PRIMARY_EXTENSION2)) 
                {
                    FileObject projectSource = Util.getProjectSource(Util.getProject(fo));
                    isContext = projectSource != null 
                            && projectSource.getFileObject(TRANSFORM_MAP_FILE) != null;
                }
            }
            
            
// TODO r
//            if (isContext) {
//                XsltMapModel xsltMapModel = XsltMapAccessor.
//                                            getXsltMapModel(getTransformMapFo(fo));
//                isContext = xsltMapModel != null && xsltMapModel.getFirstTransformationDesc(fo) != null;
//            }
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        return isContext;
    }
    
    private boolean isEqualInputFile(String inputFile, FileObject xmlFile) {
        return inputFile != null 
                && xmlFile != null 
                && inputFile.equals(xmlFile.getNameExt());
    }
    
    private FileObject getTransformMapFo(FileObject xsltFo) {
        return xsltFo.getParent().getFileObject(TRANSFORM_MAP_FILE);
    }
    
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, 
            FileObject primaryFile) 
    {
//        return new XMLDataLoader.XMLFileEntry (obj, primaryFile);  //adds smart templating
        return new FileEntry(obj, primaryFile);
    }
    
        /**
         * other modules can decorate with Special Actions
         * to see the default actions look in the layer.xml
         */
        protected String actionsContext() {
                return ACTION_CONTEXT;
        }
}
