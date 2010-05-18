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

package org.netbeans.modules.visualweb.insync.faces;


import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.apache.xml.serialize.HTMLdtd;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Class managing XHTML entity lookups
 *
 * @author Tor Norbye
 */
public final class Entities {

    private Entities() {}


// <move from designer/markup>
    public static String expandHtmlEntities(String html) {
        return expandHtmlEntities(html, true, null);
    }

    public static String expandHtmlEntities(String html, boolean warn) {
        return expandHtmlEntities(html, warn, null);
    }

    public static String expandHtmlEntities(String html, boolean warn, Node node) {
        FileObject fileObject = null;
        int lineNumber = -1;
        if (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                node = node.getParentNode();
            }

            Element element = MarkupService.getCorrespondingSourceElement((Element)node);

            if (element != null) {
                Document doc = element.getOwnerDocument();
                // <markup_separation>
//                    MarkupUnit unit = doc.getMarkup();
//                    if (unit != null) {
//                        fileObject = unit.getFileObject();
//                        lineNumber = unit.computeLine(element);
//                    }
                // ====
                fileObject = InSyncServiceProvider.get().getFileObject(doc);
                lineNumber = InSyncServiceProvider.get().computeLine(doc, element);
                // </markup_seaparation>
            }
        }

        return getExpandedString(html, warn, fileObject, lineNumber);
    }
