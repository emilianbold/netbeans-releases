/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SideBarFactory;



/**
 *
 * @author Jan Lahoda
 */
public class AnnotationViewFactory implements SideBarFactory {
    
    /** Creates a new instance of AnnotationViewFactory */
    public AnnotationViewFactory() {
    }

    public JComponent createSideBar(JTextComponent target) {
        return new AnnotationView(target);
    }
    
}
