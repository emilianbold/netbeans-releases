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
package org.netbeans.modules.bpel.properties.editors.controls;

import javax.swing.JTextField;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.properties.ResolverUtility;

/**
 *
 * @author nk160297
 */
public class QNameIndicator extends JTextField {
    static final long serialVersionUID = -5818480577098689181L;

    private QName qName;

    public QNameIndicator() {
        super();
        setEditable(false);
    }

    public QName getQName() {
        return qName;
    }

    public void setQName(QName newValue) {
        qName = newValue;
        if ( qName != null ){
            String text = ResolverUtility.qName2DisplayText(qName);
            setText(text);
        }
    }
}
