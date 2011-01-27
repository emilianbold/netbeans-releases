/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.parser.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;

/**
 *
 * @author marekfukala
 */
public enum ElementDescriptor {

    //section generated from the whatwg specification at http://www.whatwg.org/specs/web-apps/current-work
    //by the GenerateElementsIndex unit test
    //
    //>>>>>>>>>>>>>>>>>>>>>>>>

    //NOI18N
A(
        HtmlTagType.HTML,
        new Link("a", "text-level-semantics.html#the-a-element"),
         "Hyperlink",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_MEDIA, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE),
        new Link("HTMLAnchorElement", "text-level-semantics.html#htmlanchorelement")
),

ABBR(
        HtmlTagType.HTML,
        new Link("abbr", "text-level-semantics.html#the-abbr-element"),
         "Abbreviation",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

ADDRESS(
        HtmlTagType.HTML,
        new Link("address", "sections.html#the-address-element"),
         "Contact information for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

AREA(
        HtmlTagType.HTML,
        new Link("area", "the-map-element.html#the-area-element"),
         "Hyperlink or dead area on an image map",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_AREA_ALT, Attribute.ATTR_AREA_COORDS, Attribute.ATTR_AREA_SHAPE, Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_MEDIA, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE),
        new Link("HTMLAreaElement", "the-map-element.html#htmlareaelement")
),

ARTICLE(
        HtmlTagType.HTML,
        new Link("article", "sections.html#the-article-element"),
         "Self-contained syndicatable or reusable composition",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

ASIDE(
        HtmlTagType.HTML,
        new Link("aside", "sections.html#the-aside-element"),
         "Sidebar for tangentially related content",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

AUDIO(
        HtmlTagType.HTML,
        new Link("audio", "video.html#audio"),
         "Audio player",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{"source"},
        EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_CONTROLS),
        new Link("HTMLAudioElement", "video.html#htmlaudioelement")
),

B(
        HtmlTagType.HTML,
        new Link("b", "text-level-semantics.html#the-b-element"),
         "Keywords",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

BASE(
        HtmlTagType.HTML,
        new Link("base", "semantics.html#the-base-element"),
         "Base URL and default target browsing context for hyperlinks and forms",
        EnumSet.of(ContentType.METADATA),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_BASE_HREF, Attribute.ATTR_BASE_TARGET),
        new Link("HTMLBaseElement", "semantics.html#htmlbaseelement")
),

BDO(
        HtmlTagType.HTML,
        new Link("bdo", "text-level-semantics.html#the-bdo-element"),
         "Text directionality formatting",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

BLOCKQUOTE(
        HtmlTagType.HTML,
        new Link("blockquote", "grouping-content.html#the-blockquote-element"),
         "A section quoted from another source",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_BLOCKQUOTE_CITE),
        new Link("HTMLQuoteElement", "grouping-content.html#htmlquoteelement")
),

BODY(
        HtmlTagType.HTML,
        new Link("body", "sections.html#the-body-element-0"),
         "Document body",
        EnumSet.of(ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"html"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.HANDLER_WINDOW_ONAFTERPRINT, Attribute.HANDLER_WINDOW_ONBEFOREPRINT, Attribute.HANDLER_WINDOW_ONBEFOREUNLOAD, Attribute.HANDLER_WINDOW_ONBLUR, Attribute.HANDLER_WINDOW_ONERROR, Attribute.HANDLER_WINDOW_ONFOCUS, Attribute.HANDLER_WINDOW_ONHASHCHANGE, Attribute.HANDLER_WINDOW_ONLOAD, Attribute.HANDLER_WINDOW_ONMESSAGE, Attribute.HANDLER_WINDOW_ONOFFLINE, Attribute.HANDLER_WINDOW_ONONLINE, Attribute.HANDLER_WINDOW_ONPAGEHIDE, Attribute.HANDLER_WINDOW_ONPAGESHOW, Attribute.HANDLER_WINDOW_ONPOPSTATE, Attribute.HANDLER_WINDOW_ONREDO, Attribute.HANDLER_WINDOW_ONRESIZE, Attribute.HANDLER_WINDOW_ONSTORAGE, Attribute.HANDLER_WINDOW_ONUNDO, Attribute.HANDLER_WINDOW_ONUNLOAD),
        new Link("HTMLBodyElement", "sections.html#htmlbodyelement")
),

BR(
        HtmlTagType.HTML,
        new Link("br", "text-level-semantics.html#the-br-element"),
         "Line break, e.g. in poem or postal address",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLBRElement", "text-level-semantics.html#htmlbrelement")
),

BUTTON(
        HtmlTagType.HTML,
        new Link("button", "the-button-element.html#the-button-element"),
         "Button control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_FE_NAME, Attribute.ATTR_BUTTON_TYPE, Attribute.ATTR_BUTTON_VALUE),
        new Link("HTMLButtonElement", "the-button-element.html#htmlbuttonelement")
),

CANVAS(
        HtmlTagType.HTML,
        new Link("canvas", "the-canvas-element.html#the-canvas-element"),
         "Scriptable bitmap canvas",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_CANVAS_WIDTH, Attribute.ATTR_CANVAS_HEIGHT),
        new Link("HTMLCanvasElement", "the-canvas-element.html#htmlcanvaselement")
),

CAPTION(
        HtmlTagType.HTML,
        new Link("caption", "tabular-data.html#the-caption-element"),
         "Table caption",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableCaptionElement", "tabular-data.html#htmltablecaptionelement")
),

CITE(
        HtmlTagType.HTML,
        new Link("cite", "text-level-semantics.html#the-cite-element"),
         "Title of a work",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

CODE(
        HtmlTagType.HTML,
        new Link("code", "text-level-semantics.html#the-code-element"),
         "Computer code",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

COL(
        HtmlTagType.HTML,
        new Link("col", "tabular-data.html#the-col-element"),
         "Table column",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"colgroup"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_COL_SPAN),
        new Link("HTMLTableColElement", "tabular-data.html#htmltablecolelement")
),

COLGROUP(
        HtmlTagType.HTML,
        new Link("colgroup", "tabular-data.html#the-colgroup-element"),
         "Group of columns in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"col"},
        EnumSet.of(Attribute.ATTR_COLGROUP_SPAN),
        new Link("HTMLTableColElement", "tabular-data.html#htmltablecolelement")
),

COMMAND(
        HtmlTagType.HTML,
        new Link("command", "interactive-elements.html#the-command"),
         "Menu command",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_COMMAND_TYPE, Attribute.ATTR_COMMAND_LABEL, Attribute.ATTR_COMMAND_ICON, Attribute.ATTR_COMMAND_DISABLED, Attribute.ATTR_COMMAND_CHECKED, Attribute.ATTR_COMMAND_RADIOGROUP),
        new Link("HTMLCommandElement", "interactive-elements.html#htmlcommandelement")
),

DATALIST(
        HtmlTagType.HTML,
        new Link("datalist", "the-button-element.html#the-datalist-element"),
         "Container for options for combo box control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{"option"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDataListElement", "the-button-element.html#htmldatalistelement")
),

