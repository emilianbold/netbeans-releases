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
package org.netbeans.modules.cloud.oracle;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.common.api.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * 
 */
public class DataCenterHandler extends DefaultHandler {

    private final List<DataCenters.DataCenter> dataCenters = new ArrayList<DataCenters.DataCenter>();

    private final StringBuilder content = new StringBuilder();

    private boolean isDataCenters;

    private boolean isDataCenter;

    private String shortName;

    private String longName;

    private String jcsVersion;

    public List<DataCenters.DataCenter> getDataCenters() {
        return dataCenters;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        content.setLength(0);
        if ("DataCenters".equals(qName)) {
            isDataCenters = true;
        } else if (isDataCenters && "DataCenter".equals(qName)) {
            isDataCenter = true;
        } else if (isDataCenter && "JCS".equals(qName)) {
            jcsVersion = attributes.getValue("version");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDataCenter) {
            if ("DataCenter".equals(qName)) {
                isDataCenter = false;
                if (jcsVersion != null) {
                    dataCenters.add(new DataCenters.DataCenter(shortName, longName,
                            Version.fromJsr277OrDottedNotationWithFallback(jcsVersion)));
                }
                shortName = null;
                longName = null;
                jcsVersion = null;
            } else if ("ShortName".equals(qName)) {
                shortName = content.toString();
            } else if ("LongName".equals(qName)) {
                longName = content.toString();
            }
        } else if (isDataCenter) {
            if ("DataCenters".equals(qName)) {
                isDataCenters = false;
                isDataCenter = false;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isDataCenter) {
            content.append(ch, start, length);
        }
    }
}
