package org.netbeans.modules.web.jsf.navigation.graph;
import java.awt.Image;
import java.io.IOException;
import org.openide.nodes.Node;

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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import org.openide.util.HelpCtx;
/**
 *
 * @author joelle
 */
public abstract class PageFlowSceneElement {
    private String name;
    
    
    public PageFlowSceneElement(){
    }
    
    public boolean equals(Object obj) {
        return (this == obj);
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    private boolean modifiable = true;
    public boolean isModifiable() {
        return modifiable;
    }
    public void setModifiable(boolean modifiable ){
        this.modifiable = modifiable;
    }
    
    public abstract Node getNode();
    public abstract HelpCtx getHelpCtx();
    public abstract void destroy() throws IOException;
    public abstract boolean canDestroy();
    public abstract boolean canRename();
    public abstract Image getIcon( int type );
}
