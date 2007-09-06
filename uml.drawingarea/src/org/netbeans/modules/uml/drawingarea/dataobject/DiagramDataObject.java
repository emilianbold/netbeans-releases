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

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.cookies.EditCookie;
import org.openide.cookies.ViewCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.NbPreferences;


/**
 *
 * @author Sheryl
 */
public class DiagramDataObject extends MultiDataObject
{
    transient private DiagramEditorSupport diagramEditor;
    transient private OpenViewEdit openViewEdit;
    
    /** The entries for diagram data files .etld and .etlp */
    FileObject etldfo;
    FileObject etlpfo;
    
    static final long serialVersionUID =1L;
    
    public DiagramDataObject(FileObject etldfo, FileObject etlpfo, DiagramDataLoader loader)
            throws DataObjectExistsException
    {
        super(etlpfo, loader);
        this.etlpfo = etlpfo;
        this.etldfo = etldfo;
        registerEntry(etldfo);
    }


    public void addSaveCookie()
    {
        CookieSet cookies = getCookieSet();
        cookies.add(new Save());
    }
    
    public void removeSaveCookie()
    {
        Cookie cookie = getCookie(SaveCookie.class);
        if (cookie != null)
        {
            CookieSet cookies = getCookieSet();
            cookies.remove(cookie);
        }
    }
 
    @Override
    public <T extends Cookie> T getCookie(Class<T> type)
    {
        T retValue;
        
        if (OpenCookie.class.equals(type) || ViewCookie.class.equals(type) ||
                EditCookie.class.equals(type))
        {
            if (openViewEdit == null)
                openViewEdit = new OpenViewEdit();
            retValue = type.cast(openViewEdit);
        }
        else if (type.isAssignableFrom(DiagramEditorSupport.class))
        {
            retValue = (T) getDiagramEditorSupport();
        }
        else
        {
            retValue = super.getCookie(type);
        }
        return retValue;
    }
    
    
    private class Save implements SaveCookie
    {
        public void save() throws IOException
        {
            ICoreProduct coreProduct = ProductRetriever.retrieveProduct();
            IDiagram dia = null;
            IProduct product = null;
            
            if (coreProduct instanceof IProduct)
            {
                product = (IProduct)coreProduct;
                ETList<IProxyDiagram> diagrams = product.getDiagramManager().getOpenDiagrams();
                for (IProxyDiagram diagram: diagrams)
                {
                    File f = new File(diagram.getFilename());
                    
                    if (etldfo != null && f.equals(FileUtil.toFile(etldfo)) ||
                        etlpfo != null && f.equals(FileUtil.toFile(etlpfo)))
                        diagram.getDiagram().save();
                }  
            }
            
            removeSaveCookie();
            setModified(false);
        }
    }
    
    private class OpenViewEdit implements OpenCookie, ViewCookie, EditCookie
    {
        public void open()
        {
            // open Diagram editor
            getDiagramEditorSupport().openDiagramEditor();
        }
        public void view()
        {
            // open text editor
            getDiagramEditorSupport().open();
        }
        public void edit()
        {
            //open previously opened diagrams on project loading
            //kris richards - changed to NbPreference
            if (NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_Open_Project_Diagrams", true))
                open() ;
            
        }
    }
    
    public FileObject getDiagramFile()
    {
        return etldfo;
    }
    
    public boolean isReadOnly()
    {
        return !etlpfo.canWrite() || !etldfo.canWrite();
    }

    
    public synchronized DiagramEditorSupport getDiagramEditorSupport()
    {
        if (diagramEditor == null)
        {
            diagramEditor = new DiagramEditorSupport(getPrimaryEntry(), this, getCookieSet());
        }
        return diagramEditor;
    }
    
    
    public DiagramEditorSupport getDiagramEditor()
    {
        return getDiagramEditorSupport();
    }
    

    protected Node createNodeDelegate()
    {
        return new DiagramDataNode(this);
    }
}
