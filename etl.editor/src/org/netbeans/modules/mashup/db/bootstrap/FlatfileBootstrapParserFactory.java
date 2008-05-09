/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.mashup.db.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Factory for creating instances of FlatfileBootstrapParser.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 * @see org.netbeans.modules.mashup.db.bootstrap.FlatfileBootstrapParser
 */
public final class FlatfileBootstrapParserFactory {

    private static transient final Logger mLogger = Logger.getLogger(FlatfileBootstrapParserFactory.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class NullBootstrapParser implements FlatfileBootstrapParser {

        public List buildFlatfileDBColumns(FlatfileDBTable table) {
            return Collections.EMPTY_LIST;
        }

        public void makeGuess(FlatfileDBTable file) {
        }

        public boolean acceptable(FlatfileDBTable table) {
            return false;
        }
    }
    /* Singleton instance of FlatfileBootstrapParserFactory */
    private static FlatfileBootstrapParserFactory instance;
    /* Log4J category string */
    private static final String LOG_CATEGORY = FlatfileBootstrapParserFactory.class.getName();
    private static final String[][] PARSER_MAP_INFO = new String[][]{
        {PropertyKeys.DELIMITED, "org.netbeans.modules.mashup.db.bootstrap.DelimitedBootstrapParser"},
        {PropertyKeys.FIXEDWIDTH, "org.netbeans.modules.mashup.db.bootstrap.FixedWidthBootstrapParser"},
        {PropertyKeys.XML, "org.netbeans.modules.mashup.db.bootstrap.XMLBootstrapParser"},
        {PropertyKeys.RSS, "org.netbeans.modules.mashup.db.bootstrap.RSSBootstrapParser"},
        {PropertyKeys.WEB, "org.netbeans.modules.mashup.db.bootstrap.WebBootstrapParser"},
        {PropertyKeys.WEBROWSET, "org.netbeans.modules.mashup.db.bootstrap.WebrowsetBootstrapParser"},
        {PropertyKeys.SPREADSHEET, "org.netbeans.modules.mashup.db.bootstrap.SpreadsheetBootstrapParser"}    };

    /**
     * Gets an instance of FlatfileBootstrapParserFactory.
     *
     * @return FlatfileBootstrapParserFactory instance
     */
    public static FlatfileBootstrapParserFactory getInstance() {
        if (instance == null) {
            instance = new FlatfileBootstrapParserFactory();
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
    /* Map of parser type to FlatfileBootstrapParser instance */
    private Map keynameToClassMap;

    /** Creates a private default instance of FlatfileBootstrapParserFactory. */
    private FlatfileBootstrapParserFactory() {
        keynameToClassMap = new HashMap();

        for (int i = 0; i < PARSER_MAP_INFO.length; i++) {
            String key = PARSER_MAP_INFO[i][0];
            String parserClass = PARSER_MAP_INFO[i][1];

            try {
                keynameToClassMap.put(key, Class.forName(parserClass));
            } catch (Exception ignore) {
                mLogger.errorNoloc(mLoc.t("EDIT051: Caught error while loading parser class names. {0}", LOG_CATEGORY), ignore);
            // Ignore: Log but continue
            }
        }
    }

    /**
     * Gets an instance of FlatfileBootstrapParser, if any, associated with the given type
     * name.
     *
     * @param type type name of FlatfileBootstrapParser to be retrieved
     * @return an instance of the associated FlatfileBootstrapParser, or null if no such
     *         instance is associated with type
     */
    public synchronized FlatfileBootstrapParser getBootstrapParser(String type) {
        Class parserClass = (Class) keynameToClassMap.get(type);
        if (parserClass == null) {
            return null;
        }

        try {
            return (FlatfileBootstrapParser) parserClass.newInstance();
        } catch (Exception e) {
            return new NullBootstrapParser();
        }
    }

    public String getParserType(FlatfileDBTable table) {
        String result = PropertyKeys.DELIMITED;
        FlatfileBootstrapParser bootstrapParser = null;
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
