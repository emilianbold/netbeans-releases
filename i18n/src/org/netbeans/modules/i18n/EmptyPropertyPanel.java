/*
 * EmptyPropertyPanel.java
 *
 * Created on July 29, 2004, 7:52 PM
 */

package org.netbeans.modules.i18n;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import javax.swing.BoxLayout;
import javax.swing.Box;

/**
 *
 * @author  or141057
 */
public class EmptyPropertyPanel extends javax.swing.JPanel {
    
    /** Creates new form EmptyPropertyPanel */
    public EmptyPropertyPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        theLabel = new javax.swing.JLabel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(50));

        theLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        theLabel.setText("....");
        add(theLabel);
    }
    
    
    private javax.swing.JLabel theLabel;
    
    public void setBundleText(String textID) throws MissingResourceException {
        theLabel.setText(NbBundle.getMessage(EmptyPropertyPanel.class, textID));
    }
    
    public String getText() {
        return theLabel.getText();
    }
}
