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

/*
 * MEKeyToolTest.java
 * JUnit based test
 *
 * Created on 22 February 2006, 17:03
 */
package org.netbeans.modules.mobility.project.security;

import junit.framework.*;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import java.io.*;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.modules.mobility.project.security.MEKeyTool.KeyDetail;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Lukas Waldmann
 */
public class MEKeyToolTest extends NbTestCase {
    static final J2MEPlatform.Device devices[];
    static final J2MEPlatform plat;
    static final String platBase;
    
    
    static
    {
        TestUtil.setLookup( new Object[] {MEKeyToolTest.class
        }, MEKeyToolTest.class.getClassLoader());
        
        devices=new J2MEPlatform.Device[] {
            new J2MEPlatform.Device("d1","d2",null,new J2MEPlatform.J2MEProfile[0] ,null)
        };
        String base=Manager.getWorkDirPath()+"/emulator";
        File f=new File(base);
        File fa[]=f.listFiles();
        assertTrue(fa.length==1);
        platBase=FileUtil.normalizeFile(fa[0]).getAbsolutePath();
        plat=new J2MEPlatform("n1",platBase,"t1","d1",null,null,null,null,null,devices);
    }
    
    
    public MEKeyToolTest(String testName) {
        super(testName);
        TestUtil.setEnv();
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MEKeyToolTest.class);
        
        return suite;
    }
    
    /**
     * Test of getMEKeyToolPath method, of class org.netbeans.modules.mobility.project.security.MEKeyTool.
     */
    public void testGetMEKeyToolPath() throws IOException {
        System.out.println("getMEKeyToolPath");
        //just for coverage
        new MEKeyTool();
        ///////////////////
        assertNull(MEKeyTool.getMEKeyToolPath(null));
        assertNull(MEKeyTool.getMEKeyToolPath(new J2MEPlatform("n1","fakepath","t1","d1",null,null,null,null,null,devices)));
        
        String result = MEKeyTool.getMEKeyToolPath(plat);
        assertNotNull(result);
        assertTrue(result.indexOf(platBase+File.separator+"bin"+File.separator+"mekeytool")!=-1);
    }
    
    /**
     * Test of listKeys method, of class org.netbeans.modules.mobility.project.security.MEKeyTool.
     */
    public void testListKeys() {
        final int ORDER=1;
        System.out.println("listKeys");
        
        assertNull(MEKeyTool.listKeys(null));
        MEKeyTool.KeyDetail[] result = MEKeyTool.listKeys(plat);
        assertNotNull(result);
        assertTrue(result.length>1);
        int i=result[ORDER].getOrder();
        assertTrue(i==ORDER+1);
        String owner=result[ORDER].getOwner();
        assertTrue(owner.indexOf("Sun Microsystems Inc TEST")!=-1);
        String info[]=result[ORDER].getInfo();
        int lines=info.length;
        result[ORDER].addLine("New Line");
        info=result[ORDER].getInfo();
        assertTrue(lines+1==info.length);
        assertEquals(info[info.length-1],"New Line");
        
        KeyDetail detail=new KeyDetail(1);
        assertNull(detail.getOwner());
    }
}
