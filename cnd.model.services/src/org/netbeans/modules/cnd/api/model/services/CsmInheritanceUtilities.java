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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.*;

/**
 * utilities to merge/get inheritance information
 * @author Vladimir Voskresensky
 */
public final class CsmInheritanceUtilities {
    /* 
     * visibility is ordered:
     * NONE < PUBLIC < PROTECTED < PRIVATE
     * using:
     * when use min visibility:
     *  PRIVATE => everything is seen in context about asked class
     *  PROTECTED => protected and public members are visible
     *  PUBLIC => only public members are visible
     *  NONE => nothing is visible from asked class
     */
    private static final int PRIVATE   = 1 << 0; //
    private static final int PROTECTED = 1 << 1; //
    private static final int PUBLIC    = 1 << 2; //
    private static final int NONE      = 1 << 3; //   
    
    // value used when need to pass info, that everything is visible
    public static final CsmVisibility MAX_VISIBILITY = CsmVisibility.PRIVATE;
    
    /**
     * Creates a new instance of CsmInheritanceUtilities
     */
    private CsmInheritanceUtilities() {
    }
    
    private static int visToInt(CsmVisibility vis) {
        if (vis == CsmVisibility.NONE) {
            return NONE;
        } else if (vis == CsmVisibility.PRIVATE) {
            return PRIVATE;
        } else if (vis == CsmVisibility.PROTECTED) {
            return PROTECTED;
        } else {
            assert (vis == CsmVisibility.PUBLIC);
            return PUBLIC;
        }
    }
    
    private static CsmVisibility intToVis(int visInt) {
        switch (visInt) {
            case NONE:
                return CsmVisibility.NONE;
            case PRIVATE:
                return CsmVisibility.PRIVATE;
            case PROTECTED:
                return CsmVisibility.PROTECTED;
            default:
                assert (visInt == PUBLIC);
                return CsmVisibility.PUBLIC;
        }
    }
    
