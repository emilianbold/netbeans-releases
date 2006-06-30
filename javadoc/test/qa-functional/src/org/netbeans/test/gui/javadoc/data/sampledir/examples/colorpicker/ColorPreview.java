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

package examples.colorpicker;

public class ColorPreview extends javax.swing.JPanel {

    private int red;
    private java.beans.PropertyChangeSupport propertyChangeSupport;
    private int green;
    private int blue;

    /** Creates new ColorPreview */
    public ColorPreview() {
        propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        int oldRed = this.red;
        this.red = red;
        propertyChangeSupport.firePropertyChange("red" , new Integer(oldRed) , new Integer(red));
        setBackground (new java.awt.Color (red, green, blue));
        repaint ();
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        int oldGreen = this.green;
        this.green = green;
        propertyChangeSupport.firePropertyChange("green" , new Integer(oldGreen) , new Integer(green));
        setBackground (new java.awt.Color (red, green, blue));
        repaint ();
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        int oldBlue = this.blue;
        this.blue = blue;
        propertyChangeSupport.firePropertyChange("blue" , new Integer(oldBlue) , new Integer(blue));
        setBackground (new java.awt.Color (red, green, blue));
        repaint ();
    }
}
