package org.netbeans.modules.wsdlextensions.email.editor.panels;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.wsdlextensions.email.editor.EmailError;

/**
 * ErrorPropagator
 *    Convenient class to propagate the property change to the correct
 *    component that the framework is listening to.
 * 
 * @author 
 */
public class ErrorPropagator {

    public static boolean doFirePropertyChange(EmailError emailError, JComponent sourcePanel) {
        // We want to fire the property change event to the framework so the
        // message shows; however, we need to fire to the right panel that
        // the framework is listening to based on where the panels are plugged
        // in (ie WSDL Wizard or CASA as there are 2 separate interface)
        JComponent panelToFireTo = null;
        if (sourcePanel instanceof InboundMessagePanel) {
            panelToFireTo = sourcePanel;
        } else {
            panelToFireTo = (InboundMessagePanel) SwingUtilities.getAncestorOfClass(InboundMessagePanel.class,
                    sourcePanel);
        }
        if (panelToFireTo != null) {
            if (emailError == null) {
                emailError =
                        ((InboundMessagePanel) panelToFireTo).validateEmailProperties();
            }
            ((InboundMessagePanel) panelToFireTo).doFirePropertyChange(emailError.getErrorMode(), null,
                    emailError.getErrorMessage());
            return true;
        }

        if (sourcePanel instanceof OutboundMessagePanel) {
            panelToFireTo = sourcePanel;
        } else {
            panelToFireTo = (OutboundMessagePanel) SwingUtilities.getAncestorOfClass(OutboundMessagePanel.class, sourcePanel);
        }
        if (panelToFireTo != null) {
            if (emailError == null) {
                emailError = ((OutboundMessagePanel) panelToFireTo).validateEmailProperties();
            }
            ((OutboundMessagePanel) panelToFireTo).doFirePropertyChange(emailError.getErrorMode(), null,
                    emailError.getErrorMessage());
            return true;
        }

        return false;
    }
}
