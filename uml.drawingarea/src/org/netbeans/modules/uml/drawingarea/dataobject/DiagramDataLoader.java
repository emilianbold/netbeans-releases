/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.drawingarea.dataobject;


import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class DiagramDataLoader extends MultiFileLoader
{
    /** The standard extensions of the recognized files */
    public static final String ETLD_EXTENSION = "etld"; // NOI18N
    public static final String ETLP_EXTENSION = "etlp"; // NOI18N
    
    static final long serialVersionUID =1L;
    /** Constructs a new DiagramDataLoader */
    public DiagramDataLoader()
    {
        super("org.netbeans.modules.uml.drawingarea.dataobject.DiagramDataLoader"); // NOI18N
    }
    
    
    /** Gets default display name. Overides superclass method. */
    @Override
    protected String defaultDisplayName()
    {
        return NbBundle.getBundle(DiagramDataLoader.class)
                .getString("PROP_DiagramLoader_Name"); // NOI18N
    }
    
    @Override
    protected String actionsContext()
    {
        return "Loaders/text/xml-mime/Actions/"; // NOI18N
    }
    
    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo)
    {
        // never recognize folders.
        if (fo.isFolder()) return null;
        String ext = fo.getExt();
        if (ext.equals(ETLD_EXTENSION))
            return FileUtil.findBrother(fo, ETLD_EXTENSION);
        
        FileObject etlpFile = findDiagramPrimaryFile(fo);
        return etlpFile != null
                && FileUtil.findBrother(etlpFile, ETLD_EXTENSION) != null ? etlpFile : null;
    }
    
    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException
    {
        return new DiagramDataObject(FileUtil.findBrother(primaryFile, ETLD_EXTENSION),
                primaryFile, this);
    }

    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
            FileObject secondaryFile)
    {
        assert ETLD_EXTENSION.equals(secondaryFile.getExt());
        
        FileEntry diagramEntry = new FileEntry(obj, secondaryFile);
//        ((DiagramDataObject)obj).etldEntry = diagramEntry;
        return diagramEntry;
    }
    
    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile)
    {
        return new FileEntry(obj, primaryFile);
    }
    
    private FileObject findDiagramPrimaryFile(FileObject fo)
    {
        if (fo.getExt().equals(ETLP_EXTENSION))
            return fo;
        return null;
    }
    
    
}
