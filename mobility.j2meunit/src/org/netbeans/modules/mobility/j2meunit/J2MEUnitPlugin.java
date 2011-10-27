/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */


package org.netbeans.modules.mobility.j2meunit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author bohemius
 */

public class J2MEUnitPlugin extends JUnitPlugin {

    private Project p;
    private AntProjectHelper aph;

    /** Creates a new instance of J2MEUnitPlugin */
    public J2MEUnitPlugin(Project p, AntProjectHelper aph) {
        this.p=p;
        this.aph=aph;
    }

    protected JUnitPlugin.Location getTestLocation(JUnitPlugin.Location sourceLocation) {

        FileObject fileObj = sourceLocation.getFileObject();
        ClassPath srcCp;
        
        if (fileObj.isFolder() || (srcCp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null) {
            return null;
        }
        
        String baseResName = srcCp.getResourceName(fileObj, '/', false);
        String testResName = getTestResName(baseResName, fileObj.getExt());
        assert testResName != null;
        
        return getOppositeLocation(sourceLocation,
                                   srcCp,
                                   testResName,
                                   true);

    }

    protected JUnitPlugin.Location getTestedLocation(JUnitPlugin.Location testLocation) {
        FileObject fileObj = testLocation.getFileObject();
        ClassPath srcCp;
        
        if (fileObj.isFolder() || ((srcCp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null)) {
            return null;
        }
        
        String baseResName = srcCp.getResourceName(fileObj, '/', false);
        String srcResName = getSrcResName(baseResName, fileObj.getExt());
        if (srcResName == null) {
            return null;     //if the selectedFO is not a test class (by name)
        }

        return getOppositeLocation(testLocation,
                                   srcCp,
                                   srcResName,
                                   false);
    }
    
    
    /**
     */
    private static String getTestResName(String baseResName, String ext) {
        StringBuilder buf
                = new StringBuilder(baseResName.length() + ext.length() + 10);
        buf.append(baseResName).append("Test");                         //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getSrcResName(String testResName, String ext) {
        if (!testResName.endsWith("Test")) {                            //NOI18N
            return null;
        }
        
        StringBuilder buf
                = new StringBuilder(testResName.length() + ext.length());
        buf.append(testResName.substring(0, testResName.length() - 4));
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    private static Location getOppositeLocation(
                                    final Location sourceLocation,
                                    final ClassPath fileObjCp,
                                    final String oppoResourceName,
                                    final boolean sourceToTest) {
        FileObject fileObj = sourceLocation.getFileObject();
        FileObject fileObjRoot;
        
        if ((fileObjRoot = fileObjCp.findOwnerRoot(fileObj)) == null) {
            return null;
        }
        
        URL[] oppoRootsURLs = sourceToTest
                              ? UnitTestForSourceQuery.findUnitTests(fileObjRoot)
                              : UnitTestForSourceQuery.findSources(fileObjRoot);
        if ((oppoRootsURLs == null) || (oppoRootsURLs.length == 0)) {
            return null;
        }
        
        ClassPath oppoRootsClassPath = ClassPathSupport
                                           .createClassPath(oppoRootsURLs);
        final List<FileObject> oppoFiles = oppoRootsClassPath
                                           .findAllResources(oppoResourceName);
        if (oppoFiles.isEmpty()) {
            return null;
        }
        
        return new Location(oppoFiles.get(0)/*, null*/);
    }
    
    
    public boolean canCreateTests(FileObject... filesToTest)
    {
        TestUtils.TestableTypeFinder finder=new TestUtils.TestableTypeFinder();
        for (FileObject fo : filesToTest)
        {
                JavaSource javaSource = JavaSource.forFileObject(fo);
                if (javaSource != null)
                {
                    try
                    {
                        javaSource.runUserActionTask(finder, true);
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                        return false;
                    }
                    if (!finder.isTestable())
                        return false;
                }
        }
        return true;
    }

    
    protected FileObject[] createTests(FileObject[] filesToTest, FileObject targetRoot,
            Map<CreateTestParam, Object> params) {
        //add J2MEUnit JAR to library if needed
        ProjectClassPathExtender pcpe=(ProjectClassPathExtender) p.getLookup().lookup(ProjectClassPathExtender.class);
        if (pcpe!=null) {
            Library lib=LibraryManager.getDefault().getLibrary("JMUnit4CLDC10");
            try {
                pcpe.addLibrary(lib);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        

        //add TestRunner MIDlet to all configurations
        AntProjectHelper aph=(AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        try {
            TestUtils.addTestRunnerMIDletProperty(p,aph);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final TestCreator generator=new TestCreator(params,targetRoot,this.p, this.aph);
        FileObject[] result=generator.generateTests(filesToTest);
        return result;
    }

    

    public static void main(String args[]) {
        try {
            TestCreator testGenerator=new TestCreator(Collections.EMPTY_MAP,FileUtil.createData(new File("./")),null,null);
            if (args.length>0) {
                FileObject[] files2test=new FileObject[args.length];                
                for (int i=0;i<args.length;i++) {
                    files2test[i]=FileUtil.createData(new File(args[i]));
                }
                FileObject[] testFiles=testGenerator.generateTests(files2test);
            } else {
                System.out.println("Usage: J2MEUnitPlugin FILES...");
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }

}

