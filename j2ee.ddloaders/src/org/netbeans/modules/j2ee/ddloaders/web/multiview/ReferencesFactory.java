/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;

/** ReferencesFactory - factory for creating references' tables
 *
 * @author mkuchtiak
 * Created on April 11, 2005
 */
public class ReferencesFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private DDDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    ReferencesFactory(ToolBarDesignEditor editor, DDDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if ("env_entries".equals(key)) return new EnvEntriesPanel((SectionView)editor.getContentView(), dObj);
        else if ("res_refs".equals(key)) return new ResRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("res_env_refs".equals(key)) return new ResEnvRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("ejb_refs".equals(key)) return new EjbRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("message_dest_refs".equals(key)) return new MessageDestRefsPanel((SectionView)editor.getContentView(), dObj);
        else return null;
    }
}
