/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.taglib;


/**
 * <p>Tag for a component that dynamically sets the response character
 * encoding, based on the current <code>Locale</code>.</p>
 */

public class EncodingTag extends AbstractTag {


    // ------------------------------------------------------ Instance Variables



    // ------------------------------------------------------- Custom Attributes



    // -------------------------------------------------- UIComponentTag Methods


    /**
     * <p>Return the component type required by this tag handler.</p>
     */
    public String getComponentType()
    { return "com.sun.jsfcl.Encoding"; }                              //NOI18N



    /**
     * <p>Return the renderer type required by this tag handler.</p>
     */
    public String getRendererType()
    { return "com.sun.jsfcl.Encoding"; }                              //NOI18N


    // ------------------------------------------------------- Protected Methods


}
