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
import java.util.HashMap;
import java.util.Map;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.openide.ErrorManager;

/** Reflection stuff for org.apache.jasper.compiler.Compiler.
 *
 * @author Petr Jiricka
 */
public class CompilerHacks {
    
    private Compiler comp;
    protected JspCompilationContext ctxt;
    
    private static Field pageInfoF;
    private static Field errDispatcherF;
    
    static {
        initMethodsAndFields();
    }
    
    /** Creates a new instance of CompilerHacks */
    public CompilerHacks(JspCompilationContext ctxt) {
        this.ctxt = ctxt;
    }

    static void initMethodsAndFields() {
        try {
            // pageInfo field
            pageInfoF = Compiler.class.getDeclaredField("pageInfo");
            pageInfoF.setAccessible(true);
            // errDispatcher field
            errDispatcherF = Compiler.class.getDeclaredField("errDispatcher");
            errDispatcherF.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    private void setupCompiler() throws JasperException {
        if (comp == null) {
            comp = ctxt.createCompiler();
            setErrDispatcherInCompiler(comp, new ErrorDispatcher(false));
            setPageInfoInCompiler(comp, new HackPageInfo(new BeanRepository(
                ctxt.getClassLoader(), comp.getErrorDispatcher()), ""));
        }
    }
    
    Compiler getCompiler() throws JasperException {
        setupCompiler();
        return comp;
    }
    
    private static void setPageInfoInCompiler(Compiler c, PageInfo pageInfo) throws JasperException {
        try {
            pageInfoF.set(c, pageInfo);
        }
        catch (IllegalAccessException e) {
            throw new JasperException(e);
        }
    }
    
    private static void setErrDispatcherInCompiler(Compiler c, ErrorDispatcher errDispatcher) throws JasperException {
        try {
            errDispatcherF.set(c, errDispatcher);
        }
        catch (IllegalAccessException e) {
            throw new JasperException(e);
        }
    }
    
    /** Hacked PageInfo to get better XML directive data
     */
    class HackPageInfo extends PageInfo {

        /** Map of prefix -> uri. */
        private Map approxXmlPrefixMapper;
        
        HackPageInfo(BeanRepository beanRepository, String jspFile) {
            super(beanRepository, jspFile);
            approxXmlPrefixMapper = new HashMap();
        }
        
        public void pushPrefixMapping(String prefix, String uri) {
            super.pushPrefixMapping(prefix, uri);
            if (uri != null) {
                approxXmlPrefixMapper.put(prefix, uri);
            }
        }
        
        Map getApproxXmlPrefixMapper() {
            return approxXmlPrefixMapper;
        }
    }
    
}
