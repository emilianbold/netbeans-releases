package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;

class ClzConstantElementImpl extends ModelElementImpl implements ClassConstantElement {
    private String typeName;
    static ClzConstantElementImpl createClzConstantElementImpl(TypeScopeImpl inScope,
            ClassConstantDeclarationInfo clsConst) {
        return new ClzConstantElementImpl(inScope, clsConst);
    }

    ClzConstantElementImpl(TypeScopeImpl inScope, IndexedConstant indexedConstant) {
        super(inScope, indexedConstant, PhpKind.CLASS_CONSTANT);
        assert inScope instanceof TypeScope;
        String in = indexedConstant.getIn();
        if (in != null) {
            typeName = in;
        } else {
            typeName = inScope.getName();
        }
    }

    ClzConstantElementImpl(TypeScopeImpl inScope, ClassConstantDeclarationInfo clsConst) {
        super(inScope, clsConst, PhpModifiers.EMPTY);
        typeName = inScope.getName();
    }

    @Override
    StringBuilder golden(int indent) {
        String prefix = "";//NOI18N
        for (int i = 0; i <
                indent; i++) {
            prefix += "  ";//NOI18N
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(toString()).append("\n");//NOI18N

        return sb;
    }
    @Override
    public String getNormalizedName() {
        return typeName+super.getNormalizedName();
    }
}
