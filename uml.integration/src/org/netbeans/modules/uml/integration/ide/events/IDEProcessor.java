/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.integration.ide.DiagramKind;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.integration.ide.dialogs.ExceptionDialog;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;

/**
 * Processes the events recieved from the IDE.  The events are translated into
 * GDPro specific commands to accomplished the desired action.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-06-14  Darshan     Added check to screen out primitives from the
 *                              search for attribute types in the Describe
 *                              model.
 *   2  2002-06-19  Darshan     Removed unnecessary conversions from Java names
 *                              to UML names, fixed bugs in attribute updates.
 *   3  2002-06-20  Darshan     Changed behaviour of attribute type change to
 *                              delete the IAttribute if a navigable association
 *                              is created for it and to avoid modifying
 *                              existing associations (if an association already
 *                              exists, the IAttribute is not deleted).
 *   4  2002-06-21  Darshan     Reformatted, fixed attribute type handling.
 *   5  2002-07-24  Mukta       Change for displaying role name in the model on
 *                              creating a navigable association.
 */
public class IDEProcessor implements EventProcessor {
//    JavaClassUtils javaUtil = new JavaClassUtils();

    // Constructor/Destructor
    public IDEProcessor() {
    }

    /**
     * Start a method transaction.  A method tranaction must be started before
     * any updates to the method can begin.  The method transaction that is
     * returned is to be used in all messages that is to update the method.
     * When locating a method the method name and parameters is used as the
     * signature of the method.
     *
     * @param trans The symbol transaction.
     * @param method The information needed to locate the method.
     * @see MethodTransaction
     * @see #beginClassTransaction(ClassInfo info, IGDSystem system)
     */
    public MethodTransaction beginMethodTransaction(SymbolTransaction trans,
                                                      ConstructorInfo info) {
        return new MethodTransaction(trans, info);
    }

    /**
     * Issue a command to Describe to delete a class symbol.
     * @param state The transaction to act upon.
     */
    public void deleteClass(SymbolTransaction state) {
        return;
    }

    /**
     * Issue a command to Describe to delete a method from a class symbol.
     * @param state The transaction to act upon.
     */
    public void deleteMethod(MethodTransaction state) {
        try {
            IOperation attr = state.getOperation();
            if(attr != null) {
                attr.delete();
            }
        } catch(Exception E) {
            String msg = "Error occured while deleting a method from Describe.";
            ExceptionDialog.showExceptionError(msg, E);
        }
    }

    /**
     * Issue a command to Describe to delete a data member from a class symbol.
     * @param state The transaction to act upon.
     */
    public void deleteMember(MemberTransaction state) {
        try {
            IStructuralFeature attr = state.getAttribute();
            if (attr != null)
                attr.delete();
        } catch(Exception E) {
            String msg = "Error occured while deleting a data member from " +
                         "Describe.";
            ExceptionDialog.showExceptionError(msg, E);
        }
    }

    /**
     * Issue a command to Describe to update the documentation of a class
     * symbol.
     *
     * @param state The transaction to act upon.
     * @param comment The new value of the symbols docuemtation.
     */
    public void updateClassComment(SymbolTransaction state,String comment) {
        IClassifier clazz    = state.getSymbol();
        try {
            if((clazz != null)) {
                clazz.setDocumentation(comment);
            }
        } catch(Exception E) {
            String msg = "Error occured while updating a class comment in " +
                         "Describe.";
            ExceptionDialog.showExceptionError(msg, E);
        }
    }

    /**
     * Issue a command to Describe remove all imports from a class symbol.
     * @param state The transaction to act upon.
     */
    public void clearImports(SymbolTransaction state) {
//        IClassifier clazz    = state.getSymbol();
//        try {
//            if((clazz != null)) { // dont know  the API for this
//                //clazz.add.get.getI.setDocumentation(comment);;
//            }
//        } catch(Exception E) {
//            String msg = "Error occured while updating the classes imports " +
//                         "in Describe.";
//            ExceptionDialog.showExceptionError(msg, E);
//        }
    }

