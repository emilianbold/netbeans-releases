/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.dataloader;

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
		setLookups(new Lookup[] {
			new ProxyLookup(lookups), 
			delegate.getLookup(),
			Lookups.singleton(delegate),
		});
    }

    public synchronized void propertyChange(PropertyChangeEvent event) {
		if (propertyChanging)
		{
			// Avoid an infinite loop whereby changing the lookup contents
			// causes the activated nodes to change, which calls us again.
			return;
		}
		propertyChanging = true;
		try
		{
			Node[] newNodes = (Node[]) event.getNewValue();
			if(newNodes==null || newNodes.length==0)
			{
				setLookups(new Lookup[] {
					new ProxyLookup(lookups),
					new NoNodeLookup(delegate.getLookup()),
					Lookups.singleton(delegate),
				});
			}
			else
			{
				Lookup[] newNodeLookups = new Lookup[newNodes.length];
				for (int i=0;i<newNodes.length;i++)
				{
					newNodeLookups[i]=new NoNodeLookup(newNodes[i].getLookup());
				}
				setLookups(new Lookup[] {
					new ProxyLookup(lookups),
					new ProxyLookup(newNodeLookups),
					Lookups.fixed(newNodes),
				});
			}
		}
		finally
		{
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

