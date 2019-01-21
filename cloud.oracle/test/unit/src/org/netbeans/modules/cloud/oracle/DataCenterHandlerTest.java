package org.netbeans.modules.cloud.oracle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.common.api.Version;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
/**
 *
 * 
 */
public class DataCenterHandlerTest extends NbTestCase {

    public DataCenterHandlerTest(String name) {
        super(name);
    }

    public void testParsing() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        DataCenterHandler handler = new DataCenterHandler();
        File file = new File(getDataDir(), "testfiles/test.xml");
        URL url = file.toURI().toURL();
        InputStream is = new BufferedInputStream(url.openStream());
        try {
            parser.parse(is, handler);
            List<DataCenters.DataCenter> dataCenters = handler.getDataCenters();
            assertEquals(11, dataCenters.size());
            assertEquals("us1", dataCenters.get(0).getShortName());
            assertEquals("us2", dataCenters.get(1).getShortName());
            assertEquals("us3", dataCenters.get(2).getShortName());
            assertEquals("us4", dataCenters.get(3).getShortName());

            assertEquals(Version.fromJsr277OrDottedNotationWithFallback("13.2"),
                    dataCenters.get(1).getJcsVersion());
            assertEquals(Version.fromJsr277OrDottedNotationWithFallback("13.1"),
                    dataCenters.get(2).getJcsVersion());
        } finally {
            is.close();
        }
    }
}