    /**
     * Issue a command to Describe remove all exceptions from a class symbol.
     * @param state The transaction to act upon.
     */
    public void clearExceptions(MethodTransaction state) {
        IOperation oper = state.getOperation();
        if(oper == null)
            return;

        ETList<IClassifier> exceptions = state.getOperation()
                                              .getRaisedExceptions();
        if(exceptions != null) {
            int count = exceptions.getCount();
            for (int i = 0; i < count; i++)
                state.getOperation().removeRaisedException(exceptions.item(i));
        }
    }

    /**
     * Issue a command to Describe add an import to a class symbol.
     * @param state The transaction to act upon.
     * @parma value The import to add.
     */
    public void addImport(SymbolTransaction state,String value) {
        return;
    }

    /**
     * Issue a command to Describe add an interface implementation to a class
     * symbol.
     *
     * @param state The transaction to act upon.
     * @param pName The name of the package that contains the interface.
     * @param name The name of the interface.
     */
    public void addInterface(SymbolTransaction state, String pName,
                             String name) {
        IClassifier sym = state.getSymbol();
        IClassifier interfaceSym = getClassSymbol(state, name, pName, true);
        boolean   isInterface  = isClassAnInterface(sym);

        if(doesImplementationExist(sym, interfaceSym) == false)
        {
            if(sym != null && interfaceSym != null) {
                IRelationFactory rFac = new RelationFactory();
                IProductDiagramManager diaMana = UMLSupport.getUMLSupport()
                                                    .getProduct()
                                                    .getDiagramManager();
                if (rFac == null) {
                    Log.out("GOT THE NULL RelationFactory");
                    return;
                }

                if (!isInterface) {
                    IDependency rel =
                        rFac.createImplementation(
                            sym,
                            interfaceSym,
                            UMLSupport.getCurrentProject()).getParamTwo();
                    addRelationshipLinkToDiagram(rel, diaMana.getCurrentDiagram());
                }
                else {
                    IGeneralization gen =
                        rFac.createGeneralization(interfaceSym, sym);
                    addRelationshipLinkToDiagram(gen, diaMana.getCurrentDiagram());
                }
            } else {
                Log.out("One of the things is null .......");
            }
        }
    }

    protected boolean doesImplementationExist(IClassifier sym, IClassifier interfaceSym)
    {
        boolean retVal = false;
        if(interfaceSym instanceof IInterface)
        {
            ETList < IImplementation > impls = sym.getImplementations();
            for(IImplementation curImpl : impls)
            {
                IClassifier contract = curImpl.getContract();
                IClassifier implementor = curImpl.getImplementingClassifier();
                
                if((contract.isSame(interfaceSym) == true) &&
                   (implementor.isSame(sym) == true))
                {
                    retVal = true;
                }
            }
        }
        
        return retVal;
    }
    
