/*
 * SEMCPropertiesDescriptor.java
 *
 * Created on 29. leden 2007, 15:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2me.cdc.project.semc;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;

/**
 *
 * @author Adam
 */
public class SEMCPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    /** Creates a new instance of SEMCPropertiesDescriptor */
    public SEMCPropertiesDescriptor() {
    }
    
    public Set<PropertyDescriptor> getPropertyDescriptors() {
        return Collections.EMPTY_SET;
    }

}
