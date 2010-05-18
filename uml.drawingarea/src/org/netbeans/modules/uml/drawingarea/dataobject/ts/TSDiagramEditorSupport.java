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

import org.netbeans.modules.uml.drawingarea.dataobject.*;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.drawingarea.dataobject.ts.TSDiagramDataObject;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Jyothi
 */
public class TSDiagramEditorSupport extends DataEditorSupport 
        implements OpenCookie, EditorCookie.Observable, CloseCookie, PrintCookie {

    //private final CookieSet cookies;
    private TSDiagramDataObject diagramDataObject;

    public TSDiagramEditorSupport(TSDiagramDataObject obj) {
        super(obj, new TSDiagramEditorEnv(obj));
        setMIMEType("text/xml"); // NOI18N
        this.diagramDataObject = obj;
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
    
    private static class TSDiagramEditorEnv extends DataEditorSupport.Env {

//            private static final long serialVersionUID = 1L;
        public TSDiagramEditorEnv(DataObject obj) {
            super(obj);
        }

        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport) getDataObject().getCookie(TSDiagramEditorSupport.class);
//            return (CloneableOpenSupport) getDataObject().getCookie(EditorCookie.class);
        }

        protected org.openide.filesystems.FileLock takeLock() throws IOException {
            //throw new IOException("Read Only"); // I18N
            return ((UMLDiagramDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }
    }
}
