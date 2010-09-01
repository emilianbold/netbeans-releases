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
package org.netbeans.modules.html.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author marekfukala
 */
public class ElementAttributes {

    //parsed from http://www.whatwg.org/specs/web-apps/current-work/#global-attributes
    private static final String[] GLOBAL = new String[]{
        "accesskey",
        "class",
        "contenteditable",
        "contextmenu",
        "dir",
        "draggable",
        "hidden",
        "id",
        "itemid",
        "itemprop",
        "itemref",
        "itemscope",
        "itemtype",
        "lang",
        "spellcheck",
        "style",
        "tabindex",
        "title"};

    private static final String[] GLOBAL_EVENT = new String[]{
        "onabort",
        "onblur",
        "oncanplay",
        "oncanplaythrough",
        "onchange",
        "onclick",
        "oncontextmenu",
        "ondblclick",
        "ondrag",
        "ondragend",
        "ondragenter",
        "ondragleave",
        "ondragover",
        "ondragstart",
        "ondrop",
        "ondurationchange",
        "onemptied",
        "onended",
        "onerror",
        "onfocus",
        "onformchange",
        "onforminput",
        "oninput",
        "oninvalid",
        "onkeydown",
        "onkeypress",
        "onkeyup",
        "onload",
        "onloadeddata",
        "onloadedmetadata",
        "onloadstart",
        "onmousedown",
        "onmousemove",
        "onmouseout",
        "onmouseover",
        "onmouseup",
        "onmousewheel",
        "onpause",
        "onplay",
        "onplaying",
        "onprogress",
        "onratechange",
        "onreadystatechange",
        "onscroll",
        "onseeked",
        "onseeking",
        "onselect",
        "onshow",
        "onstalled",
        "onsubmit",
        "onsuspend",
        "ontimeupdate",
        "onvolumechange",
        "onwaiting"
    };

