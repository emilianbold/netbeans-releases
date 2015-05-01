/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.support.MemberInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public final class InsertPoint {
    public static final InsertPoint DEFAULT = new InsertPoint(null, null, null, Integer.MIN_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.default")); // NOI18N
    private final int index;
    private final String description;
    private final CsmOffsetable elemDecl;
    private final CsmOffsetable elemDef;
    private final CsmClass clazz;

    public static boolean initInsertPoints(JComboBox jComboInsertPoint, CsmClass encloser) {
        List<InsertPoint> result = new ArrayList<>();
        int idx = 0;
        boolean hasOutOfClassMemberDefinitions = false;
        for (CsmMember member : encloser.getMembers()) {
            if (CsmKindUtilities.isMethod(member)) {
                CsmMethod method = (CsmMethod) member;
                CsmFunction definition = ((CsmFunction)method).getDefinition();
                InsertPoint ip = new InsertPoint(encloser, method, definition, idx + 1, NbBundle.getMessage(
                        EncapsulateFieldPanel.class,
                        "MSG_EncapsulateFieldInsertPointMethod", // NOI18N
                        MemberInfo.create(method).getHtmlText()
                        ));
                if (definition != null && definition != method) {
                    hasOutOfClassMemberDefinitions = true;
                }
                result.add(ip);
            }
            ++idx;
        }
        jComboInsertPoint.addItem(InsertPoint.DEFAULT);
        if (!result.isEmpty()) {
            InsertPoint first = new InsertPoint(encloser, null, null, Integer.MIN_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.first")); // NOI18N
            InsertPoint last = new InsertPoint(encloser, null, null, Integer.MAX_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.last")); // NOI18N
            jComboInsertPoint.addItem(first); // NOI18N
            jComboInsertPoint.addItem(last); // NOI18N
            for (InsertPoint ip : result) {
                jComboInsertPoint.addItem(ip);
            }
        }
        jComboInsertPoint.setSelectedItem(InsertPoint.DEFAULT);
        return hasOutOfClassMemberDefinitions;
    }


    private InsertPoint(CsmClass clazz, CsmOffsetable elemDecl, CsmOffsetable elemDef, int index, String description) {
        this.index = index;
        this.description = description;
        this.elemDecl = elemDecl;
        this.elemDef = elemDef;
        this.clazz = clazz;
    }

    public CsmClass getContainerClass() {
        return clazz;
    }

    public CsmOffsetable getElementDeclaration() {
        return elemDecl;
    }

    public CsmOffsetable getElementDefinition() {
        return elemDef;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return description;
    }

}
