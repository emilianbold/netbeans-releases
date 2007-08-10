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


package org.netbeans.modules.bpel.core;

import java.io.IOException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
/**
 *
 * @author ads
 */
public class BPELDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 1L;

    public static final String MIME_TYPE = "text/x-bpel+xml";       // NOI18N

    public static final String ACTION_CONTEXT = "Loaders/" +        // NOI18N
        MIME_TYPE + "/Actions";                                     // NOI18N

    public static final String PRIMARY_EXTENSION = "bpel";          // NOI18N
    static final String SECONDARY_EXTENSION = "vbpel";              // NOI18N
    
    static final String LOADER_NAME ="LBL_loader_name";             // NOI18N 
    
    /**
     * Creates a new instance of BPELDataLoader
     */
    public BPELDataLoader() {
        super("org.netbeans.modules.bpel.core.BPELDataObject");
    }
    
    /** 
     * Does initialization. Initializes display name,
     * extension list and the actions. 
     */
    protected void initialize() {
        super.initialize();
		getExtensions().addMimeType(BPELDataLoader.MIME_TYPE);
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(BPELDataLoader.class, LOADER_NAME ); 
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
        return new BPELDataObject(primaryFile, this);
    }
    
    protected FileObject findPrimaryFile( FileObject fo ) {
        FileObject result = null;
        try {
            if (!fo.getFileSystem().isDefault()) {
                String extension = fo.getExt();
                if (extension.equals(PRIMARY_EXTENSION)) {
                    result = fo;
                }
                else if (extension.equals(SECONDARY_EXTENSION)) {
                    // Delete this later if we do not want to support .vbpel
                    result = FileUtil.findBrother(fo, PRIMARY_EXTENSION);
                }
            }
        }
        catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault().notify(fsie);
        }

        return result;
    }
    
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, 
            FileObject primaryFile) 
    {
        return new FileEntry(obj, primaryFile);
    }
    
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, 
            FileObject secondaryFile) 
    {       
        return new FileEntry(obj, secondaryFile);
    }
	
    /**
     * other modules can decorate with Special Actions
     * to see the default actions look in the layer.xml
     */
    @Override protected String actionsContext() {
            return ACTION_CONTEXT;
    }

}
