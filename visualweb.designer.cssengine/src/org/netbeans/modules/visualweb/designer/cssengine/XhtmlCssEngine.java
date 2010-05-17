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
package org.netbeans.modules.visualweb.designer.cssengine;

import org.netbeans.modules.visualweb.api.designer.cssengine.StyleRefreshable;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.spi.designer.cssengine.CssUserAgentInfo;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSEngineUserAgent;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleDeclaration;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.css.engine.value.*;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.engine.value.css2.ClipManager;
import org.apache.batik.css.engine.value.css2.DirectionManager;
import org.apache.batik.css.engine.value.css2.DisplayManager;
import org.apache.batik.css.engine.value.css2.FontFamilyManager;
import org.apache.batik.css.engine.value.css2.FontSizeAdjustManager;
import org.apache.batik.css.engine.value.css2.FontSizeManager;
import org.apache.batik.css.engine.value.css2.FontStretchManager;
import org.apache.batik.css.engine.value.css2.FontStyleManager;
import org.apache.batik.css.engine.value.css2.FontVariantManager;
import org.apache.batik.css.engine.value.css2.FontWeightManager;
import org.apache.batik.css.engine.value.css2.OverflowManager;
import org.apache.batik.css.engine.value.css2.TextDecorationManager;
import org.apache.batik.css.engine.value.css2.UnicodeBidiManager;
import org.apache.batik.css.engine.value.css2.VisibilityManager;
import org.apache.batik.css.engine.value.svg.ColorManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.css.parser.ExtendedParserWrapper;
import org.apache.batik.css.parser.Parser;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.css.CSSPrimitiveValue;


/**
 * This class plugs in CSS support for XHTML
 *
 * @author Tor Norbye
 * @author Carl Quinn
 */
class XhtmlCssEngine extends CSSEngine {
    private static StyleSheet defaultRules = null;

    /**
     * The value managers for XHTML - KEEP IN SYNC WITH PROPERTY INDICES IN XhtmlCss.java
     */
    public static final ValueManager[] XHTML_VALUE_MANAGERS =
        {

            // Try to keep the order alphabetical, because in
            // StyleMap.toStyleString I put the user's properties back out
            // in the below order (e.g. when manipulating a style string
            // indirectly such as by dragging a component, the styles
            // get reordered to the below order.)
            new BackgroundColorManager(), new BackgroundImageManager(),


            // If you add this:
            // new BackgroundAttachmentManager(),
            // then update the BackgroundShorthandManager too
            new BackgroundPositionManager(), new BackgroundRepeatManager(),
            new BorderCollapseManager(),
            new BorderColorManager(CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY),
            new BorderColorManager(CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY),
            new BorderColorManager(CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY),
            new BorderColorManager(CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY),
            new BorderStyleManager(CssConstants.CSS_BORDER_LEFT_STYLE_PROPERTY),
            new BorderStyleManager(CssConstants.CSS_BORDER_RIGHT_STYLE_PROPERTY),
            new BorderStyleManager(CssConstants.CSS_BORDER_TOP_STYLE_PROPERTY),
            new BorderStyleManager(CssConstants.CSS_BORDER_BOTTOM_STYLE_PROPERTY),
            new BorderWidthManager(CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY),
            new BorderWidthManager(CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY),
            new BorderWidthManager(CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY),
            new BorderWidthManager(CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY),
            new CaptionSideManager(), new ClearManager(), new ClipManager(), new ColorManager(),
            new DirectionManager(), new DisplayManager(), new FloatManager(),
            new FontFamilyManager(), new FontSizeManager(), new FontSizeAdjustManager(),
            new FontStretchManager(), new FontStyleManager(), new FontVariantManager(),
            new FontWeightManager(), new HeightManager(), new LineHeightManager(),
            new ListStyleImageManager(), new ListStyleTypeManager(),
            

            // ListStylePositionManager: if you insert this, adjust
            // ListStyleShorthandManager to reference it too
            new MarginManager(CssConstants.CSS_MARGIN_LEFT_PROPERTY),
            new MarginManager(CssConstants.CSS_MARGIN_RIGHT_PROPERTY),
            new MarginManager(CssConstants.CSS_MARGIN_TOP_PROPERTY),
            new MarginManager(CssConstants.CSS_MARGIN_BOTTOM_PROPERTY),
            new OffsetManager(CssConstants.CSS_LEFT_PROPERTY, OffsetManager.HORIZONTAL_ORIENTATION),
            new OffsetManager(CssConstants.CSS_RIGHT_PROPERTY, OffsetManager.HORIZONTAL_ORIENTATION),
            new OffsetManager(CssConstants.CSS_TOP_PROPERTY, OffsetManager.VERTICAL_ORIENTATION),
            new OffsetManager(CssConstants.CSS_BOTTOM_PROPERTY, OffsetManager.VERTICAL_ORIENTATION),
            new OverflowManager(), new PaddingManager(CssConstants.CSS_PADDING_LEFT_PROPERTY),
            new PaddingManager(CssConstants.CSS_PADDING_RIGHT_PROPERTY),
            new PaddingManager(CssConstants.CSS_PADDING_TOP_PROPERTY),
            new PaddingManager(CssConstants.CSS_PADDING_BOTTOM_PROPERTY), new PositionManager(),
            new TableLayoutManager(), new TextAlignManager(), new TextDecorationManager(),
            new TextIndentManager(), new TextTransformManager(), new UnicodeBidiManager(),
            new VerticalAlignmentManager(), new VisibilityManager(), new WhitespaceManager(),
            new WidthManager(), new ZIndexManager(), new RaveLayoutManager(),
            new RaveLinkColorManager()
        };

    /**
     * The shorthand managers for XHTML.
     */
    public static final ShorthandManager[] XHTML_SHORTHAND_MANAGERS =
        {
            new BackgroundShorthandManager(), new BorderShorthandManager(),
            new BorderColorShorthandManager(), new BorderStyleShorthandManager(),
            new BorderWidthShorthandManager(),
            new BorderSideShorthandManager(CssConstants.CSS_BORDER_LEFT_PROPERTY,
                CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY,
                CssConstants.CSS_BORDER_LEFT_STYLE_PROPERTY,
                CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY),
            new BorderSideShorthandManager(CssConstants.CSS_BORDER_RIGHT_PROPERTY,
                CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY,
                CssConstants.CSS_BORDER_RIGHT_STYLE_PROPERTY,
                CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY),
            new BorderSideShorthandManager(CssConstants.CSS_BORDER_TOP_PROPERTY,
                CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY,
                CssConstants.CSS_BORDER_TOP_STYLE_PROPERTY,
                CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY),
            new BorderSideShorthandManager(CssConstants.CSS_BORDER_BOTTOM_PROPERTY,
                CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY,
                CssConstants.CSS_BORDER_BOTTOM_STYLE_PROPERTY,
                CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY), new FontShorthandManager(),
            new ListStyleShorthandManager(), new MarginShorthandManager(),
            new PaddingShorthandManager()
        };

    /** Shared instance of an index map into the value manager names */
    static org.apache.batik.css.engine.StringIntMap valueManagerIndex;

    /** Shared instance of an index map into the shorthand manager names */
    static org.apache.batik.css.engine.StringIntMap shorthandManagerIndex;

    static {
        int len = XHTML_VALUE_MANAGERS.length;
        valueManagerIndex = new org.apache.batik.css.engine.StringIntMap(len);

        for (int i = len - 1; i >= 0; --i) {
            String pn = XHTML_VALUE_MANAGERS[i].getPropertyName();
            valueManagerIndex.put(pn, i);
        }

        len = XHTML_SHORTHAND_MANAGERS.length;
        shorthandManagerIndex = new org.apache.batik.css.engine.StringIntMap(len);

        for (int i = len - 1; i >= 0; --i) {
            String pn = XHTML_SHORTHAND_MANAGERS[i].getPropertyName();
            shorthandManagerIndex.put(pn, i);
        }
    }

    // Map for quick lookup of attribute aliases
    protected static final StringIntMap attributes = new StringIntMap(20);

