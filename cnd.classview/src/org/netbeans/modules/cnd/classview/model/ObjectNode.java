/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.classview.model;
import javax.swing.*;
import org.netbeans.modules.cnd.classview.Diagnostic;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.SmartChangeEvent;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;

/**
 * @author Vladimir Kvasihn
 */
public abstract class ObjectNode extends BaseNode {
    private String uniqueName;
    private CsmProject project;
    // for unnamed declatarions
    private CsmOffsetableDeclaration unnamedDeclaration;
    
    public ObjectNode(CsmOffsetableDeclaration declaration) {
        this(declaration, Children.LEAF);
    }
    
    public ObjectNode(CsmOffsetableDeclaration declaration, Children children) {
        super(children);
        setObject(declaration);
        if( project.findDeclaration(uniqueName) == null ) {
            if( Diagnostic.DEBUG && declaration.getName().length() > 0 ) {
                System.err.println(".ctor can't find object by unique name " + uniqueName);
            }
        }
        String name = declaration.getName();
        setName(name);
        setDisplayName(name);
        setShortDescription(name);
    }
    
    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
        return getObject();
    }
    
    public CsmOffsetableDeclaration getObject() {
        CsmOffsetableDeclaration object = null;
        if (!isDismissed()) {
            CsmProject prj = getDeclarationProject();
            if (prj != null) {
                object = (CsmOffsetableDeclaration) prj.findDeclaration(uniqueName);
                if( object == null ) {
                    if( Diagnostic.DEBUG) {
                        System.err.println("Can't find object by unique name " + uniqueName);
                    }
                    object =unnamedDeclaration;
                }
            }
        }
        return object;
    }
    
    protected CsmProject getDeclarationProject(){
        return project;
    }
    
    protected String getUniqueName(){
        return uniqueName;
    }
    
    protected void setObject(CsmOffsetableDeclaration declaration) {
        uniqueName = declaration.getUniqueName();
        project = declaration.getContainingFile().getProject();
        if (declaration.getName().length()==0){
            unnamedDeclaration = declaration;
        }
    }
    
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    protected Action createOpenAction() {
        return new GoToDeclarationAction(getObject());
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] { createOpenAction() };
    }
    
    protected void objectChanged() {
    }
    
    public boolean update(SmartChangeEvent e) {
        //super.update(e);
        if (!isDismissed()) {
            String uniqueName = getUniqueName();
            if( e.getChangedUniqueNames().contains(uniqueName) ) {
                CsmOffsetableDeclaration decl = getObject();
                if (decl != null){
                    setObject(decl);
                    objectChanged();
                    return true;
                } else {
                    final Children children = getParentNode().getChildren();
                    children.MUTEX.writeAccess(new Runnable(){
                        public void run() {
                            children.remove(new Node[] { ObjectNode.this });
                        }
                    });
                }
            } else if( e.getRemovedUniqueNames().contains(uniqueName) ) {
                final Children children = getParentNode().getChildren();
                children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        children.remove(new Node[] { ObjectNode.this });
                    }
                });
                return true;
            }
        }
        return false;
    }
    
    public void dismiss() {
        setDismissed();
        //declaration = null;
        super.dismiss();
        project = null;
    }
    
}
