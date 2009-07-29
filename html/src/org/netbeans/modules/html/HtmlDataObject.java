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

package org.netbeans.modules.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

/** Object that represents one html file.
 *
 * @author Ian Formanek
 * @author Marek Fukala
 */
public class HtmlDataObject extends MultiDataObject implements CookieSet.Factory {
    public static final String PROP_ENCODING = "Content-Encoding"; // NOI18N
    public static final String DEFAULT_ENCODING = new InputStreamReader(System.in).getEncoding();
    static final long serialVersionUID =8354927561693097159L;
    
    //constants used when finding html document content type
    private static final String CHARSET_DECL = "CHARSET="; //NOI18N
    
    private HtmlEditorSupport htmlEditorSupport;
    
    /** New instance.
     * @param pf primary file object for this data object
     * @param loader the data loader creating it
     * @exception DataObjectExistsException if there was already a data object for it
     */
    public HtmlDataObject(FileObject pf, UniFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.add(HtmlEditorSupport.class, this);
        set.add(ViewSupport.class, this);
        set.assign(SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                HtmlEditorSupport es = getCookie( HtmlEditorSupport.class );
                try {
                    es.updateEncoding();
                    es.saveAs( folder, fileName );
                } catch (UserCancelException e) {
                    //ignore, just not save anything
                }
            }
        });

        FileEncodingQueryImplementation feq = new FileEncodingQueryImplementation() {
            public Charset getEncoding(FileObject file) {
                assert file != null;
                assert file.equals(getPrimaryFile());
                
                String charsetName = getFileEncoding();
                if(charsetName == null) {
                    return null; //nothing found in the document
                }
                
                try {
                    return Charset.forName(charsetName);
                } catch(IllegalCharsetNameException ichse) {
                    //the jsp templates contains the ${encoding} property 
                    //so the ICHNE is always thrown for them, just ignore
                    Boolean template = (Boolean)file.getAttribute("template");//NOI18N
                    if(template == null || !template.booleanValue()) {
                        Logger.getLogger("global").log(Level.INFO, null, ichse);
                    }
                } catch (UnsupportedCharsetException uchse) {
                    Logger.getLogger("global").log(Level.INFO, null, uchse);
                }
                return null;
            }
        };
        set.assign(FileEncodingQueryImplementation.class, feq);
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    protected org.openide.nodes.Node createNodeDelegate() {
        DataNode n = new HtmlDataNode(this, Children.LEAF);
        n.setIconBaseWithExtension("org/netbeans/modules/html/htmlObject.png"); // NOI18N
        return n;
    }
    
    /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if (klass.isAssignableFrom(HtmlEditorSupport.class)) {
            return getHtmlEditorSupport();
        } else if (klass.isAssignableFrom(ViewSupport.class)) {
            return new ViewSupport(getPrimaryEntry());
        } else {
            return null;
        }
    }
    
    private synchronized HtmlEditorSupport getHtmlEditorSupport() {
        if (htmlEditorSupport == null) {
            htmlEditorSupport = new HtmlEditorSupport(this);
        }
        return htmlEditorSupport;
    }
    
    // Package accessibility for HtmlEditorSupport:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    /** Checks the file for UTF-16 marks and calls findEncoding with properly loaded document content then. */
    String getFileEncoding() {
        String encoding = null;
        //detect encoding from input stream
        InputStream is = null;
        try {
            is = getPrimaryFile().getInputStream();
            byte[] arr = new byte[4096];
            int len = is.read(arr);
            len = (len >= 0) ? len : 0;
            //check UTF-16 mark
            if (len > 1) {
                int mark = (arr[0]&0xff)*0x100+(arr[1]&0xff);
                if (mark == 0xfeff) {
                    encoding = "UTF-16"; // NOI18N
                } else if(mark == 0xfffe) {
                    encoding = "UTF-16LE";
                }
            }
            
            //try to read the file using some encodings
            String[] encodings = new String[]{encoding != null ? encoding : DEFAULT_ENCODING, "UTF-16LE", "UTF-16BE"};
            int i = 0;
            do {
                encoding = findEncoding(makeString(arr, 0, len, encodings[i++]));
            } while (encoding == null && i < encodings.length);
            
            
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.WARNING, null, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.WARNING, null, ex);
            }
        }
        if (encoding != null) {
            encoding = encoding.trim();
        }
        return encoding;
    }
    
    private String makeString(byte[] arr, int offset, int len, String encoding) throws UnsupportedEncodingException {
        return new String(arr, 0, len, encoding).toUpperCase();
    }
    
    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    static String findEncoding(String txt) {
        int[] offsets = findEncodingOffsets(txt);
        if (offsets.length == 3) {
            String encoding = txt.substring(offsets[0] + offsets[1], offsets[0] + offsets[2]);
            return encoding;
        }
        return null;
    }
    
    private static int[] findEncodingOffsets(String txt) {
        int[] rslt = new int[0];
        TokenHierarchy hi = TokenHierarchy.create(txt, HTMLTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        ts.moveStart();
        while(ts.moveNext()) {
            Token token = ts.token();
            if(token.id() == HTMLTokenId.VALUE) {
                String tokenImage = token.text().toString();
                int charsetOffset = tokenImage.indexOf(CHARSET_DECL);
                charsetOffset = charsetOffset == -1 ? tokenImage.indexOf(CHARSET_DECL.toLowerCase()) : charsetOffset;
                
                int charsetEndOffset = charsetOffset + CHARSET_DECL.length();
                if (charsetOffset != -1){
                    int endOffset = tokenImage.indexOf('"', charsetEndOffset);
                    
                    if (endOffset == -1){
                        endOffset = tokenImage.indexOf('\'', charsetEndOffset);
                    }
                    
                    if (endOffset == -1){
                        endOffset = tokenImage.indexOf(';', charsetEndOffset);
                    }
                    
                    if (endOffset == -1){
                        return rslt;
                    }
                    
                    rslt =  new int[]{token.offset(hi), charsetEndOffset, endOffset};
                }
            }
        }
        return rslt;
    }
    
    static final class ViewSupport implements ViewCookie {
        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport(MultiDataObject.Entry primary) {
            this.primary = primary;
        }
        
        public void view() {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(primary.getFile().getURL());
            } catch (FileStateInvalidException e) {
            }
        }
    }
    
}
