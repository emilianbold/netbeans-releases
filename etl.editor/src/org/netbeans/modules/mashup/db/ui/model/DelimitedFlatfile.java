/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mashup.db.ui.model;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;


import com.sun.sql.framework.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Concrete bean wrapper implementation for delimited instances of FlatfileDBTable,
 * exposing read-only properties for display in a Flatfile DB property sheet. TODO Extend
 * to a mutable class (adding setters as required) to allow editing of Flat File Database
 * properties.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DelimitedFlatfile extends FlatfileTable {
    
    private static transient final Logger mLogger = Logger.getLogger(DelimitedFlatfile.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Creates new instance of DelimitedFlatfile, wrapping the given FlatfileDBTable
     * instance.
     * 
     * @param dbTable FlatfileDBTable instance (delimited type) to be wrapped.
     */
    public DelimitedFlatfile(FlatfileDBTable dbTable) {
        super(dbTable);
    }

    /**
     * Gets the character(s) used to delimit fields in this file.
     * 
     * @return field delimiter string
     */
    public String getFieldDelimiter() {
        String delimiter = getProperty(PropertyKeys.FIELDDELIMITER);
        if (delimiter == null || delimiter.length() == 0) {
            String nbBundle1 = mLoc.t("BUND182: <None>");
            delimiter = nbBundle1.substring(15);
        } else {
            delimiter = StringUtil.escapeControlChars(delimiter);
        }
        return delimiter;
    }

    /**
     * Gets the text qualifier, if any, used to delimit text data in this file.
     * 
     * @return text qualifier
     */
    public String getTextQualifier() {
        String qualifier = getProperty(PropertyKeys.QUALIFIER);
        if (qualifier == null || qualifier.length() == 0) {
            String nbBundle2 = mLoc.t("BUND182: <None>");
            qualifier = nbBundle2.substring(15);
        } else {
            if("''".equals(qualifier)) {
                qualifier = "'"; 
            }
        }
        return qualifier;
    }

}

