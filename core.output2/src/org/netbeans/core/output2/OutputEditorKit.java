/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * OutputEditorKit.java
 *
 * Created on May 9, 2004, 4:34 PM
 */

package org.netbeans.core.output2;

import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.openide.util.Exceptions;

/**
 * A simple editor kit which provides instances of ExtPlainView/ExtWrappedPlainView as its views.
 *
 * @author  Tim Boudreau
 */
final class OutputEditorKit extends DefaultEditorKit implements javax.swing.text.ViewFactory, ChangeListener {
    private final boolean wrapped;
    private final JTextComponent comp;

    /** Creates a new instance of OutputEditorKit */
    OutputEditorKit(boolean wrapped, JTextComponent comp) {
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
                (javax.swing.text.View) new ExtPlainView (element);
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
    
    public void install(JEditorPane c) {
        super.install(c);
        if (wrapped) {
            c.getCaret().addChangeListener(this);
        }
    }
    
    public void deinstall(JEditorPane c) {
        super.deinstall(c);
        if (wrapped) {
            c.getCaret().removeChangeListener(this);
        }
    }    
    
    private int lastMark = -1;
    private int lastDot = -1;
    private static final Rectangle scratch = new Rectangle();
    
    /**
     * Manages repainting when the selection changes
     */
    public void stateChanged (ChangeEvent ce) {
        int mark = comp.getSelectionStart();
        int dot = comp.getSelectionEnd();
        boolean hasSelection = mark != dot;
        boolean hadSelection = lastMark != lastDot;
        
//        System.err.println("Change: " + mark + " : " + dot + "/" + lastMark + ":" + lastDot + " hadSelection " + hadSelection + " hasSelection " + hasSelection);
        
        if (lastMark != mark || lastDot != dot) {
            int begin = Math.min (mark, dot);
            int end = Math.max (mark, dot);
            int oldBegin = Math.min (lastMark, lastDot);
            int oldEnd = Math.max (lastMark, lastDot);
            
            if (hadSelection && hasSelection) {
                if (begin != oldBegin) {
                    int startChar = Math.min (begin, oldBegin);
                    int endChar = Math.max (begin, oldBegin);
                    repaintRange (startChar, endChar);
                } else {
                    int startChar = Math.min (end, oldEnd);
                    int endChar = Math.max (end, oldEnd);
                    repaintRange (startChar, endChar);
                }
            } else if (hadSelection && !hasSelection) {
                repaintRange (oldBegin, oldEnd);
            } 
            
        }
        lastMark = mark;
        lastDot = dot;
    }
    
    private void repaintRange (int start, int end) {
        try {
            Rectangle r = (Rectangle) view().modelToView(end, scratch, Position.Bias.Forward);
            int y1 = r.y + r.height;
            r = (Rectangle) view().modelToView(start, scratch, Position.Bias.Forward);
            r.x = 0;
            r.width = comp.getWidth();
            r.height = y1 - r.y;
//            System.err.println("RepaintRange " + start + " to " + end + ": " + r);
            comp.repaint (r);
        } catch (BadLocationException e) {
            comp.repaint();
            Exceptions.printStackTrace(e);
        }
    }
}
