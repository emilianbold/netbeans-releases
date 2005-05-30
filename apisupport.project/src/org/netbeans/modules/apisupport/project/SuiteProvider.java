/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

/**
 * Interface to be implemented by projects which are part of Module Suite or are
 * Suite themselves.
 *
 * @see org.netbeans.api.project.Project#getLookup
 * @author Martin Krauskopf
 */
public interface SuiteProvider {
    
    String getSuiteDirectory();
}
