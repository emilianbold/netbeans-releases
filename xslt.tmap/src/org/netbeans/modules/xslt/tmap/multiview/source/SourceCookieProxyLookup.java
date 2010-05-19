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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.multiview.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
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
 * SourceCookieProxyLookup cpl = new SourceCookieProxyLookup(new Lookup[] {
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
 * @author Vitaly Bychkov
 * @author Nathan Fiedler
 * @version 1.0
 */
public class SourceCookieProxyLookup extends ProxyLookup
        implements PropertyChangeListener {

    /** Lookups excluding activated nodes and their lookups . */
    private Lookup[] lookups;
    /** The Node to which we delegate lookup for cookies. */
    private Node delegate;
    /** Signal that we are processing a property change event. */
    private boolean propertyChanging;

    /**
     * Creates a new instance of CookieProxyLookup.
     *
     * @param  lookups   the Lookup instances to which we proxy.
     * @param  delegate  the Node delegate from which cookies come.
     */
    public SourceCookieProxyLookup(Lookup[] lookups, Node delegate) {
        super();
        this.lookups = lookups;
        this.delegate = delegate;
        setLookups(new Lookup[]{
                new ProxyLookup(lookups),
                delegate.getLookup(),
                Lookups.singleton(delegate)
		});
    }

    public synchronized void propertyChange(PropertyChangeEvent event) {
        if (propertyChanging) {
            // Avoid an infinite loop whereby changing the lookup contents
			// causes the activated nodes to change, which calls us again.
            return;
        }
        propertyChanging = true;
        try {
            Node[] newNodes = (Node[]) event.getNewValue();
            if (newNodes == null || newNodes.length == 0) {
                setLookups(new Lookup[]{
                        new ProxyLookup(lookups),
                        new NoNodeLookup(delegate.getLookup()),
                        Lookups.singleton(delegate)
                    });
            } else {
                Lookup[] newNodeLookups = new Lookup[newNodes.length];
                for (int i=0; i < newNodes.length; i++) {
                    newNodeLookups[i] = new NoNodeLookup(newNodes[i].getLookup());
                }
                setLookups(new Lookup[]{
                        new ProxyLookup(lookups),
                        new ProxyLookup(newNodeLookups),
                        Lookups.fixed(newNodes)
                        });
            }
        } finally {
            propertyChanging = false;
        }
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

        @Override
	public <T> T lookup(Class<T> clazz) {
            return (clazz == Node.class) ? null : delegate.lookup(clazz);
        }

        @Override
	public <T> Result<T> lookup(Template<T> template) {
            if (template.getType() == Node.class) {
                return Lookup.EMPTY.lookup(template);
            }
            return delegate.lookup(template);
        }
    }
}