    public static void addRelationshipLinkToDiagram(IElement elem, IDiagram dia) {
        Log.out("Inside addRelationshipLinkToDiagram");
        if (dia == null) {
            IProductDiagramManager diagMan = UMLSupport.getUMLSupport()
                                                .getProduct()
                                                .getDiagramManager();
            dia = diagMan.getCurrentDiagram();
        }
        if(elem != null && dia != null
                    && dia.getDiagramKind() == DiagramKind.DK_CLASS_DIAGRAM) {
            // Check if we already have a presentation element
            if (!hasPresentationElement(dia, elem)) {
                Log.out("==== Creating presentation element in " + dia.getName() + "!");
                ICoreRelationshipDiscovery rel = dia.getRelationshipDiscovery();
                if (rel == null) {
                    Log.err("addRelationshipLinkToDiagram: Couldn't " +
                            "obtain relationship discovery element");
                    return;
                }
                try {
                    if (elem instanceof INavigableEnd) {
                        INavigableEnd nav = (INavigableEnd)  elem;

                        ETList<IElement> els = new ETArrayList<IElement>();
                        els.add(nav.getFeaturingClassifier());
                        els.add(nav.getOtherEnd2().getFeaturingClassifier());
                        
                        // TODO:
                        ETList<IPresentationElement> pElems
                            = rel.discoverCommonRelations(true, els);
                         if (elem != null) {
                             try {
                                 addLabel(pElems);
                             }
                             catch (Exception ex) {
                                 Log.stackTrace(ex);
                             }


                         }
                    }
                    rel.discoverCommonRelations(true);
                    IPresentationElement ipe
                        = rel.createPresentationElement(elem);
//                    dia.addPresentationElement(ipe);
                    if (elem != null && elem instanceof IAssociation) {
// TODO: meteora

//                        if (ipe instanceof IAssociationEdgePresentation) {
//                            IProductGraphPresentation edPre =
//                                (IProductGraphPresentation) ipe;
//                            ILabelManager lblMgr = edPre.getLabelManager();
//                            lblMgr.showLabel(4, true);
//                        }
                    }
                }
                catch (Exception e) {
                    Log.stackTrace(e);
                }
            }
        }
    }

    private static void addLabel(ETList<IPresentationElement> pElems) {
        int count = 0;
        if (pElems != null &&
            (count = pElems.getCount()) > 0) {
            Log.out("addRelationshipLinkToDiagram: Got " + count +
                    " presentation elements");
            IPresentationElement pElem = null;
            for (int i = 0; i < count; i++) {
                pElem = pElems.item(i);

// TODO: meteora
//                if (pElem instanceof IAssociationEdgePresentation) {
//                    IProductGraphPresentation edPre = 
//                        (IProductGraphPresentation) pElem;
//                    ILabelManager lblMgr = edPre.
//                        getLabelManager();
//                    Log.out("addRelationshipLinkToDiagram: Showing label");
//                    //if(!lblMgr.isDisplayed(4)){
//                        lblMgr.createInitialLabels();
//                        lblMgr.resetLabels();
//                        lblMgr.showLabel(4,true);
//                        //lblMgr.relayoutLabels();
//                    //}
//                }
//                else
//                    Log.out("Ignoring presentation element");
            }
        }
    }

    private static boolean hasPresentationElement(IDiagram diag, IElement el) {
        ETList<IElement> elements = diag.getElements();

        if (elements == null || elements.getCount() == 0)
            return false;

        String xmiid = el.getXMIID();
        for (int i = 0; i < elements.getCount(); ++i) {
            if (elements.item(i).getXMIID().equals(xmiid))
                return true;
        }

        return false;
    }

    /**
     * Issue a command to Describe remvoe an interface implementation from a
     * class symbol.
     *
     * @param state The transaction to act upon.
     * @param pName The name of the package that contains the interface.
     * @param name The name of the interface.
     */
    public void removeInterface(SymbolTransaction state, String pName,
                                String name) {

        String qualName = JavaClassUtils.formFullClassName(pName, name);
        String sName = "";

        IClassifier cl = state.getSymbol();
        if (cl instanceof IClass) {
            ETList<IImplementation> impls = 
                    state.getSymbol().getImplementations();
            if(impls == null)
                return;
            int size = impls.getCount();
            for(int i=0; i<size; i++) {
                IImplementation impl = impls.item(i);
                sName = JavaClassUtils.getFullyQualifiedName(
                                            impl.getSupplier());
                if(impl != null && sName.equals(qualName)) {
                    IClassifier interf = (IClassifier) impl.getSupplier();
                    impl.delete();
                    if ((JavaClassUtils.isReferenceClass(interf) ||
                         (interf instanceof IInterface &&
                          ClassInfo.getSymbolFilename(interf) == null))
                            && JavaClassUtils.isOrphan(interf)) {
                        boolean rt = UMLSupport.isRoundTripEnabled();
                        UMLSupport.setRoundTripEnabled(false);
                        interf.delete();
                        UMLSupport.setRoundTripEnabled(rt);
                    }
                }
            }
        } else if (cl instanceof IInterface) {
            ETList<IGeneralization> gens = state.getSymbol().getGeneralizations();
            if(gens == null)
                return;
            int size = gens.getCount();
            for(int i=0; i<size; i++) {
                IGeneralization gen = gens.item(i);
                sName = JavaClassUtils.getFullyQualifiedName(gen.getGeneral());
                if(gen != null && sName.equals(qualName)) {
                    gen.delete();
                }
            }
        }

    }

