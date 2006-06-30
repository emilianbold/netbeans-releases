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

package org.netbeans.lib.ddl;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method.
*
* @author Slavek Psenicka
*/
public interface DDLCommand
{
    /** Returns specification (DatabaseSpecification) for this command */
    public DatabaseSpecification getSpecification();

    /** Returns name of modified object */
    public String getObjectName();

    /** Sets name to be used in command
    * @param name New name
    */
    public void setObjectName(String name);

    /** Executes command */
    public void execute() throws DDLException;

    /**
    * Returns full string representation of command. This string needs no 
    * formatting and could be used directly as argument of executeUpdate() 
    * command. Throws DDLException if format is not specified or CommandFormatter
    * can't format it (it uses MapFormat to process entire lines and can solve []
    * enclosed expressions as optional.
    */
    public String getCommand()
    throws DDLException;
    
    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException();

}
