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

package org.netbeans.modules.gsfret.navigation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.openide.util.ImageUtilities;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
 * XXX Remove the ElementScanner class from here it should be wenough to
 * consult the Elements class. It should also permit for showing inherited members.
 *
 * @author phrebejk
 */
public class ElementScanningTask implements CancellableTask<CompilationInfo>{
    
    private ClassMemberPanelUI ui;
    private volatile boolean canceled;
    
    public ElementScanningTask( ClassMemberPanelUI ui ) {
        this.ui = ui;
    }
    
    public void cancel() {
        canceled = true;
//        if ( scanner != null ) {
//            scanner.cancel();
//        }
    }

    public void run(CompilationInfo info) throws Exception {
        
        canceled = false; // Task shared for one file needs reset first
        
        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;
        
        final List<StructureItem> items = new ArrayList<StructureItem>();
        StructureItem rootDescription = new StructureItem() {
            public String getName() {
                return null;
            }

            public String getHtml(HtmlFormatter formatter) {
                return null;
            }

            public ElementHandle getElementHandle() {
                throw new UnsupportedOperationException("Not supported on the Root Node.");
            }

            public ElementKind getKind() {
                return ElementKind.OTHER;
            }

            public Set<Modifier> getModifiers() {
                return Collections.emptySet();
            }

            public boolean isLeaf() {
                return false;
            }

            public List<? extends StructureItem> getNestedItems() {
                return items;
            }

            public long getPosition() {
                return 0;
            }
            public long getEndPosition() {
                return Long.MAX_VALUE;
            }

            public ImageIcon getCustomIcon() {
                return null;
            }
            
            public String getSortText() {
                return null;
            }
        };
        
        Set<String> mimeTypes = info.getEmbeddedMimeTypes();
        LanguageRegistry registry = LanguageRegistry.getInstance();
        List<String> sortedMimes = new ArrayList<String>(mimeTypes);
        // TODO - sort results by something more interesting than the alphabetical
        // order of their mimetypes...
        Collections.sort(sortedMimes);
        
        List<MimetypeRootNode> roots = new ArrayList<MimetypeRootNode>();
        int mimetypesWithElements = 0;
        for (String mimeType : mimeTypes) {
            Language language = registry.getLanguageByMimeType(mimeType);
            StructureScanner scanner = language.getStructure();
            if (scanner != null) {
                long startTime = System.currentTimeMillis();
                List<? extends StructureItem> children = scanner.scan(info);
                long endTime = System.currentTimeMillis();
                Logger.getLogger("TIMER").log(Level.FINE, "Structure (" + mimeType + ")",
                    new Object[] {info.getFileObject(), endTime - startTime});
                
                if(children.size() > 0) {
                    mimetypesWithElements++;
                }
                roots.add(new MimetypeRootNode(language, children));
            }
        }
        if (roots.size() > 1) {
            Collections.sort(roots, new Comparator<MimetypeRootNode>() {
                public int compare(MimetypeRootNode o1, MimetypeRootNode o2) {
                    return o1.getSortText().compareTo(o2.getSortText());
                }
            });
        }
        
        if(mimetypesWithElements > 1) {
            //at least two languages provided some elements - use the root mimetype nodes
            for(MimetypeRootNode root : roots) {
                items.add(root);
            }
        } else {
            //just one or none language provided elements - put them to the root
            for(MimetypeRootNode root : roots) {
                items.addAll(root.getNestedItems());
            }
        }
        
        if ( !canceled ) {
            ui.refresh( rootDescription, info.getFileObject() );
            
        }
    }
    
    static final class MimetypeRootNode implements StructureItem {

        //hack - see the getSortText() comment
        private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N
        private static final String CSS_SORT_TEXT = "2";//NOI18N
        private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
        private static final String RUBY_MIMETYPE = "text/x-ruby";//NOI18N
        private static final String YAML_MIMETYPE = "text/x-yaml";//NOI18N
        private static final String JAVASCRIPT_SORT_TEXT = "1";//NOI18N
        private static final String HTML_MIMETYPE = "text/html";//NOI18N
        private static final String HTML_SORT_TEXT = "3";//NOI18N
        // Ensure YAML is sorted before Ruby to make navigator look primarily in the YAML offsets first
        private static final String YAML_SORT_TEXT = "4";//NOI18N
        private static final String RUBY_SORT_TEXT = "5";//NOI18N
        private static final String OTHER_SORT_TEXT = "9";//NOI18N
        
        Language language;
        private List<? extends StructureItem> items;
        long from, to;
        
        private MimetypeRootNode(Language lang, List<? extends StructureItem> items) {
            this.language = lang;
            this.items = items;
            this.from = items.size() > 0 ? items.get(0).getPosition() : 0;
            this.to = items.size() > 0 ? items.get(items.size() - 1).getEndPosition() : 0;
        }
        
        @Override
        public boolean equals(Object o) {
            if(!(o instanceof MimetypeRootNode)) {
                return false;
            }
            MimetypeRootNode compared = (MimetypeRootNode)o;
            return language.equals(compared.language);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.language != null ? this.language.hashCode() : 0);
            return hash;
        }
        
        public String getName() {
            return language.getDisplayName();
        }

        public String getSortText() {
            //hack -> I need to ensure that the navigator selects the top most
            //node when user moves caret in the source.
            //Since the navigator tree is a generic graph if there are more
            //embedded languages we need to use some tricks...
            if(language.getMimeType().equals(CSS_MIMETYPE)) {
                return CSS_SORT_TEXT;
            } else if(language.getMimeType().equals(JAVASCRIPT_MIMETYPE)) {
                return JAVASCRIPT_SORT_TEXT;
            } else if (language.getMimeType().equals(HTML_MIMETYPE)) {
                return HTML_SORT_TEXT;
            } else if (language.getMimeType().equals(YAML_MIMETYPE)) {
                return YAML_SORT_TEXT;
            } else if (language.getMimeType().equals(RUBY_MIMETYPE)) {
                return RUBY_SORT_TEXT;
            } else {
                return OTHER_SORT_TEXT + getName();
            }
            
        }

        public String getHtml(HtmlFormatter formatter) {
            return getName();
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return false;
        }

        public List<? extends StructureItem> getNestedItems() {
            return items;
        }

        public long getPosition() {
            return from;
        }

        public long getEndPosition() {
            return to;
        }

        public ImageIcon getCustomIcon() {
            String iconBase = language.getIconBase();
            return  iconBase == null ? null : new ImageIcon(ImageUtilities.loadImage(iconBase));
        }

        
    }
}    
    
