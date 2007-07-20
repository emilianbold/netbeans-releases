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

package org.netbeans.modules.uml.core;

import java.util.prefs.Preferences;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogUtilities;
import org.openide.util.NbPreferences;

/**
 * Storage for some important incrementers and default values
 * @author Craig Conover, craig.conover@sun.com
 */
public class UMLSettings
{
    private static final String NEW_DIAGRAM_COUNT = "newDiagramCount"; //NOI18N
    
    public static UMLSettings getDefault()
    {
        return new UMLSettings();
    }
    
    private static Preferences prefs()
    {
        return NbPreferences.forModule(UMLSettings.class);
    }
    
    public int getNewDiagramCount()
    {
        return prefs().getInt(NEW_DIAGRAM_COUNT, 1);
    }
    
    public void setNewDiagramCount(int count)
    {
        prefs().putInt(NEW_DIAGRAM_COUNT, count);
    }
    
    public void incrementDiagramCount(String diagramName, int diagramKind)
    {
        String defaultName = NewDialogUtilities.getDefaultDiagramName(diagramKind);
        if (diagramName.equals(defaultName))
        {
            UMLSettings settings = UMLSettings.getDefault();
            settings.setNewDiagramCount(settings.getNewDiagramCount()+1);
        }
    }
}
