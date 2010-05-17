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

package org.netbeans.modules.visualweb.designer.html;


/**
 * List of html tags the designer needs to be aware of.
 * @todo Get rid of this class. This info should be derived from
 *   CSS: display: block, display: inline, display:none set up
 * info about whether a tag is block, inline, or hidden.
 * That only leaves "replaced", and for that we know exactly
 * which elements are - image, object/applet, iframe, input,
 * select, and text area.
 * Implementation note: Don't change the strings in this table to
 * something arbitrary: in particular, a lot of code will check the
 * first character for something hardcoded, then do an equals comparison
 * on HtmlTag.FOO.name so this will break if you change say
 * HtmlTag.BODY="body" to HtmlTag.BODY="mybody".
 *
 * @todo Clean up - we shouldn't need to know about all these tags;
 *  for example, block vs inline should be determined via the css
 *  display property, and as another example, we shouldn't know anything
 *  about the "center" tag - the default style sheet will set up this
 *  tag with a "text-alignment: center" css property.
 * @todo Remove the block/inline flag: it's now pulled from the CSS file instead!
 * @author Tor Norbye
 */
final public class HtmlTag {

    /**
     * Creates a new <code>Tag</code> with the specified <code>id</code>;
     * <code>causesBreak</code> and <code>isBlock</code> are defined
     * by the user.
     *
     * @param name the name of the new tag, such as "table" or "blink"
     * @param block <code>true</code> if the tag is used
     *    to add structure to a document
     * @param hidden <code>true</code> if the tag should be hidden in
     *    the visual designer
     * @param replaced <code>true</code> if the tag is a replaced
     * element. See isReplaced() for a description of replaced elements.
     * @todo Get rid of the inline parameter!
     */
    protected HtmlTag(String name, boolean block, boolean hidden, boolean replaced) {
        this.name = name;
        this.block = block;
        this.hidden = hidden;
        this.replaced = replaced;
    }
    
    /** Return the name of this element/tag */
    public String getTagName() {
        return name;
    }

    /**
     * Returns <code>true</code> if this tag is a block
     * tag, which is a tag used to add structure to a
     * document.
     * 
     * @return <code>true</code> if this tag is a block
     *   tag, otherwise returns <code>false</code>
     */
    public boolean isBlockTag() {
        return block;
    }

    /**
     * Returns <code>true</code> if this tag is an inline
     * tag, which is a tag used to add content to lines.
     * 
     * @return <code>true</code> if this tag is an inline
     *   tag, otherwise returns <code>false</code>
     */
    public boolean isInlineTag() {
        return !block;
    }

    /**
     * Is this a tag which should be completely hidden?
     * Returns <code>true</code> if this tag is a tag that should
     * be hidden in the webform designer - such as the script tag.
     * WARNING: Use this carefully! You cannot tell from a tag alone if it's hidden.
     *    HtmlTag.INPUT is hidden whenever @type != "hidden" for example.
     */
    public boolean isHiddenTag() {
        return hidden;
    }

    /**
     * Returns <code>true</code> if this tag represents a replaced
     * element.  A replaced element is, according to
     * http://www.w3.org/TR/REC-CSS2/conform.html, 'An element for which
     * the CSS formatter knows only the intrinsic dimensions. In HTML,
     * IMG, INPUT, TEXTAREA, SELECT, and OBJECT elements can be examples
     * of replaced elements. For example, the content of the IMG element
     * is often replaced by the image that the "src" attribute
     * designates. CSS does not define how the intrinsic dimensions are
     * found.'
     */
    public boolean isReplacedTag() {
        // In CSS3, elements with display set to inline-block or inline-table
        // will also be considered replaced. That may require some reworking
        // of how this is set up.
        return replaced;
    }
    
    /**
     * Return true iff this tag is a tag that participates in a form, such
     * as &lt;input&gt;. Note that the &lt;form&gt; tag itself is not considered
     * a form member tag.
     * @return true iff this tag is a form member tag
     */
    public boolean isFormMemberTag() {
        return this == INPUT || this == SELECT || this == TEXTAREA;
    }

