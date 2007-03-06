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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.Image;

/**
 * Contract specific for Filesystem <-> UI interaction, to be replaced later with something more
 * sophisticated (hopefuly).
 *
 * <p>It's registered in default lookup (META-INF/services).
 * 
 * @author Maros Sandor
 */
class FileStatusProvider extends VCSAnnotator {

    private boolean shutdown; 

    public String annotateName(String name, VCSContext context) {
        if (shutdown) return null;
        return CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(name, context, FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        if (shutdown) return null;
        return CvsVersioningSystem.getInstance().getAnnotator().annotateIcon(icon, context);
    }

    public Action[] getActions(VCSContext context, VCSAnnotator.ActionDestination destination) {
        return Annotator.getActions(context, destination);
    }
    
    void shutdown() {
        shutdown = true;
    }
}
