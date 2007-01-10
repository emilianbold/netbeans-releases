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
package org.netbeans.modules.vmd.io.editor;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.Serializable;

/**
 * @author David Kaspar
 */
public class EditorViewDescription implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = 3317521754395749572L;

    private static final Image DESIGN_ICON = Utilities.loadImage ("org/netbeans/modules/vmd/io/resources/design.gif"); // NOI18N

    private DataObjectContext context;
    private DataEditorView view;

    public EditorViewDescription (DataObjectContext context, DataEditorView view) {
        this.context = context;
        this.view = view;
    }

    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public String getDisplayName () {
        return view.getDisplayName ();
    }

    public Image getIcon () {
        return DESIGN_ICON;
    }

    public HelpCtx getHelpCtx () {
        return view.getHelpCtx ();
    }

    public String preferredID () {
        return view.preferredID ();
    }

    public MultiViewElement createElement () {
        return new EditorViewElement (context, view);
    }

    public DataEditorView getView () {
        return view;
    }

}
