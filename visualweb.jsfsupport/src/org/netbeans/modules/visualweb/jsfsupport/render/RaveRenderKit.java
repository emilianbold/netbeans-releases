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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.jsfsupport.render;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author gjmurphy
 */
public class RaveRenderKit extends RenderKit {

    static String SUFFIX = "DesignTime";

    RenderKit renderKit;

    /** Creates a new instance of RaveRenderKit */
    public RaveRenderKit(RenderKit renderKit) {
        this.renderKit = renderKit;
    }

    public void addRenderer(String family, String rendererType, Renderer renderer) {
        this.renderKit.addRenderer(family, rendererType, renderer);
    }

    public Renderer getRenderer(String family, String rendererType) {
        Renderer renderer = this.renderKit.getRenderer(family, rendererType + SUFFIX);
        if (renderer == null)
            renderer = this.renderKit.getRenderer(family, rendererType);
        return renderer;
    }

    public ResponseStateManager getResponseStateManager() {
        return this.renderKit.getResponseStateManager();
    }

    public ResponseWriter createResponseWriter(Writer writer,
            String contentTypeList, String encoding) {
        return this.renderKit.createResponseWriter(writer, contentTypeList, encoding);
    }
	
    public ResponseStream createResponseStream(OutputStream out) {
        return this.renderKit.createResponseStream(out);
    }
}
