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

package org.netbeans.modules.uml.project;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.documentation.ui.DocumentationTopComponnet;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Sheryl
 */
public class UMLProjectDataObject extends MultiDataObject
{
//    static final long serialVersionUID = 1L;
    
    // file extension for uml project xml file
    public static final String EXT = "etd";  //NOI18N
    public static final String ICON = ImageUtil.IMAGE_FOLDER + "model-root-node.png"; // NOI18N
    private EditorCookie editor = null;
    
    private FileObject fo = null;
    
    /** Creates a new instance of UMLProjectDataObject */
    public UMLProjectDataObject(FileObject fo, MultiFileLoader loader)
    throws DataObjectExistsException
    {
        super(fo, loader);
        this.fo = fo;
        
        CookieSet.Factory factory = new CookieSet.Factory()
        {
            public Node.Cookie createCookie(Class klass)
            {
                if (klass.isAssignableFrom(EditorCookie.class)
                || klass.isAssignableFrom(OpenCookie.class)
                || klass.isAssignableFrom(CloseCookie.class)
                || klass.isAssignableFrom(PrintCookie.class) )
                {
                    if (editor == null) editor = createEditorCookie(); 
                    if (editor == null) return null;                    
                    
                    return klass.isAssignableFrom(editor.getClass()) ? editor : null;
                }
                else
                {
                    return null;
                }
            }
        };
        
        CookieSet cookies = getCookieSet();
        // EditorCookie.class must be synchronized with
        // UMLEditor.Env->findCloneableOpenSupport
        cookies.add(EditorCookie.class, factory);
        cookies.add(OpenCookie.class, factory);
        cookies.add(CloseCookie.class, factory);
        cookies.add(PrintCookie.class, factory);
    }
    
    private FileObject getFileObject()
    {
        return fo;
    }
    
    protected EditorCookie createEditorCookie()
    {
        return new UMLEditorSupport(this);
    }
    
    protected final void addSaveCookie()
    {
        getCookieSet().add(new Save());
    }
    
    private final void removeSaveCookie(SaveCookie save)
    {
        getCookieSet().remove(save);
    }
    
    private String getDisplayName()
    {
        Project proj = FileOwnerQuery.getOwner(getFileObject());
        UMLProjectHelper helper = null;
        if ( proj != null )
        {
            Lookup lkup = proj.getLookup();
            helper = (lkup != null  ? lkup.lookup(UMLProjectHelper.class) : null);
        }
        
        if (helper != null)
            return helper.getDisplayName();
// conover - removing the word "Model" in the Save dialog
//            return helper.getDisplayName() + " " + NbBundle.getMessage(
//                UMLModelRootNode.class, "CTL_UMLModelRootNode"); // NOI18N
        
        return getName();
    }
    
    protected Node createNodeDelegate()
    {
        DataNode node = null;
        
//        Project currentProj = FileOwnerQuery.getOwner(getFileObject());
//        UMLPhysicalViewProvider view = (UMLPhysicalViewProvider)currentProj.
//                getLookup().lookup(UMLPhysicalViewProvider.class);
//        
//         Node dnode = view.getModelRootNode();
//        
//        if (dnode != null)
//            return dnode;
        
        node = new DataNode(this, org.openide.nodes.Children.LEAF);
        // netbeans.core.nodes.description
        node.setShortDescription(NbBundle.getMessage(
                UMLProjectDataObject.class, "HINT_UMLProjectDataObject")); // NOI18N
        node.setIconBaseWithExtension(ICON);
        node.setDisplayName(getDisplayName());
        return node;
    }
    
    
    public boolean isMoveAllowed()
    {
        return false;
    }
    
