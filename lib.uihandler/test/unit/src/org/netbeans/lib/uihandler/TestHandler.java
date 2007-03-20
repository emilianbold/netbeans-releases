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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Jindrich Sedek
 */
public class TestHandler extends Handler{
    Queue<LogRecord> queue = new LinkedList<LogRecord>();
    
    public TestHandler(InputStream is) throws IOException {
        LogRecords.scan(is, this);
    }

    public void publish(LogRecord arg0) {
        queue.add(arg0);
    }

    public void flush() {
        // nothing to do
    }

    public void close() throws SecurityException {
        // nothing to do
    }

    public LogRecord read(){
        return queue.poll();
    }
    
    
}

