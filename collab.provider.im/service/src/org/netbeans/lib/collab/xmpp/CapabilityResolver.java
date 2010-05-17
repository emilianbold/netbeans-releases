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

import org.jabberstudio.jso.x.disco.*;


/**
 * Utility class that allows caching and retrieval of capabilities
 * disco results as recommended by JEP 115.  This can be used by
 * either servers or clients.
 */
public class CapabilityResolver extends Hashtable
{
    public CapabilityResolver()
    {
        super();
    }

    /**
     * max number of nodes you ever want to support
     * @param capacity
     */
    public CapabilityResolver(int capacity)
    {
        super(); // todo
    }

    /**
     * return the set of feature namespaces advertized for a given 
     * capabilities node.
     * @param capsNodeUri capabilities node uri
     * @return set of feature namespaces (as String)
     */
    public Set getFeatures(String capsNodeUri) {
        return (Set)get(capsNodeUri);
    }

    /**
     * fills a disco info response element with features known
     * for a given capabilities node.
     *
     * @param query empty query element to populate
     * @param capsNodeUri capabilities node uri
     *
     * @return true if the capabilities node provided is known
     */
    public boolean addFeatures(DiscoInfoQuery query,
                               String capsNodeUri) {
        Set ns = (Set)get(capsNodeUri);
        if (ns != null) {
            for (Iterator i = ns.iterator(); i.hasNext(); ) {
                query.addFeature((String)i.next());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * records features advertized for a given capabilities node.
     *
     * @param query disco#info query containing the features.
     * @param capsNodeUri capabilities node uri
     *
     */
    public void recordFeatures(DiscoInfoQuery query,
                               String capsNodeUri)
    {
        put(capsNodeUri, query.getFeatures());
    }

    /**
     * checks whether a feature was advertized for a given
     * capabilities node.
     *
     * @param feature feature namespace
     * @param capsNodeUri capabilities node uri
     *
     * @return true if the capabilities node provided matches
     * (i.e. implies support of) the given feature.
     */
    public boolean hasFeature(String capsNodeUri, String feature) {
        Set ns = (Set)get(capsNodeUri);
        if (ns != null) {
            return ns.contains(feature);
        }
        return false;
    }

}
