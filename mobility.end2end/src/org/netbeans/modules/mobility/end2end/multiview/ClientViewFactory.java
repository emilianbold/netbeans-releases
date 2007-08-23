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

/*
 * ClientViewFactory.java
 *
 * Created on July 22, 2005, 2:54 PM
 *
 */
package org.netbeans.modules.mobility.end2end.multiview;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 *
 * @author Michal Skvor
 */
public class ClientViewFactory implements InnerPanelFactory {
    
    static final String PROP_PANEL_SERVICES = "services"; //NOI18N
    static final String PROP_PANEL_CLIENT_GENERAL = "clientGeneralInfo"; //NOI18N
    static final String PROP_PANEL_CLIENT_OPTIONS = "clientOptions"; //NOI18N
    
    final private E2EDataObject dataObject;
    ToolBarDesignEditor editor;
    
    private ServicesPanel servicesPanel;
    private ClientGeneralInfoPanel clientGeneralInfoPanel;
    private ClientOptionsPanel clientOptionsPanel;
    
    /** Creates a new instance of ClientViewFactory */
    public ClientViewFactory( ToolBarDesignEditor editor, E2EDataObject dataObject ) {
        this.editor = editor;
        this.dataObject = dataObject;
    }
    
    public SectionInnerPanel createInnerPanel( final Object key ) {
        final String keyName = (String)key;
        if (PROP_PANEL_SERVICES.equals(keyName)) {
            if (servicesPanel == null) servicesPanel = new ServicesPanel((SectionView)editor.getContentView(), dataObject);
            return servicesPanel;
        } else if(PROP_PANEL_CLIENT_GENERAL.equals(keyName)) {
            if (clientGeneralInfoPanel == null ) clientGeneralInfoPanel = new ClientGeneralInfoPanel((SectionView)editor.getContentView(), dataObject );
            return clientGeneralInfoPanel;
        } else if( PROP_PANEL_CLIENT_OPTIONS.equals(keyName)) {
            if (clientOptionsPanel == null) clientOptionsPanel = new ClientOptionsPanel((SectionView)editor.getContentView(), dataObject );
            return clientOptionsPanel;
        }
        return null;
    }
}