    static {
        attributes.put(HtmlAttribute.LINK, HtmlAttribute.LINK_ID);
        attributes.put(HtmlAttribute.ALIGN, HtmlAttribute.ALIGN_ID);
        attributes.put(HtmlAttribute.VALIGN, HtmlAttribute.VALIGN_ID);
        attributes.put(HtmlAttribute.BGCOLOR, HtmlAttribute.BGCOLOR_ID);
        attributes.put(HtmlAttribute.CLEAR, HtmlAttribute.CLEAR_ID);
        attributes.put(HtmlAttribute.BACKGROUND, HtmlAttribute.BACKGROUND_ID);
        attributes.put(HtmlAttribute.TEXT, HtmlAttribute.TEXT_ID);
        attributes.put(HtmlAttribute.WIDTH, HtmlAttribute.WIDTH_ID);
        attributes.put(HtmlAttribute.HEIGHT, HtmlAttribute.HEIGHT_ID);
        attributes.put(HtmlAttribute.NOWRAP, HtmlAttribute.NOWRAP_ID);
        attributes.put(HtmlAttribute.BORDER, HtmlAttribute.BORDER_ID);
        attributes.put(HtmlAttribute.COLOR, HtmlAttribute.COLOR_ID);
        attributes.put(HtmlAttribute.SIZE, HtmlAttribute.SIZE_ID);
        attributes.put(HtmlAttribute.FACE, HtmlAttribute.FACE_ID);
        attributes.put(HtmlAttribute.TYPE, HtmlAttribute.TYPE_ID);
    }

    /** Error Handler which does nothing */
    public static final ErrorHandler SILENT_ERROR_HANDLER =
        new ErrorHandler() {
            public void error(CSSParseException cp) {
            }

            public void fatalError(CSSParseException cp) {
            }

            public void warning(CSSParseException cp) {
            }
        };

        // <markup_separation>
//    private MarkupUnit unit;
        // </markup_separation>
    private ErrorHandler errorHandler;

    /**
     * Creates a new XhtmlCssEngine
     * @param doc The associated document.
     * @param uri The document URI.
     * @param p The CSS parser to use.
     * @param ctx The CSS context.
     */
    // <markup_separation>
//    private XhtmlCssEngine(Document doc, URL uri, ExtendedParser p, CSSContext ctx, MarkupUnit unit) {
    // ====
    private XhtmlCssEngine(Document doc, URL uri, ExtendedParser p, CSSContext ctx) {
    // </markup_separation>
        super(doc, uri, p, XHTML_VALUE_MANAGERS, XHTML_SHORTHAND_MANAGERS, null, null,
            HtmlAttribute.STYLE, null, HtmlAttribute.CLASS, true, null, ctx);

        // SVG defines line-height to be font-size.
        // What about XHTML?
        //lineHeightIndex = fontSizeIndex;
        // <markup_separation>
//        this.unit = unit;
        // </markup_separation>

        assert (XhtmlCss.FINAL_INDEX + 1) == XHTML_VALUE_MANAGERS.length;

        // Initialize indices since we've commented that out from
        // the superclass
        indexes = valueManagerIndex;
        fontSizeIndex = XhtmlCss.FONT_SIZE_INDEX;
        lineHeightIndex = XhtmlCss.LINE_HEIGHT_INDEX;
        colorIndex = XhtmlCss.COLOR_INDEX;
        shorthandIndexes = shorthandManagerIndex;
    }

    /*
     * TODO: Consider a performance optimization described by David Hyatt of Gecko and
     * later Safari fame, listed here: http://weblogs.mozillazine.org/hyatt/archives/2005_05.html#007507

    One of the most interesting problems (to me at least) in browser
    layout engines is how to implement a style system that can
    determine the style information for elements on a page
    efficiently. I worked on this extensively in the Gecko layout
    engine during my time at AOL and I've also done a lot of work on
    it for WebCore at Apple. My ideal implementation would actually be
    a hybrid of the two systems, since some of the optimizations I've
    done exist only in one engine or the other.

    When dealing with style information like font size or text color,
    you have both the concept of back end information, what was
    specified in the style rule, and the concept of front end
    information, the computed result that you'll actually use when
    rendering. The interesting problem is how to compute this front
    end information for a given element efficiently.

    Back end information can be specified in two different ways. It
    can either be specified using CSS syntax, whether in a stylesheet
    or in an inline style attribute on the element itself, or it is
    implicitly present because another attribute on the element
    specified presentational information. An example of such an
    attribute would be the color attribute on the font tag. Both
    WebCore and Gecko use the term mapped attribute to describe an
    attribute whose value (or even mere presence) maps to some
    implicit style declaration.

    A rule in CSS consists of two pieces. There is the selector, that
    bit of information that says under what conditions the rule should
    match a given element, and there is the declaration, a list of
    property/value pairs that should be applied to the element should
    the selector be matched.

    All back end information can ultimately be thought of as supplying
    a declaration. A normal rule in a stylesheet that is matched has
    the declaration specified as part of the rule. An inline style
    attribute on an element has no selector and is simply a
    declaration that always applies to that element. Similarly each
    individual mapped attribute (like the color and face attributes on
    the font tag) can be thought of as supplying a declaration as
    well.

    Therefore the process of computing the style information for an
    element can be broken down into two phases. The first phase is to
    determine what set of declarations apply to an element. Once that
    back end information has been determined, the second phase is to
    take that back end information and quickly determine the
    information that should be used when rendering.

    WebCore (in upcoming Safari releases) has a really cool
    optimization that I came up with to avoid even having to compute
    the set of declarations that apply to an element. This
    optimization in practice results in not even having to match style
    for about 60% of the elements on your page.

    The idea behind the optimization is to recognize when two elements
    in a page are going to have the same style through DOM (and other
    state) inspection and to simply share the front end style
    information between those two elements whenever possible.

    There are a number of conditions that must be met in order for
    this sharing to be possible:
    
     (1) The elements must be in the same mouse state (e.g., one can't
         be in :hover while the other isn't)
    (2) Neither element should have an id
    (3) The tag names should match
    (4) The class attributes should match
    (5) The set of mapped attributes must be identical
    (6) The link states must match
    (7) The focus states must match
    (8) Neither element should be affected by attribute selectors,
        where affected is defined as having any selector match that
        uses an attribute selector in any position within the selector
        at all
    (9) There must be no inline style attribute on the elements
    (10) There must be no sibling selectors in use at all. WebCore
         simply throws a global switch when any sibling selector is
         encountered and disables style sharing for the entire
         document when they are present. This includes the + selector
         and selectors like :first-child and :last-child.

    The algorithm to locate a shared style then goes something like
    this. You walk through your previous siblings and for each one see
    if the above 10 conditions are met. If you find a match, then
    simply share your style information with the other element. Such a
    system obviously assumes a reference counting model for your front
    end style information.

    Where this optimization kicks into high gear, however, is that it
    doesn't have to give up if no siblings can be located. Because the
    detection of identical style contexts is essentially O(1), nothing
    more than a straight pointer comparison, you can easily look for
    cousins of your element and still share style with those elements.

    The way this works is that if you can't locate a sibling, you can
    go up to a parent element and attempt to find a sibling or cousin
    of the parent element that has the same style pointer. If you find
    such an element, you can then drill back down into its children
    and attempt to find a match.

    This means that for HTML like the following:

    <table>
    <tr class='row'>
    <td class='cell' width=300 nowrap>Cell One</td>
    </tr>
    <tr class='row'>
    <td class='cell' width=300 nowrap>Cell Two</td>
    </tr>

    In the above example, not only do the two rows share the same
    style information, but the two cells do as well. This optimization
    works extremely well for both old-school HTML (in which many
    deprecated presentational tags are used) and newer HTML (in which
    class attributes might figure more prominently).

    Once the engine determines that a style can't be shared, i.e.,
    that no pre-existing front end style pointer is available, then
    it's time to figure out the set of declarations that match a given
    element. It is obvious that for inline style attributes and mapped
    attributes that you can find the corresponding declaration
    quickly. The inline style declaration can be owned by the element,
    and the mapped attributes can be kept in a document-level
    hash. WebCore has a bit of an edge over Gecko here in that it
    treats each individual mapped attribute on an element as a
    separate declaration, whereas Gecko hashes all of the mapped
    attributes on an element as a single "rule." This means that Gecko
    will not be able to share the mapped attribute declaration for the
    following two elements:

    <img width=300 border=0>
    <img width=500 border=0>

    WebCore creates three unique declarations and hashes them, one for
    a width of 300, one for a width of 500, and one for a border of
    0. Gecko creates two different "rules," one for
    (width=300,border=0) and another for (width=500,border=0). As you
    can see in such a system, you will frequently not be able to treat
    the identical border attributes as the same.

    Aside from this difference in mapped attribute handling, the two
    engines employ a similar optimization for quickly determining
    matching stylesheet rules called rule filtering.
    [removed - this is done.]

    This brings us to the final phase of the style computation, which
    is taking the set of matches and quickly computing the appropriate
    front end style information. It is here that Gecko really
    shines. What I implemented in Gecko was a data structure called
    the rule tree for efficient storing of cached style information
    that can be shared *even when* two elements are not necessarily
    the same.

    The idea behind the rule tree is as follows. You can think of the
    universe of possible rules in your document as an alphabet and the
    set of rules that are matched by an element as a given input
    word. For example, imagine that you had 26 rules in a stylesheet
    and you labeled them A-Z. One element might match three rules in
    the sheet, thus forming the input word "C-A-T" or another might
    form the input word "D-O-G."

    There are several important observations one can make once you
    formulate the problem this way. The first is that words that are
    prefixes of a larger word will end up applying the same set of
    rules. All additional letters in the word do is result in the
    application of more declarations. Thus the rule tree is
    effectively a lexicographic tree of nodes, with each node in a
    tree being created lazily as you walk the tree spelling out a
    given word.

    This system allows you to cache style information at each node in
    the tree. This means that once you've looked up the word
    "C-A-T-E-R-W-A-U-L", and cached information at all of the nodes,
    then looking up the word "C-A-T" becomes more efficient.

    In order to make the caching efficient, properties can be grouped
    into categories, with the primary criterion for categorization
    being whether the property inherits by default. It's also
    important to group properties together that would logically be
    specified together, so that when a fault occurs and you have to
    make a copy of a given struct, you do so knowing that the other
    values in the struct were probably going to be different anyway.

    Once you have the properties grouped into categories like the
    border struct or the background struct, then you can either store
    these structs in the rule tree or as part of a style tree that
    more or less matches the structure of the document. Inheritance
    has to apply down the style tree and tends to force a fault,
    whereas non-inherited properties can usually be cached in the rule
    tree for easy access.

    WebCore doesn't contain a rule tree, but it is smart enough to
    refcount the structs and share them as long as no properties have
    been set in the struct. In practice this works pretty well but is
    not as ideal as the rule tree solution.
     */

