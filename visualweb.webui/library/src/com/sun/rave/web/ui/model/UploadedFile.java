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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package com.sun.rave.web.ui.model;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author avk
 */
public interface UploadedFile extends Serializable {

    /**
     * Returns a {@link java.io.InputStream InputStream} for reading the file.
     *
     * @return An {@link java.io.InputStream InputStream} for reading the file.
     *
     * @exception IOException if there is a problem while reading the file
     */
     public InputStream getInputStream() throws IOException;


    /**
     * Get the content-type that the browser communicated with the request
     * that included the uploaded file. If the browser did not specify a
     * content-type, this method returns null.
     *
     * @return  the content-type that the browser communicated with the request
     * that included the uploaded file
     */
    public String getContentType();


    /**
     * Use this method to retrieve the name that the file has on the web 
     * application user's local system. 
     *
     * @return the name of the file on the web app user's system
     */
    public String getOriginalName();


    // ------------------------------------------------------- FileItem methods


    /**
     * The size of the file in bytes
     *
     * @return The size of the file in bytes.
     */
    public long getSize();


    /**
     * Use this method to retrieve the contents of the file as an array of bytes. 
     * @return The contents of the file as a byte array
     */
    public byte[] getBytes();


    /**
     * Use this method to retrieve the contents of the file as a String
     *
     * @return the contents of the file as a String
     */
    public String getAsString();


    /**
     * Write the contents of the uploaded file to a file on the server host. 
     * Note that writing files outside of the web server's tmp directory
     * must be explicitly permitted through configuration of the server's 
     * security policy. 
     * 
     * This method is not guaranteed to succeed if called more than once for
     * the same item. 
     * @param file The <code>File</code> where the contents should be written
     *
     * @exception Exception the 
     */
    public void write(File file) throws Exception;

    /**
     * Dispose of the resources associated with the file upload (this will
     * happen automatically when the resource is garbage collected). 
     */
    public void dispose();
    
}
