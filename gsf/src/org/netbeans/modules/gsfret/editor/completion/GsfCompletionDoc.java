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
package org.netbeans.modules.gsfret.editor.completion;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.gsf.Completable;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.gsf.Language;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;


/**
 * Produce completion-popup help for elements
 * 
 * @author Tor Norbye
 */
public class GsfCompletionDoc implements CompletionDocumentation {
    private String content = null;
    private URL docURL = null;
    private AbstractAction goToSource = null;
    private ElementHandle elementHandle;
    private Language language;
    private CompilationController controller;

    private GsfCompletionDoc(final CompilationController controller, final ElementHandle elementHandle,
        URL url) {
        this.controller = controller;
        this.language = controller.getLanguage();
        Completable completer = language.getCompletionProvider();
        final Parser parser = language.getParser();
        final Element resolved;
        if ((completer != null) && (parser != null)) {
            resolved = parser.resolveHandle(controller, elementHandle);
        } else {
            resolved = null;
        }

        this.elementHandle = elementHandle;

        if (elementHandle != null) {
            goToSource =
                new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            Completion.get().hideAll();
                            UiUtils.open(controller.getSource(), elementHandle);
                        }
                    };
            if (url != null) {
                docURL = url;
            } else {
                docURL = null;
            }
        }

        if (resolved != null) {
            this.content = completer.document(controller, resolved);
        }

        if (this.content == null) {
            Completion.get().hideDocumentation();
        }
    }

    public static final GsfCompletionDoc create(CompilationController controller,
        ElementHandle elementHandle) {
        return new GsfCompletionDoc(controller, elementHandle, null);
    }

    public String getText() {
        return content;
    }

    public URL getURL() {
        return docURL;
    }

    public CompletionDocumentation resolveLink(String link) {
        if (link.startsWith("www.")) {
            link = "http://" + link;
        }
        if (link.matches("[a-z]+://.*")) { // NOI18N
            try {
                URL url = new URL(link);
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                return null;
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        } else if (link.indexOf("#") != -1) {
            final Parser parser = language.getParser();
            if (link.startsWith("#")) {
                // Put the current class etc. in front of the method call if necessary
                Element surrounding = parser.resolveHandle(null, elementHandle);
                if (surrounding != null && surrounding.getKind() != ElementKind.KEYWORD) {
                    String name = surrounding.getName();
                    ElementKind kind = surrounding.getKind();
                    if (!(kind == ElementKind.CLASS || kind == ElementKind.MODULE)) {
                        String in = surrounding.getIn();
                        if (in != null && in.length() > 0) {
                            name = in;
                        } else if (name != null) {
                            int index = name.indexOf('#');
                            if (index > 0) {
                                name = name.substring(0, index);
                            }
                        }
                    }
                    if (name != null) {
                        link = name + link;
                    }
                }
            }
            ElementHandle handle = new ElementHandle.UrlHandle(link);
            final Element resolved = parser.resolveHandle(null, handle);
            return new GsfCompletionDoc(controller, handle, null);
        }
        
        return null;
    }

    public Action getGotoSourceAction() {
        return goToSource;
    }
}
