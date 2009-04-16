/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.storage.types;

import java.beans.PropertyEditorManager;
import org.netbeans.modules.dlight.spi.impl.TimeEditor;
import org.openide.util.NbBundle;

/**
 * Metric value class for time interval in nanoseconds. Immutable.
 *
 * @author Alexey Vladykin
 */
public class Time implements Comparable<Time> {

    private static final String NANOSECOND = NbBundle.getMessage(Time.class, "Time.Nanosecond"); //NOI18N
    private final long nanos;

    /**
     * Creates new instance.
     *
     * @param nanos  time in nanoseconds
     */
    public Time(long nanos) {
        this.nanos = nanos;
    }

    /**
     * @return time in nanoseconds
     */
    public long getNanos() {
        return nanos;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(nanos).append(' ').append(NANOSECOND);
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Time) {
            return this.nanos == ((Time) obj).nanos;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 29 + (int) (nanos ^ (nanos >>> 32));
    }

    public int compareTo(Time that) {
        if (this.nanos < that.nanos) {
            return -1;
        } else if (this.nanos == that.nanos) {
            return 0;
        } else {
            return 1;
        }
    }

    static {
        PropertyEditorManager.registerEditor(Time.class, TimeEditor.class);
    }

}
