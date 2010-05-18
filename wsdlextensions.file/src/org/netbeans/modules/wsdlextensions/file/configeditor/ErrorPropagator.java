/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * ErrorPropagator
 *    Convenient class to propagate the property change to the correct
 *    component that the framework is listening to.
 * 
 * @author jalmero
 */
public class ErrorPropagator {
    public static boolean doFirePropertyChange(String name, Object oldValue,
            Object newValue, JComponent sourcePanel) {
        return doFirePropertyChange(name, oldValue, newValue, sourcePanel, null);
    }    
    
    public static boolean doFirePropertyChange(String name, Object oldValue,
            Object newValue, JComponent sourcePanel, FileError fileError) {
        
        // We want to fire the property change event to the framework so the
        // message shows; however, we need to fire to the right panel that
        // the framework is listening to based on where the panels are plugged
        // in (ie WSDL Wizard or CASA as there are 2 separate interface)
        
        JComponent panelToFireTo = (InboundOutboundMessagePanel) SwingUtilities.
                    getAncestorOfClass(InboundOutboundMessagePanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            if (fileError == null) {
                fileError =
                    ((InboundOutboundMessagePanel) panelToFireTo).validateMe();
            }
            ((InboundOutboundMessagePanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }  
        panelToFireTo = (InboundMessagePanel) SwingUtilities.
                getAncestorOfClass(InboundMessagePanel.class, sourcePanel);
        if (panelToFireTo != null) {
            if (fileError == null) {
                fileError = ((InboundMessagePanel) panelToFireTo).validateMe();
            }
            ((InboundMessagePanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }
        
        panelToFireTo = (OutboundMessageInPanel) SwingUtilities.
                getAncestorOfClass(OutboundMessageInPanel.class, sourcePanel);
        if (panelToFireTo != null) {
            if (fileError == null) {
                fileError = ((OutboundMessageInPanel) panelToFireTo).
                    validateMe();
            }
            ((OutboundMessageInPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }   
        
        panelToFireTo = (OutboundMessagePanel) SwingUtilities.
                getAncestorOfClass(OutboundMessagePanel.class, sourcePanel);
        if (panelToFireTo != null) {
            if (fileError == null) {
                fileError = ((OutboundMessagePanel) panelToFireTo).
                    validateMe();
            }
            ((OutboundMessagePanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }          

        panelToFireTo = (SolicitedReadPanel) SwingUtilities.
                getAncestorOfClass(SolicitedReadPanel.class, sourcePanel);
        if (panelToFireTo != null) {
            if (fileError == null) {
                fileError = ((SolicitedReadPanel) panelToFireTo).validateMe();
            }
            ((SolicitedReadPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        } 
        
        return false;
    }    
}
