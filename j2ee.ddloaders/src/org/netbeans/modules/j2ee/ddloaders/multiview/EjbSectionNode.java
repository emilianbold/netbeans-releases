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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;

/**
 * @author pfiala
 */
public class EjbSectionNode extends SectionNode {

    public EjbSectionNode(SectionNodeView sectionNodeView, boolean isLeaf, Object key, String title, String iconBase) {
        super(sectionNodeView, isLeaf ? Children.LEAF : new Children.Array(), key, title, iconBase);
    }

    public EjbSectionNode(SectionNodeView sectionNodeView, Object key, String title, String iconBase) {
        this(sectionNodeView, false, key, title, iconBase);
    }
}
