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
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class MessageDrivenNode extends EjbNode {

    MessageDrivenNode(SectionNodeView sectionNodeView, MessageDriven messageDriven) {
        super(sectionNodeView, messageDriven, Utils.ICON_BASE_MESSAGE_DRIVEN_NODE);
        addChild(new MessageDrivenOverviewNode(sectionNodeView, messageDriven));
        addChild(new MdbImplementationNode(sectionNodeView, messageDriven));
        addChild(new BeanEnvironmentNode(sectionNodeView, messageDriven));
        addChild(new BeanDetailNode(sectionNodeView, messageDriven));
    }
}
