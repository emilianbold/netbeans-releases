package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;

class ClassConstantElementImpl extends ModelElementImpl implements ClassConstantElement {
    private String typeName;
    static ClassConstantElementImpl createClzConstantElementImpl(TypeScopeImpl inScope,
            ClassConstantDeclarationInfo clsConst) {
        return new ClassConstantElementImpl(inScope, clsConst);
    }

    ClassConstantElementImpl(TypeScopeImpl inScope, IndexedConstant indexedConstant) {
        super(inScope, indexedConstant, PhpKind.CLASS_CONSTANT);
        assert inScope instanceof TypeScope;
        String in = indexedConstant.getIn();
        if (in != null) {
            typeName = in;
        } else {
            typeName = inScope.getName();
        }
    }

    ClassConstantElementImpl(TypeScopeImpl inScope, ClassConstantDeclarationInfo clsConst) {
        super(inScope, clsConst, PhpModifiers.EMPTY);
        typeName = inScope.getName();
    }

    @Override
    public String getNormalizedName() {
        return typeName+super.getNormalizedName();
    }
}