    /**
     * Returns the string representation of the
     * tag.
     *
     * @return the <code>String</code> representation of the tag
     */
    public String toString() {
        return name;
    }

    public final String name;
    boolean block;
    boolean hidden;
    boolean replaced;

    // HTML 4.0 tags

    // TODO Complete this list by looking up official
    // html4.0 & xhtml specs




    // Structure & header: <body>, <html>, <title>, <base>, <link>,
    // <meta>, <script>, <style>    -- none of these should be shown

    // Inline tags: <a>, <abbr>, <acronym>, <cite>, <code>, <dfn>,
    // <em>, <kbd>, <samp>, <strong>, <var>, <b>, <big>, <i>, <small>,
    // <sub>, <sup>, <tt>, <bdo>, <br>, <button>, <del>, <ins>, <img>,
    // <input>, <label>, <map>, <noscript>, <object>, <q>, <ruby>,
    // <select>, <script>, <span>, <textarea>,  <iframe>!!!!, 
    // XXX is <script> inline or not???

    // This may clarify it, from
    // http://www.mit.edu/~ddcc/xhtmlref/inline.html#noscript_elem:
    // <noscript> is both an inline and a block-level element. But,
    // however <noscript> is used, it may contain only block-level
    // elements. This strange rule means that a <noscript> used inline
    // will assuredly make no sense structurally: in order to use
    // <noscript> inside a paragraph, for example, an author needs to
    // wrap the contents of <noscript> inside another paragraph,
    // creating a nonsensical nesting. Thus, the only sane way to use
    // <noscript> is as a block-level element.

    // XXX Still missing some tags: legend, optgroup
    // XXX Make usre I got the right values for the iframe tag
 

    // For style: 
    // Need em, strong, cite, dfn, code, samp, kbd, var, abbr, acronym
    // font,basefont (deprecated)
    // big, small, <tt>