// </move from designer/markup>

    /** <em>Note:</em> This comment was before for jspxToHtml which impl
     * was calling this expand method, I am not sure it the comment is still r
     * <p>
     * Translate a JSPX string to an XHTML string:
     * e.g. "&amp;nbsp;" will translate to "&nbsp;".
     * In other words, it expands the standard 5 XML entities
     * and leaves everything else in place.
     * @param jspx The string to be converted to xhtml. Note that this
     *   string should ONLY represent jspx "text nodes", not stuff like
     *   "<jsp:root...".
     * @param warn If true, emit warnings to the output window if
     *   problems are encountered, like illegal entities.
     * @return An xhtml representation of the jspx string

     * This is an over-simplification. Here's the full sequence
     * Mark Roth sent me:


Well the entire transformation is to just run the JSP engine, so I'll point you to the JSP spec.

But I guess what you're looking for is a reasonable approximation of at least the template text portion of the output.

This is off the top of my head, so I might be missing something, but to go from the XML-syntax JSP to the ASCII output looks something like this:

1. Strip all XML metainformation - that will be consumed by the
   parser and will not appear in the output.  This includes the
   XML prolog, doctypes, etc.
2. Strip all comments
3. Remove <jsp:root>
4. Ignore or handle JSP standard actions (anything in the JSP
   namespace), except for <jsp:text>.  The body of <jsp:text> is echoed
   verbatim, including all whitespace before the first character and
   after the last character in the body.
5. Strip all text nodes that are only whitespace (with the exception
   of jsp:text).
6. Any elements that are not in a tag library namespace should be echoed
   as their ASCII equivalent, including attributes and their values.
   Just output the literal values of the attributes.  Do not perform
   any escaping.  For example, if the input document has "&lt;" as
   part of an attribute value, output the literal "<" - do not
   escape it further.
7. Any template text in the body of elements should be echoed as its
   ASCII equivalent (same note as above on escaping).

If you're dealing with JSP 2.0, there are a few other things to keep in mind:

1. EL expressions in attributes and template text should be handled
   Keep in mind the escaping rules for EL expressions
2. <jsp:output> should be processed and handled
3. There might not be a <jsp:root> element

I think this can be emulated fairly simply by parsing the XML with JAXP and constructing a DOM tree.  Then, walk the DOM tree and produce an ASCII stream (not another DOM tree) by echoing the tags and their attributes as ASCII, and echoing the text nodes of the tag verbatim as per above.

Once you have the ASCII stream, parse it with JAXP to produce your final DOM.  You may get errors, and that's what you can use to identify errors in the source document.  For example, the source JSPX:

    ...<b>1 &lt; 2</b>...

should be flagged as an error if you know the target language is XHTML, since the output ASCII stream would be the following:

    ...<b>1 < 2</b>...

which is invalid XML.  The XML parser will catch this and give you a parse error which you can present to the user in the tool.  I would beef up the error message to include a suggestion for how to fix it:

   ...<b>1 &amp;lt; 2</b>...

Does this help?  Let me know if you need more details.

By the way, if you want the output to be valid XHTML, you'd want to make sure the user adds a DOCTYPE to the output.  This can be done as a child of <jsp:root> using a <![CDATA[...]]> element.

Also by the way, instead of creating a brand-new JSPX parser inside Creator, I think a better long-term strategy is to use the parser from Tomcat and just walk the tree that it produces.  The NetBeans team is already talking to the Tomcat team about exposing a more formal contract for the parser APIs. 

    * @param jspx The jspx string to be converted to xhtml
    * @param warn Whether to interactively warn the user about errors
    * @param unit Corresponding markup unit, if any. Can be null, but must not
    *    be null when element is not null.
    * @param element If not null, points to the nearest element to the error
    *    which can be used to produce a clickable error line
    */
    private static String getExpandedString(String unexpanded, boolean warn, FileObject fileObject, int lineNumber) {
        if (unexpanded.indexOf('&') == -1) { // todo: keep index and copy up to it below
            return unexpanded;
        }
        int n = unexpanded.length();
        int nm1 = n-1;
        
        // IMPORTANT NOTE: Keeps this code in sync with getJspxOffset below!
        
        StringBuffer sb = new StringBuffer(n);
        for (int i = 0; i < n; i++) {
            char c = unexpanded.charAt(i);
            if (c == '&' && i < nm1) {
                // Locate entity
                int begin = i+1;
                int end = begin;
                while (end < n && unexpanded.charAt(end) != ';' && 
                       (end-begin <= 10)) { // longest entity is 8 chars
                    end++;
                }
                if (end == n || unexpanded.charAt(end) != ';') {
                    if (warn) {
                        String message = NbBundle.getMessage(Entities.class, "LBL_IllegalEntity", unexpanded.substring(begin-1));
                        if (fileObject != null && lineNumber > -1) {
                            InSyncServiceProvider.get().getRaveErrorHandler().displayErrorForFileObject(message, fileObject, lineNumber, 0);
                        } else {
                            InSyncServiceProvider.get().getRaveErrorHandler().displayError(message);
                        }
                    }
                    sb.append('&'); // browsers show the &
                    continue;
                }
                String entity = unexpanded.substring(begin, end);
                int e = expand(entity);
                if (e == -1) {
                    if (warn) {
                        String message = NbBundle.getMessage(Entities.class, "LBL_NoSuchEntity", entity);
                        if (fileObject != null && lineNumber > -1) {
                            InSyncServiceProvider.get().getRaveErrorHandler().displayErrorForFileObject(message, fileObject, lineNumber, 0);
                        } else {
                            InSyncServiceProvider.get().getRaveErrorHandler().displayError(message);
                        }
                    }
                    sb.append('&'); // browsers show the &
                    continue;
                } else {
                    sb.append((char)e);
                    i = end;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Computes the position in the unexpanded string
     * which corresponds to the given offset (expandedOffset) in the
     * expanded string. 
     * @param unexpanded unexpanded string
     * @param expanededOffset offset in the corresponding expanded string
     * @return offset in the unexpanded string
     */
     public static int getUnexpandedOffset(String unexpanded, int expandedOffset) {

         // IMPORTANT NOTE: Keep this code in sync with the expand method above
         
         int n = unexpanded.length();
         int nm1 = n-1;
         int offset = 0;
         for (int i = 0; i < n; i++) {
             if (offset == expandedOffset) {
                 return i;
             }
             char c = unexpanded.charAt(i);
             if (c == '&' && i < nm1) {
                 // Locate entity
                 int begin = i+1;
                 int end = begin;
                 while (end < n && unexpanded.charAt(end) != ';' && 
                        (end-begin <= 10)) { // longest entity is 8 chars
                     end++;
                 }
                 if (end == n || unexpanded.charAt(end) != ';') {
                     offset++; // for & displayed by browsers
                     continue;
                 }
                 String entity = unexpanded.substring(begin, end);
                 int e = expand(entity);
                 if (e == -1) {
                     offset++; // for & displayed by browsers
                     continue;
                 } else {
                     offset++;
                     i = end;
                 }
             } else {
                 offset++;
             }
         }
         return n;
     }

    /**
     * Computes the position in the expanded string
     * which corresponds to the given offset (unexpandedOffset) in the
     * unexpanded string.
     * @param unexpanded unexpanded string
     * @param unexpanededOffset offset in the unexpanded string
     * @return offset in the corresponding expanded string
     */
     public static int getExpandedOffset(String unexpanded, int unexpandedOffset) {

         // IMPORTANT NOTE: Keep this code in sync with the method above
         
         int n = unexpanded.length();
         int nm1 = n-1;
         int offset = 0;
         if (unexpandedOffset > n) {
             unexpandedOffset = n;
         }
         for (int i = 0; i < unexpandedOffset; i++) {
             char c = unexpanded.charAt(i);
             if (c == '&' && i < nm1) {
                 // Locate entity
                 int begin = i+1;
                 int end = begin;
                 while (end < n && unexpanded.charAt(end) != ';' && 
                        (end-begin <= 10)) { // longest entity is 8 chars
                     end++;
                 }
                 if (end == n || unexpanded.charAt(end) != ';') {
                     offset++; // for & displayed by browsers
                     continue;
                 }
                 String entity = unexpanded.substring(begin, end);
                 int e = expand(entity);
                 if (e == -1) {
                     offset++; // for & displayed by browsers
                     continue;
                 } else {
                     offset++;
                     i = end;
                 }
             } else {
                 offset++;
             }
         }
         return offset;
     }        
         
    /** Look up the given entity and return the corresponding
     * character. It actually returns an int that can be cast
     * to a char, but the return value may be -1 to indicate that
     * the entity is unknown.
     */
    private static int expand(String entity) {
        if (entity.length() > 0) {
            if (entity.charAt(0) == '#') {
                // TODO - do I have to map the parsed integer to
                // the ISO 10646 decimal character set, or is unicode
                // a subset of that already?
                return parseNumericReference(entity);
            } else {
                //return v.get(entity);
                return HTMLdtd.charFromName(entity);
            }
        }
        return -1;
    }

    /** Very similar to Integer.parseInt (based on it), but doesn't
     * panic when number strings have a non-digit suffix - for example
     * calling it with "5;" would return 5, not throw NumberFormatException.
     * It also decides whether the number is hex or decimal, and skips
     * the leading numeric reference character.
     * Example: called with "#500" would return 500, called with
     * "#x80" would return 128.  
     * The string MUST begin with '#'.  Also, negative numbers are not allowed.
     */
    private static int parseNumericReference(String s) {
        // See HTML4 spec section 5.3.1
        int radix = 10;
        if (s == null) {
            return 0;
        }

        int i = 0, max = s.length();
        if (max <= 1) {
            return -1;
        }
        assert s.charAt(0) == '#';
        if (s.charAt(1) == 'x' || s.charAt(1) == 'X') {
            radix = 16;
            i = 2;
        } else {
            i = 1;
        }

        int result = 0;
        int limit = -Integer.MAX_VALUE;
        int digit;
        int multmin = limit / radix;
        if (i < max) {
            digit = Character.digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -result;
            } else {
                result = -digit;
            }
        }
        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = Character.digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -result;
            }
            if (result < multmin) {
                return -result;
            }
            result *= radix;
            if (result < limit + digit) {
                return -result;
            }
            result -= digit;
        }
        done: return -result;
    }

    /* Now using Xerces' HTMLdtd.charFromName instead

    // I generated the following code by creating a DTD for the various
    // character entities including in XHTML, and then I ran the following
    //command line on Solaris to generate the below declarations:
    // cat entities.dtd | /bin/grep ENTITY | /bin/grep -v "ENTITY % " | tr -d '"&#;>' | /bin/nawk '{  printf "    v.put(\"%s\", \\u%04x); // NOI18N\n", $2, $3 }'  
    // "wc" on the output tells us that there are 253 entities.
    // There's a bug in the above script - it was missing escaped ''s around
    // the unicode chars - so I used an emacs macro to insert those below.

    // Note that the "apos" entity had to have special treatment below
    // since javac will substitute unicode chars before resuming, so it
    // saw the 0027 declaration as the closing apostrophy in the character
    // literal.

    // Note also that the above script did NOT handle two entities: "amp"
    // and "lt" since these specify multiple mappings, and the above script
    // just smushed the digits together and computed the resulting wrong 
    // number.
    // <!ENTITY amp     "&#38;#38;"> <!--  ampersand, U+0026 ISOnum -->
    // <!ENTITY lt      "&#38;#60;"> <!--  less-than sign, U+003C ISOnum -->
    // So these were fixed by hand. There are only two such instances in
    // the DTD.

    private final static StringIntMap v = 
        new StringIntMap(255); // 253 actually, see above
    static {
        v.put("fnof", '\u0192'); // NOI18N
        v.put("Alpha", '\u0391'); // NOI18N
        v.put("Beta", '\u0392'); // NOI18N
        v.put("Gamma", '\u0393'); // NOI18N
        v.put("Delta", '\u0394'); // NOI18N
        v.put("Epsilon", '\u0395'); // NOI18N
        v.put("Zeta", '\u0396'); // NOI18N
        v.put("Eta", '\u0397'); // NOI18N
        v.put("Theta", '\u0398'); // NOI18N
        v.put("Iota", '\u0399'); // NOI18N
        v.put("Kappa", '\u039a'); // NOI18N
        v.put("Lambda", '\u039b'); // NOI18N
        v.put("Mu", '\u039c'); // NOI18N
        v.put("Nu", '\u039d'); // NOI18N
        v.put("Xi", '\u039e'); // NOI18N
        v.put("Omicron", '\u039f'); // NOI18N
        v.put("Pi", '\u03a0'); // NOI18N
        v.put("Rho", '\u03a1'); // NOI18N
        v.put("Sigma", '\u03a3'); // NOI18N
        v.put("Tau", '\u03a4'); // NOI18N
        v.put("Upsilon", '\u03a5'); // NOI18N
        v.put("Phi", '\u03a6'); // NOI18N
        v.put("Chi", '\u03a7'); // NOI18N
        v.put("Psi", '\u03a8'); // NOI18N
        v.put("Omega", '\u03a9'); // NOI18N
        v.put("alpha", '\u03b1'); // NOI18N
        v.put("beta", '\u03b2'); // NOI18N
        v.put("gamma", '\u03b3'); // NOI18N
        v.put("delta", '\u03b4'); // NOI18N
        v.put("epsilon", '\u03b5'); // NOI18N
        v.put("zeta", '\u03b6'); // NOI18N
        v.put("eta", '\u03b7'); // NOI18N
        v.put("theta", '\u03b8'); // NOI18N
        v.put("iota", '\u03b9'); // NOI18N
        v.put("kappa", '\u03ba'); // NOI18N
        v.put("lambda", '\u03bb'); // NOI18N
        v.put("mu", '\u03bc'); // NOI18N
        v.put("nu", '\u03bd'); // NOI18N
        v.put("xi", '\u03be'); // NOI18N
        v.put("omicron", '\u03bf'); // NOI18N
        v.put("pi", '\u03c0'); // NOI18N
        v.put("rho", '\u03c1'); // NOI18N
        v.put("sigmaf", '\u03c2'); // NOI18N
        v.put("sigma", '\u03c3'); // NOI18N
        v.put("tau", '\u03c4'); // NOI18N
        v.put("upsilon", '\u03c5'); // NOI18N
        v.put("phi", '\u03c6'); // NOI18N
        v.put("chi", '\u03c7'); // NOI18N
        v.put("psi", '\u03c8'); // NOI18N
        v.put("omega", '\u03c9'); // NOI18N
        v.put("thetasym", '\u03d1'); // NOI18N
        v.put("upsih", '\u03d2'); // NOI18N
        v.put("piv", '\u03d6'); // NOI18N
        v.put("bull", '\u2022'); // NOI18N
        v.put("hellip", '\u2026'); // NOI18N
        v.put("prime", '\u2032'); // NOI18N
        v.put("Prime", '\u2033'); // NOI18N
        v.put("oline", '\u203e'); // NOI18N
        v.put("frasl", '\u2044'); // NOI18N
        v.put("weierp", '\u2118'); // NOI18N
        v.put("image", '\u2111'); // NOI18N
        v.put("real", '\u211c'); // NOI18N
        v.put("trade", '\u2122'); // NOI18N
        v.put("alefsym", '\u2135'); // NOI18N
        v.put("larr", '\u2190'); // NOI18N
        v.put("uarr", '\u2191'); // NOI18N
        v.put("rarr", '\u2192'); // NOI18N
        v.put("darr", '\u2193'); // NOI18N
        v.put("harr", '\u2194'); // NOI18N
        v.put("crarr", '\u21b5'); // NOI18N
        v.put("lArr", '\u21d0'); // NOI18N
        v.put("uArr", '\u21d1'); // NOI18N
        v.put("rArr", '\u21d2'); // NOI18N
        v.put("dArr", '\u21d3'); // NOI18N
        v.put("hArr", '\u21d4'); // NOI18N
        v.put("forall", '\u2200'); // NOI18N
        v.put("part", '\u2202'); // NOI18N
        v.put("exist", '\u2203'); // NOI18N
        v.put("empty", '\u2205'); // NOI18N
        v.put("nabla", '\u2207'); // NOI18N
        v.put("isin", '\u2208'); // NOI18N
        v.put("notin", '\u2209'); // NOI18N
        v.put("ni", '\u220b'); // NOI18N
        v.put("prod", '\u220f'); // NOI18N
        v.put("sum", '\u2211'); // NOI18N
        v.put("minus", '\u2212'); // NOI18N
        v.put("lowast", '\u2217'); // NOI18N
        v.put("radic", '\u221a'); // NOI18N
        v.put("prop", '\u221d'); // NOI18N
        v.put("infin", '\u221e'); // NOI18N
        v.put("ang", '\u2220'); // NOI18N
        v.put("and", '\u2227'); // NOI18N
        v.put("or", '\u2228'); // NOI18N
        v.put("cap", '\u2229'); // NOI18N
        v.put("cup", '\u222a'); // NOI18N
        v.put("int", '\u222b'); // NOI18N
        v.put("there4", '\u2234'); // NOI18N
        v.put("sim", '\u223c'); // NOI18N
        v.put("cong", '\u2245'); // NOI18N
        v.put("asymp", '\u2248'); // NOI18N
        v.put("ne", '\u2260'); // NOI18N
        v.put("equiv", '\u2261'); // NOI18N
        v.put("le", '\u2264'); // NOI18N
        v.put("ge", '\u2265'); // NOI18N
        v.put("sub", '\u2282'); // NOI18N
        v.put("sup", '\u2283'); // NOI18N
        v.put("nsub", '\u2284'); // NOI18N
        v.put("sube", '\u2286'); // NOI18N
        v.put("supe", '\u2287'); // NOI18N
        v.put("oplus", '\u2295'); // NOI18N
        v.put("otimes", '\u2297'); // NOI18N
        v.put("perp", '\u22a5'); // NOI18N
        v.put("sdot", '\u22c5'); // NOI18N
        v.put("lceil", '\u2308'); // NOI18N
        v.put("rceil", '\u2309'); // NOI18N
        v.put("lfloor", '\u230a'); // NOI18N
        v.put("rfloor", '\u230b'); // NOI18N
        v.put("lang", '\u2329'); // NOI18N
        v.put("rang", '\u232a'); // NOI18N
        v.put("loz", '\u25ca'); // NOI18N
        v.put("spades", '\u2660'); // NOI18N
        v.put("clubs", '\u2663'); // NOI18N
        v.put("hearts", '\u2665'); // NOI18N
        v.put("diams", '\u2666'); // NOI18N
        v.put("quot", '\u0022'); // NOI18N

        // These two items were handled specially because
        // their DTD entries were unusual. Looks like the unicode
        // chars are \u0026 and \u003c.
        v.put("amp", '&'); // NOI18N
        v.put("lt", '<'); // NOI18N


        v.put("gt", '\u003e'); // NOI18N

        // This won't compile
        // v.put("apos", '\u0027'); // NOI18N
        // so just handle it specially
        v.put("apos", '\''); // NOI18N


        v.put("OElig", '\u0152'); // NOI18N
        v.put("oelig", '\u0153'); // NOI18N
        v.put("Scaron", '\u0160'); // NOI18N
        v.put("scaron", '\u0161'); // NOI18N
        v.put("Yuml", '\u0178'); // NOI18N
        v.put("circ", '\u02c6'); // NOI18N
        v.put("tilde", '\u02dc'); // NOI18N
        v.put("ensp", '\u2002'); // NOI18N
        v.put("emsp", '\u2003'); // NOI18N
        v.put("thinsp", '\u2009'); // NOI18N
        v.put("zwnj", '\u200c'); // NOI18N
        v.put("zwj", '\u200d'); // NOI18N
        v.put("lrm", '\u200e'); // NOI18N
        v.put("rlm", '\u200f'); // NOI18N
        v.put("ndash", '\u2013'); // NOI18N
        v.put("mdash", '\u2014'); // NOI18N
        v.put("lsquo", '\u2018'); // NOI18N
        v.put("rsquo", '\u2019'); // NOI18N
        v.put("sbquo", '\u201a'); // NOI18N
        v.put("ldquo", '\u201c'); // NOI18N
        v.put("rdquo", '\u201d'); // NOI18N
        v.put("bdquo", '\u201e'); // NOI18N
        v.put("dagger", '\u2020'); // NOI18N
        v.put("Dagger", '\u2021'); // NOI18N
        v.put("permil", '\u2030'); // NOI18N
        v.put("lsaquo", '\u2039'); // NOI18N
        v.put("rsaquo", '\u203a'); // NOI18N
        v.put("euro", '\u20ac'); // NOI18N
        v.put("nbsp", '\u00a0'); // NOI18N
        v.put("iexcl", '\u00a1'); // NOI18N
        v.put("cent", '\u00a2'); // NOI18N
        v.put("pound", '\u00a3'); // NOI18N
        v.put("curren", '\u00a4'); // NOI18N
        v.put("yen", '\u00a5'); // NOI18N
        v.put("brvbar", '\u00a6'); // NOI18N
        v.put("sect", '\u00a7'); // NOI18N
        v.put("uml", '\u00a8'); // NOI18N
        v.put("copy", '\u00a9'); // NOI18N
        v.put("ordf", '\u00aa'); // NOI18N
        v.put("laquo", '\u00ab'); // NOI18N
        v.put("not", '\u00ac'); // NOI18N
        v.put("shy", '\u00ad'); // NOI18N
        v.put("reg", '\u00ae'); // NOI18N
        v.put("macr", '\u00af'); // NOI18N
        v.put("deg", '\u00b0'); // NOI18N
        v.put("plusmn", '\u00b1'); // NOI18N
        v.put("sup2", '\u00b2'); // NOI18N
        v.put("sup3", '\u00b3'); // NOI18N
        v.put("acute", '\u00b4'); // NOI18N
        v.put("micro", '\u00b5'); // NOI18N
        v.put("para", '\u00b6'); // NOI18N
        v.put("middot", '\u00b7'); // NOI18N
        v.put("cedil", '\u00b8'); // NOI18N
        v.put("sup1", '\u00b9'); // NOI18N
        v.put("ordm", '\u00ba'); // NOI18N
        v.put("raquo", '\u00bb'); // NOI18N
        v.put("frac14", '\u00bc'); // NOI18N
        v.put("frac12", '\u00bd'); // NOI18N
        v.put("frac34", '\u00be'); // NOI18N
        v.put("iquest", '\u00bf'); // NOI18N
        v.put("Agrave", '\u00c0'); // NOI18N
        v.put("Aacute", '\u00c1'); // NOI18N
        v.put("Acirc", '\u00c2'); // NOI18N
        v.put("Atilde", '\u00c3'); // NOI18N
        v.put("Auml", '\u00c4'); // NOI18N
        v.put("Aring", '\u00c5'); // NOI18N
        v.put("AElig", '\u00c6'); // NOI18N
        v.put("Ccedil", '\u00c7'); // NOI18N
        v.put("Egrave", '\u00c8'); // NOI18N
        v.put("Eacute", '\u00c9'); // NOI18N
        v.put("Ecirc", '\u00ca'); // NOI18N
        v.put("Euml", '\u00cb'); // NOI18N
        v.put("Igrave", '\u00cc'); // NOI18N
        v.put("Iacute", '\u00cd'); // NOI18N
        v.put("Icirc", '\u00ce'); // NOI18N
        v.put("Iuml", '\u00cf'); // NOI18N
        v.put("ETH", '\u00d0'); // NOI18N
        v.put("Ntilde", '\u00d1'); // NOI18N
        v.put("Ograve", '\u00d2'); // NOI18N
        v.put("Oacute", '\u00d3'); // NOI18N
        v.put("Ocirc", '\u00d4'); // NOI18N
        v.put("Otilde", '\u00d5'); // NOI18N
        v.put("Ouml", '\u00d6'); // NOI18N
        v.put("times", '\u00d7'); // NOI18N
        v.put("Oslash", '\u00d8'); // NOI18N
        v.put("Ugrave", '\u00d9'); // NOI18N
        v.put("Uacute", '\u00da'); // NOI18N
        v.put("Ucirc", '\u00db'); // NOI18N
        v.put("Uuml", '\u00dc'); // NOI18N
        v.put("Yacute", '\u00dd'); // NOI18N
        v.put("THORN", '\u00de'); // NOI18N
        v.put("szlig", '\u00df'); // NOI18N
        v.put("agrave", '\u00e0'); // NOI18N
        v.put("aacute", '\u00e1'); // NOI18N
        v.put("acirc", '\u00e2'); // NOI18N
        v.put("atilde", '\u00e3'); // NOI18N
        v.put("auml", '\u00e4'); // NOI18N
        v.put("aring", '\u00e5'); // NOI18N
        v.put("aelig", '\u00e6'); // NOI18N
        v.put("ccedil", '\u00e7'); // NOI18N
        v.put("egrave", '\u00e8'); // NOI18N
        v.put("eacute", '\u00e9'); // NOI18N
        v.put("ecirc", '\u00ea'); // NOI18N
        v.put("euml", '\u00eb'); // NOI18N
        v.put("igrave", '\u00ec'); // NOI18N
        v.put("iacute", '\u00ed'); // NOI18N
        v.put("icirc", '\u00ee'); // NOI18N
        v.put("iuml", '\u00ef'); // NOI18N
        v.put("eth", '\u00f0'); // NOI18N
        v.put("ntilde", '\u00f1'); // NOI18N
        v.put("ograve", '\u00f2'); // NOI18N
        v.put("oacute", '\u00f3'); // NOI18N
        v.put("ocirc", '\u00f4'); // NOI18N
        v.put("otilde", '\u00f5'); // NOI18N
        v.put("ouml", '\u00f6'); // NOI18N
        v.put("divide", '\u00f7'); // NOI18N
        v.put("oslash", '\u00f8'); // NOI18N
        v.put("ugrave", '\u00f9'); // NOI18N
        v.put("uacute", '\u00fa'); // NOI18N
        v.put("ucirc", '\u00fb'); // NOI18N
        v.put("uuml", '\u00fc'); // NOI18N
        v.put("yacute", '\u00fd'); // NOI18N
        v.put("thorn", '\u00fe'); // NOI18N
        v.put("yuml", '\u00ff'); // NOI18N
    }
    */
}
