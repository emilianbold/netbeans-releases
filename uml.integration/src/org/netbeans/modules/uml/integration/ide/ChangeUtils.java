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

/*
 * File         : ChangeUtils.java
 * Version      : 1.1
 * Description  : Utility methods and constants for Describe model-source
 *                roundtrip.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.netbeans.modules.uml.integration.ide.events.MethodParameterInfo;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Utility methods and constants for Describe model-source roundtrip.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Created
 *   2  2002-04-25  Darshan     Changed method parameter display to include
 *                              parameter #0.
 *   3  2002-05-03  Sumitabh    Tweaked to display request type, element type,
 *                              method parameters and return type correctly.
 *   4  2002-05-03  Darshan     Made changes to match the new Wolverine
 *                              event model.
 *   5  2002-05-06  Darshan     Added constants and string arrays to identify
 *                              roundtrip change types. Updated the display
 *                              for class and interface roundtrip events.
 *   6  2002-05-22  Darshan     Changed attribute type display from
 *                              getType().getName() to getTypeName().
 *   7  2002-05-29  Darshan     Added constant for implementation relationships.
 *   8  2002-05-30  Darshan     Fixed bug in printing interfaces implemented
 *                              by a class.
 *   9  2002-06-05  Darshan     Commented out code to display the request type,
 *                              since the request types change frequently,
 *                              causing ArrayIndexOutOfBoundsExceptions when
 *                              indexing into reqDetailEnum[].
 *  10  2002-06-18  Darshan     Updated RequestDetailType enum.
 *
 * @author  Darshan
 * @version 1.1
 */
public class ChangeUtils {
    // Constants that indicate the nature of a model-source change.
    public static final short CT_MODIFY = 0;
    public static final short CT_DELETE = 1;
    public static final short CT_CREATE = 2;

    static final String[] changeTypes = new String[] {
        "CT_MODIFY",
        "CT_DELETE",
        "CT_CREATE",
    };

    public static final int RDT_NONE = 0;
    public static final int RDT_DOCUMENTATION_MODIFIED = 1;
    public static final int RDT_ELEMENT_DELETED = 2;
    public static final int RDT_NAME_MODIFIED = 3;
    public static final int RDT_VISIBILITY_MODIFIED = 4;
    public static final int RDT_ELEMENT_ADDED_TO_NAMESPACE = 5;
    public static final int RDT_RELATION_VALIDATE = 6;
    public static final int RDT_RELATION_MODIFIED = 7;
    public static final int RDT_RELATION_DELETED = 8;
    public static final int RDT_ATTRIBUTE_DEFAULT_MODIFIED = 9;
    public static final int RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED = 10;
    public static final int RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED = 11;
    public static final int RDT_CONCURRENCY_MODIFIED = 12;
    public static final int RDT_SIGNAL_ADDED = 13;
    public static final int RDT_SIGNAL_REMOVED = 14;
    public static final int RDT_PARAMETER_ADDED = 15;
    public static final int RDT_PARAMETER_REMOVED = 16;
    public static final int RDT_ABSTRACT_MODIFIED = 17;
    public static final int RDT_FEATURE_ADDED = 18;
    public static final int RDT_FEATURE_REMOVED = 19;
    public static final int RDT_STATIC_MODIFIED = 20;
    public static final int RDT_CONDITION_ADDED = 21;
    public static final int RDT_CONDITION_REMOVED = 22;
    public static final int RDT_QUERY_MODIFIED = 23;
    public static final int RDT_PARAMETER_DEFAULT_MODIFIED = 24;
    public static final int RDT_PARAMETER_DEFAULT_BODY_MODIFIED = 25;
    public static final int RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED = 26;
    public static final int RDT_PARAMETER_DIRECTION_MODIFIED = 27;
    public static final int RDT_CHANGEABILITY_MODIFIED = 28;
    public static final int RDT_MULTIPLICITY_MODIFIED = 29;
    public static final int RDT_TYPE_MODIFIED = 30;
    public static final int RDT_LOWER_MODIFIED = 31;
    public static final int RDT_UPPER_MODIFIED = 32;
    public static final int RDT_RANGE_ADDED = 33;
    public static final int RDT_RANGE_REMOVED = 34;
    public static final int RDT_ORDER_MODIFIED = 35;
    public static final int RDT_PACKAGE_NAME_MODIFIED = 36;
    public static final int RDT_TRANSIENT_MODIFIED = 37;
    public static final int RDT_NATIVE_MODIFIED = 38;
    public static final int RDT_VOLATILE_MODIFIED = 39;
    public static final int RDT_LEAF_MODIFIED = 40;
    public static final int RDT_RELATION_END_MODIFIED = 41;
    public static final int RDT_RELATION_END_ADDED = 42;
    public static final int RDT_RELATION_END_REMOVED = 43;
    public static final int RDT_DEPENDENCY_ADDED = 44;
    public static final int RDT_DEPENDENCY_REMOVED = 45;
    public static final int RDT_ASSOCIATION_END_MODIFIED = 46;
    public static final int RDT_ASSOCIATION_END_ADDED = 47;
    public static final int RDT_ASSOCIATION_END_REMOVED = 48;
    public static final int RDT_RELATION_CREATED = 49;
    public static final int RDT_FEATURE_MOVED = 50;
    public static final int RDT_FEATURE_DUPLICATED = 51;
    public static final int RDT_NAMESPACE_MODIFIED = 52;
    public static final int RDT_CHANGED_NAMESPACE = 53;
    public static final int RDT_NAMESPACE_MOVED = 54;
    public static final int RDT_FINAL_MODIFIED = 55;
    public static final int RDT_STRICTFP_MODIFIED = 56;
    public static final int RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED = 57;
    public static final int RDT_TRANSFORM = 58;
    public static final int RDT_EXCEPTION_ADDED = 59;
    public static final int RDT_EXCEPTION_REMOVED = 60;
    public static final int RDT_SIGNATURE_CHANGED = 61;
    public static final int RDT_SOURCE_DIR_CHANGED = 62;

