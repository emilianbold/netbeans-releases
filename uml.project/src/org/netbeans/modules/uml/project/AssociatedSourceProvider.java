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

package org.netbeans.modules.uml.project;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import java.io.File;
import java.util.ArrayList;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;


public class AssociatedSourceProvider
    implements IAssociatedProjectSourceRoots
{
    
    
    // we may only need the UMLProject.
    // actually what we need are UMLProjectProperties but I cannot cache
    // reference to that.
    private UMLProject umlProj;
    private UMLProjectHelper mHelper;
    private PropertyEvaluator mEval;
    
    public AssociatedSourceProvider(
        UMLProject proj, UMLProjectHelper helper, PropertyEvaluator eval)
    {
        this.umlProj = proj;
        this.mHelper = helper;
        this.mEval = eval;
    }
    
    public boolean hasAssociatedSourceProject()
    {
        UMLProjectProperties props = umlProj.getUMLProjectProperties();
        return props.referencedJavaProjectModel.getRefStatus() ==
            ReferencedJavaProjectModel.ReferenceStatus.REF_STATUS_UNSET;
    }
    
    public Project getAssociatedSourceProject()
    {
        
        if (hasAssociatedSourceProject())
        {
            
            // TODO - is this the right thing to do? Or should we
            // throw exception?
            return null; //NOI18N
        }
        
        UMLProjectProperties props = umlProj.getUMLProjectProperties();
        Project proj = props.referencedJavaProjectModel.getProject();
        
        if(proj == null)
        {
            
            // This mean project ref is probably broken ...
            // TODO - is this the right thing to do? Or should we
            // throw exception?
            return proj; //NOI18N
        }
        
        else
            return proj;
    }
    
    public File[] getCompileDependencies()
    {
        File[] retVal = null;
        
        Project assocProject = getAssociatedSourceProject();
        if(assocProject != null)
        {
            // In the future we need to find all projects that the associated
            // project depends apon.  Then include those source roots.
            //
            // I do not know how to do that right now.
            SourceGroup[] groups = getSourceGroups();
            retVal = new File[groups.length];
            int index = 0;
            for(SourceGroup curObj : groups)
            {
                FileObject fo = curObj.getRootFolder();
                retVal[index] = FileUtil.toFile(fo);
                index++;
            }
        }
        
        return retVal;
    }
    
    
    // TODO - bulletproofing edge cases
    // this should probably throw an exception if the associtated project is
    // "off line" or "broken"
    public SourceGroup[] getSourceGroups()
    {
        if (hasAssociatedSourceProject())
        {
            // TODO - is this the right thing to do? Or should we
            // throw exception?
            return null; //NOI18N
        }
        
        UMLProjectProperties props = umlProj.getUMLProjectProperties();
        
        JavaSourceRootsModel model =
            (JavaSourceRootsModel)props.referencedJavaSourceRootsModel;
        
        if (model != null)
        {
            ArrayList result = new ArrayList();
            
            // String[] items = new String[ m.getRowCount()];
            for (int i = 0; i <  model.getRowCount(); i++)
            {
                Boolean isChecked = (Boolean)model
                    .getValueAt(i, JavaSourceRootsUI.COL_INCLUDE_FLAG);
                
                if (isChecked != null && isChecked)
                {
                    SourceGroup sg = (SourceGroup)model.getSourceGroup(i);
                    result.add(sg);
                }
            }
            
            return (SourceGroup[])
            result.toArray(new SourceGroup[result.size()]);
        }
        
        else
            return new SourceGroup[0];
    }
    
    public String createAbsolutePath(String filename)
    {
        String retVal = "";
        
        if (filename.charAt(0) == '{')
        {
            int index = filename.lastIndexOf('}');
            
            if (index > 0)
            {
                String id = filename.substring(1, index);
                SourceGroup group = getSourceGroupByID(id);
                
                if (group != null)
                {
                    // retVal = group.getDisplayName() + 
                    //    "\\" + filename.substring(index + 1);
                    retVal = getGroupRootPath(group) + filename.substring(index + 1);
                    
                    // NetBeans always has the / slashes instead of using the
                    // platform specific seperator.
                    retVal = retVal.replace("\\", "/"); // NOI18N
                }
            }
        }
        
        return retVal;
    }
    
    public String createRelativePath(String filename)
    {
        String retVal = "";
        
        // NetBeans always has the / slashes instead of using the
        // platform specific seperator.
        String converted = filename.replace("\\", "/"); // NOI18N
        SourceGroup group = getSourceGroupForFile(converted);
        
        if (group != null)
        {
            String groupRoot = getGroupRootPath(group);
            //String displayName = group.getDisplayName();
            String idString = annotateAsIdentifier(group.getName());
            
            
            groupRoot = groupRoot.replace("\\", "/"); // NOI18N
            retVal = converted.replace(groupRoot, idString);
        }
        
        return retVal;
    }
    
    public String getSourceRootId(String filename)
    {
        String retVal = ""; // NOI18N
        
        SourceGroup group = getSourceGroupForFile(filename);
        
        if (group != null)
            retVal = group.getName();
        
        return retVal;
    }
    
    protected String getGroupRootPath(SourceGroup group)
    {
        String retVal = ""; // NOI18N
        
        FileObject fObj = group.getRootFolder();
        
        if (fObj != null)
            retVal = FileUtil.toFile(fObj).getAbsolutePath();
        
        return retVal;
    }
    
    protected SourceGroup getSourceGroupForFile(String filename)
    {
        SourceGroup retVal = null;
        
        if (filename != null && filename.length() > 0)
        {
            SourceGroup[] groups = getSourceGroups();
            if (groups != null)
            {
                for (SourceGroup group : groups)
                {

		    if (group == null) 
			continue;
                    // TODO: cvc - group has a potential for being null
                    FileObject fobj = group.getRootFolder();
                    
//                  if(group.contains(fo) == true)
//                  {
//                     retVal = group;
//                     break;
//                  }
                    
                    try
                    {
                        if (fobj.getFileSystem().findResource(filename)!=null)
                        {
                            retVal = group;
                            break;
                        }
                    }
                    
                    catch (FileStateInvalidException e)
                    {
                        Log.stackTrace(e);
                    }
                }
            }
        }
        
        return retVal;
    }
    
    protected SourceGroup getSourceGroupByID(String id)
    {
        SourceGroup retVal = null;
        
        if (id != null && id.length() > 0)
        {
            SourceGroup[] groups = getSourceGroups();
            
            if (groups != null)
            {
                for (SourceGroup group : groups)
                {
                    if ((group != null) && (id.equals(group.getName()) == true))
                    {
                        retVal = group;
                        break;
                    }
                }
            }
        }
        
        return retVal;
    }
    
    protected String annotateAsIdentifier(String identifier)
    {
        StringBuffer buffer = new StringBuffer("{"); // NOI18N
        buffer.append(identifier);
        buffer.append("}"); // NOI18N
        return buffer.toString();
    }
}
