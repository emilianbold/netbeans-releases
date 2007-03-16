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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * CsmInheritance implementation
 * @author Vladimir Kvashin
 */
public class InheritanceImpl extends OffsetableBase implements CsmInheritance {

    private CsmVisibility visibility;
    private boolean virtual;
    
    // only one of resolvedAncestorClassCacheOLD/resolvedAncestorClassCacheUID must be used (based on USE_REPOSITORY) 
    private CsmClass resolvedAncestorClassCacheOLD;
    private CsmUID<CsmClass> resolvedAncestorClassCacheUID;
    
    // only one of classifierCacheOLD/classifierCacheUID must be used (based on USE_REPOSITORY) 
    private CsmClassifier classifierCacheOLD;
    private CsmUID<CsmClassifier> classifierCacheUID;
    
    private String ancestorName;
    
    public InheritanceImpl(AST ast, CsmFile file) {
        super(ast, file);
        render(ast);
    }

    public boolean isVirtual() {
        return virtual;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public CsmClass getCsmClass() {
        CsmClass ancestorCache = _getAncestorCache();
        if (ancestorCache == null || !ancestorCache.isValid())
        {
            ancestorCache = null;
            CsmClassifier classifier = getCsmClassifier();
            classifier = CsmBaseUtilities.getOriginalClassifier(classifier);
            if (CsmKindUtilities.isClass(classifier)) {
                ancestorCache = (CsmClass)classifier;
            }
            _setAncestorCache(ancestorCache);
        }
        return ancestorCache;
    }
    
    public CsmClassifier getCsmClassifier() {
        CsmClassifier classifierCache = _getClassifierCache();
        if (classifierCache == null || 
                ((classifierCache instanceof CsmValidable) && !((CsmValidable)classifierCache).isValid())) {
            classifierCache = renderClassifier(ancestorName);
            _setClassifierCache(classifierCache);
        }
        return classifierCache;        
    }
    
    private void render(AST node) {
        visibility = CsmVisibility.PRIVATE;
        for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_private:
                    visibility = CsmVisibility.PRIVATE;
                    break;
                case CPPTokenTypes.LITERAL_public:
                    visibility = CsmVisibility.PUBLIC;
                    break;
                case CPPTokenTypes.LITERAL_protected:
                    visibility = CsmVisibility.PROTECTED;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    virtual = true;
                    break;
                case CPPTokenTypes.ID:
                    StringBuffer ancNameBuffer = new StringBuffer();
                    int counter = 0;
                    for( ; token != null; token = token.getNextSibling() ) {
                        switch( token.getType() ) {
                            case CPPTokenTypes.ID:
                                ancNameBuffer.append(token.getText());
                                break;
                            case CPPTokenTypes.SCOPE:
                                ancNameBuffer.append("::"); // NOI18N
                                counter++;
                                break;
                            default:
                                // here can be "<", ">" and other template stuff
                        }
                    }
                    //CsmObject o = ResolverFactory.createResolver(this).resolve(new String[] { token.getText() } );
                    this.ancestorName = ancNameBuffer.toString();
                    this.ancestorName = counter == 0 ? TextCache.getString(this.ancestorName) : QualifiedNameCache.getString(this.ancestorName);
                    return; // it's definitely the last!; besides otherwise we get NPE in for 
                    //break;
            }
        }
    }

    private CsmClassifier renderClassifier(String ancName) {
        CsmClassifier result = null;
        CsmObject o = ResolverFactory.createResolver(this).resolve(ancName);
        if( CsmKindUtilities.isClassifier(o) ) {
            result = (CsmClassifier) o;
        }
        return result;
    }
    
    public CsmClass _getAncestorCache() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmClass ancestorCache = UIDCsmConverter.UIDtoDeclaration(resolvedAncestorClassCacheUID);
            assert (ancestorCache != null || resolvedAncestorClassCacheUID == null);
            return ancestorCache;            
        } else {
            return resolvedAncestorClassCacheOLD;
        }        
    }

    public void _setAncestorCache(CsmClass ancestorCache) {
        if (TraceFlags.USE_REPOSITORY) {
            resolvedAncestorClassCacheUID = UIDCsmConverter.declarationToUID(ancestorCache);
            assert (resolvedAncestorClassCacheUID != null || ancestorCache == null);
        } else {
            this.resolvedAncestorClassCacheOLD = ancestorCache;
        }        
    }
    

    private CsmClassifier _getClassifierCache() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmClassifier classifierCache = UIDCsmConverter.UIDtoDeclaration(classifierCacheUID);
            assert (classifierCache != null || classifierCacheUID == null);
            return classifierCache;            
        } else {
            return classifierCacheOLD;
        } 
    }

    private void _setClassifierCache(CsmClassifier classifierCache) {
        if (TraceFlags.USE_REPOSITORY) {
            classifierCacheUID = UIDCsmConverter.declarationToUID(classifierCache);
            assert (classifierCacheUID != null || classifierCacheUID == null);
        } else {
            this.classifierCacheOLD = classifierCache;
        }  
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
        output.writeBoolean(this.virtual);
        assert this.ancestorName != null;
        output.writeUTF(ancestorName);        

        // save cache
        UIDObjectFactory.getDefaultFactory().writeUID(classifierCacheUID, output);     
        boolean theSame = ((CsmUID)resolvedAncestorClassCacheUID == (CsmUID)classifierCacheUID);
        output.writeBoolean(theSame);
        if (!theSame) {
            UIDObjectFactory.getDefaultFactory().writeUID(resolvedAncestorClassCacheUID, output);        
        }
    }

    public InheritanceImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
        this.virtual = input.readBoolean();
        this.ancestorName = input.readUTF();
        this.ancestorName = ancestorName.indexOf("::") == -1 ? TextCache.getString(ancestorName) : QualifiedNameCache.getString(ancestorName); // NOI18N
        assert this.ancestorName != null;

        // restore cached value
        this.classifierCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        boolean theSame = input.readBoolean();
        if (!theSame) {
            this.resolvedAncestorClassCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        } else {
            this.resolvedAncestorClassCacheUID = (CsmUID)this.classifierCacheUID;
        }
    }    
    
}
