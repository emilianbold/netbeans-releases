/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * PreverifyTaskTest.java
 * JUnit based test
 *
 * Created on April 6, 2005, 9:58 AM
 */
package org.netbeans.mobility.antext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.NbTestCase;
import junit.framework.*;

/**
 *
 * @author Lukas Waldmann
 */
public class PreverifyTaskTest extends NbTestCase {
    
    public PreverifyTaskTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(PreverifyTaskTest.class);
        
        return suite;
    }

    /**
     * Test of execute method, of class org.netbeans.mobility.antext.PreverifyTask.
     */
    public void testExtract() throws IOException
    { 
        System.out.println("setExtract");
        final String DEFAULT_PLATFORM_TYPE = "UEI-1.0"; 
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();        
        File dataDir=getDataDir();
        File file   =getGoldenFile( "bin");
        File baseDir =new File(file.getParent());
        File srcDir  =new File(dir,"src");        
        assertTrue(srcDir.mkdir());
        File f=File.createTempFile("cmdline","",srcDir);
        File log=null;
        PrintStream out=new PrintStream(new FileOutputStream(f));
        File em=null;
        File exe=null;
        //Here we go
        try
        {     
            Project p=new Project();
            PreverifyTask instance = new PreverifyTask();            
            instance.setProject(p);
            instance.setClassPath(new Path(p,file.getAbsolutePath()));
            instance.setPlatformHome(baseDir);
            instance.setSrcDir(srcDir);
            instance.setDestDir(dataDir);
            StringBuffer commandLine = new StringBuffer(Bundle.getMessage("CMD_Preverify_" + DEFAULT_PLATFORM_TYPE
                    + "_" + Bundle.getMessage("CFG_Preverify_" + DEFAULT_PLATFORM_TYPE)));            

            /* Prepare right executable */
            String name=System.getProperty("os.name");
            int index=name.indexOf("Windows");            
            em=index!=-1?getGoldenFile("bin/preverify.bin"):getGoldenFile("bin/preverify.sh");
            exe=new File(em.getParent()+"/preverify");
            exe.delete();                
            assertTrue(em.renameTo(exe));
            
            HashMap args = new HashMap ();
            args.put ("platformhome", baseDir); // No I18N
            args.put ("classpath", file.getAbsolutePath()); // No I18N
            args.put ("destdir", dataDir); // No I18N
            args.put ("srcdir", srcDir); // No I18N
            args.put ("/", File.separator); // NO I18N
            String line = EMapFormat.format(commandLine.toString(), args);
            String[] commands = Commandline.translateCommandline (line);

            //Set executable permission as it is lost during ant copy task
            if (name.indexOf("Windows")==-1)
            {   
                Process pr=java.lang.Runtime.getRuntime().exec("chmod +x "+commands[0]);
                try {
                    assertTrue(pr.waitFor()==0);
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            for (int i=0;i<commands.length;i++)
            {
                out.println(commands[i]);
            }         
            instance.execute();            
            String wdir=System.getProperty("user.dir");
            System.err.println(wdir);
            log=new File(wdir+File.separator+"cmdLine.log");
            this.assertFile(log,f);
        }
        finally
        {
            out.close();
            if (log!=null) log.delete();
            if (exe!=null) exe.renameTo(em);
        }
    }
}