    /**
     * Issue a command to Describe add an exception to a class symbol.
     * @param state The transaction to act upon.
     * @param value The exception to add.
     */
    public void addException(MethodTransaction state, String value) {
    }

    /**
     * Issue a command to Describe add a collection of exceptions to a class
     * symbol.
     *
     * @param state The transaction to act upon.
     * @param value The exceptions to add.
     */
    public void setExceptions(MethodTransaction state,String value) {

        setAttribute(state, "ThrowType", value);
    }

    /**
     * Issue a command to Describe to updates a attibute on a class symbol.  The
     * attribute must be specified in a fully qualified manner.
     * <br>
     * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
     *
     * @param state The transaction to act upon.
     * @param attr The fully qualified name of the attribute.
     * @param value The new value of the attribute.
     */
    public void setAttribute(SymbolTransaction state, String attr,
                             String value) {
    }

    /**
     * Issue a command to Describe to updates a attibute on a <b>Operations</b>
     * attribute.  The attribute must be specified in a fully qualified manner.
     * <br>
     * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
     *
     * @param state The transaction to act upon.
     * @param attr The fully qualified name of the attribute.
     * @param value The new value of the attribute.
     */
    public void setAttribute(MethodTransaction state, String attrName,
                             String value) {
    }

    /**
     * Issue a command to Describe to updates a attibute on a <b>Attributes</b>
     * attribute.  The attribute must be specified in a fully qualified manner.
     * <br>
     * <b>Example:</b> setAttribute("ClassIdentifier.FullyScopedName", name);
     *
     * @param state The transaction to act upon.
     * @param attr The fully qualified name of the attribute.
     * @param value The new value of the attribute.
     */
    public void setAttribute(MemberTransaction state, String attrName,
                             String value) {
    }

    /**
     * Issue a command to Describe to remove a generalization associated with a
     * class symbol.
     * @param state The transaction to act upon.
     * @param value The value.
     */
    public void removeSuperClass(SymbolTransaction state, String cName,
                                 String pName) {
    }

    public void addSuperClass(SymbolTransaction state, String className,
                              String packageName) {
    }

    /**
     * Issue a command to Describe to add a generalization associated with a
     * class symbol.
     * @param state The transaction to act upon.
     * @param value The value.
     */
    public void setMethodParameters(MethodTransaction state, String params) {
        setAttribute(state, "Parameters", params);
    }

    /**
     * Issue a command to Describe to updates a tagged value on a class symbol.
     *
     * @param state The transaction to act upon.
     * @param tag The name of the tag to be set.
     * @param value The new value of the attribute.
     */
    public void setTaggedValue(SymbolTransaction state, String tag,
                               String value) {
    }

    /**
     * Issue a command to Describe to updates a tagged value on a
     * <b>Operations</b> attribute.
     *
     * @param state The transaction to act upon.
     * @param tag The name of the tag to be set.
     * @param value The new value of the attribute.
     */
    public void setTaggedValue(MethodTransaction state, String tag,
                               String value) {
    }

    /**
     * Issue a command to Describe to updates a tagged value on a
     * <b>Attributes</b> attribute.
     *
     * @param state The transaction to act upon.
     * @param tag The name of the tag to be set.
     * @param value The new value of the attribute.
     */
    public void setTaggedValue(MemberTransaction state, String tag,
                               String value) {
    }

