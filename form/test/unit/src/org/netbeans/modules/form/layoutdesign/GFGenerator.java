/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
