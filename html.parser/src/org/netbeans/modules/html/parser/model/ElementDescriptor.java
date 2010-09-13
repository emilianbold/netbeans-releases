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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import nu.validator.htmlparser.impl.ElementName;

/**
 *
 * @author marekfukala
 */
public enum ElementDescriptor {

    //section generated from the whatwg specification at http://www.whatwg.org/specs/web-apps/current-work
    //by the GenerateElementsIndex unit test
    //
    //>>>>>>>>>>>>>>>>>>>>>>>>

A(
        new Link("a", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-a-element"),
         "Hyperlink",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_MEDIA, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE),
        new Link("HTMLAnchorElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#htmlanchorelement")
),

ABBR(
        new Link("abbr", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-abbr-element"),
         "Abbreviation",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

ADDRESS(
        new Link("address", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-address-element"),
         "Contact information for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

AREA(
        new Link("area", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#the-area-element"),
         "Hyperlink or dead area on an image map",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_AREA_ALT, Attribute.ATTR_AREA_COORDS, Attribute.ATTR_AREA_SHAPE, Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_MEDIA, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE),
        new Link("HTMLAreaElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#htmlareaelement")
),

ARTICLE(
        new Link("article", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-article-element"),
         "Self-contained syndicatable or reusable composition",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

ASIDE(
        new Link("aside", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-aside-element"),
         "Sidebar for tangentially related content",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

AUDIO(
        new Link("audio", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#audio"),
         "Audio player",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"source"},
        EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_CONTROLS),
        new Link("HTMLAudioElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#htmlaudioelement")
),

B(
        new Link("b", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-b-element"),
         "Keywords",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

BASE(
        new Link("base", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-base-element"),
         "Base URL and default target browsing context for hyperlinks and forms",
        EnumSet.of(ContentType.METADATA),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_BASE_HREF, Attribute.ATTR_BASE_TARGET),
        new Link("HTMLBaseElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmlbaseelement")
),

BDO(
        new Link("bdo", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-bdo-element"),
         "Text directionality formatting",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

BLOCKQUOTE(
        new Link("blockquote", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-blockquote-element"),
         "A section quoted from another source",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_BLOCKQUOTE_CITE),
        new Link("HTMLQuoteElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlquoteelement")
),

BODY(
        new Link("body", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-body-element-0"),
         "Document body",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"html"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.HANDLER_WINDOW_ONAFTERPRINT, Attribute.HANDLER_WINDOW_ONBEFOREPRINT, Attribute.HANDLER_WINDOW_ONBEFOREUNLOAD, Attribute.HANDLER_WINDOW_ONBLUR, Attribute.HANDLER_WINDOW_ONERROR, Attribute.HANDLER_WINDOW_ONFOCUS, Attribute.HANDLER_WINDOW_ONHASHCHANGE, Attribute.HANDLER_WINDOW_ONLOAD, Attribute.HANDLER_WINDOW_ONMESSAGE, Attribute.HANDLER_WINDOW_ONOFFLINE, Attribute.HANDLER_WINDOW_ONONLINE, Attribute.HANDLER_WINDOW_ONPAGEHIDE, Attribute.HANDLER_WINDOW_ONPAGESHOW, Attribute.HANDLER_WINDOW_ONPOPSTATE, Attribute.HANDLER_WINDOW_ONREDO, Attribute.HANDLER_WINDOW_ONRESIZE, Attribute.HANDLER_WINDOW_ONSTORAGE, Attribute.HANDLER_WINDOW_ONUNDO, Attribute.HANDLER_WINDOW_ONUNLOAD),
        new Link("HTMLBodyElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlbodyelement")
),

BR(
        new Link("br", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-br-element"),
         "Line break, e.g. in poem or postal address",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLBRElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#htmlbrelement")
),

BUTTON(
        new Link("button", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-button-element"),
         "Button control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_FE_NAME, Attribute.ATTR_BUTTON_TYPE, Attribute.ATTR_BUTTON_VALUE),
        new Link("HTMLButtonElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmlbuttonelement")
),

CANVAS(
        new Link("canvas", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#the-canvas-element"),
         "Scriptable bitmap canvas",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_CANVAS_WIDTH, Attribute.ATTR_CANVAS_HEIGHT),
        new Link("HTMLCanvasElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#htmlcanvaselement")
),

CAPTION(
        new Link("caption", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-caption-element"),
         "Table caption",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableCaptionElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablecaptionelement")
),

CITE(
        new Link("cite", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-cite-element"),
         "Title of a work",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

CODE(
        new Link("code", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-code-element"),
         "Computer code",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

COL(
        new Link("col", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-col-element"),
         "Table column",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"colgroup"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_COL_SPAN),
        new Link("HTMLTableColElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablecolelement")
),

COLGROUP(
        new Link("colgroup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-colgroup-element"),
         "Group of columns in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"col"},
        EnumSet.of(Attribute.ATTR_COLGROUP_SPAN),
        new Link("HTMLTableColElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablecolelement")
),

COMMAND(
        new Link("command", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#the-command"),
         "Menu command",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_COMMAND_TYPE, Attribute.ATTR_COMMAND_LABEL, Attribute.ATTR_COMMAND_ICON, Attribute.ATTR_COMMAND_DISABLED, Attribute.ATTR_COMMAND_CHECKED, Attribute.ATTR_COMMAND_RADIOGROUP),
        new Link("HTMLCommandElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#htmlcommandelement")
),

DATALIST(
        new Link("datalist", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-datalist-element"),
         "Container for options for combo box control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{"option"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDataListElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmldatalistelement")
),

DD(
        new Link("dd", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-dd-element"),
         "Content for corresponding dt element(s)",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"dl"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

DEL(
        new Link("del", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#the-del-element"),
         "A removal from the document",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME),
        new Link("HTMLModElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#htmlmodelement")
),

DETAILS(
        new Link("details", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#the-details-element"),
         "Disclosure control for hiding details",
        EnumSet.of(ContentType.FLOW, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"summary"},
        EnumSet.of(Attribute.ATTR_DETAILS_OPEN),
        new Link("HTMLDetailsElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#htmldetailselement")
),

DFN(
        new Link("dfn", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-dfn-element"),
         "Defining instance",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

DIV(
        new Link("div", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-div-element"),
         "Generic flow container",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDivElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmldivelement")
),

DL(
        new Link("dl", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-dl-element"),
         "Association list consisting of zero or more name-value groups",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"dt", "dd"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLDListElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmldlistelement")
),

DT(
        new Link("dt", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-dt-element"),
         "Legend for corresponding dd element(s)",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"dl"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

EM(
        new Link("em", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-em-element"),
         "Stress emphasis",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

EMBED(
        new Link("embed", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#the-embed-element"),
         "Plugin",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_EMBED_SRC, Attribute.ATTR_EMBED_TYPE, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLEmbedElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#htmlembedelement")
),

FIELDSET(
        new Link("fieldset", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#the-fieldset-element"),
         "Group of form controls",
        EnumSet.of(ContentType.FLOW),
        EnumSet.of(FormAssociatedElementsCategory.LISTED),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"legend"},
        EnumSet.of(Attribute.ATTR_FIELDSET_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME),
        new Link("HTMLFieldSetElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#htmlfieldsetelement")
),

FIGCAPTION(
        new Link("figcaption", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-figcaption-element"),
         "Caption for figure",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"figure"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

FIGURE(
        new Link("figure", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-figure-element"),
         "Figure with optional caption",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"figcaption"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

FOOTER(
        new Link("footer", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-footer-element"),
         "Footer for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

FORM(
        new Link("form", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#the-form-element"),
         "User-submittable form",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FORM_ACCEPT_CHARSET, Attribute.ATTR_FS_ACTION, Attribute.ATTR_FORM_AUTOCOMPLETE, Attribute.ATTR_FS_ENCTYPE, Attribute.ATTR_FS_METHOD, Attribute.ATTR_FORM_NAME, Attribute.ATTR_FS_NOVALIDATE, Attribute.ATTR_FS_TARGET),
        new Link("HTMLFormElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#htmlformelement")
),

H1(
        new Link("h1", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

H2(
        new Link("h2", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

H3(
        new Link("h3", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

H4(
        new Link("h4", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

H5(
        new Link("h5", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

H6(
        new Link("h6", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
         "Section heading",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"hgroup"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadingElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#htmlheadingelement")
),

HEAD(
        new Link("head", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-head-element-0"),
         "Container for document metadata",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"html"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHeadElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmlheadelement")
),

HEADER(
        new Link("header", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-header-element"),
         "Introductory or navigational aids for a page or section",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

HGROUP(
        new Link("hgroup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-hgroup-element"),
         "heading group",
        EnumSet.of(ContentType.FLOW, ContentType.HEADING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"h1", "h2", "h3", "h4", "h5", "h6"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

HR(
        new Link("hr", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-hr-element"),
         "Thematic break",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLHRElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlhrelement")
),

HTML(
        new Link("html", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-html-element-0"),
         "Root element",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"head", "body"},
        EnumSet.of(Attribute.ATTR_HTML_MANIFEST),
        new Link("HTMLHtmlElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmlhtmlelement")
),

I(
        new Link("i", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-i-element"),
         "Alternate voice",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

IFRAME(
        new Link("iframe", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#the-iframe-element"),
         "Nested browsing context",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_IFRAME_SRC, Attribute.ATTR_IFRAME_SRCDOC, Attribute.ATTR_IFRAME_NAME, Attribute.ATTR_IFRAME_SANDBOX, Attribute.ATTR_IFRAME_SEAMLESS, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLIFrameElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#htmliframeelement")
),

IMG(
        new Link("img", "http://www.whatwg.org/specs/web-apps/current-work/multipage/embedded-content-1.html#the-img-element"),
         "Image",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_IMG_ALT, Attribute.ATTR_IMG_SRC, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_IMG_ISMAP, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLImageElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/embedded-content-1.html#htmlimageelement")
),

INPUT(
        new Link("input", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#the-input-element"),
         "Form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_INPUT_ACCEPT, Attribute.ATTR_INPUT_ALT, Attribute.ATTR_INPUT_AUTOCOMPLETE, Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_INPUT_CHECKED, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_DIM_HEIGHT, Attribute.ATTR_INPUT_LIST, Attribute.ATTR_INPUT_MAX, Attribute.ATTR_INPUT_MAXLENGTH, Attribute.ATTR_INPUT_MIN, Attribute.ATTR_INPUT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_INPUT_PATTERN, Attribute.ATTR_INPUT_PLACEHOLDER, Attribute.ATTR_INPUT_READONLY, Attribute.ATTR_INPUT_REQUIRED, Attribute.ATTR_INPUT_SIZE, Attribute.ATTR_INPUT_SRC, Attribute.ATTR_INPUT_STEP, Attribute.ATTR_INPUT_TYPE, Attribute.ATTR_INPUT_VALUE, Attribute.ATTR_DIM_WIDTH),
        new Link("HTMLInputElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#htmlinputelement")
),

INS(
        new Link("ins", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#the-ins-element"),
         "An addition to the document",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME),
        new Link("HTMLModElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#htmlmodelement")
),

KBD(
        new Link("kbd", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-kbd-element"),
         "User input",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

KEYGEN(
        new Link("keygen", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-keygen-element"),
         "Cryptographic key-pair generator form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_KEYGEN_CHALLENGE, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_KEYGEN_KEYTYPE, Attribute.ATTR_FE_NAME),
        new Link("HTMLKeygenElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmlkeygenelement")
),

LABEL(
        new Link("label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#the-label-element"),
         "Caption for a form control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FAE_FORM, Attribute.ATTR_LABEL_FOR),
        new Link("HTMLLabelElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#htmllabelelement")
),

LEGEND(
        new Link("legend", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#the-legend-element"),
         "Caption for fieldset",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"fieldset"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLLegendElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#htmllegendelement")
),

LI(
        new Link("li", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-li-element"),
         "List item",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ol", "ul", "menu"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_LI_VALUE),
        new Link("HTMLLIElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmllielement")
),

LINK(
        new Link("link", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-link-element"),
         "Link metadata",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_LINK_HREF, Attribute.ATTR_LINK_REL, Attribute.ATTR_LINK_MEDIA, Attribute.ATTR_LINK_HREFLANG, Attribute.ATTR_LINK_TYPE, Attribute.ATTR_LINK_SIZES),
        new Link("HTMLLinkElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmllinkelement")
),

MAP(
        new Link("map", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#the-map-element"),
         "Image map",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"area"},
        EnumSet.of(Attribute.ATTR_MAP_NAME),
        new Link("HTMLMapElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#htmlmapelement")
),

MARK(
        new Link("mark", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-mark-element"),
         "Highlight",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

MENU(
        new Link("menu", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#menus"),
         "Menu of commands",
        EnumSet.of(ContentType.FLOW, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{"li"},
        EnumSet.of(Attribute.ATTR_MENU_TYPE, Attribute.ATTR_MENU_LABEL),
        new Link("HTMLMenuElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#htmlmenuelement")
),

META(
        new Link("meta", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#meta"),
         "Text metadata",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_META_NAME, Attribute.ATTR_META_HTTP_EQUIV, Attribute.ATTR_META_CONTENT, Attribute.ATTR_META_CHARSET),
        new Link("HTMLMetaElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmlmetaelement")
),

METER(
        new Link("meter", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-meter-element"),
         "Gauge",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LABELABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_METER_VALUE, Attribute.ATTR_METER_MIN, Attribute.ATTR_METER_MAX, Attribute.ATTR_METER_LOW, Attribute.ATTR_METER_HIGH, Attribute.ATTR_METER_OPTIMUM, Attribute.ATTR_FAE_FORM),
        new Link("HTMLMeterElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmlmeterelement")
),

NAV(
        new Link("nav", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-nav-element"),
         "Section with navigational links",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

NOSCRIPT(
        new Link("noscript", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#the-noscript-element"),
         "Fallback content for script",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

OBJECT(
        new Link("object", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#the-object-element"),
         "Image, nested browsing context, or plugin",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.SUBMITTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"param"},
        EnumSet.of(Attribute.ATTR_OBJECT_DATA, Attribute.ATTR_OBJECT_TYPE, Attribute.ATTR_OBJECT_NAME, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_FAE_FORM, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLObjectElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#htmlobjectelement")
),

OL(
        new Link("ol", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-ol-element"),
         "Ordered list",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"li"},
        EnumSet.of(Attribute.ATTR_OL_REVERSED, Attribute.ATTR_OL_START),
        new Link("HTMLOListElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlolistelement")
),

OPTGROUP(
        new Link("optgroup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-optgroup-element"),
         "Group of options in a list box",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"select"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"option"},
        EnumSet.of(Attribute.ATTR_OPTGROUP_DISABLED, Attribute.ATTR_OPTGROUP_LABEL),
        new Link("HTMLOptGroupElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmloptgroupelement")
),

OPTION(
        new Link("option", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-option-element"),
         "Option in a list box or combo box control",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"select", "datalist", "optgroup"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_OPTION_DISABLED, Attribute.ATTR_OPTION_LABEL, Attribute.ATTR_OPTION_SELECTED, Attribute.ATTR_OPTION_VALUE),
        new Link("HTMLOptionElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmloptionelement")
),

OUTPUT(
        new Link("output", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-output-element"),
         "Calculated output value",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_OUTPUT_FOR, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME),
        new Link("HTMLOutputElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmloutputelement")
),

P(
        new Link("p", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-p-element"),
         "Paragraph",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLParagraphElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlparagraphelement")
),

PARAM(
        new Link("param", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#the-param-element"),
         "Parameter for object",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"object"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_PARAM_NAME, Attribute.ATTR_PARAM_VALUE),
        new Link("HTMLParamElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#htmlparamelement")
),

PRE(
        new Link("pre", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-pre-element"),
         "Block of preformatted text",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLPreElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlpreelement")
),

PROGRESS(
        new Link("progress", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-progress-element"),
         "Progress bar",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.of(FormAssociatedElementsCategory.LABELABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_PROGRESS_VALUE, Attribute.ATTR_PROGRESS_MAX, Attribute.ATTR_FAE_FORM),
        new Link("HTMLProgressElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmlprogresselement")
),

Q(
        new Link("q", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-q-element"),
         "Quotation",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_Q_CITE),
        new Link("HTMLQuoteElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlquoteelement")
),

RP(
        new Link("rp", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-rp-element"),
         "Parenthesis for ruby annotation text",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ruby"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

RT(
        new Link("rt", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-rt-element"),
         "Ruby annotation text",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"ruby"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

RUBY(
        new Link("ruby", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-ruby-element"),
         "Ruby annotation(s)",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{"rt", "rp"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SAMP(
        new Link("samp", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-samp-element"),
         "Computer output",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SCRIPT(
        new Link("script", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#script"),
         "Embedded script",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_SCRIPT_SRC, Attribute.ATTR_SCRIPT_ASYNC, Attribute.ATTR_SCRIPT_DEFER, Attribute.ATTR_SCRIPT_TYPE, Attribute.ATTR_SCRIPT_CHARSET),
        new Link("HTMLScriptElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#htmlscriptelement")
),

SECTION(
        new Link("section", "http://www.whatwg.org/specs/web-apps/current-work/multipage/sections.html#the-section-element"),
         "Generic document or application section",
        EnumSet.of(ContentType.FLOW, ContentType.SECTIONING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SELECT(
        new Link("select", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-select-element"),
         "List box control",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"option", "optgroup"},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_SELECT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_SELECT_REQUIRED, Attribute.ATTR_SELECT_SIZE),
        new Link("HTMLSelectElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmlselectelement")
),

SMALL(
        new Link("small", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-small-element"),
         "Side comment",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SOURCE(
        new Link("source", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#the-source-element"),
         "Media source for video or audio",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"video", "audio"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_SOURCE_SRC, Attribute.ATTR_SOURCE_TYPE, Attribute.ATTR_SOURCE_MEDIA),
        new Link("HTMLSourceElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#htmlsourceelement")
),

SPAN(
        new Link("span", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-span-element"),
         "Generic phrasing container",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLSpanElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#htmlspanelement")
),

STRONG(
        new Link("strong", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-strong-element"),
         "Importance",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

STYLE(
        new Link("style", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-style-element"),
         "Embedded styling information",
        EnumSet.of(ContentType.METADATA, ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{"head", "noscript"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_STYLE_MEDIA, Attribute.ATTR_STYLE_TYPE, Attribute.ATTR_STYLE_SCOPED),
        new Link("HTMLStyleElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmlstyleelement")
),

SUB(
        new Link("sub", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-sub-and-sup-elements"),
         "Subscript",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SUMMARY(
        new Link("summary", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#the-summary-element"),
         "Caption for details",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"details"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

SUP(
        new Link("sup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-sub-and-sup-elements"),
         "Superscript",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

TABLE(
        new Link("table", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-table-element"),
         "Table",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"caption", "colgroup", "thead", "tbody", "tfoot", "tr"},
        EnumSet.of(Attribute.ATTR_TABLE_SUMMARY),
        new Link("HTMLTableElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltableelement")
),

TBODY(
        new Link("tbody", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-tbody-element"),
         "Group of rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablesectionelement")
),

TD(
        new Link("td", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-td-element"),
         "Table cell",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS),
        new Link("HTMLTableDataCellElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltabledatacellelement")
),

TEXTAREA(
        new Link("textarea", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#the-textarea-element"),
         "Multiline text field",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE),
        EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_TEXTAREA_COLS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_TEXTAREA_MAXLENGTH, Attribute.ATTR_FE_NAME, Attribute.ATTR_TEXTAREA_PLACEHOLDER, Attribute.ATTR_TEXTAREA_READONLY, Attribute.ATTR_TEXTAREA_REQUIRED, Attribute.ATTR_TEXTAREA_ROWS, Attribute.ATTR_TEXTAREA_WRAP),
        new Link("HTMLTextAreaElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#htmltextareaelement")
),

TFOOT(
        new Link("tfoot", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-tfoot-element"),
         "Group of footer rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablesectionelement")
),

TH(
        new Link("th", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-th-element"),
         "Table header cell",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS, Attribute.ATTR_TH_SCOPE),
        new Link("HTMLTableHeaderCellElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltableheadercellelement")
),

THEAD(
        new Link("thead", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-thead-element"),
         "Group of heading rows in a table",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"tr"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableSectionElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablesectionelement")
),

TIME(
        new Link("time", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-time-element"),
         "Date and/or time",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TIME_DATETIME, Attribute.ATTR_TIME_PUBDATE),
        new Link("HTMLTimeElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#htmltimeelement")
),

TITLE(
        new Link("title", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#the-title-element-0"),
         "Document title",
        EnumSet.of(ContentType.METADATA),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"head"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTitleElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#htmltitleelement")
),

TR(
        new Link("tr", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#the-tr-element"),
         "Table row",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"table", "thead", "tbody", "tfoot"},
        EnumSet.noneOf(ContentType.class),
        new String[]{"th", "td"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLTableRowElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#htmltablerowelement")
),

TRACK(
        new Link("track", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#the-track-element"),
         "Timed track",
        EnumSet.noneOf(ContentType.class),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{"audio", "video"},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.of(Attribute.ATTR_TRACK_KIND, Attribute.ATTR_TRACK_LABEL, Attribute.ATTR_TRACK_SRC, Attribute.ATTR_TRACK_SRCLANG),
        new Link("HTMLTrackElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#htmltrackelement")
),

UL(
        new Link("ul", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#the-ul-element"),
         "List",
        EnumSet.of(ContentType.FLOW),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.FLOW),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"li"},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLUListElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#htmlulistelement")
),

VAR(
        new Link("var", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-var-element"),
         "Variable",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
),

VIDEO(
        new Link("video", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#video"),
         "Video player",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{"source"},
        EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_VIDEO_POSTER, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_CONTROLS, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT),
        new Link("HTMLVideoElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#htmlvideoelement")
),

WBR(
        new Link("wbr", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#the-wbr-element"),
         "Line breaking opportunity",
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.of(ContentType.PHRASING),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        new Link("HTMLElement", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#htmlelement")
);






    //<<<<<<<<<<<<<<<<<<<<<<<<
    //end of the generated section

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

    private ElementDescriptor() {
    }

    private ElementDescriptor(Link name,
            String description,
            Collection<ContentType> categories,
            Collection<FormAssociatedElementsCategory> formCategories,
            Collection<ContentType> parents, 
            String[] parentElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<ContentType> children,
            String[] childrenElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<Attribute> attributes,
            Link domInterface) {
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
            return valueOf(name.toUpperCase());
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

    public Link getName() {
        return name;
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
        return ElementDescriptorRules.OPTIONAL_END_TAGS.contains(this);
    }

    public boolean isEmpty() {
        return children.isEmpty(); //empty content model
    }
    
}
