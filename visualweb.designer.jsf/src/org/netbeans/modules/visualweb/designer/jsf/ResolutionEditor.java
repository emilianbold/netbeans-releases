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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer.jsf;

import java.beans.PropertyEditorSupport;

import org.openide.util.NbBundle;


/**
 * Property editor for editing the "page size" property of the designer
 */
public class ResolutionEditor extends PropertyEditorSupport {
    // XXX should this be static?
    private static final String[] tags =
        new String[] {
            NbBundle.getMessage(ResolutionEditor.class, "ResolutionNone"), // NOI18N
            NbBundle.getMessage(ResolutionEditor.class, "Resolution640x480"), // NOI18N
            NbBundle.getMessage(ResolutionEditor.class, "Resolution800x600"), // NOI18N
            NbBundle.getMessage(ResolutionEditor.class, "Resolution1024x768"), // NOI18N
            NbBundle.getMessage(ResolutionEditor.class, "Resolution1280x1024") // NOI18N
        };

    public String getJavaInitializationString() {
        return getAsText();
    }

    public String getAsText() {
        int val = ((Integer)getValue()).intValue();

        return tags[val];
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        for (int i = 0, n = tags.length; i < n; i++) {
            if (text.equals(tags[i])) {
                setValue(new Integer(i));

                return;
            }
        }

        throw new java.lang.IllegalArgumentException(text);
    }

    public String[] getTags() {
        return tags.clone();
    }
}
