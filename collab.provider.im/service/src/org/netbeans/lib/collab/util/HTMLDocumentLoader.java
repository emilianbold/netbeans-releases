/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.collab.util;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
* Html Document Loader for optimized parsing and loading of Html files and url's
* TODO -- need to thread loading of document
 *
 */
public class HTMLDocumentLoader {

    static public ImageDictionary _dic = new ImageDictionary();
    static final String IMAGE_CACHE_PROPERTY = "imageCache";


    public HTMLDocument loadDocument(HTMLDocument doc,
                                     URL url, String charSet)
        throws IOException {
            doc.putProperty(Document.StreamDescriptionProperty, url);

            InputStream in = null;
            boolean ignoreCharSet = false;

            for (;;) {
                try {
                    doc.remove(0, doc.getLength());
                    URLConnection urlc = url.openConnection();
                    in = urlc.getInputStream();
                    Reader reader = (charSet == null) ?
                        new InputStreamReader(in) :
                        new InputStreamReader(in, charSet);

                    HTMLEditorKit.Parser parser = getParser();
                    HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);
                    parser.parse(reader, htmlReader, ignoreCharSet);
                    htmlReader.flush();
                    in.close();
                    break;
                } catch (BadLocationException ex) {
                    throw new IOException(ex.getMessage());
                } catch (ChangedCharSetException e) {
                    // The character set has changed - restart
                    charSet = getNewCharSet(e);
                    // Prevent recursion by suppressing further exceptions
                    ignoreCharSet = true;
                    in.close();
                }
            }

