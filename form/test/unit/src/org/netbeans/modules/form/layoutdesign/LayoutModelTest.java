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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.layoutdesign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;
import junit.framework.TestCase;

public class LayoutModelTest extends TestCase {

    public LayoutModelTest(String name) {
        super(name);
    }

    /**
     * Tests the layout model by comparing a dumped layout model (filled with
     * a layout structure) with a golden file. In case the dump does not match,
     * it is saved into a file under build/test/unit/results so it can be
     * compared with the golden file manually.
     */
    public void testDump() throws IOException {
        String golden = getExpectedDump();

        LayoutModel layoutModel = new LayoutModel();
        LayoutModelTest.fillModelToDump(layoutModel);
        String dump = layoutModel.dump(null);
        dump = Pattern.compile("\n").matcher(dump) // NOI18N
            .replaceAll(System.getProperty("line.separator")); // NOI18N

        boolean same = dump.equals(golden);
        if (!same) {
            writeWrongDump(dump);
        }

        assertTrue("Model dump gives different result than expected", same);
    }

    private String getExpectedDump() throws IOException {
        URL url = getClass().getClassLoader().getResource("");
        // classloader points to form/build/test/unit/classes/
        // golden file is in form/test/unit/data/goldenfiles/
        File file = new File(url.getFile() + "../../../../test/unit/data/goldenfiles/layoutModelDump.pass")
                    .getCanonicalFile();
        assert file.length() < 100000;
        int length = (int) file.length();

        FileReader fr = null;
        try {
            fr = new FileReader(file);
            char[] buf = new char[length];
            fr.read(buf);
            return new String(buf);
        }
        finally {
            if (fr != null) {
                fr.close();
            }
        }
    }

    static void fillModelToDump(LayoutModel model) {
        int id = 0;

        LayoutComponent container = new LayoutComponent(Integer.toString(++id), true);
        model.addComponent(container, null, -1);

        LayoutComponent lc1 = new LayoutComponent(Integer.toString(++id), false);
        model.addComponent(lc1,  container, -1);
        LayoutComponent lc2 = new LayoutComponent(Integer.toString(++id), false);
        model.addComponent(lc2,  container, -1);
        LayoutComponent lc3 = new LayoutComponent(Integer.toString(++id), false);
        model.addComponent(lc3,  container, -1);
        LayoutComponent lc4 = new LayoutComponent(Integer.toString(++id), false);
        model.addComponent(lc4,  container, -1);
        LayoutComponent lc5 = new LayoutComponent(Integer.toString(++id),  false);
        model.addComponent(lc5,  container, -1);

        // horizontal, first row
        LayoutInterval li;
        LayoutInterval subGroup;
        LayoutInterval group = new LayoutInterval(LayoutInterval.SEQUENTIAL);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        group.add(lc1.getLayoutInterval(LayoutConstants.HORIZONTAL), -1);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        li = lc2.getLayoutInterval(LayoutConstants.HORIZONTAL);
        li.setSizes(LayoutConstants.USE_PREFERRED_SIZE, 80, LayoutConstants.USE_PREFERRED_SIZE);
        group.add(li, -1);
        li = new LayoutInterval(LayoutInterval.SINGLE);
        li.setSizes(LayoutConstants.NOT_EXPLICITLY_DEFINED, 100,  Short.MAX_VALUE);
        group.add(li, -1);
        container.getDefaultLayoutRoot(LayoutConstants.HORIZONTAL).add(group, -1);

        // horizontal, second row
        group = new LayoutInterval(LayoutInterval.SEQUENTIAL);
        subGroup = new LayoutInterval(LayoutInterval.PARALLEL);
        subGroup.setGroupAlignment(LayoutConstants.TRAILING);
        li = lc3.getLayoutInterval(LayoutConstants.HORIZONTAL);
        li.setAlignment(LayoutConstants.LEADING);
        subGroup.add(li, -1);
        subGroup.add(lc4.getLayoutInterval(LayoutConstants.HORIZONTAL), -1);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        group.add(subGroup, -1);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        li = lc5.getLayoutInterval(LayoutConstants.HORIZONTAL);
        li.setSizes(0, LayoutConstants.NOT_EXPLICITLY_DEFINED, Short.MAX_VALUE);
        group.add(li, -1);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        container.getDefaultLayoutRoot(LayoutConstants.HORIZONTAL).add(group, -1);

        // vertical, first row
        group = new LayoutInterval(LayoutInterval.SEQUENTIAL);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        subGroup = new LayoutInterval(LayoutInterval.PARALLEL);
        subGroup.setGroupAlignment(LayoutConstants.BASELINE);
        subGroup.add(lc1.getLayoutInterval(LayoutConstants.VERTICAL), -1);
        subGroup.add(lc2.getLayoutInterval(LayoutConstants.VERTICAL), -1);
        group.add(subGroup, -1);
        group.add(new LayoutInterval(LayoutInterval.SINGLE), -1);

        // vertical, second row
        subGroup = new LayoutInterval(LayoutInterval.PARALLEL);
        li = new LayoutInterval(LayoutInterval.SEQUENTIAL);
        li.add(lc3.getLayoutInterval(LayoutConstants.VERTICAL), -1);
        li.add(new LayoutInterval(LayoutInterval.SINGLE), -1);
        li.add(lc4.getLayoutInterval(LayoutConstants.VERTICAL), -1);
        subGroup.add(li, -1);
        subGroup.add(lc5.getLayoutInterval(LayoutConstants.VERTICAL), -1);
        group.add(subGroup, -1);
        li = new LayoutInterval(LayoutInterval.SINGLE);
        li.setSizes(LayoutConstants.NOT_EXPLICITLY_DEFINED, 50,  Short.MAX_VALUE);
        group.add(li, -1);

        container.getDefaultLayoutRoot(LayoutConstants.VERTICAL).add(group, -1);
    }

    private void writeWrongDump(String dump) throws IOException {
        URL url = getClass().getClassLoader().getResource("");
        // will go to form/build/test/unit/results
        File file = new File(url.getFile() + "../results").getCanonicalFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, "layoutModelDump.fail");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(dump);
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
}
