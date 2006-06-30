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
