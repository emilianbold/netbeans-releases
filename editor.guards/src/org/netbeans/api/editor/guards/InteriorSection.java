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

import javax.swing.text.Position;
import org.netbeans.modules.editor.guards.InteriorSectionImpl;

/**
 * Represents an advanced guarded section.
 * It consists of three pieces: a header, body, and footer.
 * The header and footer are guarded but the body is not.
 */
public final class InteriorSection extends GuardedSection {
    /**
     * Creates new section.
     * @param name Name of the new section.
     */
    InteriorSection(InteriorSectionImpl impl) {
        super(impl);
    }

    /**
     * Sets the text of the body.
     * @param text the new text
     */
    public void setBody(String text) {
        getImpl().setBody(text);
    }
    
    /**
     * Returns the contents of the body part of the section. If the
     * section is invalid the method returns null.
     * @return contents of the body or null, if the section is not valid.
     */
    public String getBody() {
        return getImpl().getBody();
    }

    /**
     * Sets the text of the header.
     * @param text the new text
     */
    public void setHeader(String text) {
        getImpl().setHeader(text);
    }

    /**
     * Returns the contents of the header part of the section. If the
     * section is invalid the method returns null.
     * @return contents of the header or null, if the section is not valid.
     */
    public String getHeader() {
        return getImpl().getHeader();
    }

    /**
     * Sets the text of the footer.
     * Note that the footer of the section must have exactly one line.
     * So, all interior newline characters will be replaced by spaces.
     *
     * @param text the new text
     */
    public void setFooter(String text) {
        getImpl().setFooter(text);
    }

    /**
     * Returns the contents of the footer part of the guarded section.
     * The method will return null, if the section is not valid.
     * @return contents of the footer part, or null if the section is not valid.
     */
    public String getFooter() {
        return getImpl().getFooter();
    }
    
    /**
     * Returns a position where the body starts
     * @return the start position of the body part
     */
    public Position getBodyStartPosition() {
        return getImpl().getBodyStartPosition();
    }
    
    /**
     * Returns a position where the body ends
     * @return the end position of the body part
     */
    public Position getBodyEndPosition() {
        return getImpl().getBodyEndPosition();
    }
    
    InteriorSectionImpl getImpl() {
        return (InteriorSectionImpl) super.getImpl();
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("InteriorSection:"+name); // NOI18N
      try {
        buf.append("HEADER:\""); // NOI18N
        buf.append(header.getText());
        buf.append("\"");
        buf.append("BODY:\""); // NOI18N
        buf.append(body.getText());
        buf.append("\"");
        buf.append("BOTTOM:\""); // NOI18N
        buf.append(bottom.getText());
        buf.append("\"");
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      return buf.toString();
    }*/
}
