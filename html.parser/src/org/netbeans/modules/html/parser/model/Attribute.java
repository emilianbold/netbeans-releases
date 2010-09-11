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
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author marekfukala
 */
public enum Attribute {

    //GENERATED SECTION>>>
    //global attributes
    ACCESSKEY(new Link("accesskey", "http://www.whatwg.org/specs/web-apps/current-work/multipage/editing.html#the-accesskey-attribute")),
    CLASS(new Link("class", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#classes")),
    CONTENTEDITABLE(new Link("contenteditable", "http://www.whatwg.org/specs/web-apps/current-work/multipage/editing.html#attr-contenteditable")),
    CONTEXTMENU(new Link("contextmenu", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-contextmenu")),
    DIR(new Link("dir", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#the-dir-attribute")),
    DRAGGABLE(new Link("draggable", "http://www.whatwg.org/specs/web-apps/current-work/multipage/dnd.html#the-draggable-attribute")),
    HIDDEN(new Link("hidden", "http://www.whatwg.org/specs/web-apps/current-work/multipage/editing.html#the-hidden-attribute")),
    ID(new Link("id", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#the-id-attribute")),
    ITEMID(new Link("itemid", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-itemid")),
    ITEMPROP(new Link("itemprop", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#names:-the-itemprop-attribute")),
    ITEMREF(new Link("itemref", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-itemref")),
    ITEMSCOPE(new Link("itemscope", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-itemscope")),
    ITEMTYPE(new Link("itemtype", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-itemtype")),
    LANG(new Link("lang", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#attr-lang")),
    SPELLCHECK(new Link("spellcheck", "http://www.whatwg.org/specs/web-apps/current-work/multipage/editing.html#attr-spellcheck")),
    STYLE(new Link("style", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#the-style-attribute")),
    TABINDEX(new Link("tabindex", "http://www.whatwg.org/specs/web-apps/current-work/multipage/editing.html#attr-tabindex")),
    TITLE(new Link("title", "http://www.whatwg.org/specs/web-apps/current-work/multipage/elements.html#the-title-attribute")),
    //event attributes
    ONABORT(new Link("onabort", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onabort")),
    ONBLUR(new Link("onblur", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onblur")),
    ONCANPLAY(new Link("oncanplay", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-oncanplay")),
    ONCANPLAYTHROUGH(new Link("oncanplaythrough", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-oncanplaythrough")),
    ONCHANGE(new Link("onchange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onchange")),
    ONCLICK(new Link("onclick", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onclick")),
    ONCONTEXTMENU(new Link("oncontextmenu", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-oncontextmenu")),
    ONDBLCLICK(new Link("ondblclick", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondblclick")),
    ONDRAG(new Link("ondrag", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondrag")),
    ONDRAGEND(new Link("ondragend", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondragend")),
    ONDRAGENTER(new Link("ondragenter", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondragenter")),
    ONDRAGLEAVE(new Link("ondragleave", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondragleave")),
    ONDRAGOVER(new Link("ondragover", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondragover")),
    ONDRAGSTART(new Link("ondragstart", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondragstart")),
    ONDROP(new Link("ondrop", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondrop")),
    ONDURATIONCHANGE(new Link("ondurationchange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ondurationchange")),
    ONEMPTIED(new Link("onemptied", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onemptied")),
    ONENDED(new Link("onended", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onended")),
    ONERROR(new Link("onerror", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onerror")),
    ONFOCUS(new Link("onfocus", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onfocus")),
    ONFORMCHANGE(new Link("onformchange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onformchange")),
    ONFORMINPUT(new Link("onforminput", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onforminput")),
    ONINPUT(new Link("oninput", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-oninput")),
    ONINVALID(new Link("oninvalid", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-oninvalid")),
    ONKEYDOWN(new Link("onkeydown", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onkeydown")),
    ONKEYPRESS(new Link("onkeypress", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onkeypress")),
    ONKEYUP(new Link("onkeyup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onkeyup")),
    ONLOAD(new Link("onload", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onload")),
    ONLOADEDDATA(new Link("onloadeddata", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onloadeddata")),
    ONLOADEDMETADATA(new Link("onloadedmetadata", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onloadedmetadata")),
    ONLOADSTART(new Link("onloadstart", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onloadstart")),
    ONMOUSEDOWN(new Link("onmousedown", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmousedown")),
    ONMOUSEMOVE(new Link("onmousemove", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmousemove")),
    ONMOUSEOUT(new Link("onmouseout", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmouseout")),
    ONMOUSEOVER(new Link("onmouseover", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmouseover")),
    ONMOUSEUP(new Link("onmouseup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmouseup")),
    ONMOUSEWHEEL(new Link("onmousewheel", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onmousewheel")),
    ONPAUSE(new Link("onpause", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onpause")),
    ONPLAY(new Link("onplay", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onplay")),
    ONPLAYING(new Link("onplaying", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onplaying")),
    ONPROGRESS(new Link("onprogress", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onprogress")),
    ONRATECHANGE(new Link("onratechange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onratechange")),
    ONREADYSTATECHANGE(new Link("onreadystatechange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onreadystatechange")),
    ONSCROLL(new Link("onscroll", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onscroll")),
    ONSEEKED(new Link("onseeked", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onseeked")),
    ONSEEKING(new Link("onseeking", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onseeking")),
    ONSELECT(new Link("onselect", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onselect")),
    ONSHOW(new Link("onshow", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onshow")),
    ONSTALLED(new Link("onstalled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onstalled")),
    ONSUBMIT(new Link("onsubmit", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onsubmit")),
    ONSUSPEND(new Link("onsuspend", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onsuspend")),
    ONTIMEUPDATE(new Link("ontimeupdate", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-ontimeupdate")),
    ONVOLUMECHANGE(new Link("onvolumechange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onvolumechange")),
    ONWAITING(new Link("onwaiting", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-onwaiting")),

    //properietary attributes
    ATTR_TIME_PUBDATE(new Link("attr-time-pubdate", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#attr-time-pubdate")),
    ATTR_PROGRESS_VALUE(new Link("attr-progress-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-progress-value")),
    ATTR_FS_FORMMETHOD(new Link("attr-fs-formmethod", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-formmethod")),
    ATTR_TEXTAREA_MAXLENGTH(new Link("attr-textarea-maxlength", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-maxlength")),
    ATTR_FE_NAME(new Link("attr-fe-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fe-name")),
    ATTR_TDTH_HEADERS(new Link("attr-tdth-headers", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-tdth-headers")),
    ATTR_BUTTON_TYPE(new Link("attr-button-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-button-type")),
    ATTR_METER_VALUE(new Link("attr-meter-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-value")),
    ATTR_INPUT_REQUIRED(new Link("attr-input-required", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-required")),
    ATTR_COLGROUP_SPAN(new Link("attr-colgroup-span", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-colgroup-span")),
    ATTR_INPUT_CHECKED(new Link("attr-input-checked", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#attr-input-checked")),
    ATTR_COMMAND_ICON(new Link("attr-command-icon", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-icon")),
    HANDLER_WINDOW_ONREDO(new Link("handler-window-onredo", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onredo")),
    ATTR_FE_DISABLED(new Link("attr-fe-disabled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fe-disabled")),
    HANDLER_WINDOW_ONPAGESHOW(new Link("handler-window-onpageshow", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onpageshow")),
    ATTR_INPUT_PATTERN(new Link("attr-input-pattern", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-pattern")),
    ATTR_VIDEO_POSTER(new Link("attr-video-poster", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-video-poster")),
    ATTR_OBJECT_TYPE(new Link("attr-object-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-object-type")),
    ATTR_TRACK_KIND(new Link("attr-track-kind", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-track-kind")),
    ATTR_AREA_SHAPE(new Link("attr-area-shape", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-area-shape")),
    ATTR_FS_ACTION(new Link("attr-fs-action", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-action")),
    ATTR_SCRIPT_DEFER(new Link("attr-script-defer", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#attr-script-defer")),
    ATTR_KEYGEN_KEYTYPE(new Link("attr-keygen-keytype", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-keygen-keytype")),
    ATTR_HYPERLINK_USEMAP(new Link("attr-hyperlink-usemap", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-hyperlink-usemap")),
    ATTR_TEXTAREA_PLACEHOLDER(new Link("attr-textarea-placeholder", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-placeholder")),
    HANDLER_WINDOW_ONOFFLINE(new Link("handler-window-onoffline", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onoffline")),
    ATTR_EMBED_TYPE(new Link("attr-embed-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-embed-type")),
    ATTR_FS_TARGET(new Link("attr-fs-target", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-target")),
    ATTR_Q_CITE(new Link("attr-q-cite", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#attr-q-cite")),
    ATTR_MEDIA_CONTROLS(new Link("attr-media-controls", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-media-controls")),
    ATTR_IFRAME_NAME(new Link("attr-iframe-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-iframe-name")),
    ATTR_SCRIPT_TYPE(new Link("attr-script-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#attr-script-type")),
    ATTR_HYPERLINK_PING(new Link("attr-hyperlink-ping", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#ping")),
    ATTR_META_HTTP_EQUIV(new Link("attr-meta-http-equiv", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-meta-http-equiv")),
    ATTR_AREA_ALT(new Link("attr-area-alt", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-area-alt")),
    HANDLER_WINDOW_ONERROR(new Link("handler-window-onerror", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onerror")),
    ATTR_METER_OPTIMUM(new Link("attr-meter-optimum", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-optimum")),
    HANDLER_WINDOW_ONMESSAGE(new Link("handler-window-onmessage", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onmessage")),
    ATTR_IFRAME_SRCDOC(new Link("attr-iframe-srcdoc", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-iframe-srcdoc")),
    ATTR_IMG_ISMAP(new Link("attr-img-ismap", "http://www.whatwg.org/specs/web-apps/current-work/multipage/embedded-content-1.html#attr-img-ismap")),
    ATTR_COMMAND_TYPE(new Link("attr-command-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-type")),
    ATTR_CANVAS_WIDTH(new Link("attr-canvas-width", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#attr-canvas-width")),
    ATTR_COMMAND_CHECKED(new Link("attr-command-checked", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-checked")),
    ATTR_OPTION_SELECTED(new Link("attr-option-selected", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-option-selected")),
    ATTR_META_CONTENT(new Link("attr-meta-content", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-meta-content")),
    ATTR_COMMAND_RADIOGROUP(new Link("attr-command-radiogroup", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-radiogroup")),
    HANDLER_WINDOW_ONBLUR(new Link("handler-window-onblur", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onblur")),
    HANDLER_WINDOW_ONPOPSTATE(new Link("handler-window-onpopstate", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onpopstate")),
    ATTR_MEDIA_PRELOAD(new Link("attr-media-preload", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-media-preload")),
    ATTR_LINK_TYPE(new Link("attr-link-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-link-type")),
    HANDLER_WINDOW_ONUNLOAD(new Link("handler-window-onunload", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onunload")),
    ATTR_MOD_CITE(new Link("attr-mod-cite", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#attr-mod-cite")),
    ATTR_INPUT_MIN(new Link("attr-input-min", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-min")),
    ATTR_TEXTAREA_READONLY(new Link("attr-textarea-readonly", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-readonly")),
    ATTR_OL_REVERSED(new Link("attr-ol-reversed", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#attr-ol-reversed")),
    ATTR_MENU_TYPE(new Link("attr-menu-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-menu-type")),
    ATTR_OPTGROUP_DISABLED(new Link("attr-optgroup-disabled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-optgroup-disabled")),
    ATTR_INPUT_PLACEHOLDER(new Link("attr-input-placeholder", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-placeholder")),
    ATTR_INPUT_SIZE(new Link("attr-input-size", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-size")),
    ATTR_HYPERLINK_HREF(new Link("attr-hyperlink-href", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-href")),
    ATTR_BLOCKQUOTE_CITE(new Link("attr-blockquote-cite", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#attr-blockquote-cite")),
    ATTR_LINK_HREF(new Link("attr-link-href", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-link-href")),
    ATTR_COMMAND_LABEL(new Link("attr-command-label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-label")),
    ATTR_SOURCE_SRC(new Link("attr-source-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-source-src")),
    ATTR_MAP_NAME(new Link("attr-map-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-map-name")),
    ATTR_INPUT_MAXLENGTH(new Link("attr-input-maxlength", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-maxlength")),
    ATTR_FS_ENCTYPE(new Link("attr-fs-enctype", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-enctype")),
    ATTR_INPUT_ACCEPT(new Link("attr-input-accept", "http://www.whatwg.org/specs/web-apps/current-work/multipage/number-state.html#attr-input-accept")),
    ATTR_BASE_HREF(new Link("attr-base-href", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-base-href")),
    ATTR_BASE_TARGET(new Link("attr-base-target", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-base-target")),
    ATTR_SELECT_SIZE(new Link("attr-select-size", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-select-size")),
    ATTR_SCRIPT_ASYNC(new Link("attr-script-async", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#attr-script-async")),
    ATTR_LINK_SIZES(new Link("attr-link-sizes", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-link-sizes")),
    ATTR_TEXTAREA_WRAP(new Link("attr-textarea-wrap", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-wrap")),
    ATTR_METER_MAX(new Link("attr-meter-max", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-max")),
    ATTR_INPUT_TYPE(new Link("attr-input-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#attr-input-type")),
    ATTR_INPUT_MAX(new Link("attr-input-max", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-max")),
    ATTR_IMG_SRC(new Link("attr-img-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/embedded-content-1.html#attr-img-src")),
    ATTR_HYPERLINK_TARGET(new Link("attr-hyperlink-target", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-target")),
    ATTR_LABEL_FOR(new Link("attr-label-for", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#attr-label-for")),
    ATTR_TEXTAREA_REQUIRED(new Link("attr-textarea-required", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-required")),
    ATTR_OBJECT_DATA(new Link("attr-object-data", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-object-data")),
    ATTR_FS_FORMTARGET(new Link("attr-fs-formtarget", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-formtarget")),
    ATTR_OUTPUT_FOR(new Link("attr-output-for", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-output-for")),
    ATTR_OPTION_VALUE(new Link("attr-option-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-option-value")),
    ATTR_OPTGROUP_LABEL(new Link("attr-optgroup-label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-optgroup-label")),
    ATTR_META_CHARSET(new Link("attr-meta-charset", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-meta-charset")),
    ATTR_LINK_HREFLANG(new Link("attr-link-hreflang", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-link-hreflang")),
    ATTR_HYPERLINK_MEDIA(new Link("attr-hyperlink-media", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-media")),
    ATTR_TIME_DATETIME(new Link("attr-time-datetime", "http://www.whatwg.org/specs/web-apps/current-work/multipage/text-level-semantics.html#attr-time-datetime")),
    ATTR_TEXTAREA_ROWS(new Link("attr-textarea-rows", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-rows")),
    ATTR_METER_MIN(new Link("attr-meter-min", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-min")),
    ATTR_LI_VALUE(new Link("attr-li-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#attr-li-value")),
    ATTR_HTML_MANIFEST(new Link("attr-html-manifest", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-html-manifest")),
    ATTR_KEYGEN_CHALLENGE(new Link("attr-keygen-challenge", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-keygen-challenge")),
    HANDLER_WINDOW_ONLOAD(new Link("handler-window-onload", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onload")),
    ATTR_FORM_ACCEPT_CHARSET(new Link("attr-form-accept-charset", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#attr-form-accept-charset")),
    ATTR_OBJECT_NAME(new Link("attr-object-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-object-name")),
    HANDLER_WINDOW_ONAFTERPRINT(new Link("handler-window-onafterprint", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onafterprint")),
    ATTR_TH_SCOPE(new Link("attr-th-scope", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-th-scope")),
    ATTR_SCRIPT_CHARSET(new Link("attr-script-charset", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#attr-script-charset")),
    ATTR_FS_FORMACTION(new Link("attr-fs-formaction", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-formaction")),
    ATTR_TEXTAREA_COLS(new Link("attr-textarea-cols", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-textarea-cols")),
    ATTR_PARAM_VALUE(new Link("attr-param-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-param-value")),
    ATTR_AREA_COORDS(new Link("attr-area-coords", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-area-coords")),
    ATTR_INPUT_LIST(new Link("attr-input-list", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-list")),
    ATTR_IMG_ALT(new Link("attr-img-alt", "http://www.whatwg.org/specs/web-apps/current-work/multipage/embedded-content-1.html#attr-img-alt")),
    ATTR_MEDIA_AUTOPLAY(new Link("attr-media-autoplay", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-media-autoplay")),
    ATTR_SOURCE_MEDIA(new Link("attr-source-media", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-source-media")),
    ATTR_LINK_MEDIA(new Link("attr-link-media", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-link-media")),
    ATTR_FAE_FORM(new Link("attr-fae-form", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fae-form")),
    ATTR_STYLE_SCOPED(new Link("attr-style-scoped", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-style-scoped")),
    ATTR_TDTH_ROWSPAN(new Link("attr-tdth-rowspan", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-tdth-rowspan")),
    ATTR_INPUT_SRC(new Link("attr-input-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/number-state.html#attr-input-src")),
    ATTR_FIELDSET_DISABLED(new Link("attr-fieldset-disabled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#attr-fieldset-disabled")),
    HANDLER_WINDOW_ONHASHCHANGE(new Link("handler-window-onhashchange", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onhashchange")),
    HANDLER_WINDOW_ONBEFOREPRINT(new Link("handler-window-onbeforeprint", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onbeforeprint")),
    HANDLER_WINDOW_ONFOCUS(new Link("handler-window-onfocus", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onfocus")),
    ATTR_COMMAND_DISABLED(new Link("attr-command-disabled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-command-disabled")),
    HANDLER_WINDOW_ONRESIZE(new Link("handler-window-onresize", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onresize")),
    ATTR_TRACK_LABEL(new Link("attr-track-label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-track-label")),
    ATTR_METER_HIGH(new Link("attr-meter-high", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-high")),
    ATTR_SCRIPT_SRC(new Link("attr-script-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/scripting-1.html#attr-script-src")),
    ATTR_LINK_REL(new Link("attr-link-rel", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-link-rel")),
    ATTR_MEDIA_SRC(new Link("attr-media-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-media-src")),
    ATTR_METER_LOW(new Link("attr-meter-low", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-meter-low")),
    HANDLER_WINDOW_ONUNDO(new Link("handler-window-onundo", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onundo")),
    ATTR_DIM_WIDTH(new Link("attr-dim-width", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-dim-width")),
    ATTR_INPUT_MULTIPLE(new Link("attr-input-multiple", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-multiple")),
    ATTR_STYLE_TYPE(new Link("attr-style-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-style-type")),
    ATTR_IFRAME_SEAMLESS(new Link("attr-iframe-seamless", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-iframe-seamless")),
    ATTR_INPUT_ALT(new Link("attr-input-alt", "http://www.whatwg.org/specs/web-apps/current-work/multipage/number-state.html#attr-input-alt")),
    ATTR_FS_METHOD(new Link("attr-fs-method", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-method")),
    ATTR_PROGRESS_MAX(new Link("attr-progress-max", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-progress-max")),
    ATTR_OPTION_DISABLED(new Link("attr-option-disabled", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-option-disabled")),
    ATTR_INPUT_VALUE(new Link("attr-input-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#attr-input-value")),
    ATTR_PARAM_NAME(new Link("attr-param-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-param-name")),
    ATTR_META_NAME(new Link("attr-meta-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-meta-name")),
    ATTR_MOD_DATETIME(new Link("attr-mod-datetime", "http://www.whatwg.org/specs/web-apps/current-work/multipage/edits.html#attr-mod-datetime")),
    ATTR_TRACK_SRCLANG(new Link("attr-track-srclang", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-track-srclang")),
    ATTR_STYLE_MEDIA(new Link("attr-style-media", "http://www.whatwg.org/specs/web-apps/current-work/multipage/semantics.html#attr-style-media")),
    ATTR_SELECT_REQUIRED(new Link("attr-select-required", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-select-required")),
    ATTR_INPUT_AUTOCOMPLETE(new Link("attr-input-autocomplete", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-autocomplete")),
    ATTR_IFRAME_SRC(new Link("attr-iframe-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-iframe-src")),
    ATTR_INPUT_READONLY(new Link("attr-input-readonly", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-readonly")),
    ATTR_HYPERLINK_HREFLANG(new Link("attr-hyperlink-hreflang", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-hreflang")),
    ATTR_FS_FORMNOVALIDATE(new Link("attr-fs-formnovalidate", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-formnovalidate")),
    ATTR_INPUT_STEP(new Link("attr-input-step", "http://www.whatwg.org/specs/web-apps/current-work/multipage/common-input-element-attributes.html#attr-input-step")),
    ATTR_CANVAS_HEIGHT(new Link("attr-canvas-height", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#attr-canvas-height")),
    ATTR_MEDIA_LOOP(new Link("attr-media-loop", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-media-loop")),
    HANDLER_WINDOW_ONSTORAGE(new Link("handler-window-onstorage", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onstorage")),
    HANDLER_WINDOW_ONPAGEHIDE(new Link("handler-window-onpagehide", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onpagehide")),
    ATTR_HYPERLINK_TYPE(new Link("attr-hyperlink-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-type")),
    ATTR_DIM_HEIGHT(new Link("attr-dim-height", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-map-element.html#attr-dim-height")),
    ATTR_FORM_NAME(new Link("attr-form-name", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#attr-form-name")),
    ATTR_TABLE_SUMMARY(new Link("attr-table-summary", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-table-summary")),
    ATTR_OL_START(new Link("attr-ol-start", "http://www.whatwg.org/specs/web-apps/current-work/multipage/grouping-content.html#attr-ol-start")),
    ATTR_SOURCE_TYPE(new Link("attr-source-type", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-source-type")),
    ATTR_COL_SPAN(new Link("attr-col-span", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-col-span")),
    ATTR_HYPERLINK_REL(new Link("attr-hyperlink-rel", "http://www.whatwg.org/specs/web-apps/current-work/multipage/links.html#attr-hyperlink-rel")),
    ATTR_EMBED_SRC(new Link("attr-embed-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-embed-src")),
    ATTR_TDTH_COLSPAN(new Link("attr-tdth-colspan", "http://www.whatwg.org/specs/web-apps/current-work/multipage/tabular-data.html#attr-tdth-colspan")),
    ATTR_MENU_LABEL(new Link("attr-menu-label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-menu-label")),
    ATTR_BUTTON_VALUE(new Link("attr-button-value", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-button-value")),
    ATTR_TRACK_SRC(new Link("attr-track-src", "http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#attr-track-src")),
    ATTR_IFRAME_SANDBOX(new Link("attr-iframe-sandbox", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-iframe-element.html#attr-iframe-sandbox")),
    ATTR_FS_NOVALIDATE(new Link("attr-fs-novalidate", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-novalidate")),
    ATTR_FS_FORMENCTYPE(new Link("attr-fs-formenctype", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fs-formenctype")),
    HANDLER_WINDOW_ONONLINE(new Link("handler-window-ononline", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-ononline")),
    ATTR_DETAILS_OPEN(new Link("attr-details-open", "http://www.whatwg.org/specs/web-apps/current-work/multipage/interactive-elements.html#attr-details-open")),
    HANDLER_WINDOW_ONBEFOREUNLOAD(new Link("handler-window-onbeforeunload", "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-window-onbeforeunload")),
    ATTR_FE_AUTOFOCUS(new Link("attr-fe-autofocus", "http://www.whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.html#attr-fe-autofocus")),
    ATTR_FORM_AUTOCOMPLETE(new Link("attr-form-autocomplete", "http://www.whatwg.org/specs/web-apps/current-work/multipage/forms.html#attr-form-autocomplete")),
    ATTR_OPTION_LABEL(new Link("attr-option-label", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-option-label")),
    ATTR_SELECT_MULTIPLE(new Link("attr-select-multiple", "http://www.whatwg.org/specs/web-apps/current-work/multipage/the-button-element.html#attr-select-multiple"));

    //<<END OF THE GENERATED SECTION

    public static Collection<Attribute> GLOBAL_ATTRIBUTES = EnumSet.range(ACCESSKEY, TITLE);
    public static Collection<Attribute> EVENT_ATTRIBUTES = EnumSet.range(ONABORT, ONWAITING);
    
    private Link link;

    private Attribute() {
    }

    private Attribute(Link link) {
        this.link = link;
    }

    public static String attributeId2EnumName(String attributeId) {
        return attributeId.replace('-', '_').toUpperCase();
    }

    public String getName() {
        return getAttributeId().substring(getAttributeId().lastIndexOf('_'));
    }

    public URL getDescription() {
        return link.getUrl();
    }

    public String getAttributeId() {
        return link.getName();
    }
}
