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

package org.netbeans.modules.websvc.api.registry;

import java.beans.PropertyChangeListener;

import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;

/** Retrieve an implementation of this instance via Global Lookup to obtain
 *  access to the registry of web services within the IDE.
 *
 *  !! This is not a supported API.  Rather, it exists in it's current form
 *     to allow other webservices modules to utilize the registry.  It should
 *     be redone to have a service interface and iterate/search through the
 *     registered services.  It will also need to allow external modules to
 *     register a WSDL, bypassing the UI, but doing the rest.
 *
 * @author Peter Williams
 */
public interface WebServicesRegistryView {
	
	/** Retrieve the root node of the registry view on the runtime tab.
	 *
	 *  !PW used by the webservice core to relate the client view to the registered
	 *      services.  Not stable, as this will be replaced with a search mechanism.
	 */
	public Node getRegistryRootNode();
	

	/** Retrieve the node(s) representing the services present in the specified
	 *  wsdl file.
	 *
	 * !PW we may need to add the name of the WSDL file here, or other identifying
	 *     information, as the name of the registered service can be changed by
	 *     the user and is not necessarily present in the WSDL file that was originally
	 *     installed.  For now, we'll assume the WSDL file has only one service
	 *     and that the name has not been changed.
	 */
	public Node[] getWebServiceNodes(FileObject wsdlFile);
	
	
	/** Checks to see if the service specified has been registered.
	 *
	 * !PW This assumes that the serviceName and displayName of the installed
	 *     service (if any) match.  This is the default, but the user is allowed
	 *     to change the name of an installed service added manually so we will
	 *     need to enhance the backend and possible this method to allow better
	 *     matching.
	 */
	public boolean isServiceRegistered(String serviceName);
	
	
	/** Registers the services in the specified WSDL file.
	 *
	 * !PW the user can change the package in the project view, but what about
	 *     the name, as displayed in the registry?  Maybe add a 'Rename...'
	 *     action to service nodes.
	 *
	 * !PW what exception(s) should this throw if any?
	 */
	public boolean registerService(FileObject wsdlFile, boolean replaceService);
	
	
	/** Registers the service running on the specifiedl URL.  All this means is
	 *  that the implementation will access [wsdlUrl]?WSDL and process the result
	 *  as if it were a WSDL file (which it will be if this is a valid service.)
	 *
	 * !PW the user can change the package in the project view, but what about
	 *     the name, as displayed in the registry?  Maybe add a 'Rename...'
	 *     action to service nodes.
	 *
	 * !PW what exception(s) should this throw if any?
	 */
	public boolean registerService(java.net.URL wsdlUrl, boolean replaceService);

	/** Event notification of services being added and removed.
	 *
	 * !PW Probably should define my own event harness.
	 */
	public static final String WEB_SERVICE_ADDED = "webServiceAdded";
	public static final String WEB_SERVICE_REMOVED = "webServiceRemoved";
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
