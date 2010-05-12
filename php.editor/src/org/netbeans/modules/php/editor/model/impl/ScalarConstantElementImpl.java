package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;

class ScalarConstantElementImpl extends ModelElementImpl implements ConstantElement {
    private final String value;

    ScalarConstantElementImpl(final NamespaceScopeImpl inScope, final ASTNodeInfo<Scalar> node, final String value) {
        super(inScope,node.getName(),inScope.getFile(),node.getRange(), PhpElementKind.CONSTANT);
        this.value = value;
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
        sb.append(getValue() != null ? getValue() : "?").append(";");//NOI18N
        return sb.toString();
    }

    @Override
    public String getValue() {
        return value;
    }
}
