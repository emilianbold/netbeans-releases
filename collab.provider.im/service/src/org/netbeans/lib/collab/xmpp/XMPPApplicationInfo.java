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

import org.netbeans.lib.collab.ApplicationInfo;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.disco.*;

/**
 * xmpp-specific extension of ApplicationInfo.  Its rationale is
 * to provide a mechanism to automatically build capabilities 
 * advertisement element (included in published presence updates).
 *
 * The api has no or limited knowledge of what is a base feature
 * and what is an optional extension.  Therefore for any feature
 * added, a feature bundle (i.e. an ext value) is created. the ext
 * value is derived from the feature url using a hacky mechanism. 
 *
 * Note: We should
 * at some point extend applicationInfo to allow the application to
 * define multi-features caps bundles. 
 */
public class XMPPApplicationInfo extends ApplicationInfo
{

    private HashMap _capsExtensions = new HashMap();
    private StreamElement _capsElement = null;
    private boolean _dirty = true;
    private boolean _capsSupported = true;

    public XMPPApplicationInfo() {
        super();
        _capsElement = XMPPSession._sdf.createElementNode(XMPPSessionProvider.CAPS_NSI);
        setCategory("client");
        setType("pc");
        setName("collab.netbeans.org");
        setVersion("1.0");
        addFeature(XMPPSessionProvider.CAPS_NAMESPACE);
    }

    public XMPPApplicationInfo(ApplicationInfo ai) {
        super();
        _capsElement = XMPPSession._sdf.createElementNode(XMPPSessionProvider.CAPS_NSI);
        update(ai);
        addFeature(XMPPSessionProvider.CAPS_NAMESPACE);
    }

    void update(ApplicationInfo ai) {
        setCategory(ai.getCategory());
        setType(ai.getType());
        for (Iterator i = ai.getFeatures().iterator(); i.hasNext(); ) {
            super.addFeature((String)i.next());
        }
        resetCapabilities();
    }

    public void setVersion(String version) {
        super.setVersion(version);
        resetCapabilities();
    }
    
    public void addFeature(String feature) {
        super.addFeature(feature);
        if (!isBaseCapability(feature)) {
            _capsExtensions.put(makeupExt(feature), feature);
            resetCapabilities();
        }
    }

    private boolean isBaseCapability(String feature) {
        // there may be other base features
        return (XMPPSessionProvider.CAPS_NAMESPACE.equals(feature));
    }

    void supportsCaps(boolean b) {
        _capsSupported = b;
    }

    public void removeFeature(String feature){
        super.removeFeature(feature);
        _capsExtensions.remove(makeupExt(feature));
    }

    private static final String JABBER_PROTOCOL_PREFIX =
        "http://jabber.org/protocol/";

    // This is a quick hack to come up with an ext value
    // that is not too large and still makes some sense.
    // the latter is not a requirement.  Just a nice debugging
    // aid
    private String makeupExt(String ns) {
        if (ns.startsWith(JABBER_PROTOCOL_PREFIX)) {
            return ns.substring(JABBER_PROTOCOL_PREFIX.length());
        } else {
            return removeNonAlphaChars(ns);
        }
    }

    private String removeNonAlphaChars(String ns) {
        StringBuffer b = new StringBuffer(ns.length());
        char[] c = ns.toLowerCase().toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (Character.isLetterOrDigit(c[i])) b.append(c[i]);
        }
        return b.toString();
    }

    /**
     * returns the current capabilities element, derived from 
     * declared features
     * @param onlyIfChanged if this is true, null is returned unless
     *     features have been changed since the last access.
     *     This is used in order to avoid publishing caps in every
     *     presence update.
     * @return caps extension element.
     */
    StreamElement getCapabilities(boolean onlyIfChanged) {
        if (!_capsSupported) return null;
        if (_dirty || !onlyIfChanged) {
            if (_dirty) _dirty = false;
            return _capsElement;
        } else {
            return null;
        }
    }

    private void resetCapabilities()
    {
        StringBuffer exts = new StringBuffer();
        boolean first = true;
        for (Iterator i = _capsExtensions.keySet().iterator(); i.hasNext(); ) {
            String ns = (String)i.next();
            if (!first) exts.append(" ");
            exts.append(ns);
            first = false;
        }
            
        if (exts.length() > 0) {
            _capsElement.setAttributeValue("node", 
                                           XMPPSessionProvider.CAPS_NODE);
            _capsElement.setAttributeValue("ver", getVersion());
            _capsElement.setAttributeValue("ext", exts.toString());
        }

        _dirty = true;
    }

    void fillDiscoResponse(DiscoInfoQuery query, String node) {
        String feature = null;

        // figure out the node
        if (node != null &&
            node.startsWith(XMPPSessionProvider.CAPS_NODE + "#")) {
            feature = (String)_capsExtensions.get(node.substring(XMPPSessionProvider.CAPS_NODE.length() + 1));
            if (feature != null) {
                query.addFeature(feature);
            } else if (node.equals(XMPPSessionProvider.CAPS_NODE + "#" + getVersion())) {
                query.addFeature(XMPPSessionProvider.CAPS_NAMESPACE);
                // there should probably be other base features
            }
        } else {
            for(Iterator i = getFeatures().iterator(); i.hasNext(); ) {
                query.addFeature((String)i.next());
            }
        }
    }

    
}
