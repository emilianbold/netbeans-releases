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
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.gsf.GsfHtmlFormatter;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;

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

            public String getHtml() {
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
        
        for (String mimeType : mimeTypes) {
            Language language = registry.getLanguageByMimeType(mimeType);
            StructureScanner scanner = language.getStructure();
            if (scanner != null) {
                List<? extends StructureItem> children = scanner.scan(info, new NavigatorFormatter());
                for (StructureItem co : children) {
                    items.add(co);
                }
            }
        }
        
        if ( !canceled ) {
            ui.refresh( rootDescription, info.getFileObject() );
            
        }
    }
    
    private class NavigatorFormatter extends GsfHtmlFormatter {
        @Override
        public void name(ElementKind kind, boolean start) {
            // No special formatting for names
        }
    }
}    
    