    // name, block, hidden, replaced
    public static final HtmlTag A = 
        new HtmlTag("a", false, false, false); // NOI18N
    public static final HtmlTag ABBR = 
        new HtmlTag("abbr", false, false, false); // NOI18N
    public static final HtmlTag ACRONYM = 
        new HtmlTag("acronym", false, false, false); // NOI18N
    public static final HtmlTag ADDRESS = 
        new HtmlTag("address", true, false, false); // NOI18N
    public static final HtmlTag APPLET = 
        new HtmlTag("applet", false, false, true); // NOI18N
    public static final HtmlTag AREA = 
        new HtmlTag("area", false, true, false); // NOI18N
    public static final HtmlTag B = 
        new HtmlTag("b", false, false, false); // NOI18N
    public static final HtmlTag BASE = 
        new HtmlTag("base", false, false, false); // NOI18N
    public static final HtmlTag BASEFONT = 
        new HtmlTag("basefont", false, false, false); // NOI18N
    public static final HtmlTag BIG = 
        new HtmlTag("big", false, false, false); // NOI18N
    // XXX what about <blink> ???   :-)
    public static final HtmlTag BLOCKQUOTE = 
        new HtmlTag("blockquote", true, false, false); // NOI18N
    public static final HtmlTag BODY = 
        new HtmlTag("body", true, false, false); // NOI18N
    public static final HtmlTag BR = 
        new HtmlTag("br", false, false, false); // NOI18N
    public static final HtmlTag BUTTON = 
        new HtmlTag("button", false, false, true); // NOI18N
    public static final HtmlTag CAPTION = 
//        new HtmlTag("caption", false, false, false); // NOI18N
    // I changed the caption to be block type since I want
    // to format it on its own. This may be wrong. Revisit.
        new HtmlTag("caption", true, false, false); // NOI18N
    public static final HtmlTag CENTER = 
        new HtmlTag("center", false, false, false); // NOI18N
    public static final HtmlTag CITE = 
        new HtmlTag("cite", false, false, false); // NOI18N
    public static final HtmlTag CODE = 
        new HtmlTag("code", false, false, false); // NOI18N
    public static final HtmlTag COL = 
        new HtmlTag("col", false, false, false); // NOI18N
    public static final HtmlTag COLGROUP = 
        new HtmlTag("colgroup", false, false, false); // NOI18N
    public static final HtmlTag DD = 
        new HtmlTag("dd", true, false, false); // NOI18N
    public static final HtmlTag DEL = 
        // Note - DEL can be both block and inline!
        new HtmlTag("del", true, false, false); // NOI18N
    public static final HtmlTag DFN = 
        new HtmlTag("dfn", false, false, false); // NOI18N
    public static final HtmlTag DIR = 
        new HtmlTag("dir", true, false, false); // NOI18N
    public static final HtmlTag DIV = 
        new HtmlTag("div", true, false, false); // NOI18N
    public static final HtmlTag DL = 
        new HtmlTag("dl", true, false, false); // NOI18N
    public static final HtmlTag DT = 
        new HtmlTag("dt", true, false, false); // NOI18N
    public static final HtmlTag EM = 
        new HtmlTag("em", false, false, false); // NOI18N
    public static final HtmlTag FIELDSET = 
        // inline??? not sure
        new HtmlTag("fieldset", true, false, false); // NOI18N
    public static final HtmlTag FONT = 
        new HtmlTag("font", false, false, false); // NOI18N
    public static final HtmlTag FORM = 
        new HtmlTag("form", true, false, false); // NOI18N
    public static final HtmlTag FRAME = 
        // XXX is this replaced? IFRAME is!
        new HtmlTag("frame", false, false, false); // NOI18N
    public static final HtmlTag FRAMESET = 
        new HtmlTag("frameset", false, false, false); // NOI18N
    public static final HtmlTag H1 = 
        new HtmlTag("h1", true, false, false); // NOI18N
    public static final HtmlTag H2 = 
        new HtmlTag("h2", true, false, false); // NOI18N
    public static final HtmlTag H3 = 
        new HtmlTag("h3", true, false, false); // NOI18N
    public static final HtmlTag H4 = 
        new HtmlTag("h4", true, false, false); // NOI18N
    public static final HtmlTag H5 = 
        new HtmlTag("h5", true, false, false); // NOI18N
    public static final HtmlTag H6 = 
        new HtmlTag("h6", true, false, false); // NOI18N
    public static final HtmlTag HEAD = 
        new HtmlTag("head", false, true, false); // NOI18N
    public static final HtmlTag HR = 
        new HtmlTag("hr", true, false, false); // NOI18N
    public static final HtmlTag HTML = 
        new HtmlTag("html", false, true, false); // NOI18N
    public static final HtmlTag I = 
        new HtmlTag("i", false, false, false); // NOI18N
    // Double check what the right display disposition is for iframes!
    public static final HtmlTag IFRAME = 
        new HtmlTag("iframe", true, false, true); // NOI18N
    public static final HtmlTag IMG = 
        new HtmlTag("img", false, false, true); // NOI18N
    public static final HtmlTag INPUT = 
        new HtmlTag("input", false, false, true); // NOI18N
    public static final HtmlTag INS = 
        // Note - INS can be both block and inline!
        new HtmlTag("ins", true, false, false); // NOI18N
    public static final HtmlTag ISINDEX = 
        // Deprecated - use INPUT instead
        new HtmlTag("isindex", false, false, false); // NOI18N

    // FAKE TAG!
    // Here so we don't display these
    public static final HtmlTag JSPDECLARATION = 
        new HtmlTag("jsp:declaration", false, true, false); // NOI18N
    public static final HtmlTag JSPEXPRESSION = 
        new HtmlTag("jsp:expression", false, true, false); // NOI18N
    public static final HtmlTag JSPINCLUDE = 
        new HtmlTag("jsp:directive.include", true, false, false); // NOI18N
    // XXX #94248 Weblogic problem, supporting also this version of fragments.
    public static final HtmlTag JSPINCLUDEX = 
        new HtmlTag("jsp:include", true, false, false); // NOI18N
    public static final HtmlTag JSPSCRIPTLET = 
        new HtmlTag("jsp:scriptlet", false, true, false); // NOI18N
    public static final HtmlTag FSUBVIEW = 
        new HtmlTag("f:subview", true, false, false); // NOI18N
    
