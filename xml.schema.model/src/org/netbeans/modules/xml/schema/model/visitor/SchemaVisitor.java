/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.*;

/**
 * This interface represents a way for
 * @author Chris Webster
 */
public interface SchemaVisitor {
	void visit(All all);
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
	void visit(GlobalGroup gd);
	void visit(GroupReference gr);
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
