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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.LinkedList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefGroupNode;
import org.netbeans.modules.j2ee.sun.share.configbean.J2EEBaseVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.J2EEVersion;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class EnvironmentView extends DDSectionNodeView {

    public EnvironmentView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp || rootDD instanceof SunApplicationClient)) {
            throw new IllegalArgumentException("Data object is not a root that contains top level reference elements (" + rootDD + ")");
        }

        LinkedList<SectionNode> children = new LinkedList<SectionNode>();
        children.add(new EjbRefGroupNode(this, rootDD, version));
        children.add(new ResourceRefGroupNode(this, rootDD, version));
        children.add(new ResourceEnvRefGroupNode(this, rootDD, version));
        if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(version) <= 0) {
            J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
            if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.JAVAEE_5_0) >= 0) {
                children.add(new MessageDestinationRefGroupNode(this, rootDD, version));
            }
        }
        setChildren(children);
    }

}
