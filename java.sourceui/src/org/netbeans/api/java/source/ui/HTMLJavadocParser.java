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

package org.netbeans.api.java.source.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 *  HTML Parser. It retrieves sections of the javadoc HTML file.
 *
 * @author  Martin Roskanin
 */
class HTMLJavadocParser {
    

    /** Gets the javadoc text from the given URL
     *  @param url nbfs protocol URL
     *  @param pkg true if URL should be retrieved for a package
     */
    public static String getJavadocText(URL url, boolean pkg) {
        if (url == null) return null;
        
        HTMLEditorKit.Parser parser;
        InputStream is = null;
        
        String charset = null;
        for (;;) {
            try{
                is = url.openStream();
                parser = new ParserDelegator();
                String urlStr = URLDecoder.decode(url.toString(), "UTF-8"); //NOI18N
                int offsets[] = null;
                Reader reader = charset == null?new InputStreamReader(is): new InputStreamReader(is, charset);
                
                if (pkg){
                    // package description
                    offsets = parsePackage(reader, parser, charset != null);
                }else if (urlStr.indexOf('#')>0){
                    // member javadoc info
                    String memberName = urlStr.substring(urlStr.indexOf('#')+1);
                    if (memberName.length()>0) offsets = parseMember(reader, memberName, parser, charset != null);
                }else{
                    // class javadoc info
                    offsets = parseClass(reader, parser, charset != null);
                }
                
                if (offsets != null){
                    return getTextFromURLStream(url, offsets, charset);
                }
                break;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    private static String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }
        
        //charsetspec is in form "text/html; charset=UTF-8"
                
        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }
        
        spec = spec.toLowerCase();
        
        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }
            
            foundCharSet = false;
            foundEquals = false;
        }
        
        return null;
    }
    
    private static String getTextFromURLStream(URL url, int[] offsets, String charset) throws IOException {
        if (url == null)
            return null;

        InputStream fis = null;
        InputStreamReader fisreader = null;
        try {
            fis = url.openStream();
            fisreader = charset == null ? new InputStreamReader(fis) : new InputStreamReader(fis, charset);
            
            StringBuilder sb = new StringBuilder();
            int offset = 0;

            for (int i = 0; i < offsets.length - 1; i+=2) {
                int startOffset = offsets[i];
                int endOffset = offsets[i + 1];
                if (startOffset < 0 || endOffset < 0)
                    continue;
                if (startOffset > endOffset)
                    throw new IOException();

                int len = endOffset - startOffset;
                char buffer[] = new char[len];
                int bytesToSkip = startOffset - offset;
                long bytesSkipped = 0;
                do {
                    bytesSkipped = fisreader.skip(bytesToSkip);
                    bytesToSkip -= bytesSkipped;
                } while ((bytesToSkip > 0) && (bytesSkipped > 0));

                int bytesAlreadyRead = 0;
                do {
                    int count = fisreader.read(buffer, bytesAlreadyRead, len - bytesAlreadyRead);
                    if (count < 0){
                        break;
                    }
                    bytesAlreadyRead += count;
                } while (bytesAlreadyRead < len);
                sb.append(buffer);
                offset = endOffset;
            }
            return sb.toString();
        } finally {
            if (fisreader != null)
                fisreader.close();
        }
    }

    
    /** Retrieves the position (start offset and end offset) of class javadoc info
      * in the raw html file */
    private static int[] parseClass(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // javadoc HTML comment '======== START OF CLASS DATA ========'
        final int CLASS_DATA_START = 1;
        // start of the text we need. Located just after first P.
        final int TEXT_START = 2;

        final int state[] = new int[] {INIT};
        final int offset[] = new int[] {-1, -1, -1, -1};

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int nextHRPos = -1;
            int lastHRPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR){
                    if (state[0] == TEXT_START){
                        nextHRPos = pos;
                    }
                    lastHRPos = pos;
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.P && state[0] == CLASS_DATA_START){
                    if (offset[0] != -1 && offset[1] == -1)
                        offset[1] = pos + 3;
                    else
                        state[0] = TEXT_START;
                }
                if (t == HTML.Tag.A && state[0] == TEXT_START) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (attrName!=null && attrName.length()>0){
                        if (nextHRPos!=-1){
                            offset[3] = nextHRPos;
                        }else{
                            offset[3] = pos;
                        }
                        state[0] = INIT;
                    }
                }
            }

            public void handleComment(char[] data, int pos){
                String comment = String.valueOf(data);
                if (comment!=null){
                    if (comment.indexOf("START OF CLASS DATA")>0){ //NOI18N
                        state[0] = CLASS_DATA_START;
                    } else if (comment.indexOf("NESTED CLASS SUMMARY")>0){ //NOI18N
                        if (lastHRPos!=-1){
                            offset[3] = lastHRPos;
                        }else{
                            offset[3] = pos;
                        }
                    }
                }
            }
            
            public void handleText(char[] data, int pos) {
                if (state[0] == CLASS_DATA_START && "Deprecated.".equals(new String(data))) //NOI18N
                    offset[0] = lastHRPos + 4;
                else if (state[0] == TEXT_START && offset[2] < 0)
                    offset[2] = pos;
            }
        };        

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parseMember(Reader reader, final String name, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;
        // close tag of 'A'
        final int A_CLOSE = 2;
        // PRE close tag after the A_CLOSE
        final int PRE_CLOSE = 3;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == PRE_CLOSE){
                        hrPos = pos;
                    }
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name.equals(attrName)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                    } else {
                        if (state[0] == PRE_CLOSE && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } else if (t == HTML.Tag.DD && state[0] == PRE_CLOSE && offset[0] < 0){
                    offset[0] = pos;
                }

            }

            public void handleEndTag(HTML.Tag t, int pos){
                if (t == HTML.Tag.A && state[0] == A_OPEN){
                    state[0] = A_CLOSE;
                } else if (t == HTML.Tag.PRE && state[0] == A_CLOSE){
                    state[0] = PRE_CLOSE;
                }
            }

        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parsePackage(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final String name = "package_description"; //NOI18N
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == A_OPEN){
                        hrPos = pos;
                        offset[1] = pos;
                    }
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name.equals(attrName)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                        offset[0] = pos;
                    } else {
                        if (state[0] == A_OPEN && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } 
            }
        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }
    
}
