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
package com.sun.rave.web.ui.component;

import java.util.Comparator;

/**
 * <p>Defines a component for displaying an alarm icon based on the
 * alarm seveirty.</p>
 */
public class Alarm extends AlarmBase implements Comparator {

    /** Severity of an alarm. */
    public static final String SEVERITY_DOWN     = "down";
    public static final String SEVERITY_CRITICAL = "critical";
    public static final String SEVERITY_MAJOR    = "major";
    public static final String SEVERITY_MINOR    = "minor";
    public static final String SEVERITY_OK       = "ok";

    public static final String DEFAULT_SEVERITY = SEVERITY_OK;

    // Severity level of an alarm.
    private final int SEVERITY_LEVEL_DOWN     = 1;
    private final int SEVERITY_LEVEL_CRITICAL = 2;
    private final int SEVERITY_LEVEL_MAJOR    = 3;
    private final int SEVERITY_LEVEL_MINOR    = 4;
    private final int SEVERITY_LEVEL_OK       = 5;

    /** Default constructor. */
    public Alarm() {
    }

    /** Create an instance with the given severity. */
    public Alarm(String severity) {
        setSeverity(severity);
    }

    /**
     * Compare the given objects for severity order.
     */
    public int compare(Object o1, Object o2) throws ClassCastException {
        int s1 = getSeverityLevel((Alarm) o1);
        int s2 = getSeverityLevel((Alarm) o2);
        return (s1 > s2) ? -1 : s1 == s2 ? 0 : 1;
    }

    /**
     * Indicates whether some other object is "equal to" this Comparator.
     */
    public boolean equals(Object o) throws ClassCastException {
        int s1 = getSeverityLevel(this);
        int s2 = getSeverityLevel((Alarm) o);
        return (s1 == s2);
    }

    /**
     * Helper method to get the severity level of an alarm.
     */
    private int getSeverityLevel(Alarm alarm) {
        int severity = SEVERITY_LEVEL_OK;
        if (alarm.getSeverity().equals(SEVERITY_DOWN)) {
            severity = SEVERITY_LEVEL_DOWN;
        } else if (alarm.getSeverity().equals(SEVERITY_CRITICAL)) {
            severity = SEVERITY_LEVEL_CRITICAL;
        } else if (alarm.getSeverity().equals(SEVERITY_MAJOR)) {
            severity = SEVERITY_LEVEL_MAJOR;
        } else if (alarm.getSeverity().equals(SEVERITY_MINOR)) {
            severity = SEVERITY_LEVEL_MINOR;          
        }
        return severity;
    }
}
