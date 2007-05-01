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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplate;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplateGroup;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TemplateManager {

    private static final String LOG_CATEGORY = TemplateManager.class.getName();

    private HashMap nodeMap = new HashMap();
    private IResource rManager;
    private ITemplateGroup tg;

    /**
     * Creates a new instance of TemplateManager using the contents supplied by the
     * InputStream and IResource instance.
     * 
     * @param in InputStream containing template configuration
     * @param resource IResource instance
     */
    public TemplateManager(InputStream in, IResource resource) {
        rManager = resource;
        TemplateFactory fac = new TemplateFactory(rManager);

        init(new TemplateParser(in, fac), fac);
    }

    /**
     * Gets PropertyNode, if any, associated with the given name.
     * 
     * @param templateName name of template whose PropertyNode is sought
     * @return associated PropertyNode instance, or null if none exists
     */
    public PropertyNode getNodeForTemplateName(String templateName) {
        return (PropertyNode) nodeMap.get(templateName);
    }

    /**
     * Gets ITemplateGroup instance managed by this instance.
     * 
     * @return associated ITemplateGroup instance
     */
    public ITemplateGroup getTemplateGroup() {
        return tg;
    }

    /*
     * Creates PropertyNode instances from internal TemplateGroup reference.
     */
    private void createNodes() {
        Map map = tg.getTemplates();
        Iterator it = map.keySet().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            ITemplate template = (ITemplate) map.get(name);
            PropertyNode node = new PropertyNode(template);
            nodeMap.put(name, node);
        }
    }

    /*
     * Performs common initialization tasks for overloaded constructors. @param parser
     * TemplateParser to use in parsing template configuration info @param fac
     * TemplateFactory to generate ITemplateGroup instance.
     */
    private void init(TemplateParser parser, TemplateFactory fac) {
        if (parser == null) {
            Logger.print(Logger.DEBUG, LOG_CATEGORY, "init(TemplateParser)", "TemplateParser is null");
        }

        tg = fac.getTemplateGroup();
        createNodes();
    }

}

