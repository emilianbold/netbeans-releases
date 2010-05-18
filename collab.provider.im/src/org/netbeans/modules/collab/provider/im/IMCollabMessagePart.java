/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
