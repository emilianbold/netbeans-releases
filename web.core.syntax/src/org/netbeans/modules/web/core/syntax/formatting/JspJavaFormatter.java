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

package org.netbeans.modules.web.core.syntax.formatting;

import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.java.JavaFormatter;


/**
 * Formatter for writing java scriplet in jsp and tag files.
 * @author Petr Pisl
 */

public class JspJavaFormatter extends JavaFormatter{
	
    public JspJavaFormatter(Class kitClass) {
	super(kitClass);
    }

    protected void initFormatLayers() {

	addFormatLayer(new StripEndWhitespaceLayer());
	addFormatLayer(new JspJavaLayer());
    }

    public class JspJavaLayer extends JavaFormatter.JavaLayer {	    

	protected FormatSupport createFormatSupport(FormatWriter fw) {
	    BaseDocument doc = (BaseDocument)fw.getDocument();
	    JspSyntaxSupport sup = new JspSyntaxSupport(doc);
	    try{
		TokenItem token = sup.getItemAtOrBefore(fw.getOffset());
		return new JspJavaFormatSupport(fw, token.getTokenContextPath());
	    }
	    catch (Exception e){
		e.printStackTrace(System.out);
	    }
	    return null;
	}
    }
}
