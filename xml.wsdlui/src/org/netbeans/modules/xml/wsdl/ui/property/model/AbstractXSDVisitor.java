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
