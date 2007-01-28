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

package org.netbeans.modules.visualweb.project.jsf.api;

import java.io.File;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;

/**
 * Interface used for items that can be imported into the project.
 * Importable items should be registered in the layer file.
 *
 * @author Tor Norbye
 */
public interface Importable extends ProjectActionPerformer {

    /** Description of the type of object to be imported. This item
     * will be displayed in the Import action's pullright menu. */
    String getDisplayName();

    /** Icon to use along with the display name in the menu. */
    // TODO   public ImageIcon getImage() {
    //        }

    /** XXX API to distinguish importable of page.
     * TODO Create better API. */
    public interface PageImportable {
        // XXX Ugly method, historical impl, revise.
        public JPanel importRandomFile(Project project, File file, String extension, JPanel panel);
    }
}
