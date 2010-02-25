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

package org.netbeans.modules.html.editor.api;

import java.io.File;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author marekfukala
 */
public class Utils {

   
    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo
    * can not be related to, or if url is intended to be a directory
    */
    public static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }

      /** This method returns an image, which is displayed for the FileObject in the explorer.
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    public static java.awt.Image getIcon(FileObject fo){
        try {
            return DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }catch(DataObjectNotFoundException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Cannot find icon for " + fo.getNameExt(), e);
        }
        return null;
    }

    /** returns top most joined html token seuence for the document at the specified offset. */
    public static TokenSequence<HTMLTokenId> getJoinedHtmlSequence(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence ts = th.tokenSequence();
        //XXX this seems to be wrong, the return code should be checked
        ts.move(offset);

        while(ts.moveNext() || ts.movePrevious()) {
            if(ts.language() == HTMLTokenId.language()) {
                return ts;
            }
            
            ts = ts.embeddedJoined();
            
            if(ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            //XXX this seems to be wrong, the return code should be checked
            ts.move(offset);
        }

        return null;
        
    }

    /**
     * Finds and returns a tag open token in token sequence positioned inside an html tag.
     * If tokens sequence contains <div onclick="alert()"/> and is positioned on a token
     * within the tag it will return OPEN_TAG token for the "div" text
     *
     * @param ts a TokenSequence to be used
     * @return
     */
    public static Token<HTMLTokenId> findTagOpenToken(TokenSequence ts) {
        assert ts != null;

        //skip the tag close symbol if the sequence points to it
        if(ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
            if(!ts.movePrevious()) {
                return null;
            }
        }

        do {
            Token t = ts.token();
            TokenId id = t.id();
            if( id == HTMLTokenId.TAG_OPEN) {
                return t;
            }

            if(id == HTMLTokenId.TAG_OPEN_SYMBOL || id == HTMLTokenId.TAG_CLOSE_SYMBOL ||
                    id == HTMLTokenId.TEXT) {
                break;
            }


        } while (ts.movePrevious());

        return null;
    }

  
}
