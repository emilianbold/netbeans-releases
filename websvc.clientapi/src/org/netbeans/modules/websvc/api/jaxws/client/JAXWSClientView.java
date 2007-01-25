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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.api.jaxws.client;

import java.util.Iterator;
import org.netbeans.modules.websvc.jaxws.client.JAXWSClientViewAccessor;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** JAXWSlientView should be used to display web service references in project.
 *  The wiew containes nodes representing web service references in project.
 * <p>
 * A client may obtain a JAXWClientView instance using 
 * <code>JAXWSClientView.getJAXWSClientView()</code> static 
 * method
 *
 * @author Peter Williams, Milan Kuchtiak  
 */
public final class JAXWSClientView {

	private JAXWSClientViewImpl impl;
	private static final Lookup.Result implementations =
		Lookup.getDefault().lookup(new Lookup.Template(JAXWSClientViewProvider.class));

	static  {
		JAXWSClientViewAccessor.DEFAULT = new JAXWSClientViewAccessor() {
			public JAXWSClientView createJAXWSClientView(JAXWSClientViewImpl spiWebServicesClientView) {
				return new JAXWSClientView(spiWebServicesClientView);
			}

			public JAXWSClientViewImpl getJAXWSClientViewImpl(JAXWSClientView wsv) {
				return wsv == null ? null : wsv.impl;
			}
		};
	}

	private JAXWSClientView(JAXWSClientViewImpl impl) {
		if (impl == null)
			throw new IllegalArgumentException ();
		this.impl = impl;
	}

	/** Lookup the IDE and find JAXWSClientView instance
     */
	public static JAXWSClientView getJAXWSClientView() {
		Iterator it = implementations.allInstances().iterator();
		while (it.hasNext()) {
			JAXWSClientViewProvider impl = (JAXWSClientViewProvider)it.next();
			JAXWSClientView wsv = impl.findJAXWSClientView ();
			if (wsv != null) {
				return wsv;
			}
		}

		JAXWSClientViewProvider impl = (JAXWSClientViewProvider) Lookup.getDefault().lookup(JAXWSClientViewProvider.class);
		if(impl != null) {
			JAXWSClientView wsv = impl.findJAXWSClientView();
			return wsv;
		}
		return null;
	}

	// Delegated methods from WebServicesClientViewImpl
    
	/** Create Node representing Web Service References for a given project
     *@param project project containing web service references (WS Clients) 
	*/ 
	public Node createJAXWSClientView(Project p) {
		return impl.createJAXWSClientView(p);
	}

}