    /** Create a new engine and associate it with the given document */
    // <markup_separation>
//    public static XhtmlCssEngine create(RaveDocument doc, MarkupUnit unit, URL url) {
    // ====
    static XhtmlCssEngine create(Document doc, URL url, CssUserAgentInfo userAgentInfo) {
        if (doc == null) {
            throw new NullPointerException("Document many not be null!"); // NOI18N
        }
        
    // </markup_separation>
        UserAgent userAgent = new UserAgent(userAgentInfo);
        DesignerContext ctx = new DesignerContext(doc, userAgent, userAgentInfo);
//        Parser p = new org.apache.batik.css.parser.Parser()
        Parser p = new RaveParser();
        ExtendedParser ep = ExtendedParserWrapper.wrap(p);
        // <markup_separation>
//        XhtmlCssEngine engine = new XhtmlCssEngine(doc, url, ep, ctx, unit);
        // ====
        XhtmlCssEngine engine = new XhtmlCssEngine(doc, url, ep, ctx);
        // </markup_separation>
        ctx.setEngine(engine);
        engine.setUserAgentStyleSheet(getUserAgentStyleSheet(engine));

// <moved to caller this is not interesting for the engine itself>
//        if (doc != null) {
//            doc.setCssEngine(engine);
//        }
// </moved to caller>

        engine.setCSSEngineUserAgent(userAgent);
        engine.setMedia(userAgent.getMedia());

        /* TODO User defined stylesheets not yet supported
        String uri = userAgent.getUserStyleSheetURI();
        if (uri != null) {
            try {
                URL url = new URL(uri);
                eng.setUserAgentStyleSheet
                    (eng.parseStyleSheet(url, "all"));
            } catch (MalformedURLException e) {
                userAgent.displayError(e);
            }
        }
        engine.setAlternateStyleSheet(userAgent.getAlternateStyleSheet());
         */
        return engine;
    }

    private static StyleSheet getUserAgentStyleSheet(CSSEngine engine) {
        if (defaultRules == null) {
            URL url = XhtmlCssEngine.class.getResource("default.css"); // TODO: reuse UserAgentStylesheet

            //URL url = null;
            //
            //try {
            //    //url = new URL("file:" + System.getProperty("netbeans.home") + "/ua.css"); // NOI18N
            //    url = new URL("file:/tmp/default.css"); // NOI18N
            //} catch (java.net.MalformedURLException mue) {
            //    mue.printStackTrace();
            //}
            if (url != null) {
                InputSource is = new InputSource(url.toString());
                defaultRules = engine.parseStyleSheet(is, url, "all", url);                
                defaultRules.setupFilters();
            }
        }

        return defaultRules;
    }

// XXX Not used.
//    /** Reinitialize the engine by re-reading the document stylesheets
//     * @todo Attach to my DocumentFragment instead!
//     */
//    public void setDocument(/*RaveDocument*/Document doc) {
//        DesignerContext ctx = (DesignerContext)getCSSContext();
//
//        if (document != null) {
//            refreshStyles(document);
//        }
//
//        ctx.setDocument(doc);
//        this.document = doc;
//// <moving RaveDoc refs outside> engine is not interested in registering itself somewhere.
////        doc.setCssEngine(this);
//// </moving RaveDoc refs outside>
//    }

//    private void handleError(String message, Object location, int lineno, int column, Exception e) {
//        if (errorHandler != null) {
//            String filename = null;
//            int line = lineno;
//
//            if (location != null) {
//                final String fullname = computeFilename(location);
//                filename = fullname.substring(fullname.lastIndexOf('/') + 1);
//                line = computeLineNumber(location, lineno);
//
//                if (filename != null) {
//                    message = filename + ":" + line + /* ":" + column + */
//                        ": " + message;
//                }
//            }
//
//            CSSParseException cpe = new CSSParseException(message, filename, line, column, e);
//            errorHandler.error(cpe);
//        }
//    }
    private static CSSParseException createCSSParseException(String message, Object location, int lineno, int column, Exception e) {
        String filename = null;
        int line = lineno;

        if (location != null) {
//            final String fullname = computeFilename(location);
//            filename = fullname.substring(fullname.lastIndexOf('/') + 1);
//            line = computeLineNumber(location, lineno);
//
//            if (filename != null) {
//                message = filename + ":" + line + /* ":" + column + */
//                    ": " + message;
//            }
            message = location + ":" + lineno + ": " + message; // NOI18N
        }

        return new CSSParseException(message, filename, line, column, e);
    }
    
    protected void warnCircularReference(URL uri, Object location) {
        String message = NbBundle.getMessage(XhtmlCssEngine.class, "MSG_CircularReference", uri); // NOI18N

        if (errorHandler != null) {
//            handleError(message, location, -1, -1, null);
            errorHandler.error(createCSSParseException(message, location, -1, -1, null));
        } else {
//            OutputListener listener = getListener(location, -1, -1);
//            MarkupService.displayError(message, listener);
            if (location instanceof AbstractValue) {
                location = ((AbstractValue)location).getLocation();
            }
//            InSyncService.getProvider().getRaveErrorHandler().displayErrorForLocation(message, location, -1, -1);
            UserAgent userAgent = getUserAgent();
            if (userAgent == null) {
                // XXX What to do now, log it?
            } else {
                userAgent.displayErrorForLocation(message, location, -1, -1);
            }
        }
    }

