/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.completion;

import java.awt.Color;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.web.common.api.FileReferenceCompletion;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public abstract class AttrValuesCompletion {

    private static final Map<String, Map<String, ValueCompletion<HtmlCompletionItem>>> SUPPORTS =
            new HashMap<String, Map<String, ValueCompletion<HtmlCompletionItem>>>();

    public static final ValueCompletion<HtmlCompletionItem> FILE_NAME_SUPPORT = new FilenameSupport();
    private static final ValueCompletion<HtmlCompletionItem> CONTENT_TYPE_SUPPORT = new ContentTypeSupport();

    static {
        //TODO uff, such long list ... redo it so it resolves according to the DTD attribute automatically
        putSupport("a", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("area", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("link", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("base", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("script", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "longdesc", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("frame", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("iframe", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("body", "background", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "classid", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "codebase", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "data", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "usemap", FILE_NAME_SUPPORT); //NOI18N
        putSupport("applet", "codebase", FILE_NAME_SUPPORT); //NOI18N
        putSupport("q", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("blackquote", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("ins", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("del", "cite", FILE_NAME_SUPPORT); //NOI18N
        putSupport("form", "action", FILE_NAME_SUPPORT); //NOI18N

        putSupport("script", "type", CONTENT_TYPE_SUPPORT); //NOI18N
        putSupport("style", "type", CONTENT_TYPE_SUPPORT); //NOI18N
        putSupport("link", "type", CONTENT_TYPE_SUPPORT); //NOI18N
    }

    private static void putSupport(String tag, String attr, ValueCompletion<HtmlCompletionItem> support) {
        Map<String, ValueCompletion<HtmlCompletionItem>> map = SUPPORTS.get(tag);
        if (map == null) {
            map = new HashMap<String, ValueCompletion<HtmlCompletionItem>>();
            SUPPORTS.put(tag, map);
        }
        map.put(attr, support);
    }

    public static Map<String, ValueCompletion<HtmlCompletionItem>> getSupportsForTag(String tag) {
        return SUPPORTS.get(tag.toLowerCase(Locale.ENGLISH));
    }

    public static ValueCompletion<HtmlCompletionItem> getSupport(String tag, String attr) {
        Map<String, ValueCompletion<HtmlCompletionItem>> map = getSupportsForTag(tag);
        if(map == null) {
            return null;
        } else {
            return map.get(attr.toLowerCase(Locale.ENGLISH));
        }
    }

    public static class ContentTypeSupport implements ValueCompletion {

        public static String[] TYPICAL_CONTENT_TYPES = new String[]{"text/css", "text/javascript"}; //NOI18N

        @Override
        public List<HtmlCompletionItem> getItems(FileObject file, int offset, String valuePart) {
            //linear search, too little items, no problem
            List<HtmlCompletionItem> items = new ArrayList<HtmlCompletionItem>();
            for(int i = 0; i < TYPICAL_CONTENT_TYPES.length; i++) {
                if(TYPICAL_CONTENT_TYPES[i].startsWith(valuePart)) {
                    items.add(HtmlCompletionItem.createAttributeValue(TYPICAL_CONTENT_TYPES[i], offset));
                }
            }
            return items;
        }

    }

    public static class FilenameSupport extends FileReferenceCompletion<HtmlCompletionItem> {

        @Override
        public HtmlCompletionItem createFileItem(int anchor, String name, Color color, ImageIcon icon) {
            return HtmlCompletionItem.createFileCompletionItem(name, anchor, color, icon);
        }

        @Override
        public HtmlCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            return HtmlCompletionItem.createGoUpFileCompletionItem(anchor, color, icon); // NOI18N
        }
    }
    
}
