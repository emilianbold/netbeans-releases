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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.settings;

/**
 * Style Constants for Fonts and Colors AttributeSets.
 *
 * @author Martin Roskanin
 */
public final class EditorStyleConstants {

    private String representation;

    private EditorStyleConstants(String representation) {
        this.representation = representation;
    }

    /**
     * Name of the wave underline color attribute.
     */
    public static final Object WaveUnderlineColor = new EditorStyleConstants ("wave underline color"); //NOI18N

    /**
     * Name of the display name attribute.
     */
    public static final Object DisplayName = new EditorStyleConstants ("display name"); //NOI18N
    
    /**
     * Name of the default fonts and colots attribute.
     */
    public static final Object Default = new EditorStyleConstants ("default"); //NOI18N

    /**
     * Name of the tooltip attribute. It's value can be either <code>String</code> or
     * <code>HighlightAttributeValue</code> returning <code>String</code>.
     * 
     * <p>The tooltip text can either be plain text or HTML. If using HTML the
     * text must be enclosed in the case insensitive &lt;html&gt;&lt/html&gt; tags.
     * The only HTML tags guaranteed to work are those defined in <code>HtmlRenderer</code>.
     * 
     * @see org.openide.awt.HtmlRenderer
     * @since 1.12
     */
    public static final Object Tooltip = new EditorStyleConstants ("tooltip"); //NOI18N
    
    public String toString() {
        return representation;
    }

}