    /**
     * Updates a data members type.  If the data member is current a
     * implementation attribute then then the implementation attribute is
     * updated.  Otherwise, the attribute on the CLD_Class symbol is updated.
     * The parameter fullName is the fully qualified name of the data member's
     * type .  However if the data member's type is a class that Describes knows
     * about then the data member will reside on a <B>Class Associaton</B>
     * between the containing class symbol and the symbol that represents the
     * data type of the data member.
     *
     * @param state The data of the attribute in Describe.
     * @param fullName The fully qualified name of the data member type.
     * @param soruceName How the data member is specified in code.
     */
    public void updateMemberType(MemberTransaction state, String fullName,
                                 String sourceName) {
        Log.out("Member type = " + fullName);
        handleUpdateImplAttr(state, fullName, sourceName);
    }

    public static INavigableEnd makeNavigableAssociation(IClassifier clazz,
                                                         IClassifier type,
                                                         String name) {

        RelationFactory fact = new RelationFactory();
        IProject proj = clazz.getProject();
        IAssociation assoc = fact.createAssociation(clazz, type, proj);

        // set the navigability for the association added
        ETList<IAssociationEnd> assoEnds = assoc.getEnds();

        INavigableEnd nav = null;
        //IAssociationEnd ase = null;
        if(assoEnds != null) {
            int count = assoEnds.getCount();
            IAssociationEnd assoEnd;
            for(int i = 0; i < count; i++) {
                assoEnd = assoEnds.item(i);
                if(assoEnd.getParticipant().getName().equals(clazz.getName())) {
                    continue;
                }
                // make this end navigable
                if(!assoEnd.getIsNavigable()) {
                    assoEnd.setName(name);
                    nav = assoEnd.makeNavigable();
                    break;
                }
            }
        }

        IProductDiagramManager diaMana = UMLSupport.getUMLSupport()
                                                     .getProduct()
                                                     .getDiagramManager();
        IDiagram dia = diaMana.getCurrentDiagram();
        if(dia != null) {
            addRelationshipLinkToDiagram(assoc, dia);
        }

        return nav;
    }

    /**
     * Creates a navigable association for the given MemberTransaction, with
     * the given IClassifier. This assumes that the MemberTransaction already
     * has an IAttribute associated with it. The IAttribute of the transaction
     * will be deleted from the model and replaced by the navigable association.
     *
     * @param state   The MemberTransaction
     * @param type    The IClassifier of the attribute's type.
     */
    protected void createAssociation(MemberTransaction state,
                                     IClassifier type, String fullName) {
        IStructuralFeature attr = state.getAttribute();

        IClassifier clazz = state.getSymbol();
        // Check if there's already an association; if there is, don't create
        // another.
        IAssociation assoc = JavaClassUtils.findAssociation(clazz, type);

        if (assoc != null)
            return ;

        String doc  = attr.getDocumentation();
        boolean vol = attr.getIsVolatile(),
                fin = attr.getIsFinal(),
                tra = attr.getIsTransient(),
                sta = attr.getIsStatic();
        int     vis = attr.getVisibility();

        IAssociation attrAssoc =
            JavaClassUtils.findAssociation(clazz, attr.getType());
        if (attrAssoc != null) {
            Log.out(
                "createAssociation: Found association between "
                    + clazz.getName()
                    + " and "
                    + attr.getTypeName()
                    + ", deleting it");
            attrAssoc.delete();
        }
        else {
            Log.out(
                "createAssociation: Found no association, deleting attribute "
                    + attr.getName());
            attr.delete();
        }

        IAttribute nav = null;
        if (type == null && fullName != null) {
            IAttributeChangeFacility facility =
                EventManager.getAttributeFacility();
            if (facility == null) {
                Log.impossible(
                    "createAssociation: No IAttributeChangeFacility?");
                return ;
            }
            Log.out(
                "createAssociation: Creating attribute : "
                    + attr.getName()
                    + ": "
                    + fullName);
            //retVal = facility.createAttribute(name, sourceName, sym);
            String umlFullName = JavaClassUtils.convertJavaToUML(fullName);
            nav =
                facility.addAttribute2(
                    attr.getName(),
                    umlFullName,
                    clazz,
                    true,
                    false);
        }
        else
            nav =
                (IAttribute) makeNavigableAssociation(clazz,
                    type,
                    attr.getName());

        boolean rtEnabled = UMLSupport.isRoundTripEnabled();
        UMLSupport.setRoundTripEnabled(false);

        if (nav != null) {
            if (doc != null)
                nav.setDocumentation(doc);
            nav.setIsVolatile(vol);
            nav.setIsFinal(fin);
            nav.setIsStatic(sta);
            nav.setIsTransient(tra);
            nav.setVisibility(vis);
            //attr.delete();

            state.setAttribute(nav);
        }
        UMLSupport.setRoundTripEnabled(rtEnabled);
    }

