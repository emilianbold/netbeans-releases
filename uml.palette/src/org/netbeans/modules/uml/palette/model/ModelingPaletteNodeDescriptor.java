/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * NodeDescriptor.java
 *
 * Created on March 2, 2005, 3:11 PM
 */

package org.netbeans.modules.uml.palette.model;

import java.awt.Image;

import org.openide.util.Utilities;

/**
 *
 * @author Praveen Savur
 */
public interface  ModelingPaletteNodeDescriptor {

    public String getName();

    public String getDisplayName();

    public Image getIcon(int type);

    public  String getButtonID();

    public String getTooltip();

}
