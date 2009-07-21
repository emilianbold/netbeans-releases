/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
    final J2MEPlatform.Device devices[];    
    final String platBase;
    final J2MEPlatform plat;
        
    static
    {
        TestUtil.setLookup( new Object[] {MEKeyToolTest.class
        }, MEKeyToolTest.class.getClassLoader());
    }
    
    
    public MEKeyToolTest(String testName) {
        super(testName);
        TestUtil.setEnv();
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
        assertNull(MEKeyTool.getMEKeyToolPath((Object)null));
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
        
        assertNull(MEKeyTool.listKeys((Object)null));
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
