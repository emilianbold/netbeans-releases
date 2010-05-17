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
package org.netbeans.modules.xslt.project.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationDesc;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationType;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationUC;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XsltMapPropertyChangeListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformationUCChildren extends Children.Keys implements XsltMapPropertyChangeListener {
    private TransformationUC tUseCase;
    
    public TransformationUCChildren(TransformationUC tUseCase) {
        assert tUseCase != null;
        this.tUseCase = tUseCase;        
    }

    protected Node[] createNodes(Object key) {
        List<Node> nodes = new ArrayList<Node>();
        if (key instanceof TransformationDesc) {
            nodes.add(new TransformationDescNode((TransformationDesc)key));
        }
        return nodes.toArray(new Node[nodes.size()]);
    }
    
    private Collection getNodeKeys() {
//        System.out.println("invoked getNodeKeys() !!! ");
        
        if (tUseCase == null) {
            return Collections.EMPTY_SET;
        }
        List<TransformationDesc> tUseCaseDescs = new ArrayList<TransformationDesc>();//tUseCase.getTransformationDescs();
        if (TransformationType.FILTER_REQUEST_REPLY.equals(tUseCase.getTransformationType())) {
            tUseCaseDescs = tUseCase.getTransformationDescs();
        } else {
            tUseCaseDescs.add(tUseCase.getInputTransformationDesc());
        }
        
        return tUseCaseDescs == null ? Collections.EMPTY_SET : tUseCaseDescs;
    }
    
    protected void addNotify() {
        super.addNotify();
        tUseCase.getXsltMapModel().addPropertyChangeListener(this);
        setKeys(getNodeKeys());
    }

    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        tUseCase.getXsltMapModel().removePropertyChangeListener(this);
        super.removeNotify();
    }

    public void transformationDescChanged(TransformationDesc oldDesc, TransformationDesc newDesc) {
        setKeys(getNodeKeys());
    }
}
