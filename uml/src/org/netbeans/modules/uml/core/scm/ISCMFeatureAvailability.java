/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.core.scm;

public interface ISCMFeatureAvailability
{
	/**
	 * Determines whether or not the passed in feature is available given the element.
	*/
	public boolean isFeatureAvailable( /* SCMFeatureKind */ int kind, String fileName );

   /**
	 * Executes the feature on the passed in files.  The SCM UI will be displayed.
    *
    * @param kind The type of feature to execute.  The value of kind must be one
    *             of the SCMFeatureKind values.
    * @param group The SCM group to execute on.
	 */
	public void executeFeature( /* SCMFeatureKind */ int kind, ISCMItemGroup Group );

   /**
	 * Executes the feature on the passed in files.
    *
    * @param kind The type of feature to execute.  The value of kind must be one
    *             of the SCMFeatureKind values.
    * @param group The SCM group to execute on.
    * @param showGUI determines if the action is executed silently or not.
	 */
	public void executeFeature( /* SCMFeatureKind */ int kind, ISCMItemGroup Group, boolean showGUI );

   /**
	 * Executes the feature on the passed in files.  The SCM UI will be displayed.
    *
    * @param kind The type of feature to execute.  The value of kind must be one
    *             of the SCMFeatureKind values.
    * @param group The SCM group to execute on.
    * @param options The SCM options to use.
	 */
	public void executeFeature( /* SCMFeatureKind */ int kind, ISCMItemGroup Group, ISCMOptions pOptions );

	/**
	 * Executes the feature on the passed in files.
    *
    * @param kind The type of feature to execute.  The value of kind must be one
    *             of the SCMFeatureKind values.
    * @param group The SCM group to execute on.
    * @param options The SCM options to use.
    * @param showGUI determines if the action is executed silently or not.
	 */
	public void executeFeature( /* SCMFeatureKind */ int kind, ISCMItemGroup Group, ISCMOptions pOptions, boolean showGUI );

	/**
	 * Retrieves an explanation on why the last call to IsFeatureAvailable returned a false result. If status has no length, no error status is available.
	*/
	public String getLastFeatureStatus();

}