DD(
        HtmlTagType.HTML,
        new Link("dd", "grouping-content.html#the-dd-element"),
         "Content for corresponding dt element(s)",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"dl"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

DEL(
        HtmlTagType.HTML,
        new Link("del", "edits.html#the-del-element"),
         "A removal from the document",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME),
        new Link("HTMLModElement", "edits.html#htmlmodelement")
),

DETAILS(
        HtmlTagType.HTML,
        new Link("details", "interactive-elements.html#the-details-element"),
         "Disclosure control for hiding details",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"summary"},
        EnumSet.of(Attribute.ATTR_DETAILS_OPEN),
        new Link("HTMLDetailsElement", "interactive-elements.html#htmldetailselement")
),

DFN(
        HtmlTagType.HTML,
        new Link("dfn", "text-level-semantics.html#the-dfn-element"),
         "Defining instance",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

DIV(
        HtmlTagType.HTML,
        new Link("div", "grouping-content.html#the-div-element"),
         "Generic flow container",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDivElement", "grouping-content.html#htmldivelement")
),

DL(
        HtmlTagType.HTML,
        new Link("dl", "grouping-content.html#the-dl-element"),
         "Association list consisting of zero or more name-value groups",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"dt", "dd"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDListElement", "grouping-content.html#htmldlistelement")
),

DT(
        HtmlTagType.HTML,
        new Link("dt", "grouping-content.html#the-dt-element"),
         "Legend for corresponding dd element(s)",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"dl"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

EM(
        HtmlTagType.HTML,
        new Link("em", "text-level-semantics.html#the-em-element"),
         "Stress emphasis",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

EMBED(
        HtmlTagType.HTML,
        new Link("embed", "the-iframe-element.html#the-embed-element"),
         "Plugin",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_EMBED_SRC, Attribute.ATTR_EMBED_TYPE, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLEmbedElement", "the-iframe-element.html#htmlembedelement")
),

FIELDSET(
        HtmlTagType.HTML,
        new Link("fieldset", "forms.html#the-fieldset-element"),
         "Group of form controls",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.of(FormAssociatedElementsCategory.LISTED),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"legend"},
        EnumSet.of(Attribute.ATTR_FIELDSET_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME),
        new Link("HTMLFieldSetElement", "forms.html#htmlfieldsetelement")
),

FIGCAPTION(
        HtmlTagType.HTML,
        new Link("figcaption", "grouping-content.html#the-figcaption-element"),
         "Caption for figure",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"figure"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

FIGURE(
        HtmlTagType.HTML,
        new Link("figure", "grouping-content.html#the-figure-element"),
         "Figure with optional caption",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"figcaption"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

FOOTER(
        HtmlTagType.HTML,
        new Link("footer", "sections.html#the-footer-element"),
         "Footer for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

FORM(
        HtmlTagType.HTML,
        new Link("form", "forms.html#the-form-element"),
         "User-submittable form",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FORM_ACCEPT_CHARSET, Attribute.ATTR_FS_ACTION, Attribute.ATTR_FORM_AUTOCOMPLETE, Attribute.ATTR_FS_ENCTYPE, Attribute.ATTR_FS_METHOD, Attribute.ATTR_FORM_NAME, Attribute.ATTR_FS_NOVALIDATE, Attribute.ATTR_FS_TARGET),
        new Link("HTMLFormElement", "forms.html#htmlformelement")
),

H1(
        HtmlTagType.HTML,
        new Link("h1", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

H2(
        HtmlTagType.HTML,
        new Link("h2", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

H3(
        HtmlTagType.HTML,
        new Link("h3", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

H4(
        HtmlTagType.HTML,
        new Link("h4", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

H5(
        HtmlTagType.HTML,
        new Link("h5", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

H6(
        HtmlTagType.HTML,
        new Link("h6", "sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "Section heading",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "sections.html#htmlheadingelement")
),

HEAD(
        HtmlTagType.HTML,
        new Link("head", "semantics.html#the-head-element-0"),
         "Container for document metadata",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"html"},
        EnumSet.of(ContentType.METADATA),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadElement", "semantics.html#htmlheadelement")
),

HEADER(
        HtmlTagType.HTML,
        new Link("header", "sections.html#the-header-element"),
         "Introductory or navigational aids for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

HGROUP(
        HtmlTagType.HTML,
        new Link("hgroup", "sections.html#the-hgroup-element"),
         "heading group",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"h1", "h2", "h3", "h4", "h5", "h6"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

HR(
        HtmlTagType.HTML,
        new Link("hr", "grouping-content.html#the-hr-element"),
         "Thematic break",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHRElement", "grouping-content.html#htmlhrelement")
),

HTML(
        HtmlTagType.HTML,
        new Link("html", "semantics.html#the-html-element-0"),
         "Root element",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"head", "body"},
        EnumSet.of(Attribute.ATTR_HTML_MANIFEST),
        new Link("HTMLHtmlElement", "semantics.html#htmlhtmlelement")
),

I(
        HtmlTagType.HTML,
        new Link("i", "text-level-semantics.html#the-i-element"),
         "Alternate voice",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

IFRAME(
        HtmlTagType.HTML,
        new Link("iframe", "the-iframe-element.html#the-iframe-element"),
         "Nested browsing context",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_IFRAME_SRC, Attribute.ATTR_IFRAME_SRCDOC, Attribute.ATTR_IFRAME_NAME, Attribute.ATTR_IFRAME_SANDBOX, Attribute.ATTR_IFRAME_SEAMLESS, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLIFrameElement", "the-iframe-element.html#htmliframeelement")
),

IMG(
        HtmlTagType.HTML,
        new Link("img", "embedded-content-1.html#the-img-element"),
         "Image",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_IMG_ALT, Attribute.ATTR_IMG_SRC, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_IMG_ISMAP, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLImageElement", "embedded-content-1.html#htmlimageelement")
),

INPUT(
        HtmlTagType.HTML,
        new Link("input", "the-input-element.html#the-input-element"),
         "Form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_INPUT_ACCEPT, Attribute.ATTR_INPUT_ALT, Attribute.ATTR_INPUT_AUTOCOMPLETE, Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_INPUT_CHECKED, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_DIM_HEIGHT, Attribute.ATTR_INPUT_LIST, Attribute.ATTR_INPUT_MAX, Attribute.ATTR_INPUT_MAXLENGTH, Attribute.ATTR_INPUT_MIN, Attribute.ATTR_INPUT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_INPUT_PATTERN, Attribute.ATTR_INPUT_PLACEHOLDER, Attribute.ATTR_INPUT_READONLY, Attribute.ATTR_INPUT_REQUIRED, Attribute.ATTR_INPUT_SIZE, Attribute.ATTR_INPUT_SRC, Attribute.ATTR_INPUT_STEP, Attribute.ATTR_INPUT_TYPE, Attribute.ATTR_INPUT_VALUE, Attribute.ATTR_DIM_WIDTH),
        new Link("HTMLInputElement", "the-input-element.html#htmlinputelement")
),

INS(
        HtmlTagType.HTML,
        new Link("ins", "edits.html#the-ins-element"),
         "An addition to the document",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME),
        new Link("HTMLModElement", "edits.html#htmlmodelement")
),

KBD(
        HtmlTagType.HTML,
        new Link("kbd", "text-level-semantics.html#the-kbd-element"),
         "User input",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

KEYGEN(
        HtmlTagType.HTML,
        new Link("keygen", "the-button-element.html#the-keygen-element"),
         "Cryptographic key-pair generator form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_KEYGEN_CHALLENGE, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_KEYGEN_KEYTYPE, Attribute.ATTR_FE_NAME),
        new Link("HTMLKeygenElement", "the-button-element.html#htmlkeygenelement")
),

LABEL(
        HtmlTagType.HTML,
        new Link("label", "forms.html#the-label-element"),
         "Caption for a form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FAE_FORM, Attribute.ATTR_LABEL_FOR),
        new Link("HTMLLabelElement", "forms.html#htmllabelelement")
),

LEGEND(
        HtmlTagType.HTML,
        new Link("legend", "forms.html#the-legend-element"),
         "Caption for fieldset",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"fieldset"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLLegendElement", "forms.html#htmllegendelement")
),

LI(
        HtmlTagType.HTML,
        new Link("li", "grouping-content.html#the-li-element"),
         "List item",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ol", "ul", "menu"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_LI_VALUE),
        new Link("HTMLLIElement", "grouping-content.html#htmllielement")
),

LINK(
        HtmlTagType.HTML,
        new Link("link", "semantics.html#the-link-element"),
         "Link metadata",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_LINK_HREF, Attribute.ATTR_LINK_REL, Attribute.ATTR_LINK_MEDIA, Attribute.ATTR_LINK_HREFLANG, Attribute.ATTR_LINK_TYPE, Attribute.ATTR_LINK_SIZES),
        new Link("HTMLLinkElement", "semantics.html#htmllinkelement")
),

MAP(
        HtmlTagType.HTML,
        new Link("map", "the-map-element.html#the-map-element"),
         "Image map",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{"area"},
        EnumSet.of(Attribute.ATTR_MAP_NAME),
        new Link("HTMLMapElement", "the-map-element.html#htmlmapelement")
),

MARK(
        HtmlTagType.HTML,
        new Link("mark", "text-level-semantics.html#the-mark-element"),
         "Highlight",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

MENU(
        HtmlTagType.HTML,
        new Link("menu", "interactive-elements.html#menus"),
         "Menu of commands",
        EnumSet.of(ContentType.FLOW, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"li"},
        EnumSet.of(Attribute.ATTR_MENU_TYPE, Attribute.ATTR_MENU_LABEL),
        new Link("HTMLMenuElement", "interactive-elements.html#htmlmenuelement")
),

META(
        HtmlTagType.HTML,
        new Link("meta", "semantics.html#meta"),
         "Text metadata",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_META_NAME, Attribute.ATTR_META_HTTP_EQUIV, Attribute.ATTR_META_CONTENT, Attribute.ATTR_META_CHARSET),
        new Link("HTMLMetaElement", "semantics.html#htmlmetaelement")
),

METER(
        HtmlTagType.HTML,
        new Link("meter", "the-button-element.html#the-meter-element"),
         "Gauge",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LABELABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_METER_VALUE, Attribute.ATTR_METER_MIN, Attribute.ATTR_METER_MAX, Attribute.ATTR_METER_LOW, Attribute.ATTR_METER_HIGH, Attribute.ATTR_METER_OPTIMUM, Attribute.ATTR_FAE_FORM),
        new Link("HTMLMeterElement", "the-button-element.html#htmlmeterelement")
),

NAV(
        HtmlTagType.HTML,
        new Link("nav", "sections.html#the-nav-element"),
         "Section with navigational links",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

NOSCRIPT(
        HtmlTagType.HTML,
        new Link("noscript", "scripting-1.html#the-noscript-element"),
         "Fallback content for script",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

OBJECT(
        HtmlTagType.HTML,
        new Link("object", "the-iframe-element.html#the-object-element"),
         "Image, nested browsing context, or plugin",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.SUBMITTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{"param"},
        EnumSet.of(Attribute.ATTR_OBJECT_DATA, Attribute.ATTR_OBJECT_TYPE, Attribute.ATTR_OBJECT_NAME, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_FAE_FORM, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLObjectElement", "the-iframe-element.html#htmlobjectelement")
),

OL(
        HtmlTagType.HTML,
        new Link("ol", "grouping-content.html#the-ol-element"),
         "Ordered list",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"li"},
        EnumSet.of(Attribute.ATTR_OL_REVERSED, Attribute.ATTR_OL_START),
        new Link("HTMLOListElement", "grouping-content.html#htmlolistelement")
),

OPTGROUP(
        HtmlTagType.HTML,
        new Link("optgroup", "the-button-element.html#the-optgroup-element"),
         "Group of options in a list box",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"select"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"option"},
        EnumSet.of(Attribute.ATTR_OPTGROUP_DISABLED, Attribute.ATTR_OPTGROUP_LABEL),
        new Link("HTMLOptGroupElement", "the-button-element.html#htmloptgroupelement")
),

OPTION(
        HtmlTagType.HTML,
        new Link("option", "the-button-element.html#the-option-element"),
         "Option in a list box or combo box control",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"select", "datalist", "optgroup"},
        EnumSet.of(ContentType.TEXT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_OPTION_DISABLED, Attribute.ATTR_OPTION_LABEL, Attribute.ATTR_OPTION_SELECTED, Attribute.ATTR_OPTION_VALUE),
        new Link("HTMLOptionElement", "the-button-element.html#htmloptionelement")
),

OUTPUT(
        HtmlTagType.HTML,
        new Link("output", "the-button-element.html#the-output-element"),
         "Calculated output value",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_OUTPUT_FOR, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME),
        new Link("HTMLOutputElement", "the-button-element.html#htmloutputelement")
),

P(
        HtmlTagType.HTML,
        new Link("p", "grouping-content.html#the-p-element"),
         "Paragraph",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLParagraphElement", "grouping-content.html#htmlparagraphelement")
),

PARAM(
        HtmlTagType.HTML,
        new Link("param", "the-iframe-element.html#the-param-element"),
         "Parameter for object",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"object"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_PARAM_NAME, Attribute.ATTR_PARAM_VALUE),
        new Link("HTMLParamElement", "the-iframe-element.html#htmlparamelement")
),

PRE(
        HtmlTagType.HTML,
        new Link("pre", "grouping-content.html#the-pre-element"),
         "Block of preformatted text",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLPreElement", "grouping-content.html#htmlpreelement")
),

PROGRESS(
        HtmlTagType.HTML,
        new Link("progress", "the-button-element.html#the-progress-element"),
         "Progress bar",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LABELABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_PROGRESS_VALUE, Attribute.ATTR_PROGRESS_MAX, Attribute.ATTR_FAE_FORM),
        new Link("HTMLProgressElement", "the-button-element.html#htmlprogresselement")
),

Q(
        HtmlTagType.HTML,
        new Link("q", "text-level-semantics.html#the-q-element"),
         "Quotation",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_Q_CITE),
        new Link("HTMLQuoteElement", "grouping-content.html#htmlquoteelement")
),

RP(
        HtmlTagType.HTML,
        new Link("rp", "text-level-semantics.html#the-rp-element"),
         "Parenthesis for ruby annotation text",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ruby"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

RT(
        HtmlTagType.HTML,
        new Link("rt", "text-level-semantics.html#the-rt-element"),
         "Ruby annotation text",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ruby"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

RUBY(
        HtmlTagType.HTML,
        new Link("ruby", "text-level-semantics.html#the-ruby-element"),
         "Ruby annotation(s)",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{"rt", "rp"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SAMP(
        HtmlTagType.HTML,
        new Link("samp", "text-level-semantics.html#the-samp-element"),
         "Computer output",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SCRIPT(
        HtmlTagType.HTML,
        new Link("script", "scripting-1.html#script"),
         "Embedded script",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_SCRIPT_SRC, Attribute.ATTR_SCRIPT_ASYNC, Attribute.ATTR_SCRIPT_DEFER, Attribute.ATTR_SCRIPT_TYPE, Attribute.ATTR_SCRIPT_CHARSET),
        new Link("HTMLScriptElement", "scripting-1.html#htmlscriptelement")
),

SECTION(
        HtmlTagType.HTML,
        new Link("section", "sections.html#the-section-element"),
         "Generic document or application section",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SELECT(
        HtmlTagType.HTML,
        new Link("select", "the-button-element.html#the-select-element"),
         "List box control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"option", "optgroup"},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_SELECT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_SELECT_REQUIRED, Attribute.ATTR_SELECT_SIZE),
        new Link("HTMLSelectElement", "the-button-element.html#htmlselectelement")
),

SMALL(
        HtmlTagType.HTML,
        new Link("small", "text-level-semantics.html#the-small-element"),
         "Side comment",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SOURCE(
        HtmlTagType.HTML,
        new Link("source", "video.html#the-source-element"),
         "Media source for video or audio",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"video", "audio"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_SOURCE_SRC, Attribute.ATTR_SOURCE_TYPE, Attribute.ATTR_SOURCE_MEDIA),
        new Link("HTMLSourceElement", "video.html#htmlsourceelement")
),

SPAN(
        HtmlTagType.HTML,
        new Link("span", "text-level-semantics.html#the-span-element"),
         "Generic phrasing container",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLSpanElement", "text-level-semantics.html#htmlspanelement")
),

STRONG(
        HtmlTagType.HTML,
        new Link("strong", "text-level-semantics.html#the-strong-element"),
         "Importance",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

STYLE(
        HtmlTagType.HTML,
        new Link("style", "semantics.html#the-style-element"),
         "Embedded styling information",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_STYLE_MEDIA, Attribute.ATTR_STYLE_TYPE, Attribute.ATTR_STYLE_SCOPED),
        new Link("HTMLStyleElement", "semantics.html#htmlstyleelement")
),

SUB(
        HtmlTagType.HTML,
        new Link("sub", "text-level-semantics.html#the-sub-and-sup-elements"),
         "Subscript",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SUMMARY(
        HtmlTagType.HTML,
        new Link("summary", "interactive-elements.html#the-summary-element"),
         "Caption for details",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"details"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

SUP(
        HtmlTagType.HTML,
        new Link("sup", "text-level-semantics.html#the-sub-and-sup-elements"),
         "Superscript",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

TABLE(
        HtmlTagType.HTML,
        new Link("table", "tabular-data.html#the-table-element"),
         "Table",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"caption", "colgroup", "thead", "tbody", "tfoot", "tr"},
        EnumSet.of(Attribute.ATTR_TABLE_SUMMARY),
        new Link("HTMLTableElement", "tabular-data.html#htmltableelement")
),

TBODY(
        HtmlTagType.HTML,
        new Link("tbody", "tabular-data.html#the-tbody-element"),
         "Group of rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "tabular-data.html#htmltablesectionelement")
),

TD(
        HtmlTagType.HTML,
        new Link("td", "tabular-data.html#the-td-element"),
         "Table cell",
        EnumSet.of(ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS),
        new Link("HTMLTableDataCellElement", "tabular-data.html#htmltabledatacellelement")
),

TEXTAREA(
        HtmlTagType.HTML,
        new Link("textarea", "the-button-element.html#the-textarea-element"),
         "Multiline text field",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TEXT),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_TEXTAREA_COLS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_TEXTAREA_MAXLENGTH, Attribute.ATTR_FE_NAME, Attribute.ATTR_TEXTAREA_PLACEHOLDER, Attribute.ATTR_TEXTAREA_READONLY, Attribute.ATTR_TEXTAREA_REQUIRED, Attribute.ATTR_TEXTAREA_ROWS, Attribute.ATTR_TEXTAREA_WRAP),
        new Link("HTMLTextAreaElement", "the-button-element.html#htmltextareaelement")
),

TFOOT(
        HtmlTagType.HTML,
        new Link("tfoot", "tabular-data.html#the-tfoot-element"),
         "Group of footer rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "tabular-data.html#htmltablesectionelement")
),

TH(
        HtmlTagType.HTML,
        new Link("th", "tabular-data.html#the-th-element"),
         "Table header cell",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS, Attribute.ATTR_TH_SCOPE),
        new Link("HTMLTableHeaderCellElement", "tabular-data.html#htmltableheadercellelement")
),

THEAD(
        HtmlTagType.HTML,
        new Link("thead", "tabular-data.html#the-thead-element"),
         "Group of heading rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "tabular-data.html#htmltablesectionelement")
),

TIME(
        HtmlTagType.HTML,
        new Link("time", "text-level-semantics.html#the-time-element"),
         "Date and/or time",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TIME_DATETIME, Attribute.ATTR_TIME_PUBDATE),
        new Link("HTMLTimeElement", "text-level-semantics.html#htmltimeelement")
),

TITLE(
        HtmlTagType.HTML,
        new Link("title", "semantics.html#the-title-element-0"),
         "Document title",
        EnumSet.of(ContentType.METADATA),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"head"},
        EnumSet.of(ContentType.TEXT),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTitleElement", "semantics.html#htmltitleelement")
),

TR(
        HtmlTagType.HTML,
        new Link("tr", "tabular-data.html#the-tr-element"),
         "Table row",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table", "thead", "tbody", "tfoot"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"th", "td"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableRowElement", "tabular-data.html#htmltablerowelement")
),

TRACK(
        HtmlTagType.HTML,
        new Link("track", "video.html#the-track-element"),
         "Timed track",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"audio", "video"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TRACK_KIND, Attribute.ATTR_TRACK_LABEL, Attribute.ATTR_TRACK_SRC, Attribute.ATTR_TRACK_SRCLANG),
        new Link("HTMLTrackElement", "video.html#htmltrackelement")
),

UL(
        HtmlTagType.HTML,
        new Link("ul", "grouping-content.html#the-ul-element"),
         "List",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"li"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLUListElement", "grouping-content.html#htmlulistelement")
),

VAR(
        HtmlTagType.HTML,
        new Link("var", "text-level-semantics.html#the-var-element"),
         "Variable",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),

VIDEO(
        HtmlTagType.HTML,
        new Link("video", "video.html#video"),
         "Video player",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.TRANSPARENT),
        new String[]{"source"},
        EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_VIDEO_POSTER, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_CONTROLS, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLVideoElement", "video.html#htmlvideoelement")
),

WBR(
        HtmlTagType.HTML,
        new Link("wbr", "text-level-semantics.html#the-wbr-element"),
         "Line breaking opportunity",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "elements.html#htmlelement")
),


//MATHML elements:
//-----------------------

DIVERGENCE(
        HtmlTagType.MATHML,
        new Link("divergence", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOS(
        HtmlTagType.MATHML,
        new Link("arccos", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DIFF(
        HtmlTagType.MATHML,
        new Link("diff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CURL(
        HtmlTagType.MATHML,
        new Link("curl", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOT(
        HtmlTagType.MATHML,
        new Link("arccot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OTHERWISE(
        HtmlTagType.MATHML,
        new Link("otherwise", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LAPLACIAN(
        HtmlTagType.MATHML,
        new Link("laplacian", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REALS(
        HtmlTagType.MATHML,
        new Link("reals", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANNOTATION(
        HtmlTagType.MATHML,
        new Link("annotation", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTANUMBER(
        HtmlTagType.MATHML,
        new Link("notanumber", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LAMBDA(
        HtmlTagType.MATHML,
        new Link("lambda", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARG(
        HtmlTagType.MATHML,
        new Link("arg", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NATURALNUMBERS(
        HtmlTagType.MATHML,
        new Link("naturalnumbers", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ABS(
        HtmlTagType.MATHML,
        new Link("abs", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEMANTICS(
        HtmlTagType.MATHML,
        new Link("semantics", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTEXT(
        HtmlTagType.MATHML,
        new Link("mtext", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CODOMAIN(
        HtmlTagType.MATHML,
        new Link("codomain", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTSUBSET(
        HtmlTagType.MATHML,
        new Link("notsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FALSE(
        HtmlTagType.MATHML,
        new Link("false", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COMPOSE(
        HtmlTagType.MATHML,
        new Link("compose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECEWISE(
        HtmlTagType.MATHML,
        new Link("piecewise", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFRAC(
        HtmlTagType.MATHML,
        new Link("mfrac", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FN(
        HtmlTagType.MATHML,
        new Link("fn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECE(
        HtmlTagType.MATHML,
        new Link("piece", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFRACTION(
        HtmlTagType.MATHML,
        new Link("mfraction", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LIMIT(
        HtmlTagType.MATHML,
        new Link("limit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PLUS(
        HtmlTagType.MATHML,
        new Link("plus", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATRIXROW(
        HtmlTagType.MATHML,
        new Link("matrixrow", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SECH(
        HtmlTagType.MATHML,
        new Link("sech", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OR(
        HtmlTagType.MATHML,
        new Link("or", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUBSUP(
        HtmlTagType.MATHML,
        new Link("msubsup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCSCH(
        HtmlTagType.MATHML,
        new Link("arccsch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRSUBSET(
        HtmlTagType.MATHML,
        new Link("prsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MROOT(
        HtmlTagType.MATHML,
        new Link("mroot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGE(
        HtmlTagType.MATHML,
        new Link("image", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCTANH(
        HtmlTagType.MATHML,
        new Link("arctanh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TENDSTO(
        HtmlTagType.MATHML,
        new Link("tendsto", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOG(
        HtmlTagType.MATHML,
        new Link("log", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DECLARE(
        HtmlTagType.MATHML,
        new Link("declare", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOT(
        HtmlTagType.MATHML,
        new Link("not", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MEDIAN(
        HtmlTagType.MATHML,
        new Link("median", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FLOOR(
        HtmlTagType.MATHML,
        new Link("floor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOWLIMIT(
        HtmlTagType.MATHML,
        new Link("lowlimit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GT(
        HtmlTagType.MATHML,
        new Link("gt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PI(
        HtmlTagType.MATHML,
        new Link("pi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TAN(
        HtmlTagType.MATHML,
        new Link("tan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LN(
        HtmlTagType.MATHML,
        new Link("ln", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTABLE(
        HtmlTagType.MATHML,
        new Link("mtable", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MERROR(
        HtmlTagType.MATHML,
        new Link("merror", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MACTION(
        HtmlTagType.MATHML,
        new Link("maction", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TRANSPOSE(
        HtmlTagType.MATHML,
        new Link("transpose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECEWICE(
        HtmlTagType.MATHML,
        new Link("piecewice", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTERVAL(
        HtmlTagType.MATHML,
        new Link("interval", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VECTORPRODUCT(
        HtmlTagType.MATHML,
        new Link("vectorproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPADDED(
        HtmlTagType.MATHML,
        new Link("mpadded", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOMENT(
        HtmlTagType.MATHML,
        new Link("moment", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DOMAINOFAPPLICATION(
        HtmlTagType.MATHML,
        new Link("domainofapplication", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGINARY(
        HtmlTagType.MATHML,
        new Link("imaginary", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NEQ(
        HtmlTagType.MATHML,
        new Link("neq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LT(
        HtmlTagType.MATHML,
        new Link("lt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MODE(
        HtmlTagType.MATHML,
        new Link("mode", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DETERMINANT(
        HtmlTagType.MATHML,
        new Link("determinant", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INVERSE(
        HtmlTagType.MATHML,
        new Link("inverse", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TIMES(
        HtmlTagType.MATHML,
        new Link("times", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
UNION(
        HtmlTagType.MATHML,
        new Link("union", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ENCODING(
        HtmlTagType.MATHML,
        new Link("encoding", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DOMAIN(
        HtmlTagType.MATHML,
        new Link("domain", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MI(
        HtmlTagType.MATHML,
        new Link("mi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MN(
        HtmlTagType.MATHML,
        new Link("mn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MIN(
        HtmlTagType.MATHML,
        new Link("min", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
UPLIMIT(
        HtmlTagType.MATHML,
        new Link("uplimit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MLABELEDTR(
        HtmlTagType.MATHML,
        new Link("mlabeledtr", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SETDIFF(
        HtmlTagType.MATHML,
        new Link("setdiff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MO(
        HtmlTagType.MATHML,
        new Link("mo", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MAX(
        HtmlTagType.MATHML,
        new Link("max", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LEQ(
        HtmlTagType.MATHML,
        new Link("leq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCTAN(
        HtmlTagType.MATHML,
        new Link("arctan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MS(
        HtmlTagType.MATHML,
        new Link("ms", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATRIX(
        HtmlTagType.MATHML,
        new Link("matrix", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TRUE(
        HtmlTagType.MATHML,
        new Link("true", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DIVIDE(
        HtmlTagType.MATHML,
        new Link("divide", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRODUCT(
        HtmlTagType.MATHML,
        new Link("product", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATH(
        HtmlTagType.MATHML,
        new Link("math", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSIN(
        HtmlTagType.MATHML,
        new Link("arcsin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DEGREE(
        HtmlTagType.MATHML,
        new Link("degree", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SIN(
        HtmlTagType.MATHML,
        new Link("sin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFENCED(
        HtmlTagType.MATHML,
        new Link("mfenced", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REM(
        HtmlTagType.MATHML,
        new Link("rem", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXISTS(
        HtmlTagType.MATHML,
        new Link("exists", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EMPTYSET(
        HtmlTagType.MATHML,
        new Link("emptyset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FACTORIAL(
        HtmlTagType.MATHML,
        new Link("factorial", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANNOTATION_XML(
        HtmlTagType.MATHML,
        new Link("annotation-xml", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGINARYI(
        HtmlTagType.MATHML,
        new Link("imaginaryi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EQ(
        HtmlTagType.MATHML,
        new Link("eq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SELECTOR(
        HtmlTagType.MATHML,
        new Link("selector", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
QUOTIENT(
        HtmlTagType.MATHML,
        new Link("quotient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSINH(
        HtmlTagType.MATHML,
        new Link("arcsinh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
APPLY(
        HtmlTagType.MATHML,
        new Link("apply", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTEGERS(
        HtmlTagType.MATHML,
        new Link("integers", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COTH(
        HtmlTagType.MATHML,
        new Link("coth", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGN(
        HtmlTagType.MATHML,
        new Link("malign", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COMPLEXES(
        HtmlTagType.MATHML,
        new Link("complexes", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOVER(
        HtmlTagType.MATHML,
        new Link("mover", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ROOT(
        HtmlTagType.MATHML,
        new Link("root", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTR(
        HtmlTagType.MATHML,
        new Link("mtr", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COS(
        HtmlTagType.MATHML,
        new Link("cos", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RELN(
        HtmlTagType.MATHML,
        new Link("reln", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COT(
        HtmlTagType.MATHML,
        new Link("cot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUB(
        HtmlTagType.MATHML,
        new Link("msub", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FUNCTION(
        HtmlTagType.MATHML,
        new Link("function", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TANH(
        HtmlTagType.MATHML,
        new Link("tanh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MUNDEROVER(
        HtmlTagType.MATHML,
        new Link("munderover", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LCM(
        HtmlTagType.MATHML,
        new Link("lcm", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSEC(
        HtmlTagType.MATHML,
        new Link("arcsec", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SDEV(
        HtmlTagType.MATHML,
        new Link("sdev", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POWER(
        HtmlTagType.MATHML,
        new Link("power", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTIN(
        HtmlTagType.MATHML,
        new Link("notin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NONE(
        HtmlTagType.MATHML,
        new Link("none", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CARD(
        HtmlTagType.MATHML,
        new Link("card", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOMENTABOUT(
        HtmlTagType.MATHML,
        new Link("momentabout", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EULERGAMMA(
        HtmlTagType.MATHML,
        new Link("eulergamma", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PARTIALDIFF(
        HtmlTagType.MATHML,
        new Link("partialdiff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GEQ(
        HtmlTagType.MATHML,
        new Link("geq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COSH(
        HtmlTagType.MATHML,
        new Link("cosh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MINUS(
        HtmlTagType.MATHML,
        new Link("minus", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSCH(
        HtmlTagType.MATHML,
        new Link("csch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNSCOPE(
        HtmlTagType.MATHML,
        new Link("malignscope", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CONDITION(
        HtmlTagType.MATHML,
        new Link("condition", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INT(
        HtmlTagType.MATHML,
        new Link("int", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MROW(
        HtmlTagType.MATHML,
        new Link("mrow", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SINH(
        HtmlTagType.MATHML,
        new Link("sinh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FORALL(
        HtmlTagType.MATHML,
        new Link("forall", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CONJUGATE(
        HtmlTagType.MATHML,
        new Link("conjugate", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCSC(
        HtmlTagType.MATHML,
        new Link("arccsc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MMULTISCRIPTS(
        HtmlTagType.MATHML,
        new Link("mmultiscripts", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FACTOROF(
        HtmlTagType.MATHML,
        new Link("factorof", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXPONENTIALE(
        HtmlTagType.MATHML,
        new Link("exponentiale", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
AND(
        HtmlTagType.MATHML,
        new Link("and", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTD(
        HtmlTagType.MATHML,
        new Link("mtd", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXP(
        HtmlTagType.MATHML,
        new Link("exp", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MGLYPH(
        HtmlTagType.MATHML,
        new Link("mglyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RATIONALS(
        HtmlTagType.MATHML,
        new Link("rationals", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CN(
        HtmlTagType.MATHML,
        new Link("cn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CEILING(
        HtmlTagType.MATHML,
        new Link("ceiling", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSYMBOL(
        HtmlTagType.MATHML,
        new Link("csymbol", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTERSECT(
        HtmlTagType.MATHML,
        new Link("intersect", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CI(
        HtmlTagType.MATHML,
        new Link("ci", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOGBASE(
        HtmlTagType.MATHML,
        new Link("logbase", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MUNDER(
        HtmlTagType.MATHML,
        new Link("munder", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPHANTOM(
        HtmlTagType.MATHML,
        new Link("mphantom", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSTYLE(
        HtmlTagType.MATHML,
        new Link("mstyle", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EQUIVALENT(
        HtmlTagType.MATHML,
        new Link("equivalent", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GRAD(
        HtmlTagType.MATHML,
        new Link("grad", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTPRSUBSET(
        HtmlTagType.MATHML,
        new Link("notprsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SET(
        HtmlTagType.MATHML,
        new Link("set", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMPLIES(
        HtmlTagType.MATHML,
        new Link("implies", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SUM(
        HtmlTagType.MATHML,
        new Link("sum", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSQRT(
        HtmlTagType.MATHML,
        new Link("msqrt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSECH(
        HtmlTagType.MATHML,
        new Link("arcsech", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INFINITY(
        HtmlTagType.MATHML,
        new Link("infinity", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SUBSET(
        HtmlTagType.MATHML,
        new Link("subset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SCALARPRODUCT(
        HtmlTagType.MATHML,
        new Link("scalarproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REAL(
        HtmlTagType.MATHML,
        new Link("real", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
APPROX(
        HtmlTagType.MATHML,
        new Link("approx", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IDENT(
        HtmlTagType.MATHML,
        new Link("ident", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VECTOR(
        HtmlTagType.MATHML,
        new Link("vector", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
BVAR(
        HtmlTagType.MATHML,
        new Link("bvar", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MEAN(
        HtmlTagType.MATHML,
        new Link("mean", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OUTERPRODUCT(
        HtmlTagType.MATHML,
        new Link("outerproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOTH(
        HtmlTagType.MATHML,
        new Link("arccoth", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
XOR(
        HtmlTagType.MATHML,
        new Link("xor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MENCLOSE(
        HtmlTagType.MATHML,
        new Link("menclose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VARIANCE(
        HtmlTagType.MATHML,
        new Link("variance", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LIST(
        HtmlTagType.MATHML,
        new Link("list", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNGROUP(
        HtmlTagType.MATHML,
        new Link("maligngroup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IN(
        HtmlTagType.MATHML,
        new Link("in", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSPACE(
        HtmlTagType.MATHML,
        new Link("mspace", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CARTESIANPRODUCT(
        HtmlTagType.MATHML,
        new Link("cartesianproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEC(
        HtmlTagType.MATHML,
        new Link("sec", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNMARK(
        HtmlTagType.MATHML,
        new Link("malignmark", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSC(
        HtmlTagType.MATHML,
        new Link("csc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEP(
        HtmlTagType.MATHML,
        new Link("sep", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUP(
        HtmlTagType.MATHML,
        new Link("msup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRIMES(
        HtmlTagType.MATHML,
        new Link("primes", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPRESCRIPTS(
        HtmlTagType.MATHML,
        new Link("mprescripts", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOSH(
        HtmlTagType.MATHML,
        new Link("arccosh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GCD(
        HtmlTagType.MATHML,
        new Link("gcd", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),

//SVG elements:
//-----------------------

STOP(
        HtmlTagType.SVG,
        new Link("stop", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DESC(
        HtmlTagType.SVG,
        new Link("desc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATE(
        HtmlTagType.SVG,
        new Link("animate", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE_SRC(
        HtmlTagType.SVG,
        new Link("font-face-src", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TBREAK(
        HtmlTagType.SVG,
        new Link("tbreak", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_SET(
        HtmlTagType.SVG,
        new Link("set", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
USE(
        HtmlTagType.SVG,
        new Link("use", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LINE(
        HtmlTagType.SVG,
        new Link("line", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SWITCH(
        HtmlTagType.SVG,
        new Link("switch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CIRCLE(
        HtmlTagType.SVG,
        new Link("circle", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DISCARD(
        HtmlTagType.SVG,
        new Link("discard", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POLYGON(
        HtmlTagType.SVG,
        new Link("polygon", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_TITLE(
        HtmlTagType.SVG,
        new Link("title", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATETRANSFORM(
        HtmlTagType.SVG,
        new Link("animateTransform", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_TEXTAREA(
        HtmlTagType.SVG,
        new Link("textArea", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POLYLINE(
        HtmlTagType.SVG,
        new Link("polyline", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MISSING_GLYPH(
        HtmlTagType.SVG,
        new Link("missing-glyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PATH(
        HtmlTagType.SVG,
        new Link("path", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LISTENER(
        HtmlTagType.SVG,
        new Link("listener", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_VIDEO(
        HtmlTagType.SVG,
        new Link("video", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
METADATA(
        HtmlTagType.SVG,
        new Link("metadata", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
G(
        HtmlTagType.SVG,
        new Link("g", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TEXT(
        HtmlTagType.SVG,
        new Link("text", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE_URI(
        HtmlTagType.SVG,
        new Link("font-face-uri", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DEFS(
        HtmlTagType.SVG,
        new Link("defs", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PREFETCH(
        HtmlTagType.SVG,
        new Link("prefetch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ELLIPSE(
        HtmlTagType.SVG,
        new Link("ellipse", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG(
        HtmlTagType.SVG,
        new Link("svg", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LINEARGRADIENT(
        HtmlTagType.SVG,
        new Link("linearGradient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_AUDIO(
        HtmlTagType.SVG,
        new Link("audio", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATION(
        HtmlTagType.SVG,
        new Link("animation", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FOREIGNOBJECT(
        HtmlTagType.SVG,
        new Link("foreignObject", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RADIALGRADIENT(
        HtmlTagType.SVG,
        new Link("radialGradient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT(
        HtmlTagType.SVG,
        new Link("font", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GLYPH(
        HtmlTagType.SVG,
        new Link("glyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_A(
        HtmlTagType.SVG,
        new Link("a", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_IMAGE(
        HtmlTagType.SVG,
        new Link("image", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_SCRIPT(
        HtmlTagType.SVG,
        new Link("script", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RECT(
        HtmlTagType.SVG,
        new Link("rect", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
HKERN(
        HtmlTagType.SVG,
        new Link("hkern", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPATH(
        HtmlTagType.SVG,
        new Link("mpath", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATEMOTION(
        HtmlTagType.SVG,
        new Link("animateMotion", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TSPAN(
        HtmlTagType.SVG,
        new Link("tspan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATECOLOR(
        HtmlTagType.SVG,
        new Link("animateColor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE(
        HtmlTagType.SVG,
        new Link("font-face", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SOLIDCOLOR(
        HtmlTagType.SVG,
        new Link("solidColor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
HANDLER(
        HtmlTagType.SVG,
        new Link("handler", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
);

    //<<<<<<<<<<<<<<<<<<<<<<<<
    //end of the generated section

    public static final String BASE_URL = "http://www.whatwg.org/specs/web-apps/current-work/multipage/";//NOI18N

    private Link name;
    
    private String description;

    private Collection<ContentType> categories;
    private Collection<FormAssociatedElementsCategory> formCategories; //for form elements only, if empty, its non-form element

    //the parents of the elements may be a content or a concrete element or combinations of those
    private Collection<ContentType> parents;
    private Collection<ElementDescriptor> parentElements;
    private String[] parentElementNames;

    //the children of the elements may be a content or a concrete element or combinations of those
    private Collection<ContentType> children;
    private Collection<ElementDescriptor> childrenElements;
    private String[] childrenElementNames;

    private Collection<Attribute> attributes;

    private Link domInterface;

    private HtmlTagType type;

    private ElementDescriptor() {
    }

    private ElementDescriptor(HtmlTagType type,
            Link name,
            String description,
            Collection<ContentType> categories,
            Collection<FormAssociatedElementsCategory> formCategories,
            Collection<ContentType> parents, 
            String[] parentElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<ContentType> children,
            String[] childrenElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<Attribute> attributes,
            Link domInterface) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.formCategories = formCategories;
        this.parentElementNames = parentElements;
        this.parents = parents;
        this.childrenElementNames = childrenElements;
        this.children = children;
        this.attributes = attributes;
        this.domInterface = domInterface;
    }

    public static ElementDescriptor forName(String name) {
        try {
            return valueOf(elementName2EnumName(name));
        } catch (IllegalArgumentException iae) {
            //no such enum member
            return null;
        }
    }

    public static ElementDescriptor forElementName(ElementName elementName) {
        return forName(elementName.name);
    }

    public Collection<Attribute> getAttributes() {
        Collection<Attribute> withGlobal = new LinkedList<Attribute>(attributes);
        withGlobal.addAll(Attribute.GLOBAL_ATTRIBUTES);
        withGlobal.addAll(Attribute.EVENT_ATTRIBUTES);
        return withGlobal;
    }

    public HtmlTagType getTagType() {
        return type;
    }

    public Collection<ContentType> getCategoryTypes() {
        return categories;
    }

    public Collection<ContentType> getChildrenTypes() {
        return children;
    }

    public synchronized Collection<ElementDescriptor> getChildrenElements() {
        //lazy init
        if(childrenElements == null) {
            childrenElements = new ArrayList<ElementDescriptor>();
            for(String elementName : childrenElementNames) {
                childrenElements.add(forName(elementName));
            }
        }
        return childrenElements;
    }

    public String getDescription() {
        return description;
    }

    public Link getDomInterface() {
        return domInterface;
    }

    public Collection<FormAssociatedElementsCategory> getFormCategories() {
        return formCategories;
    }

    public String getName() {
        return name.getName();
    }

    public URL getHelpUrl() {
        return name.getUrl(BASE_URL);
    }
    
    public String getHelpLink() {
        return name.getLink();
    }

    public synchronized Collection<ElementDescriptor> getParentElements() {
        //lazy init
        if(parentElements == null) {
            parentElements = new ArrayList<ElementDescriptor>();
            for(String elementName : parentElementNames) {
                parentElements.add(forName(elementName));
            }
        }
        return parentElements;
    }

    public Collection<ContentType> getParentTypes() {
        return parents;
    }

    public boolean hasOptionalOpenTag() {
        return ElementDescriptorRules.OPTIONAL_OPEN_TAGS.contains(this);
    }

    public boolean hasOptionalEndTag() {
        return ElementDescriptorRules.OPTIONAL_END_TAGS.contains(this) || isEmpty();
    }

    public boolean isEmpty() {
        return children.isEmpty() && getChildrenElements().isEmpty(); //empty content model
    }

    public static String elementName2EnumName(String elementName) {
        return elementName.replace('-', '_').toUpperCase();
    }

    public static synchronized Collection<String> getAttrNamesForElement(String elementName) {
        ElementDescriptor descriptor = ElementDescriptor.forName(elementName);
        if(descriptor == null) {
            return Collections.emptyList();
        }
        Collection<String> attrNames = new LinkedList<String>();
        for(Attribute a : descriptor.getAttributes()) {
            attrNames.add(a.getName());
        }

        return attrNames;
    }

}