    /**
     * Scan attributes for an element and transcribe deprecated
     * style-type attributes into real style properties
     */
    protected void applyNonCSSPresentationalHints(CSSStylableElement e, StyleMap map) {
        String tname = e.getTagName(); // should already be lowercase
        assert (tname != null) && (tname.length() > 0);

        if (tname.indexOf(':') != -1) {
            // Attributes on JSF elements etc. should not alias.
            // For example, the Braveheart button component has a "text"
            // attribute which has nothing to do with the text attribute
            // in html which affects screen color.
            return;
        }

        org.w3c.dom.NamedNodeMap atts = e.getAttributes();

        if (atts == null) {
            return;
        }

        int ac = atts.getLength();

        for (int i = 0; i < ac; i++) {
            Attr att = (Attr)atts.item(i);
            String aname = att.getName().toLowerCase().intern();
            int attribute = attributes.get(aname);

            if (attribute == -1) {
                continue;
            }

            String aval = att.getValue();

            if ((aval == null) || (aval.length() == 0)) {
                // Avoid attributes like this:
                //  <body color="">
                // Here we'll try to CSS parse "", and we get a parse
                // error for this:
                // 'The attribute "?" represents an invalid CSS value ("").'
                continue;
            }

            int pname = -1;
            String pval = aval;

            switch (attribute) {
            // Todo: ALINK/VLINK attributes -> e.g. LINK=blue becomes
            //    a[href] {color: blue;}
            case HtmlAttribute.ALIGN_ID:

                // <table>: align = left|center|right [CI]
                // <object>/<image>: align = bottom|middle|top|left|right
                // <td>: align = left|center|right|justify|char [CI]
                if (tname.equals(HtmlTag.IMG.name) || // XXX just switch to block tag check instead?
                        tname.equals(HtmlTag.DIV.name) || tname.equals(HtmlTag.FORM.name) ||
                        tname.equals(HtmlTag.IFRAME.name) || tname.equals(HtmlTag.TABLE.name) ||
                        tname.equals(HtmlTag.APPLET.name) || tname.equals(HtmlTag.OBJECT.name)) {
                    if (aval.equals("left") || aval.equals("right")) { //!CQ need to tokenize aval?
                        pname = XhtmlCss.FLOAT_INDEX;
                    } else if (aval.equals("center")) { // NOI18N
                        applyNonCSSPresentationalHint(e, map, XhtmlCss.TEXT_ALIGN_INDEX,
                            CssConstants.CSS_RAVECENTER_VALUE);
                    } else {
                        // top/texttop,middle,bottom/baseline,absbottom
                        pname = XhtmlCss.VERTICAL_ALIGN_INDEX;

                        if (aval.equals("texttop")) { // NOI18N
                            pval = CssConstants.CSS_TEXT_TOP_VALUE;
                        } else if (aval.equals("absmiddle")) { // NOI18N

                            // This is NOT right but from a quick google
                            // there's no direct equivalent value for
                            // vertical align to emulate the absmiddle behavior
                            pval = CssConstants.CSS_MIDDLE_VALUE;
                        } else if (aval.equals("absbottom")) { // NOI18N
                            pval = CssConstants.CSS_BOTTOM_VALUE;
                        }
                    }
                } else {
                    pname = XhtmlCss.TEXT_ALIGN_INDEX; // P, TD, TH, THEAD, TFOOT, TBODY
                }

                break;

            case HtmlAttribute.LINK_ID:

                if (tname.equals(HtmlTag.BODY.name)) {
                    pname = XhtmlCss.RAVELINKCOLOR_INDEX;
                }

                break;

            case HtmlAttribute.VALIGN_ID: // TD, TH, THEAD, TFOOT, TBODY

                // valign = top|middle|bottom|baseline [CI]
                pname = XhtmlCss.VERTICAL_ALIGN_INDEX;

                break;

            case HtmlAttribute.CLEAR_ID: // BR, possibly others

                // clear =  none|left|right|all [CI]
                if (aval.equals("all")) { // NOI18N
                    pval = "both"; // NOI18N
                } // none, left, right are the same for both

                pname = XhtmlCss.CLEAR_INDEX;

                break;

            case HtmlAttribute.BGCOLOR_ID: // BODY, TABLE
                pname = XhtmlCss.BACKGROUND_COLOR_INDEX;

                break;

            case HtmlAttribute.BACKGROUND_ID: // BODY
                pname = XhtmlCss.BACKGROUND_IMAGE_INDEX;

                // XXX #6457821 Encode the url string, to fix cases when there are spaces.
                if (!isEncodedUrl(aval)) {
                    aval = encodeUrl(aval);
                }

                //plu = new LexicalUnitImpl.createURI(null, aval);
                pval = "url(" + aval + ")";

                break;

            case HtmlAttribute.TEXT_ID: // BODY
                pname = XhtmlCss.COLOR_INDEX;

                break;

            case HtmlAttribute.WIDTH_ID: //!CQ PRE, TH, TD, IMG only?
                pname = XhtmlCss.WIDTH_INDEX;

                if (pval.indexOf('%') > 0) { // XXXX should this be aval????
                    pval = aval;
                } else if (hasNoUnits(pval)) {
                    pval = aval + "px";
                }

                break;

            case HtmlAttribute.HEIGHT_ID: //!CQ PRE, TH, TD, IMG only?
                pname = XhtmlCss.HEIGHT_INDEX;

                if (pval.indexOf('%') > 0) { // XXXX should this be aval????
                    pval = aval;
                } else if (hasNoUnits(pval)) {
                    pval = aval + "px";
                }

                break;

            case HtmlAttribute.NOWRAP_ID:

                // MSDN docs on dhtml lists the nowrap attribute as applicable
                // to body, dd, div, dt, td, th.  However, Mozilla doesn't
                // seem to recognize it on div.
                if (tname.equals(HtmlTag.BODY.name) || tname.equals(HtmlTag.TD.name) ||
                        tname.equals(HtmlTag.TH.name) || tname.equals(HtmlTag.DD.name) ||
                        tname.equals(HtmlTag.DT.name) || tname.equals(HtmlTag.DIV.name)) {
                    pname = XhtmlCss.WHITE_SPACE_INDEX;
                    pval = CssConstants.CSS_NOWRAP_VALUE;
                }

                break;

            case HtmlAttribute.BORDER_ID:

                if (hasNoUnits(pval)) { // XXXX should this be aval????
                    pval = aval + "px";
                }

                // Property translates to multiple properties,
                // so set them directly and continue
                applyNonCSSPresentationalHint(e, map, XhtmlCss.BORDER_TOP_WIDTH_INDEX, pval);
                applyNonCSSPresentationalHint(e, map, XhtmlCss.BORDER_LEFT_WIDTH_INDEX, pval);
                applyNonCSSPresentationalHint(e, map, XhtmlCss.BORDER_RIGHT_WIDTH_INDEX, pval);
                applyNonCSSPresentationalHint(e, map, XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX, pval);

                continue; // NOTE - continue, not break

            case HtmlAttribute.COLOR_ID: // generally applicable
                pname = XhtmlCss.COLOR_INDEX;

                break;

            case HtmlAttribute.SIZE_ID:

                if (tname.equals(HtmlTag.FONT.name) || tname.equals(HtmlTag.BASEFONT.name)) {
                    pname = XhtmlCss.FONT_SIZE_INDEX;
                    pval = CssConstants.CSS_MEDIUM_VALUE;

                    if (aval.length() > 0) {
                        if (aval.charAt(0) == '+') {
                            // XXX We could try to read the actual relative
                            // value here, and adjust - but the <font> tag
                            // is already deprecated and discouraged. If it
                            // was easy I'd do it but with CSS there isn't
                            // a way to mage the font size bigger/smaller
                            // by more than a factor of 1 so let's just
                            // leave it at this: <font size="+n"> is
                            // will be equivalent to <font size="+1">
                            pval = CssConstants.CSS_LARGER_VALUE;
                        } else if (aval.charAt(0) == '-') {
                            // Same comment as under '+' handling above
                            pval = CssConstants.CSS_SMALLER_VALUE;
                        } else {
                            // Map to absolute font size
                            try {
                                int n = Integer.parseInt(aval);

                                switch (n) {
                                case 1:
                                    pval = CssConstants.CSS_XX_SMALL_VALUE;

                                    break;

                                case 2:
                                    pval = CssConstants.CSS_X_SMALL_VALUE;

                                    break;

                                case 3:
                                    pval = CssConstants.CSS_SMALL_VALUE;

                                    break;

                                case 4:
                                    pval = CssConstants.CSS_MEDIUM_VALUE;

                                    break;

                                case 5:
                                    pval = CssConstants.CSS_LARGE_VALUE;

                                    break;

                                case 6:
                                    pval = CssConstants.CSS_X_LARGE_VALUE;

                                    break;

                                case 7:
                                    pval = CssConstants.CSS_XX_LARGE_VALUE;

                                    break;
                                }
                            } catch (NumberFormatException ex) {
                                // The attribute is set to a bogus value.
                                // In this case we should simply use the
                                // default of "medium".
                                // pval was set to "medium" already.
                            }
                        }
                    }
                } else if (tname.equals(HtmlTag.HR.name)) { //!CQ HR only?
                    pname = XhtmlCss.HEIGHT_INDEX;

                    if (pval.indexOf('%') > 0) {
                        pval = aval;
                    } else if (hasNoUnits(pval)) {
                        pval = aval + "px";
                    }
                }

                break;

            case HtmlAttribute.FACE_ID: // FONT
                pname = XhtmlCss.FONT_FAMILY_INDEX;

                break;

            case HtmlAttribute.TYPE_ID:

                if (tname.equals(HtmlTag.UL.name) || tname.equals(HtmlTag.OL.name) ||
                        tname.equals(HtmlTag.DL.name) || tname.equals(HtmlTag.LI.name)) {
                    pname = XhtmlCss.LIST_STYLE_TYPE_INDEX;
                }

                break;

                //hspace, vspace => margin-[left+right], margin-[top+bottom] ?
                // What about this:
                //   <basefont> => "font"
            }

            // set the style property iff it was found
            if (pname != -1) {
                applyNonCSSPresentationalHint(e, map, pname, pval);
            }
        }
    }

