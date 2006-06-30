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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** JUnit test suite with Jemmy support
 *
 * @author pj97932
 */
public class ParserPresentTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ParserPresentTest(String testName) {
        super(testName);
    }
    
    public void testParserPresent() throws IOException {
        JspParserAPI api = JspParserFactory.getJspParser();
        assertNotNull(api);
    }
    
    public void testSameInstance() throws IOException {
        JspParserAPI api1 = JspParserFactory.getJspParser();
        JspParserAPI api2 = JspParserFactory.getJspParser();
        log(api1.toString());
        log(api2.toString());
        assertSame("JSP parser instance should be the same all the time", api1, api2);
    }
    
}
