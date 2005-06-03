/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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
        container.getLayoutRoot(LayoutConstants.HORIZONTAL).add(group, -1);

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
        container.getLayoutRoot(LayoutConstants.HORIZONTAL).add(group, -1);

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

        container.getLayoutRoot(LayoutConstants.VERTICAL).add(group, -1);
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
