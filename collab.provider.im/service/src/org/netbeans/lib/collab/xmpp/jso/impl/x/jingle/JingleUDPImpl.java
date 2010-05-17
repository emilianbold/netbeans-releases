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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.outer_planes.jso.ElementNode;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleUDP;

/**
 *
 * @author jerry
 */
public class JingleUDPImpl  extends ElementNode implements JingleUDP {


    public JingleUDPImpl(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    public JingleUDPImpl(StreamElement parent, JingleUDP child){
        super(parent, (ElementNode)child);
    }
    public StreamObject copy(StreamElement parent){
        return new JingleUDPImpl(parent, this);
    }
    public void addCandidate(JingleCandidate candidate) {
        StreamElement c = addElement("candidate");
        c.setAttributeValue("ip", candidate.getIP());
        c.setAttributeValue("port", String.valueOf(candidate.getPort()));
        c.setAttributeValue("generation", String.valueOf(candidate.getGeneration()));
        c.setAttributeValue("name", candidate.getName());
    }

    public List listCandidates() {
        List l = listElements("candidate");
        ArrayList newlist = new ArrayList(l.size());
        for(ListIterator i = l.listIterator(); i.hasNext(); ){
            StreamElement elem = (StreamElement)i.next();
            JingleCandidate c = new JingleCandidate(elem);
            newlist.add(c);
        }
        return newlist;
    }
    
}
