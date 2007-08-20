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

package org.netbeans.modules.cnd.classview;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.netbeans.modules.cnd.classview.model.ClassNode;
import org.netbeans.modules.cnd.classview.model.EnumNode;
import org.netbeans.modules.cnd.classview.model.GlobalFuncNode;
import org.netbeans.modules.cnd.classview.model.GlobalVarNode;
import org.netbeans.modules.cnd.classview.model.NamespaceNode;
import org.netbeans.modules.cnd.classview.model.ObjectNode;
import org.netbeans.modules.cnd.classview.model.TypedefNode;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class NamespaceKeyArray extends HostKeyArray implements UpdatebleHost, CsmProgressListener {
    private boolean isRootNamespase;
    
    public NamespaceKeyArray(ChildrenUpdater childrenUpdater, CsmNamespace namespace){
        super(childrenUpdater, namespace.getProject(),PersistentKey.createKey(namespace));
        CsmProject project = namespace.getProject();
        if (namespace.equals(project.getGlobalNamespace())){
            if (!project.isArtificial()) {
                CsmModelAccessor.getModel().addProgressListener(this);
                isRootNamespase = true;
            }
        }
    }
    
    @Override
    protected boolean isGlobalNamespace() {
        return isRootNamespase;
    }
    
    @Override
    protected boolean isNamespace() {
        return true;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
    }
    
    protected java.util.Map<PersistentKey,SortedName> getMembers() {
        CsmNamespace namespace = getNamespace();
        java.util.Map<PersistentKey,SortedName> res = new HashMap<PersistentKey,SortedName>();
        if (namespace != null){
            for( Iterator/*<CsmNamespace>*/ iter = namespace.getNestedNamespaces().iterator(); iter.hasNext(); ) {
                CsmNamespace ns = (CsmNamespace)iter.next();
                PersistentKey key = PersistentKey.createKey(ns);
                if (key != null) {
                    res.put(key, getSortedName(ns));
                }
            }
            Collection/*<CsmDeclaration>*/ decl = namespace.getDeclarations();
            if (decl != null) {
                for( Iterator/*<CsmDeclaration>*/ iter = decl.iterator(); iter.hasNext(); ) {
                    CsmDeclaration d = (CsmDeclaration) iter.next();
                    if (d != null && d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION){
                        CsmFunctionDefinition def = (CsmFunctionDefinition) d;
                        CsmFunction func = def.getDeclaration();
                        if (func != null){
                            d = func;
                        }
                    }
                    if (CsmKindUtilities.isOffsetable(d)) {
                        if (canCreateNode((CsmOffsetableDeclaration) d)){
                            PersistentKey key = PersistentKey.createKey(d);
                            if (key != null) {
                                res.put(key, getSortedName((CsmOffsetableDeclaration) d));
                            }
                        }
                    }
                }
            }
        }
        if (isRootNamespase && !getProject().isStable(null)){
            PersistentKey key = PersistentKey.createKey(getProject());
            if (key != null) {
                res.put(key, new SortedName(0,"",0)); // NOI18N
            }
        }
        return res;
    }
    
    protected boolean canCreateNode(CsmOffsetableDeclaration d) {
        if (d.getName().length() > 0) {
            if( CsmKindUtilities.isClass(d) ) {
                CsmClass cls = (CsmClass) d;
                if( !CsmKindUtilities.isClassMember(cls) ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
                return true;
            } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ) {
                return true;
            } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                CsmFunctionDefinition definition = (CsmFunctionDefinition) d;
                CsmFunction func = definition.getDeclaration();
                if( func == null || func == definition ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
                CsmEnum en = (CsmEnum) d;
                if( ! CsmKindUtilities.isClassMember(en) || ((CsmMember) en).getContainingClass() == null ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                return true;
            }
        }
        return false;
    }
    
    private ObjectNode createNode(CsmOffsetableDeclaration d) {
        ChildrenUpdater updater = getUpdater();
        if (updater != null) {
            // TODO: shouldn't be empty, if everything was resolved!
            if (d.getName().length() > 0) {
                if( CsmKindUtilities.isClass(d) ) {
                    CsmClass cls = (CsmClass) d;
                    // inner classes are return in namespace declarations list
                    // (since they act just like top-level classes),
                    // but shouldn't be included in class view at the top level
		    if( ! CsmKindUtilities.isClassMember(cls) ) {
                    //if( cls.getContainingClass() == null ) {
                        return new ClassNode( (CsmClass) d,
                                new ClassifierKeyArray(updater, (CsmClass) d));
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
                    return new GlobalVarNode((CsmVariable) d);
                }
//            else if( d.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
//                CsmVariableDefinition definition = (CsmVariableDefinition) d;
//                CsmVariable var = definition.getDeclaration();
//                if( var == null || var == definition ) {
//                    return new GlobalVarNode(definition);
//                }
//            }
                else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ) {
                    return new GlobalFuncNode((CsmFunction) d);
                } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                    CsmFunctionDefinition definition = (CsmFunctionDefinition) d;
                    CsmFunction func = definition.getDeclaration();
                    if( func == null || func == definition ) {
                        return new GlobalFuncNode(definition);
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
                    CsmEnum en = (CsmEnum) d;
                    if( ! CsmKindUtilities.isClassMember(en) || ((CsmMember) en).getContainingClass() == null ) {
                        return new EnumNode(en,
                                new ClassifierKeyArray(updater, en));
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                    CsmTypedef def = (CsmTypedef) d;
                    if (def.isTypeUnnamed()) {
                        CsmClassifier cls = def.getType().getClassifier();
                        if (cls != null && cls.getName().length()==0 &&
                                (cls instanceof CsmCompoundClassifier)) {
                            return new TypedefNode(def,new ClassifierKeyArray(updater, def, (CsmCompoundClassifier) cls));
                        }
                    }
                    return new TypedefNode(def);
                }
            }
        }
        return null;
    }
    
    private CsmNamespace getNamespace(){
        return (CsmNamespace)getHostId().getObject();
    }
    
    protected CsmOffsetableDeclaration findDeclaration(PersistentKey declId){
        return (CsmOffsetableDeclaration) declId.getObject();
    }
    
    private CsmNamespace findNamespace(PersistentKey nsId){
        return (CsmNamespace) nsId.getObject();
    }
    
    protected Node createNode(PersistentKey key){
        Node node = null;
        ChildrenUpdater updater = getUpdater();
        if (updater != null) {
            Object o = key.getObject();
            if (CsmKindUtilities.isNamespace((CsmObject) o)){
                CsmNamespace ns = (CsmNamespace) o;
                node = new NamespaceNode(ns,
                        new NamespaceKeyArray(updater,ns));
            } else if (o instanceof CsmProject){ // NOI18N
                node = CVUtil.createLoadingNode();
            } else {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) o;
                if (decl != null && canCreateNode(decl)){
                    node = createNode(decl);
                }
            }
        }
        return node;
    }
    
    public void projectParsingStarted(CsmProject project) {
    }
    
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }
    
    public void projectParsingFinished(CsmProject project) {
        onPprojectParsingFinished(project);
    }
    
    public void projectLoaded(CsmProject project) {
	onPprojectParsingFinished(project);
    }

    public void projectParsingCancelled(CsmProject project) {
    }

    public void fileInvalidated(CsmFile file) {
    }
    
    public void fileParsingStarted(CsmFile file) {
    }
    
    public void fileParsingFinished(CsmFile file) {
    }
    
    public void parserIdle() {
    }
}
