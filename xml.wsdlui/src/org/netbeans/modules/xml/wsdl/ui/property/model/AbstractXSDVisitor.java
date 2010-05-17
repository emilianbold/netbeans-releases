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

package org.netbeans.modules.xml.wsdl.ui.property.model;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.FractionDigits;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.MaxLength;
import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.MinInclusive;
import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Selector;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 *
 * @author radval
 */
public abstract class AbstractXSDVisitor implements XSDVisitor {
    
    /** Creates a new instance of AbstractXSDVisitor */
    public AbstractXSDVisitor() {
    }

	public void visit(All all) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Annotation ann) {
		// TODO Auto-generated method stub
		
	}

	public void visit(AnyAttribute anyAttr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(AnyElement any) {
		// TODO Auto-generated method stub
		
	}

	public void visit(AppInfo appinfo) {
		// TODO Auto-generated method stub
		
	}

	public void visit(AttributeGroupReference agr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(AttributeReference reference) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Choice choice) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ComplexContent cc) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ComplexContentRestriction ccr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ComplexExtension ce) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Documentation d) {
		// TODO Auto-generated method stub
		
	}

	public void visit(ElementReference er) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Enumeration e) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Field f) {
		// TODO Auto-generated method stub
		
	}

	public void visit(FractionDigits fd) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalAttribute ga) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalAttributeGroup gag) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalComplexType gct) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalElement ge) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalGroup gd) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GlobalSimpleType gst) {
		// TODO Auto-generated method stub
		
	}

	public void visit(GroupReference gr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Import im) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Include include) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Key key) {
		// TODO Auto-generated method stub
		
	}

	public void visit(KeyRef kr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Length length) {
		// TODO Auto-generated method stub
		
	}

	public void visit(List l) {
		// TODO Auto-generated method stub
		
	}

	public void visit(LocalAttribute la) {
		// TODO Auto-generated method stub
		
	}

	public void visit(LocalComplexType type) {
		// TODO Auto-generated method stub
		
	}

	public void visit(LocalElement le) {
		// TODO Auto-generated method stub
		
	}

	public void visit(LocalSimpleType type) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MaxExclusive me) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MaxInclusive mi) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MaxLength ml) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MinExclusive me) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MinInclusive mi) {
		// TODO Auto-generated method stub
		
	}

	public void visit(MinLength ml) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Notation notation) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Pattern p) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Redefine rd) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Schema s) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Selector s) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Sequence s) {
		// TODO Auto-generated method stub
		
	}

	public void visit(SimpleContent sc) {
		// TODO Auto-generated method stub
		
	}

	public void visit(SimpleContentRestriction scr) {
		// TODO Auto-generated method stub
		
	}

	public void visit(SimpleExtension se) {
		// TODO Auto-generated method stub
		
	}

	public void visit(SimpleTypeRestriction str) {
		// TODO Auto-generated method stub
		
	}

	public void visit(TotalDigits td) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Union u) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Unique u) {
		// TODO Auto-generated method stub
		
	}

	public void visit(Whitespace ws) {
		// TODO Auto-generated method stub
		
	}

	    
}
