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

package org.netbeans.api.editor.guards;

import org.netbeans.modules.editor.guards.SimpleSectionImpl;

/**
 * Represents a simple guarded section.
 * It consists of one contiguous block.
 */
public final class SimpleSection extends GuardedSection {

    /**
     * Creates new section.
     * @param name Name of the new section.
     * @param bounds The range of the section.
     */
    SimpleSection(SimpleSectionImpl impl) {
        super(impl);
    }

    /**
     * Sets the text of the section.
     * @param text the new text
     */
    public void setText(String text) {
        getImpl().setText(text);
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("SimpleSection:"+name); // NOI18N
      buf.append("\"");
      try {
        buf.append(bounds.getText());
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      buf.append("\"");
      return buf.toString();
    }*/

    SimpleSectionImpl getImpl() {
        return (SimpleSectionImpl) super.getImpl();
    }
}
