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

package org.netbeans.spi.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/** Represents the environment an {@link OptionProcessor} operates in. Streams can be
 * used to read and write data provided by the user. It is also possible
 * to obtain current user directory. In future this class may be extended
 * with additional new getters that will describe the operating environment
 * in greater detail.
 *
 * @author Jaroslav Tulach
 */
public final class Env {
    private InputStream is;
    private PrintStream os;
    private PrintStream err;
    private File currentDir;

    /** Creates a new instance of Env */
    Env(InputStream is, OutputStream os, OutputStream err, File currentDir) {
        this.is = is;
        this.os = os instanceof PrintStream ? (PrintStream)os : new PrintStream(os);
        this.err = err instanceof PrintStream ? (PrintStream)err : new PrintStream(err);
        this.currentDir = currentDir;
    }
    
    /**
     * Get an output stream to which data may be sent.
     * @return stream to write to
     */
    public PrintStream getOutputStream() {
        return os;
    }
    /**
     * Get an output stream to which error messages may be sent.
     * @return stream to write to
     */
    public PrintStream getErrorStream() {
        return err;
    }

    /** 
     * The directory relative file operations shall be relative to. Can 
     * be specified while starting the parse of {@link org.netbeans.api.sendopts.CommandLine}.
     * 
     * @return file representing current directory
     */
    public File getCurrentDirectory () {
        return currentDir;
    }

    /**
     * Get an input stream that may supply additional data.
     * @return stream to read from
     */
    public InputStream getInputStream() {
        return is;
    }
} 
