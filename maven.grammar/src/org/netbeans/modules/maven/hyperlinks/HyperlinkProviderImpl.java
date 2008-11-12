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

package org.netbeans.modules.maven.hyperlinks;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * adds hyperlinking support to pom.xml files..
 * @author mkleint
 */
public class HyperlinkProviderImpl implements HyperlinkProvider {
    
    /** Creates a new instance of HyperlinkProvider */
    public HyperlinkProviderImpl() {
    }

    public boolean isHyperlinkPoint(Document doc, int offset) {
        if (!isPomFile(doc)) {
            return false;
        }
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
        xml.move(offset);
        xml.moveNext();
        Token<XMLTokenId> token = xml.token();
        // when it's not a value -> do nothing.
        if (token == null) {
            return false;
        }
        if (token.id() == XMLTokenId.TEXT) {
            //we are in element text
            FileObject fo = getProjectDir(doc);
            String text = token.text().toString();
            if (getPath(fo, text) != null) {
                return true;
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                return true;
            }
        }
        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!isPomFile(doc)) {
            return null;
        }
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
        xml.move(offset);
        xml.moveNext();
        Token<XMLTokenId> token = xml.token();
        // when it's not a value -> do nothing.
        if (token == null) {
            return null;
        }
        if (token.id() == XMLTokenId.TEXT) {
            //we are in element text
            FileObject fo = getProjectDir(doc);
            String text = token.text().toString();
            if (getPath(fo, text) != null) {
                return new int[] { xml.offset(), xml.offset() + text.length() };
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                return new int[] { xml.offset(), xml.offset() + text.length() };
            }
        }
        return null;
    }

    public void performClickAction(Document doc, int offset) {
        if (!isPomFile(doc)) {
            return;
        }
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XMLTokenId> xml = th.tokenSequence(XMLTokenId.language());
        xml.move(offset);
        xml.moveNext();
        Token<XMLTokenId> token = xml.token();
        // when it's not a value -> do nothing.
        if (token == null) {
            return;
        }
        if (token.id() == XMLTokenId.TEXT) {
            //we are in element text
            FileObject fo = getProjectDir(doc);
            String text = token.text().toString();
            if (getPath(fo, text) != null) {
                xml.movePrevious();
                token = xml.token();
                if (token != null && token.id() == XMLTokenId.TAG && token.text().equals(">")) {
                    xml.movePrevious();
                    token = xml.token();
                    if (token != null && token.id() == XMLTokenId.TAG && token.text().equals("<module")) {
                        text = text + "/pom.xml"; //NOI18N
                    }
                }
                if (getPath(fo, text) != null) {
                    FileObject file = getPath(fo, text);
                    DataObject dobj;
                    try {
                        dobj = DataObject.find(file);
                        EditCookie edit = dobj.getCookie(EditCookie.class);
                        if (edit != null) {
                            edit.edit();
                        }
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // urls get opened..
            if (text != null &&
                    (text.startsWith("http://") || //NOI18N
                    (text.startsWith("https://")))) { //NOI18N
                try {
                    URL url = new URL(text);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private FileObject getProjectDir(Document doc) {
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        return dObject.getPrimaryFile().getParent();
    }
    
    private FileObject getPath(FileObject parent, String path) {
        // TODO more substitutions necessary probably..
        if (path.startsWith("${basedir}/")) { //NOI18N
            path = path.substring("${basedir}/".length()); //NOI18N
        }
        while (path.startsWith("../") && parent.getParent() != null) { //NOI18N
            path = path.substring("../".length()); //NOI18N
            parent = parent.getParent();
        }
        return parent.getFileObject(path);
    }

    private boolean isPomFile(Document doc) {
        String type = (String) doc.getProperty("mimeType"); //NOI18N
        if (type != null) {
            if ("text/x-maven-pom+xml".equals(type)) { //NOI18N
                return true;
            }
            if (!"text/xml".equals(type)) { //NOI18N
                return false;
            }
        }

        //TODO this should be eventually abandoned in favour of specific supported mimetypes.
        
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (dObject != null && "pom.xml".equalsIgnoreCase(dObject.getPrimaryFile().getNameExt())) { //NOI18N
            // is that enough?
            return true;
        }
        if (dObject != null && "settings.xml".equals(dObject.getPrimaryFile().getNameExt()) && ".m2".equals(dObject.getPrimaryFile().getParent().getNameExt())) { //NOI18N
            return true;
        }
        return false;
    }
    
}
