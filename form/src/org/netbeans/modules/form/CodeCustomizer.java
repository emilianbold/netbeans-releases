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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.guards.SimpleSection;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import static org.netbeans.modules.form.CustomCodeData.*;

/**
 * This class controls the code customization - opens the
 * customizer dialog, reads and stores data, etc.
 * 
 * @author Tomas Pavek
 */

public class CodeCustomizer implements CustomCodeView.Listener {

    private FormModel formModel;

    // changed code data for components
    private Map<RADComponent, CustomCodeData> changedDataMap = new HashMap();

    private CustomCodeView codeView;

    private RADComponent customizedComponent; // actual selected component

    private CodeCustomizer(FormModel formModel) {
        this.formModel = formModel;
        codeView = new CustomCodeView(this);
        setupComponentNames();
    }

    public static void show(RADComponent metacomp) {
        CodeCustomizer customizer = new CodeCustomizer(metacomp.getFormModel());
        customizer.selectComponent(metacomp);
        customizer.show();
    }

    private void show() {
        JavaCodeGenerator codeGen = (JavaCodeGenerator) FormEditor.getCodeGenerator(formModel);
        codeGen.regenerateCode(); // to have fresh code for code completion

        DialogDescriptor dd = new DialogDescriptor(codeView,
                NbBundle.getMessage(CodeCustomizer.class, "TITLE_CodeCustomizer"), // NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        dd.setHelpCtx(new HelpCtx("gui.codecustomizer")); // NOI18N
        Object res = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION.equals(res)) {
            retreiveCurrentData();
            storeChanges();
        }
    }

    private void setupComponentNames() {
        Collection<RADComponent> allComps = formModel.getAllComponents();
        String[] compNames = new String[allComps.size()];
        int i = 0;
        for (RADComponent metacomp : allComps) {
            compNames[i++] = metacomp.getName();
        }
        Arrays.sort(compNames, 0, compNames.length-1);
        codeView.setComponentNames(compNames);
    }

    private void selectComponent(RADComponent metacomp) {
        customizedComponent = metacomp;
        CustomCodeData codeData = changedDataMap.get(metacomp);
        if (codeData == null) {
            codeData = JavaCodeGenerator.getCodeData(metacomp);
            codeData.check();
        }
        codeView.setCodeData(customizedComponent.getName(), codeData, getSourceFile(), getSourcePositions());
    }

    private FileObject getSourceFile() {
        return FormEditor.getFormDataObject(formModel).getPrimaryFile();
    }

    /**
     * @return array of 2 ints - positions of the customized code within entire
     *         source to be used for code completion; the first is the position
     *         of the init code, the second is position for the field variable
     *         declaration code
     */
    private int[] getSourcePositions() {
        SimpleSection sec = FormEditor.getFormDataObject(formModel).getFormEditorSupport().getInitComponentSection();
        return new int[] { sec.getText().indexOf('{') + 2 + sec.getStartPosition().getOffset(),
                           sec.getEndPosition().getOffset() + 1 };
    }

    private void retreiveCurrentData() {
        if (codeView.isChanged()) {
            changedDataMap.put(customizedComponent, codeView.retreiveCodeData());
        }
    }

    private void storeChanges() {
        for (Map.Entry<RADComponent, CustomCodeData> e : changedDataMap.entrySet()) {
            storeComponent(e.getKey(), e.getValue(), true);
        }
        changedDataMap.clear();
    }

    private void storeComponent(RADComponent metacomp,
                                CustomCodeData codeData,
                                boolean definite)
    {
        storeCodeCategory(metacomp, codeData, CodeCategory.CREATE_AND_INIT, definite);
        storeCodeCategory(metacomp, codeData, CodeCategory.DECLARATION, definite);
        storeDeclaration(metacomp, codeData.getDeclarationData(), definite);
    }

    private static void storeCodeCategory(RADComponent metacomp,
                                          CustomCodeData codeData,
                                          CodeCategory category,
                                          boolean definite)
    {
        int eCount = codeData.getEditableBlockCount(category);
        for (int i=0; i < eCount; i++) {
            EditableBlock eBlock = codeData.getEditableBlock(category, i);
            for (CodeEntry e : eBlock.getEntries()) {
                storeCodeEntry(metacomp, e, definite);
            }
        }
        int gCount = codeData.getGuardedBlockCount(category);
        for (int i=0; i < gCount; i++) {
            GuardedBlock gBlock = codeData.getGuardedBlock(category, i);
            if (gBlock.isCustomizable()) {
                storeCodeEntry(metacomp, gBlock.getCustomEntry(), definite);
            }
        }
    }