    /* Getter for rename action.
     * @return true if the object can be renamed
     */
    public boolean isRenameAllowed()
    {
        return false;
    }
    
    
    private static class UMLEditorSupport extends DataEditorSupport 
            implements OpenCookie, EditorCookie.Observable, PrintCookie, CloseCookie
    {
        public UMLEditorSupport(UMLProjectDataObject obj)
        {
            super(obj, new UMLEditorEnv(obj));
            setMIMEType("text/xml"); // NOI18N
        }
        
    
        private static class UMLEditorEnv extends DataEditorSupport.Env
        {
//            private static final long serialVersionUID = 1L;
            
            public UMLEditorEnv(DataObject obj)
            {
                super(obj);
            }
            
            protected FileObject getFile()
            {
                return getDataObject().getPrimaryFile();
            }
            
            /* make uml project data file read only in editor to prevent users
             * from tampering it accidentally
             */
            protected FileLock takeLock() throws IOException
            {
                throw new IOException("Read Only"); // I18N
            }
            
            public CloneableOpenSupport findCloneableOpenSupport()
            {
                return (CloneableOpenSupport) getDataObject().getCookie(EditorCookie.class);
            }
        }
    }
    
    
    private class Save implements SaveCookie
    {
        public void save() throws IOException
        {
            //custom logic to save uml project files
            Project currentProj = FileOwnerQuery.getOwner(getFileObject());
            if (currentProj != null && currentProj.getLookup() != null) 
            {
                UMLProjectHelper helper = (UMLProjectHelper)currentProj.getLookup().
                    lookup(UMLProjectHelper.class);
                if (helper!=null)
                {
                    // save modified documentation in the edit pane
                    DocumentationTopComponnet.saveDocumentation();
                    
                    helper.saveProject();
                    SaveCookie save = (SaveCookie) UMLProjectDataObject.this.
                        getCookie(SaveCookie.class);
                    if (save!=null)
                        UMLProjectDataObject.this.removeSaveCookie(save);
                    setModified(false);
                }
            }
        }
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ private Loader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /** The DataLoader for UMLProjectDataObjects.
     */
    static final class Loader extends MultiFileLoader
    {
        static final long serialVersionUID =1L;
        /** Creates a new XMLDataLoader */
        public Loader()
        {
            super("org.netbeans.modules.uml.project.UMLProjectDataObject"); // NOI18N
        }
        
        protected String actionsContext()
        {
            return "Loaders/text/xml/Actions"; // NOI18N
        }
        
        /** Get the default display name of this loader.
         * @return default display name
         */
        protected String defaultDisplayName()
        {
            return NbBundle.getMessage(UMLProjectDataObject.class, "PROP_UMLProjectDataLoader_Name");
        }
        
        /** For a given file finds a primary file.
         * @param fo the file to find primary file for
         *
         * @return the primary file for the file or null if the file is not
         *   recognized by this loader
         */
        protected FileObject findPrimaryFile(FileObject fo)
        {
            String ext = fo.getExt();
            if (ext.equals(EXT))
            { 
                return fo;
            }
            // not recognized
            return null;
        }
        
        /** Creates the right data object for given primary file.
         * It is guaranteed that the provided file is realy primary file
         * returned from the method findPrimaryFile.
         *
         * @param primaryFile the primary file
         * @return the data object for this file
         * @exception DataObjectExistsException if the primary file already has data object
         */
        protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException
        {
            return new UMLProjectDataObject(primaryFile, this);
        }
        
        /** Creates the right primary entry for given primary file.
         *
         * @param primaryFile primary file recognized by this loader
         * @return primary entry for that file
         */
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile)
        {
            return new FileEntry(obj, primaryFile);
        }
        
        /** Creates right secondary entry for given file. The file is said to
         * belong to an object created by this loader.
         *
         * @param secondaryFile secondary file for which we want to create entry
         * @return the entry
         */
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile)
        {
            // uml project defines two project files, *.etd as the primary (data file) and *.ettm as the 
            // secondary (schema), but the secondary file is not used for save cookie purpose,
            // in fact, the logic to update all relevant files is captured in save()
            return new FileEntry(obj, secondaryFile);
        }
    }
}
