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

package org.netbeans.modules.vmd.api.palette;

import javax.swing.*;
import java.util.List;

/**
 * This interface represents category in the VMD palette.
 *
 * @author Anton Chechel
 */
public interface PaletteProvider {

    /**
     * Initializes palette categories as a directories provided by particular implementation of project type.
     *
     * @param projectType type of project
     */
    void initPaletteCategories(String projectType);

    /**
     * Returns a list of actions that are available in the palette manager
     * @param projectType the project type of the palette
     * @return the list of actions
     */
    List<? extends Action> getActions (String projectType);

}
