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

package workdir;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;
import java.io.File;


/** Example of workdir usage.
 */
public class UseWorkdir extends NbTestCase {


    public UseWorkdir(String testName) {
        super(testName);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(UseWorkdir.class));
    }

    /** Example of workdir usage. You can get unique workdir for each method
     * and do whatever you want in it.
     * @throws java.io.IOException if some IO operation fails
     */
    public void testPart1() throws java.io.IOException {
        // print workdir path 
        System.out.println("WORKDIR="+getWorkDirPath());
        File workdir = getWorkDir();
        new File(workdir, "testfile.xx").createNewFile();
        new File(workdir, "testfile.xy").createNewFile();
        // clear all the files in the workdir
        clearWorkDir();
        // do something else in the workdir
        new File(workdir, "testfile2.xx").createNewFile();
        new File(workdir, "testfile2.xy").createNewFile();
    }

    /** This test method will have a different workdir then previous method.
     * @throws java.io.IOException if some IO operation fails
     */
    public void testPart2() throws java.io.IOException {
        // print workdir path 
        System.out.println("WORKDIR="+getWorkDirPath());
        File workdir = getWorkDir();
        new File(workdir, "testfile_part2.xx").createNewFile();
    }
}
