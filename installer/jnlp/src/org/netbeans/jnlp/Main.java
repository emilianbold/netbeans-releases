/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Nokia. Portions Copyright 2003 Nokia.
 * All Rights Reserved.
 */
package org.netbeans.jnlp;

/**
 * Just start core main. Note: there is another main
 * in core/bootstrap. I hope that the one from core/src is fine.
 * @author David Strupl
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        org.netbeans.core.startup.Main.main(args);
    }
    
}
