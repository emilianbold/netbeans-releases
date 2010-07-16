package org.netbeans.modules.wsdlextensions.ldap;


import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.wsdlextensions.ldap.LDAPError;

/**
 * ErrorPropagator
 *    Convenient class to propagate the property change to the correct
 *    component that the framework is listening to.
 * 
 * @author 
 */
public class ErrorPropagator {
    public static boolean doFirePropertyChange(String name, Object oldValue,
            Object newValue, JComponent sourcePanel) {
        return doFirePropertyChange(name, oldValue, newValue, sourcePanel, null);
    }    
    
    public static boolean doFirePropertyChange(String name, Object oldValue,
            Object newValue, JComponent sourcePanel, LDAPError ldapError) {
        
        // We want to fire the property change event to the framework so the
        // message shows; however, we need to fire to the right panel that
        // the framework is listening to based on where the panels are plugged
        // in (ie WSDL Wizard or CASA as there are 2 separate interface)

       JComponent panelToFireTo = (LDAPServerBrowserVisualPanel2) SwingUtilities.
                    getAncestorOfClass(LDAPServerBrowserVisualPanel2.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            if (ldapError == null) {
            	ldapError =
                    ((LDAPServerBrowserVisualPanel2) panelToFireTo).validateInput();
            }            ((LDAPServerBrowserVisualPanel2) panelToFireTo).
                    doFirePropertyChange(ldapError.getErrorMode(), oldValue,
                    		ldapError.getErrorMessage());
            return true;
        }  
        return false;
    }   
	
       /* JComponent panelToFireTo = (LDAPServerBrowserVisualPanel3) SwingUtilities.
                    getAncestorOfClass(LDAPServerBrowserVisualPanel3.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            if (ldapError == null) {
            	ldapError =
                    ((LDAPServerBrowserVisualPanel3) panelToFireTo).validateSelectedRDN();
            }            ((LDAPServerBrowserVisualPanel3) panelToFireTo).
                    doFirePropertyChange(ldapError.getErrorMode(), oldValue,
                    		ldapError.getErrorMessage());
            return true;
        }  */
         
}
