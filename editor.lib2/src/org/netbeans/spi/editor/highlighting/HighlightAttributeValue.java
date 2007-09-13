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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.highlighting;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Lazy evaluator for attribute values. It is up to each particular attribute
 * to declare if its values can be lazily evaluated. Attributes that declare
 * themselvs as supporting lazy evaluation can have their values specified either
 * directly or through <code>HighlightAttributeValue</code>. All users of such an attribute
 * must check for both the direct value and the lazy evaluator and use them
 * accordingly.
 * 
 * <p class="nonnormative">If an attribute supports
 * lazy evaluation the result of <code>getValue</code> call should have the same
 * type as if the attribute value were specified directly. For example, the
 * <code>EditorStyleConstants.Tooltip</code> attribute supports lazy evaluation
 * and its value can either be <code>String</code> or <code>HighlightAttributeValue&lt;String&gt;</code>.
 * 
 * @author Vita Stejskal
 * @since 1.5
 */
public interface HighlightAttributeValue<T> {

    /**
     * Gets value of an attribute.
     * 
     * @param component The text component, which highlighting layer supplied a highlight
     *   with an attribute using this evaluator as its value.
     * @param document The document, which highlighting layer supplied a highlight
     *   with an attribute using this evaluator as its value.
     * @param attributeKey The key of the attribute.
     * @param startOffset The start offset of the original highlight or any other offset
     *   inside the highlight. Always less than <code>endOffset</code>.
     * @param endOffset The end offset of the original highlight or any other offset
     *   inside the highlight. Always greater than <code>startOffset</code>.
     * 
     * @return The value of the <code>attributeKey</code> attribute.
     */
    T getValue(JTextComponent component, Document document, Object attributeKey, int startOffset, int endOffset);
    
}
