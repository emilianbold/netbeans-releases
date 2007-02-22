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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.datatype.Datatype.Facet;

/**
 * This class represents UnionType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 *
 *
 * @author Ayub Khan
 */
public class UnionType extends Datatype {
    
    static List<Facet> applicableFacets;
    
    private Datatype.Kind kind;
    
    protected boolean hasFacets;
    
    private boolean isList;
    
    /**
     * Creates a new instance of UnionType
     */
    public UnionType() {
        this.kind = Datatype.Kind.UNION;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public List<Facet> getApplicableFacets() {
        if(applicableFacets == null) {
            List<Facet> facets = new ArrayList<Facet>();
            applicableFacets = Collections.unmodifiableList(facets);
        }
        return applicableFacets;
    }
    
    public boolean hasFacets() {
        return hasFacets;
    }
    
    public boolean isList() {
        return isList;
    }
    
    public void setIsList(boolean isList) {
        this.isList = isList;
    }
    
    public void addMemberType(Datatype memberType) {
        m.add(memberType);
    }
    
    public List<Datatype> getMemberTypes() {
        return m;
    }
    
    public void setHasFacets(boolean hasFacets) {
        this.hasFacets = hasFacets;
    }
    
    List<Datatype> m = new ArrayList<Datatype>();
}
