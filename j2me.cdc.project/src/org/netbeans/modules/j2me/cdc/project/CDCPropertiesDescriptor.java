/*
 * CDCPropertiesDescriptor.java
 *
 */
package org.netbeans.modules.j2me.cdc.project;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;

/**
 *
 * @author Adam
 */
public class CDCPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    /** Creates a new instance of CDCPropertiesDescriptor */
    public CDCPropertiesDescriptor() {
    }
    
    public Set<PropertyDescriptor> getPropertyDescriptors() {
        return Collections.EMPTY_SET;
    }

}
