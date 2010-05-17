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

package org.netbeans.lib.collab;

/**
 * a message body part
 *
 * @since version 0.1
 *
 */
public interface MessagePart extends ReadOnlyMessagePart {

    /**
    * set the content of this message part
    * using the specified byte array
    * @param content content of the message
    * @param enc content encoding.  If non-null, this value is used to
    * reset the content encoding for this part.
    */
    public void setContent(byte[] content, String enc) throws CollaborationException;

    /**
     * set the content of this message part
     * using the specified byte array
     * @param is content stream
     * @param enc content encoding.  If non-null, this value is used to 
     * reset the content encoding for this part.
     */
    public void setContent(java.io.InputStream is, String enc) throws CollaborationException;

    /**
    * set the content of this message part
    * using the specified String object
    * @param content content of the message
    */
    public void setContent(String content) throws CollaborationException;
    
    
    /**
     * set the content of this message part
     * using the specified String object
     * @param content content of the message
     *@param  contentType contentType of the message
     */
    public void setContent(String content, String contentType) throws CollaborationException;
    
    
    
    /**
     * reset the part's content to "".
     */
    public void clearContent() throws CollaborationException;
    
    /**
     * sets the value of the Content-type header for this part
     * 
     * @param contentType MIME or extended content-type value.
     *  Example: text/xml
     */
    public void setContentType(String contentType) throws CollaborationException;

    /**
     * sets the name of this mesage part
     *
     * @param name filename  Example: image.gif
     */
    public void setContentName(String name) throws CollaborationException;

}

