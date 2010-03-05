/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jrubyparser.ast.Node;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

abstract class RubyDeclarationFinderHelper {

    protected static final boolean CHOOSE_ONE_DECLARATION = Boolean.getBoolean("ruby.choose_one_decl");

    protected static DeclarationLocation fix(final DeclarationLocation location, final ParserResult result) {
        if ((location != DeclarationLocation.NONE) && (location.getFileObject() == null) &&
                (location.getUrl() == null)) {
            return new DeclarationLocation(RubyUtils.getFileObject(result), location.getOffset(), location.getElement());
        }
        return location;
    }

    protected static DeclarationLocation getLocation(final ParserResult result, final Node node) {
        AstElement element = AstElement.create(result, node);
        int lexOffset = LexUtilities.getLexerOffset(result, node.getPosition().getStartOffset());
        return new DeclarationLocation(null, lexOffset, element);
    }

    static class RubyAltLocation implements AlternativeLocation {

        private IndexedElement element;
        private boolean isPreferred;
        private String cachedDisplayItem;

        RubyAltLocation(IndexedElement element, boolean isPreferred) {
            this.element = element;
            this.isPreferred = isPreferred;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.setMaxLength(120);
            if (cachedDisplayItem == null) {
                formatter.reset();

                boolean nodoc = element.isNoDoc();
                boolean documented = element.isDocumented();
                if (isPreferred) {
                    formatter.emphasis(true);
                } else if (nodoc) {
                    formatter.deprecated(true);
                }

                if (element instanceof IndexedMethod) {
                    if (element.getFqn() != null) {
                        formatter.appendText(element.getFqn());
                        formatter.appendText(".");
                    }
                    formatter.appendText(element.getName());
                    IndexedMethod method = (IndexedMethod) element;
                    Collection<String> parameters = method.getParameters();

                    if ((parameters != null) && (parameters.size() > 0)) {
                        formatter.appendText("("); // NOI18N

                        Iterator<String> it = parameters.iterator();

                        while (it.hasNext()) { // && tIt.hasNext()) {
                            formatter.parameters(true);
                            formatter.appendText(it.next());
                            formatter.parameters(false);

                            if (it.hasNext()) {
                                formatter.appendText(", "); // NOI18N
                            }
                        }

                        formatter.appendText(")"); // NOI18N
                    }
                } else {
                    formatter.appendText(element.getFqn());
                }

                String filename = null;
                String url = element.getFileUrl();
                if (url == null) {
                    // Deleted file?
                    // Just leave out the file name
                } else if (RubyUtils.isRubyStubsURL(url)) {
                    filename = NbBundle.getMessage(RubyDeclarationFinder.class, "RubyLib");

                    if (url.indexOf("/stub_") == -1) {
                        // Not a stub file, such as ftools.rb
                        // TODO - don't hardcode for version 0.2
                        String stub = RubyPlatform.RUBYSTUBS + "/" + RubyPlatform.RUBYSTUBS_VERSION;
                        int stubStart = url.indexOf(stub);
                        if (stubStart != -1) {
                            filename = filename + ": " + url.substring(stubStart);
                        }
                    }
                } else {
                    FileObject fo = element.getFileObject();
                    if (fo != null) {
                        filename = fo.getNameExt();
                    } else {
                        // Perhaps a file that isn't present here, such as something in site_ruby
                        int lastIndex = url.lastIndexOf('/');
                        if (lastIndex != -1) {
                            String s = url.substring(0, lastIndex);
                            int almostLastIndex = s.lastIndexOf('/');
                            if (almostLastIndex != -1 && ((url.length() - almostLastIndex) < 40)) {
                                filename = url.substring(almostLastIndex + 1);
                                if (filename.indexOf(':') != -1) {
                                    // Don't include prefix like cluster:, file:, etc.
                                    filename = url.substring(lastIndex + 1);
                                }
                            } else {
                                filename = url.substring(lastIndex + 1);
                            }
                        }
                    }

                    // TODO - make this work with 1.9 etc.
                    //final String GEM_LOC = "lib/ruby/gems/1.8/gems/";
                    Pattern p = Pattern.compile("lib/ruby/gems/\\d+\\.\\d+/gems/");
                    Matcher m = p.matcher(url);
                    //int gemIndex = url.indexOf(GEM_LOC);
                    //if (gemIndex != -1) {
                    if (m.find()) {
                        //int gemIndex = m.start();
                        //gemIndex += GEM_LOC.length();
                        int gemIndex = m.end();
                        int gemEnd = url.indexOf('/', gemIndex);
                        if (gemEnd != -1) {
                            //int libIndex = url.indexOf("lib/", gemEnd);
                            //if (libIndex != -1) {
                            //    filename = url.substring(libIndex+4);
                            //}
                            filename = url.substring(gemIndex, gemEnd) + ": " + filename;
                        }
                    }
                }

                if (filename != null) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(RubyDeclarationFinder.class, "In"));
                    formatter.appendText(" ");
                    formatter.appendText(filename);
                }

                if (documented) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(RubyDeclarationFinder.class, "Documented"));
                } else if (nodoc) {
                    formatter.appendText(" ");
                    formatter.appendText(NbBundle.getMessage(RubyDeclarationFinder.class, "NoDoced"));
                }

                if (isPreferred) {
                    formatter.emphasis(false);
                } else if (nodoc) {
                    formatter.deprecated(false);
                }

                cachedDisplayItem = formatter.getText();
            }

            return cachedDisplayItem;
        }

        @Override
        public DeclarationLocation getLocation() {
            Node node = AstUtilities.getForeignNode(element);
            int lineOffset = node != null ? node.getPosition().getStartOffset() : -1;
            DeclarationLocation loc = new DeclarationLocation(element.getFileObject(),
                    lineOffset, element);

            return loc;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public int compareTo(final AlternativeLocation alternative) {
            RubyAltLocation alt = (RubyAltLocation) alternative;

            // The preferred item should be chosen
            if (isPreferred) {
                return -1;
            } else if (alt.isPreferred) {
                return 1;
            } // Can't both be so no else == check

            // Nodoced items last
            if (element.isNoDoc() != alt.element.isNoDoc()) {
                return element.isNoDoc() ? 1 : -1;
            }

            // Documented items on top
            if (element.isDocumented() != alt.element.isDocumented()) {
                return element.isDocumented() ? -1 : 1;
            }

            // TODO: Sort by gem?

            // Sort by containing clz - just do fqn here?
            String thisIn = element.getIn() != null ? element.getIn() : "";
            String thatIn = alt.element.getIn() != null ? alt.element.getIn() : "";
            int cmp = thisIn.compareTo(thatIn);
            if (cmp != 0) {
                return cmp;
            }

            // Sort by file
            String thisFile = element.getFileObject() != null ? element.getFileObject().getNameExt() : "";
            String thatFile = alt.element.getFileObject() != null ? alt.element.getFileObject().getNameExt() : "";
            cmp = thisFile.compareTo(thatFile);

            return cmp;
        }
    }

}
