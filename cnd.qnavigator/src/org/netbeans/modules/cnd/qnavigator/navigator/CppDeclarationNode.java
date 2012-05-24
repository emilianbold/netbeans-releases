/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.qnavigator.navigator;


import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.nodes.Children;
import org.openide.util.CharSequences;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Navigator Tree node.
 */
public class CppDeclarationNode extends AbstractCsmNode implements Comparable<CppDeclarationNode> {
    private static final String FONT_COLORCONTROLSHADOW = "<font color='!controlShadow'>  "; // NOI18N
    private Image icon;
    private CsmObject object;
    private CsmFile file;
    private boolean isFriend;
    private boolean isSpecialization;
    private CsmFileModel model;
    private boolean needInitHTML = true;
    private CharSequence name;
    private CharSequence htmlDisplayName;
    private CharSequence scopeName = CharSequences.empty();
    private byte weight;
    private final InstanceContent ic;

    private CppDeclarationNode(CsmOffsetableDeclaration element, CsmFileModel model, List<IndexOffsetNode> lineNumberIndex) {
        this(element, model, null, lineNumberIndex);
    }

    private CppDeclarationNode(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier classifier, List<IndexOffsetNode> lineNumberIndex) {
        this(new NavigatorChildren(element, model, classifier, lineNumberIndex), element, model);
    }

    private CppDeclarationNode(Children children, CsmFile csmFile, CsmFileModel model) {
        this(children, new InstanceContent(), csmFile, csmFile, model);
    }

    private CppDeclarationNode(Children children, CsmOffsetableDeclaration element, CsmFileModel model, boolean isFriend) {
        this(children, element, model);
        this.isFriend = isFriend;
    }

    private CppDeclarationNode(Children children, CsmOffsetable element, CsmFileModel model) {
        this(children, new InstanceContent(), element, element.getContainingFile(), model);
    }

    private CppDeclarationNode(Children children, InstanceContent ic, CsmObject element, CsmFile file, CsmFileModel model) {
        super(children, new AbstractLookup(ic));
        this.object = element;
        this.file = file;
        this.model = model;
        this.weight = getObjectWeight();
        ic.add(element);
        ic.add(model.getFileObject());
        this.ic = ic;
    }

    private CharSequence createFunctionSpecializationHtmlDisplayName() {
        return CharSequences.create(CsmDisplayUtilities.htmlize(getDisplayName()) + FONT_COLORCONTROLSHADOW + scopeName); // NOI18N
    }
    private CharSequence createMemberHtmlDisplayName() {
        String aName = CsmDisplayUtilities.htmlize(scopeName); 
        String displayName = CsmDisplayUtilities.htmlize(getDisplayName()); // NOI18N
        String in = NbBundle.getMessage(getClass(), "LBL_inClass", aName); //NOI18N
        return CharSequences.create(displayName + FONT_COLORCONTROLSHADOW + in);
    }

    private CharSequence getFunctionSpecializationName(CsmObject csmObject) throws MissingResourceException {
        CsmFunction fun = (CsmFunction)csmObject;
        String specializationContainerName = fun.getQualifiedName().toString();
        int endInd = specializationContainerName.lastIndexOf("::");//NOI18N
        String in;
        if (endInd > 0) {
            specializationContainerName = CsmDisplayUtilities.htmlize(specializationContainerName.substring(0, endInd));
            in = NbBundle.getMessage(getClass(), "LBL_forClassSpecialization", specializationContainerName); //NOI18N
        } else {
            in = "";//NOI18N
        }
        return CharSequences.create(in);
    }

    private static CharSequence getClassifierName(CsmClassifier cls) {
        CharSequence clsName = cls.getName();
        if (CsmKindUtilities.isClass(cls) && CsmKindUtilities.isTemplate(cls)) {
            clsName = ((CsmTemplate)cls).getDisplayName();
        }
        return clsName;
    }

