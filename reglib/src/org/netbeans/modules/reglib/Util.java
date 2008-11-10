/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mslama
 */
class Util {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.Util"); // NOI18N
    
    static String commandOutput(Process p) throws IOException {
        Reader r = null;
        Reader err = null;
        try {
            r = new InputStreamReader(p.getInputStream());
            err = new InputStreamReader(p.getErrorStream());
            String output = commandOutput(r);
            String errorMsg = commandOutput(err);
            p.waitFor();
            return output + errorMsg.trim();
        } catch (InterruptedException e) {
            LOG.log(Level.INFO,"Interrupted:",e);
            return e.getMessage();
        } finally {
            IOException exc = null;
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ex) {
                exc = ex;
            }
            if (err != null) {
                err.close();
            }
            if (exc != null) {
                throw exc;
            }
        }
    }
    
    static String commandOutput(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = r.read()) > 0) {
            if (c != '\r') {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Formats the Date into a timestamp string in YYYY-MM-dd HH:mm:ss GMT.
     * @param timestamp Date
     * @return a string representation of the timestamp
     *         in the YYYY-MM-dd HH:mm:ss GMT format.
     */
    static String formatTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "[No timestamp]";
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(timestamp);
    }

    /**
     * Parses a timestamp string in YYYY-MM-dd HH:mm:ss GMT format.
     * @param timestamp Timestamp in the YYYY-MM-dd HH:mm:ss GMT format.
     * @return Date
     */
    static Date parseTimestamp(String timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return df.parse(timestamp);
        } catch (ParseException e) {
            // should not reach here
            e.printStackTrace();
            return new Date();
        }
    }
    
}
