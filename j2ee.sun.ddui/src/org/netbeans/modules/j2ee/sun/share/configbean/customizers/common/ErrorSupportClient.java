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
/*
 * ErrorSupportClient.java
 *
 * Created on November 14, 2003, 4:22 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface ErrorSupportClient {
    java.awt.Container getErrorPanelParent();
    java.awt.GridBagConstraints getErrorPanelConstraints();
    java.util.Collection getErrors();
	java.awt.Color getMessageForegroundColor();
}
