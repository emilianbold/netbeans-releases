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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Tests for FileEncodingQuery
 *
 * @author Tomas Zezula
 */
public class FileEncodingQueryTest extends NbTestCase {        

    public FileEncodingQueryTest(String testName) {
        super(testName);        
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private AntProjectHelper helper;
    private J2SEProject prj;

    protected void setUp() throws Exception {        
        ClassLoader l = this.getClass().getClassLoader();
        TestUtil.setLookup(new ProxyLookup (new Lookup[]{
            Lookups.fixed(l, new DummyXMLEncodingImpl ()),
            Lookups.metaInfServices(l),            
        }));        
        super.setUp();        
        this.clearWorkDir();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        prj = null;
        super.tearDown();
    }


    private void prepareProject () throws IOException {
        File wd = getWorkDir();
        scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null);
        Project p = FileOwnerQuery.getOwner(projdir);
        assertNotNull(p);
        prj = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(prj);
        sources = projdir.getFileObject("src");
    }

    public void testFileEncodingQuery () throws Exception {
        this.prepareProject();
        final Charset UTF8 = Charset.forName("UTF-8");
        final Charset ISO15 = Charset.forName("ISO-8859-15");
        final Charset CP1252 = Charset.forName("CP1252");
        FileObject java = sources.createData("a.java");
        Charset enc = FileEncodingQuery.getEncoding(java);
        assertEquals(UTF8,enc);
        FileObject xml = sources.createData("b.xml");
        enc = FileEncodingQuery.getEncoding(xml);
        assertEquals(ISO15,enc);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("project.encoding", CP1252.name());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        enc = FileEncodingQuery.getEncoding(java);
        assertEquals(CP1252,enc);
        FileObject standAloneJava = scratch.createData("b.java");
        enc = FileEncodingQuery.getEncoding(standAloneJava);
        assertEquals(UTF8,enc);
    }


    public static class DummyXMLEncodingImpl extends FileEncodingQueryImplementation {
               
                            
        public Charset getEncoding(FileObject file) {
            if ("xml".equals(file.getExt())) {
                return Charset.forName("ISO-8859-15");
            }
            else {
                return null;
            }
        }
    }



}
