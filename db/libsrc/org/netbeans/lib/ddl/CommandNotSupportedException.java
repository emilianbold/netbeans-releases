/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.ddl;

/** Command is not supported by system.
* System is not able to locate appropriate resources to create a command.
* It can't find relevant section in definition file, can't allocate or
* initialize command object. 
*
* @author Slavek Psenicka
*/
public class CommandNotSupportedException extends Exception
{
    /** Unsuccessfull command */
    private String cmd;

    static final long serialVersionUID =3121142575910991422L;
    /** Creates new exception
    * @param command The text describing the exception
    */
    public CommandNotSupportedException (String command) {
        super ();
        cmd = command;
    }

    /** Creates new exception with text specified string.
    * @param command Executed command
    * @param desc The text describing the exception
    */
    public CommandNotSupportedException (String command, String desc) {
        super (desc);
        cmd = command;
    }

    /** Returns executed command */
    public String getCommand()
    {
        return cmd;
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
