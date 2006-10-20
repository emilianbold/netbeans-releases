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

package org.netbeans.modules.web.jsps.parserapi;

import java.util.*;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.JspException;

/**
 * This class defines internal representation for an EL Expression
 *
 * It currently only defines functions.  It can be expanded to define
 * all the components of an EL expression, if need to.
 */

public abstract class ELNode {

    public abstract void accept(Visitor v) throws JspException;

    /**
     * Child classes
     */


    /**
     * Represents an EL expression: anything in ${ and }.
     */
    public static class Root extends ELNode {

	private ELNode.Nodes expr;

	public Root(ELNode.Nodes expr) {
	    this.expr = expr;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public ELNode.Nodes getExpression() {
	    return expr;
	}
    }

    /**
     * Represents text outside of EL expression.
     */
    public static class Text extends ELNode {

	private String text;

	public Text(String text) {
	    this.text = text;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public String getText() {
	    return text;
	}
    }

    /**
     * Represents anything else EL expression, including function arguments etc
     */
    public static class ELText extends ELNode {

	private String text;

	public ELText(String text) {
	    this.text = text;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public String getText() {
	    return text;
	}
    }

    /**
     * Represents a function
     * Currently only the prefix and function name, but not its arguments.
     */
    public static class Function extends ELNode {

	private String prefix;
	private String name;
	private String uri;
	private FunctionInfo functionInfo;
	private String methodName;
	private String[] parameters;

	Function(String prefix, String name) {
	    this.prefix = prefix;
	    this.name = name;
	}

	public void accept(Visitor v) throws JspException {
	    v.visit(this);
	}

	public String getPrefix() {
	    return prefix;
	}

	public String getName() {
	    return name;
	}

	public void setUri(String uri) {
	    this.uri = uri;
	}

	public String getUri() {
	    return uri;
	}

	public void setFunctionInfo(FunctionInfo f) {
	    this.functionInfo = f;
	}

	public FunctionInfo getFunctionInfo() {
	    return functionInfo;
	}

	public void setMethodName(String methodName) {
	    this.methodName = methodName;
	}

	public String getMethodName() {
	    return methodName;
	}

	public void setParameters(String[] parameters) {
	    this.parameters = parameters;
	}

	public String[] getParameters() {
	    return parameters;
	}
    }

    /**
     * An ordered list of ELNode.
     */
    public static class Nodes {

	/* Name used for creating a map for the functions in this
	   EL expression, for communication to Generator.
	 */
	String mapName = null;
	private List<ELNode> list;

	public Nodes() {
	    list = new ArrayList<ELNode>();
	}

	public void add(ELNode en) {
	    list.add(en);
	}

	/**
	 * Visit the nodes in the list with the supplied visitor
	 * @param v The visitor used
	 */
	public void visit(Visitor v) throws JspException {
            for (ELNode n: list) {
		n.accept(v);
	    }
	}

	public Iterator<ELNode> iterator() {
	    return list.iterator();
	}

	public boolean isEmpty() {
	    return list.size() == 0;
	}

	/**
	 * @return true if the expression contains a ${...}
	 */
	public boolean containsEL() {
	    Iterator<ELNode> iter = list.iterator();
	    while (iter.hasNext()) {
		ELNode n = iter.next();
		if (n instanceof Root) {
		    return true;
		}
	    }
	    return false;
	}

	public void setMapName(String name) {
	    this.mapName = name;
	}

	public String getMapName() {
	    return mapName;
	}
    }

    public static class Visitor {

	public void visit(Root n) throws JspException {
	    n.getExpression().visit(this);
	}

	public void visit(Function n) throws JspException {
	}

	public void visit(Text n) throws JspException {
	}

	public void visit(ELText n) throws JspException {
	}
    }
}

