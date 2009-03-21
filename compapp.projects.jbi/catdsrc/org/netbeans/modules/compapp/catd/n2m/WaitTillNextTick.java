/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.catd.n2m;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author Bing Lu
 */
public class WaitTillNextTick implements Runnable {
    //Example  2001-07-04T12:08:56.235-0700
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private long mStart;
    private double mIntervalInMillis;

    public WaitTillNextTick(String intervalInMillis) {
        this(null, intervalInMillis);
    }

    public WaitTillNextTick(String startTime, String intervalInMillis) {
        Date start = null;
        if (startTime == null || startTime.trim().equals("")) {
            start = new Date(0);
        } else {
            try {
                start = DATE_FORMAT.parse(startTime);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        mStart = start.getTime();
        mIntervalInMillis = Long.parseLong(intervalInMillis);
    }

    public void run() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - mStart <= 0) {
            TestCase.fail("The test framework does not support a start time in future");
        }
        long nextTick = mStart + (long)(Math.ceil((currentTime - mStart) / mIntervalInMillis) * mIntervalInMillis);
        long timeToSleep = nextTick - currentTime;

        if (timeToSleep == 0) {
            return;
        }
        //System.out.println("\nEnter sleeping for " + timeToSleep + " milliseconds");
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(timeToSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //System.out.println("Wake up from sleeping");
    }
}
