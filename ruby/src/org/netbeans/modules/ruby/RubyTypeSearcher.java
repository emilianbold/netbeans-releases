/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.ruby;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.Index.SearchScope;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.TypeSearcher;
import org.netbeans.api.gsf.TypeSearcher.GsfTypeDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @todo Figure out why "base" searches gives me lower-case matches on "base" (which seems invalid)
 * 
 * @author Tor Norbye
 */
public class RubyTypeSearcher implements TypeSearcher {
    public RubyTypeSearcher() {
    }
    
    //public Set<ElementHandle<Element>> getDeclaredTypes(Index gsfIndex,
    //public Set<? extends Element> getDeclaredTypes(Index gsfIndex,
    public Set<? extends GsfTypeDescriptor> getDeclaredTypes(Index gsfIndex,
                                                        String textForQuery,
                                                        NameKind kind,
                                                        EnumSet<SearchScope> scope, Helper helper) {
        RubyIndex index = RubyIndex.get(gsfIndex);
        if (index == null) {
            return Collections.emptySet();
        }
        
        if (kind == NameKind.CASE_INSENSITIVE_PREFIX || kind == NameKind.CASE_INSENSITIVE_REGEXP) {
            textForQuery = textForQuery.toLowerCase();
        }
        
        Set<IndexedClass> classes = index.getClasses(textForQuery, kind, true, false, false, scope);
        //return classes;
        
        Set<RubyTypeDescriptor> result = new HashSet<RubyTypeDescriptor>();
        for (IndexedClass cls : classes) {
            result.add(new RubyTypeDescriptor(cls, helper));
        }
        
        return result;
    }
    
    private class RubyTypeDescriptor extends GsfTypeDescriptor {
        private final IndexedClass cls;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private boolean isLibrary;
        private static final String RUBY_KEYWORD = "org/netbeans/modules/ruby/jruby.png"; //NOI18N
        
        public RubyTypeDescriptor(IndexedClass cls, Helper helper) {
            this.cls = cls;
            this.helper = helper;
        }

        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            if (isLibrary) {
                return new ImageIcon(org.openide.util.Utilities.loadImage(RUBY_KEYWORD));
            }
            return helper.getIcon(cls);
        }

        public String getTypeName() {
            return cls.getName();
        }

        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = cls.getFileObject();
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                String lib = RubyInstallation.getInstance().getRubyLib();
                if (lib != null && f.getPath().startsWith(lib)) {
                    projectName = "Ruby Library";
                    isLibrary = true;
                } else {
                    Project p = FileOwnerQuery.getOwner(fo);                    
                    if (p != null) {
                        ProjectInformation pi = ProjectUtils.getInformation( p );
                        projectName = pi.getDisplayName();
                        projectIcon = pi.getIcon();
                    }
                }
            } else {
                isLibrary = true;
                Logger.getLogger(RubyTypeSearcher.class.getName()).fine("No fileobject for " + cls.toString() + " with fileurl=" + cls.getFileUrl());
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
                return new ImageIcon(org.openide.util.Utilities.loadImage(RUBY_KEYWORD));
            }
            return projectIcon;
        }

        public FileObject getFileObject() {
            return cls.getFileObject();
        }

        public void open() {
            Node node = AstUtilities.getForeignNode(cls, null);
            
            if (node != null) {
                NbUtilities.open(cls.getFileObject(), node.getPosition().getStartOffset(), cls.getName());
                return;
            }

            helper.open(cls.getFileObject(), cls);
        }

        public String getContextName() {
            // XXX This is lame - move formatting logic to the goto action!
            StringBuilder sb = new StringBuilder();
            String require = cls.getRequire();
            String fqn = cls.getFqn();
            if (cls.getName().equals(fqn)) {
                fqn = null;
            }
            if (fqn != null || require != null) {
                sb.append(" (");
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
                sb.append(")");
                return sb.toString();
            } else {
                return null;
            }
        }

        public Element getElement() {
            return cls;
        }

        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSimpleName() {
            return cls.getName();
        }

        public String getOuterName() {
            return null;
        }

    }

}
