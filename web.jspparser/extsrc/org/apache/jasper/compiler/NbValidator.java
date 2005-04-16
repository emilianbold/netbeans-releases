/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.jasper.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.jsp.tagext.PageData;

import org.openide.ErrorManager;

import org.apache.jasper.JasperException;

/** This class is similar to org.apache.jasper.compiler.Validator, it only 
 * allows getting access to the XML view of the page.
 *
 * @author Petr Jiricka
 */
public class NbValidator {
    
    private static Method validateXmlViewM;
    private static Field bufF;
    
    static {
        initReflection();
    }
    
    private static void initReflection() {
        try {
            validateXmlViewM = Validator.class.getDeclaredMethod("validateXmlView", new Class[] {PageData.class, Compiler.class});
            validateXmlViewM.setAccessible(true);
            bufF = PageDataImpl.class.getDeclaredField("buf");
            bufF.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (NoSuchFieldException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    /** Returns the XML view of the page.
     */
    public static String validate(Compiler compiler,
				Node.Nodes page) throws JasperException {

	/*
	 * Visit the page/tag directives first, as they are global to the page
	 * and are position independent.
	 */
	page.visit(new Validator.DirectiveVisitor(compiler));

	// Determine the default output content type
	PageInfo pageInfo = compiler.getPageInfo();
	String contentType = pageInfo.getContentType();

	if (contentType == null || contentType.indexOf("charset=") < 0) {
	    boolean isXml = page.getRoot().isXmlSyntax();
	    String defaultType;
	    if (contentType == null) {
		defaultType = isXml? "text/xml": "text/html";
	    } else {
		defaultType = contentType;
	    }

	    String charset = null;
	    if (isXml) {
		charset = "UTF-8";
	    } else {
		if (!page.getRoot().isDefaultPageEncoding()) {
		    charset = page.getRoot().getPageEncoding();
		}
	    }

	    if (charset != null) {
		pageInfo.setContentType(defaultType + ";charset=" + charset);
	    } else {
		pageInfo.setContentType(defaultType);
	    }
	}

	/*
	 * Validate all other nodes.
	 * This validation step includes checking a custom tag's mandatory and
	 * optional attributes against information in the TLD (first validation
	 * step for custom tags according to JSP.10.5).
	 */
	page.visit(new Validator.ValidateVisitor(compiler));

	/*
	 * Invoke TagLibraryValidator classes of all imported tags
	 * (second validation step for custom tags according to JSP.10.5).
	 */
        // validateXmlView(new PageDataImpl(page, compiler), compiler);
        try {
            PageDataImpl pdi = new PageDataImpl(page, compiler);
            
            validateXmlViewM.invoke(null, new Object[] {pdi, compiler});
            
            /*
             * Invoke TagExtraInfo method isValid() for all imported tags 
             * (third validation step for custom tags according to JSP.10.5).
             */
            page.visit(new Validator.TagExtraInfoVisitor(compiler));
            
            StringBuffer buf = (StringBuffer)bufF.get(pdi);
            return buf.toString();
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new JasperException(e.getMessage());
        }
        catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof JasperException) {
                throw (JasperException)target;
            }
            else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                throw new JasperException(e.getMessage());
            }
        }

    }


    
}
