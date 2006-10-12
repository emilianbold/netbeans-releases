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
/*
 * UserCatalogOperator.java
 *
 * Created on September 19, 2006, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.jellytools.modules.xml.catalog.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author jindra
 */
public class UserCatalogOperator extends NbDialogOperator {
    
    private JTextFieldOperator _txtPublic;
    private JTextFieldOperator _txtUri;
    private JButtonOperator _btBrowse;
    private JRadioButtonOperator _rbPublic;
    
    /** Creates a new instance of UserCatalogOperator */
    public UserCatalogOperator() {
        super("Register");
        
    }
    
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }
    
    public JRadioButtonOperator rbPublic() {
        if (_rbPublic==null) {
            _rbPublic = new JRadioButtonOperator(this, "Public ID:");
        }
        return _rbPublic;
    }

    public JTextFieldOperator txtPublic() {
        if (_txtPublic==null) {
            _txtPublic = new JTextFieldOperator(this, "");
        }
        return _txtPublic;
    }

    public JTextFieldOperator txtUri() {
        if (_txtUri==null) {
            _txtUri = new JTextFieldOperator(this, 2);
        }
        return _txtUri;
    }
    
}
