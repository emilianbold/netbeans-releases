/*
 * JavonProfileProvider.java
 *
 * Created on March 5, 2007, 3:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.javon;

import org.netbeans.modules.mobility.javon.JavonSerializer;
import java.util.List;
import org.netbeans.modules.mobility.e2e.mapping.JavonMappingImpl;

/**
 *
 * @author Michal Skvor
 */
public interface JavonProfileProvider {
    
    /**
     * Return name of the profile
     * 
     * @return name of the profile
     */
    public String getName();
    
    /**
     * Return human readable name for the JavonProfileProvider
     * 
     * @return displayable name
     */
    public String getDisplayName();
    
    /**
     * Return all provided templates
     * 
     * @param mapping Javon mapping
     * @return {@link #java.util.List} of JavonTemplates
     */
    public List<JavonTemplate> getTemplates( JavonMappingImpl mapping );
    
    /**
     * Return all supported serializers
     * 
     * @return list of JavonSerializers
     */
    public List<JavonSerializer> getSerializers();
}
