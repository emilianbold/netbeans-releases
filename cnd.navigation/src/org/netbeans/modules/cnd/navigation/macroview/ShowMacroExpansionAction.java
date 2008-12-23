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

package org.netbeans.modules.cnd.navigation.macroview;

import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.UserQuestionException;

public final class ShowMacroExpansionAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        EditorCookie c = activatedNodes[0].getCookie(EditorCookie.class);
        if (c != null) {
            Document doc = c.getDocument();
            if (doc != null) {
                MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
                if (!view.isOpened()) {
                    view.open();
                }
                Document doc2 = null;
                try {
                    FileObject root = FileUtil.createMemoryFileSystem().getRoot();
                    //.createData(CsmUtilities.getFile(doc).getName());
                    FileObject file = FileUtil.copyFile(FileUtil.createData(CsmUtilities.getFile(doc)), root, CsmUtilities.getFile(doc).getName());
//                    File f = File.createTempFile("temp", CsmUtilities.getFile(doc).getName()); // NOI18N
//                    FileObject file = FileUtil.createData(f);

                    DataObject dob = DataObject.find(file);
                    EditorCookie ec = dob.getCookie(EditorCookie.class);
                    doc2 = ec.openDocument();
                    try {
                        doc2 = ec.openDocument();
                    } catch (UserQuestionException ex) {
                        ex.confirmed();
                        doc2 = ec.openDocument();
                    }
                    //DataObject dob = DataObject.find(FileUtil.createData(CsmUtilities.getFile(doc)));
                    //doc2.putProperty(Document.StreamDescriptionProperty, NbEditorUtilities.getDataObject(doc));
                    //doc2.putProperty(Document.StreamDescriptionProperty, dob);

                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                doc2.putProperty(Document.TitleProperty, doc.getProperty(Document.TitleProperty));
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, true);
                if(csmFile != null) {
                    doc2.putProperty(CsmFile.class, csmFile);
                } else {
                    System.err.println("CsmFile" + CsmUtilities.getFile(doc).getName() + "is null"); // NOI18N
                }

//                CsmFile f = CsmUtilities.getCsmFile(doc2, true);
//
//                DataObject openedDob = NbEditorUtilities.getDataObject(doc2);
//                StyledDocument openedDoc = null;
//                EditorCookie ec = openedDob.getCookie(EditorCookie.class);
//                if (ec != null) {
//                    openedDoc = ec.getDocument();
//                }

//                DataObject openedDob2 = NbEditorUtilities.getDataObject(doc);
//                StyledDocument openedDoc2 = null;
//                EditorCookie ec2 = openedDob2.getCookie(EditorCookie.class);
//                if (ec != null) {
//                    openedDoc2 = ec2.getDocument();
//                }


//                try {
//                    doc2.insertString(0, doc.getText(0, doc.getLength()), null);
//                } catch (BadLocationException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                CsmDeclaration decl = ContextUtils.findDeclaration(activatedNodes[0]);
                String declName = null;
                if (decl != null) {
                    declName = decl.getName().toString();
                }
                if(declName != null) {
                    view.setDocument(doc2, decl.getName().toString());
                } else {
                    view.setDocument(doc2, ""); // NOI18N
                }
                view.requestActive();
            }
        }

//        CsmDeclaration decl = ContextUtils.findDeclaration(activatedNodes[0]);
//        if (decl != null){
//            MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
//            if (!view.isOpened()) {
//                view.open();
//            }
//            view.setDeclaration(decl.toString());
//            view.requestActive();
//        }
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            if (ContextUtils.USE_REFERENCE_RESOLVER) {
                CsmReference ref = ContextUtils.findReference(activatedNodes[0]);
                if (ref != null && CsmKindUtilities.isInclude(ref.getOwner())) {
                    return true;
                }
            }
            return ContextUtils.findFile(activatedNodes[0]) != null;
        }
        return false;
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowMacroExpansionAction"); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            EditorCookie.class
        };
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
}

