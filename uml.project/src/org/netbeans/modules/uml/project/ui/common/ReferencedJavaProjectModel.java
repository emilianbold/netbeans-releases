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

/*
 * PropertydProjectModel.java
 *
 * Created on March 20, 2005, 3:51 PM
 */

package org.netbeans.modules.uml.project.ui.common;

import java.io.File;
import org.netbeans.api.project.Project;

/**
 *
 * @author Mike
 */
public class ReferencedJavaProjectModel
{

    /** Creates a new instance of PropertydProjectModel */
    public ReferencedJavaProjectModel()
    {
        super();
    }


    // Property to a broken object
    private static final String BROKEN = "BrokenProperty"; // NOI18N

    private Project project;
    // private URI artifactURI;
    // private int type;
    private String property;
    private File projFile;
    private String expectedLocation = ""; // NOI18N
    private ReferenceStatus refStatus = ReferenceStatus.REF_STATUS_UNSET;
    
    public enum ReferenceStatus
    {
        REF_STATUS_BROKEN,
        REF_STATUS_MOUNTED,
        REF_STATUS_UNMOUNTED,
        REF_STATUS_UNSET;
    }
    
    
    
    private ReferencedJavaProjectModel(
        String property,  String expectedLocation,
        File projFile, Project project, ReferenceStatus refStatus)
    {
        this.project = project;
        this.property = property;
        this.projFile = projFile;
        this.expectedLocation = expectedLocation;
        this.refStatus = refStatus;
    }
    
    
    // Factory methods -----------------------------------------------------
    
    
    public static ReferencedJavaProjectModel createMounted(String property,
        Project project )
    {
        if (property == null || project == null)
        {
            throw new IllegalArgumentException(
                "property and project must not be null" ); // NOI18N
        }
        
        return new ReferencedJavaProjectModel(
            property, null, null, project, ReferenceStatus.REF_STATUS_MOUNTED);
    }
    
    
    public static ReferencedJavaProjectModel createUnset(String property )
    {
        if (property == null)
        {
            throw new IllegalArgumentException(
                "property must not be null" ); // NOI18N
        }
        
        return new ReferencedJavaProjectModel(
            property, null, null, null, ReferenceStatus.REF_STATUS_UNSET);
    }
    
    public static ReferencedJavaProjectModel createMounted(
        String property, String expectedLocation,
        File projectFile, Project project )
    {
        if (property == null)
        {
            throw new IllegalArgumentException( 
                "property must not be null" ); // NOI18N
        }
        
        return new ReferencedJavaProjectModel(property, expectedLocation,
            projectFile, project, ReferenceStatus.REF_STATUS_MOUNTED);
    }
    
    public static ReferencedJavaProjectModel createUnmounted(
        String property, String expectedLocation,
        File projectFile)
    {
        if (property == null)
        {
            throw new IllegalArgumentException( 
                "property must not be null" ); // NOI18N
        }
        
        return new ReferencedJavaProjectModel(property, expectedLocation,
            projectFile, null, ReferenceStatus.REF_STATUS_UNMOUNTED);
    }
    
    public static ReferencedJavaProjectModel createBroken(
        String property, String expectedLocation)
    {
        if (property == null)
        {
            throw new IllegalArgumentException( 
                "property must not be null in broken items" ); // NOI18N
        }
        
        return new ReferencedJavaProjectModel(property, expectedLocation,
            null, null, ReferenceStatus.REF_STATUS_BROKEN);
    }
    
    // Instance methods ----------------------------------------------------
    
    public ReferenceStatus getRefStatus()
    {
        return refStatus;
    }
    
    
    public Project getProject()
    {
        return project;
    }
    
    public String getProperty()
    {
        return property;
    }
    
    public String getExpectedLocation()
    {
        return expectedLocation;
    }
    
    public File getProjectFile()
    {
        return projFile;
    }
    
    public boolean isBroken()
    {
        return refStatus == ReferenceStatus.REF_STATUS_BROKEN;
    }
    
    public int hashCode()
    {
        int hash = 0;
        
        if ( isBroken())
            return BROKEN.hashCode();
        
        if( project != null)
            hash += project.hashCode();

        hash += property.hashCode();
        
        return hash;
    }
    
    public boolean equals(Object itemObject)
    {
        if (!(itemObject instanceof ReferencedJavaProjectModel))
            return false;
        
        ReferencedJavaProjectModel item = (ReferencedJavaProjectModel)itemObject;
        
        if (isBroken() != item.isBroken())
            return false;
        
        if (isBroken())
            return getProperty().equals(item.getProperty());
        
        if (!getProperty().equals(item.getProperty()))
            return false;
        
        return getProject() == item.getProject();
    }
}
