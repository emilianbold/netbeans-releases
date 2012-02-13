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
package org.netbeans.modules.javascript2.editor.doclets;

import java.util.List;
import org.netbeans.modules.javascript2.editor.doclets.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.model.JsComment;

/**
 * Represents block of JSDoc comment which contains particular {@link JsDocTag}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocBlock extends JsComment {

    private final List<JsDocElement> tags;
    private final JsDocCommentType type;

    /**
     * Creates new {@code JsDocBlock} with given parameters.
     *
     * @param startOffset start offset of the comment
     * @param endOffset end offset of the comment
     * @param type comment {@code JsDocCommentType}
     * @param tags list of tags contained in this block or {@code null} if block is of special type
     */
    public JsDocBlock(int startOffset, int endOffset, JsDocCommentType type, List<JsDocElement> tags) {
        super(startOffset, endOffset);
        this.type = type;
        this.tags = tags;
    }

    /**
     * Gets list of {@code JsDocTag}s of this block.
     * @return list of {@code JsDocTag}s
     */
    public List<JsDocElement> getTags() {
        return tags;
    }

    /**
     * Gets type of the jsDoc block comment.
     * @return type of the jsDoc block comment
     */
    public JsDocCommentType getType() {
        return type;
    }
}