    protected void removeAssociation(MemberTransaction state,
                                     String fullName,
                                     String sourceName) {
        INavigableEnd attr = (INavigableEnd) state.getAttribute();
        //IAttribute attr = (IAttribute) state.getAttribute();

        IClassifier clazz = state.getSymbol();

        String name = attr.getName();
        String doc  = attr.getDocumentation();
        boolean vol = attr.getIsVolatile(),
                fin = attr.getIsFinal(),
                tra = attr.getIsTransient(),
                sta = attr.getIsStatic();
        int     vis = attr.getVisibility();

        attr.getAssociation().delete();

        IAttribute at = clazz.createAttribute(sourceName, name);
        clazz.addAttribute(at);

        boolean rtEnabled = UMLSupport.isRoundTripEnabled();
        UMLSupport.setRoundTripEnabled(false);

        if (doc != null)
            at.setDocumentation(doc);
        at.setIsVolatile(vol);
        at.setIsFinal(fin);
        at.setIsStatic(sta);
        at.setIsTransient(tra);
        at.setVisibility(vis);

        UMLSupport.setRoundTripEnabled(rtEnabled);

        state.setAttribute(at);
    }

    /**
     * The work horse of updateMemberType that updates an implemenation
     * attribute.  If the type ot the data member is no longer in Describe then
     * the data member is moved to the containing classes symbol.  Otherwise, a
     * class association is created to the data members type and the data member
     * is added as a implementation attribute.
     *
     * @param state The data of the attribute in Describe.
     * @param fullName The fully qualified name of the data member's type.
     * @param soruceName How the data member is specified in code.
     */
    protected void handleUpdateImplAttr(MemberTransaction state,
                                        String fullName, String sourceName) {
        IStructuralFeature attr = state.getAttribute();

        MemberInfo modelInfo = new MemberInfo(attr);
//        String collectionType = UMLSupport.getUMLSupport()
//                                            .getCollectionOverride();
        String collectionType = modelInfo.getCollectionOverrideDataType();
        if (modelInfo.isUseCollectionOverride() && collectionType != null
               && (collectionType.equals(sourceName) ||
                   collectionType.equals(fullName)))
            return ;

        int oldMul = MemberInfo.getMultiplicity(attr),
            newMul = MemberInfo.getMultiplicity(sourceName);

        fullName   = MemberInfo.getTypeName(fullName);
        sourceName =
            JavaClassUtils.getInnerClassName(
                MemberInfo.getTypeName(sourceName));

        String modelFullName =
            JavaClassUtils.getFullyQualifiedName(attr.getType());

        Log.out("handleUpdateImplAttr: Attribute type full name : " + fullName 
            + ", source name : " + sourceName);
        Log.out(
            "handleUpdateImplAttr: Attribute type in model, full = "
                + modelFullName + ", unqualified = " + attr.getTypeName());
        
        // set the type of attribute to be added to class
        if ((attr.getTypeName().equals(sourceName)
                || modelFullName.equals(fullName))
                && oldMul == newMul)
            return;

        String umlName = JavaClassUtils.convertJavaToUML(fullName);
        Log.out("handleUpdateImplAttr: Setting attribute type to " + umlName);
        attr.setType2(umlName);

        Log.out("Setting multiplicity for '" + attr.getName()
                           + "' to " + newMul + " (was " + oldMul + ")");
        MemberInfo.setMultiplicity(attr, newMul, oldMul);

        /*
        // Add the association if doesn't exist already
        boolean isAttr = attr instanceof IAttribute;

        IClassifier attrClazz = JavaClassUtils.findClassSymbol(fullName);
        if (attrClazz != null && !JavaClassUtils.isPrimitive(fullName)) {
            Log.out(
                "handleUpdateImplAttr: Creating association for " + fullName);
            createAssociation(state, attrClazz, fullName);
        } else {
            Log.out(
                "handleUpdateImplAttr: Removing association, creating " +                "attribute for " + fullName);
            if (!isAttr)
                removeAssociation(state, fullName, sourceName);
        }
         */
    }

