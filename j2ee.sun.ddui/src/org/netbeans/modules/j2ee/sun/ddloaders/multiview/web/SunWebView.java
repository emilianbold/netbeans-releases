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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import java.util.LinkedList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class SunWebView extends SunWebBaseView {

    public SunWebView(SunDescriptorDataObject dataObject) {
        super(dataObject);

        LinkedList<SectionNode> children = new LinkedList<SectionNode>();
        if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(version) <= 0) {
            children.add(new SunWebDetailsNode(this, sunWebApp, version));
        }
        children.add(new SunWebClassLoaderNode(this, sunWebApp, version));
//        children.add(new SunWebJspConfigPropertyNode(this, sunWebApp, version));

        setChildren(children);
    }

}
