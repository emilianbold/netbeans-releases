/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ComponentPeer;
import java.lang.reflect.*;

/**
 *
 * @author Tran Duc Trung
 */

public class FakePeerSupport
{
    private FakePeerSupport() {
    }

    public static boolean attachFakePeer(Component comp) {
        if (comp == null || comp.isDisplayable()
              || comp instanceof javax.swing.JComponent
              || comp instanceof javax.swing.RootPaneContainer)
            return false;

        FakePeer peer = null;

        if (comp instanceof Label)
            peer = new FakeLabelPeer((Label) comp);
        else if (comp instanceof Button)
            peer = new FakeButtonPeer((Button) comp);
        else if (comp instanceof Panel)
            peer = new FakePanelPeer((Panel) comp);
        else if (comp instanceof TextField)
            peer = new FakeTextFieldPeer((TextField) comp);
        else if (comp instanceof TextArea)
            peer = new FakeTextAreaPeer((TextArea) comp);
        else if (comp instanceof TextComponent)
            peer = new FakeTextComponentPeer((TextComponent) comp);
        else if (comp instanceof Checkbox)
            peer = new FakeCheckboxPeer((Checkbox) comp);
        else if (comp instanceof Choice)
            peer = new FakeChoicePeer((Choice) comp);
        else if (comp instanceof List)
            peer = new FakeListPeer((List) comp);
        else if (comp instanceof Scrollbar)
            peer = new FakeScrollbarPeer((Scrollbar) comp);
        else if (comp instanceof ScrollPane)
            peer = new FakeScrollPanePeer((ScrollPane) comp);
        else if (comp instanceof Canvas)
            peer = new FakeCanvasPeer((Canvas) comp);
        else
            return false;

        attachFakePeer(comp, peer);
        return true;
    }

    public static void attachFakePeer(Component comp, ComponentPeer peer) {
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            f.set(comp, peer);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void attachFakePeerRecursively(Container container) {
        Component components[] = container.getComponents();
        int ncomponents = components.length;

        for (int i = 0; i < ncomponents; i++) {
            Component comp = components[i];
            attachFakePeer(comp);
            if (comp instanceof Container)
                attachFakePeerRecursively((Container) comp);
        }
    }

    public static ComponentPeer detachFakePeer(Component comp) {
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            Object peer = (ComponentPeer) f.get(comp);
            if (peer instanceof FakePeer) {
                f.set(comp, null);
                return (FakePeer) peer;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Font getDefaultAWTFont() {
        return new Font("Dialog", Font.PLAIN, 12); // NOI18N
    }
}
