/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.jrubyparser.ast.Node;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.IndexSearcher.Descriptor;
import org.netbeans.modules.csl.api.IndexSearcher.Helper;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @todo Figure out why "base" searches gives me lower-case matches on "base" (which seems invalid)
 * 
 * @author Tor Norbye
 */
public class RubyTypeSearcher implements IndexSearcher {
    
    public RubyTypeSearcher() {
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
    
    private QuerySupport.Kind cachedKind;
    private String cachedString = "/";
    
    private QuerySupport.Kind adjustKind(QuerySupport.Kind kind, String text) {
        if (text.equals(cachedString)) {
            return cachedKind;
        }
        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX) {
            if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
                kind = QuerySupport.Kind.CAMEL_CASE;            
            }
        }

        cachedString = text;
        cachedKind = kind;
        return kind;
    }

    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind kind, Helper helper) {
        // In addition to just computing the declared types here, we perform some additional
        // "second guessing" of the query. In particular, we want to allow double colons
        // to be part of the query names (to specify full module names), but since colon is
        // treated by the Open Type dialog as a regexp char, it will turn it into a regexp query
        // for example. Thus, I look at the query string and I might turn it into a different kind
        // of query. (We also allow #method suffixes which are handled here.)


        if (textForQuery.endsWith("::")) {
            textForQuery = textForQuery.substring(0, textForQuery.length() - 2);
        } else if (textForQuery.endsWith(":")) {
            textForQuery = textForQuery.substring(0, textForQuery.length()-1);
        }
        
        RubyIndex index = RubyIndex.get(QuerySupport.findRoots(project,
                Collections.singleton(RubyLanguage.SOURCE),
                Collections.singleton(RubyLanguage.BOOT),
                Collections.<String>emptySet()));
        if (index == null) {
            return Collections.emptySet();
        }

        kind = adjustKind(kind, textForQuery);
        
        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX /*|| kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP*/) {
            textForQuery = textForQuery.toLowerCase();
        }
        
        String method = null;
        int methodIndex = textForQuery.indexOf('#');
        if (methodIndex != -1) {
            method = textForQuery.substring(methodIndex+1);
            textForQuery = textForQuery.substring(0, methodIndex);
        }
        
        Set<IndexedClass> classes = null;
        if (method == null || textForQuery.length() > 0) {
            classes = index.getClasses(textForQuery, kind, true, false, false, null);
        }
        
        Set<RubyTypeDescriptor> result = new HashSet<RubyTypeDescriptor>();
        
        if (method != null) {
            // Query methods
            Set<IndexedMethod> methods = index.getMethods(method, kind);
            for (IndexedMethod m : methods) {
                if (textForQuery.length() > 0 && m.getClz() != null) {
                    String in = m.getClz();
                    switch (kind) {
                    case CASE_INSENSITIVE_REGEXP:
                    case REGEXP:
                        try {
                            if (in.indexOf("::") != -1 && textForQuery.indexOf("::") == -1) { // NOI18N
                                // Try matching each
                                boolean matches = false;
                                for (String c : in.split("::")) { // NOI18N
                                    if (c.matches(textForQuery)) {
                                        matches = true;
                                        break;
                                    }
                                }
                                
                                if (!matches) {
                                    continue;
                                }
                            } else if (!in.matches(textForQuery)) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Silently ignore errors in regexps since they can come from the user
                        }
                        break;
                    case CASE_INSENSITIVE_PREFIX:
                        if (!in.regionMatches(true, 0, textForQuery, 0, textForQuery.length())) {
                            continue;
                        }
                        break;
                    case PREFIX:
                        if (!in.regionMatches(false, 0, textForQuery, 0, textForQuery.length())) {
                            continue;
                        }
                        break;
                    case EXACT:
                        if (!in.equalsIgnoreCase(textForQuery)) {
                            continue;
                        }
                    }
                }
                result.add(new RubyTypeDescriptor(m, helper));
            }
        } else {
            for (IndexedClass cls : classes) {
                result.add(new RubyTypeDescriptor(cls, helper));
            }
        }
        
        return result;
    }


    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind kind, Helper helper) {
        if (textForQuery.indexOf("::") != -1 || textForQuery.indexOf("#") != -1) {
            return getTypes(project, textForQuery, kind, helper);
        }

        RubyIndex index = RubyIndex.get(QuerySupport.findRoots(project,
                Collections.singleton(RubyLanguage.SOURCE),
                Collections.singleton(RubyLanguage.BOOT),
                Collections.<String>emptySet()));
        if (index == null) {
            return Collections.emptySet();
        }

        kind = adjustKind(kind, textForQuery);

        if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX /*|| kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP*/) {
            textForQuery = textForQuery.toLowerCase();
        }

        if (textForQuery.length() > 0) {
            Set<RubyTypeDescriptor> result = new HashSet<RubyTypeDescriptor>();
            Set<IndexedClass> classes = index.getClasses(textForQuery, kind, true, false, false, null);

            Set<IndexedMethod> methods = index.getMethods(textForQuery, kind);
            for (IndexedClass cls : classes) {
                result.add(new RubyTypeDescriptor(cls, helper));
            }
            for (IndexedMethod mtd : methods) {
                result.add(new RubyTypeDescriptor(mtd, helper));
            }
            
            return result;
        }

        return null;
    }
    
    private class RubyTypeDescriptor extends Descriptor {
        private final IndexedElement element;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private boolean isLibrary;
        private static final String RUBY_KEYWORD = "org/netbeans/modules/ruby/jruby.png"; //NOI18N
        
        public RubyTypeDescriptor(IndexedElement element, Helper helper) {
            this.element = element;
            this.helper = helper;
        }

        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            //if (isLibrary) {
            //    return new ImageIcon(org.openide.util.ImageUtilities.loadImage(RUBY_KEYWORD));
            //}
            //return helper.getIcon(element);
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
                File f = FileUtil.toFile(fo);
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    RubyPlatform platform = RubyPlatform.platformFor(p);
                    if (platform != null) {
                        String lib = platform.getLibDir();
                        if (lib != null && f.getPath().startsWith(lib)) {
                            projectName = "Ruby Library";
                            isLibrary = true;
                        }
                    } else {
                        // TODO - check to see if we're a library - and if so include the language icon
                        
                        ProjectInformation pi = ProjectUtils.getInformation(p);
                        projectName = pi.getDisplayName();
                        projectIcon = pi.getIcon();
                    }
                }
            } else {
                isLibrary = true;
                Logger.getLogger(RubyTypeSearcher.class.getName()).fine("No fileobject for " + element.toString() + " with fileurl=" + element.getFileUrl());
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
                return ImageUtilities.loadImageIcon(RUBY_KEYWORD, false);
            }
            return projectIcon;
        }

        public FileObject getFileObject() {
            return element.getFileObject();
        }

        public void open() {
            Node node = AstUtilities.getForeignNode(element);
            
            if (node != null) {
                GsfUtilities.open(element.getFileObject(), node.getPosition().getStartOffset(), element.getName());
                return;
            }
            
            FileObject fileObject = element.getFileObject();
            if (fileObject == null) {
                NotifyDescriptor nd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(RubyTypeSearcher.class, "FileDeleted"), 
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
            String require = element.getRequire();
            String fqn = element.getFqn();
            if (element.getName().equals(fqn)) {
                fqn = null;
            }
            if (fqn != null || require != null) {
                if (fqn != null) {
                    sb.append(fqn);
                }
                if (require != null) {
                    if (fqn != null) {
                        sb.append(" ");
                    }
                    sb.append("in ");
                    sb.append(require);
                    sb.append(".rb");
                }
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
