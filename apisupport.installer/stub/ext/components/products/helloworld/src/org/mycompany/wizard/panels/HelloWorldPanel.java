package org.mycompany.wizard.panels;

import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;

/**
 *
 * @author Dmitry Lipin
 */
public class HelloWorldPanel extends DestinationPanel {
    
    
    public HelloWorldPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);       
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.description"); // NOI18N
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.destination.button.text"); // NOI18N  
}
