/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.drawingarea.dataobject.ts;

import java.io.IOException;
import org.netbeans.modules.uml.drawingarea.dataobject.UMLDiagramEditorSupport;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbPreferences;

public class TSDiagramDataObject extends MultiDataObject
{

    /** The entries for diagram data files .etld and .etlp */
    FileObject etldfo;
    FileObject etlpfo;
    transient private TSDiagramEditorSupport diagramEditor;
    transient private OpenViewEdit openViewEdit;

    public TSDiagramDataObject(
        FileObject etldfo, FileObject etlpfo, TSDiagramDataLoader loader)
        throws DataObjectExistsException, IOException
    {
        super(etlpfo, loader);
        this.etlpfo = etlpfo;
        this.etldfo = etldfo;
        registerEntry(etldfo);

        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected Node createNodeDelegate()
    {
        return new TSDiagramDataNode(this, getLookup());
    }

    @Override
    public Lookup getLookup()
    {
        return getCookieSet().getLookup();
    }

    public EditorCookie createEditorCookie()
    {
        return new TSDiagramEditorSupport(this);
    }

//    public void addSaveCookie(GraphScene scene)
//    {
//        Cookie saveCookie = getCookie(SaveDiagram.class);
//        if (saveCookie == null)
//        {
//            CookieSet cookies = getCookieSet();
//            cookies.add(new SaveDiagram(scene, diagFO));
//        }
//    }

//    public void removeSaveCookie()
//    {
//        Cookie saveCookie = getCookie(SaveDiagram.class);
//        if (saveCookie != null)
//        {
//            CookieSet cookies = getCookieSet();
//            cookies.remove(saveCookie);
//        }
//    }

//    public void setDirty(boolean modified, GraphScene scene)
//    {
//        if (isModified() == modified)
//        {
//            return;
//        }
//
//        setModified(modified);
//        if (modified)
//        {
//            addSaveCookie(scene);
//        }
//        else
//        {
//            removeSaveCookie();
//        }
//    }

    public FileObject getDiagramFile()
    {
        return etldfo; // TODO
    }

    @Override
    public <T extends Cookie> T getCookie(Class<T> type)
    {
        T retValue;

        if (OpenCookie.class.equals(type) || ViewCookie.class.equals(type) ||
            EditCookie.class.equals(type))
        {
            if (openViewEdit == null)
            {
                openViewEdit = new OpenViewEdit();
            }
            retValue = type.cast(openViewEdit);
        }
        else
        {
            if (type.isAssignableFrom(UMLDiagramEditorSupport.class))
            {
                retValue = (T) getDiagramEditorSupport();
            }
            else
            {
                retValue = super.getCookie(type);
            }
        }
        return retValue;
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
            if (NbPreferences.forModule(DummyCorePreference.class)
                .getBoolean("UML_Open_Project_Diagrams", true))
            {
                open();
            }

        }
    }

    public boolean isReadOnly()
    {
        // TODO
        return true; 
        // return !etldfo.canWrite();
    }

    public synchronized TSDiagramEditorSupport getDiagramEditorSupport()
    {
        if (diagramEditor == null)
        {
            // TODO
            diagramEditor = new TSDiagramEditorSupport(this);
        }
        return diagramEditor;
    }

    public TSDiagramEditorSupport getDiagramEditor()
    {
        return getDiagramEditorSupport();
    }
}
