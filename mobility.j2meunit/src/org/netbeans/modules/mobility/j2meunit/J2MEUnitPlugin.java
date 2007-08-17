/*

 *                 Sun Public License Notice

 *

 * The contents of this file are subject to the Sun Public License

 * Version 1.0 (the "License"). You may not use this file except in

 * compliance with the License. A copy of the License is available at

 * http://www.sun.com/

 *

 * The Original Code is NetBeans. The Initial Developer of the Original

 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun

 * Microsystems, Inc. All Rights Reserved.

 */



/*

 * J2MEUnitPlugin.java

 *

 * Created on April 19, 2006, 2:35 PM

 *

 */

package org.netbeans.modules.mobility.j2meunit;



import java.io.File;

import java.io.IOException;

import java.util.Collections;

import java.util.Map;

import org.netbeans.api.project.Project;

import org.netbeans.api.project.libraries.Library;

import org.netbeans.api.project.libraries.LibraryManager;

import org.netbeans.modules.junit.plugin.JUnitPlugin;

import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;

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

        return null;

    }

    

    protected JUnitPlugin.Location getTestedLocation(JUnitPlugin.Location testLocation) {

        return null;

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

