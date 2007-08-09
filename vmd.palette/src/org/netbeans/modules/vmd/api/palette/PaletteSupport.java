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
 *
 */

package org.netbeans.modules.vmd.api.palette;

import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.palette.PaletteMap;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

import java.awt.datatransfer.Transferable;

/**
 * @author David Kaspar
 */
public final class PaletteSupport {

    public static final String VIEW_TAG_NO_PALETTE = "no-palette"; // NOI18N

    private PaletteSupport () {
    }

    public static PaletteController getPaletteController (DesignDocument document) {
        return PaletteMap.getInstance ().getPaletteKitForProjectType (document.getDocumentInterface ().getProjectType ()).getPaletteController ();
    }

    public static PaletteController getPaletteController (String projectType) {
        return PaletteMap.getInstance ().getPaletteKitForProjectType (projectType).getPaletteController ();
    }

    public static Transferable createTransferable (DesignDocument document, Lookup item) {
        ExTransferable transferable = ExTransferable.create (ExTransferable.EMPTY);
        PaletteMap.getInstance ().getPaletteKitForProjectType (document.getDocumentInterface ().getProjectType ()).getDndHandler ().customize (transferable, item);
        return transferable;
    }

}
