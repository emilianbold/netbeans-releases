/*
 * CustomButton.java
 * 
 * Created on Aug 8, 2007, 9:56:02 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package data.components;

import javax.swing.JButton;

/**
 *
 * @author jirka
 */
public class CustomButton extends JButton {
    
    public CustomButton() {
        super();
        this.setText("Custom Button");
    }
    
    public static JButton createButton() {
        return new CustomButton();
    }

}
