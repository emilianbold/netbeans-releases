/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.parsing.impl;

import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private static DocumentListener documentListener;
    
    public void restored () {
        System.out.println("\nIntaller.restored");
        documentListener = new DocumentListener ();
    }
    
    public void close () {
        System.out.println("\nIntaller.close");
        documentListener.remove ();
    }
}
