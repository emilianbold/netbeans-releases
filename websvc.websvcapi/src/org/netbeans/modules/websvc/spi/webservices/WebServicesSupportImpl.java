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

package org.netbeans.modules.websvc.spi.webservices;

import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesSupportImpl {
	
	public void addServiceImpl(String serviceName, String serviceEndpoint, FileObject configFile);
	
    
	public FileObject getDD();

    /**
     *  Returns the directory that contains webservices.xml
     */
    public FileObject getWsDDFolder();

    /**
     * Returns the name of the implementation bean class
     * given the servlet-link or ejb-link name
     */
    public String getImplementationBean(String linkName);

    /**
     *  Given the servlet-link or ejb-link, remove the servlet or
     *  ejb entry in the module's deployment descriptor.
     */
    public void removeServiceEntry(String serviceName, String linkName);
	
	/**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();
	
}
