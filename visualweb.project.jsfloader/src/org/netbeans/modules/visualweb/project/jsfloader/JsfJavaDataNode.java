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


package org.netbeans.modules.visualweb.project.jsfloader;


import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 * Node that represents JSF java data object.
 *
 * @author  Peter Zavadsky
 */
public class JsfJavaDataNode extends FilterNode {

    private static final String HELP_ID = "org.netbeans.modules.editor.java.JavaKit";

    /** Creates new JsfDataNode */
    public JsfJavaDataNode (JsfJavaDataObject jsfDataObject) {
        this(JavaDataSupport.createJavaNode(jsfDataObject.getPrimaryFile()));
    }

    private JsfJavaDataNode (Node javaNode) {
        super(javaNode);
        ((AbstractNode)javaNode).setIconBaseWithExtension("com/sun/rave/project/jsfloader/resources/jsfJavaObject.png"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
}