    static final String[] reqDetailEnum = new String[] {
        "RDT_NONE",
        "RDT_DOCUMENTATION_MODIFIED",
        "RDT_ELEMENT_DELETED",
        "RDT_NAME_MODIFIED",
        "RDT_VISIBILITY_MODIFIED",
        "RDT_ELEMENT_ADDED_TO_NAMESPACE",
        "RDT_RELATION_VALIDATE",
        "RDT_RELATION_MODIFIED",
        "RDT_RELATION_DELETED",
        "RDT_ATTRIBUTE_DEFAULT_MODIFIED",
        "RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED",
        "RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED",
        "RDT_CONCURRENCY_MODIFIED",
        "RDT_SIGNAL_ADDED",
        "RDT_SIGNAL_REMOVED",
        "RDT_PARAMETER_ADDED",
        "RDT_PARAMETER_REMOVED",
        "RDT_ABSTRACT_MODIFIED",
        "RDT_FEATURE_ADDED",
        "RDT_FEATURE_REMOVED",
        "RDT_STATIC_MODIFIED",
        "RDT_CONDITION_ADDED",
        "RDT_CONDITION_REMOVED",
        "RDT_QUERY_MODIFIED",
        "RDT_PARAMETER_DEFAULT_MODIFIED",
        "RDT_PARAMETER_DEFAULT_BODY_MODIFIED",
        "RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED",
        "RDT_PARAMETER_DIRECTION_MODIFIED",
        "RDT_CHANGEABILITY_MODIFIED",
        "RDT_MULTIPLICITY_MODIFIED",
        "RDT_TYPE_MODIFIED",
        "RDT_LOWER_MODIFIED",
        "RDT_UPPER_MODIFIED",
        "RDT_RANGE_ADDED",
        "RDT_RANGE_REMOVED",
        "RDT_ORDER_MODIFIED",
        "RDT_PACKAGE_NAME_MODIFIED",
        "RDT_TRANSIENT_MODIFIED",
        "RDT_NATIVE_MODIFIED",
        "RDT_VOLATILE_MODIFIED",
        "RDT_LEAF_MODIFIED",
        "RDT_RELATION_END_MODIFIED",
        "RDT_RELATION_END_ADDED",
        "RDT_RELATION_END_REMOVED",
        "RDT_DEPENDENCY_ADDED",
        "RDT_DEPENDENCY_REMOVED",
        "RDT_ASSOCIATION_END_MODIFIED",
        "RDT_ASSOCIATION_END_ADDED",
        "RDT_ASSOCIATION_END_REMOVED",
        "RDT_RELATION_CREATED",
        "RDT_FEATURE_MOVED",
        "RDT_FEATURE_DUPLICATED",
        "RDT_NAMESPACE_MODIFIED",
        "RDT_CHANGED_NAMESPACE",
        "RDT_NAMESPACE_MOVED",
        "RDT_FINAL_MODIFIED",
        "RDT_STRICTFP_MODIFIED",
        "RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED",
        "RDT_TRANSFORM",
        "RDT_EXCEPTION_ADDED",
        "RDT_EXCEPTION_REMOVED",
        "RDT_SIGNATURE_CHANGED",
        "RDT_SOURCE_DIR_CHANGED",
    };

