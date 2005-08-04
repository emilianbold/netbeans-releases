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
package org.netbeans.modules.xml.multiview.ui;

/**
 * @author pfiala
 */
public abstract class SectionNodeInnerPanel extends SectionInnerPanel {
    /**
     * Constructor that takes the enclosing SectionView object as its argument
     *
     * @param sectionNodeView enclosing SectionView object
     */
    public SectionNodeInnerPanel(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
    }

    protected void signalUIChange() {
        ((SectionNodeView)getSectionView()).getModelSynchronizer().requestUpdateData();
    }

    public void focusData(Object element) {
    }
}
