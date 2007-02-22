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

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.xam.Nameable;

/**
 *
 * @author radval
 *
 */
public class SchemaElementMinMaxOccursFinderVisitor extends AbstractXSDVisitor {

        private Logger mLogger = Logger.getLogger(SchemaElementAttributeFinderVisitor.class.getName());

	private int mMinOccurs;
	
	private int mMaxOccurs;
	
	public int getMinOccurs() {
		return this.mMinOccurs;
	}
	
	public int getMaxOccurs() {
		return this.mMaxOccurs;
	}
	
	public void visit(GlobalElement ge) {
            this.mMinOccurs = 1;
            this.mMaxOccurs = Integer.MAX_VALUE;
        
       }
	

	
       public void visit(LocalElement ge) {
            this.mMinOccurs = ge.getMinOccursEffective();
            try {
                String maxOccurs = ge.getMaxOccursEffective();
                this.mMaxOccurs = Integer.parseInt(maxOccurs);
            } catch(Exception ex) {
                mLogger.log(Level.SEVERE, "failed to find maxoccurs for " + ((Nameable)ge).getName(), ex);
            }
       }
	
}