    /**
     * The work horse of updateMemberType that updates an attribute on a CLD_Class
     * symbol.   If the new type of the data member is known by Describe the
     * data member will be moved to a Class Association and made an implemenation
     * attribute.  If an association does not exist between the data members containing
     * class and the data type then the association will be created.
     *
     * @param state The data of the attribute in Describe.
     * @param fullName The fully qualified name of the data member's type.
     * @param soruceName How the data member is specified in code.
     */
    protected void handleUpdateSymbolAttr(MemberTransaction state, String fullName, String type) {
        /*
           IGDAttribute attr = state.getAttribute();
           try {
           IGDSymbol    ownerSym = attr.getSymbol();
           IGDSystem    system   = state.getSystem();
           IGDSymbols   symbols  = system.isDuplicated4("CLD_Class.ClassIdentifier.FullyScopedName", fullName);

           RelationshipHelper helper = new RelationshipHelper(system);
           if(symbols.getCount() > 0) {
        // Get the relationship and the role that the new type plays
        String attrRole = "EndClass";
        IGDRelation rel = helper.findRelationship(ownerSym, "CLD_ClassAssociation", fullName, "CLD_EndClass");
        if(rel == null) {
        // Try search for the start end
        attrRole = "StartClass";
        rel = helper.findRelationship(ownerSym, "CLD_ClassAssociation", fullName, "CLD_StartClass");
        }

        // If still null the new have to create the relationship.
        IGDSymbol linkSym = null;
        if(rel == null) {
        IGDSymbol otherSym = symbols.item(0);
        linkSym = helper.createRelationship("CLD_ClassAssociation",
        ownerSym, "CLD_StartClass",
        otherSym, "CLD_EndClass",
        "CLD_ClassAssociation","CLD_ClassAssociation");

        // Now set the navigiablity
        IGDAttribute topAttr = linkSym.getAttributes();
        if(topAttr != null) {
        topAttr.setData3("Role1Navigability", "Composition", true);
        topAttr.setData3("Role2Navigability", "Navigable", true);
        }

        // Now I have to find the relationship because the API does not return
        // the new relationship
        attrRole = "EndClass";
        rel = helper.findRelationship(ownerSym, "CLD_ClassAssociation", otherSym, "CLD_EndClass");
        }

        if (linkSym != null) {
        // now move the attribute to the link
        IGDAttribute topLevel = linkSym.getAttributes();
        IGDAttribute newAttr  = topLevel.createSubAttribute("Attributes");
        newAttr.setData2("RoleIdentifier", attrRole);
        copyAttribute(attr, newAttr);

        state.setAttribute(newAttr);
        state.setMemberRelationship(rel);
        state.setIsImplAttribute(true);

        attr.delete();
        }
        } else {
        attr.setData2("Type", type);
        }
        } catch(Exception E) {
        String msg = "Error occured while updating a data members type.";
        ExceptionDialog.showExceptionError(msg, E);
        }
        */
    }

