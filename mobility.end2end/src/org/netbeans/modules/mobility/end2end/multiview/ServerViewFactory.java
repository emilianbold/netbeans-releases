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
 * ServerViewFactory.java
 *
 * Created on July 22, 2005, 4:02 PM
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
public class ServerViewFactory implements InnerPanelFactory {
    
    final private E2EDataObject dataObject;
    ToolBarDesignEditor editor;
    
    private ServerGeneralInfoPanel serverGeneralInfoPanel;
    
    /** Creates a new instance of ServerViewFactory */
    public ServerViewFactory( ToolBarDesignEditor editor, E2EDataObject dataObject ) {
        this.editor = editor;
        this.dataObject = dataObject;
    }
    
    public SectionInnerPanel createInnerPanel( @SuppressWarnings("unused")
	final Object key ) {
        // FIXME: devel hack
        if( serverGeneralInfoPanel == null )
            serverGeneralInfoPanel = new ServerGeneralInfoPanel((SectionView)editor.getContentView(), dataObject );
        return serverGeneralInfoPanel;
    }
}
