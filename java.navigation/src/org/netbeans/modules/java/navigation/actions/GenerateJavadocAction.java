/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * GenerateJavadocAction.java
 *
 * Created on September 24, 2004, 11:59 PM
 */

package org.netbeans.modules.java.navigation.actions;

import org.netbeans.jmi.javamodel.*;
import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Generates a javadoc stub.
 *
 * @author Tim Boudreau
 */
public final class GenerateJavadocAction extends AbstractAction {
    private final ClassMember member;

    /**
     * Creates a new instance of GenerateJavadocAction
     */
    public GenerateJavadocAction (ClassMember e) {
        this.member = e;
        putValue ( NAME, NbBundle.getMessage ( GenerateJavadocAction.class,
                "LBL_GenJavadoc" ) ); //NOI18N
    }

    /** Renders template of Javadoc for asociated class member */
    public void actionPerformed (ActionEvent ae) {
        StringBuffer sb = new StringBuffer ( 80 );
        if ( member instanceof CallableFeature ) {
            // [dafe] space on following line is intentional! it fixes #52881
            sb.append ( " \n" ); //NOI18N
            CallableFeature cf = ((CallableFeature) member);
            for ( Iterator i = cf.getParameters ().iterator (); i.hasNext ();) {
                Parameter p = (Parameter) i.next ();
                sb.append ( " @param " ); //NOI18N
                sb.append ( p.getName () );
                sb.append ( " \n" );
            }
            Type t = cf.getType();
            if (t != null && !"void".equals(t.getName())) {
                sb.append ("@return ");
            }
            
            member.setJavadocText ( sb.toString () );
        }
        
    }

    public boolean isEnabled () {
        return member != null && member.getJavadoc () == null &&
               member instanceof CallableFeature;
    }
    
}