    private static void storeCodeEntry(RADComponent metacomp, CodeEntry entry, boolean definite) {
        FormProperty prop = entry.getTargetProperty();
        String code = entry.getCode();

        boolean firing;
        if (!definite) {
            firing = prop.isChangeFiring();
            prop.setChangeFiring(false);
        }
        else firing = true;

        try {
            if (entry.isPropertyPreInit()) {
                prop.setPreCode(code);
            }
            else if (entry.isPropertyPostInit()) {
                prop.setPostCode(code);
            }
            else if (prop instanceof RADProperty) { // custom code for bean property
                if (code != null) { // custom code specified
                    Object codeValue = new FormProperty.ValueWithEditor(
                        new RADConnectionPropertyEditor.RADConnectionDesignValue(code),
                        new RADConnectionPropertyEditor(prop.getValueType()));
                    prop.setValue(codeValue);
                }
                else if (JavaCodeGenerator.isPropertyWithCustomCode(prop)) { // default code
                    prop.restoreDefaultValue(); // cancel custom code
                    if (!definite && prop.getPreCode() == null && prop.getPostCode() == null)
                        prop.setPreCode("\n"); // set something to get the property generated again // NOI18N
                }
                // otherwise do nothing (property does not contain custom code)
            }
            else { // synthetic code property
                prop.setValue(code != null ? code : ""); // NOI18N
            }
        }
        catch (Exception ex) { // should not happen 
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        if (!definite)
            prop.setChangeFiring(firing);
    }

    private static void storeDeclaration(RADComponent metacomp, VariableDeclaration decl, boolean definite) {
        FormProperty varProp = (FormProperty) metacomp.getSyntheticProperty(
                JavaCodeGenerator.PROP_VARIABLE_LOCAL);
        FormProperty modifProp = (FormProperty) metacomp.getSyntheticProperty(
                JavaCodeGenerator.PROP_VARIABLE_MODIFIER);

        boolean firing;
        if (!definite) {
            firing = varProp.isChangeFiring();
            varProp.setChangeFiring(false);
            modifProp.setChangeFiring(false);
        }
        else firing = true;

        try {
            varProp.setValue(decl.local);
            int modif = decl.modifiers;
            if (modif < 0)
                modif = (((Integer)modifProp.getValue()).intValue() & ~Modifier.FINAL)
                        | (modif & Modifier.FINAL);
            modifProp.setValue(modif);
        }
        catch (Exception ex) { // should not happen 
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        if (!definite) {
            varProp.setChangeFiring(firing);
            modifProp.setChangeFiring(firing);
        }
    }

    // -----
    // CustomCodeView.Listener implementation

    public void componentExchanged(String compName) {
        retreiveCurrentData();
        selectComponent(formModel.findRADComponent(compName));
    }

    public void renameInvoked() {
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(CodeCustomizer.class, "CTL_RenameLabel"), // NOI18N
                NbBundle.getMessage(CodeCustomizer.class, "CTL_RenameTitle")); // NOI18N
        input.setInputText(customizedComponent.getName());

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(input))) {
            // code data must be saved to component before renaming
            retreiveCurrentData();
            CustomCodeData codeData = changedDataMap.get(customizedComponent);
            if (codeData != null) {
                NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(CodeCustomizer.class, "CTL_ApplyChangesLabel"), // NOI18N
                        NbBundle.getMessage(CodeCustomizer.class, "CTL_ApplyChangesTitle"), // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);
                if (!NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(confirm)))
                    return;

                storeComponent(customizedComponent, codeData, true);
                changedDataMap.remove(customizedComponent);
            }

            try {
                String newName = input.getInputText();
                if (!newName.equals("")) // NOI18N
                    customizedComponent.rename(newName);
            }
            catch (IllegalArgumentException e) {
                Exceptions.printStackTrace(e);
                return;
            }

            setupComponentNames();
            codeData = JavaCodeGenerator.getCodeData(customizedComponent);
            codeData.check();
            codeView.setCodeData(customizedComponent.getName(), codeData, getSourceFile(), getSourcePositions());
        }
    }

    public void declarationChanged() {
        // remeber the current data - we'll return to it so there is no change in the model
        CustomCodeData original = JavaCodeGenerator.getCodeData(customizedComponent);
        // write the customized configuration to the component
        retreiveCurrentData();
        CustomCodeData actual = changedDataMap.get(customizedComponent);
        storeComponent(customizedComponent, actual, false);
        // get the new code data for the changed configuartion
        CustomCodeData renewed = JavaCodeGenerator.getCodeData(customizedComponent);
        renewed.check();
        // restore the original data in the component
        storeComponent(customizedComponent, original, false);
        // set the new data to the view
        codeView.setCodeData(customizedComponent.getName(), renewed, getSourceFile(), getSourcePositions());
    }
}
