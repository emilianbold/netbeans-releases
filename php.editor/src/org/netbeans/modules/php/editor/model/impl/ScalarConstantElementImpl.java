package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

class ScalarConstantElementImpl extends ModelElementImpl implements ConstantElement {
    ScalarConstantElementImpl(NamespaceScopeImpl inScope, ASTNodeInfo<Scalar> node) {
        this(inScope,node.getName(),inScope.getFile(),node.getRange());
    }
    ScalarConstantElementImpl(IndexScopeImpl inScope, org.netbeans.modules.php.editor.api.elements.ConstantElement indexedConstant) {
        this(inScope,indexedConstant.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedConstant.getFilenameUrl()),
                new OffsetRange(indexedConstant.getOffset(), indexedConstant.getOffset() + indexedConstant.getName().length()));
    }

    private ScalarConstantElementImpl(ScopeImpl inScope, String name,
            Union2<String, FileObject> file, OffsetRange offsetRange) {
        super(inScope, name, file, offsetRange, PhpElementKind.CONSTANT);
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final QualifiedName qualifiedName = QualifiedName.create(getName());
        final String name = qualifiedName.getName();
        sb.append(name.toLowerCase()).append(";");//NOI18N
        sb.append(name).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        sb.append(qualifiedName.getNamespaceName()).append(";");//NOI18N

        return sb.toString();
    }    
}
