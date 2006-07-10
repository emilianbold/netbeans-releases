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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessagePart;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.lib.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMCollabMessagePart extends Object implements CollabMessagePart {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private MessagePart part;

    /**
     *
     *
     */
    protected IMCollabMessagePart(MessagePart part) {
        super();
        this.part = part;
    }

    /**
     *
     *
     */
    protected MessagePart getMessagePart() {
        return part;
    }

    /**
     *
     *
     */
    public void clearContent() throws CollabException {
        try {
            getMessagePart().clearContent();
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public String getContent() throws CollabException {
        try {
            return getMessagePart().getContent();
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public String getContent(String contentType) throws CollabException {
        try {
            return getMessagePart().getContent(contentType);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public String getContentEncoding() {
        return getMessagePart().getContentEncoding();
    }

    /**
     *
     *
     */
    public String getContentName() {
        return getMessagePart().getContentName();
    }

    /**
     *
     *
     */
    public String getContentType() {
        return getMessagePart().getContentType();
    }

    /**
     *
     *
     */
    public InputStream getInputStream() throws CollabException {
        try {
            return getMessagePart().getInputStream();
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public int getSize() throws CollabException {
        try {
            return getMessagePart().getSize();
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContent(String content) throws CollabException {
        try {
            getMessagePart().setContent(content);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContent(byte[] content, String encoding)
    throws CollabException {
        try {
            getMessagePart().setContent(content, encoding);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContent(InputStream is, String encoding)
    throws CollabException {
        try {
            getMessagePart().setContent(is, encoding);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContent(String content, String contentType)
    throws CollabException {
        try {
            getMessagePart().setContent(content, contentType);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContentName(String name) throws CollabException {
        try {
            getMessagePart().setContentName(name);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void setContentType(String contentType) throws CollabException {
        try {
            getMessagePart().setContentType(contentType);
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }
}
