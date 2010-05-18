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

package org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub;

import org.jabberstudio.jso.util.Enumerator;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;

/**
 *
 */
public interface PubSubEntityElement extends PubSubElement {

    public static final NSI NAME = new NSI("entity",PubSubQuery.NAMESPACE);

    public static final class SubscriptionStatus extends Enumerator {
	
        protected SubscriptionStatus(String status) {
            super(status);
        }

        public boolean equals(Object cmp) {
            if (cmp instanceof SubscriptionStatus) {
                return toString().equals(cmp.toString());
            } else if (cmp instanceof String) {
                return toString().equals((String)cmp);
            } else {
                return false;
            }
        }
        
    }

    public static final class Affiliation extends Enumerator {
        
        protected Affiliation(String affiliation) {
            super(affiliation);
        }
        
        public boolean equals(Object cmp) {
            if (cmp instanceof Affiliation) {
                return toString().equalsIgnoreCase(cmp.toString());
            } else if (cmp instanceof String) {
                return toString().equals((String)cmp);
            } else {
                return false;
            }
        }        
    }


    public static final SubscriptionStatus PENDING = new SubscriptionStatus("pending");
    public static final SubscriptionStatus SUBSCRIBED = new SubscriptionStatus("subscribed");
    public static final SubscriptionStatus UNCONFIGURED = new SubscriptionStatus("unconfigured");
    public static final SubscriptionStatus NONE = new SubscriptionStatus("none");


    public static final Affiliation OWNER = new Affiliation("owner");
    public static final Affiliation PUBLISHER = new Affiliation("publisher");
    public static final Affiliation OUTCAST = new Affiliation("outcast");
    public static final Affiliation AFFIL_NONE = new Affiliation("none");

	
    public JID getSubscriberJID();
    
    
    public void setSubscriberJID(JID jid);
    
    
    public void setSubscriptionStatus(SubscriptionStatus subStatus) throws IllegalArgumentException;
    
    
    public SubscriptionStatus getSubscriptionStatus() throws IllegalArgumentException;
    
    
    public boolean hasSubscriptionStatus(SubscriptionStatus s);
    
    
    public void setAffiliation(Affiliation aff) throws IllegalArgumentException;
    
    
    public Affiliation getAffiliation() throws IllegalArgumentException;
    
    
    public boolean hasAffiliation(Affiliation a);

}
