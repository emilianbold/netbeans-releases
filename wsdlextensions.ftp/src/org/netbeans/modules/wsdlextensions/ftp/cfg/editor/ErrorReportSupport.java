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

package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * ErrorReportSupport
 *    Convenient class to propagate the property change to the correct
 *    component that the framework is listening to.
 * 
 * @author Sun Microsystems
 */
public class ErrorReportSupport {
    public static boolean doFirePropertyChange(String name, Object oldValue,
            Object newValue, JComponent sourcePanel) {
        
        // We want to fire the property change event to the framework so the
        // message shows; however, we need to fire to the right panel that
        // the framework is listening to based on where the panels are plugged
        // in (ie WSDL Wizard or CASA as there are 2 separate interface)
        
        JComponent targetPanel = (InboundMessagePanel) SwingUtilities.
                    getAncestorOfClass(InboundMessagePanel.class, 
                    sourcePanel); 
        if (targetPanel != null) {
            ErrorDescription error =
                    ((InboundMessagePanel) targetPanel).validateMe();
            ((InboundMessagePanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        }
        
        targetPanel = (InboundTransferPanel) SwingUtilities.
                getAncestorOfClass(InboundTransferPanel.class, sourcePanel);
        
        if (targetPanel != null) {
            ErrorDescription error = ((InboundTransferPanel) targetPanel).
                    validateMe();
            ((InboundTransferPanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        }
        
        targetPanel = (OutboundMessagePanel) SwingUtilities.
                getAncestorOfClass(OutboundMessagePanel.class, sourcePanel);
        if (targetPanel != null) {
            ErrorDescription error = ((OutboundMessagePanel) targetPanel).
                    validateMe();
            ((OutboundMessagePanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        }   
        
        targetPanel = (OutboundTransferPanel) SwingUtilities.
                getAncestorOfClass(OutboundTransferPanel.class, sourcePanel);
        if (targetPanel != null) {
            ErrorDescription error = ((OutboundTransferPanel) targetPanel).
                    validateMe();
            ((OutboundTransferPanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        }          

        targetPanel = (FTPSettingsOneWayPanel) SwingUtilities.
                getAncestorOfClass(FTPSettingsOneWayPanel.class, sourcePanel);
        if (targetPanel != null) {
            ErrorDescription error = ((FTPSettingsOneWayPanel) targetPanel).
                    validateMe();
            ((FTPSettingsOneWayPanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        } 
        
        targetPanel = (FTPSettingsRequestResponseMessagePanel) SwingUtilities.
                getAncestorOfClass(FTPSettingsRequestResponseMessagePanel.class, sourcePanel);
        if (targetPanel != null) {
            ErrorDescription error = ((FTPSettingsRequestResponseMessagePanel) targetPanel).
                    validateMe();
            ((FTPSettingsRequestResponseMessagePanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        } 
        
        targetPanel = (FTPSettingsRequestResponseTransferPanel) SwingUtilities.
                getAncestorOfClass(FTPSettingsRequestResponseTransferPanel.class, sourcePanel);
        if (targetPanel != null) {
            ErrorDescription error = ((FTPSettingsRequestResponseTransferPanel) targetPanel).
                    validateMe();
            ((FTPSettingsRequestResponseTransferPanel) targetPanel).
                    doFirePropertyChange(error.getErrorMode(), oldValue,
                    error.getErrorMessage());
            return true;
        } 

        return false;
    }    
}
