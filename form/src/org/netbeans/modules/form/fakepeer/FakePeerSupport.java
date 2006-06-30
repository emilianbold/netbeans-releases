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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextComponentPeer;
import java.awt.peer.TextFieldPeer;
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
            peer = getFakePeer(LabelPeer.class, new FakeLabelPeer((Label) comp));
        else if (comp instanceof Button)
            peer = getFakePeer(ButtonPeer.class, new FakeButtonPeer((Button) comp));                   
        else if (comp instanceof Panel)
            peer = getFakePeer(new Class[] {ContainerPeer.class, PanelPeer.class}, new FakePanelPeer((Panel) comp));
        else if (comp instanceof TextField)
            peer = getFakePeer(new Class[] {TextFieldPeer.class, TextComponentPeer.class}, new FakeTextFieldPeer((TextField) comp));
        else if (comp instanceof TextArea)
            peer = getFakePeer(new Class[] {TextAreaPeer.class, TextComponentPeer.class}, new FakeTextAreaPeer((TextArea) comp));
        else if (comp instanceof TextComponent)
            peer = getFakePeer(TextComponentPeer.class, new FakeTextComponentPeer((TextComponent) comp));
        else if (comp instanceof Checkbox)
            peer = getFakePeer(CheckboxPeer.class, new FakeCheckboxPeer((Checkbox) comp));
        else if (comp instanceof Choice)
            peer = getFakePeer(ChoicePeer.class, new FakeChoicePeer((Choice) comp));
        else if (comp instanceof List)
            peer = getFakePeer(ListPeer.class, new FakeListPeer((List) comp));
        else if (comp instanceof Scrollbar)
            peer = getFakePeer(ScrollbarPeer.class, new FakeScrollbarPeer((Scrollbar) comp));
        else if (comp instanceof ScrollPane)
            peer = getFakePeer(new Class[] {ContainerPeer.class, ScrollPanePeer.class}, new FakeScrollPanePeer((ScrollPane) comp));
        else if (comp instanceof Canvas)
            peer = getFakePeer(CanvasPeer.class, new FakeCanvasPeer((Canvas) comp));
        else
            return false;

        attachFakePeer(comp, peer);
        return true;
    }
    
    private static FakePeer getFakePeer(Class fakePeerInterfaces, FakeComponentPeer compPeer) {                
        return getFakePeer(new Class[] {fakePeerInterfaces}, compPeer);
    }
    
    private static FakePeer getFakePeer(Class[] fakePeerInterfaces, FakeComponentPeer compPeer) {        
        
        // FakePeer.class and java.awt.peer.LightweightPeer.class interfaces
        // should be implemented for each FakeComponentPeer
        Class[] interfaces = new Class[fakePeerInterfaces.length + 2];
        System.arraycopy(fakePeerInterfaces, 0, interfaces, 0,  fakePeerInterfaces.length);
        interfaces[fakePeerInterfaces.length] = FakePeer.class;
        interfaces[fakePeerInterfaces.length+1] = java.awt.peer.LightweightPeer.class;
        
        Class proxyClass = Proxy.getProxyClass(compPeer.getClass().getClassLoader(), interfaces);        
        FakePeerInvocationHandler handler = new FakePeerInvocationHandler(compPeer); 
        try {
           return (FakePeer) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });                   
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    public static void attachFakePeer(Component comp, ComponentPeer peer) {
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            f.set(comp, peer);
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

    public static void attachFakePeerRecursively(Container container) {
        Component components[] = getComponents(container);
        for (int i=0; i < components.length; i++) {
            Component comp = components[i];
            attachFakePeer(comp);
            if (comp instanceof Container)
                attachFakePeerRecursively((Container) comp);
        }
    }

    static Component[] getComponents(Container container) {
        // hack for the case some "smart" containers delegate getComponents()
        // to some subcontainer (which becomes inaccessible then)
        try {
            Field f = Container.class.getDeclaredField("component"); // NOI18N
            f.setAccessible(true);
            return (Component[]) f.get(container);
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return container.getComponents();
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
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    public static Font getDefaultAWTFont() {
        if (defaultFont == null) {
            defaultFont = org.openide.windows.WindowManager.getDefault()
                                               .getMainWindow().getFont();
            if (defaultFont == null)
                defaultFont = new Font("Dialog", Font.PLAIN, 12); // NOI18N
        }
        return defaultFont;
    }

    private static Font defaultFont;
}
