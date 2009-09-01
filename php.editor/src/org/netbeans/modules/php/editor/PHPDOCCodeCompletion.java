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
package org.netbeans.modules.php.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author tomslot
 */
public class PHPDOCCodeCompletion {
    private static final String TAG_PREFIX = "@";
    private static final String TAGS[] = new String[]{
        "abstract", "access", "author", "category", "copyright", "deprecated", "example", "final",
        "filesource", "global", "ignore", "internal", "license", "link", "method", "name", "package",
        "param", "property", "property-read", "property-write", "return", "see", "since", "static",
        "staticvar", "subpackage", "todo", "tutorial", "uses", "var", "version"
    };
    private static final Map<String, String> CUSTOM_TEMPLATES = new TreeMap<String, String>();
    private static String docURLBase;
    private static Collection<PHPDocCommentTokenId> TYPE_TOKENS = Arrays.asList(
            PHPDocCommentTokenId.PHPDOC_RETURN,
            PHPDocCommentTokenId.PHPDOC_VAR,
            PHPDocCommentTokenId.PHPDOC_PARAM,
            PHPDocCommentTokenId.PHPDOC_METHOD,
            PHPDocCommentTokenId.PHPDOC_PROPERTY,
            PHPDocCommentTokenId.PHPDOC_PROPERTY_READ,
            PHPDocCommentTokenId.PHPDOC_PROPERTY_WRITE);

    static {
        File file = InstalledFileLocator.getDefault().locate("docs/phpdocdesc.zip", null, true); //NoI18N
        if (file != null) {
            try {
                URL urll = file.toURL();
                urll = FileUtil.getArchiveRoot(urll);
                docURLBase = urll.toString();
            } catch (java.net.MalformedURLException e) {
                // nothing to do
                }
        }
    }
    
    static boolean isTypeCtx(PHPCompletionItem.CompletionRequest request){
        Document document = request.info.getSnapshot().getSource().getDocument(false);
        TokenSequence<PHPTokenId> phpTS = LexUtilities.getPHPTokenSequence(document, request.anchor);
        if (phpTS != null) {
            phpTS.move(request.anchor);
            phpTS.moveNext();
            TokenSequence<PHPDocCommentTokenId> tokenSequence = phpTS.embedded(PHPDocCommentTokenId.language());
            tokenSequence.move(request.anchor);
            if (tokenSequence.movePrevious()) {

                if (TYPE_TOKENS.contains(tokenSequence.token().id())) {
                    int offset = tokenSequence.offset() + tokenSequence.token().length();
                    try {
                        // text between PHPDoc directive and begining of the prefix, should only contain white spaces
                        String txt = document.getText(offset, request.anchor - offset);
                        if (txt.charAt(txt.length()-1) == '|') {
                            // expect that user wants to complete mixed type
                            txt = txt.trim();
                            for (int i = 0; i < txt.length(); i++) {
                            if (Character.isWhitespace(txt.charAt(i))) {
                                    return false;
                                }
                            }
                            return true;
                        }
                        for (int i = 0; i < txt.length(); i++) {
                            if (!Character.isWhitespace(txt.charAt(i))) {
                                return false;
                            }
                        }

                        return true;
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
        return false;
    }

    public static void complete(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        
        
        if (!request.prefix.startsWith("@")){
            return;
        }

        String prefix = request.prefix.startsWith(TAG_PREFIX) ?
            request.prefix.substring(1) : request.prefix;

        for (String tag : TAGS){
            if (tag.startsWith(prefix)){
                PHPDOCCodeCompletionItem item = new PHPDOCCodeCompletionItem(request, tag);
                proposals.add(item);
            }
        }
    }

    public static String getDoc(String tag) {
        String resPath = String.format("%s%s.desc", docURLBase, tag); //NOI18N
        
        try{
            URL url = new URL(resPath);
            InputStream is = url.openStream();
            byte buffer[] = new byte[1000];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count = 0;
            do {
                count = is.read(buffer);
                if (count > 0) baos.write(buffer, 0, count);
            } while (count > 0);
            
            is.close();
            String text = baos.toString();
            baos.close();
            return text;
        } catch (java.io.IOException e){
            return null;
        }
    }

    public static class PHPDOCCodeCompletionItem implements CompletionProposal {
        private String tag;
        private PHPCompletionItem.CompletionRequest request;
        private PHPDOCTagElement elem;

        public PHPDOCCodeCompletionItem(CompletionRequest request, String tag) {
            this.tag = tag;
            this.request = request;
            elem = new PHPDOCTagElement(tag);
        }

        public int getAnchorOffset() {
            return request.anchor;
        }

        public ElementHandle getElement() {
            return elem;
        }

        public String getName() {
            return TAG_PREFIX + tag; //NOI18N
        }

        public String getInsertPrefix() {
            return getName(); 
        }

        public String getSortText() {
            return getName();
        }

        public int getSortPrioOverride() {
            return 0;
        }

        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.appendText(getName());
            return formatter.getText();
        }

        public String getRhsHtml(HtmlFormatter formatter) {
            return ""; //NOI18N
        }

        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        public ImageIcon getIcon() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        public boolean isSmart() {
            return false;
        }

        public String getCustomInsertTemplate() {
            return CUSTOM_TEMPLATES.get(tag);
        }
    }
}
