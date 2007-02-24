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
 * File         : DiagramKind.java
 * Version      : 1.0
 * Description  : Enumerates the valid DiagramKind constants.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import javax.swing.ImageIcon;

import org.netbeans.modules.uml.integration.ide.actions.ImageFinder;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;

/**
 *  Enumerates the valid DiagramKind constants and maps between DiagramKind
 * and NewWorkspaceItemKind.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-22  Darshan     Created.
 *
 * @author Darshan
 */
public class DiagramKind {
    public static final int DK_UNKNOWN                  = 0x0000;
    public static final int DK_DIAGRAM                  = 0x0001;
    public static final int DK_ACTIVITY_DIAGRAM         = 0x0002;
    public static final int DK_CLASS_DIAGRAM            = 0x0004;
    public static final int DK_COLLABORATION_DIAGRAM    = 0x0008;
    public static final int DK_COMPONENT_DIAGRAM        = 0x0010;
    public static final int DK_DEPLOYMENT_DIAGRAM       = 0x0020;
    public static final int DK_SEQUENCE_DIAGRAM         = 0x0040;
    public static final int DK_STATE_DIAGRAM            = 0x0080;
    public static final int DK_USECASE_DIAGRAM          = 0x0100;
    public static final int DK_ALL                      = 0xFFFF;


    public static final int DK_CREATE_FROM_SELECTED     = 0x10000;


    public static ImageIcon getDiagramIcon(IDiagram diag) {
        switch (diag.getDiagramKind()) {
        case DK_CLASS_DIAGRAM:
            return ImageFinder.CLASS_DIAGRAM_ICON;
        case DK_SEQUENCE_DIAGRAM:
            return ImageFinder.SEQ_DIAGRAM_ICON;
		case DK_ACTIVITY_DIAGRAM:
			return ImageFinder.ACTIVITY_DIAGRAM_ICON;
		case DK_COLLABORATION_DIAGRAM:
			return ImageFinder.COLLABORATION_DIAGRAM_ICON;
		case DK_COMPONENT_DIAGRAM:
			return ImageFinder.COMPONENT_DIAGRAM_ICON;
		case DK_DEPLOYMENT_DIAGRAM:
			return ImageFinder.DEPLOYMENT_DIAGRAM_ICON;
		case DK_STATE_DIAGRAM:
			return ImageFinder.STATE_DIAGRAM_ICON;
		case DK_USECASE_DIAGRAM:
			return ImageFinder.USECASE_DIAGRAM_ICON;
        }
        return null;
    }

    public static String getDiagramIconPath(IDiagram diag) {
        return getDiagramIconPath(diag.getDiagramKind());
    }

    public static String getDiagramIconPath(int kind) {
        switch (kind) {
        case DK_CLASS_DIAGRAM:
            return ImageFinder.CLASS_DIAGRAM_PATH;
        case DK_SEQUENCE_DIAGRAM:
            return ImageFinder.SEQ_DIAGRAM_PATH;
        case DK_ACTIVITY_DIAGRAM:
            return ImageFinder.ACTIVITY_DIAGRAM_PATH;
        case DK_COLLABORATION_DIAGRAM:
            return ImageFinder.COLLABORATION_DIAGRAM_PATH;
        case DK_COMPONENT_DIAGRAM:
            return ImageFinder.COMPONENT_DIAGRAM_PATH;
        case DK_DEPLOYMENT_DIAGRAM:
            return ImageFinder.DEPLOYMENT_DIAGRAM_PATH;
        case DK_STATE_DIAGRAM:
            return ImageFinder.STATE_DIAGRAM_PATH;
        case DK_USECASE_DIAGRAM:
            return ImageFinder.USECASE_DIAGRAM_PATH;
        }
        return null;
    }
}
