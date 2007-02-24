/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * ReferencedJavaProjectSupport.java
 *
 * Created on March 14, 2005, 6:40 PM
 */

package org.netbeans.modules.uml.project.ui.common;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mike
 */
public class ReferencedJavaProjectSupport
{
    
    /** Creates a new instance of ReferencedJavaProjectSupport */
    public ReferencedJavaProjectSupport()
    {
    }
    
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private Set /*<String>*/ sourceGroups;
    private String antArtifactPrefix;
    
    /** Creates a new instance of ClassPathSupport */
    public  ReferencedJavaProjectSupport(
        PropertyEvaluator evaluator,
        ReferenceHelper referenceHelper,
        AntProjectHelper antProjectHelper,
        String sourceGroups[],
        String antArtifactPrefix )
    {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        
        this.sourceGroups = sourceGroups == null 
            ? null 
            : new HashSet(Arrays.asList(sourceGroups));
        
        this.antArtifactPrefix = antArtifactPrefix;
    }
    
    
    
    public ReferencedJavaProjectModel createReferencedJeavaProjectModel(
        String property, String propertyValue)
    {
        
        ReferencedJavaProjectModel model = null;
        Project project = null;
        
        if (propertyValue == null || propertyValue.length() == 0)
            return  ReferencedJavaProjectModel.createUnset(property);
        
        String expectedLocation = evaluator.evaluate(propertyValue);
        
        
        // following other examples we would compare the expected location
        // with the actual location
        File projFile =
            antProjectHelper.resolveFile(expectedLocation);
        
        if (projFile == null || !(projFile.exists()))
        {
            // project file not on local system, probably moved
            return ReferencedJavaProjectModel.createBroken(
                property, expectedLocation);
        }
        
        // This will determine if other project is mounted
        FileObject projFo =
            antProjectHelper.resolveFileObject(expectedLocation);
        
        if (projFo == null)
        {
            // project file exists but is not mounted
            return ReferencedJavaProjectModel.createUnmounted(
                property, expectedLocation, projFile);
        }
        
        else
        {
            try
            {
                project = ProjectManager.getDefault().findProject(projFo);
                
                return ReferencedJavaProjectModel.createMounted(
                    property, expectedLocation, projFile, project);
            }
            
            catch(Exception e)
            {
                
                // TODO - FIX THIS - bulletproof
                //  Debug.out.println("MCF - createReferencedJeavaProjectModel : "
                //         + e);
            }
        }
        
        // not sure what to do if we get here
        return ReferencedJavaProjectModel.createBroken(
            property, expectedLocation);
    }
    
    
    // propValue is assumed to be a unparsed string
    public JavaSourceRootsModel createReferencedJavaSourceRootsModel(
        ReferencedJavaProjectModel projModel,
        String propertyValue)
    {
        
        DefaultListModel jsrm = new DefaultListModel();
        
        if (propertyValue != null)
        {
    
            String token = "$";  // NOI18N
            StringTokenizer tokenizer =
                new StringTokenizer(propertyValue, token);
            
            while(tokenizer.hasMoreTokens())
            {
                String nextVal = token.concat((String)tokenizer.nextElement());
                jsrm.addElement(nextVal);
            }
        }
        
        return JavaSourceRootsUI.createModel(projModel, jsrm );
    }
    
    
    public String[] encodeSrcGroupsToStrings(JavaSourceRootsModel model)
    {
        
        ArrayList result = new ArrayList();
        
        for (int i = 0; i <  model.getRowCount(); i++)
        {
            Boolean isChecked = (Boolean)model
                .getValueAt(i, JavaSourceRootsUI.COL_INCLUDE_FLAG);
            
            if (isChecked)
            {
                SourceGroup sg = (SourceGroup)model.getSourceGroup(i);
                result.add(sg.getName());
            }
        }
        
        return (String[]) result.toArray(new String[result.size()]);
    }
}
