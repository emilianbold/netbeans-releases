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

package org.apache.jasper.compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.openide.ErrorManager;

/**
 *
 * @author Petr Jiricka
 */
public class ExtractPageData {

    protected JspCompilationContext ctxt;

    protected Options options;
    private CompilerHacks compHacks;

    private boolean isXml;
    private String sourceEnc;

    /** Creates a new instance of ExtractPageData */
    public ExtractPageData(JspCompilationContext ctxt) {
        this.ctxt = ctxt;
        this.options = ctxt.getOptions();
        this.compHacks = new CompilerHacks(ctxt);
    }
    
    
    public boolean isXMLSyntax() throws JasperException, FileNotFoundException, IOException {
        if (sourceEnc == null) {
            extractPageData();
        }
        return isXml;
    }

    public String getEncoding() throws JasperException, FileNotFoundException, IOException {
        if (sourceEnc == null) {
            extractPageData();
        }
        return sourceEnc;
    }

    
    private void extractPageData() throws JasperException, FileNotFoundException, IOException {
        
        // the following also sets up ErrorDispatcher and PageInfo in the compiler
        Compiler comp = compHacks.getCompiler();
        PageInfo pageInfo = comp.getPageInfo();
        
	JspConfig jspConfig = options.getJspConfig();
	JspConfig.JspProperty jspProperty =
			jspConfig.findJspProperty(ctxt.getJspFile());

    /*
     * If the current uri is matched by a pattern specified in
     * a jsp-property-group in web.xml, initialize pageInfo with
     * those properties.
     */
    pageInfo.setELIgnored(JspUtil.booleanValue(jspProperty.isELIgnored()));
    pageInfo.setScriptingInvalid(JspUtil.booleanValue(jspProperty.isScriptingInvalid()));
    if (jspProperty.getIncludePrelude() != null) {
        pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
    }
	if (jspProperty.getIncludeCoda() != null) {
	    pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
	}
        /*String javaFileName = ctxt.getServletJavaFileName();

        // Setup the ServletWriter
        String javaEncoding = ctxt.getOptions().getJavaEncoding();
	OutputStreamWriter osw = null; 
	try {
	    osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
					 javaEncoding);
	} catch (UnsupportedEncodingException ex) {
            errDispatcher.jspError("jsp.error.needAlternateJavaEncoding", javaEncoding);
	}

	ServletWriter writer = new ServletWriter(new PrintWriter(osw));
        ctxt.setWriter(writer);*/

        // Reset the temporary variable counter for the generator.
        JspUtil.resetTemporaryVariableName();

	// Parse the file
	ParserControllerProxy parserCtl = new ParserControllerProxy(ctxt, comp);
	parserCtl.extractSyntaxAndEncoding(ctxt.getJspFile());
        
        isXml = parserCtl.isXml;
        sourceEnc = parserCtl.sourceEnc;
    }
    
}
