/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.*;

/**
 * This interface represents a way for 
 * @author Chris Webster
 */
public interface SchemaVisitor {
	void visit(All all);
	void visit(AllElement allElement);
	void visit(AllElementReference allElementReference);
	void visit(Annotation ann);
	void visit(AnyElement any);
	void visit(AnyAttribute anyAttr);
        void visit(AppInfo appinfo);
	void visit(AttributeReference reference);
	void visit(AttributeGroupReference agr);
	void visit(Choice choice);
	void visit(ComplexContent cc);
	void visit(ComplexContentRestriction ccr);
	void visit(ComplexExtension ce);
	void visit(Documentation d);
	void visit(ElementReference er);
	void visit(Enumeration e);
	void visit(Field f);
	void visit(FractionDigits fd);
	void visit(GlobalAttribute ga);
	void visit(GlobalAttributeGroup gag);
	void visit(GlobalComplexType gct);
	void visit(GlobalElement ge);
	void visit(GlobalSimpleType gst);
	void visit(GroupAll ga);
	void visit(GroupChoice gc);
	void visit(GlobalGroup gd);
	void visit(GroupReference gr);
	void visit(GroupSequence gs);
	void visit(Import im);
	void visit(Include include);
	void visit(Key key);
	void visit(KeyRef kr);
	void visit(Length length);
	void visit(List l);
	void visit(LocalAttribute la);
	void visit(LocalComplexType type);
	void visit(LocalElement le);
	void visit(LocalSimpleType type);
	void visit(MaxExclusive me);
	void visit(MaxInclusive mi);
	void visit(MaxLength ml);
	void visit(MinInclusive mi);
	void visit(MinExclusive me);
	void visit(MinLength ml);
        void visit(Notation notation);
	void visit(Pattern p);
	void visit(Redefine rd);
	void visit(Schema s);
        void visit(Selector s);
	void visit(Sequence s);
	void visit(SimpleContent sc);
	void visit(SimpleContentRestriction scr);
	void visit(SimpleExtension se);
	void visit(SimpleTypeRestriction str);
	void visit(TotalDigits td);
	void visit(Union u);
	void visit(Unique u);
	void visit(Whitespace ws);
}
