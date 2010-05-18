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
 *
 *
 * @since version 0.1
 *
 */
public interface ReceiverFileStreamingProfile extends ReceiverStreamingProfile {

    /**
     * The integrity of the streamed file was maintained.
     */
    public static int INTEGRITY_OK = 0;

    /**
     * The integrity of the streamed file was compromised.
     */
    public static int INTEGRITY_COMPROMISED = 1;

    /**
     * The integrity of the streamed file cannot be determined.
     */
    public static int INTEGRITY_UNKNOWN = 2;

    /**
     * Get the name associated with this profile
     * @return The name of the file associated with the profile
     */
    public String getName();
    
    /**
     * Get the description associated with the stream
     * @return Description
     */
    public String getDescription();

    /**
     * Gets the size of the file to be streamed
     * @return The size of the stream if it is defined otherwise returns -1
     */
    public long size();
    
    /**
     * Gets the MD5 hash of the file for the streaming
     * @return The MD5 hash
     */
    public byte[] getHash();
    
    /**
     * Gets the last modified date of the file being streamed
     * @return date The date on which the file was last modified
     */
    public long getLastModified();
    
    /**
     * Gets the offset for the file transfer.
     * @return offset. The default value is 0.
     */
    public long getOffset();
    
    /**
     * Sets the offset for the file transfer. The offset should be set before  
     * accepting the stream
     * @param offset The offset from which the file should be streamed
     */
    public void setOffset(long offset);
    
    /**
     * Gets the length of the file to be transferred
     * @return length The length of bytes from the offset to be transferred. 
     * The default value is same as the file size.
     */
    public long getLength();
    
    /**
     * Sets the length of the bytes from the offset that needs to be transferred. 
     * The length should be set before accepting the stream
     * @param length The length of bytes from offset that needs to be transferred
     */
    public void setLength(long length);
    
    /**
     * Used to store the streamed data into the file. 
     * @param f The file where the data needs to be stored. If f represents a directory then
     * a file will be created in that directory with name as returned by getName.
     */
    public void addOutput(java.io.File f);
    
    /**
     * Checks the integrity of the file after the streaming is successfully completed.
     * The integrity is checked by comparing the hash value sent by the sender of the stream
     * with the file being received. This method should be called only if the streamed 
     * data was stored in a file using the {@link #addOutput(java.io.File)} method.
     * @return The result of the integrity check as defined in ReceiverFileStreamingProfile
     * @throws IllegalStateException when this method is called before the streaming is 
     * successfully completed or when the streamed data was not stored into a file.
     */
    public int checkIntegrity();
}
