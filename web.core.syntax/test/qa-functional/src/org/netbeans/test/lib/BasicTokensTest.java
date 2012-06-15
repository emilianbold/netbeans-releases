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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public abstract class BasicTokensTest extends JellyTestCase {

    private String goldenFilePath;

    public BasicTokensTest(String name) {
        super(name);
    }

    abstract protected boolean generateGoldenFiles();

    @Override
    public void tearDown() {
        if (generateGoldenFiles()) {
            fail("GENERATING GOLDEN FILES TO " + goldenFilePath);
        } else {
            compareReferenceFiles();
        }
    }

    protected void testRun(String fileName) {
        String result = null;
        File dir = new File(getDataDir(), "tokens");
        File file = new File(dir, fileName);
        try {
            result = DumpTokens.printTokens(file);
        } catch (Throwable t) {
            NbTestCase.fail("Unable to get tokens "+t.toString());
            t.printStackTrace(System.err);
        }
        if (generateGoldenFiles()) {
            try {
                goldenFilePath = getGoldenFile().getPath().replace("build/", "");
                File gFile = new File(goldenFilePath);
                gFile.createNewFile();
                FileWriter writer = new FileWriter(gFile);
                writer.write(result + "\n");
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                NbTestCase.fail("IO EXCEPTION");
            }
        } else {
            ref(result);
        }
    }
}
