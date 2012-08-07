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
package org.netbeans.modules.javascript2.editor.sdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.doc.api.JsModifier;
import org.netbeans.modules.javascript2.editor.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.javascript2.editor.sdoc.elements.SDocDescriptionElement;
import org.netbeans.modules.javascript2.editor.sdoc.elements.SDocElement;
import org.netbeans.modules.javascript2.editor.sdoc.elements.SDocElementType;
import org.netbeans.modules.javascript2.editor.sdoc.elements.SDocTypeNamedElement;
import org.netbeans.modules.javascript2.editor.sdoc.elements.SDocTypeSimpleElement;

/**
 * Represents documentation comment block of ScriptDoc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocComment extends JsComment {

    private final Map<SDocElementType, List<SDocElement>> tags = new EnumMap<SDocElementType, List<SDocElement>>(SDocElementType.class);

    public SDocComment(OffsetRange offsetRange, List<SDocElement> elements) {
        super(offsetRange);
        initComment(elements);
    }

    @Override
    public List<String> getSummary() {
        List<String> summaries = new LinkedList<String>();
        for (SDocElement sDocElement : getTagsForTypes(
                new SDocElementType[]{SDocElementType.DESCRIPTION, SDocElementType.CLASS_DESCRIPTION, SDocElementType.PROJECT_DESCRIPTION})) {
            summaries.add(((SDocDescriptionElement) sDocElement).getDescription());
        }
        return summaries;
    }

    @Override
    public List<String> getSyntax() {
        return Collections.<String>emptyList();
    }

    @Override
    public DocParameter getReturnType() {
        for (SDocElement sDocElement : getTagsForTypes(
                new SDocElementType[]{SDocElementType.RETURN, SDocElementType.TYPE, SDocElementType.PROPERTY})) {
            return (SDocTypeSimpleElement) sDocElement;
        }
        return null;
    }

    @Override
    public List<DocParameter> getParameters() {
        List<DocParameter> params = new LinkedList<DocParameter>();
        for (SDocElement jsDocElement : getTagsForType(SDocElementType.PARAM)) {
            params.add((SDocTypeNamedElement) jsDocElement);
        }
        return params;
    }

    @Override
    public boolean isDeprecated() {
        return !getTagsForType(SDocElementType.DEPRECATED).isEmpty();
    }

    @Override
    public Set<JsModifier> getModifiers() {
        Set<JsModifier> modifiers = EnumSet.noneOf(JsModifier.class);
        for (SDocElement jsDocElement : getTagsForType(SDocElementType.PRIVATE)) {
            modifiers.add(JsModifier.fromString(jsDocElement.getType().toString().substring(1)));
        }
        return modifiers;
    }

    private void initComment(List<SDocElement> elements) {
        for (SDocElement element : elements) {
            List<SDocElement> list = tags.get(element.getType());
            if (list == null) {
                list = new LinkedList<SDocElement>();
                tags.put(element.getType(), list);
            }
            tags.get(element.getType()).add(element);
        }
    }

    /**
     * Gets list of all {@code SDocTag}s inside this comment. <p> Used just in testing use cases.
     *
     * @return list of {@code SDocTag}s
     */
    protected List<? extends SDocElement> getTags() {
        List<SDocElement> allTags = new ArrayList<SDocElement>();
        for (List<SDocElement> list : tags.values()) {
            allTags.addAll(list);
        }
        return allTags;
    }

    /**
     * Gets list of {@code SDocElement}s of given type.
     *
     * @return list of {@code SDocElement}s
     */
    public List<? extends SDocElement> getTagsForType(SDocElementType type) {
        List<SDocElement> tagsForType = tags.get(type);
        return tagsForType == null ? Collections.<SDocElement>emptyList() : tagsForType;
    }

    /**
     * Gets list of {@code JsDocTag}s of given types.
     *
     * @return list of {@code JsDocTag}s
     */
    public List<? extends SDocElement> getTagsForTypes(SDocElementType[] types) {
        List<SDocElement> list = new LinkedList<SDocElement>();
        for (SDocElementType type : types) {
            list.addAll(getTagsForType(type));
        }
        return list;
    }
}