    /**
     * Return true if the string parameter contains a number without any units at the end. For this
     * purpose a unit will be considered any alphabetical characters or non space character.
     */
    public static boolean hasNoUnits(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);

            if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }
    
    // XXX Ugly method to test whether the url string is encoded already.
    // FIXME How these things should be done correctly?
    private static boolean isEncodedUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        // XXX Checking for the same like used in encodeUrl.
        if (url.indexOf("%09") != -1 // NOI18N
        || url.indexOf("%20") != -1 // NOI18N
        || url.indexOf("%23") != -1 // NOI18N
        || url.indexOf("%25") != -1 // NOI18N
        || url.indexOf("%3C") != -1 // NOI18N
        || url.indexOf("%3E") != -1 // NOI18N
        || url.indexOf("%5B") != -1 // NOI18N
        || url.indexOf("%5D") != -1 // NOI18N
        || url.indexOf("%7B") != -1 // NOI18N
        || url.indexOf("%7D") != -1 // NOI18N
        || url.indexOf("%7E") != -1) { // NOI18N
            return true;
        }
        return false;
    }
    // XXX Copied from propertyeditors/UrlPropertyEditor.
    // There should be common utility method.
    /**
     * Convert a file system path to a URL by converting unsafe characters into
     * numeric character entity references. The unsafe characters are listed in
     * in the IETF specification of URLs
     * (<a href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738</a>). Safe URL
     * characters are all printable ASCII characters, with the exception of the
     * space characters, '#', <', '>', '%', '[', ']', '{', '}', and '~'. This
     * method differs from {@link java.net.URLEncoder#encode(String)}, in that
     * it is intended for encoding the path portion of a URL, not the query
     * string.
     */
    private static String encodeUrl(String url) {
        if (url == null || url.length() == 0)
            return url;
        StringBuffer buffer = new StringBuffer();
        String anchor = null;
        int index = url.lastIndexOf('#');
        if (index >= 0) {
            anchor = url.substring(index + 1);
            url = url.substring(0, index);
        }
        char[] chars = url.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] <= '\u0020') {
                buffer.append('%');
                buffer.append(Integer.toHexString((int) chars[i]));
            } else {
                switch(chars[i]) {
                    case '\u0009': // Tab
                        buffer.append("%09");
                        break;
                    case '\u0020': // Space
                        buffer.append("%20");
                        break;
                    case '#':
                        buffer.append("%23");
                        break;
                    case '%':
                        buffer.append("%25");
                        break;
                    case '<':
                        buffer.append("%3C");
                        break;
                    case '>':
                        buffer.append("%3E");
                        break;
                    case '[':
                        buffer.append("%5B");
                        break;
                    case ']':
                        buffer.append("%5D");
                        break;
                    case '{':
                        buffer.append("%7B");
                        break;
                    case '}':
                        buffer.append("%7D");
                        break;
                    case '~':
                        buffer.append("%7E");
                        break;
                    default:
                        buffer.append(chars[i]);
                }
            }
        }
        if (anchor != null) {
            buffer.append('#');
            buffer.append(anchor);
        }
        if (buffer.length() == url.length())
            return url;
        return buffer.toString();
    }

    /** Refresh all styles
     * @todo Rename to disposeStyles to reflect what this method
     *   actually does.
     */
    public void refreshStyles() {
        if (styleSheetNodes != null) {
            // Since elements hang on to their stylesheets, and on a pure
            // refresh we don't recreate the elements, we've gotta clear them
            // out
            Iterator it = styleSheetNodes.iterator();

            while (it.hasNext()) {
                Object o = it.next();

//                if (o instanceof StyleElement) {
//                    ((StyleElement)o).refresh();
//                } else if (o instanceof StylesheetLinkElement) {
//                    ((StylesheetLinkElement)o).refresh();
//                }
                if (o instanceof StyleRefreshable) {
                    ((StyleRefreshable)o).refresh();
                }
            }

            styleSheetNodes = null;
        }

        disposeStyleMaps(document.getDocumentElement());

        // XXX Is this still needed?
        if (document.getDocumentElement() != document) {
            disposeStyleMaps(document);
        }
    }

// Moved to API.
//    /** XXX To be implemented by style elements in order the engine calls refresh on them.
//     * TODO the refreshing smells badly, revise entire func. */
//    public interface StyleRefreshable {
//        public void refresh();
//    } // Enf of StyleRefreshable.
    
    /** Remove precomputed styles for the given element - compute them
     * over again.
     * @todo Do I have to clear computed styles on all the children too?
     */
    public void clearComputedStyles(Element element/*, String pseudo*/) {
        disposeStyleMaps(element);
    }

// <removing design bean manipulation in engine>
//    protected void setAttributeValue(Element elt, String name, String value) {
//        RaveElement xel = (RaveElement)elt;
//
//        if (xel.getDesignBean() != null) {
//            DesignProperty property = xel.getDesignBean().getProperty("style"); // NOI18N
//
//            if (property != null) {
//                try {
//                    if ((value != null) && (value.length() > 0)) {
//                        property.setValue(value);
//                    } else {
//                        property.unset();
//                    }
//
//                    return;
//                } catch (Exception ex) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    // For some reason the above throws exceptions
//                    // sometimes, not sure why org.w3c.dom.DOMException:
//                    // NOT_FOUND_ERR: An attempt is made to reference a
//                    // node in a context where it does not exist.  TODO
//                    // - figure out WHY!  For now just swallow since
//                    // there's nothing more we can do about it.
//                    // (Update: It think this may be fixed now)
//                }
//            }
//        }
//
//        // If the above fails (shouldn't)
//        super.setAttributeValue(elt, name, value);
//    }
// ====
    /** XXX Only a fallback for the original suspicious pattern. */
    public void setStyleAttributeValue(Element elt, String value) {
        setAttributeValue(elt, HtmlAttribute.STYLE, value);
    }
// </removing design bean manipulation in engine>

    /**
     * Set the error handler to be used in the case of parse errors.
     * If not set, the default handler will be used, which emits warnings
     * in the output window.
     */
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

//    protected OutputListener getListener(Object location, int lineno, final int column) {
//        OutputListener listener = null; // TODO - provide clickable errors
//
//        if (location != null) {
//            final String fullname = computeFilename(location);
//            String filename = fullname.substring(fullname.lastIndexOf('/') + 1);
//            final int line = computeLineNumber(location, lineno);
//
//            if (filename != null) {
//                listener =
//                    new OutputListener() {
//                            public void outputLineSelected(OutputEvent ev) {
//                            }
//
//                            public void outputLineAction(OutputEvent ev) {
//                                // <markup_separation>
////                                Util.show(fullname, null, (line >= 1) ? line : 1, column, true);
//                                // ====
//                                MarkupService.show(fullname, (line >= 1) ? line : 1, column, true);
//                                // </markup_separation>
//                            }
//
//                            public void outputLineCleared(OutputEvent ev) {
//                            }
//                        };
//            }
//        }
//
//        return listener;
//    }

    protected void displayError(DOMException e, Object location, int lineno, final int column) {
        String message = e.getLocalizedMessage();

        if ((message == null) || (message.length() == 0)) {
            return;
        }

        if (errorHandler != null) {
//            handleError(message, location, lineno, column, null);
            errorHandler.error(createCSSParseException(message, location, lineno, column, null));
        } else {
//            OutputListener listener = getListener(location, lineno, column);
//            MarkupService.displayError(message, listener);
//            String fileName = computeFilename(location);
//            int line = computeLineNumber(location, lineno);
            if (location instanceof AbstractValue) {
                location = ((AbstractValue)location).getLocation();
            }
//            InSyncService.getProvider().getRaveErrorHandler().displayErrorForLocation(message, location, lineno, column);
            UserAgent userAgent = getUserAgent();
            if (userAgent == null) {
                // XXX Log it?
            } else {
                userAgent.displayErrorForLocation(message, location, lineno, column);
            }
        }
    }

    protected void displayMissingStyleSheet(String uri) {
        String message =
            NbBundle.getMessage(XhtmlCssEngine.class, "MissingStylesheet", // NOI18N
                uri);

        if (errorHandler != null) {
//            handleError(message, null, -1, 0, null);
            errorHandler.error(createCSSParseException(message, null, -1, 0, null));
        } else {
//            OutputListener listener = null; // TODO - provide clickable errors
//            MarkupService.displayError(message, listener);
//            InSyncService.getProvider().getRaveErrorHandler().displayError(message);
            UserAgent userAgent = getUserAgent();
            if (userAgent == null) {
                // XXX Log it?
            } else {
                userAgent.displayErrorForLocation(message, null, -1, -1);
            }
        }
    }

    /** Initialize all values for a given element (this is otherwise done
     * lazily) on a getComputedStyle call for a particular property).
     */
    public void precomputeAllStyles(CSSStylableElement elt) {
        for (int i = 0, n = XHTML_VALUE_MANAGERS.length; i < n; i++) {
            getComputedStyle(elt, null, i); // throwing away result
        }
    }

