/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.folding;

import org.netbeans.editor.CodeFoldingSideBar;
import org.netbeans.editor.SideBarFactory;

/**
 *  HTML Code Folding Side Bar Factory, responsible for creating CodeFoldingSideBar
 *  Plugged via layer.xml
 *
 *  @author  Martin Roskanin, Marek Fukala
 */
public class JspCodeFoldingSideBarFactory implements SideBarFactory{
    
    public JspCodeFoldingSideBarFactory() {
    }
    
    public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent target) {
        return new CodeFoldingSideBar(target);
    }

}
