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

package org.netbeans.modules.visualweb.spi.designer;


import java.awt.Image;
import javax.swing.Action;
import org.openide.util.Lookup;


/**
 * Interface representing a decoration whitin css box UI tree, which is a visual
 * feedback associated with particular component, typically
 * representing some back end artifact providing data for the component.
 *
 * @author Peter Zavadsky
 */
public interface Decoration {

    /** Gets width of the decoration. */
    public int getWidth();
    /** Gets height of the decoration.  */
    public int getHeight();
    /** Gets image representing the back end artifact. */
    public Image getImage();
    /** Gets actions possible to invoke on the artifact.  */
    public Action[] getActions();
    /** Gets default action to invoke on the artifact. */
    public Action getDefaultAction();
    /** Gets context representing the artifact. */
    public Lookup getContext();
}