    private byte getObjectWeight(){
        try {
            if (CsmKindUtilities.isFunctionDefinition(object)) {
                CsmFunction function = ((CsmFunctionDefinition) object).getDeclaration();
                if (function != null && !function.equals(object) && CsmKindUtilities.isClassMember(function)) {
                    CsmClass cls = ((CsmMember) function).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = getClassifierName(cls);
                    } else if (CsmKindUtilities.isSpecialization(function)) {
                        isSpecialization = true;
                        scopeName = getFunctionSpecializationName(function);
                    }
                } else if (CsmKindUtilities.isSpecialization(object)) {
                    isSpecialization = true;
                    scopeName = getFunctionSpecializationName(object);
                }
            } else if (CsmKindUtilities.isVariableDefinition(object)) {
                CsmVariable variable = ((CsmVariableDefinition) object).getDeclaration();
                if (variable != null && !variable.equals(object) && CsmKindUtilities.isClassMember(variable)) {
                    CsmClass cls = ((CsmMember) variable).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = getClassifierName(cls);
                    }
                }
            } else if (CsmKindUtilities.isFunction(object) && CsmKindUtilities.isSpecialization(object)) {
                isSpecialization = true;
                scopeName = getFunctionSpecializationName(object);
            }
        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        if(CsmKindUtilities.isNamespaceDefinition(object)) {
            return 0*10+2;
        } else if(CsmKindUtilities.isFile(object)) {
            return 0*10+0;
        } else if(CsmKindUtilities.isNamespaceAlias(object)) {
            return 0*10+0;
        } else if(CsmKindUtilities.isUsing(object)) {
            return 0*10+1;
        } else if(CsmKindUtilities.isModule(object)) {
            return 0*10+3;
        } else if(CsmKindUtilities.isProgram(object)) {
            return 0*10+4;
        } else if(CsmKindUtilities.isClass(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isFriendClass(object)) {
            return 1*10+0;
        } else if(CsmKindUtilities.isClassForwardDeclaration(object)) {
            return 1*10+0;
        } else if (CsmKindUtilities.isEnumForwardDeclaration(object)) {
            return 1*10 + 0;
        } else if(CsmKindUtilities.isEnum(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isTypedef(object)) {
            return 1*10+2;
        } else if(CsmKindUtilities.isVariableDeclaration(object)) {
            return 2*10+0;
        } else if(CsmKindUtilities.isVariableDefinition(object)) {
            return 2*10+1;
        } else if(CsmKindUtilities.isFunctionDeclaration(object)) {
            return 3*10+0;
        } else if(CsmKindUtilities.isFunctionDefinition(object)) {
            return 3*10+1;
        } else if(CsmKindUtilities.isMacro(object)) {
            return 4*10+0;
        } else if(CsmKindUtilities.isInclude(object)) {
            return 5*10+0;
        }
        return 9*10+0;
    }

    @Override
    public CsmObject getCsmObject() {
        if (CsmKindUtilities.isCsmObject(object)) {
            return object;
        }
        return null;
    }

    int getOffset() {
        if (CsmKindUtilities.isOffsetable(object)) {
            return ((CsmOffsetable)object).getStartOffset();
        } else {
            return 0;
        }
    }

    void resetNode(CppDeclarationNode node){
        if (object != node.object) {
            ic.remove(object);
            object = node.object;
            ic.add(object);
        }
        if (object instanceof CsmFile) {
            file = (CsmFile) object;
        } else {
            file = ((CsmOffsetable)object).getContainingFile();
        }
        weight = node.weight;
        scopeName = node.scopeName;
        isFriend = node.isFriend;
        isSpecialization = node.isSpecialization;
        needInitHTML = node.needInitHTML;
        htmlDisplayName = node.htmlDisplayName;
        fireDisplayNameChange(null, null);
        fireIconChange();
    }
    
    @Override
    public int compareTo(CppDeclarationNode o) {
        int res = compareToWithoutOffset(o);
        if (res == 0) {
            int start1 = 0;
            if (object instanceof CsmOffsetable) {
                start1 = ((CsmOffsetable)object).getStartOffset();
            }
            int start2 = 0;
            if (o.object instanceof CsmOffsetable) {
                start2 = ((CsmOffsetable)o.object).getStartOffset();
            }
            res = start1 - start2;
        }
        return res;
    }

    public int compareToWithoutOffset(CppDeclarationNode o) {
        int res = 0;
        switch(model.getFilter().getSortMode()) {
            case Name:
                res = CharSequences.comparator().compare(scopeName, o.scopeName);
                if (res == 0) {
                    if (model.getFilter().isGroupByKind()) {
                        res = weight/10 - o.weight/10;
                        if (res == 0) {
                            res = getDisplayName().compareTo(o.getDisplayName());
                            if (res == 0) {
                                res = weight - o.weight;
                            }
                        }
                    } else {
                        res = getDisplayName().compareTo(o.getDisplayName());
                        if (res == 0) {
                            res = weight - o.weight;
                        }
                    }
                }
                break;
            case Offset:
                if (model.getFilter().isGroupByKind()) {
                    res = weight/10 - o.weight/10;
                }
                break;
        }
        return res;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String getHtmlDisplayName() {
        if(needInitHTML) {
            htmlDisplayName = createHtmlDisplayName();
            needInitHTML = false;
        }
        if (htmlDisplayName != null) {
            return htmlDisplayName.toString();
        }
        return null;
    }

    private CharSequence createHtmlDisplayName() {
        try {
            final CsmObject csmObject = getCsmObject();
            if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                if (scopeName.length() > 0) {
                    if (isSpecialization) {
                        return createFunctionSpecializationHtmlDisplayName();
                    } else {
                        return createMemberHtmlDisplayName();
                    }
                }
            } else if (CsmKindUtilities.isVariableDefinition(csmObject)) {
                if (scopeName.length() > 0) {
                    return createMemberHtmlDisplayName();
                }
            } else if (csmObject instanceof CsmFile) {
                if (model.getUnopenedProject() != null) {
                    // unopened project
                    return CharSequences.create("<font color='"+CsmDisplayUtilities.getHTMLColor(Color.red)+">"+ // NOI18N
                            NbBundle.getMessage(CppDeclarationNode.class, "UnopenedProject",  // NOI18N
                            ProjectUtils.getInformation(model.getUnopenedProject()).getDisplayName()));
                } else {
                    //Restricted code assistance
                    return CharSequences.create("<font color='"+CsmDisplayUtilities.getHTMLColor(Color.red)+">"+ // NOI18N
                            NbBundle.getMessage(CppDeclarationNode.class, "StandAloneFile")); // NOI18N
                }
            } else if (CsmKindUtilities.isFunction(csmObject) && CsmKindUtilities.isSpecialization(csmObject)) {
                if (scopeName.length() > 0) {
                    if (isSpecialization) {
                        return createFunctionSpecializationHtmlDisplayName();
                    }
                }
            }

        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        if (object instanceof CsmFile) {
            return " "+super.getDisplayName(); // NOI18N
        }
        return super.getDisplayName();
    }

    
    @Override
    public Image getIcon(int param) {
        if (icon != null){
            return icon;
        }
        if (file != null && !file.isValid()){
            if (object instanceof CsmOffsetable) {
                CsmOffsetable obj = (CsmOffsetable) object;
                object = null;
                Image aIcon = super.getIcon(param);
                object = obj;
                return aIcon;
            } else {
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/qnavigator/resources/exclamation.gif");
            }
        }
        if (isFriend) {
            CsmFriend csmObj = (CsmFriend)object;
            return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getFriendFunctionImage(csmObj);
        } else {
            if (object instanceof CsmOffsetable) {
                return super.getIcon(param);
            } else {
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/qnavigator/resources/exclamation.gif");
            }
        }
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        if (CsmKindUtilities.isOffsetable(object)){
            return new GoToDeclarationAction((CsmOffsetable) object);
        } else if (object instanceof CsmFile) {
            Project project = model.getUnopenedProject();
            if (project != null) {
                return new OpenContainingProjectAction(project);
            }
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (action != null){
            List<Action> list = new ArrayList<Action>();
            list.add(action);
            if (CsmRefactoringActionsFactory.supportRefactoring(file)) {
                list.add(RefactoringActionsFactory.renameAction());
                list.add(RefactoringActionsFactory.whereUsedAction());
                CsmObject obj = this.getCsmObject();
                if (CsmKindUtilities.isField(obj) || CsmKindUtilities.isClass(obj)) {
                    list.add(CsmRefactoringActionsFactory.encapsulateFieldsAction());
                } else if (CsmKindUtilities.isFunction(obj) && !CsmKindUtilities.isDestructor(obj)) {
                    list.add(CsmRefactoringActionsFactory.changeParametersAction());
                }
            }
            list.add(null);
            list.addAll(Arrays.asList(model.getActions()));
            return list.toArray(new Action[list.size()]);
        }
        return model.getActions();
    }

    public static CppDeclarationNode nodeFactory(CsmObject element, CsmFileModel model, boolean isFriend, List<IndexOffsetNode> lineNumberIndex){
        if (!(element instanceof CsmFile)) {
            if (!model.getFilter().isApplicable((CsmOffsetable)element)){
                return null;
            }
        }
        CppDeclarationNode node = null;
        if (CsmKindUtilities.isTypedef(element)){
            CsmTypedef def = (CsmTypedef) element;
            if (def.isTypeUnnamed()) {
                CsmClassifier cls = def.getType().getClassifier();
                if (cls != null && cls.getName().length()==0 &&
                   (cls instanceof CsmCompoundClassifier)) {
                    node = new CppDeclarationNode((CsmOffsetableDeclaration)element, model, (CsmCompoundClassifier) cls, lineNumberIndex);
                    node.name = ((CsmDeclaration)element).getName();
                    return node;
                }
            }
            node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
            node.name = ((CsmDeclaration)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if (CsmKindUtilities.isClassifier(element)){
            CharSequence name = ((CsmClassifier)element).getName();
            if (name.length()==0 && (element instanceof CsmCompoundClassifier)) {
                Collection<CsmTypedef> list = ((CsmCompoundClassifier)element).getEnclosingTypedefs();
                if (list.size() > 0) {
                    return null;
                }
            }
            if (CsmKindUtilities.isTemplate(element)) {
                if (((CsmTemplate)element).isExplicitSpecialization()) {
                    return null;
                }
            }
            if (CsmKindUtilities.isClassForwardDeclaration(element) || CsmKindUtilities.isEnumForwardDeclaration(element)) {
                node = new CppDeclarationNode(Children.LEAF, (CsmOffsetableDeclaration)element, model);
            } else {
                node = new CppDeclarationNode((CsmOffsetableDeclaration)element, model,lineNumberIndex);
            }
            node.name = getClassifierName((CsmClassifier)element);
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isNamespaceDefinition(element)){
            node = new CppDeclarationNode((CsmNamespaceDefinition)element, model, lineNumberIndex);
            node.name = ((CsmNamespaceDefinition)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isDeclaration(element)){
            if(CsmKindUtilities.isFunction(element)){
                node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
                node.name = CharSequences.create(CsmUtilities.getSignature((CsmFunction)element, true));
            } else if(CsmKindUtilities.isFunctionExplicitInstantiation(element)) {
                return null;
            } else {
                CharSequence name = ((CsmDeclaration)element).getName();
                if (name.length() == 0 && CsmKindUtilities.isVariable(element)){
                    return node;
                }
                node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
                node.name = name;
            }
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isEnumerator(element)){
            node = new CppDeclarationNode(Children.LEAF,(CsmEnumerator)element,model);
            node.name = ((CsmEnumerator)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isMacro(element)){
            node = new CppDeclarationNode(Children.LEAF,(CsmMacro)element,model);
            node.name = ((CsmMacro)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(element instanceof CsmInclude){
            node = new CppDeclarationNode(Children.LEAF,(CsmInclude)element,model);
            node.name = ((CsmInclude)element).getIncludeName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(element instanceof CsmFile) {
            node = new CppDeclarationNode(Children.LEAF,(CsmFile)element,model);
            node.name = ((CsmFile)element).getName();
            model.addFileOffset(node, (CsmFile)element, lineNumberIndex);
            return node;
        }
        return node;
    }
}
