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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.wadl.design.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * A ProxyLookup that changes the last delegate lookup to not return instances
 * of Node.class depending on changes to the "activatedNodes" property. The
 * value of this is that the delegate lookup comes from a DataObject Node
 * delegate, which presumably provides cookies such as SaveCookie. But we
 * do not want the Node delegate to return instances of Node via its Lookup
 * (specifically the Node delegate itself).
 *
 * <p>Usage, from within a <code>TopComponent</code> constructor:</p>
 *
 * <pre>
 * CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[] {
 *     lookup1,
 *     lookup2,
 *     // The Node delegate Lookup must be the last one in the list
 *     // for the CookieProxyLookup to work properly.
 *     delegate.getLookup(),
 * }, delegate);
 * associateLookup(cpl);
 * addPropertyChangeListener("activatedNodes", cpl);
 * </pre>
 *
 * @author Nathan Fiedler
 */
public class CookieProxyLookup extends ProxyLookup
        implements PropertyChangeListener {
    /** The Node to which we delegate lookup for cookies. */
    private Node delegate;
    /** Signal that we are processing a property change event. */
    private volatile boolean propertyChanging;

    /**
     * Creates a new instance of CookieProxyLookup.
     *
     * @param  lookups   the Lookup instances to which we proxy.
     * @param  delegate  the Node delegate from which cookies come.
     */
    public CookieProxyLookup(Lookup[] lookups, Node delegate) {
        super(lookups);
        this.delegate = delegate;
    }

    public synchronized void propertyChange(PropertyChangeEvent event) {
        if (propertyChanging) {
            // Avoid an infinite loop whereby changing the lookup contents
            // causes the activated nodes to change, which calls us again.
            return;
        }
        propertyChanging = true;
        try {
            Lookup[] lookups = getLookups();
            Node[] oldNodes = (Node[]) event.getOldValue();
            Node[] newNodes = (Node[]) event.getNewValue();
            Lookup lastLookup = lookups[lookups.length - 1];
            if (!(lastLookup instanceof NoNodeLookup) &&
                    (oldNodes.length >= 1) &&
                    (!oldNodes[0].equals(delegate))) {
                switchLookup();
            } else if ((lastLookup instanceof NoNodeLookup) &&
                    (newNodes.length == 0)) {
                switchLookup();
            }
        } finally {
            propertyChanging = false;
        }
    }

    /**
     * Switch out the last lookup in the proxy with a special NoNodeLookup
     * that delegates to our Node delegate's lookup.
     */
    private void switchLookup() {
        Lookup[] lookups = getLookups();
        Lookup nodeLookup = delegate.getLookup();
        int index = lookups.length - 1;
        if (lookups[index] instanceof NoNodeLookup) {
            lookups[index] = nodeLookup;
        } else {
            lookups[index] = new NoNodeLookup(nodeLookup);
        }
        setLookups(lookups);
    }

    /*
     * Lookup that excludes nodes. Needed for use with instanceof in the
     * property change listener.
     */
    private static class NoNodeLookup extends Lookup {
        private final Lookup delegate;

        public NoNodeLookup(Lookup delegate) {
            this.delegate = delegate;
        }

        public Object lookup(Class clazz) {
            return (clazz == Node.class) ? null : delegate.lookup(clazz);
        }

        public Lookup.Result lookup(Lookup.Template template) {
            if (template.getType() == Node.class) {
                return Lookup.EMPTY.lookup(new Lookup.Template(Node.class));
            } else {
                return delegate.lookup(template);
            }
        }
    }
}
