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