    //generated
    private static final String[][] ATTRS = new String[][]{
        {"a"}, {"href", "target", "ping", "rel", "media", "hreflang", "type"},
        {"b"}, {},
        {"i"}, {},
        {"p"}, {},
        {"q"}, {"cite"},
        {"br"}, {},
        {"dd"}, {},
        {"dl"}, {},
        {"dt"}, {},
        {"em"}, {},
        {"hr"}, {},
        {"li"}, {"ol", "value"},
        {"ol"}, {"reversed", "start"},
        {"rp"}, {},
        {"rt"}, {},
        {"td"}, {"colspan", "rowspan", "headers"},
        {"th"}, {"colspan", "rowspan", "headers", "scope"},
        {"tr"}, {},
        {"ul"}, {},
        {"bdo"}, {"dir"},
        {"col"}, {"span"},
        {"del"}, {"cite", "datetime"},
        {"dfn"}, {"title"},
        {"div"}, {},
        {"img"}, {"alt", "src", "usemap", "ismap", "width", "height"},
        {"ins"}, {"cite", "datetime"},
        {"kbd"}, {},
        {"map"}, {"name"},
        {"nav"}, {},
        {"pre"}, {},
        {"var"}, {},
        {"wbr"}, {},
        {"area"}, {"alt", "coords", "shape", "href", "target", "ping", "rel", "media", "hreflang", "type"},
        {"abbr"}, {"title"},
        {"base"}, {"href", "target"},
        {"body"}, {"onafterprint", "onbeforeprint", "onbeforeunload", "onblur", "onerror", "onfocus", "onhashchange", "onload", "onmessage", "onoffline", "ononline", "onpagehide", "onpageshow", "onpopstate", "onredo", "onresize", "onstorage", "onundo", "onunload"},
        {"code"}, {},
        {"cite"}, {},
        {"form"}, {"accept-charset", "action", "autocomplete", "enctype", "method", "name", "novalidate", "target"},
        {"head"}, {},
        {"html"}, {"manifest"},
        {"link"}, {"href", "rel", "media", "hreflang", "type", "sizes", "title"},
        {"meta"}, {"name", "http-equiv", "content", "charset"},
        {"mark"}, {},
        {"menu"}, {"type", "label"},
        {"ruby"}, {},
        {"span"}, {},
        {"samp"}, {},
        {"time"}, {"datetime", "pubdate"},
        {"aside"}, {},
        {"audio"}, {"src", "preload", "autoplay", "loop", "controls"},
        {"embed"}, {"src", "type", "width", "height"},
        {"input"}, {"accept", "alt", "autocomplete", "autofocus", "checked", "disabled", "form", "formaction", "formenctype", "formmethod", "formnovalidate", "formtarget", "height", "list", "max", "maxlength", "min", "multiple", "name", "pattern", "placeholder", "readonly", "required", "size", "src", "step", "type", "value", "width"},
        {"label"}, {"form", "for"},
        {"meter"}, {"value", "min", "max", "low", "high", "optimum", "form"},
        {"param"}, {"name", "value"},
        {"style"}, {"media", "type", "scoped", "title"},
        {"small"}, {},
        {"thead"}, {},
        {"table"}, {"summary"},
        {"title"}, {},
        {"tfoot"}, {},
        {"tbody"}, {},
        {"video"}, {"src", "poster", "preload", "autoplay", "loop", "controls", "width", "height"},
        {"button"}, {"autofocus", "disabled", "form", "formaction", "formenctype", "formmethod", "formnovalidate", "formtarget", "name", "type", "value"},
        {"canvas"}, {"width", "height"},
        {"figure"}, {},
        {"footer"}, {},
        {"header"}, {},
        {"iframe"}, {"src", "srcdoc", "name", "sandbox", "seamless", "width", "height"},
        {"keygen"}, {"autofocus", "challenge", "disabled", "form", "keytype", "name"},
        {"legend"}, {},
        {"option"}, {"disabled", "label", "selected", "value"},
        {"object"}, {"data", "type", "name", "usemap", "form", "width", "height"},
        {"output"}, {"for", "form", "name"},
        {"source"}, {"src", "type", "media"},
        {"strong"}, {},
        {"select"}, {"autofocus", "disabled", "form", "multiple", "name", "size"},
        {"script"}, {"Global", " attributes", "src", "async", "defer", "type", "charset"},
        {"article"}, {},
        {"address"}, {},
        {"command"}, {"type", "label", "icon", "disabled", "checked", "radiogroup", "title"},
        {"caption"}, {},
        {"details"}, {"open"},
        {"section"}, {},
        {"colgroup"}, {"span"},
        {"fieldset"}, {"disabled", "form", "name"},
        {"noscript"}, {},
        {"optgroup"}, {"disabled", "label"},
        {"progress"}, {"value", "max", "form"},
        {"textarea"}, {"autofocus", "cols", "disabled", "form", "maxlength", "name", "placeholder", "readonly", "required", "rows", "wrap"},
        {"blockquote"}, {"cite"}};


    private static Map<String, Collection<String>> ELEMENT2ATTRS;

    private static void initialize() {
        ELEMENT2ATTRS = new HashMap<String, Collection<String>>();

        Collection<String> global = Arrays.asList(GLOBAL);
        Collection<String> event = Arrays.asList(GLOBAL_EVENT);

        for(int i = 0; i < ATTRS.length; ) {
            String name = ATTRS[i++][0];
            String[] attrs = ATTRS[i++];

            Collection joined = new LinkedList(Arrays.asList(attrs)); //specific
            joined.addAll(global);
            joined.addAll(event);

            ELEMENT2ATTRS.put(name, joined);
        }

    }

    public static synchronized Collection<String> getAttrNamesForElement(String elementName) {
        if(ELEMENT2ATTRS == null) {
            initialize();
        }

        return ELEMENT2ATTRS.get(elementName);
    }

}
