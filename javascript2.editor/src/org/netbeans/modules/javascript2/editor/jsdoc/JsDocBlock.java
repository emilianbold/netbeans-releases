/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.jsdoc;

import java.util.*;
import java.util.Map.Entry;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement.Type;
import org.netbeans.modules.javascript2.editor.model.JsComment;

/**
 * Represents block of JSDoc comment which contains particular {@link JsDocTag}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocBlock extends JsComment {

    private final Map<JsDocElement.Type, List<JsDocElement>> tags =
            new EnumMap<JsDocElement.Type, List<JsDocElement>>(JsDocElement.Type.class);
    private final JsDocCommentType type;

    /**
     * Creates new {@code JsDocBlock} with given parameters.
     *
     * @param offsetRange offset range of the comment
     * @param type comment {@code JsDocCommentType}
     * @param elements list of tags contained in this block, never {@code null}
     */
    public JsDocBlock(OffsetRange offsetRange, JsDocCommentType type, List<JsDocElement> elements) {
        super(offsetRange);
        this.type = type;
        initTags(elements);
    }

    private void initTags(List<JsDocElement> elements) {
        for (JsDocElement jsDocElement : elements) {
            List<JsDocElement> list = tags.get(jsDocElement.getType());
            if (list == null) {
                list = new LinkedList<JsDocElement>();
                tags.put(jsDocElement.getType(), list);
            }
            tags.get(jsDocElement.getType()).add(jsDocElement);
        }
    }

    /**
     * Gets list of all {@code JsDocTag}s.
     * <p>
     * Should be used just in testing use cases.
     * @return list of {@code JsDocTag}s
     */
    protected List<? extends JsDocElement> getTags() {
        List<JsDocElement> allTags = new LinkedList<JsDocElement>();
        Iterator<Entry<Type, List<JsDocElement>>> iterator = tags.entrySet().iterator();
        while (iterator.hasNext()) {
            allTags.addAll(iterator.next().getValue());
        }
        return allTags;
    }

    /**
     * Gets list of {@code JsDocTag}s of given type.
     * @return list of {@code JsDocTag}s
     */
    public List<? extends JsDocElement> getTagsForType(JsDocElement.Type type) {
        List<JsDocElement> tagsForType = tags.get(type);
        return tagsForType == null ? Collections.<JsDocElement>emptyList() : tagsForType;
    }

    /**
     * Gets list of {@code JsDocTag}s of given types.
     * @return list of {@code JsDocTag}s
     */
    public List<? extends JsDocElement> getTagsForTypes(JsDocElement.Type[] types) {
        List<JsDocElement> list = new LinkedList<JsDocElement>();
        for (JsDocElement.Type type : types) {
            list.addAll(getTagsForType(type));
        }
        return list;
    }



    /**
     * Gets type of the jsDoc block comment.
     * @return type of the jsDoc block comment
     */
    public JsDocCommentType getType() {
        return type;
    }
}