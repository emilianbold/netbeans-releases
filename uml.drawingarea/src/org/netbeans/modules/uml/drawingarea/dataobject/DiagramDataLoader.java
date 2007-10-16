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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        super("org.netbeans.modules.uml.drawingarea.dataobject.DiagramDataObject"); // NOI18N
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
