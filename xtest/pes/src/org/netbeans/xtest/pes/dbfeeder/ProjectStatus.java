/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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
