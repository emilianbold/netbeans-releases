/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.beaninfo.editors;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;
import javax.swing.UIManager;
import org.openide.nodes.PropertyEditorRegistration;

/**
 * A simple property editor for javafx.scene.paint.Color class. (copied from
 * ColorEditor).
 *
 * @author S. Aubrecht
 */
@PropertyEditorRegistration(targetType = javafx.scene.paint.Color.class)
public final class JavaFxColorEditor implements PropertyEditor {

    // static .....................................................................................
    static final boolean GTK = "GTK".equals(UIManager.getLookAndFeel().getID());//NOI18N
    static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID());//NOI18N

    private static final boolean antialias = Boolean.getBoolean("nb.cellrenderer.antialiasing") // NOI18N
            || Boolean.getBoolean("swing.aatext") // NOI18N
            || (GTK && gtkShouldAntialias()) // NOI18N
            || AQUA;

    private static Boolean gtkAA;
    private static Map hintsMap;

    // variables ..................................................................................
    /**
     * Selected color.
     */
    private Color theColor;
    /**
     * Property change support. Helper field.
     */
    private PropertyChangeSupport support;

    // init .......................................................................................
    /**
     * Creates color editor.
     */
    public JavaFxColorEditor() {
        support = new PropertyChangeSupport(this);
    }

    // main methods .......................................................................................
    /**
     * Gets value. Implements <code>PropertyEditor</code> interface.
     *
     * @return <code>Color</code> value or <code>null</code>
     */
    @Override
    public Object getValue() {
        return theColor;
    }

    /**
     * Sets value. Implements <code>PropertyEditor</code> interface.
     *
     * @param object object to set, accepts <code>Color</code> or <code>SuperColor<code> types
     */
    @Override
    public void setValue(Object object) {
        if( object instanceof Color ) {
            theColor = (Color) object;
        } else {
            theColor = null;
        }

        support.firePropertyChange("", null, null); // NOI18N
    }

    /**
     * Gets value as text. Implements <code>PropertyEditor</code> interface.
     */
    @Override
    public String getAsText() {
        if( theColor == null ) {
            return "null"; // NOI18N
        }
        return theColor.toString();
    }

    /**
     * Sets value ad text. Implements <code>PropertyEditor</code> interface.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if( text == null ) {
            throw new IllegalArgumentException("null parameter"); // NOI18N
        }

        text = text.trim();

        if( "null".equals(text) ) { // NOI18N
            setValue(null);
            return;
        }

        setValue(Color.valueOf(text));
    }

    /**
     * Gets java inititalization string. Implements <code>PropertyEditor</code>
     * interface.
     */
    @Override
    public String getJavaInitializationString() {
        if( theColor == null ) {
            return "null"; // NOI18N
        }
        return "new javafx.scene.paint.Color(" + theColor.getRed() + ", " + theColor.getGreen() + // NOI18N
                ", " + theColor.getBlue() + ", " + theColor.getOpacity() + ")"; // NOI18N
    }

    /**
     * Get tags possible for choosing value. Implements
     * <code>PropertyEditor</code> interface.
     */
    @Override
    public String[] getTags() {
        return null;
    }

    /**
     * Insicates whether this editor is paintable. Implements
     * <code>PropertyEditor</code> interface.
     *
     * @return <code>true</code>
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints the current value. Implements <code>ProepertyEditor</code>
     * interface.
     */
    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        int px;

        ((Graphics2D) g).setRenderingHints(getHints());

        if( this.theColor != null ) {
            java.awt.Color color = g.getColor();
            g.drawRect(rectangle.x, rectangle.y + rectangle.height / 2 - 5, 10, 10);
            g.setColor(toAwtColor(this.theColor));
            g.fillRect(rectangle.x + 1, rectangle.y + rectangle.height / 2 - 4, 9, 9);
            g.setColor(color);
            px = 18;
        } else {
            px = 0;
        }

        FontMetrics fm = g.getFontMetrics();
        g.drawString(getAsText(), rectangle.x + px, rectangle.y
                + (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    /**
     * Indicates whether this editor supports custom editing. Implements
     * <code>PropertyEditor</code> interface.
     *
     * @return <code>true</code>
     */
    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    /**
     * Gets custom editor. Implements <code>PropertyEditor</code> interface.
     * *return <code>NbColorChooser</code> instance
     */
    @Override
    public Component getCustomEditor() {
        return null;
    }

    /**
     * Adds property change listener.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Removes property change listner.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    public static final boolean gtkShouldAntialias() {
        if( gtkAA == null ) {
            Object o = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/Antialias"); //NOI18N
            gtkAA = Boolean.valueOf(Integer.valueOf(1).equals(o));
        }

        return gtkAA.booleanValue();
    }

    // copied from openide/awt/HtmlLabelUI
    @SuppressWarnings("unchecked") // need to use reflective access, no idea of type
    private static Map getHints() {
        if( hintsMap == null ) {
            hintsMap = (Map) (Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if( hintsMap == null ) {
                hintsMap = new HashMap();
                if( antialias ) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
        }
        return hintsMap;
    }

    private static java.awt.Color toAwtColor(Color c) {
        return new java.awt.Color((float) c.getRed(), (float) c.getGreen(), (float) c.getBlue(), (float) c.getOpacity());
    }
}
