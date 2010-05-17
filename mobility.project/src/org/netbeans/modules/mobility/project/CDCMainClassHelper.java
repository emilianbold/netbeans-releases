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
 * CDCMainClassHelper.java
 *
 * Created on 01 June 2007, 11:56
 *
 */

package org.netbeans.modules.mobility.project;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Lukas Waldmann
 */
public class CDCMainClassHelper implements AntProjectListener, FileChangeListener 
{    
    final private AntProjectHelper helper;
    private String mainClass;
    private FileObject lastMain=null;
    
    /** Creates a new instance of CDCMainClassHelper */
    public CDCMainClassHelper(AntProjectHelper helper)
    {
        this.helper = helper;
        EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);        
        mainClass=ep.getProperty("main.class");
        if (mainClass != null)
            setUp(mainClass);
        helper.addAntProjectListener(this);        
    }

    public void configurationXmlChanged(AntProjectEvent ev)
    {
    }

    public void propertiesChanged(AntProjectEvent ev)
    {
       String newMC=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty("main.class");
       if (newMC!=null && !newMC.equals(mainClass))
       {
           setUp(newMC);
           mainClass=newMC;
       }
    }
    
    public void fileFolderCreated(FileEvent fe)
    {
    }

    public void fileDataCreated(FileEvent fe)
    {
    }

    public void fileChanged(FileEvent fe)
    {
    }

    public void fileDeleted(FileEvent fe)
    {
    }

    public void fileRenamed(FileRenameEvent fe)
    {
        
        FileObject o=(FileObject)fe.getSource();
        mainClass=mainClass.substring(0,mainClass.lastIndexOf('.')+1)+o.getName();
        EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("main.class",mainClass);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        // And save the project
        try {
            Project project=ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            ProjectManager.getDefault().saveProject(project);
        }
        catch ( IOException ex ) {
            ErrorManager.getDefault().notify( ex );
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe)
    {
    }

    
    synchronized private void setUp(final String str)
    {
        
        ProjectManager.mutex().postWriteRequest(new Runnable() 
        {
            public void run()
            {
                final FileObject root = helper.getProjectDirectory().getFileObject(helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(DefaultPropertiesDescriptor.SRC_DIR));
                if (root != null)
                {
                    final ClassPath path1 = ClassPath.getClassPath (root, ClassPath.SOURCE);        
                    final ClassPath path2 = ClassPath.getClassPath (root, ClassPath.COMPILE);
                    final ClassPath path  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { path1, path2 } );
                    FileObject o=path.findResource(str.replace('.','/')+".java");
                    if (lastMain != null)
                        lastMain.removeFileChangeListener(CDCMainClassHelper.this);
                    if (o!=null)
                    {
                        o.addFileChangeListener(CDCMainClassHelper.this);                    
                    }
                    lastMain=o;
                }
            }
        });
    }
}
