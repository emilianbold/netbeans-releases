/*
 * CheckoutOperator.java
 *
 * Created on 05/03/08 20:05
 */
package org.netbeans.test.clearcase.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.clearcase.operators.actions.CheckoutAction;

/** Class implementing all necessary methods for handling "Checkout" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class CheckoutOperator extends NbDialogOperator {

    /** Creates new CheckoutOperator that can handle it.
     */
    public CheckoutOperator() {
        super("Checkout");
    }

    private JLabelOperator _lblCheckoutMessage;
    private JButtonOperator _btJButton;
    private JTextAreaOperator _txtCheckoutMessage;
    private JCheckBoxOperator _cbReservedCheckout;
    private JButtonOperator _btCheckout;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    public static CheckoutOperator invoke(Node[] nodes) {
        new CheckoutAction().perform(nodes);
        return new CheckoutOperator();
    }
    
    public static CheckoutOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Checkout Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCheckoutMessage() {
        if (_lblCheckoutMessage==null) {
            _lblCheckoutMessage = new JLabelOperator(this, "Checkout Message:");
        }
        return _lblCheckoutMessage;
    }

    /** Tries to find null JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btJButton() {
        if (_btJButton==null) {
            _btJButton = new JButtonOperator(this);
        }
        return _btJButton;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtCheckoutMessage() {
        if (_txtCheckoutMessage==null) {
            _txtCheckoutMessage = new JTextAreaOperator(this);
        }
        return _txtCheckoutMessage;
    }

    /** Tries to find "Reserved Checkout" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbReservedCheckout() {
        if (_cbReservedCheckout==null) {
            _cbReservedCheckout = new JCheckBoxOperator(this, "Reserved Checkout");
        }
        return _cbReservedCheckout;
    }

    /** Tries to find "Checkout" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCheckout() {
        if (_btCheckout==null) {
            _btCheckout = new JButtonOperator(this, "Checkout");
        }
        return _btCheckout;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on null JButton
     */
    public void jButton() {
        btJButton().push();
    }

    /** gets text for txtCheckoutMessage
     * @return String text
     */
    public String getCheckoutMessage() {
        return txtCheckoutMessage().getText();
    }

    /** sets text for txtCheckoutMessage
     * @param text String text
     */
    public void setCheckoutMessage(String text) {
        txtCheckoutMessage().setText(text);
    }

    /** types text for txtCheckoutMessage
     * @param text String text
     */
    public void typeCheckoutMessage(String text) {
        txtCheckoutMessage().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkReservedCheckout(boolean state) {
        if (cbReservedCheckout().isSelected()!=state) {
            cbReservedCheckout().push();
        }
    }

    /** clicks on "Checkout" JButton
     */
    public void checkout() {
        btCheckout().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CheckoutOperator by accessing all its components.
     */
    public void verify() {
        lblCheckoutMessage();
        btJButton();
        txtCheckoutMessage();
        cbReservedCheckout();
        btCheckout();
        btCancel();
        btHelp();
    }

    /** Performs simple test of CheckoutOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CheckoutOperator().verify();
        System.out.println("CheckoutOperator verification finished.");
    }
}

