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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.dm.virtual.db.api.Property;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.openide.util.NbBundle;

/**
 * Factory for creating property template with default value.
 * 
 * @author Ahimanikya Satapathy
 */
public class TemplateFactory {

    private static Map<String, List<Property>> templateMap = new HashMap<String, List<Property>>(2);
    private static List<Property> delimiterMap = new ArrayList<Property>(11);
    private static List<Property> fixedwidthMap = new ArrayList<Property>(12);
    private static List<Property> webMap = new ArrayList<Property>(12);
    private static List<Property> xmlMap = new ArrayList<Property>(4);
    private static List<Property> webrowsetMap = new ArrayList<Property>(2);
    private static List<Property> rssMap = new ArrayList<Property>(2);
    private static List<Property> spreadsheetMap = new ArrayList<Property>(3);
    private static Class STR_TYPE = java.lang.String.class;
    private static Class INT_TYPE = java.lang.Integer.class;
    private static Class BOOL_TYPE = java.lang.Boolean.class;
    private static String EMPTY_STR = "";
    private static String ZERO = "0";
    private static String CRLFORLF = "\r\n \n";
    private static String CRLF = "\r\n";
    private static String TRUE = "true";
    private static String VARCHAR = "varchar";

    static {
        webMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.WEB));
        webMap.add(new Property(PropertyKeys.REFRESH, BOOL_TYPE, TRUE));
        webMap.add(new Property(PropertyKeys.ISFIRSTLINEHEADER, BOOL_TYPE, TRUE));
        webMap.add(new Property(PropertyKeys.RECORDDELIMITER, STR_TYPE, CRLFORLF));
        webMap.add(new Property(PropertyKeys.ROWSTOSKIP, INT_TYPE, ZERO, false));
        webMap.add(new Property(PropertyKeys.FIELDDELIMITER, STR_TYPE, ","));
        webMap.add(new Property(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER, STR_TYPE, EMPTY_STR));
        webMap.add(new Property(PropertyKeys.QUALIFIER, STR_TYPE, "\""));
        webMap.add(new Property(PropertyKeys.MAXFAULTS, INT_TYPE, ZERO, false));
        webMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        webMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "60", false));
        webMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
    }

    static {
        delimiterMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.DELIMITED));
        delimiterMap.add(new Property(PropertyKeys.FILENAME, STR_TYPE, EMPTY_STR));
        delimiterMap.add(new Property(PropertyKeys.ISFIRSTLINEHEADER, BOOL_TYPE, TRUE));
        delimiterMap.add(new Property(PropertyKeys.RECORDDELIMITER, STR_TYPE, CRLFORLF));
        delimiterMap.add(new Property(PropertyKeys.ROWSTOSKIP, INT_TYPE, ZERO, false));
        delimiterMap.add(new Property(PropertyKeys.FIELDDELIMITER, STR_TYPE, ","));
        delimiterMap.add(new Property(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER, STR_TYPE, EMPTY_STR));
        delimiterMap.add(new Property(PropertyKeys.QUALIFIER, STR_TYPE, "\""));
        delimiterMap.add(new Property(PropertyKeys.MAXFAULTS, INT_TYPE, ZERO, false));
        delimiterMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        delimiterMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "60", false));
        delimiterMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
    }

    static {
        fixedwidthMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.FIXEDWIDTH));
        fixedwidthMap.add(new Property(PropertyKeys.FILENAME, STR_TYPE, EMPTY_STR));
        fixedwidthMap.add(new Property(PropertyKeys.ISFIRSTLINEHEADER, BOOL_TYPE, TRUE));
        fixedwidthMap.add(new Property(PropertyKeys.RECORDDELIMITER, STR_TYPE, CRLF));
        fixedwidthMap.add(new Property(PropertyKeys.ROWSTOSKIP, INT_TYPE, ZERO, false));
        fixedwidthMap.add(new Property(PropertyKeys.HEADERBYTESOFFSET, INT_TYPE, ZERO));
        fixedwidthMap.add(new Property(PropertyKeys.WIZARDRECORDLENGTH, INT_TYPE, ZERO));
        fixedwidthMap.add(new Property(PropertyKeys.MAXFAULTS, INT_TYPE, ZERO, false));
        fixedwidthMap.add(new Property(PropertyKeys.WIZARDFIELDCOUNT, INT_TYPE, ZERO));
        fixedwidthMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        fixedwidthMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
    }

    static {
        xmlMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.XML));
        xmlMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        xmlMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
        xmlMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "60", false));
        xmlMap.add(new Property(PropertyKeys.TYPE, STR_TYPE, PropertyKeys.READWRITE));
        xmlMap.add(new Property(PropertyKeys.ROWNAME, STR_TYPE, EMPTY_STR));
    }

    static {
        webrowsetMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.WEBROWSET));
        webrowsetMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        webrowsetMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
        webrowsetMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "60", false));
    }

    static {
        rssMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.RSS));
        rssMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        rssMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
        rssMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "200", false));
    }
    
    static {
        spreadsheetMap.add(new Property(PropertyKeys.LOADTYPE, STR_TYPE, PropertyKeys.SPREADSHEET));
        spreadsheetMap.add(new Property(PropertyKeys.WIZARDDEFAULTSQLTYPE, STR_TYPE, VARCHAR, false));
        spreadsheetMap.add(new Property(PropertyKeys.WIZARDFILEPATH, STR_TYPE, EMPTY_STR));
        spreadsheetMap.add(new Property(PropertyKeys.WIZARDDEFAULTPRECISION, INT_TYPE, "60", false));
        spreadsheetMap.add(new Property(PropertyKeys.ISFIRSTLINEHEADER, BOOL_TYPE, TRUE));
        spreadsheetMap.add(new Property(PropertyKeys.ROWSTOSKIP, INT_TYPE, ZERO, false));
        spreadsheetMap.add(new Property(PropertyKeys.MAXFAULTS, INT_TYPE, ZERO, false));
    }
    
    static {
        templateMap.put(PropertyKeys.DELIMITED, delimiterMap);
        templateMap.put(PropertyKeys.FIXEDWIDTH, fixedwidthMap);
        templateMap.put(PropertyKeys.WEB, webMap);
        templateMap.put(PropertyKeys.RSS, rssMap);
        templateMap.put(PropertyKeys.XML, xmlMap);
        templateMap.put(PropertyKeys.WEBROWSET, webrowsetMap);
        templateMap.put(PropertyKeys.SPREADSHEET, spreadsheetMap);
    }

    public synchronized static Map getProperties(String type) {
        List base = (List) templateMap.get(type);
        if (base == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(TemplateFactory.class, "MSG_file_type") + type);
        }

        Map clonedProps = new HashMap(base.size());
        Iterator iter = base.iterator();
        while (iter.hasNext()) {
            Property p = (Property) ((Property) iter.next()).clone();
            clonedProps.put(p.getName(), p);
        }
        return clonedProps;
    }
}
