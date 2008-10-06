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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
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
        if (!(doc instanceof BaseDocument) || !isPomFile(doc)) {
            return false;
        }
        BaseDocument bdoc = (BaseDocument) doc;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
        TokenItem token;
        try {
            token = sup.getTokenChain(offset, offset + 1);
            //if (debug) debug ("token: "  +token.getTokenID().getNumericID() + ":" + token.getTokenID().getName());
            // when it's not a value -> do nothing.
            if (token == null) {
                return false;
            }
            TokenItem previous = token.getPrevious();
            if (previous != null && previous.getImage().equals(">")) { //NOI18N
                //we are in element text
                FileObject fo = getProjectDir(doc);
                if (getPath(fo, token.getImage()) != null) {
                    return true;
                } 
            }
            // urls get opened..
            if (token.getImage() != null && 
                    (token.getImage().startsWith("http://") || //NOI18N
                    (token.getImage().startsWith("https://")))) { //NOI18N
                return true;
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
            
        
        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument) || !isPomFile(doc)) {
            return null;
        }
        
        BaseDocument bdoc = (BaseDocument) doc;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
        TokenItem token;
        try {
            token = sup.getTokenChain(offset, offset + 1);
            if (token == null) {
                return null;
            }
            TokenItem previous = token.getPrevious();
            if (previous != null && previous.getImage().equals(">")) {//NOI18N
                //we are in element text
                FileObject fo = getProjectDir(doc);
                if (getPath(fo, token.getImage()) != null) {
                    return new int[] {token.getOffset(), token.getNext().getOffset()};
                } 
            }
            if (token.getImage() != null && 
                    (token.getImage().startsWith("http://") || //NOI18N
                    (token.getImage().startsWith("https://")))) { //NOI18N
                return new int[] {token.getOffset(), token.getNext().getOffset()};
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void performClickAction(Document doc, int offset) {
        if (!(doc instanceof BaseDocument) || !isPomFile(doc)) {
            return;
        }
        
        BaseDocument bdoc = (BaseDocument) doc;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
        TokenItem token;
        try {
            token = sup.getTokenChain(offset, offset + 1);
            if (token == null) {
                return;
            }
            TokenItem previous = token.getPrevious();
            if (previous != null && previous.getImage().equals(">")) { //NOI18N
                //we are in element text
                FileObject fo = getProjectDir(doc);
                String path = token.getImage();
                if (previous.getPrevious().getImage().equals("<module")) { //NOI18N
                    path = path + "/pom.xml"; //NOI18N
                }
                if (getPath(fo, path) != null) {
                    FileObject file = getPath(fo, path);
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
            if (token.getImage() != null && 
                    (token.getImage().startsWith("http://") || //NOI18N
                    (token.getImage().startsWith("https://")))) { //NOI18N
                try {
                    URL url = new URL(token.getImage());
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return;
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
