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

package org.netbeans.lib.collab.xmpp;

import java.util.*;
import org.jabberstudio.jso.JID;


import org.netbeans.lib.collab.*;

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class XMPPPersonalGateway extends XMPPPersonalStoreEntry implements PersonalGateway {

    private String _service;
    private boolean _registered;

    private Set _features = new TreeSet();

	//public static final String GATEWAY_FOLDER = "Transports";
	public static final String GATEWAY_FOLDER = "";

    public XMPPPersonalGateway(XMPPSession s, String name, String jid) {
        super(s,name,PersonalStoreEntry.GATEWAY,jid);
    }
    
    /** Creates a new instance of XMPPPersonalStoreGateway */
    public XMPPPersonalGateway(XMPPSession s, String name, String jid, String service) {
        super(s,name,PersonalStoreEntry.GATEWAY,jid);
        _service = service;
    }
    
    public String getHostName() throws CollaborationException {
        return getEntryId();
    }
    
    public String getName() throws CollaborationException {
        return getDisplayName();
    }
    
    public String getService() throws CollaborationException {
        return _service;
    }
    
    public void setService(String service) throws CollaborationException {
        _service = service;
    }
    
    
    public void register(RegistrationListener listener) throws CollaborationException {
        XMPPRegistrationListenerWrapper regisListenerWrapper = new XMPPRegistrationListenerWrapper(listener);
        regisListenerWrapper.setRequestType(XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION);
        _session.register(getHostName(), regisListenerWrapper);        
    }
    
    
    public void unregister(RegistrationListener regisListener) throws CollaborationException {        
        XMPPRegistrationListenerWrapper regisListenerWrapper = new XMPPRegistrationListenerWrapper(regisListener);      
        regisListenerWrapper.setRequestType(XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION);
        _session.unregister(new JID(getHostName()), regisListenerWrapper);
        // remove all the legacy users from the roster
        XMPPPersonalStoreService pss = 
            (XMPPPersonalStoreService)_session.getPersonalStoreService();
        for (Iterator i = pss.getEntries(PersonalStoreEntry.CONTACT).iterator();
                                                        i.hasNext();) 
        {
            PersonalContact pc = (PersonalContact)i.next();
            String domain = pc.getPrincipal().getDomainName();             
            if (domain != null && domain.equalsIgnoreCase(_jid)) {
                pc.remove();
            }                                       
        }   
        _registered = false;
    }
    
    public void addSupportedFeature(String feature) throws CollaborationException {
        _features.add(feature);
    }
    
    public Set getSupportedFeatures() throws CollaborationException {
        return _features;
    }
    
    public boolean isSupportedFeature(String feature) throws CollaborationException {
        return _features.contains(feature);
    }
    
    public void removeSupportedFeature(String feature) throws CollaborationException {
        _features.remove(feature);
    }
    
    public boolean isRegistered() throws CollaborationException {
        PersonalStoreService pss = (PersonalStoreService)_session.getPersonalStoreService();
        return pss.getEntry(PersonalStoreEntry.GATEWAY, getHostName()) != null;
    }    
    
}
