/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * OutputEditorKit.java
 *
 * Created on May 9, 2004, 4:34 PM
 */

package org.netbeans.core.output2;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

/**
 * A simple editor kit which provides instances of ExtPlainView/ExtWrappedPlainView as its views.
 *
 * @author  Tim Boudreau
 */
final class OutputEditorKit extends DefaultEditorKit implements javax.swing.text.ViewFactory {
    boolean wrapped;
    private JTextComponent comp;

    /** Creates a new instance of OutputEditorKit */
    public OutputEditorKit(boolean wrapped, JTextComponent comp) {
        this.comp = comp;
        this.wrapped = wrapped;
    }

    public WrappedTextView view() {
        return lastWrappedView;
    }

    private WrappedTextView lastWrappedView = null;
    public javax.swing.text.View create(Element element) {
        javax.swing.text.View result =
                wrapped ? (javax.swing.text.View) new WrappedTextView(element, comp) :
                (javax.swing.text.View) new ExtPlainView (element, comp);
        lastWrappedView = wrapped ? (WrappedTextView) result : null;
        if (wrapped) {
            lastWrappedView.updateInfo(null);
        }
        return result;
    }

    public javax.swing.text.ViewFactory getViewFactory() {
        return this;
    }

    public boolean isWrapped() {
        return wrapped;
    }

}
