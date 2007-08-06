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


import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Sheryl
 */
public class DiagramEditorSupport extends DataEditorSupport
        implements EditorCookie.Observable, CloseCookie, PrintCookie
{
    private final CookieSet cookies;
    private DiagramDataObject diagramDataObject;
    
    /** Creates a new instance of DiagramEditorSupport */
    public DiagramEditorSupport(MultiDataObject.Entry javaEntry,
            DiagramDataObject diagramDataObject,
            CookieSet cookies)
    {
        super(diagramDataObject, new Environment(diagramDataObject));
        setMIMEType("text/xml"); // NOI18N
        this.diagramDataObject = diagramDataObject;
        this.cookies = cookies;
    }
    
    public void openDiagramEditor()
    {
        try 
        {
            FileObject fo = diagramDataObject.getDiagramFile();
            Project p = FileOwnerQuery.getOwner(fo);
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            //Fixed IZ=104742. 
            // Modified to call FileUtil.toFile(fo).getCanonicalPath() instead of 
            // calling fo.getPath()
            String diagFileName = FileUtil.toFile(fo).getCanonicalPath();
            if (Arrays.asList(projects).contains(p)) 
            {
                ProductHelper.getProductDiagramManager().openDiagram(diagFileName, false, null);
            }
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    
    private static final class Environment extends DataEditorSupport.Env
    {
        
        private static final long serialVersionUID = -1;
        
        public Environment(DataObject obj)
        {
            super(obj);
        }
        
        protected FileObject getFile()
        {
            return this.getDataObject().getPrimaryFile();
        }
        
        @Override
        protected FileLock takeLock() throws java.io.IOException
        {
            return ((DiagramDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }
        
        @Override
        public CloneableOpenSupport findCloneableOpenSupport()
        {
            return this.getDataObject().getCookie(DiagramEditorSupport.class);
        }
        
    }
}
