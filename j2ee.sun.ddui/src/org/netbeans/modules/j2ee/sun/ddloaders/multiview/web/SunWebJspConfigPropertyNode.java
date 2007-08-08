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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import java.util.ArrayList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.AttributeEntry;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.InnerTablePanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.ParentManagedDDBeanTableModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.TableEntry;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables.ValueEntry;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * @author pfiala
 * @auther Peter Williams
 */
public class SunWebJspConfigPropertyNode extends BaseSectionNode {

    public SunWebJspConfigPropertyNode(SectionNodeView sectionNodeView, SunWebApp sunWebApp, final ASDDVersion version) {
        super(sectionNodeView, sunWebApp, version, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, "HEADING_JspConfigProperties"),
                ICON_BASE_MISC_NODE);
    }

    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        ArrayList<TableEntry> tableColumns = 
                new ArrayList<TableEntry>(3);
        tableColumns.add(new AttributeEntry(
                WebProperty.NAME, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Name"), 150, true)); // NOI18N
        tableColumns.add(new AttributeEntry(
                WebProperty.VALUE, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Value"), 150, true)); // NOI18N
        tableColumns.add(new ValueEntry(
                WebProperty.DESCRIPTION, NbBundle.getMessage(SunWebJspConfigPropertyNode.class, 
                "LBL_Description"), 300)); // NOI18N		
        
        SunWebApp swa = (SunWebApp) key;
        SectionNodeView sectionNodeView = getSectionNodeView();
        return new InnerTablePanel(sectionNodeView, new ParentManagedDDBeanTableModel(
                sectionNodeView.getModelSynchronizer(), 
                swa.getJspConfig(), JspConfig.PROPERTY, tableColumns,
                null, new JspConfigPropertyFactory()), version);
    }
    

    private static class JspConfigPropertyFactory implements ParentManagedDDBeanTableModel.ParentPropertyFactory {
        public CommonDDBean newInstance(CommonDDBean parent) {
            return ((JspConfig) parent).newWebProperty();
        }
    } 
}
