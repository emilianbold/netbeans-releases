/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.util;

import java.text.ParseException;
import javax.swing.JFormattedTextField.AbstractFormatter;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Alexey Vladykin
 */
public final class TimeFormatter extends AbstractFormatter implements ValueFormatter {

    private static final String MAX = "max"; // NOI18N

    @Override
    public Object stringToValue(String text) throws ParseException {

        if (MAX.equalsIgnoreCase(text)) {
            return Long.MAX_VALUE;
        }

        long minutes = 0;
        int colonPos = text.indexOf(':');
        if (0 <= colonPos) {
            try {
                minutes = Long.parseLong(text.substring(0, colonPos));
                text = text.substring(colonPos + 1);
            } catch (NumberFormatException ex) {
                throw new ParseException(text, 0);
            }
        }

        long nanos = 0;
        int dotPos = text.indexOf('.');
        if (0 <= dotPos) {
            nanos = parseNanos(text.substring(dotPos + 1));
            text = text.substring(0, dotPos);
        }

        long seconds = 0;
        try {
            seconds = Long.parseLong(text);
        } catch (NumberFormatException ex) {
            throw new ParseException(text, 0);
        }

        return minutesToNanos(minutes) + SECONDS.toNanos(seconds) + nanos;
    }

    @Override
    public String valueToString(Object value) {
        return format(((Long) value).longValue());
    }

    @Override
    public String format(long value) {
        if (value == Long.MAX_VALUE) {
            return MAX;
        }

        long minutes = nanosToMinutes(value);
        long seconds = NANOSECONDS.toSeconds(value - minutesToNanos(minutes));
        long nanos = value - minutesToNanos(minutes) - SECONDS.toNanos(seconds);

        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%d:%02d", minutes, seconds)); // NOI18N

        if (1000000 <= nanos) {
            buf.append('.');
            buf.append(nanos / 100000000);
            nanos %= 100000000;
            if (0 < nanos) {
                buf.append(nanos /  10000000);
                nanos %= 10000000;
                if (0 < nanos) {
                    buf.append(nanos / 1000000);
                }
            }
        }

        return buf.toString();
    }

    private static long parseNanos(String frac) throws ParseException {
        long result = 0;
        for (int i = 0; i < 9; ++i) {
            char c = i < frac.length() ? frac.charAt(i) : '0';
            if (c < '0' || '9' < c) {
                throw new ParseException(frac, i);
            }
            result = 10 * result + (c - '0');
        }
        return result;
    }

    private static long minutesToNanos(long m) {
        return SECONDS.toNanos(60 * m);
    }

    private static long nanosToMinutes(long m) {
        return NANOSECONDS.toSeconds(m) / 60;
    }
}
