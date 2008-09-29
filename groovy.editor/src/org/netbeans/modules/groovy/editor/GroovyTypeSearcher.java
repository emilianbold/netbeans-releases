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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.editor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.elements.IndexedElement;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.IndexSearcher;
import org.netbeans.modules.gsf.api.IndexSearcher.Descriptor;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GroovyTypeSearcher implements IndexSearcher {

    public Set<? extends Descriptor> getTypes(Index gsfIndex, String textForQuery, NameKind kind, EnumSet<SearchScope> scope, Helper helper) {
        GroovyIndex index = new GroovyIndex(gsfIndex);

        kind = adjustKind(kind, textForQuery);
        
        if (kind == NameKind.CASE_INSENSITIVE_PREFIX /*|| kind == NameKind.CASE_INSENSITIVE_REGEXP*/) {
            textForQuery = textForQuery.toLowerCase(Locale.ENGLISH);
        }
        
        Set<IndexedClass> classes = null;
        if (textForQuery.length() > 0) {
            classes = index.getClasses(textForQuery, kind, true, false, false, scope, null);
        }
        
        Set<GroovyTypeDescriptor> result = new HashSet<GroovyTypeDescriptor>();
        
        for (IndexedClass cls : classes) {
            result.add(new GroovyTypeDescriptor(cls, helper));
        }
        
        return result;
    }

    private static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            char c = text.charAt(i);
            if (!Character.isUpperCase(c) && c != ':' ) {
                return false;
            }
        }
        
        return true;
    }

    private static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\:|\\.|\\$)*){2,}"); // NOI18N
    
    private static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }

    private NameKind cachedKind;
    private String cachedString = "/";

    private NameKind adjustKind(NameKind kind, String text) {
        if (text.equals(cachedString)) {
            return cachedKind;
        }
        if (kind == NameKind.CASE_INSENSITIVE_PREFIX) {
            if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
                kind = NameKind.CAMEL_CASE;            
            }
        }

        cachedString = text;
        cachedKind = kind;
        return kind;
    }

    public Set<? extends Descriptor> getSymbols(Index gsfIndex, String textForQuery, NameKind kind, EnumSet<SearchScope> scope, Helper helper) {
        // TODO - search for methods too!!

        // For now, just at a minimum do the types
        return getTypes(gsfIndex, textForQuery, kind, scope, helper);
    }
    
    private class GroovyTypeDescriptor extends Descriptor {
        private final IndexedElement element;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private boolean isLibrary;
        
        public GroovyTypeDescriptor(IndexedElement element, Helper helper) {
            this.element = element;
            this.helper = helper;
        }

        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            //if (isLibrary) {
            //    return new ImageIcon(org.openide.util.Utilities.loadImage(Js_KEYWORD));
            //}
            return helper.getIcon(element);
        }

        public String getTypeName() {
            return element.getName();
        }

        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                // Findbugs-Removed: File f = FileUtil.toFile(fo);
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {

                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();

                }
            } else {
                isLibrary = true;
                Logger.getLogger(GroovyTypeSearcher.class.getName()).fine("No fileobject for " + element.toString() + " with fileurl=" + element.getFileUrl());
            }
            if (projectName == null) {
                projectName = "";
            }
        }
        
        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            if (isLibrary) {
                return new ImageIcon(ImageUtilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
            }
            return projectIcon;
        }

        public FileObject getFileObject() {
            return element.getFileObject();
        }

        public void open() {
            ASTNode node = AstUtilities.getForeignNode(element);
            
            if (node != null) {
                // TODO - embedding context?
                try {
                    int offset = AstUtilities.getRange(node, (BaseDocument) element.getDocument()).getStart();
                    GsfUtilities.open(element.getFileObject(), offset, element.getName());
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                return;
            }
            
            FileObject fileObject = element.getFileObject();
            if (fileObject == null) {
                NotifyDescriptor nd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(GroovyTypeSearcher.class, "FileDeleted"), 
                    NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                // TODO: Try to remove the item from the index? Can't fix yet because the url is wiped
                // out by getFileObject (to avoid checking file existence multiple times; use a boolean
                // flag for that instead)
                
                return;
            }
            
            helper.open(fileObject, element);
        }

        public String getContextName() {
            // XXX This is lame - move formatting logic to the goto action!
            StringBuilder sb = new StringBuilder();
//            String require = element.getRequire();
//            String fqn = element.getFqn();
            String fqn = element.getIn() != null ? element.getIn() + "." + element.getName() : element.getName();
            if (element.getName().equals(fqn)) {
                fqn = null;
            }
            if (fqn != null/* || require != null*/) {
                if (fqn != null) {
                    sb.append(fqn);
                }
//                if (require != null) {
//                    if (fqn != null) {
//                        sb.append(" ");
//                    }
//                    sb.append("in ");
//                    sb.append(require);
//                    sb.append(".rb");
//                }
                return sb.toString();
            } else {
                return null;
            }
        }

        public ElementHandle getElement() {
            return element;
        }

        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSimpleName() {
            return element.getName();
        }

        public String getOuterName() {
            return null;
        }

    }

}
