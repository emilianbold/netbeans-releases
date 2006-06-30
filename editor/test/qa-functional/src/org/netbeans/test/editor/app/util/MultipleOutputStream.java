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
package org.netbeans.test.editor.app.util;

import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * @author  jlahoda
 * @version
 */
public class MultipleOutputStream extends java.io.OutputStream {

    /** Creates new MultipleOutputStream */
    private Vector streams;

    public MultipleOutputStream() {
        streams = new Vector(1, 1);
    }

    public MultipleOutputStream(Vector with) throws IllegalArgumentException {
        this();
        addStreams(with);
    }

    public MultipleOutputStream(OutputStream[] with) {
        this();
        addStreams(with);
    }
    
    public void addStreams(Vector with) throws IllegalArgumentException {
        for (int cntr = 0; cntr < with.size(); cntr++) {
            if (!(with.elementAt(cntr) instanceof OutputStream)) 
                throw new IllegalArgumentException();
            Object out = with.elementAt(cntr);
            
            if (out != null)
                streams.add(out);
        };
    }
    
    public void addStreams(OutputStream[] with) {
        for (int cntr = 0; cntr < with.length; cntr++) {
            if (with[cntr] != null)
                streams.add(with[cntr]);
        };
    }
    
    public void write(int b) throws java.io.IOException {
        for (int cntr = 0; cntr < streams.size(); cntr++) {
            OutputStream out = ((OutputStream)streams.elementAt(cntr));
            
            if (out != null)
                out.write(b);
        };
    }
    
    public void flush() throws java.io.IOException {
        for (int cntr = 0; cntr < streams.size(); cntr++) {
            OutputStream out = ((OutputStream)streams.elementAt(cntr));
            
            if (out != null)
                out.flush();
        };
    }

    public void close() throws java.io.IOException {
        for (int cntr = 0; cntr < streams.size(); cntr++) {
            OutputStream out = ((OutputStream)streams.elementAt(cntr));
            
            if (out != null)
                out.close();
        };
    }

}
