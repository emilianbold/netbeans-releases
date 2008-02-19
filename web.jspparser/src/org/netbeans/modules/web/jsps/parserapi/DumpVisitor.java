/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.jsps.parserapi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import org.xml.sax.Attributes;

class DumpVisitor extends Node.Visitor {

    private int indent = 0;

    private StringBuilder buf;

    private DumpVisitor() {
        super();
        buf = new StringBuilder();
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

        StringBuilder buf = new StringBuilder();
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
        printString(n.getText());
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
            Logger.getLogger("global").log(Level.INFO, null, e);
            return e.getMessage();
	}
    }

    public static String dump(Node.Nodes page) {
	try {
            DumpVisitor dv = new DumpVisitor();
	    page.visit(dv);
            return dv.getString();
	} catch (JspException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
            return e.getMessage();
	}
    }
}

