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
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class DocumentEventContext extends EventContext {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* offset */
    protected int offset;

    /* length */
    protected int length;

    /* text */
    protected String text;

    /**
         *
         * @param channel
         */
    public DocumentEventContext(String eventID, Document document, int offset, int length, String text) {
        super(eventID, document);
        this.offset = offset;
        this.length = length;
        this.text = text;
    }

    /**
     * getSource
     *
     * @return        event source
     */
    public Object getSource() {
        return this.evSource;
    }

    /**
     * getOffset
     *
     * @return        offset
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * getText
     *
     * @return        text
     */
    public String getText() {
        return this.text;
    }

    /**
     * getLength
     *
     * @return        length
     */
    public int getLength() {
        return this.length;
    }
}
