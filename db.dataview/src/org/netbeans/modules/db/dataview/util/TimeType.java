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

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

/**
 * Implements a date type which can generate instances of java.sql.Date and other JDBC
 * date-related types.
 * 
 * @author Ahimanikya Satapathy
 */
public class TimeType extends TimestampType {

    // DateFormat objects are not thread safe. Do not share across threads w/o synch block.
    private final DateFormat[] TIME_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat("HH:mm:ss", LOCALE),
        DateFormat.getTimeInstance(DateFormat.SHORT, LOCALE)
    };

    public TimeType() {
        for (int i = 0; i < TIME_PARSING_FORMATS.length; i++) {
            TIME_PARSING_FORMATS[i].setLenient(false);
        }
    }
    private static TimeZone TIME_ZONE = TimeZone.getDefault();

    public static TimeZone getTimeZone() {
        return TIME_ZONE;
    }

    public static long normalizeTime(long rawTimeMillis) {
        int dstOffset = (TIME_ZONE.inDaylightTime(new java.util.Date(rawTimeMillis))) ? TIME_ZONE.getDSTSavings() : 0;
        return (rawTimeMillis < DateType.INCREMENT_DAY) ? rawTimeMillis : (rawTimeMillis % DateType.INCREMENT_DAY) + dstOffset;
    }

    private Time getNormalizedTime(long time) {
        Time ret = null;
        ret = new Time(normalizeTime(time));
        return ret;
    }

    private Time convertToTime(Object value) throws DBException {
        if (null == value) {
            return null;
        } else if (value instanceof java.sql.Time) {
            return (Time) value;
        } else if (value instanceof String) {
            java.util.Date dVal = null;
            int i = 0;
            while (dVal == null && i < TIME_PARSING_FORMATS.length) {
                dVal = TIME_PARSING_FORMATS[i].parse((String) value, new ParsePosition(0));
                i++;
            }

            if (dVal == null) {
                throw new DBException(NbBundle.getMessage(TimeType.class,"LBL_invalid_time"));
            }
            return getNormalizedTime(dVal.getTime());
        } else {
            throw new DBException(NbBundle.getMessage(TimeType.class,"LBL_invalid_time"));
        }
    }

    @Override
    public Object convert(Object value) throws DBException {
        if (value instanceof Time) {
            return value;
        }
        return convertToTime(value);
    }
}