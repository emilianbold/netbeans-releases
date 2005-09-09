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
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "CVS|Import into Repository" popup or main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class ImportAction extends ActionNoBlock {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Import into Repository"
    private static final String IMPORT_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "BK0006");
    
    /** Creates new ImportAction instance. */
    public ImportAction() {
        super(CVS_ITEM+"|"+IMPORT_ITEM, CVS_ITEM+"|"+IMPORT_ITEM);
    }
}

