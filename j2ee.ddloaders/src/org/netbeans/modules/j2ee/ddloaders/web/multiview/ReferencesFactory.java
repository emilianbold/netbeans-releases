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