//    /**
//     * The filename where this CSS value is defined. This could be a
//     * CSS file but doesn't have to be -- it could point to a markup file
//     * with an inline style attribute.
//     */
//    public static String computeFilename(AbstractValue av) {
//        return computeFilename(av.getLocation());
//    }
//
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct file name to use.
//     */
//    public static String computeFilename(Object location) {
//        if (location instanceof String) {
//            return (String)location;
//        } else if (location instanceof URL) {
//            // <markup_separation>
////            return MarkupUnit.fromURL(((URL)location).toExternalForm());
//            // ====
//            return MarkupService.fromURL(((URL)location).toExternalForm());
//            // </markup_separation>
//        } else if (location instanceof Element) {
//            // Locate the filename for a given element
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//
//            // <markup_separation>
////            // XXX I should derive this from the engine instead, after all
////            // the engine can know the unit! (Since engines cannot be used
////            // with multiple DOMs anyway)
////            FileObject fo = unit.getFileObject();
//            // ====
//            FileObject fo = InSyncService.getProvider().getFileObject(element.getOwnerDocument());
//            // </markup_separation>
//            File f = FileUtil.toFile(fo);
//
//            return f.toString();
//        } else if (location != null) {
//            return location.toString();
//        }
//
//        return "";
//    }
//
//    /**
//     * The line number where this CSS value is defined, within the file
//     * returned by computeFilename. The first line is number 0.
//     */
//    public static int computeLineNumber(AbstractValue av) {
//        return computeLineNumber(av.getLocation(), av.getLineNumber());
//    }
//
//    public static int computeLineNumber(Object location, int lineno) {
//        if (location instanceof Element) {
//            /*
//            // The location is an XhtmlElement -- so the line number
//            // needs to be relative to it.... compute the line number
//            // of the element
//            if (lineno == -1)
//                lineno = 0;
//            Element element = (Element)location;
//            RaveDocument doc = (RaveDocument)element.getOwnerDocument();
//            lineno += doc.getLineNumber(element);
//             */
//            if (lineno == -1) {
//                lineno = 0;
//            }
//
//            Element element = (Element)location;
//            element = MarkupService.getCorrespondingSourceElement(element);
//            // <markup_separation>
////            lineno += unit.computeLine(element);
//            // ====
//            lineno += InSyncService.getProvider().computeLine(element.getOwnerDocument(), element);
//            // </markup_separation>
//        }
//
//        return lineno;
//    }

    /** Get the Link Color to use in this document */
    Value getLinkColor() {
//        Element body = DesignerService.getDefault().getBody(document);
//        Element body = InSyncService.getProvider().getHtmlBodyForMarkupFile(InSyncService.getProvider().getFileObject(document));
        
        UserAgent userAgent = getUserAgent();
        Element body = userAgent == null ? null : userAgent.getHtmlBodyForDocument(document);
        if (body == null) {
            // XXX Is it OK?
            return new RGBColorValue
                (new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 0),
                 new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 0),
                 new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 200));
        }

        return getComputedStyle((CSSStylableElement)body, null, XhtmlCss.RAVELINKCOLOR_INDEX);
    }

    /** For use by page import */
    public boolean mediaMatch(SACMediaList ml) {
        return super.mediaMatch(ml);
    }

    /**
     * Given a Map of style properties, serialize the set and compress
     * properties into shorthands, when possible. See styleToMap.
     */
    public String mapToStyle(Map<String, String> map) {
        StyleMap styleMap = new StyleMap(getNumberOfProperties());
        StringBuffer unknown = null;
        Iterator<String> it = map.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            String value = map.get(key);

            int index = getXhtmlPropertyIndex(key);

            if (index == -1) {
                // Unknown property.... what to do, what to do... remember it
                // so we can attach it to the end
                if (unknown == null) {
                    unknown = new StringBuffer(60);
                }

                unknown.append(key);
                unknown.append(':');
                unknown.append(' ');
                unknown.append(value);
                unknown.append(';');
            } else {
                styleMap.putValue(index, new MapStringValue(value));
            }
        }

        String style = toMinimalStyleString(styleMap);

        if (unknown != null) {
            if (style.length() > 0) {
                style = style + "; " + unknown;
            } else {
                style = unknown.toString();
            }
        }

        return style;
    }

    /** Parse the given style declaration and return a map of properties
     * stored in it. The Map will have String keys which correspond to
     * property names, and String values which correspond to CSS
     * raw text for the values.
     */
    public Map<String, String> styleToMap(String style) {
        CSSStylableElement old = element;
        StyleDeclaration sd = null;
        int size = 0;

        try {
            // There needs to be a current element for resolving
            // urls, for example for a background-image property.
            // The URL won't be right, but that's okay for now since
            // we won't try to access it.
            element = (CSSStylableElement)document.getDocumentElement();
            unknownPropertyNames = new ArrayList<String>();
            unknownPropertyValues = new ArrayList<String>();
            sd = parseStyleDeclaration(element, style);
            size = sd.size();

            if (size == 0) {
                return new HashMap<String, String>(0);
            }

            // XXX Pick a better data structure that preserves order?
            // Yeah, we don't really need to Hash aspect here; we're mostly
            // iterating!
//            LinkedHashMap map = new LinkedHashMap(2 * size);
            Map<String, String> map = new LinkedHashMap<String, String>(2 * size);

            for (int j = 0, m = size; j < m; j++) {
                int idx = sd.getIndex(j);
                String key = getPropertyName(idx);
                Value value = sd.getValue(j);

                if (value != null) {
                    map.put(key, value.getCssText());
                }
            }

            // Add unknown properties
            for (int i = 0, n = unknownPropertyNames.size(); i < n; i++) {
                map.put((String)unknownPropertyNames.get(i), (String)unknownPropertyValues.get(i));
            }

            return map;
        } finally {
            unknownPropertyNames = null;
            unknownPropertyValues = null;
            element = old;
        }
    }

    /**
     * Returns the property index, or -1.
     */
    public static int getXhtmlPropertyIndex(String name) {
        return valueManagerIndex.get(name);
    }

    /**
     * Returns the shorthand property index, or -1.
     */
    public static int getXhtmlShorthandIndex(String name) {
        return shorthandManagerIndex.get(name);
    }

    protected void findStyleSheetNodes() {
// <removing set/getRoot from RaveDocument>
//        Node root = ((RaveDocument)document).getRoot();
//        findStyleSheetNodes(root);
// ====
        // XXX FIXME Here we need to work with the rendered HTML DOM directly,
        // there is no interest in the original JSP DOM.
//        FileObject markupFile = InSyncService.getProvider().getFileObject(document);
//        if (markupFile != null) {
//            DocumentFragment df = InSyncService.getProvider().getHtmlDomFragmentForMarkupFile(markupFile);
//        DocumentFragment df = InSyncService.getProvider().getHtmlDomFragmentForDocument(document);
        UserAgent userAgent = getUserAgent();
        DocumentFragment df = userAgent == null ? null : userAgent.getHtmlDomFragmentForDocument(document);
        if (df != null) {
            findStyleSheetNodes(df);
            return;
        }
//        }

        // XXX Log problem?
        super.findStyleSheetNodes();
// <removing set/getRoot from RaveDocument>
    }

    // Moved to service impl.