    public static final HtmlTag KBD = 
        new HtmlTag("kbd", false, false, false); // NOI18N
    public static final HtmlTag LABEL = 
        new HtmlTag("label", false, false, false); // NOI18N
    public static final HtmlTag LI = 
        new HtmlTag("li", true, false, false); // NOI18N
    public static final HtmlTag LINK = 
        new HtmlTag("link", false, true, false); // NOI18N
    public static final HtmlTag MAP = 
        new HtmlTag("map", false, true, false); // NOI18N
    public static final HtmlTag MENU = 
        new HtmlTag("menu", true, false, false); // NOI18N
    public static final HtmlTag META = 
        new HtmlTag("meta", false, true, false); // NOI18N
    public static final HtmlTag NOBR = 
        new HtmlTag("nobr", false, false, false); // NOI18N
    public static final HtmlTag NOFRAMES = 
        new HtmlTag("noframes", true, true, false); // NOI18N
    public static final HtmlTag NOSCRIPT = 
        new HtmlTag("noscript", true, true, false); // NOI18N
    public static final HtmlTag OBJECT = 
        new HtmlTag("object", false, false, true); // NOI18N
    public static final HtmlTag OL = 
        new HtmlTag("ol", true, false, false); // NOI18N
    public static final HtmlTag OPTION = 
        new HtmlTag("option", false, false, false); // NOI18N
    public static final HtmlTag P = 
        new HtmlTag("p", true, false, false); // NOI18N
    public static final HtmlTag PARAM = 
        new HtmlTag("param", false, true, false); // NOI18N
    public static final HtmlTag PRE = 
        new HtmlTag("pre", true, false, false); // NOI18N
    public static final HtmlTag Q = 
        new HtmlTag("q", false, false, false); // NOI18N
    public static final HtmlTag S = 
        new HtmlTag("s", false, false, false); // NOI18N
    public static final HtmlTag SAMP = 
        new HtmlTag("samp", false, false, false); // NOI18N
    public static final HtmlTag SCRIPT = 
        new HtmlTag("script", true, true, false); // NOI18N
    public static final HtmlTag SELECT = 
        new HtmlTag("select", false, false, true); // NOI18N
    public static final HtmlTag SMALL = 
        new HtmlTag("small", false, false, false); // NOI18N
    public static final HtmlTag SPAN = 
        new HtmlTag("span", false, false, false); // NOI18N
    public static final HtmlTag STRIKE = 
        new HtmlTag("strike", false, false, false); // NOI18N
    public static final HtmlTag STRONG = 
        new HtmlTag("strong", false, false, false); // NOI18N
    public static final HtmlTag STYLE = 
        new HtmlTag("style", false, true, false); // NOI18N
    public static final HtmlTag SUB = 
        new HtmlTag("sub", false, false, false); // NOI18N
    public static final HtmlTag SUP = 
        new HtmlTag("sup", false, false, false); // NOI18N
    public static final HtmlTag TABLE = 
        new HtmlTag("table", true, false, false); // NOI18N
    public static final HtmlTag TBODY = 
        new HtmlTag("tbody", false, false, false); // NOI18N
    public static final HtmlTag TD = 
        new HtmlTag("td", true, false, false); // NOI18N
    public static final HtmlTag TEXTAREA = 
        new HtmlTag("textarea", false, false, true); // NOI18N
    public static final HtmlTag TFOOT = 
        new HtmlTag("tfoot", false, false, false); // NOI18N
    public static final HtmlTag TH = 
        new HtmlTag("th", true, false, false); // NOI18N
    public static final HtmlTag THEAD = 
        new HtmlTag("thead", false, false, false); // NOI18N
    public static final HtmlTag TITLE = 
        new HtmlTag("title", false, true, false); // NOI18N
    public static final HtmlTag TR = 
        new HtmlTag("tr", true, false, false); // NOI18N
    public static final HtmlTag TT = 
        new HtmlTag("tt", false, false, false); // NOI18N
    public static final HtmlTag U = 
        new HtmlTag("u", false, false, false); // NOI18N
    public static final HtmlTag UL = 
        new HtmlTag("ul", true, false, false); // NOI18N
    public static final HtmlTag VAR = 
        new HtmlTag("var", false, false, false); // NOI18N

