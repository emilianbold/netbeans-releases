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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Maros Sandor
 */
public class DiffControllerTest extends NbTestCase {

    private DiffController controller;

    public DiffControllerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        controller = DiffController.create(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
    }

    public void testCurrentDifference() throws Exception {
        int dc = controller.getDifferenceCount();
        assertEquals("Wrong number of differences", 2, dc);
    }

    public void testDifferenceIndex() throws Exception {
        int dc = controller.getDifferenceCount();
        int di = controller.getDifferenceIndex();
        assertTrue("Wrong difference index", di == -1 || di >= 0 && di < dc);
    }

    public void testComponent() throws Exception {
        JComponent c = controller.getJComponent();
        assertNotNull("Not a JComponent", c);
    }

    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private String buffer;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
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
