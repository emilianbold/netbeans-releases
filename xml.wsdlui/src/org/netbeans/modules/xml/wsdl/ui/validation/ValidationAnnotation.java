/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * Created on Mar 22, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ValidationAnnotation extends Annotation implements PropertyChangeListener {
	
	/** The error message shown on mouseover on the pmd icon */
	private String errormessage = null;
	
	/** The annotations currently existing. */
	private static List annotations = new ArrayList();
	
	private ValidationAnnotation() {}
	
	public static final ValidationAnnotation getNewInstance() {
		ValidationAnnotation va = new ValidationAnnotation();
		annotations.add( va );
		return va;
	}
	
	public static final void clearAll() {
		Iterator iterator = annotations.iterator();
		while( iterator.hasNext() ) {
			((Annotation)iterator.next()).detach();
		}
		annotations.clear();
	}
	
	/**
	 * The annotation type.
	 *
	 * @return the string "wsdl-validation-annotation"
	 */
	@Override
	public String getAnnotationType() {
		return "org-netbeans-modules-xml-error"; //NOI18N
	}
	
	
	/**
	 * Sets the current errormessage
	 *
	 * @param message the errormessage
	 */
	public void setErrorMessage( String message ) {
		errormessage = message;
	}
	
	
	/**
	 * A short description of this annotation
	 *
	 * @return the short description
	 */
	@Override
	public String getShortDescription() {
		return errormessage;
	}
	
	
	/**
	 * Invoked when the user change the content on the line where the annotation is
	 * attached
	 *
	 * @param propertyChangeEvent the event fired
	 */
	public void propertyChange( PropertyChangeEvent propertyChangeEvent ) {
		Line line = ( Line )propertyChangeEvent.getSource();
		line.removePropertyChangeListener( this );
		detach();
	}
	
}

