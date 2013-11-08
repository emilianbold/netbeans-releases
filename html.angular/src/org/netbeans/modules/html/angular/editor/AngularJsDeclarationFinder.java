/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.angular.index.AngularJsController;
import org.netbeans.modules.html.angular.index.AngularJsIndex;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority=13)
public class AngularJsDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        System.out.println("findDeclaration");
        int embeddedOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot(), embeddedOffset);
        if (ts == null) {
            return DeclarationLocation.NONE;
        }
        
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return DeclarationLocation.NONE;
        }
        
        ts.move(embeddedOffset);
        if (ts.moveNext()) {
            JsTokenId id = ts.token().id();
            if (id == JsTokenId.IDENTIFIER) {
                Project project = FileOwnerQuery.getOwner(fo);
                try {
                    Collection<AngularJsController> controllers = AngularJsIndex.get(project).getControllers(ts.token().text().toString(), true);
                    if (!controllers.isEmpty()) {
                        DeclarationLocation dl = null;
                        for (AngularJsController controller : controllers) {
                            URI uri = null;
                            try {
                                uri = controller.getDeclarationFile().toURI();
                            } catch (URISyntaxException ex) {
                                // nothing
                            }
                            if (uri != null) {
                                File file = new File(uri);
                                FileObject dfo = FileUtil.toFileObject(file);
                                DeclarationLocation dloc = new DeclarationLocation(dfo, controller.getOffset());
                                //grrr, the main declarationlocation must be also added to the alternatives
                                //if there are more than one
                                if (dl == null) {
                                    //ugly DeclarationLocation alternatives handling workaround - one of the
                                    //locations simply must be "main"!!!
                                    dl = dloc;
                                }
                                AlternativeLocation aloc = new AlternativeLocationImpl(controller.getName(), dloc);
                                dl.addAlternative(aloc);
                            }
                        }
                        //and finally if there was just one entry, remove the "alternative"
                        if (dl != null && dl.getAlternativeLocations().size() == 1) {
                            dl.getAlternativeLocations().clear();
                        }

                        if (dl != null) {
                            return dl;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        System.out.println("getReferenceSpan");
//        int embeddedOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
        
        ts.move(caretOffset);
        if (ts.moveNext()) {
            JsTokenId id = ts.token().id();
            if (id == JsTokenId.IDENTIFIER) {
                return new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
            }
        }
        return OffsetRange.NONE;
    }
    
    private static class AlternativeLocationImpl implements AlternativeLocation {
        
        private final DeclarationLocation location;
        private String name;
        
        public AlternativeLocationImpl(String name, DeclarationLocation location) {
            this.location = location;
            this.name = name;
        }
        
        @Override
        public ElementHandle getElement() {
            return null;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            FileObject fo = location.getFileObject();
            if (fo != null) {
                return fo.getPath();
            }
            return name;
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }
        

        @Override
        public int compareTo(AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            StringBuilder sb = new StringBuilder();
            sb.append(loc.getLocation().getOffset()); //offset
            FileObject fo = loc.getLocation().getFileObject();
            if (fo != null) {
                sb.append(fo.getPath()); //filename
            }
            return sb.toString();
        }
        
    }
}
