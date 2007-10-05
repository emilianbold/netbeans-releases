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

package org.netbeans.xtest.pes.dbfeeder;

import org.netbeans.xtest.xmlserializer.*;

public class ProjectStatus implements XMLSerializable {

	static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(ProjectStatus.class);
    static {
        try {
	        classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
                classMappingRegistry.registerSimpleField("team",ClassMappingRegistry.ATTRIBUTE,"team");
	        //classMappingRegistry.registerSimpleField("buildsAvailable",ClassMappingRegistry.ATTRIBUTE,"buildsAvailable");
                //classMappingRegistry.registerSimpleField("fullDetailsAvailable",ClassMappingRegistry.ATTRIBUTE,"fullDetailsAvailable");
                classMappingRegistry.registerSimpleField("lastBuildAvailable",ClassMappingRegistry.ATTRIBUTE,"lastBuildAvailable");
	        classMappingRegistry.registerSimpleField("lastFullDetailsBuildAvailable",ClassMappingRegistry.ATTRIBUTE,"lastFullDetailsBuildAvailable");

        } catch (MappingException me) {
        	me.printStackTrace();
        	classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }
    
    // empty constructor - required by XMLSerializer
    public ProjectStatus() {}
    
    /*
    public ProjectStatus(String name, int buildsAvailable, int fullDetailsAvailable) {
        this.name=name;
        this.buildsAvailable = buildsAvailable;
        this.fullDetailsAvailable = fullDetailsAvailable;
    }
     **/
    
    public ProjectStatus(String name, String team, String lastBuildAvailable, String lastFullDetailsBuildAvailable) {
        this.name = name;
        this.team = team;
        this.lastBuildAvailable = lastBuildAvailable;
        this.lastFullDetailsBuildAvailable = lastFullDetailsBuildAvailable;
    }
    


    public String getTeam() {
        return team;
    }
    
    
    public String getName() {
        return name;
    }
    
    public int getBuildsAvailable() {
        return buildsAvailable;
    }
    
    public int getFullDetailsAvailable() {
        return fullDetailsAvailable;
    }
    
    /** Getter for property lastBuildAvailable.
     * @return Value of property lastBuildAvailable.
     *
     */
    public java.lang.String getLastBuildAvailable() {
        return lastBuildAvailable;
    }
    
    
    /** Getter for property lastFullDetailsBuildAvailable.
     * @return Value of property lastFullDetailsBuildAvailable.
     *
     */
    public java.lang.String getLastFullDetailsBuildAvailable() {
        return lastFullDetailsBuildAvailable;
    }    
    
    private String name;
    private String team;    
    private int buildsAvailable;
    private int fullDetailsAvailable;
    
    private String lastBuildAvailable;
    private String lastFullDetailsBuildAvailable;
}
