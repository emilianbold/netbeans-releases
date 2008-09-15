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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.uml.drawingarea.view;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;
import java.util.Locale;

/**
 * 
 */
public class UMLWidgetAccessibleContext extends AccessibleContext 
    implements AccessibleComponent 
{

    private Widget widget = null;

    public UMLWidgetAccessibleContext (Widget widget) {
        this.widget = widget;
    }

    public AccessibleRole getAccessibleRole () {
        return AccessibleRole.PANEL;
    }

    public AccessibleStateSet getAccessibleStateSet () {
        AccessibleStateSet accStateSet = new AccessibleStateSet();
        ObjectState state = widget.getState();
        if (state != null) 
        {
            if (state.isSelected())
            {
                accStateSet.add(AccessibleState.SELECTED);
            }
            if (state.isFocused())
            {
                accStateSet.add(AccessibleState.FOCUSED);
            }
            if (widget.isVisible())
            {
                accStateSet.add(AccessibleState.VISIBLE);
            }
            if (isShowing()) 
            {
                accStateSet.add(AccessibleState.SHOWING);
            }
            if (widget.isEnabled())
            {
                accStateSet.add(AccessibleState.ENABLED);
            }
            if (widget.isOpaque())
            {
                accStateSet.add(AccessibleState.OPAQUE);
            }
        }
        return accStateSet;            
    }

    public int getAccessibleIndexInParent () {
        return widget != widget.getScene () ? widget.getParentWidget ().getChildren ().indexOf (widget) : 0;
    }

    public int getAccessibleChildrenCount () {
        return widget.getChildren ().size ();
    }

    public Accessible getAccessibleChild (int i) {
        if (i >= 0  && i < widget.getChildren().size()) 
        {  
            return widget.getChildren().get (i);
        }
        return null;
    }
     
    public Locale getLocale () throws IllegalComponentStateException {
        JComponent view = widget.getScene ().getView ();
        return view != null ? view.getLocale () : Locale.getDefault ();
    }

    public void notifyChildAdded (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, null, child);
    }

    public void notifyChildRemoved (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, child, null);
    }


    public AccessibleComponent getAccessibleComponent()
    {
        return this;
    }

    public Accessible getAccessibleParent()
    {
        if (widget instanceof Scene) 
        {
            JComponent comp = ((Scene)widget).getView();
            if (comp instanceof Accessible) 
            {
                return (Accessible)comp;
            }
        }
        return widget.getParentWidget();
    }


    public String getAccessibleName() 
    {
        String name = super.getAccessibleName();
        if (name != null)
        {
            return name;
        }
        Scene scene = widget.getScene();
        if (scene instanceof ObjectScene) 
        {
            IPresentationElement pe 
                = (IPresentationElement)((ObjectScene)scene).findObject(widget);
            if (pe != null) 
            {
                IElement elem = pe.getFirstSubject();
                if (elem != null) 
                {
                    name = elem.getExpandedElementType();
                    if (elem instanceof INamedElement) 
                    {
                        name += " " +((INamedElement)elem).getName();
                    }
                }
            }
        }
        return name;
    }

    public String getAccessibleDescription() 
    {
        String desc = super.getAccessibleDescription();
        if (desc != null)
        {
            return desc;
        }
        return getAccessibleName();
    }

    public boolean contains(Point p) 
    {
        if (p != null) 
        {
            Rectangle bounds = widget.getBounds();
            if (bounds != null) 
            {
                return bounds.contains(p);
            }
        }
        return false;
    }

    public Accessible getAccessibleAt(Point p)
    {
        int count = getAccessibleChildrenCount();
        for (int i = 0; i < count; i++) 
        {
            Accessible child = getAccessibleChild(i);
            if (child != null) 
            {
                AccessibleContext ctx = child.getAccessibleContext();
                if (ctx != null) 
                {
                    AccessibleComponent comp = ctx.getAccessibleComponent();
                    if (comp.contains(p)) 
                    {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    public Color getBackground() 
    {
        Paint p = widget.getBackground();
        if (p instanceof Color) 
        {
            return (Color)p;
        }
        return null; 
    }

    public void setBackground(Color c) 
    {
        widget.setBackground(c);
    }

    public Rectangle getBounds() 
    {
        return new Rectangle(getLocation(), widget.getBounds().getSize());
    }

    public void setBounds(Rectangle r) 
    {
        widget.resolveBounds(widget.getLocation(), r);        
    }

    public Cursor getCursor() 
    {
        return widget.getCursor();
    }

    public void setCursor(Cursor cursor) 
    {
        widget.setCursor(cursor);
    }

    public Font getFont()  
    {
        return widget.getFont();
    }

    public void setFont(Font f)  
    {
        widget.setFont(f);
    }

    public FontMetrics getFontMetrics(Font f) 
    {
        Scene scene = widget.getScene();
        if (scene != null) 
        {
            Component view = scene.getView();
            if (view != null) {
                return view.getFontMetrics(f);
            }
        }
        return null;
    }

    public Color getForeground() 
    {
        return widget.getForeground();
    }

    public void setForeground(Color c) 
    {
        widget.setForeground(c);
    }

    public Point getLocation() 
    {
        Point point = widget.getLocation();
        if (widget instanceof ConnectionWidget) {

            ConnectionWidget cw = (ConnectionWidget)widget;
            
            java.util.List<Point> points = cw.getControlPoints ();
            int xmin = 0, ymin = 0, xmax = 0, ymax = 0;
            boolean first = true;
            for(Point p : points) {
                if (first) {
                    xmin = p.x;
                    xmax = p.x;
                    ymin = p.y;
                    ymax = p.y;
                    first = false;
                } else {
                    if (p.x > xmax) 
                    {
                     xmax = p.x;
                    }
                    else if (p.x < xmin) 
                    {
                        xmin = p.x;
                    } 
                    if (p.y > ymax) 
                    {
                        ymax = p.y;
                    }
                    else if (p.y < ymin) 
                    {
                        ymin = p.y;
                    }     
                }      
            }
            return new Point(xmin, ymin);    
        }
        /*
        Widget parent = widget.getParentWidget();
        if (p!= null && parent != null) 
        {
            Point parentLocation = parent.getLocation();
            if (parentLocation != null) 
            {
                p.translate(-parentLocation.x, -parentLocation.y);
                return p;
            }
            
        }
        */
        return point;               
    }

    public Point getLocationOnScreen() 
    {
        Point location = widget.getLocation();
        if (widget instanceof Scene) 
        {
            JComponent view = ((Scene)widget).getView();
            if (view != null) 
            {
                if (view instanceof Accessible) 
                {
                    AccessibleContext ctx = view.getAccessibleContext();
                    if (ctx != null) 
                    {
                        AccessibleComponent comp = ctx.getAccessibleComponent();
                        if (comp != null) 
                        {
                            Point p = comp.getLocationOnScreen();
                            if (p != null) 
                            {
                                location.translate(p.x, p.y);
                            }
                        }
                    }  
                }
            }            
        }
        else
        {
            Scene scene = widget.getScene();
            if (scene != null) 
            {
                if (scene instanceof Accessible) 
                {
                    AccessibleContext ctx = scene.getAccessibleContext();
                    if (ctx != null) 
                    {
                        AccessibleComponent comp = ctx.getAccessibleComponent();
                        if (comp != null) 
                        {
                            Point p = comp.getLocationOnScreen();
                            if (p != null) 
                            {
                                location.translate(p.x, p.y);
                            }
                        }  
                    }
                }            
            }
        }
        return location;
    }

    public void setLocation(Point p) {
        widget.resolveBounds(p, widget.getBounds());
    }


    public Dimension getSize() 
    {
        Rectangle bounds = widget.getBounds();
        if (bounds != null) 
        {
            return bounds.getSize();
        }
        return null;
    }

    public void  setSize(Dimension d) 
    {
        widget.resolveBounds(widget.getLocation(), 
                             new Rectangle(widget.getLocation(), d));
    }

    public boolean isEnabled()  
    {
        return widget.isEnabled();
    }

    public void setEnabled(boolean b)  
    {
        widget.setEnabled(b);
    }


    public void addFocusListener(FocusListener l) 
    {
    }

    public void removeFocusListener(FocusListener l) 
    {
    }

    public boolean isFocusTraversable() 
    { 
        // TBD
        return true;        
    }
    
    public void requestFocus() 
    {

    }

    public boolean isShowing() 
    {
        if ( ! (widget.isVisible())) 
        {
            return false;
        }
        AccessibleComponent comp = null;
        Widget parent = widget;
        while (comp == null) 
        {
            Widget next = parent.getParentWidget();
            if (next == null) 
            {
                break;
            } 
            else 
            {
                parent = next;
            }
            if (parent.isVisible()) 
            {
                AccessibleContext ctx = parent.getAccessibleContext();
                if (ctx != null) 
                {
                    comp = ctx.getAccessibleComponent();
                    if (comp != null) 
                    {
                        return comp.isShowing();
                    } 
                }
            }
            else 
            {
                return false;
            }
        }
    
        if (parent instanceof Scene) {
            Container guiComp = ((Scene)parent).getView();
            while (comp == null) 
            {
                AccessibleContext ctx = guiComp.getAccessibleContext();
                if (ctx != null) 
                {
                    comp = ctx.getAccessibleComponent();
                    if (comp != null) 
                    {
                        return comp.isShowing();
                    } 
                }
                if (guiComp.isVisible()) 
                {
                    Container next = guiComp.getParent();
                    if (next == null) 
                    {
                        return true;
                    } 
                    else 
                    {
                        guiComp = next;
                    }
                }
                else 
                {
                    return false;
                }
            }
        }             
        return false;
    }
    
    public boolean isVisible() 
    {
        return widget.isVisible();
    }

    public void setVisible(boolean b) 
    {
        widget.setVisible(b);
    }


}