    // match if member is valid for input minimal visibility value
    public static boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        assert (member.getVisibility() != null) : "can't be null visibility";
        return matchVisibility(member.getVisibility(), minVisibility);
    }
    
    // match if "toCheck" visibility is valid for input minimal visibility value
    private static boolean matchVisibility(CsmVisibility toCheck, CsmVisibility minVisibility) {
        assert (toCheck != null && minVisibility != null);
        if (minVisibility == CsmVisibility.NONE) {
            // quick escape for "invisible"
            return false;
        }
        int memberVis = visToInt(toCheck);
        int minVis = visToInt(minVisibility);
        return minVis <= memberVis;
    }
    
    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class B's context (for example method of class B)
     * +---------------------------------------------------------+
     * |B extends A as    |    A's members visibility from B     |
     * |---------------------------------------------------------+
     * |public            |     protected, public                |
     * |protected         |     protected, public                |
     * |private           |     protected, public                |
     * |---------------------------------------------------------+
     *
     * for B-derived childs use getChildInheritanceVisibility
     */
    private static CsmVisibility getInheritanceVisibility(CsmVisibility inheritBA) {
        // for all other - protected and public level
        return CsmVisibility.PROTECTED;
    }

    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class B objects in not B's context (for example global method used B)
     * +---------------------------------------------------------------+
     * |B extends A as    |    A's members visibility where B is used  |
     * |---------------------------------------------------------------+
     * |public            |     public                                 |
     * |protected         |     invisible                              |
     * |private           |     invisible                              |
     * |---------------------------------------------------------------+
     *
     * for B-derived childs use getExtChildInheritanceVisibility
     */
    private static CsmVisibility getExtInheritanceVisibility(CsmVisibility inheritBA) {
        if (inheritBA == CsmVisibility.PUBLIC) {
            return CsmVisibility.PUBLIC;
        } else {
            return CsmVisibility.NONE;
        }
    }
    
    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * class C "extends" B {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class C's context (for example method of class C)
     * +---------------------------------------------------------+
     * |B extends A as    |    A's members visibility from C     |
     * |---------------------------------------------------------+
     * |public            |     protected, public                |
     * |protected         |     protected, public                |
     * |private           |     invisible                        |
     * |---------------------------------------------------------+
     */
    private static CsmVisibility getChildInheritanceVisibility(CsmVisibility inheritBA) {
        if (inheritBA == CsmVisibility.PUBLIC || inheritBA == CsmVisibility.PROTECTED) {
            return CsmVisibility.PROTECTED;
        } else {
            return CsmVisibility.NONE;
        }
    }
    
//    /*
//     * class A {
//     * };
//     * class B "extends" A {
//     * };
//     * class C "extends" B {
//     * };
//     * this method is used for getting visibility of members asked from class A
//     * when handling class B in not B's context (for example global method used C)
//     * +---------------------------------------------------------------+
//     * |B extends A as    |    A's members visibility where C is used  |
//     * |---------------------------------------------------------------+
//     * |public            |     public                                 |
//     * |protected         |     invisible                              |
//     * |private           |     invisible                              |
//     * |---------------------------------------------------------------+
//     */
//    public static CsmVisibility getExtChildInheritanceVisibility(CsmVisibility inheritBA) {
//        if (inheritBA == CsmVisibility.PUBLIC) {
//            return CsmVisibility.PUBLIC;
//        } else {
//            return CsmVisibility.NONE;
//        }
//    }
    
    public static CsmVisibility mergeInheritedVisibility(CsmVisibility curVisibility, CsmVisibility inherVisibility) {
        return getMinVisibility(curVisibility, getInheritanceVisibility(inherVisibility));
    }    

    public static CsmVisibility mergeExtInheritedVisibility(CsmVisibility curVisibility, CsmVisibility inherVisibility) {
        return getMinVisibility(curVisibility, getExtInheritanceVisibility(inherVisibility));
    }    
    
    public static CsmVisibility mergeChildInheritanceVisibility(CsmVisibility curVisibility, CsmVisibility inheritBA) {
        return getMinVisibility(curVisibility, getChildInheritanceVisibility(inheritBA));
    }
    
    // get new minimal visibility as result of analyzing input visibilities
    private static CsmVisibility getMinVisibility(CsmVisibility vis1, CsmVisibility vis2) {
        assert (vis1 != null && vis2 != null);
        int visInt1 = visToInt(vis1);
        int visInt2 = visToInt(vis2);
        int newMinVis = Math.max(visInt1, visInt2);
        return intToVis(newMinVis);
    }    
    
    // get new maximal visibility as result of analyzing input visibilities
    private static CsmVisibility getMaxVisibility(CsmVisibility vis1, CsmVisibility vis2) {
        assert (vis1 != null && vis2 != null);
        int visInt1 = visToInt(vis1);
        int visInt2 = visToInt(vis2);
        int newMaxVis = Math.min(visInt1, visInt2);
        return intToVis(newMaxVis);
    }
    
    /**
     * gets info, how content of class "clazz" is visible from context defined by
     * "contextDeclaration". Context declaration could be class or function. If
     * it is function => function also could have associated class to check
     * Examples:
     *  - context class could be child of interested class => depending on depth
     *    could be different visibility
     *  - context is null => global context => only public is visible
     *  - context is friend of interested class => everything is visible
     *  - context class is not inherited from interested class and not a friend => global context => only public is visible
     */
    public static CsmVisibility getContextVisibility(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration) {
        return getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, false);
    }
    public static CsmVisibility getContextVisibility(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmVisibility defVisibilityValue, boolean checkInheritance) {
        assert (clazz != null);
        CsmClass contextClass = CsmBaseUtilities.getContextClass(contextDeclaration);
        // if we are in the same class => we see everything
        if (areEqualClasses(clazz, contextClass)) {
            return MAX_VISIBILITY;
        }
        // friend has maximal visibility
        if (CsmFriendResolver.getDefault().isFriend(contextDeclaration, clazz)) {
            return MAX_VISIBILITY;
        }
        // nested classes should see at least themselves
        if (isNestedClass(contextClass, clazz)) {
            return MAX_VISIBILITY;
        }
        // from global context only public members are visible, friend is checked above
        // return passed default public visibility
        if (contextClass == null || !checkInheritance) {
            return defVisibilityValue;
        }

        List<CsmInheritance> chain = findInheritanceChain(contextClass, clazz);
        if (chain != null) {
            assert (chain.size() > 0);
            // walk through inheritance chain to find corrected visibility
            // by default we see public and protected members of parent
            CsmVisibility mergedVisibility = CsmVisibility.PROTECTED;
            for (int i = 0; i < chain.size(); i++) {
                CsmInheritance inherit = chain.get(i);
                if (i == 0) {
                    // create merged visibility based on child inheritance
                    mergedVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(mergedVisibility, inherit.getVisibility());
                } else {
                    // create merged visibility based on direct inheritance
                    mergedVisibility = CsmInheritanceUtilities.mergeChildInheritanceVisibility(mergedVisibility, inherit.getVisibility());
                }
            }          
            return mergedVisibility;
        } else {
            // not inherited class see only public, friend was checked above
            // return passed default public visibility
            return defVisibilityValue;
        }
    }
    
    /**
     * gets chain of inheritance
     * class B : public A {
     * }
     * class C : public B {
     * }
     * class D : public C {
     * } 
     * chain for findInheritanceChain(D, A) will be (D->C, C->B, B->A)
     * if no inheritance => return "null"
     */
    private static List<CsmInheritance> findInheritanceChain(CsmClass child, CsmClass parent) {
        List<CsmInheritance> res = new ArrayList<CsmInheritance>();
        Set<CharSequence> handledClasses = new HashSet<CharSequence>();
        if (findInheritanceChain(child, parent, res, handledClasses)) {
            return res;
        } else {
            return null;
        }
    }

    public static boolean isAssignableFrom(CsmClass child, CsmClass parent) {
        assert (parent != null);
        if (areEqualClasses(parent, child)) {
            return true;
        }
        List<CsmInheritance> chain = CsmInheritanceUtilities.findInheritanceChain(child, parent);
        return chain != null;
    }
    
    private static boolean findInheritanceChain(CsmClass child, CsmClass parent, 
                                        List<CsmInheritance> res, 
                                        Set<CharSequence> handledClasses) {
        // remember visited childs
        // quick exit, if already handled before
        if (child == null || !handledClasses.add(child.getQualifiedName())) {
            return false;
        }
        // quick escapement if child doesn't have base classes
        Collection base = child.getBaseClasses();
        if (base == null || base.size() == 0) {
            return false;
        }
        // check if direct child of parent
        CsmInheritance inh = findDirectInheritance(child, parent);
        if (inh != null) {
            res.add(inh);
            return true;
        }
        // TODO: we have to find all chains and then select the right one
        // for now we are looking for the first found chain
        List<CsmInheritance> bestChain = null;
        CsmInheritance bestInh = null;
        for (Iterator it = base.iterator(); it.hasNext();) {
            CsmInheritance curInh = (CsmInheritance) it.next();
            List<CsmInheritance> curInhRes = new ArrayList<CsmInheritance>();
            if (findInheritanceChain(getCsmClass(curInh), parent, curInhRes, handledClasses)) {
                bestChain = curInhRes;
                bestInh = curInh;
                // TODO: comment as above
                // for now we stop on the first found chain
                break;
            }
        }        
        if (bestChain != null) {
            assert (bestChain.size() > 0);
            res.add(bestInh);
            res.addAll(bestChain);            
            return true;
        } 
        return false;
    }

    public static CsmClass getCsmClass(CsmInheritance inh) {
        CsmClassifier classifier = inh.getClassifier();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier);
        if (CsmKindUtilities.isClass(classifier)) {
            return (CsmClass)classifier;
        }
        return null;
    }

    private static CsmInheritance findDirectInheritance(CsmClass child, CsmClass parent) {
        assert (parent != null);
        Collection base = child.getBaseClasses();
        if (base != null && base.size() > 0) {
            for (Iterator it = base.iterator(); it.hasNext();) {
                CsmInheritance curInh = (CsmInheritance) it.next();
                if (areEqualClasses(parent, getCsmClass(curInh))) {
                    return curInh;
                }
            }
        }
        return null;
    }

    private static boolean areEqualClasses(CsmClass clazz, CsmClass contextClass) {
        assert clazz != null;
        if (clazz.equals(contextClass)) {
            return true;
        } else if (contextClass != null) {
            // TODO: may be move such logic into equals methods of instantiations?
            if (CsmKindUtilities.isTemplate(clazz) ||
                    CsmKindUtilities.isTemplateInstantiation(clazz)) {
                return clazz.getUniqueName().equals(contextClass.getUniqueName());
            }
        }
        return false;
    }

    private static boolean isNestedClass(CsmClass inner, CsmClass outer) {
        return inner != null && outer != null
                && inner.getQualifiedName().toString().startsWith(outer.getQualifiedName().toString());
    }

}