    public static final String REL_GENER = "Generalization";
    public static final String REL_REALZ = "Realization";
    public static final String REL_IMPL  = "Implementation";
    public static final String REL_ASSOS = "Association";

    private static void say(String s) {
        Log.out(s);
    }

    private static void say(String left, String right) {
        final int margin = 29;
        StringBuffer truth = new StringBuffer(left);
        if (left.length() < margin) {
            char[] buf = new char[margin - left.length()];
            Arrays.fill(buf, ' ');
            truth.append(buf);
        }
        truth.append(": ").append(right);
        Log.out(truth.toString());
    }

    private static String getVis(int mod) {
        return Modifier.toString(JavaClassUtils.getJavaModifier(mod));
    }

    public static void say(IChangeRequest req) {
        if (true) return ;
        say("Change type", Integer.toString(req.getState())
                            + " (" + changeTypes[req.getState()] + ")");
        int reqType = req.getRequestDetailType();
        say("Request detail type", Integer.toString(reqType)/* + " ("
                                    + reqDetailEnum[reqType] + ")"*/);
        if (reqType == RDT_RELATION_DELETED || reqType == RDT_RELATION_MODIFIED
                || reqType == RDT_RELATION_VALIDATE) {
            IRelationProxy rel = req.getRelation();
            say("Relation type", rel.getConnectionElementType());
            say("From:");
            sayElement(rel.getFrom());
            say("To:");
            sayElement(rel.getTo());
        }

        say("Element type", req.getAfter().getElementType());
        IElement before = req.getBefore(),
                 after  = req.getAfter();
        say("Before");
        sayElement(before);
        say("After");
        sayElement(after);


        if (after instanceof IParameter) {
            IParameterChangeRequest preq = (IParameterChangeRequest)  req;


            say("Before Operation");
            sayElement(preq.getBeforeOperation());
            say("After Operation");
            sayElement(preq.getAfterOperation());
        }
    }

    public static void sayElement(IElement el) {
        if (el instanceof IClass) {
            IClass cprox = (IClass)  el;
            sayClass(cprox);
        } else if (el instanceof IInterface) {
            IInterface ip = (IInterface)  el;
            sayInterface(ip);
        } else if (el instanceof IAttribute) {
            IAttribute aprox = (IAttribute)  el;
            sayAttribute(aprox);
        } else if (el instanceof IOperation) {
            IOperation oprox = (IOperation)  el;
            sayOperation(oprox);
        } else if (el instanceof IParameter) {
            IParameter pp = (IParameter)  el;
            sayParameter(pp);
        } else if (el instanceof IAssociation) {
            IAssociation ap = (IAssociation)  el;
            sayAssociation(ap);
        } else if (el instanceof IAssociationEnd) {
            IAssociationEnd end = (IAssociationEnd)  el;
            sayAssociation(end.getAssociation());
        }
    }

