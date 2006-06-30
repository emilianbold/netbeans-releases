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

package org.netbeans.modules.projectimport.j2seimport;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Radek Matous
 */
public class ImportProcessTest extends NbTestCase {
    private ImportProcess iproc;
    static {
        System.setProperty("projectimport.logging.level", "FINE");
    }

    public ImportProcessTest(String testName) {
        super(testName);
    }


    protected void setUp() throws Exception {
        clearWorkDir();
        iproc = ImportUtils.createImportProcess(FileUtil.toFileObject(getWorkDir()),createProjectDefinitions(),true);
    }
    
    
    
    /**
     * Test of getNumberOfSteps method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.ImportProcess.
     */
    public void testJustLetItRunAndWatchLoggerOutputs() {        
        assertNotNull(iproc.getWarnings());        
        assertEquals(iproc.getCurrentStep(),-1);
        assertFalse(iproc.isFinished());
        
        
        iproc.startImport(false);
        
        assertEquals(iproc.getCurrentStep(),iproc.getNumberOfSteps());        
        assertTrue(iproc.isFinished());
        assertNotNull(iproc.getWarnings());                        
    }
    
    
    private Collection createProjectDefinitions() throws Exception {
        AbstractProject p = new AbstractProject("A", FileUtil.toFileObject(getWorkDir()));
        
        p.addSourceRoot(
                new AbstractProject.SourceRoot("src", new File(getWorkDir(), "src")));
        p.addLibrary(
                new AbstractProject.Library(AbstractProjectDefinitionTest.createArchivFile(getWorkDir(), "a.jar")));
        
        p.setJDKDirectory(getWorkDir());
        
        AbstractProject s = new AbstractProject("B", FileUtil.toFileObject(getWorkDir()));
        p.addDependency(s);
        
        Collection ret = new HashSet();
        ret.add(p);
        ret.add(s);
        return ret;
    }
    
}
