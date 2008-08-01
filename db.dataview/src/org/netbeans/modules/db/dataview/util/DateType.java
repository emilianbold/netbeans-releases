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
package org.netbeans.modules.db.dataview.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

/**
 * Implements a date type which can generate instances of java.sql.Date and other JDBC
 * date-related types.
 *
 * @author Ahimanikya Satapathy
 */
public class DateType extends TimestampType {

    // DateFormat objects are not thread safe. Do not share across threads w/o synch block.
    private final DateFormat[] DATE_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat("yyyy-MM-dd", LOCALE),
        new SimpleDateFormat("MM-dd-yyyy", LOCALE),
        DateFormat.getTimeInstance(DateFormat.SHORT, LOCALE)
    };

    public DateType() {
        for (int i = 0; i < DATE_PARSING_FORMATS.length; i++) {
            DATE_PARSING_FORMATS[i].setLenient(false);
        }
    }

    /* Increment to use in computing a successor value. */
    // One day = 1 day x 24 hr/day x 60 min/hr x 60 sec/min x 1000 ms/sec
    static final long INCREMENT_DAY = 1 * 24 * 60 * 60 * 1000;

    private Date convertToDate(Object value) throws DBException {
        Calendar cal = Calendar.getInstance();

        if (null == value) {
            return null;
        } else if (value instanceof Timestamp) {
            cal.setTimeInMillis(((Timestamp) value).getTime());
        } else if (value instanceof String) {
            java.util.Date dVal = null;
            int i = 0;
            while (dVal == null && i < DATE_PARSING_FORMATS.length) {
                dVal = DATE_PARSING_FORMATS[i].parse((String) value, new ParsePosition(0));
                i++;
            }

            if (dVal == null) {
                throw new DBException(NbBundle.getMessage(DateType.class,"LBL_invalid_date"));
            }
            cal.setTimeInMillis(dVal.getTime());
        } else {
            throw new DBException(NbBundle.getMessage(DateType.class,"LBL_invalid_date"));
        }

        // Normalize to 0 hour in default time zone.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    @Override
    public Object convert(Object value) throws DBException {
        try {
            if (value instanceof Date) {
                return value;
            }
            return convertToDate(value);

        } catch (DBException e) {
            throw new DBException(NbBundle.getMessage(DateType.class,"MSG_failure_convert_date",value.getClass().getName(), value));
  
        }
    }
}