    public static void sayAssociation(IAssociation a) {
        if (a == null)
            return ;
        say(" Association type", a.getElementType());
        say(" Association end participants");
        ETList<IAssociationEnd> ends = a.getAssociationEnds();
        if (ends != null) {
            for (int i = 0; i < ends.getCount(); ++i) {
                IAssociationEnd end = ends.item(i);
                IClassifier el = end.getParticipant();
                say("    " + (end.getIsNavigable()? "-> " : "   ") +
                            el.getName());
            }
        } else {
            say("    << None >>");
        }
    }

    public static void sayClass(IClass cp) {
        say(" Class name", JavaClassUtils.getFullyQualifiedName(cp));
        say(" Modifiers", getVis(cp.getVisibility()));
        say(" Superclass", getSuperclasses(cp));
        say(" Subclasses", getSubclasses(cp));
        say(" Interfaces", getSuperInterfaces(cp));
    }

    public static void sayInterface(IInterface ip) {
        say(" Interface name", JavaClassUtils.getFullyQualifiedName(ip));
        say(" Modifiers", getVis(ip.getVisibility()));
        say(" Superclasses?", getSuperclasses(ip));
        say(" Super interfaces", getSuperInterfaces(ip));
        say(" Sub types", getSubTypes(ip));
    }

    public static void sayParameter(IParameter p) {
        say(" Parameter name", p.getName());
        say(" Type", p.getTypeName());
        say(" Owning operation:");

        IOperation owner = (IOperation)  p.getOwner();
        sayOperation(owner);
    }

    public static void sayOperation(IOperation op) {
        StringBuffer parStr = new StringBuffer();
        ETList<IParameter> pars = op.getFormalParameters();
        for (int j = 0; pars != null && j < pars.getCount(); ++j) {
            IParameter par = pars.item(j);
            if (j > 0)
                parStr.append(", ");
            parStr.append(MethodParameterInfo.getType(par)
                          + " " + par.getName());
        }
        IParameter retType = op.getReturnType();
        say(" Operation", getVis(op.getVisibility()) + " "
                           + op.getName()
                           + "("
                           + parStr.toString()
                           + ") : "
                           + (retType != null? retType.getTypeName() : ""));
        IElement owner = op.getOwner();
        if (owner instanceof INamedElement) {
            INamedElement nel = (INamedElement)  owner;
            say(" Owner", nel.getName());
        }
    }

    public static void sayAttribute(IAttribute ap) {
        say(" Attribute", ap.getName());
        say(" Type", ap.getTypeName());
        say(" Modifiers", getVis(ap.getVisibility()));

        IElement owner = ap.getOwner();
        if (owner instanceof INamedElement) {
            INamedElement nel = (INamedElement)  owner;
            say(" Owner", nel.getName());
        }
    }

    public static String getSuperclasses(IClassifier cp) {
        ETList<IGeneralization> gens = cp.getGeneralizations();
        if (gens != null && gens.getCount() > 0) {
            StringBuffer sups = new StringBuffer();
            for (int i = 0; i < gens.getCount(); ++i) {
                if (i > 0)
                    sups.append(", ");
                sups.append(gens.item(i).getGeneral().getName());
            }
            return sups.toString();
        }
        return "<none>";
    }

    public static String getSubclasses(IClass cp) {
        ETList<IGeneralization> gens = cp.getSpecializations();
        if (gens != null && gens.getCount() > 0) {
            StringBuffer specs = new StringBuffer();
            for (int i = 0; i < gens.getCount(); ++i) {
                if (i > 0)
                    specs.append(", ");
                specs.append(gens.item(i).getSpecific().getName());
            }
            return specs.toString();
        }
        return "<none>";
    }

    public static String getSuperInterfaces(IClassifier cp) {
        ETList<IImplementation> impls = cp.getImplementations();

        if (impls != null && impls.getCount() > 0) {
            StringBuffer ints = new StringBuffer("{" + impls.getCount() + "} ");
            for (int i = 0; i < impls.getCount(); ++i) {
                if (i > 0)
                    ints.append(", ");
                INamedElement el = impls.item(i).getSupplier();
                ints.append(el.getName());
            }
            return ints.toString();
        }
        return "<none>";
    }

    public static String getSuperInterfaces(IInterface ip) {
        return getSuperInterfaces((IClassifier) ip);
    }

    public static String getSubTypes(IInterface ip) {
        return "<not impl>";
    }
}
