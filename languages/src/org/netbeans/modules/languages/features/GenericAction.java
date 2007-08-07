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

package org.netbeans.modules.languages.features;

import org.netbeans.modules.languages.*;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTNode;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.api.languages.ParseException;
import org.openide.ErrorManager;


/**
 * @author Dan Prusa
 */
public class GenericAction extends BaseAction {
    
    String performerName = null;
    String enablerName = null;
    Feature performer = null;
    Feature enabler = null;
    
    public GenericAction(String name, String performerName, String enablerName) {
        super(name);
        this.performerName = performerName;
        this.enablerName = enablerName;
    }
    
    private Feature getPerformer() {
        if (performer == null) {
            performer = Feature.createMethodCallFeature (null, null, performerName);
        }
        return performer;
    }
    
    private Feature getEnabler() {
        if (enablerName == null) {
            return null;
        }
        if (enabler == null) {
            enabler = Feature.createMethodCallFeature (null, null, enablerName);
        }
        return enabler;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        try {
            return ParserManagerImpl.get(comp.getDocument()).getAST();
        } catch (ParseException ex) {
            ErrorManager.getDefault().notify(ex);
        } 
        return null;
    }
    
    
    public void actionPerformed(ActionEvent e, JTextComponent comp) {
        ASTNode node = getASTNode(comp);
        if (node != null) {
            getPerformer().getValue (new Object[] {node, comp});
        }
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        ASTNode node = getASTNode(comp);
        if (node == null)
            return false;
        Feature em = getEnabler();
        if (em == null) {
            return super.isEnabled();
        }
        Object result = em.getValue (new Object[] {node, comp});
        return result != null && ((Boolean)result).booleanValue();
    }
    
}
