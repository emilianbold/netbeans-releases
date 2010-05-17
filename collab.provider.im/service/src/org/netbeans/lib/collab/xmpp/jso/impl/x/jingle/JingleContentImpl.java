/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.collab.xmpp.jso.impl.x.jingle;

import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleContent;

/**
 *
 * @author jerry
 */
public class JingleContentImpl extends ExtensionNode implements JingleContent {

    public static final String ATTR_NAME = "name";
    public static final String ATTR_CREATOR="creator";
    public JingleContentImpl(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    public JingleContentImpl(StreamElement parent, JingleContent child){
        super(parent, (ExtensionNode)child);
    }
     public StreamObject copy(StreamElement parent){
        return new JingleContentImpl(parent, this);
    }

    public void setName(String name) {
        setAttributeValue(ATTR_NAME, name);
    }

    public String getName() {
        return getAttributeValue(ATTR_NAME);
    }

    public void setCreator(String creator) {
        setAttributeValue(ATTR_CREATOR, creator);
    }

    public String getCreator() {
        return getAttributeValue(ATTR_CREATOR);
    }

    
 
}
