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
package org.netbeans.modules.web.javascript.debugger;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;


/**
 * @author ads
 *
 */
public abstract class ViewModelSupport {
    
    private CopyOnWriteArrayList<ModelListener> myListeners;
    
    protected ViewModelSupport() {
        myListeners = new CopyOnWriteArrayList<ModelListener>();
    }

    public void addModelListener(ModelListener l) {
        myListeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        myListeners.remove(l);
    }

    protected void refresh() {
        fireChangeEvent(new TreeChanged(this));
    }
    
    protected void fireChangeEvent(ModelEvent modelEvent) {
        for ( ModelListener listener : myListeners ) {
            listener.modelChanged(modelEvent);
        }
    }
    
    protected void fireChangeEvents(ModelEvent[] events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }
    
    protected void fireChangeEvents(Collection<ModelEvent> events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }
    
    
    public static String toHTML (
        String text,
        boolean bold,
        boolean italics,
        Color color
    ) {
        if (text == null) return null;
        if (text.length() > 6 && text.substring(0, 6).equalsIgnoreCase("<html>")) {
            return text; // Already HTML
        }
        StringBuffer sb = new StringBuffer ();
        sb.append ("<html>");
        if (bold) sb.append ("<b>");
        if (italics) sb.append ("<i>");
        if (color != null) {
            sb.append ("<font color=");
            sb.append (Integer.toHexString ((color.getRGB () & 0xffffff)));
            sb.append (">");
        } else {
            sb.append ("<font color=000000>");
        }
        text = text.replaceAll ("&", "&amp;");
        text = text.replaceAll ("<", "&lt;");
        text = text.replaceAll (">", "&gt;");
        sb.append (text);
        /*if (color != null)*/ sb.append ("</font>");
        if (italics) sb.append ("</i>");
        if (bold) sb.append ("</b>");
        sb.append ("</html>");
        return sb.toString ();
    }
    
}
