/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
