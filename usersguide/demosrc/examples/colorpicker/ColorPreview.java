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

package examples.colorpicker;

/** ColorPreview class is a visual component that sets its background color according to
 * given red, green and blue values.
 */
public class ColorPreview extends javax.swing.JPanel {

    private int red;
    private java.beans.PropertyChangeSupport propertyChangeSupport;
    private int green;
    private int blue;

    /** ColorPreview constructor.
     */
    public ColorPreview() {
        propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    }

    /** Adds new property change listener to be registered with this bean.
     * @param l PropertyChangeListener to be registered with this bean.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    /** Removes previously added property added listener.
     * @param l PropertyChangeListener to be unregistered from this bean.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    /** Red value getter.
     * @return Red value of this bean.
     */
    public int getRed() {
        return red;
    }

    /** Red value setter.
     * @param red Red value of this bean.
     */
    public void setRed(int red) {
        int oldRed = this.red;
        this.red = red;
        propertyChangeSupport.firePropertyChange("red", new Integer(oldRed), new Integer(red));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }

    /** Green value getter.
     * @return Green value of this bean.
     */
    public int getGreen() {
        return green;
    }

    /** Green value setter.
     * @param green Green value of this bean.
     */
    public void setGreen(int green) {
        int oldGreen = this.green;
        this.green = green;
        propertyChangeSupport.firePropertyChange("green", new Integer(oldGreen), new Integer(green));
        setBackground(new java.awt.Color(red, green, blue));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }

    public void setYellow(int yellow) {
        int oldYellow = this.yellow;
        i
        this.yellow = yellow;
        propertyChangeSupport.firePropertyChange ("yellow", new Integer(oldYellow), new Integer(yellow));
        setBackground(new java.awt.Color(red, green, blue))
        repaint();
        
    }
    System.out.println("whatever")
System.out.println("yeah, I guess")
    /** Blue value getter.
     * @return Blue value of this bean.
     */
    public int getBlue() {
        return blue;
    }

    /** Blue value setter.
     * @param blue Blue value of this bean.
     */
    public void setBlue(int blue) {
        int oldBlue = this.blue;
        this.blue = blue;
        propertyChangeSupport.firePropertyChange("blue", new Integer(oldBlue), new Integer(blue));
        setBackground(new java.awt.Color(red, green, blue));
        repaint();
    }
}
