/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 * filter node for display of web sources
 * @author  Milos Kleint 
 */
class WebAppFilterNode extends FilterNode {
    private boolean isTopLevelNode = false;
    private FileObject file;
    
    WebAppFilterNode(Project proj, Node orig, File root) {
        this(proj, orig, root, true);
    }
    
    private WebAppFilterNode(Project proj, Node orig, File root, boolean isTopLevel) {
        //#142744 if orig child is leaf, put leave as well.
        super(orig, orig.getChildren() == Children.LEAF ? Children.LEAF : new WebAppFilterChildren(proj, orig, root));
        isTopLevelNode = isTopLevel;
        if (isTopLevel) {
            file = FileUtil.toFileObject(root);
        }
    }
    
    @Override
    public String getDisplayName() {
        if (isTopLevelNode) {
            String s = NbBundle.getMessage(WebAppFilterNode.class, "LBL_Web_Pages");
            
            try {
                s = file.getFileSystem().getStatus().annotateName(s, Collections.singleton(file));
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            return s;
        }
        return getOriginal().getDisplayName();
        
    }

    @Override
    public String getHtmlDisplayName() {
        if (!isTopLevelNode) {
            return getOriginal().getHtmlDisplayName();
        }
         try {
             FileSystem.Status stat = file.getFileSystem().getStatus();
             if (stat instanceof FileSystem.HtmlStatus) {
                 FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                String s = NbBundle.getMessage(WebAppFilterNode.class, "LBL_Web_Pages");
                 String result = hstat.annotateNameHtml (
                     s, Collections.singleton(file));

                 //Make sure the super string was really modified
                 if (!s.equals(result)) {
                     return result;
                 }
             }
         } catch (FileStateInvalidException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         }
         return super.getHtmlDisplayName();
    }    
    
    
    @Override
    public javax.swing.Action[] getActions(boolean param) {
//        if (isTopLevelNode) {
//            Action[] toReturn = new Action[1];
//            toReturn[0] = CommonProjectActions.newFileAction();
//            return toReturn;
//        } else {
            return super.getActions(param);
//        }
    }    

    @Override
    public java.awt.Image getIcon(int param) {
        java.awt.Image retValue = super.getIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage("org/netbeans/modules/maven/j2ee/web/webPagesBadge.png"), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }

    @Override
    public java.awt.Image getOpenedIcon(int param) {
        java.awt.Image retValue = super.getOpenedIcon(param);
        if (isTopLevelNode) {
            retValue = ImageUtilities.mergeImages(retValue,
                                             ImageUtilities.loadImage("org/netbeans/modules/maven/j2ee/web/webPagesBadge.png"), //NOI18N
                                             8, 8);
        } 
        return retValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.isTopLevelNode ? 1 : 0);
        hash = 67 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebAppFilterNode other = (WebAppFilterNode) obj;
        if (this.isTopLevelNode != other.isTopLevelNode) {
            return false;
        }
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    private static class WebAppFilterChildren extends FilterNode.Children {

        private File root;
        private Project project;

        private WebAppFilterChildren(Project proj, Node original, File rootpath) {
            super(original);
            root = rootpath;
            project = proj;
        }
        
        @Override
        protected Node[] createNodes(Node obj) {
            FileObject fobj = obj.getLookup().lookup(FileObject.class);
        
            if (fobj != null) {
                if (!VisibilityQuery.getDefault().isVisible(fobj)) {
                    return new Node[0];
                }
                Node n = new WebAppFilterNode(project, obj, root, false);
                return new Node[] {n};
            }
            Node origos = obj;
            return new Node[] { origos.cloneNode() };
        }        
    }    
}

