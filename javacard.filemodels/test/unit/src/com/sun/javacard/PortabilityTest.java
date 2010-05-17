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
package com.sun.javacard;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tim Boudreau
 */
public class PortabilityTest {

    public PortabilityTest() {
    }

    @Test
    public void testGetString() {
        System.out.println("getString");
        String key = "Invalid_hexadecimal_number";
        String result = Portability.getString(key, null);
        assertEquals("Invalid hexadecimal number", result);

        key = "Must_be_exactly_5_bytes";
        String expResult = "Monkey is not 5 bytes long";
        result = Portability.getString(key, "Monkey");
        assertEquals (expResult, result);
    }

    @Test
    public void testCreateXMLReader() throws IOException, SAXException {
        System.out.println("createXMLReader");
        XMLReader result = Portability.createXMLReader();
        assertNotNull (result);
        assertTrue (result instanceof XMLReader);
        result.setContentHandler(new DefaultHandler());
        InputStream in = PortabilityTest.class.getResourceAsStream("random.xml");
        try {
            result.parse(new InputSource(in));
        } finally {
            in.close();
        }
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        InputStream in = PortabilityTest.class.getResourceAsStream("random.xml");
        try {
            Document result = Portability.parse(in);
            assertNotNull (result);
        } finally {
            in.close();
        }
    }

    @Test
    public void testLogException() {
        System.err.println("The following exception *should* be logged");
        Portability.logException(new Exception());
    }

}