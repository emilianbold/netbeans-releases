package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;

class ClassConstantElementImpl extends ModelElementImpl implements ClassConstantElement {
    private String typeName;

    ClassConstantElementImpl(Scope inScope, TypeConstantElement indexedConstant) {
        super(inScope, indexedConstant, PhpElementKind.TYPE_CONSTANT);
        assert inScope instanceof TypeScope;
        String in = indexedConstant.getIn();
        if (in != null) {
            typeName = in;
        } else {
            typeName = inScope.getName();
        }
    }

    ClassConstantElementImpl(Scope inScope, ClassConstantDeclarationInfo clsConst) {
        super(inScope, clsConst, PhpModifiers.noModifiers());
        typeName = inScope.getName();
    }

    @Override
    public String getNormalizedName() {
        return typeName+super.getNormalizedName();
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        return sb.toString();
    }
}
