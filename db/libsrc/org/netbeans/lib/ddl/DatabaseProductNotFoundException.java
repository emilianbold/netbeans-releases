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
* System is not able to locate appropriate resources to create DatabaseSpecification object
* (object describing the database). It means that database product is not
* supported by system. You can use generic database system or write your
* own description file. If you are sure that it is, please check location
* of description files.
*
* @author Slavek Psenicka
*/
public class DatabaseProductNotFoundException extends Exception
{
    /** Database product name */
    private String sname;

    static final long serialVersionUID =-1108211224066947350L;
    /** Creates new exception
    * @param desc The text describing the exception
    */
    public DatabaseProductNotFoundException (String spec) {
        super ();
        sname = spec;
    }

    /** Creates new exception with text specified string.
    * @param spec Database product name
    * @param desc The text describing the exception
    */
    public DatabaseProductNotFoundException (String spec, String desc) {
        super (desc);
        sname = spec;
    }

    /** Returns database product name.
    * This database is not supported by system. You can use generic database 
    * system or write your own description file.
    */
    public String getDatabaseProductName()
    {
        return sname;
    }
}

/*
 * <<Log>>
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
 *  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
