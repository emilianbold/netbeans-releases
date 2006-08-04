package org.netbeans.spi.mobility.project.ui.customizer;

import java.util.Map;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.*;



public interface CustomizerPanel {
    
    public void initValues(ProjectProperties props, String configuration);
    
}