    /**
     * Generic method for setting the value of a Describe's attribute.
     * @param sym The symbol to update.
     * @param attr The name of the attribute to update.
     * @param value The new value of the symbols attribute.
     */
    protected void setAttribute(IClass sym,String attr,String value) {
        /*
           try {
           if(sym != null) {
           IGDAttribute curAttr = sym.getSubAttribute(attr);
           if((curAttr == null) || (curAttr.getIsActual() == false)) {
           IGDAttribute topLevel = sym.getAttributes();
           curAttr = topLevel.createSubAttribute(attr);
           }

           if((curAttr != null) && (curAttr.getIsActual() == true)) {
           curAttr.setData(value);
           }
           }
           } catch(Exception E) {
           String msg = "Error occured while updating a symbols attribute.";
           msg += "Attribute Name: " + attr + " Value: " + value;
           ExceptionDialog.showExceptionError(msg, E);
           }
           */
    }

    /**
     * Retievies an interface from Describe.  If the interface does not current exist
     * in Describe it will be created as a <b>Reference Class</b>
     * @param trans The symbols state data.
     * @param name The name of the Interface class.
     * @param packgeName The name of the package that the interface resides.
     */
    /*
       private IClassifier getInterfaceSymbol(SymbolTransaction trans, String name, String packageName) {
       IClassifier retVal = getClassSymbol(trans, name, packageName);

       try {
       setAttribute(retVal, "ClassType", "Abstract");
       setAttribute(retVal, "ClassFormat", "interface");

       IGDAttribute sAttr = retVal.getSubAttribute("Stereotype.Stereotype");
       String stereotype = sAttr.getData();
       if(stereotype.length() <= 0) {
       sAttr.setData("Interface");
       }
       } catch(Exception E) {
       String msg = "Error occured while retrieving an interface class from Describe.";
       ExceptionDialog.showExceptionError(msg, E);
       }

       return retVal;
       }
       */

    /**
     * Retievies an class symbol from Describe.  If the class symbol does not current exist
     * in Describe it will be created as a <b>Reference Class</b>
     * @param trans The symbols state data.
     * @param name The name of the class.
     * @param packgeName The name of the package that the class resides.
     */
    private IClassifier getClassSymbol(SymbolTransaction trans, String name, String packageName, boolean isInterface) {
        IClassifier retVal = null;

        if(name.length() > 0) {
            try {
                //IProject proj = UMLSupport.getCurrentProject();
                String qualifiedName = JavaClassUtils.formFullClassName(packageName, name);
                IClassifier sym = JavaClassUtils.findClassSymbol(qualifiedName);
                if (sym == null) {
                    boolean evt = UMLSupport.isRoundTripEnabled();
                    UMLSupport.setRoundTripEnabled(false);
                    try {
                        sym = JavaClassUtils.createDataType(
                                JavaClassUtils.formFullClassName(packageName, name));
                        //sym = trans.createClass(name, packageName, isInterface, null, ElementInfo.CREATE);
                        // Tag it as a reference class
                        //JavaClassUtils.setReferenceClass(sym, true);
                    } finally {
                        UMLSupport.setRoundTripEnabled(evt);
                    }
                }
                retVal = sym;
            } catch (Exception e) {
                Log.stackTrace(e);
            }
        }
        return retVal;
    }

    /**
     * Test if a symbol is a Interface.  First the symbol is verified to be a
     * CLD_Class symbol.  The the class format is checked if it is a interface.
     *
     * @param sym The symbol to check.
     * @param True if the symbol is an interface, false otherwise.
     */
    protected boolean isClassAnInterface(IClassifier sym) {
        boolean retVal = false;

        try {
            if(sym instanceof IInterface ||
                    sym instanceof IInterface) {
                return true;
            }
        } catch(Exception E) {
            String msg = "Error occured while reteiving a format of a class in Describe.";
            ExceptionDialog.showExceptionError(msg, E);
        }

        return retVal;
    }
}
