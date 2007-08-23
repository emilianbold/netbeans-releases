/*
 * ProjectLookupProvider.java
 *
 * Created on 23 August 2007, 11:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.spi.mobility.project;

import java.util.List;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author Lukas Waldmann
 */
public interface ProjectLookupProvider
{
    public List createLookupElements(J2MEProject project , AntProjectHelper helper, ReferenceHelper refHelper, ProjectConfigurationsHelper profHelper);
}
