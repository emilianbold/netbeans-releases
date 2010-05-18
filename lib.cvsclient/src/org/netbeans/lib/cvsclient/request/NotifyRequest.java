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
package org.netbeans.lib.cvsclient.request;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * Notify Entry.java
 *  E   Sun Nov 11 10:25:40 2001 GMT    worker  E:\test\admin   EUC
 *
 * @author  Thomas Singer
 * @version Nov 14, 2001
 */
public class NotifyRequest extends Request {
    // Constants ==============================================================

    private static final DateFormat DATE_FORMAT;
    private static final String HOST_NAME;

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy z", Locale.US);

        // detect host name
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        HOST_NAME = hostName;
    }

    // Fields =================================================================

    private final String request;

    // Setup ==================================================================

    /**
     * Creates an NotifyRequest for the specified file.
     * If the specified file is null, a IllegalArgumentException is thrown.
     */
    public NotifyRequest(File file, String command, String parameters) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("Notify "); // NOI18N
        buffer.append(file.getName());
        buffer.append('\n');
        buffer.append(command);
        buffer.append('\t');
        buffer.append(DATE_FORMAT.format(new Date()));
        buffer.append('\t');
        buffer.append(HOST_NAME);
        buffer.append('\t');
        buffer.append(file.getParent());
        buffer.append('\t');
        buffer.append(parameters);
        buffer.append('\n');
        this.request = buffer.toString();
    }

    // Implemented ============================================================

    public String getRequestString() {
        return request;
    }

    public boolean isResponseExpected() {
        return false;
    }
}