    // What about these - should they be blocktags?:
    // Table Content Elements: <caption>, <col>, 
    // <colgroup>, <thead>, <tbody>, <tfoot>, <td>, <th>, <tr>
    // Form Fieldset Legends and Menu Options
    // <legend>, <optgroup>, <option>
    // Map Areas: <area>
    // Object Parameters: <param>
    // Ruby Annotations (?): <rb>, <rbc>, <rp>, <rt>, <rtc>

    private static HtmlTag[] tags = {
        A, ABBR, ACRONYM, ADDRESS, APPLET, AREA, B, BASE, BASEFONT,
        BIG, BLOCKQUOTE, BODY, BR, BUTTON, CAPTION, CENTER, CITE, CODE, COL, 
        COLGROUP, DD, DEL, DFN, DIR, DIV, DL, DT, EM, FSUBVIEW, FIELDSET, FONT,
        FORM, FRAME, FRAMESET, H1, H2, H3, H4, H5, H6, HEAD, HR, HTML, 
        I, IFRAME, IMG, INPUT, INS, ISINDEX, JSPDECLARATION, JSPINCLUDE, JSPINCLUDEX,
        JSPEXPRESSION, JSPSCRIPTLET, KBD, LABEL, LI, LINK, MAP, MENU, 
        META, NOBR, NOFRAMES, NOSCRIPT, OBJECT, OL, OPTION, P, PARAM,
        PRE, Q, S, SAMP, SCRIPT, SELECT, SMALL, SPAN, STRIKE, STRONG, 
        STYLE, SUB, SUP, TABLE, TBODY, TD, TEXTAREA, TFOOT, TH, THEAD, 
        TITLE, TR, TT, U, UL, VAR, null
    };

//    static void ensureAlphabetical() {
//        if (!Trace.ON) {
//            return;
//        }
//        // Binsearch depends on the list being in alphabetical order....
//        // Make sure getTag can find all tags
//        for (int i = 0; tags[i] != null; i++) {
//            HtmlTag tag = getTag(tags[i].name);
//            if (tag == null) {
//                System.err.println("Can't find tag " + tags[i]);
//                System.exit(0); // Only in debug builds! (Trace.ON)
//            }
//        }
//    }
    
    /** Return the set of known tags */
    public static HtmlTag[] getTags() {
        return tags;
    }

    /** Locate a tag by name
     * @param name The tag name to search for
     * @return the tag for that name, or null if the name is not a valid
     *         html tag name.
     */
    public static HtmlTag getTag(String name) {
	// Do a binary search
	int low = 0;
	int high = tags.length-1;
//        boolean ignoreCase = Character.isUpperCase(name.charAt(0));
	while (high > low) {
	    int middle = (low+high) / 2;
	    // I can optimize this further by comparing on a per character
	    // basis, increasingly more matching characters.
	    int result = name.compareTo(tags[middle].name);
//	    int result;
//            if (ignoreCase) {
//                result = name.compareToIgnoreCase(tags[middle].name);
//            } else {
//	        result = name.compareTo(tags[middle].name);
//            }
	    if (result == 0) {
		return tags[middle];
	    } else if (result < 0) {
		high = middle;
	    } else if (low != middle) {
		low = middle;
	    } else {
                break;
            }
	}
	return null;
    }

    public static boolean isTableChild(String tag) {
        char c = tag.charAt(0);
        if (c == 't' || c == 'c') { // xxx what about rowgroups?
            return tag.equals(HtmlTag.TD.name) || 
                tag.equals(HtmlTag.TH.name) ||
                tag.equals(HtmlTag.TBODY.name) || 
                tag.equals(HtmlTag.THEAD.name) ||
                tag.equals(HtmlTag.TFOOT.name) || 
                tag.equals(HtmlTag.CAPTION.name) ||
                tag.equals(HtmlTag.COLGROUP.name) ||
                tag.equals(HtmlTag.COL.name);
        }
        return false;
    }
        
    /*
    static {
        if (Trace.ON) {
            HtmlTag.ensureAlphabetical();
        }
    }
    */

}
