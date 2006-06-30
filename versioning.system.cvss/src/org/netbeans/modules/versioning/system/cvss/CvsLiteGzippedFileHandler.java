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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.request.Request;
import org.netbeans.lib.cvsclient.request.GzipStreamRequest;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Cvs client library FileHandler that performs
 * operations using openide filesystems.
 *
 * @author Petr Kuzel
 */
class CvsLiteGzippedFileHandler extends CvsLiteFileHandler {

    /**
     * Get any requests that must be sent before commands are sent, to init
     * this file handler.
     * @return an array of Requests that must be sent
     */
    public Request[] getInitialisationRequests() {
        return new Request[]{
            new GzipStreamRequest()
        };
    }

    protected Reader getProcessedReader(File f) throws IOException {
        return new InputStreamReader(new GZIPInputStream(new
                FileInputStream(f)));
    }

    protected InputStream getProcessedInputStream(File f) throws IOException {
        return new GZIPInputStream(new FileInputStream(f));
    }

}
