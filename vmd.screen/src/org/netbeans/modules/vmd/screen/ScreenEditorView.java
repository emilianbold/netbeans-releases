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
package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.javame.DeviceListener;
import org.netbeans.modules.vmd.api.io.javame.MidpProjectPropertiesSupport;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
// TODO - reload screen designer only when it is visible
public class ScreenEditorView implements DataEditorView, DeviceListener {

    private static final long serialVersionUID = -1;

    public static final String SCREEN_EDITOR_VIEW_DISPLAY_NAME = NbBundle.getMessage (ScreenEditorView.class, "TITLE_ScreenView"); // NOI18N

    private DataObjectContext context;
    private transient ScreenViewController controller;
    
    public ScreenEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        controller = new ScreenViewController (context);
    }

    public DataObjectContext getContext () {
        return context;
    }

    public Kind getKind () {
        return Kind.MODEL;
    }

    public boolean canShowSideWindows () {
        return true;
    }

    public Collection<String> getTags () {
        return Collections.emptySet ();
    }

    public String preferredID () {
        return ScreenViewController.SCREEN_ID;
    }

    public String getDisplayName () {
        return SCREEN_EDITOR_VIEW_DISPLAY_NAME;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ScreenEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return controller.getVisualRepresentation ();
    }

    public JComponent getToolbarRepresentation () {
        return controller.getToolbarRepresentation ();
    }

    public UndoRedo getUndoRedo () {
        return null;
    }

    public void componentOpened () {
    }

    public void componentClosed () {
    }

    public void componentShowing () {
        MidpProjectPropertiesSupport.addDeviceListener (context, this);
        deviceChanged ();
    }

    public void componentHidden () {
        MidpProjectPropertiesSupport.removeDeviceChangedListener (context, this);
    }

    public void componentActivated () {
    }

    public void componentDeactivated () {
    }

    public int getOpenPriority () {
        return getOrder ();
    }

    public int getEditPriority () {
        return - getOrder ();
    }

    public int getOrder () {
        return 1000;
    }

    public void deviceChanged () {
        controller.setScreenSize (MidpProjectPropertiesSupport.getDeviceScreenSizeFromProject(context));
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (context);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found"); // NOI18N
        context = (DataObjectContext) object;
        init ();
    }

}
