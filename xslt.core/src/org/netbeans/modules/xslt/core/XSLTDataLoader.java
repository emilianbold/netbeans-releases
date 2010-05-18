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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
import org.netbeans.modules.soa.ui.SoaUtil;

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
    public static final String PRIMARY_EXTENSION = "xsl";                 // NOI18N
    public static final String PRIMARY_EXTENSION2 = "xslt";              // NOI18N
  
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
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }
    
    /**
     * Lazy init name.
     */
    @Override
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
    
    @Override
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
                    FileObject projectSource = Util.getProjectSource(SoaUtil.getProject(fo));
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
    
    @Override
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
    @Override
        protected String actionsContext() {
                return ACTION_CONTEXT;
        }
}
