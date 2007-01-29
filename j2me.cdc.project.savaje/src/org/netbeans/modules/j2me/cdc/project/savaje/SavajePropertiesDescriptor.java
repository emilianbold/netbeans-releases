/*
 * SavajePropertiesDescriptor.java
 *
 * Created on 29. leden 2007, 15:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2me.cdc.project.savaje;

import java.util.Collections;
import java.util.Set;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;

/**
 *
 * @author Adam
 */
public class SavajePropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    /** Creates a new instance of SavajePropertiesDescriptor */
    public SavajePropertiesDescriptor() {
    }
    
    public Set getPropertyDescriptors() {
        return Collections.EMPTY_SET;
    }

}
