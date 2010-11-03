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

package org.netbeans.modules.reglib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * A {@code StatusData} object contains product registration status.
 * Actually it stores user selection in reminder dialog. Registration status
 * is used to control showing of reminder dialog.
 * 
 */
public class StatusData {
    public static final String STATUS_UNKNOWN = "unknown";
    public static final String STATUS_REGISTERED = "registered";
    public static final String STATUS_LATER = "later";
    public static final String STATUS_NEVER = "never";
    
    public static final int DEFAULT_DELAY = 7;
    
    private String status;
    private Date timestamp;
    /** Delay after which reminder dialo will be shown again when Later option is selected.*/
    private int delay;

    /**
     * Creates a {@code StatusData} object with status value and generated time stamp.
     * 
     */
    public StatusData(String status, int delay) {
        this.status = status;
        this.timestamp = new Date();
        this.delay = delay;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    public int getDelay() {
        return delay;
    }
    
    /**
     * Reads the status data from the XML document on the 
     * specified input stream.  The XML document must be 
     * in the format described by the <a href="#XMLSchema">
     * status data schema</a>.
     * 
     * The specified stream remains open after this method returns.
     * 
     * @param in the input stream from which to read the XML document.
     * @return a {@code StatusData} object read from the input
     * stream.
     * 
     * @throws IllegalArgumentException if the input stream 
     * contains an invalid status data.
     * 
     * @throws IOException if an error occurred when reading from the input stream.
     */
    public static StatusData loadFromXML(InputStream in) throws IOException {
        return StatusDocument.load(in);
    }
    
    /**
     * Writes the status data to the specified output stream 
     * in the format described by the <a href="#XMLSchema">
     * status data schema</a> with "UTF-8" encoding.
     * 
     * The specified stream remains open after this method returns.
     * 
     * @param os the output stream on which to write the XML document.
     * 
     * @throws IOException if an error occurred when writing to the output stream.
     */
    public void storeToXML(OutputStream os) throws IOException {
        StatusDocument.store(os, this);
    }

    /** 
     * Returns a newly allocated byte array containing the status
     * data in XML format.
     *
     * @return a newly allocated byte array containing the status
     * data in XML format.
     */
    public byte[] toXML() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            storeToXML(out);
            return out.toByteArray();
        } catch (IOException e) {
            // should not reach here
            return new byte[0];
        }
    }

    /**
     * Returns a string representation of this status data in XML
     * format.
     * 
     * @return a string representation of this status data in XML
     *         format.
     */
    @Override
    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            storeToXML(out);
            return out.toString("UTF-8");
        } catch (IOException e) {
            // should not reach here
            return "Error creating the return string.";
        }
    }
}
