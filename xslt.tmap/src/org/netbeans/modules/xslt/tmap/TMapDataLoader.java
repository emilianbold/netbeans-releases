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
package org.netbeans.modules.xslt.tmap;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapDataLoader extends UniFileLoader {
    
    public static final String MIME_TYPE = "text/x-tmap+xml";// NOI18N
    public static final String TRANSFORMMAP_XML = "transformmap.xml";// NOI18N
    public static final String LOADER_NAME = "LBL_loader_name"; // NOI18N
    public static final String ACTION_CONTEXT = "Loaders/" + 
            MIME_TYPE + "/Actions"; // NOI18N
    
    private static final long serialVersionUID = 1L;
    
    public TMapDataLoader() {
        super("org.netbeans.modules.xslt.tmap.TMapDataObject");
    }
    
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(TMapDataLoader.class, LOADER_NAME);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new TMapDataObject(primaryFile, this);
    }
    
    @Override
    protected FileObject findPrimaryFile( FileObject fo ) {
        FileObject primaryFo = null;
        String extension = fo.getNameExt();
        if (extension.equals(TRANSFORMMAP_XML)) 
        {
            // recognize xslt file only in context of XSLT project
            primaryFo = isXsltProjectContext(fo) ? fo : null;
        }
        return primaryFo;
    }    
    
    // TODO m
    private boolean isXsltProjectContext(FileObject fo) {
        boolean isXsltProject = false;
        
        Project project = FileOwnerQuery.getOwner(fo);
        FileObject source = project == null ? null : Util.getProjectSource(project);

        FileObject tMapFo = source == null 
                ? null 
                : source.getFileObject(TRANSFORMMAP_XML) ;
        
        isXsltProject = tMapFo != null && tMapFo.isValid(); 
        
        return isXsltProject;
    }
    
    
    @Override
    protected String actionsContext() {
        return ACTION_CONTEXT;
    }
    
}
