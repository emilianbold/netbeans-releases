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

package org.netbeans.modules.web.jspparser_ext;

import javax.servlet.ServletContext;
import org.apache.jasper.JspC;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldLocationsCache;

/**
 *
 * @author Petr Jiricka
 */
public class OptionsImpl implements Options {

    /**
     * Cache for the TLD locations
     */
    private TldLocationsCache tldLocationsCache = null;

    /**
     * Jsp config information
     */
    private JspConfig jspConfig = null;

    /**
     * TagPluginManager
     */
    private TagPluginManager tagPluginManager = null;

    /** Creates a new instance of OptionsImpl */
    public OptionsImpl(ServletContext context) {
        tldLocationsCache = new TldLocationsCache(context, this, true);
        jspConfig = new JspConfig(context);
        tagPluginManager = new TagPluginManager(context);
    }
    
    public int getCheckInterval() {
        return 300;
    }
    
    public boolean getClassDebugInfo() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getClassPath() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompiler() {
        // should not be needed
        //throw new UnsupportedOperationException();
        return null;
    }
    
    public boolean getDevelopment() {
        return true;
    }
    
    public boolean getFork() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getIeClassId() {
        return JspC.DEFAULT_IE_CLASS_ID;
    }
    
    public String getJavaEncoding() {
        // should not be needed
        throw new UnsupportedOperationException();
        //return "UTF-8";
    }
    
    public JspConfig getJspConfig() {
	return jspConfig;
    }
    
    public boolean getKeepGenerated() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getTrimSpaces() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean genStringAsCharArray() {
        // should not be needed
        throw new UnsupportedOperationException();
    }

    public boolean getMappedFile() {
        return false;
    }
    
    public boolean getReloading() {
        return true;
    }
    
    public java.io.File getScratchDir() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getSendErrorToClient() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public TagPluginManager getTagPluginManager() {
        return tagPluginManager;
    }
    
    public TldLocationsCache getTldLocationsCache() {
        return tldLocationsCache;
    }
    
    public boolean isPoolingEnabled() {
        // should not be needed
        throw new UnsupportedOperationException();
        //return true;
    }
    
    public boolean isSmapDumped() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean isSmapSuppressed() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean isXpoweredBy() {
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        // should not be needed
        throw new UnsupportedOperationException();
    }

    public int getModificationTestInterval(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompilerSourceVM(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public String getCompilerTargetVM(){
        // should not be needed
        throw new UnsupportedOperationException();
    }
    
    public int getInitialCapacity(){
        throw new UnsupportedOperationException();
    }
    
    public boolean getUsePrecompiled(){
        return false;
    }
    
    public String getSystemClassPath(){
        throw new UnsupportedOperationException();
    }

    public boolean isTldValidationEnabled() {
        return false;
    }
    
}
