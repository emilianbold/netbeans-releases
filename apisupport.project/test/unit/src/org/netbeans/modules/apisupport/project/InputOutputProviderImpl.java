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

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * @author Jaroslav Tulach
 */
public class InputOutputProviderImpl extends IOProvider {

    static NbTestCase running;
    
    /** Creates a new instance of InputOutputProviderImpl */
    public InputOutputProviderImpl() {}
    
    public static void registerCase(NbTestCase r) {
        running = r;
    }
    
    public InputOutput getIO(String name, boolean newIO) {
        return new IO(name);
    }
    
    public OutputWriter getStdOut() {
        Assert.assertNotNull("A test case must be registered", running);
        return new OW("stdout");
    }
    
    private static class OW extends OutputWriter {
        
        private ErrorManager err;
        
        public OW(String prefix) {
            super(new StringWriter());
            err = ErrorManager.getDefault().getInstance("output[" + prefix + "]");
        }
        
        public void println(String s, OutputListener l) throws IOException {
            write("println: " + s + " listener: " + l);
            flush();
        }
        
        public void reset() throws IOException {
            write("Internal reset");
            flush();
        }
        
        public void write(char[] buf, int off, int len) {
            write(new String(buf, off, len));
        }
        
        public void write(int c) {
            write(String.valueOf((char)c));
        }
        
        public void write(char[] buf) {
            write(buf, 0, buf.length);
        }
        
        public void write(String s, int off, int len) {
            write(s.substring(off, off + len));
        }
        public void write(String s) {
            err.log(s);
        }
    }
    
    private static class IO implements InputOutput {
        
        private OW w;
        private boolean closed;
        
        public IO(String n) {
            w = new OW(n);
            w.write("Created IO named '" + n + "'");
            w.flush();
        }
        
        public OutputWriter getOut() {
            return w;
        }
        
        public Reader getIn() {
            w.write("Creating reader");
            return new StringReader("");
        }
        
        public OutputWriter getErr() {
            return w;
        }
        
        public void closeInputOutput() {
            w.write("closeInputOutput");
            closed = true;
        }
        
        public boolean isClosed() {
            w.write("isClosed");
            return closed;
        }
        
        public void setOutputVisible(boolean value) {
            w.write("setOutputVisible: " + value);
        }
        
        public void setErrVisible(boolean value) {
            w.write("setErrVisible: " + value);
        }
        
        public void setInputVisible(boolean value) {
            w.write("setInputVisible: " + value);
        }
        
        public void select() {
            w.write("select");
        }
        
        public boolean isErrSeparated() {
            return false;
        }
        
        public void setErrSeparated(boolean value) {}
        
        public boolean isFocusTaken() {
            return false;
        }
        
        public void setFocusTaken(boolean value) {}
        
        public Reader flushReader() {
            return getIn();
        }
        
    }
}
