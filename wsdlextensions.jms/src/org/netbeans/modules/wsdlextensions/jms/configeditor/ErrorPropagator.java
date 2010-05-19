/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.jms.configeditor;

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
        
        // We want to fire the property change event to the framework so the
        // message shows; however, we need to fire to the right panel that
        // the framework is listening to based on where the panels are plugged
        // in (ie WSDL Wizard or CASA as there are 2 separate interface)
        
        JComponent panelToFireTo = (InboundOneWayMainPanel) SwingUtilities.
                    getAncestorOfClass(InboundOneWayMainPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((InboundOneWayMainPanel) panelToFireTo).validateMe();
            ((InboundOneWayMainPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }  
        
        panelToFireTo = (InboundRequestResponseMainPanel) SwingUtilities.
                getAncestorOfClass(InboundRequestResponseMainPanel.class, sourcePanel);
        if (panelToFireTo != null) {
            FileError fileError = ((InboundRequestResponseMainPanel) panelToFireTo).
                    validateMe();
            ((InboundRequestResponseMainPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }
        
        
        panelToFireTo = (InboundResponseMessageConsumerPanel) SwingUtilities.getAncestorOfClass(InboundResponseMessageConsumerPanel.class, sourcePanel);
	if (panelToFireTo != null) {
	    FileError fileError = ((InboundResponseMessageConsumerPanel) panelToFireTo).validateMe();
	    ((InboundResponseMessageConsumerPanel) panelToFireTo).doFirePropertyChange(fileError.getErrorMode(), oldValue, fileError.getErrorMessage());
	    return true;
	}
        
        panelToFireTo = (InboundOneWayMessagePanel) SwingUtilities.
                getAncestorOfClass(InboundOneWayMessagePanel.class, sourcePanel);
        if (panelToFireTo != null) {
            FileError fileError = ((InboundOneWayMessagePanel) panelToFireTo).
                    validateMe();
            ((InboundOneWayMessagePanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }
        
        panelToFireTo = (InboundOneWayConsumerPanel) SwingUtilities.
                getAncestorOfClass(InboundOneWayConsumerPanel.class, sourcePanel);
        if (panelToFireTo != null) {
            FileError fileError = ((InboundOneWayConsumerPanel) panelToFireTo).
                    validateMe();
            ((InboundOneWayConsumerPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }
      
        panelToFireTo = (InboundResponseMessagePanel) SwingUtilities.
                getAncestorOfClass(InboundResponseMessagePanel.class, sourcePanel);
        if (panelToFireTo != null) {
            FileError fileError = ((InboundResponseMessagePanel) panelToFireTo).
                    validateMe();
            ((InboundResponseMessagePanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }     
        
        panelToFireTo = (InboundRequestResponseMainPanel) SwingUtilities.
                    getAncestorOfClass(InboundRequestResponseMainPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((InboundRequestResponseMainPanel) panelToFireTo).validateMe();
            ((InboundRequestResponseMainPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }              
        
        panelToFireTo = (OutboundOneWayMainPanel) SwingUtilities.
                getAncestorOfClass(OutboundOneWayMainPanel.class, sourcePanel);
        if (panelToFireTo != null) {
            FileError fileError = ((OutboundOneWayMainPanel) panelToFireTo).
                    validateMe();
            ((OutboundOneWayMainPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }     
        
        panelToFireTo = (OutboundOneWayConnectionPanel) SwingUtilities.
                    getAncestorOfClass(OutboundOneWayConnectionPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((OutboundOneWayConnectionPanel) panelToFireTo).validateMe();
            ((OutboundOneWayConnectionPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }  
        
        panelToFireTo = (SynchronousReadPanel) SwingUtilities.
                    getAncestorOfClass(SynchronousReadPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((SynchronousReadPanel) panelToFireTo).validateMe();
            ((SynchronousReadPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        } 
        
        panelToFireTo = (SolicitedMainPanel) SwingUtilities.
                    getAncestorOfClass(SolicitedMainPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((SolicitedMainPanel) panelToFireTo).validateMe();
            ((SolicitedMainPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }   
        
        panelToFireTo = (OutboundResponseReplyPanel) SwingUtilities.
                    getAncestorOfClass(OutboundResponseReplyPanel.class, 
                    sourcePanel); 
        if (panelToFireTo != null) {
            FileError fileError =
                    ((OutboundResponseReplyPanel) panelToFireTo).validateMe();
            ((OutboundResponseReplyPanel) panelToFireTo).
                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
                    fileError.getErrorMessage());
            return true;
        }          
//        
//        panelToFireTo = (OutboundMessagePanel) SwingUtilities.
//                getAncestorOfClass(OutboundMessagePanel.class, sourcePanel);
//        if (panelToFireTo != null) {
//            FileError fileError = ((OutboundMessagePanel) panelToFireTo).
//                    validateMe();
//            ((OutboundMessagePanel) panelToFireTo).
//                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
//                    fileError.getErrorMessage());
//            return true;
//        }          
//
//        panelToFireTo = (SolicitedReadPanel) SwingUtilities.
//                getAncestorOfClass(SolicitedReadPanel.class, sourcePanel);
//        if (panelToFireTo != null) {
//            FileError fileError = ((SolicitedReadPanel) panelToFireTo).
//                    validateMe();
//            ((SolicitedReadPanel) panelToFireTo).
//                    doFirePropertyChange(fileError.getErrorMode(), oldValue,
//                    fileError.getErrorMessage());
//            return true;
//        } 
        
        return false;
    }    
}
