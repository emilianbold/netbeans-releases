/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * HtmlTags.java
 *
 * Created on February 3, 2005, 9:16 AM
 */

package org.netbeans.modules.css.visual.model;


/**
 * List of html tags
 * @author Winston Prakash
 * @version 1.0
 */
final public class HtmlTags {

    // HTML 4.0 tags

    public static final String A = "a"; // NOI18N
    public static final String ABBR = "abbr"; // NOI18N
    public static final String ACRONYM = "acronym"; // NOI18N
    public static final String ADDRESS = "address"; // NOI18N
    public static final String APPLET = "applet"; // NOI18N
    public static final String AREA = "area"; // NOI18N
    public static final String B = "b"; // NOI18N
    public static final String BASE = "base"; // NOI18N
    public static final String BASEFONT = "basefont"; // NOI18N
    public static final String BIG = "big"; // NOI18N
    // XXX what about <blink> ???   :-)
    public static final String BLOCKQUOTE = "blockquote"; // NOI18N
    public static final String BODY = "body"; // NOI18N
    public static final String BR = "br"; // NOI18N
    public static final String BUTTON = "button"; // NOI18N
    public static final String CAPTION = "caption"; // NOI18N
   
    public static final String CENTER = "center"; // NOI18N
    public static final String CITE = "cite"; // NOI18N
    public static final String CODE = "code"; // NOI18N
    public static final String COL = "col"; // NOI18N
    public static final String COLGROUP = "colgroup"; // NOI18N
    public static final String DD = "dd"; // NOI18N
    public static final String DEL = "del"; // NOI18N
    public static final String DFN = "dfn"; // NOI18N
    public static final String DIR = "dir"; // NOI18N
    public static final String DIV = "div"; // NOI18N
    public static final String DL = "dl"; // NOI18N
    public static final String DT = "dt"; // NOI18N
    public static final String EM = "em"; // NOI18N
    public static final String FIELDSET = "fieldset"; // NOI18N
    public static final String FONT = "font"; // NOI18N
    public static final String FORM = "form"; // NOI18N
    public static final String FRAME = "frame"; // NOI18N
    public static final String FRAMESET = "frameset"; // NOI18N
    public static final String H1 = "h1"; // NOI18N
    public static final String H2 = "h2"; // NOI18N
    public static final String H3 = "h3"; // NOI18N
    public static final String H4 = "h4"; // NOI18N
    public static final String H5 = "h5"; // NOI18N
    public static final String H6 = "h6"; // NOI18N
    public static final String HEAD = "head"; // NOI18N
    public static final String HR = "hr"; // NOI18N
    public static final String HTML = "html"; // NOI18N
    public static final String I =  "i"; // NOI18N
 
    public static final String IFRAME = "iframe"; // NOI18N
    public static final String IMG = "img"; // NOI18N
    public static final String INPUT = "input"; // NOI18N
    public static final String INS = "ins"; // NOI18N
    public static final String ISINDEX = "isindex"; // NOI18N

    public static final String KBD = "kbd"; // NOI18N
    public static final String LABEL = "label"; // NOI18N
    public static final String LI = "li"; // NOI18N
    public static final String LINK = "link"; // NOI18N
    public static final String MAP = "map"; // NOI18N
    public static final String MENU = "menu"; // NOI18N
    public static final String META = "meta"; // NOI18N
    public static final String NOBR = "nobr"; // NOI18N
    public static final String NOFRAMES = "noframes"; // NOI18N
    public static final String NOSCRIPT = "noscript"; // NOI18N
    public static final String OBJECT = "object"; // NOI18N
    public static final String OL = "ol"; // NOI18N
    public static final String OPTION = "option"; // NOI18N
    public static final String P = "p"; // NOI18N
    public static final String PARAM = "param"; // NOI18N
    public static final String PRE = "pre"; // NOI18N
    public static final String Q = "q"; // NOI18N
    public static final String S = "s"; // NOI18N
    public static final String SAMP = "samp"; // NOI18N
    public static final String SCRIPT = "script"; // NOI18N
    public static final String SELECT = "select"; // NOI18N
    public static final String SMALL = "small"; // NOI18N
    public static final String SPAN = "span"; // NOI18N
    public static final String STRIKE = "strike"; // NOI18N
    public static final String STRONG = "strong"; // NOI18N
    public static final String STYLE = "style"; // NOI18N
    public static final String SUB = "sub"; // NOI18N
    public static final String SUP = "sup"; // NOI18N
    public static final String TABLE = "table"; // NOI18N
    public static final String TBODY = "tbody"; // NOI18N
    public static final String TD = "td"; // NOI18N
    public static final String TEXTAREA = "textarea"; // NOI18N
    public static final String TFOOT =  "tfoot"; // NOI18N
    public static final String TH = "th"; // NOI18N
    public static final String THEAD = "thead"; // NOI18N
    public static final String TITLE = "title"; // NOI18N
    public static final String TR = "tr"; // NOI18N
    public static final String TT = "tt"; // NOI18N
    public static final String U = "u"; // NOI18N
    public static final String UL = "ul"; // NOI18N
    public static final String VAR = "var"; // NOI18N

    
    private static String[] tags = {
        A, ABBR, ACRONYM, ADDRESS, APPLET, AREA, B, BASE, BASEFONT,
        BIG, BLOCKQUOTE, BODY, BR, BUTTON, CAPTION, CENTER, CITE, CODE, COL, 
        COLGROUP, DD, DEL, DFN, DIR, DIV, DL, DT, EM, FIELDSET, FONT,
        FORM, FRAME, FRAMESET, H1, H2, H3, H4, H5, H6, HEAD, HR, HTML, 
        I, IFRAME, IMG, INPUT, INS, ISINDEX, KBD, LABEL, LI, LINK, MAP, MENU, 
        META, NOBR, NOFRAMES, NOSCRIPT, OBJECT, OL, OPTION, P, PARAM,
        PRE, Q, S, SAMP, SCRIPT, SELECT, SMALL, SPAN, STRIKE, STRONG, 
        STYLE, SUB, SUP, TABLE, TBODY, TD, TEXTAREA, TFOOT, TH, THEAD, 
        TITLE, TR, TT, U, UL, VAR
    };
    
    /** Return the set of known tags */
    public static String[] getTags() {
        return tags;
    }
 
}
