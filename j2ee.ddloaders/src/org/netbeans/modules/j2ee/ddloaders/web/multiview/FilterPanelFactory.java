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
import org.netbeans.modules.j2ee.dd.api.web.Filter;

/**
 * @author mkuchtiak
 */
public class FilterPanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private DDDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    FilterPanelFactory(ToolBarDesignEditor editor, DDDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if ("filter_mappings".equals(key)) return new FilterMappingsPanel((SectionView)editor.getContentView(), dObj); //NOI18N
        return new FilterPanel((SectionView)editor.getContentView(), dObj, (Filter)key);
    }
}
