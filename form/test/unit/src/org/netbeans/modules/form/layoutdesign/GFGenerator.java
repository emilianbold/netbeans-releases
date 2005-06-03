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
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import junit.framework.TestCase;

/**
 * Updates golden files. Not a real test. It generates a golden file for
 * LayoutModelTest. Run this test manually from IDE when the golden files
 * needs to be updated.
 */

public class GFGenerator extends TestCase {
    
    public GFGenerator(String name) {
        super(name);
    }

    /**
     * Generates a golden file for layout model dump test.
     */
    public void testGenerate() throws IOException {
        File file = createFile();

        LayoutModel layoutModel = new LayoutModel();
        LayoutModelTest.fillModelToDump(layoutModel);

        writeFile(file, layoutModel.dump(null));
    }

    private File createFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("");
        // classloader points to form/build/test/unit/classes/
        // we need directory form/test/unit/data/goldenfiles/
        File file = new File(url.getFile() + "../../../../test/unit/data/goldenfiles")
                    .getCanonicalFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, "layoutModelDump.pass");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    private void writeFile(File f, String dump) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(dump);
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
}
