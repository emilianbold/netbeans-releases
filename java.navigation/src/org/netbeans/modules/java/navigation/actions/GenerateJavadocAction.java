/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * GenerateJavadocAction.java
 *
 * Created on September 24, 2004, 11:59 PM
 */

package org.netbeans.modules.java.navigation.actions;

import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;
import java.lang.UnsupportedOperationException;
import java.util.*;

/**
 * Generates a javadoc stub.
 *
 * @author Tim Boudreau
 */
public final class GenerateJavadocAction extends AbstractAction {
    
    //private final ClassMember member;

    /**
     * Creates a new instance of GenerateJavadocAction
     */
    public GenerateJavadocAction (/*ClassMember e*/) {
        // this.member = e;
        putValue ( NAME, NbBundle.getMessage ( GenerateJavadocAction.class,
                "LBL_GenJavadoc" ) ); //NOI18N
    }

    /** Renders template of Javadoc for asociated class member */
    public void actionPerformed (ActionEvent ae) {
        throw new UnsupportedOperationException( "Not rewritten to new Java Infrastructure" );
        /*
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
        */
    }

    public boolean isEnabled () {
        return false;
//        return member != null && member.getJavadoc () == null &&
//               member instanceof CallableFeature;
    }
    
}
