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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;

/**
 *
 * @author Kirill Sorokin
 */
public class Text {
    private String text = "";
    private ContentType contentType = ContentType.PLAIN_TEXT;
    
    public Text() {
        // does nothing
    }
    
    public Text(final String text, final ContentType contentType) {
        this.text = text;
        this.contentType = contentType;
    }
    
    public String getText() {
        return text;
    }
    
    public ContentType getContentType() {
        return contentType;
    }
    
    public static enum ContentType {
        PLAIN_TEXT,
        HTML;
        
        public static ContentType parseContentType(final String string) throws UnrecognizedObjectException {
            if (string.equals("text/plain")) {
                return PLAIN_TEXT;
            }
            
            if (string.equals("text/html")) {
                return HTML;
            }
            
            throw new UnrecognizedObjectException("Cannot recognize content type");
        }
        
        public String getExtension() {
            switch (this) {
                case PLAIN_TEXT:
                    return ".txt";
                case HTML:
                    return ".html";
                default:
                    return "";
            }
        }
        
        public String toString() {
            switch (this) {
                case PLAIN_TEXT:
                    return "text/plain";
                case HTML:
                    return "text/html";
                default:
                    return "";
            }
        }
    }
}
