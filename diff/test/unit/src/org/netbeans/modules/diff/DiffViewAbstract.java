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

package org.netbeans.modules.diff;

import java.io.*;

import org.netbeans.junit.*;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

/**
 *
 * @author Martin Entlicher
 */
public abstract class DiffViewAbstract extends NbTestCase {
    /** the DiffView to work on */
    private DiffView dv;

    public DiffViewAbstract(String name) {
        super(name);
    }

    protected abstract DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException;
    
    protected void setUp() throws Exception {
        dv = createDiffView(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
    }
    
    public void testCanDiffConsistent() throws Exception {
        if (dv.canSetCurrentDifference()) {
            dv.setCurrentDifference(0);
        } else {
            try {
                dv.setCurrentDifference(0);
                fail("Should throw UnsupportedOperationException");
            } catch (UnsupportedOperationException uoex) {
                // OK
            }
        }
    }
    
    public void testCurrentDifference() throws Exception {
        if (dv.canSetCurrentDifference()) {
            int dc = dv.getDifferenceCount();
            assertEquals("Just one difference", 2, dc);
            dv.setCurrentDifference(1);
            assertEquals("Test current difference", 1, dv.getCurrentDifference());
            try {
                dv.setCurrentDifference(10);
                fail("Should report an exception.");
            } catch (IllegalArgumentException ioex) {
                // OK
            }
        }
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private String buffer;
        private Writer w;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.w = null;
            buffer = str;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getMIMEType() {
            return MIMEType;
        }

        public Reader createReader() throws IOException {
            return new StringReader(buffer);
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
    
}
