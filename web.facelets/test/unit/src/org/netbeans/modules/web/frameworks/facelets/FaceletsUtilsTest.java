/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.frameworks.facelets;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsUtilsTest extends NbTestCase {

    public FaceletsUtilsTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }


    /**
     * Test of getRelativePath method, of class FaceletsUtils.
     */
    public void testGetRelativePath() throws Exception {
        System.out.println("getRelativePath");
        System.out.println("datadir: " + getDataDir());
        File file = new File (getDataDir(), "/testproject1/web/template.xhtml");
        FileObject fromFO = FileUtil.toFileObject(file);
        file = new File (getDataDir(), "/testproject1/web/css/default.css");
        FileObject toFO =  FileUtil.toFileObject(file);
        assertEquals("./css/default.css",  FaceletsUtils.getRelativePath(fromFO, toFO));
        
        file = new File (getDataDir(), "/testproject1/web/testFolder/template.xhtml");
        fromFO = FileUtil.toFileObject(file);
        assertEquals("./../css/default.css", FaceletsUtils.getRelativePath(fromFO, toFO)); ;

    }
    
}