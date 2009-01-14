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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.ui;

import java.awt.Component;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 * Wizard Panel that allows the user to select the Page Template
 * @author Winston Prakash
 */
public class PageLayoutChooserPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private PageLayoutChooserPanelGUI component;
    Vector<PageLayoutData> pageLayoutList = new Vector<PageLayoutData>();
    PageLayoutData selectedPageLayoutData;

    /** Creates a new instance of TemplatePanel */
    public PageLayoutChooserPanel() {
        component = null;
        String pageLayoutsFolderName = "Templates/PageLayoutTemplates"; // NOI18N
        FileObject pageLayoutsFolder = FileUtil.getConfigFile(pageLayoutsFolderName);
        if (pageLayoutsFolder != null) {
            FileObject[] pageLayouts = pageLayoutsFolder.getChildren();

            for (FileObject template : pageLayouts) {
                PageLayoutData templateData = new PageLayoutData(template);
                if (templateData.isPageLayoutTemplate()) {
                    pageLayoutList.add(new PageLayoutData(template));
                }
            }
            pageLayoutList = sortPosition(pageLayoutList);
        }
    }

    void setSelectedPageLayout(PageLayoutData selPageLayoutdata) {
        selectedPageLayoutData = selPageLayoutdata;
    }
    
    PageLayoutData getSelectedPageLayout() {
        return selectedPageLayoutData;
    }

    // Not a very effective algorithm for sorting. Ok for small array
    private Vector sortPosition(Vector<PageLayoutData> pageLayoutList) {
        Vector<PageLayoutData> sortedList = new Vector<PageLayoutData>();
        for (PageLayoutData template : pageLayoutList) {
            if (sortedList.isEmpty()) {
                sortedList.add(template);
            } else {
                boolean inserted = false;
                for (PageLayoutData template1 : sortedList) {
                    if (template.getPosition() < template1.getPosition()) {
                        sortedList.insertElementAt(template, sortedList.indexOf(template1));
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    sortedList.add(template);
                }
            }
        }
        return sortedList;
    }

    public boolean isPageLayoutsAvailable() {
        return !pageLayoutList.isEmpty();
    }
    
    Vector<PageLayoutData> getPageLayoutList(){
        return pageLayoutList;
    }

    public Component getComponent() {
        if (component == null) {
            component = new PageLayoutChooserPanelGUI(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PageLayoutChooserPanel.class);
    }

    public void readSettings(Object settings) {
 
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void storeSettings(Object settings) {
    }

    public boolean isValid() {
        return true;
    }

    public void addChangeListener(ChangeListener l) {
         
    }

    public void removeChangeListener(ChangeListener l) {
         
    }
}