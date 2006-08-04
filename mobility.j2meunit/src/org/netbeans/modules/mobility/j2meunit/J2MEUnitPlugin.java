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
 * J2MEUnitPlugin.java
 *
 * Created on April 19, 2006, 2:35 PM
 *
 */
package org.netbeans.modules.mobility.j2meunit;

import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bohemius
 */
public class J2MEUnitPlugin extends JUnitPlugin {
    
    /** Creates a new instance of J2MEUnitPlugin */
    public J2MEUnitPlugin() {
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
        Project p=FileOwnerQuery.getOwner(targetRoot);
        ProjectClassPathExtender pcpe=p.getLookup().lookup(ProjectClassPathExtender.class);
        if (pcpe!=null) {
            Library lib=LibraryManager.getDefault().getLibrary("J2MEUnit");
            try {
            pcpe.addLibrary(lib);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        /*System.out.println("Preparing to generate test");
        System.out.println("Number of files to test:"+filesToTest.length);
        System.out.println("Target root:"+targetRoot.getPath());
        System.out.println("parameters: "+params);*/
        TestCreator generator=new TestCreator(params,targetRoot);
        FileObject[] result=generator.generateTests(filesToTest);
        generator=null;
        return result;
    }
}
