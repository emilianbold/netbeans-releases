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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.layout;

import java.awt.Dimension;
import javax.swing.JComponent;

/** A JSplitterSpace is a completely plain lightweight AWT Component.  Its
	only purpose in life is to hold a space that can act as a handle for
	a JSplitterBar.
 * <p>Use this code at your own risk!  MageLang Institute is not
 * responsible for any damage caused directly or indirctly through
 * use of this code.
 * <p><p>
 * <b>SOFTWARE RIGHTS</b>
 * <p>
 * MageLang support classes, version 1.0, MageLang Institute
 * <p>
 * We reserve no legal rights to this code--it is fully in the
 * public domain. An individual or company may do whatever
 * they wish with source code distributed with it, including
 * including the incorporation of it into commerical software.
 *
 * <p>However, this code cannot be sold as a standalone product.
 * <p>
 * We encourage users to develop software with this code. However,
 * we do ask that credit is given to us for developing it
 * By "credit", we mean that if you use these components or
 * incorporate any source code into one of your programs
 * (commercial product, research project, or otherwise) that
 * you acknowledge this fact somewhere in the documentation,
 * research report, etc... If you like these components and have
 * developed a nice tool with the output, please mention that
 * you developed it using these components. In addition, we ask that
 * the headers remain intact in our source code. As long as these
 * guidelines are kept, we expect to continue enhancing this
 * system and expect to make other tools available as they are
 * completed.
 * <p>
 * The MageLang Support Classes Gang:
 * @version MageLang Support Classes 1.0, MageLang Insitute, 1997
 * @author <a href="http:www.scruz.net/~thetick">Scott Stanchfield</a>, <a href=http://www.MageLang.com>MageLang Institute</a>
	@see JSplitterBar
*/
public class JSplitterSpace extends JComponent {

    static final long serialVersionUID = 1L;

    public synchronized Dimension getMinimumSize() {
        return new Dimension(10, 10);
    }

    public synchronized Dimension getPreferredSize() {
        return new Dimension(10, 10);
    }
}
