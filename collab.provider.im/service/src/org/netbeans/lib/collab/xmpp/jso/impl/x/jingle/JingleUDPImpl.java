/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 * 
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
