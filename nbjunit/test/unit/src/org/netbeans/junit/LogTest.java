/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.junit;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/** Is logging working OK?
 */
public class LogTest extends NbTestCase {
    private Logger LOG = Logger.getLogger("my.log.for.test");
    
    public LogTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }
    
    public void testLogEnable() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LOG.setLevel(Level.FINEST);
        LOG.finest("Too finest message to be seen");
        assertEquals(seq.toString(), 0, seq.length());
    }


    public void testLogSurviveRemoval() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LogManager.getLogManager().readConfiguration();

        LOG.warning("Look msg");
        if (seq.toString().indexOf("Look msg") == -1) {
            fail(seq.toString());
        }
    }

    public void testPublish() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.INFO);
        LOG.info("some stuff");
        LOG.log(Level.INFO, null);
        LOG.log(Level.INFO, "found {0} great", new File(getWorkDir(), "some/thing"));
        Object o0 = new Object();
        Object o1 = new Object();
        LOG.log(Level.INFO, "o0={0} o1={1}", new Object[] {o0, o1});
        LOG.log(Level.INFO, "o0={0}", o0);
        class Group {
            @Override public String toString() {
                return String.format("Group@%h", this);
            }
            class Item {
                @Override public String toString() {
                    return String.format("Item@%h:%H", this, Group.this);
                }
            }
        }
        Group g2 = new Group();
        Object i3 = g2.new Item();
        Object i4 = g2.new Item();
        Group g5 = new Group();
        Object i6 = g5.new Item();
        LOG.log(Level.INFO, "g2={0} i3={1} i4={2}", new Object[] {g2, i3, i4});
        LOG.log(Level.INFO, "g5={0} i6={1}", new Object[] {g5, i6});
        LOG.log(Level.INFO, "i4={0} o1={1}", new Object[] {i4, o1});
        assertEquals("some stuff\n"
                + "null\n"
                + "found WORKDIR/org.netbeans.junit.LogTest/testPublish/some/thing great\n"
                + "o0=java.lang.Object@0 o1=java.lang.Object@1\n"
                + "o0=java.lang.Object@0\n"
                + "g2=Group@2 i3=Item@3:2 i4=Item@4:2\n"
                + "g5=Group@5 i6=Item@6:5\n"
                + "i4=Item@4:2 o1=java.lang.Object@1\n",
                seq.toString().replaceAll("(?m)^\\Q[my.log.for.test] THREAD: Test Watch Dog: testPublish MSG: \\E(.+)(\r?\n)+", "$1\n").replace('\\', '/'));
    }

}
