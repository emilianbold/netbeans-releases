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

package org.netbeans.modules.xml.xam.ui.customizer;

/**
 *
 * @author Ajit Bhate
 */
public interface MessageDisplayer {

	public enum Type {INFO,WARNING,ERROR}
 
	/**
     * This api adds a new message to message displayer at the end.
     */
    public void annotate(String message, Type type) ;

    /**
     * This api discards any messages in message displayer.
     */
    public void clear();

    /**
     * This api returns the message displayer ui component
     */
    public javax.swing.JComponent getComponent();

}
