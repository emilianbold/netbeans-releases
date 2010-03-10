package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

class ConstantElementImpl extends ModelElementImpl implements ConstantElement {
    ConstantElementImpl(NamespaceScopeImpl inScope, ASTNodeInfo<Scalar> node) {
        this(inScope,node.getName(),inScope.getFile(),node.getRange());
    }
    ConstantElementImpl(NamespaceScopeImpl inScope, ClassConstantDeclarationInfo node) {
        this(inScope,node.getName(),inScope.getFile(),node.getRange());
    }

    ConstantElementImpl(IndexScopeImpl inScope, org.netbeans.modules.php.editor.api.elements.ConstantElement indexedConstant) {
        this(inScope,indexedConstant.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedConstant.getFilenameUrl()),
                new OffsetRange(indexedConstant.getOffset(), indexedConstant.getOffset() + indexedConstant.getName().length()));
    }

    private ConstantElementImpl(ScopeImpl inScope, String name,
            Union2<String, FileObject> file, OffsetRange offsetRange) {
        super(inScope, name, file, offsetRange, PhpElementKind.CONSTANT);
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N

        return sb.toString();
    }    
}
