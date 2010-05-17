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

package org.netbeans.lib.collab.util;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 *
 */
class TextConverterCallback extends HTMLEditorKit.ParserCallback {
    String text = "";
    private StringBuffer buffer = new StringBuffer();
    private String link;

    public TextConverterCallback() {
    }

    public void flush() {
        text = buffer.toString();
        buffer = new StringBuffer();
    }

    public void handleText(char []data, int pos) {            
        if (link == null) { //we are not inside a link tag
            if (buffer.equals("")) buffer.append(" ");
            buffer.append(data);
        } else {
            String linkprompt = new String(data);
            if (linkprompt.equals(link)) link = null;
            buffer.append(linkprompt);
        }
    }
    
    public void handleComment(char [] data, int pos) {
        
    }
    
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t == HTML.Tag.P) buffer.append("\r\n");
        else if (t == HTML.Tag.A) {
            link = (String)a.getAttribute(HTML.Attribute.HREF);    
        }
    }
    public void handleEndTag(HTML.Tag t, int pos) {
        if (t == HTML.Tag.H1 || 
        t == HTML.Tag.H2 || 
        t == HTML.Tag.H3 || 
        t == HTML.Tag.H4 || 
        t == HTML.Tag.H5 || 
        t == HTML.Tag.H6) {
            buffer.append("\r\n");
        }
        if (t == HTML.Tag.A) {
            if (link != null) {
                buffer.append("[");
                buffer.append(link);
                buffer.append("]");
                link = null;
            }
        }
        
    }
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t == HTML.Tag.BR) buffer.append("\r\n");            
    }
    
    public void handleError(String errorMsg, int pos) {
        /*buffer = new StringBuffer("Error parsing HTML\r\n");
        buffer.append(errorMsg);*/
        
    }
}

public class HTMLConverter extends HTMLDocumentLoader {
    
    HTMLEditorKit.ParserCallback c;
    
    public String convertToText(String html) {
        c = new TextConverterCallback();
        try {
            loadDocument(html);
            return ((TextConverterCallback)c).text;                                            
        } catch (Exception e)  {
            return "Error Parsing HTML";
        }
    }
        
    public synchronized HTMLEditorKit.ParserCallback getParserCallback(HTMLDocument doc) {
		return c;
	}
    
}


