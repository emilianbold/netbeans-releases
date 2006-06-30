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

import javax.servlet.jsp.JspException;
import org.openide.ErrorManager;
import org.xml.sax.Attributes;

class DumpVisitor extends Node.Visitor {

    private int indent = 0;

    private StringBuffer buf;

    private DumpVisitor() {
        super();
        buf = new StringBuffer();
    }

    /**
     * This method provides a place to put actions that are common to
     * all nodes. Override this in the child visitor class if need to.
     */
    protected void visitCommon(Node n) throws JspException {
        printString("\nNode [" + n.getStart() + ", " + getDisplayClassName(n.getClass().getName()) + "] ");
    }
    
    private String getDisplayClassName(String cn) {
        int amp = cn.indexOf('$');
        return cn.substring(amp + 1);
    }

    private String getAttributes(Attributes attrs) {
        if (attrs == null)
            return "";

        StringBuffer buf = new StringBuffer();
        for (int i=0; i < attrs.getLength(); i++) {
            buf.append(" " + attrs.getQName(i) + "=\""
                       + attrs.getValue(i) + "\"");
        }
        return buf.toString();
    }

    private void printString(String str) {
        printIndent();
        buf.append(str);
    }

    private void printString(String prefix, char[] chars, String suffix) {
        String str = null;
        if (chars != null) {
            str = new String(chars);
        }
        printString(prefix, str, suffix);
    }

    private void printString(String prefix, String str, String suffix) {
        printIndent();
        if (str != null) {
            buf.append(prefix);
            buf.append(str);
            buf.append(suffix);
        } else {
            buf.append(prefix);
            buf.append(suffix);
        }
    }

    private void printAttributes(String prefix, Attributes attrs,
                                 String suffix) {
        printString(prefix, getAttributes(attrs), suffix);
    }

    private void dumpBody(Node n) throws JspException {
        Node.Nodes page = n.getBody();
        if (page != null) {
		indent++;
            page.visit(this);
		indent--;
        }
    }

    public void visit(Node.TagDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ tag", n.getAttributes(), "%>");
    }

    public void visit(Node.PageDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ page", n.getAttributes(), "%>");
    }

    public void visit(Node.TaglibDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ taglib", n.getAttributes(), "%>");
    }

    public void visit(Node.IncludeDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ include", n.getAttributes(), "%>");
        dumpBody(n);
    }

    public void visit(Node.Comment n) throws JspException {
        visitCommon(n);
        printString("<%--", n.getText(), "--%>");
    }

    public void visit(Node.Declaration n) throws JspException {
        visitCommon(n);
        printString("<%!", n.getText(), "%>");
    }

    public void visit(Node.Expression n) throws JspException {
        visitCommon(n);
        printString("<%=", n.getText(), "%>");
    }

    public void visit(Node.Scriptlet n) throws JspException {
        visitCommon(n);
        printString("<%", n.getText(), "%>");
    }

    public void visit(Node.IncludeAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:include", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:include>");
    }

    public void visit(Node.ForwardAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:forward", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:forward>");
    }

    public void visit(Node.GetProperty n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:getProperty", n.getAttributes(), "/>");
    }

    public void visit(Node.SetProperty n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:setProperty", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:setProperty>");
    }

    public void visit(Node.UseBean n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:useBean", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:useBean>");
    }

    public void visit(Node.PlugIn n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:plugin", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:plugin>");
    }

    public void visit(Node.ParamsAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:params", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:params>");
    }

    public void visit(Node.ParamAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:param", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:param>");
    }

    public void visit(Node.NamedAttribute n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:attribute", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:attribute>");
    }

    public void visit(Node.JspBody n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:body", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:body>");
    }

    public void visit(Node.ELExpression n) throws JspException {
        visitCommon(n);
        printString( "${" + new String( n.getText() ) + "}" );
    }

    public void visit(Node.CustomTag n) throws JspException {
        visitCommon(n);
        printAttributes("<" + n.getQName(), n.getAttributes(), ">");
        dumpBody(n);
        printString("</" + n.getQName() + ">");
    }

    public void visit(Node.UninterpretedTag n) throws JspException {
        visitCommon(n);
        String tag = n.getQName();
        printAttributes("<"+tag, n.getAttributes(), ">");
        dumpBody(n);
        printString("</" + tag + ">");
    }

    public void visit(Node.InvokeAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:invoke", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:invoke>");
    }

    public void visit(Node.DoBodyAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:doBody", n.getAttributes(), ">");
        dumpBody(n);
        printString("</jsp:doBody>");
    }

    public void visit(Node.TemplateText n) throws JspException {
        visitCommon(n);
        printString(new String(n.getText()));
    }

    private void printIndent() {
        for (int i=0; i < indent; i++) {
            buf.append("  ");
        }
    }
    
    private String getString() {
        return buf.toString();
    }

    public static String dump(Node n) {
	try {
            DumpVisitor dv = new DumpVisitor();
	    n.accept(dv);
            return dv.getString();
	} catch (JspException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return e.getMessage();
	}
    }

    public static String dump(Node.Nodes page) {
	try {
            DumpVisitor dv = new DumpVisitor();
	    page.visit(dv);
            return dv.getString();
	} catch (JspException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return e.getMessage();
	}
    }
}

