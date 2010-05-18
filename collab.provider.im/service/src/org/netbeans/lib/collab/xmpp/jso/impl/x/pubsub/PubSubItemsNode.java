/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab.xmpp.jso.impl.x.pubsub;

import java.util.*;

import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamDataFactory;

import net.outer_planes.jso.ElementNode;

import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubItems;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEvent;



/**
 *
 */
public class PubSubItemsNode extends ElementNode implements PubSubItems {


	private static final String ATTR_ID = "id";
	private static final NSI NSI_ID = new NSI(ATTR_ID,PubSubEvent.NAMESPACE);
	private static final NSI NSI_ITEM = new NSI("item",PubSubEvent.NAMESPACE);
	private static final NSI NSI_RETRACT = new NSI("retract",PubSubEvent.NAMESPACE);

	public PubSubItemsNode(StreamDataFactory sdf) {
		super(sdf,NAME);
	}

	public PubSubItemsNode(StreamElement parent, PubSubItemsNode base) {
		super(parent, base);
	}

	public void setNodeIdentifier(String nodeValue) throws IllegalArgumentException {
		setAttributeValue("node",nodeValue);
	}

	public String getNodeIdentifier() {
		return getAttributeValue("node");
	}
	
	public StreamElement addPubSubItem(String id) {
		StreamElement elem = createPubSubItem(id);
		add(elem);
                return elem;
	}

	public StreamElement addPubSubItem(String id, String content) {
		StreamElement elem = createPubSubItem(id);
		elem.addText(content);
		add(elem);
                return elem;
	}

        public StreamElement addPubSubItem(String id, StreamElement el) {
                StreamElement elem = createPubSubItem(id);
                elem.add(el);
                add(elem);
                return elem;
        }

	public void removePubSubItem(String id) {
		StreamElement item = getPubSubItem(id);
		if (item != null) {
			remove(item);
		}
	}

	public List listPubSubItems() {
		List l = new ArrayList();
		for( Iterator i = listElements("item").iterator(); i.hasNext();) {
			StreamElement item = (StreamElement)i.next();
			l.add(item);
		}
		return l;
	}
	
	public void addPubSubRetractItem(String id) {
		StreamElement elem = createPubSubRetractItem(id);
		add(elem);
	}

	public StreamElement createPubSubItem(String itemId) {
		StreamElement item = getDataFactory().createElementNode(NSI_ITEM);
		item.setAttributeValue(ATTR_ID,itemId);
		return item;
	}
	
	public StreamElement createPubSubRetractItem(String itemId) {
		StreamElement item = getDataFactory().createElementNode(NSI_RETRACT);
		item.setAttributeValue(ATTR_ID,itemId);
		return item;
	}

	public StreamElement getPubSubItem(String id) {
		for (Iterator i = listElements("item").iterator(); i.hasNext();) {
			StreamElement item = (StreamElement)i.next();
			if (item.getAttributeValue(ATTR_ID).equals(id)) {
				return item;
			}
		}
		return null;
	}
	
	public StreamElement getPubSubRetractItem() {
		return getFirstElement(NSI_RETRACT); 
	}

	public boolean hasPubSubRetractItem() {
		return getFirstElement(NSI_RETRACT) != null;
	}

	public void clearPubSubItems() {
		for(Iterator i = listElements("item").iterator();i.hasNext();) {
			StreamElement item = (StreamElement)i.next();
			remove(item);
		}
	}

	public StreamObject copy(StreamElement parent) {
		return new PubSubItemsNode(parent,this);
	}

}

