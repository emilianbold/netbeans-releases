/*
 * ProjectPropertyProvider.java
 *
 * Created on December 17, 2004, 2:34 PM
 */

package org.netbeans.modules.j2ee.earproject;

import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;

/**
 *
 * @author  vkraemer
 */
public interface ProjectPropertyProvider {
    
    public ArchiveProjectProperties getProjectProperties(); 
}
