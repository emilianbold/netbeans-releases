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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.configstringframework.AllConfigStringFrameworkTests;
import org.netbeans.modules.uml.core.coreapplication.AllCoreApplicationTests;
import org.netbeans.modules.uml.core.generativeframework.AllGenerativeFrameworkTests;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.AllBasicActionsTests;
import org.netbeans.modules.uml.core.metamodel.behavior.AllBehaviorTests;
import org.netbeans.modules.uml.core.metamodel.common.commonactions.AllCommonActionsTests;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.AllCommonActivitiesTests;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.AllCommonStateMachineTests;
import org.netbeans.modules.uml.core.metamodel.core.constructs.AllConstructsTests;
import org.netbeans.modules.uml.core.metamodel.dynamics.AllDynamicsTests;
import org.netbeans.modules.uml.core.metamodel.infrastructure.AllInfrastructureTests;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AllCoreInfraTests;
import org.netbeans.modules.uml.core.metamodel.modelanalysis.AllModelAnalysisTests;
import org.netbeans.modules.uml.core.metamodel.profiles.AllProfilesTests;
import org.netbeans.modules.uml.core.metamodel.structure.AllStructureTests;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AllUMLParserTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.reframework.AllREFrameworkTests;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.AllParsingFrameworkTests;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.AllREIntegrationTests;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.AllJavaRPComponentTests;
import org.netbeans.modules.uml.core.roundtripframework.roundtripevents.AllRoundtripEventsTests;
import org.netbeans.modules.uml.core.support.umlmessagingcore.AllMessagingCoreTests;
import org.netbeans.modules.uml.core.typemanagement.TypeManagerTestCase;
import org.netbeans.modules.uml.core.eventframework.EventFrameworkTests;
import org.netbeans.modules.uml.UMLCoreModule;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class ModuleUnitTestSuiteBuilder
{
    public static Test suite()
    {
        File deleteDir = createTempDotUmlDir();
        TestSuite suite = new TestSuite("UML Core Module Unit Tests"); // NOI18N
        
        //$JUnit-BEGIN$
        suite.addTest(AllJavaRPComponentTests.suite());
        suite.addTest(AllREFrameworkTests.suite());
        suite.addTest(AllGenerativeFrameworkTests.suite());
        suite.addTest(AllConfigStringFrameworkTests.suite());
        suite.addTest(AllParsingFrameworkTests.suite());
        suite.addTest(AllBehaviorTests.suite());
        suite.addTest(AllDynamicsTests.suite());
        suite.addTest(new TestSuite(TypeManagerTestCase.class));
        suite.addTest(new TestSuite(QueryManagerTestCase.class));
        suite.addTest(new TestSuite(WorkspaceTestCase.class));
        suite.addTest(AllStructureTests.suite());
        suite.addTest(AllInfrastructureTests.suite());
        suite.addTest(AllConstructsTests.suite());
        suite.addTest(AllCommonActionsTests.suite());
        suite.addTest(AllCommonActivitiesTests.suite());
        suite.addTest(AllCoreInfraTests.suite());
        suite.addTest(AllCommonStateMachineTests.suite());
        suite.addTest(AllBasicActionsTests.suite());
        suite.addTest(AllProfilesTests.suite());
        suite.addTest(AllModelAnalysisTests.suite());
        suite.addTest(AllREIntegrationTests.suite());
        suite.addTest(AllMessagingCoreTests.suite());
        suite.addTest(AllUMLParserTestSuite.suite());
        suite.addTest(EventFrameworkTests.suite());
		suite.addTest(AllRoundtripEventsTests.suite());
	
        // This needs to be last, since these test cases destroy the
        // CoreProductManager.
        suite.addTest(AllCoreApplicationTests.suite());
        
        //$JUnit-END$
        return suite;
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }
    
    private static File createTempDotUmlDir()
    {
        File tempDotUmlDir = null;
        
        try
        {
            File javaTempDir = new File(
                System.getProperty("java.io.tmpdir")); // NOI18N
            
            File temp = new File(javaTempDir, "dotuml.zip"); // NOI18N
            
            tempDotUmlDirName =
                javaTempDir + File.separator + ".uml"; // NOI18N
            
            // check to see if a temp .uml dir already exists
            tempDotUmlDir = new File(tempDotUmlDirName);
            
            // if it already exists, it could be old and corrupted, so delete
            // to be on the safe side
            if (tempDotUmlDir.exists())
            {
                recursiveDelete(tempDotUmlDir);
            }
            
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            
            ClassLoader loader = UMLCoreModule.class.getClassLoader();
            InputStream in = null;
            
            if (loader!=null)
            {
                in = loader.getResourceAsStream(
                    "org/netbeans/modules/uml/dotuml.zip"); // NOI18N
            }
            
            else
            {
                return null;
            }
            
            if (in == null)
            {
                return null;
            }
            
            byte[] b = new byte[BUFFER];
            int c=0;
            
            FileOutputStream out = new FileOutputStream(temp);
            
            while ((c=in.read(b,0,BUFFER))!=-1)
            {
                out.write(b,0,c);
            }
            
            out.flush();
            out.close();
            
            ZipFile zipfile = new ZipFile(temp); //NO I18n
            Enumeration e = zipfile.entries();
            
            while(e.hasMoreElements())
            {
                entry = (ZipEntry) e.nextElement();
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                
                if (entry.isDirectory())
                {
                    File f1 = new File(javaTempDir +
                        File.separator + entry.getName());
                    
                    f1.mkdirs();
                }
                
                else
                {
                    FileOutputStream fos = new FileOutputStream(
                        javaTempDir + File.separator + entry.getName());
                    
                    dest = new BufferedOutputStream(fos, BUFFER);
                    
                    while ((count = is.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, count);
                    }
                    
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            
            temp.deleteOnExit();
            javaTempDir.deleteOnExit();
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return tempDotUmlDir;
    }
    
    private static void recursiveDelete(File target)
    {
        if (target.exists() == true)
        {
            if (target.isDirectory() == true)
            {
                File[] children = target.listFiles();
                
                for (int index = 0; index < children.length; index++)
                {
                    recursiveDelete(children[index]);
                }
            }
            
            target.delete();
        }
    }
    
    public static String tempDotUmlDirName = "";
    static final int BUFFER = 2048;
}
