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

/*
 * QueryConstraint.java
 *
 * Created on December 15, 2005, 11:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.ui;

/**
 *
 * @author Jeri Lockhart
 */
public interface QueryConstraint {

	public static enum Position {BEGINS_WITH, CONTAINS};
	
	/**
	 * Getter for property string.
	 * @return Value of property string.
	 */
	public String getStringConstraint();

	/**
	 * Setter for property string.
	 * @param string New value of property string.
	 */
	public void setStringConstraint(String string);

	/**
	 * Getter for property caseSensitive.
	 * @return Value of property caseSensitive.
	 */
	public boolean isCaseSensitive();

	/**
	 * Setter for property caseSensitive.
	 * @param caseSensitive New value of property caseSensitive.
	 */
	public void setCaseSensitive(boolean caseSensitive);

	/**
	 * Getter for property description.
	 * @return Value of property description.
	 */
	public String getDescription();
	/**
	 * Setter for property description.
	 * @param description New value of property description.
	 */
	public void setDescription(String description);
	
	
	/**
	 * Getter for property position.
	 * @return Value of property position.
	 */
	public Position getPosition();

	/**
	 * Setter for property position.
	 * @param position New value of property position.
	 */
	public void setPosition(Position position);
	
}
