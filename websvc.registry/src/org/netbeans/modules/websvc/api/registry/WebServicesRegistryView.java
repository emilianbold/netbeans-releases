/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