            return doc;
    }


    /**
     *
     *
     * @param
     */
    public void setImageCache(ImageDictionary d){
        _dic = d;
    }


    /**
     *
     *
     * @param
     */
    public HTMLDocument loadDocument(URL url, String charSet)
        throws IOException {
            return loadDocument((HTMLDocument)kit.createDefaultDocument(), url, charSet);
    }


    /**
     * Returns a new HtmlDoc with the given URL inserted and rendered into the new doc
     * @param URL url
     */
    public HTMLDocument loadDocument(URL url) throws IOException {
        return loadDocument(url, null);
    }


    /**
     * Returns a new HtmlDoc with the given text inserted and rendered into the new doc
     * @param String text
     */
    public HTMLDocument loadDocument(String text) throws IOException {
        return loadDocument((HTMLDocument)kit.createDefaultDocument(), text, true);
    }


    /**
     * Returns a new HtmlDoc with the given text inserted and rendered into the given doc
     * if the boolean parameter use_cache == true, any images will be flushed if reloaded
     * @param HTMLDocument doc, String text, boolean use_cache
     */
    public HTMLDocument loadDocument(HTMLDocument doc, String text, boolean use_cache) throws IOException {
        if(use_cache && _dic != null)
            doc.putProperty(IMAGE_CACHE_PROPERTY, _dic);

        boolean ignoreCharSet = false;
        for (;;) {
            try {
                StringReader in = new StringReader(text);
                BufferedReader bufin = new BufferedReader(in);
                HTMLEditorKit.Parser parser = getParser();
                HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);
                parser.parse(bufin, htmlReader, ignoreCharSet);
                htmlReader.flush();
                bufin.close();
                in.close();
                break;
            } catch (BadLocationException ex) {
                throw new IOException(ex.getMessage());
            } catch (ChangedCharSetException e) {
                System.out.println("loadDocument:"+e);
                // The character set has changed - restart
                //charSet = getNewCharSet(e);
                // Prevent recursion by suppressing further exceptions
                //Aqueel - 550108
                ignoreCharSet = true;
                // Close original input stream
                //in.close();
            }
        }

        return doc;
    }


    /**
     * Inserts a string of HTML into the document at the given position.
     * parent is used to identify the tag to look for in
     * html (unless insertTag, in which case it
     * is used). If parent is a leaf this can have
     * unexpected results.
     */
    public void insertHTML(HTMLDocument doc, Element parent,
                           int offset, String html,
                           HTML.Tag insertTag) throws BadLocationException, IOException 
    {
        if (parent != null && html != null) {
            // Determine the tag we are to look for in html.
            Object name = (insertTag != null) ? insertTag :
                parent.getAttributes().getAttribute
                (StyleConstants.NameAttribute);
            HTMLEditorKit.Parser parser = getParser();
            
            if (parser != null && name != null && (name instanceof HTML.Tag)) {
                int lastOffset = Math.max(0, offset - 1);
                Element charElement = doc.getCharacterElement(lastOffset);
                Element commonParent = parent;
                int pop = 0;
                int push = 0;
                
                if (parent.getStartOffset() > lastOffset) {
                    while (commonParent != null &&
                           commonParent.getStartOffset() > lastOffset) {
                        commonParent = commonParent.getParentElement();
                        push++;
                    }
                    if (commonParent == null) {
                        throw new BadLocationException("No common parent",
                                                       offset);
                    }
                }
                while (charElement != null && charElement != commonParent) {
                    pop++;
                    charElement = charElement.getParentElement();
                }
                if (charElement != null) {
                    // Found it, do the insert.
                    
                    HTMLEditorKit.ParserCallback callback = doc.getReader
                        (offset, pop - 1, push, (HTML.Tag)name);
                    //(insertTag != null));
                    
                    parser.parse(new StringReader(html), callback, true);
                    callback.flush();
                }
            }
        }
    }


    /*public void insertHTML(HTMLDocument doc, Element parent, int offset, String html,
      boolean wantsTrailingNewline)
      throws BadLocationException, IOException {
      if (parent != null && html != null) {
      HTMLEditorKit.Parser parser = getParser();
      if (parser != null) {
      int lastOffset = Math.max(0, offset - 1);
      Element charElement = doc.getCharacterElement(lastOffset);
      Element commonParent = parent;
      int pop = 0;
      int push = 0;

      if (parent.getStartOffset() > lastOffset) {
      while (commonParent != null &&
      commonParent.getStartOffset() > lastOffset) {
      commonParent = commonParent.getParentElement();
      push++;
      }
      if (commonParent == null) {
      throw new BadLocationException("No common parent",
      offset);
      }
      }
      while (charElement != null && charElement != commonParent) {
      pop++;
      charElement = charElement.getParentElement();
      }
      if (charElement != null) {
      // Found it, do the insert.
      HTMLReader reader = doc.getReader(offset, pop - 1, push, null);
      HTMLReader reader = new HTMLReader(offset, pop - 1, push,
      null, false, true,
      wantsTrailingNewline);

      parser.parse(new StringReader(html), reader, true);
      reader.flush();
      }
      }
      }
      }
      */

    // Methods that allow customization of the parser and the callback
    public synchronized HTMLEditorKit.Parser getParser() {
        if (parser == null) {
            try {
                Class c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
                parser = (HTMLEditorKit.Parser)c.newInstance();
            } catch (Throwable e) {
            }
        }
        return parser;
    }


    /**
     * Returns the parser callback for a given html doc
     * @param HTMLDocument doc
     */
    public synchronized HTMLEditorKit.ParserCallback getParserCallback(HTMLDocument doc) {
        return doc.getReader(0);
    }


    /**
     * Returns a String id for a new Character Set
     * If none is found, a guess ("8859_1") will be returned
     * The event contains the content type
     * plus ";" plus qualifiers which may
     * contain a "charset" directive.
     * @param ChangedCharSetException e
     */
    protected String getNewCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) return spec;

        //First remove the content type.
        int index = spec.indexOf(";");
        if (index != -1) {
            spec = spec.substring(index + 1);
        }

        spec = spec.toLowerCase();
            StringTokenizer st = new StringTokenizer(spec, " \t=", true);
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) {
                continue;
            }
            if (foundCharSet == false &&
                foundEquals == false &&
                token.equals("charset")) {
                foundCharSet = true;
                continue;
            } else if (foundEquals == false &&
                       token.equals("=")) {
                foundEquals = true;
                continue;
            } else if (foundEquals == true &&
                       foundCharSet == true) {
                return token;
            }

            // Not recognized
            foundCharSet = false;
            foundEquals = false;
        }

        // No charset found - return a guess
        return "8859_1";
    }

    protected static HTMLEditorKit kit;
    protected static HTMLEditorKit.Parser parser;

    static {
        kit = new HTMLEditorKit();
    }
}
