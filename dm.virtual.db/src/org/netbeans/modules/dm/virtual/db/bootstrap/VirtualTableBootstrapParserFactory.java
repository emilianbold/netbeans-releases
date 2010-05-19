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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.openide.util.NbBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating instances of VirtualTableBootstrapParser.
 *
 * @author Ahimanikya Satapathy
 */
public final class VirtualTableBootstrapParserFactory {

    private static Logger mLogger = Logger.getLogger(VirtualTableBootstrapParserFactory.class.getName());

    class NullBootstrapParser implements VirtualTableBootstrapParser {

        public List buildVirtualDBColumns(VirtualDBTable table) {
            return Collections.EMPTY_LIST;
        }

        public void makeGuess(VirtualDBTable file) {
        }

        public boolean acceptable(VirtualDBTable table) {
            return false;
        }
    }

    /* Singleton instance of VirtualTableBootstrapParserFactory */
    private static VirtualTableBootstrapParserFactory instance;
    private static final String LOG_CATEGORY = VirtualTableBootstrapParserFactory.class.getName();
    private static final String[][] PARSER_MAP_INFO = new String[][]{
        {PropertyKeys.DELIMITED, "org.netbeans.modules.dm.virtual.db.bootstrap.DelimitedBootstrapParser"},
        {PropertyKeys.FIXEDWIDTH, "org.netbeans.modules.dm.virtual.db.bootstrap.FixedWidthBootstrapParser"},
        {PropertyKeys.XML, "org.netbeans.modules.dm.virtual.db.bootstrap.XMLBootstrapParser"},
        {PropertyKeys.RSS, "org.netbeans.modules.dm.virtual.db.bootstrap.RSSBootstrapParser"},
        {PropertyKeys.WEB, "org.netbeans.modules.dm.virtual.db.bootstrap.WebBootstrapParser"},
        {PropertyKeys.WEBROWSET, "org.netbeans.modules.dm.virtual.db.bootstrap.WebrowsetBootstrapParser"},
        {PropertyKeys.SPREADSHEET, "org.netbeans.modules.dm.virtual.db.bootstrap.SpreadsheetBootstrapParser"}
    };

    public static VirtualTableBootstrapParserFactory getInstance() {
        if (instance == null) {
            instance = new VirtualTableBootstrapParserFactory();
        }
        return instance;
    }

    public static Collection getParserTypes() {
        List<String> types = new ArrayList<String>(7);
        types.add(PropertyKeys.FIXEDWIDTH);
        types.add(PropertyKeys.RSS);
        types.add(PropertyKeys.WEBROWSET);
        types.add(PropertyKeys.SPREADSHEET);
        types.add(PropertyKeys.XML);
        types.add(PropertyKeys.WEB);
        types.add(PropertyKeys.DELIMITED);
        return types;
    }
    /* Map of parser type to VirtualTableBootstrapParser instance */
    private Map keynameToClassMap;

    /** Creates a private default instance of VirtualTableBootstrapParserFactory. */
    private VirtualTableBootstrapParserFactory() {
        keynameToClassMap = new HashMap();

        for (int i = 0; i < PARSER_MAP_INFO.length; i++) {
            String key = PARSER_MAP_INFO[i][0];
            String parserClass = PARSER_MAP_INFO[i][1];

            try {
                keynameToClassMap.put(key, Class.forName(parserClass));
            } catch (Exception ignore) {
                mLogger.log(Level.SEVERE, NbBundle.getMessage(VirtualTableBootstrapParserFactory.class, "MSG_parser_classnames") + LOG_CATEGORY, ignore);
            }
        }
    }

    public synchronized VirtualTableBootstrapParser getBootstrapParser(String type) {
        Class parserClass = (Class) keynameToClassMap.get(type);
        if (parserClass == null) {
            return null;
        }

        try {
            return (VirtualTableBootstrapParser) parserClass.newInstance();
        } catch (Exception e) {
            return new NullBootstrapParser();
        }
    }

    public String getParserType(VirtualDBTable table) {
        String result = PropertyKeys.DELIMITED;
        VirtualTableBootstrapParser bootstrapParser = null;
        String[] parsers = (String[]) getParserTypes().toArray(new String[0]);
        for (String parser : parsers) {
            bootstrapParser = getBootstrapParser(parser);
            try {
                if (bootstrapParser.acceptable(table)) {
                    result = parser;
                    break;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
    }
}
