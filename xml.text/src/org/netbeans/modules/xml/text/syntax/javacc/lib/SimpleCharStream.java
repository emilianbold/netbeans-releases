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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * JavaCC 3.2 generates TokenManagers that use SimpleCharStream
 * instead of CharStream interface.
 *
 * @see https://javacc.dev.java.net/issues/show_bug.cgi?id=77
 * @author Petr Kuzel
 */
public abstract class SimpleCharStream implements CharStream {

    public static final boolean staticFlag = false;    
}