//    /**
//     * Return true iff the given CSS property for the given element
//     * is set by an inline property setting
//     */
//    public static boolean isInlineValue(CSSStylableElement elt, int propidx) {
//        String pseudo = ""; // Pending
//        StyleMap sm = elt.getComputedStyleMap(pseudo);
//
//        if (sm == null) {
//            return false;
//        }
//
//        Value value = sm.getValue(propidx);
//
//        if (value == null) {
//            return false;
//        }
//
//        return sm.getOrigin(propidx) == StyleMap.INLINE_AUTHOR_ORIGIN;
//    }

    /**
     * Try to create a minimal (as short as possible) style string
     * from this map, by using shorthands when possible.
     * This will for example compress border-color-left/right/bottom/top
     * into border-color: one two three four, and so on.
     * @todo Handle the font shorthand property.
     */
    protected String toMinimalStyleString(StyleMap map) {
        int size = map.getSize(true);
        boolean[] used = new boolean[size];
        StringBuffer sb = new StringBuffer();

        // TODO how does isImportant on an individual property get translated
        // into a shorthand??? Try the reverse!
        // First see if I can use border. That's true when
        // all of
        boolean allBorderWidths =
            (map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_TOP_WIDTH_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_RIGHT_WIDTH_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX) != null);
        boolean sameBorderWidth =
            allBorderWidths &&
            map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_TOP_WIDTH_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_TOP_WIDTH_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_RIGHT_WIDTH_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_RIGHT_WIDTH_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX));
        boolean allBorderColors =
            (map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_TOP_COLOR_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_RIGHT_COLOR_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_BOTTOM_COLOR_INDEX) != null);
        boolean sameBorderColor =
            allBorderColors &&
            map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_TOP_COLOR_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_TOP_COLOR_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_RIGHT_COLOR_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_RIGHT_COLOR_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_BOTTOM_COLOR_INDEX));
        boolean allBorderStyles =
            (map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_TOP_STYLE_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_RIGHT_STYLE_INDEX) != null) &&
            (map.getValue(XhtmlCss.BORDER_BOTTOM_STYLE_INDEX) != null);
        boolean sameBorderStyle =
            allBorderStyles &&
            map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_TOP_STYLE_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_TOP_STYLE_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_RIGHT_STYLE_INDEX)) &&
            map.getValue(XhtmlCss.BORDER_RIGHT_STYLE_INDEX).equals(map.getValue(
                    XhtmlCss.BORDER_BOTTOM_STYLE_INDEX));

        if (sameBorderStyle && sameBorderWidth && sameBorderColor) {
            sb.append(CssConstants.CSS_BORDER_PROPERTY);
            sb.append(": "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX));
            used[XhtmlCss.BORDER_LEFT_WIDTH_INDEX] = true;
            used[XhtmlCss.BORDER_TOP_WIDTH_INDEX] = true;
            used[XhtmlCss.BORDER_RIGHT_WIDTH_INDEX] = true;
            used[XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX] = true;
            used[XhtmlCss.BORDER_LEFT_COLOR_INDEX] = true;
            used[XhtmlCss.BORDER_TOP_COLOR_INDEX] = true;
            used[XhtmlCss.BORDER_RIGHT_COLOR_INDEX] = true;
            used[XhtmlCss.BORDER_BOTTOM_COLOR_INDEX] = true;
            used[XhtmlCss.BORDER_LEFT_STYLE_INDEX] = true;
            used[XhtmlCss.BORDER_TOP_STYLE_INDEX] = true;
            used[XhtmlCss.BORDER_RIGHT_STYLE_INDEX] = true;
            used[XhtmlCss.BORDER_BOTTOM_STYLE_INDEX] = true;
            sb.append("; ");
        } else {
            if (allBorderWidths) {
                sb.append(CssConstants.CSS_BORDER_WIDTH_PROPERTY);
                sb.append(": "); // NOI18N

                if (sameBorderWidth) {
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX));
                } else {
                    sb.append(map.getValue(XhtmlCss.BORDER_TOP_WIDTH_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_WIDTH_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX));
                }

                used[XhtmlCss.BORDER_LEFT_WIDTH_INDEX] = true;
                used[XhtmlCss.BORDER_TOP_WIDTH_INDEX] = true;
                used[XhtmlCss.BORDER_RIGHT_WIDTH_INDEX] = true;
                used[XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX] = true;
                sb.append("; ");
            }

            if (allBorderStyles) {
                sb.append(CssConstants.CSS_BORDER_STYLE_PROPERTY);
                sb.append(": "); // NOI18N

                if (sameBorderStyle) {
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX));
                } else {
                    sb.append(map.getValue(XhtmlCss.BORDER_TOP_STYLE_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_STYLE_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_STYLE_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX));
                }

                used[XhtmlCss.BORDER_LEFT_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_TOP_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_RIGHT_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_BOTTOM_STYLE_INDEX] = true;
                sb.append("; ");
            }

            if (allBorderColors) {
                sb.append(CssConstants.CSS_BORDER_COLOR_PROPERTY);
                sb.append(": "); // NOI18N

                if (sameBorderColor) {
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX));
                } else {
                    sb.append(map.getValue(XhtmlCss.BORDER_TOP_COLOR_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_COLOR_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_COLOR_INDEX));
                    sb.append(" "); // NOI18N
                    sb.append(map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX));
                }

                used[XhtmlCss.BORDER_LEFT_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_TOP_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_RIGHT_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_BOTTOM_COLOR_INDEX] = true;
                sb.append("; ");
            }

            // TODO - do something about border-left, border-right, etc.?
            boolean allBorderTop =
                !used[XhtmlCss.BORDER_TOP_STYLE_INDEX] && !used[XhtmlCss.BORDER_TOP_COLOR_INDEX] &&
                !used[XhtmlCss.BORDER_TOP_WIDTH_INDEX] &&
                (map.getValue(XhtmlCss.BORDER_TOP_STYLE_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_TOP_COLOR_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_TOP_WIDTH_INDEX) != null);

            if (allBorderTop) {
                sb.append(CssConstants.CSS_BORDER_TOP_PROPERTY);
                sb.append(": "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_TOP_STYLE_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_TOP_COLOR_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_TOP_WIDTH_INDEX));
                sb.append("; ");
                used[XhtmlCss.BORDER_TOP_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_TOP_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_TOP_WIDTH_INDEX] = true;
            }

            boolean allBorderRight =
                !used[XhtmlCss.BORDER_RIGHT_STYLE_INDEX] &&
                !used[XhtmlCss.BORDER_RIGHT_COLOR_INDEX] &&
                !used[XhtmlCss.BORDER_RIGHT_WIDTH_INDEX] &&
                (map.getValue(XhtmlCss.BORDER_RIGHT_STYLE_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_RIGHT_COLOR_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_RIGHT_WIDTH_INDEX) != null);

            if (allBorderRight) {
                sb.append(CssConstants.CSS_BORDER_RIGHT_PROPERTY);
                sb.append(": "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_STYLE_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_COLOR_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_RIGHT_WIDTH_INDEX));
                sb.append("; ");
                used[XhtmlCss.BORDER_RIGHT_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_RIGHT_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_RIGHT_WIDTH_INDEX] = true;
            }

            boolean allBorderBottom =
                !used[XhtmlCss.BORDER_BOTTOM_STYLE_INDEX] &&
                !used[XhtmlCss.BORDER_BOTTOM_COLOR_INDEX] &&
                !used[XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX] &&
                (map.getValue(XhtmlCss.BORDER_BOTTOM_STYLE_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_BOTTOM_COLOR_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX) != null);

            if (allBorderBottom) {
                sb.append(CssConstants.CSS_BORDER_BOTTOM_PROPERTY);
                sb.append(": "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_STYLE_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_COLOR_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX));
                sb.append("; ");
                used[XhtmlCss.BORDER_BOTTOM_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_BOTTOM_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX] = true;
            }

            boolean allBorderLeft =
                !used[XhtmlCss.BORDER_LEFT_STYLE_INDEX] && !used[XhtmlCss.BORDER_LEFT_COLOR_INDEX] &&
                !used[XhtmlCss.BORDER_LEFT_WIDTH_INDEX] &&
                (map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX) != null) &&
                (map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX) != null);

            if (allBorderLeft) {
                sb.append(CssConstants.CSS_BORDER_LEFT_PROPERTY);
                sb.append(": "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_LEFT_STYLE_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_LEFT_COLOR_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.BORDER_LEFT_WIDTH_INDEX));
                sb.append("; ");
                used[XhtmlCss.BORDER_LEFT_STYLE_INDEX] = true;
                used[XhtmlCss.BORDER_LEFT_COLOR_INDEX] = true;
                used[XhtmlCss.BORDER_LEFT_WIDTH_INDEX] = true;
            }
        }

        // Look for collapsible margins
        boolean allMargins =
            (map.getValue(XhtmlCss.MARGIN_TOP_INDEX) != null) &&
            (map.getValue(XhtmlCss.MARGIN_RIGHT_INDEX) != null) &&
            (map.getValue(XhtmlCss.MARGIN_BOTTOM_INDEX) != null) &&
            (map.getValue(XhtmlCss.MARGIN_LEFT_INDEX) != null);

        if (allMargins) {
            sb.append(CssConstants.CSS_MARGIN_PROPERTY);
            sb.append(": "); // NOI18N

            // TODO - compress to two or one values if sides are the same
            sb.append(map.getValue(XhtmlCss.MARGIN_TOP_INDEX));

            if (map.getValue(XhtmlCss.MARGIN_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.MARGIN_RIGHT_INDEX)) &&
                    map.getValue(XhtmlCss.MARGIN_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.MARGIN_BOTTOM_INDEX)) &&
                    map.getValue(XhtmlCss.MARGIN_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.MARGIN_LEFT_INDEX))) {
                // Use a single value to represent all four sides.
                // I could also look to see if the horizontal and vertical sides are identical
                // and compress to two values but I'm getting bored
            } else {
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.MARGIN_RIGHT_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.MARGIN_BOTTOM_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.MARGIN_LEFT_INDEX));
            }

            sb.append("; ");
            used[XhtmlCss.MARGIN_TOP_INDEX] = true;
            used[XhtmlCss.MARGIN_RIGHT_INDEX] = true;
            used[XhtmlCss.MARGIN_BOTTOM_INDEX] = true;
            used[XhtmlCss.MARGIN_LEFT_INDEX] = true;
        }

        // Look for collapsible padding
        boolean allPadding =
            (map.getValue(XhtmlCss.PADDING_TOP_INDEX) != null) &&
            (map.getValue(XhtmlCss.PADDING_RIGHT_INDEX) != null) &&
            (map.getValue(XhtmlCss.PADDING_BOTTOM_INDEX) != null) &&
            (map.getValue(XhtmlCss.PADDING_LEFT_INDEX) != null);

        if (allPadding) {
            sb.append(CssConstants.CSS_PADDING_PROPERTY);
            sb.append(": "); // NOI18N

            // TODO - compress to two or one values if sides are the same
            sb.append(map.getValue(XhtmlCss.PADDING_TOP_INDEX));

            if (map.getValue(XhtmlCss.PADDING_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.PADDING_RIGHT_INDEX)) &&
                    map.getValue(XhtmlCss.PADDING_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.PADDING_BOTTOM_INDEX)) &&
                    map.getValue(XhtmlCss.PADDING_TOP_INDEX).equals(map.getValue(
                            XhtmlCss.PADDING_LEFT_INDEX))) {
                // Use a single value to represent all four sides.
                // I could also look to see if the horizontal and vertical sides are identical
                // and compress to two values but I'm getting bored
            } else {
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.PADDING_RIGHT_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.PADDING_BOTTOM_INDEX));
                sb.append(" "); // NOI18N
                sb.append(map.getValue(XhtmlCss.PADDING_LEFT_INDEX));
            }

            sb.append("; ");
            used[XhtmlCss.PADDING_TOP_INDEX] = true;
            used[XhtmlCss.PADDING_RIGHT_INDEX] = true;
            used[XhtmlCss.PADDING_BOTTOM_INDEX] = true;
            used[XhtmlCss.PADDING_LEFT_INDEX] = true;
        }

        // Background
        boolean allBackground =
            (map.getValue(XhtmlCss.BACKGROUND_COLOR_INDEX) != null) && // OR: same as default
            (map.getValue(XhtmlCss.BACKGROUND_IMAGE_INDEX) != null) &&
            (map.getValue(XhtmlCss.BACKGROUND_POSITION_INDEX) != null) &&
            // TODO: (map.getValue(XhtmlCss.BACKGROUND_ATTACHMENT_INDEX) != null) &&
            (map.getValue(XhtmlCss.BACKGROUND_REPEAT_INDEX) != null);

        if (allBackground) {
            sb.append(CssConstants.CSS_BACKGROUND_PROPERTY);
            sb.append(": "); // NOI18N

            // TODO -- only do if different from the default!
            sb.append(map.getValue(XhtmlCss.BACKGROUND_COLOR_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BACKGROUND_IMAGE_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BACKGROUND_REPEAT_INDEX));

            //sb.append(" "); // NOI18N
            //sb.append(map.getValue(XhtmlCss.BACKGROUND_ATTACHMENT_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.BACKGROUND_POSITION_INDEX));
            sb.append("; ");
            used[XhtmlCss.BACKGROUND_COLOR_INDEX] = true;
            used[XhtmlCss.BACKGROUND_IMAGE_INDEX] = true;
            used[XhtmlCss.BACKGROUND_POSITION_INDEX] = true;
            used[XhtmlCss.BACKGROUND_REPEAT_INDEX] = true;

            //used[XhtmlCss.BACKGROUND_ATTACHMENT_INDEX] = true;
        }

        // List Style
        boolean allListStyles =
            (map.getValue(XhtmlCss.LIST_STYLE_TYPE_INDEX) != null) &&
            //(map.getValue(XhtmlCss.LIST_STYLE_POSITION_INDEX) != null) &&
            (map.getValue(XhtmlCss.LIST_STYLE_IMAGE_INDEX) != null);

        if (allListStyles) {
            sb.append(CssConstants.CSS_LIST_STYLE_PROPERTY);
            sb.append(": "); // NOI18N
            sb.append(map.getValue(XhtmlCss.LIST_STYLE_TYPE_INDEX));
            sb.append(" "); // NOI18N
            sb.append(map.getValue(XhtmlCss.LIST_STYLE_IMAGE_INDEX));

            //sb.append(" "); // NOI18N
            //sb.append(map.getValue(XhtmlCss.LIST_STYLE_POSITION_INDEX));
            sb.append("; ");
            used[XhtmlCss.LIST_STYLE_TYPE_INDEX] = true;
            used[XhtmlCss.LIST_STYLE_IMAGE_INDEX] = true;

            //used[XhtmlCss.LIST_STYLE_POSITION_INDEX] = true;
        }

        // TODO: font
        // Write out all items in the map we haven't already covered by a shorthand property
        boolean first = true;

        for (int i = 0; i < size; i++) {
            if (used[i]) {
                // Already processed as a shorthand
                continue;
            }

            Value v = map.getValue(i);

            if (v != null) {
                if (first) {
                    first = false;
                } else {
                    sb.append("; ");
                }

                sb.append(getPropertyName(i));
                sb.append(": ");
                sb.append(v);

                if (map.isImportant(i)) {
                    sb.append(" !important");
                }
            }
        }

        return sb.toString();
    }

    String computeFileName(Object location) {
        UserAgent userAgent = getUserAgent();
        if (userAgent == null) {
            return location == null ? null : location.toString();
        }
        
        return userAgent.computeFileName(location);
    }
    

    int computeLineNumber(Object location, int lineno) {
        UserAgent userAgent = getUserAgent();
        if (userAgent == null) {
            return lineno;
        }
        
        return userAgent.computeLineNumber(location, lineno);
    }

    URL getDocumentUrl() {
        UserAgent userAgent = getUserAgent();
        if (userAgent == null) {
            return null;
        }
        
        return userAgent.getDocumentUrl(getDocument());
    }

    private UserAgent getUserAgent() {
        CSSEngineUserAgent cssEngineUserAgent = getCSSEngineUserAgent();
        if (cssEngineUserAgent instanceof UserAgent) {
            return (UserAgent)cssEngineUserAgent;
        }
        return null;
    }


    /** Class used to wrap serialized Strings as batik values and get
     * the right behavior out of the style map serialization routines
     * without having the Strings modified in anyway. These Strings
     * aren't necessarily Strings in the style - the may represent
     * floats, urls, etc.
     */
    class MapStringValue extends AbstractValue {
        private String s;

        MapStringValue(String s) {
            this.s = s;
        }

        public String getCssText() {
            return s;
        }

        public boolean equals(Object obj) {
            if (obj instanceof MapStringValue) {
                return s.equals(((MapStringValue)obj).s);
            }

            return false;
        }

        public String toString() {
            return s;
        }
    }
    
}
