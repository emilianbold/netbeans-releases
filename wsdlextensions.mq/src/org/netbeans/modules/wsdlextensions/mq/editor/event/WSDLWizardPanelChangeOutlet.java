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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor.event;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;

/**
 * ChangeOutlet for (to) WSDLWizardDescriptorPanels.
 *
 * @author Noel.Ang@sun.com
 * @see {@link org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel}
 */
public class WSDLWizardPanelChangeOutlet
        implements ChangeOutlet {

    public WSDLWizardPanelChangeOutlet(WSDLWizardDescriptorPanel descriptor) {
        this.descriptor = descriptor;
    }

    public void notifyChange(Object context) {
        descriptor.fireChange();
    }

    private final WSDLWizardDescriptorPanel descriptor;
}
