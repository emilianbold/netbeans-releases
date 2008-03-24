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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.refactoring;

import org.netbeans.modules.hibernate.mapping.model.Array;
import org.netbeans.modules.hibernate.mapping.model.Bag;
import org.netbeans.modules.hibernate.mapping.model.Component;
import org.netbeans.modules.hibernate.mapping.model.CompositeElement;
import org.netbeans.modules.hibernate.mapping.model.DynamicComponent;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.Idbag;
import org.netbeans.modules.hibernate.mapping.model.Join;
import org.netbeans.modules.hibernate.mapping.model.JoinedSubclass;
import org.netbeans.modules.hibernate.mapping.model.KeyManyToOne;
import org.netbeans.modules.hibernate.mapping.model.List;
import org.netbeans.modules.hibernate.mapping.model.ManyToOne;
import org.netbeans.modules.hibernate.mapping.model.Map;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.mapping.model.NaturalId;
import org.netbeans.modules.hibernate.mapping.model.NestedCompositeElement;
import org.netbeans.modules.hibernate.mapping.model.OneToOne;
import org.netbeans.modules.hibernate.mapping.model.Properties;
import org.netbeans.modules.hibernate.mapping.model.Set;
import org.netbeans.modules.hibernate.mapping.model.Subclass;
import org.netbeans.modules.hibernate.mapping.model.UnionSubclass;

/**
 * Contains methods to do the acutal refactoring changes in the Hiberante mapping file
 * 
 * @author Dongmei
 */
public class JavaRenameChanger {

    private boolean packageOnly;
    private String origName;
    private String newName;

    public JavaRenameChanger(boolean packageOnly, String origName, String newName) {
        this.packageOnly = packageOnly;
        this.origName = origName;
        this.newName = newName;
    }

    public void refactoringImports(HibernateMapping hbMapping) {
        for (int i = 0; i < hbMapping.sizeImport(); i++) {
            String clsName = hbMapping.getAttributeValue(HibernateMapping.IMPORT, i, "Class");
            if (clsName == null)
                continue;
            
            if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName) ) {
                String newClsName = clsName.replaceFirst(origName, newName);
                hbMapping.setAttributeValue(HibernateMapping.IMPORT, i, "Class", newClsName);
            } else if (clsName.equals(origName)) {
                hbMapping.setAttributeValue(HibernateMapping.IMPORT, i, "Class", newName);
            }
        }
    }

    public void refactoringMyClasses(MyClass[] myClazz) {
        for (int ci = 0; ci < myClazz.length; ci++) {

            MyClass thisClazz = myClazz[ci];
            
            // <class> element can have <subclass>, <joined-subclass> and/or <union-sbuclass> elements
            refactoringSubclasses(thisClazz.getSubclass());
            refactoringJoinedSubclasses(thisClazz.getJoinedSubclass());
            refactoringUnionSubclasses(thisClazz.getUnionSubclass());

            // The name attribute of <class> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    myClazz[ci].setAttributeValue("Name", newClsName);
                } else if (clsName.equals(origName)) {
                    myClazz[ci].setAttributeValue("Name", newName);
                }
            }

            // The class attribute of <composite-id> element
            if (thisClazz.getCompositeId() != null) {
                String compositeIdClsName = thisClazz.getCompositeId().getAttributeValue("Class");
                if (compositeIdClsName != null) {
                    if (packageOnly && HibernateRefactoringUtil.getPackageName(compositeIdClsName).equals(origName)) {
                        String newompositeIdClsName = compositeIdClsName.replaceFirst(origName, newName);
                        thisClazz.getCompositeId().setAttributeValue("Class", newompositeIdClsName);
                    } else if (compositeIdClsName.equals(origName)) {
                        thisClazz.getCompositeId().setAttributeValue("Class", newName);
                    }
                }
            }

            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());

            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisClazz.getManyToOne());

            // The class attribute of <many-to-one> in <join>
            refactoringJoins(thisClazz.getJoin());

            // The class attribute of <many-to-one> in <natural-id>
            refactoringNaturalId(thisClazz.getNaturalId());

            // The class attribute of <many-to-one> in <properties>
            refactoringPropertiez(thisClazz.getProperties());

            // The class attribute of <many-to-one> in <idbag><composite-element>
            refactoringIdBags(thisClazz.getIdbag());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisClazz.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisClazz.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisClazz.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisClazz.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisClazz.getArray());

            // <component><one-to-many class="">
            refactoringComponents(thisClazz.getComponent());

            // <dynamic-component><one-to-many class="">
            refactoringDynamicComponents(thisClazz.getDynamicComponent());
        }
    }

    public void refactoringSubclasses(Subclass[] subclazz) {
        for (int ci = 0; ci < subclazz.length; ci++) {

            Subclass thisClazz = subclazz[ci];
            
            // <subclass> elements can be contained inside <subclass> element
            refactoringSubclasses(thisClazz.getSubclass());

            // The name attribute of <subclass> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    thisClazz.setAttributeValue("Name", newClsName);
                } else if (clsName.equals(origName)) {
                   thisClazz.setAttributeValue("Name", newName);
                }
            }

            // The extends attribute of <subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if (extendsClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(extendsClsName).equals(origName)) {
                    String newExtendsClsName = extendsClsName.replaceFirst(origName, newName);
                    thisClazz.setAttributeValue("Extends", newExtendsClsName);
                } else if (extendsClsName.equals(origName)) {
                   thisClazz.setAttributeValue("Extends", newName);
                }
            }
                
            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());

            // The class attribute of <many-to-one> in <join>
            refactoringJoins(thisClazz.getJoin());

            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisClazz.getManyToOne());

            // The class attribute of <many-to-one> in <idbag><composite-element>
            refactoringIdBags(thisClazz.getIdbag());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisClazz.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisClazz.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisClazz.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisClazz.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisClazz.getArray());

            // <component><one-to-many class="">
            refactoringComponents(thisClazz.getComponent());

            // <dynamic-component><one-to-many class="">
            refactoringDynamicComponents(thisClazz.getDynamicComponent());
        }
    }

    public void refactoringJoinedSubclasses(JoinedSubclass[] joinedSubclazz) {
        for (int ci = 0; ci < joinedSubclazz.length; ci++) {

            JoinedSubclass thisClazz = joinedSubclazz[ci];
            
            // <joined-subclass> elements can be contained inside <joined-subclass> 
            refactoringJoinedSubclasses(thisClazz.getJoinedSubclass());

            // The name attribute of <joined-subclass> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    joinedSubclazz[ci].setAttributeValue("Name", newClsName);
                } else if (clsName.equals(origName)) {
                   joinedSubclazz[ci].setAttributeValue("Name", newName);
                }
            }

            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());

            // The extends attribute of <joined-subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if (extendsClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(extendsClsName).equals(origName)) {
                    String newExtendsClsName = extendsClsName.replaceFirst(origName, newName);
                    thisClazz.setAttributeValue("Extends", newExtendsClsName);
                } else if (extendsClsName.equals(origName)) {
                   thisClazz.setAttributeValue("Extends", newName);
                }
            }

            // The persister attribute of <joined-subclass> element
            String persisterClsName = thisClazz.getAttributeValue("Persister");
            if (persisterClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(persisterClsName).equals(origName)) {
                    String newPersisterClsName= persisterClsName.replaceFirst(origName, newName);
                     thisClazz.setAttributeValue("Persister", newPersisterClsName);
                } else if (persisterClsName.equals(origName)) {
                    thisClazz.setAttributeValue("Persister", newName);
                }
            }
            
            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisClazz.getManyToOne());

            // The class attribute of <many-to-one> in <properties>
            refactoringPropertiez(thisClazz.getProperties());

            // The class attribute of <many-to-one> in <idbag><composite-element>
            refactoringIdBags(thisClazz.getIdbag());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisClazz.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisClazz.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisClazz.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisClazz.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisClazz.getArray());

            // <component><one-to-many class="">
            refactoringComponents(thisClazz.getComponent());

            // <dynamic-component><one-to-many class="">
            refactoringDynamicComponents(thisClazz.getDynamicComponent());

        }
    }

    public void refactoringUnionSubclasses(UnionSubclass[] unionSubclazz) {
        for (int ci = 0; ci < unionSubclazz.length; ci++) {

            UnionSubclass thisClazz = unionSubclazz[ci];
            
            // <union-subclass> elements can be inside <union-subclass> 
            refactoringUnionSubclasses(thisClazz.getUnionSubclass());

            // The name attribute of <sub-class> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    unionSubclazz[ci].setAttributeValue("Name", newClsName);
                } else if (clsName.equals(origName)) {
                    unionSubclazz[ci].setAttributeValue("Name", newName);
                }
            }
            if (clsName.equals(origName)) {
                unionSubclazz[ci].setAttributeValue("Name", newName);
            }

            // The extends attribute of <union-subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if (extendsClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(extendsClsName).equals(origName)) {
                    String newExtendsClsName = extendsClsName.replaceFirst(origName, newName);
                    thisClazz.setAttributeValue("Extends", newExtendsClsName);
                } else if (extendsClsName.equals(origName)) {
                    thisClazz.setAttributeValue("Extends", newName);
                }
            }

            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());

            // The persister attribute of <joined-subclass> element
            String persisterClsName = thisClazz.getAttributeValue("Persister");
            if (persisterClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(persisterClsName).equals(origName)) {
                    String newPersisterClsName = persisterClsName.replaceFirst(origName, newName);
                    thisClazz.setAttributeValue("Persister", newPersisterClsName);
                } else if (persisterClsName.equals(origName)) {
                    thisClazz.setAttributeValue("Persister", newName);
                }
            }

            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisClazz.getManyToOne());

            // The class attribute of <many-to-one> in <properties>
            refactoringPropertiez(thisClazz.getProperties());

            // The class attribute of <many-to-one> in <idbag><composite-element>
            refactoringIdBags(thisClazz.getIdbag());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisClazz.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisClazz.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisClazz.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisClazz.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisClazz.getArray());

            // <component><one-to-many class="">
            refactoringComponents(thisClazz.getComponent());

            // <dynamic-component><one-to-many class="">
            refactoringDynamicComponents(thisClazz.getDynamicComponent());
        }
    }

    private void refactoringOneToOnes(OneToOne[] hbModelOneToOnes) {
        for (int i = 0; i < hbModelOneToOnes.length; i++) {
            String clsName = hbModelOneToOnes[i].getAttributeValue("Class");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    hbModelOneToOnes[i].setAttributeValue("Class", newClsName);
                } else if (clsName.equals(origName)) {
                    hbModelOneToOnes[i].setAttributeValue("Class", newName);
                }
            }
        }
    }

    private void refactoringNaturalId(NaturalId nId) {
        if (nId == null) {
            return;
        }

        // The class attribute of <many-to-one> in <natural-id>
        refactoringManyToOnes(nId.getManyToOne());

        // <component><one-to-many class="">
        refactoringComponents(nId.getComponent());
    }

    private void refactoringPropertiez(Properties[] hbModelPropertiez) {
        for (int i = 0; i < hbModelPropertiez.length; i++) {

            // The class attribute of <many-to-one>
            refactoringManyToOnes(hbModelPropertiez[i].getManyToOne());

            // <component><one-to-many class="">
            refactoringComponents(hbModelPropertiez[i].getComponent());
        }
    }

    private void refactoringJoins(Join[] hbModelJoins) {
        for (int i = 0; i < hbModelJoins.length; i++) {

            // The class attribute of <many-to-one>
            Join theJoin = hbModelJoins[i];
            refactoringManyToOnes(theJoin.getManyToOne());

            // <component><one-to-many class="">
            refactoringComponents(theJoin.getComponent());
        }
    }

    private void refactoringComponents(Component[] hbModelComponents) {
        for (int i = 0; i < hbModelComponents.length; i++) {

            Component thisComp = hbModelComponents[i];

            // The class attribute of itself
            String clsName = thisComp.getAttributeValue("Class");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    thisComp.setAttributeValue("Class", newClsName);
                } else if (clsName.equals(origName)) {
                    thisComp.setAttributeValue("Class", newName);
                }
            }

            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisComp.getManyToOne());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisComp.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisComp.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisComp.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisComp.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisComp.getArray());

            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisComp.getOneToOne());
        }

    }

    private void refactoringDynamicComponents(DynamicComponent[] hbModelDynComps) {
        for (int i = 0; i < hbModelDynComps.length; i++) {

            DynamicComponent thisComp = hbModelDynComps[i];

            // The class attribute of <many-to-one>
            refactoringManyToOnes(thisComp.getManyToOne());

            // The class attribute of <one-to-many> element in <map>
            refactoringMaps(thisComp.getMap());

            // The class attribute of <one-to-many> element in <set>
            refactoringSets(thisComp.getSet());

            // The class attribute of <one-to-many> element in <list>
            refactoringLists(thisComp.getList());

            // The class attribute of <one-to-many> element in <bag>
            refactoringBags(thisComp.getBag());

            // The class attribute of <one-to-many> element in <array>
            refactoringArrays(thisComp.getArray());

            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisComp.getOneToOne());
        }
    }

    private void refactoringManyToOnes(ManyToOne[] hbModelManyToOnes) {
        for (int i = 0; i < hbModelManyToOnes.length; i++) {
            String clsName = hbModelManyToOnes[i].getAttributeValue("Class");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    hbModelManyToOnes[i].setAttributeValue("Class", newClsName);
                } else if (clsName.equals(origName)) {
                    hbModelManyToOnes[i].setAttributeValue("Class", newName);
                }
            }
        }
    }

    private void refactoringMaps(Map[] hbModelMaps) {
        for (int mi = 0; mi < hbModelMaps.length; mi++) {

            Map theMap = hbModelMaps[mi];

            // The class attribute of <key-many-to-one> in <composite-map-key>
            if (theMap.getCompositeMapKey() != null) {
                refactoringKeyManyToOnes(theMap.getCompositeMapKey().getKeyManyToOne());
            }

            // The class attribute of <key-many-to-one> in <composite-index>
            if (theMap.getCompositeIndex() != null) {
                refactoringKeyManyToOnes(theMap.getCompositeIndex().getKeyManyToOne());
            }

            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(theMap.getCompositeElement());

            // The class attribute in <one-to-many>
            String oneToManyClsName = theMap.getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(oneToManyClsName).equals(origName)) {
                    String newOneToManyClsName = oneToManyClsName.replaceFirst(origName, newName);
                    theMap.setAttributeValue(Map.ONE_TO_MANY, "Class", newOneToManyClsName);
                } else if (oneToManyClsName.equals(origName)) {
                    theMap.setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
                }
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = theMap.getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    theMap.setAttributeValue(Map.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    theMap.setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
                }
            }
        }
    }

    private void refactoringCompositeElement(CompositeElement compositeElement) {
        if (compositeElement == null) {
            return;
        }

        String clsName = compositeElement.getAttributeValue("Class");
        if (clsName != null) {
            if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                String newClsName = clsName.replaceFirst(origName, newName);
                compositeElement.setAttributeValue("Class", newClsName);
            } else if (clsName.equals(origName)) {
                compositeElement.setAttributeValue("Class", newName);
            }
        }

        // The class attribute of <many-to-one> in <nested-composite-element>
        refactoringNestedCompositeElements(compositeElement.getNestedCompositeElement());
    }

    private void refactoringNestedCompositeElements(NestedCompositeElement[] nestedCompElems) {
        for (int i = 0; i < nestedCompElems.length; i++) {
            refactoringManyToOnes(nestedCompElems[i].getManyToOne());
        }
    }

    private void refactoringKeyManyToOnes(KeyManyToOne[] keyManyToOnes) {
        for (int i = 0; i < keyManyToOnes.length; i++) {
            KeyManyToOne theOne = keyManyToOnes[i];
            String clsName = theOne.getAttributeValue("Class");
            if (clsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(clsName).equals(origName)) {
                    String newClsName = clsName.replaceFirst(origName, newName);
                    theOne.setAttributeValue("Class", newClsName);
                } else if (clsName.equals(origName)) {
                    theOne.setAttributeValue("Class", newName);
                }
            }
        }
    }

    private void refactoringSets(Set[] hbModelSets) {
        for (int si = 0; si < hbModelSets.length; si++) {

            String oneToManyClsName = hbModelSets[si].getAttributeValue(Set.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(oneToManyClsName).equals(origName)) {
                    String newOneToManyClsName = oneToManyClsName.replaceFirst(origName, newName);
                    hbModelSets[si].setAttributeValue(Set.ONE_TO_MANY, "Class", newOneToManyClsName);
                } else if (oneToManyClsName.equals(origName)) {
                    hbModelSets[si].setAttributeValue(Set.ONE_TO_MANY, "Class", newName);
                }
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelSets[si].getAttributeValue(Set.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    hbModelSets[si].setAttributeValue(Set.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    hbModelSets[si].setAttributeValue(Set.MANY_TO_MANY, "Class", newName);
                }
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelSets[si].getCompositeElement());
        }
    }

    private void refactoringLists(List[] hbModelLists) {
        for (int li = 0; li < hbModelLists.length; li++) {

            String oneToManyClsName = hbModelLists[li].getAttributeValue(List.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(oneToManyClsName).equals(origName)) {
                    String newOneToManyClsName = oneToManyClsName.replaceFirst(origName, newName);
                    hbModelLists[li].setAttributeValue(List.ONE_TO_MANY, "Class", newOneToManyClsName);
                } else if (oneToManyClsName.equals(origName)) {
                    hbModelLists[li].setAttributeValue(List.ONE_TO_MANY, "Class", newName);
                }
            }

            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelLists[li].getAttributeValue(List.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    hbModelLists[li].setAttributeValue(List.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    hbModelLists[li].setAttributeValue(List.MANY_TO_MANY, "Class", newName);
                }
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelLists[li].getCompositeElement());
        }
    }

    private void refactoringBags(Bag[] hbModelBags) {
        for (int bi = 0; bi < hbModelBags.length; bi++) {

            String oneToManyClsName = hbModelBags[bi].getAttributeValue(Bag.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(oneToManyClsName).equals(origName)) {
                    String newOneToManyClsName = oneToManyClsName.replaceFirst(origName, newName);
                    hbModelBags[bi].setAttributeValue(Bag.ONE_TO_MANY, "Class", newOneToManyClsName);
                } else if (oneToManyClsName.equals(origName)) {
                    hbModelBags[bi].setAttributeValue(Bag.ONE_TO_MANY, "Class", newName);
                }
            }

            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelBags[bi].getAttributeValue(Bag.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    hbModelBags[bi].setAttributeValue(Bag.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    hbModelBags[bi].setAttributeValue(Bag.MANY_TO_MANY, "Class", newName);
                }
            }

            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelBags[bi].getCompositeElement());
        }
    }

    private void refactoringIdBags(Idbag[] hbModelIdbags) {
        for (int i = 0; i < hbModelIdbags.length; i++) {

            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelIdbags[i].getCompositeElement());

            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelIdbags[i].getAttributeValue(Idbag.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    hbModelIdbags[i].setAttributeValue(Idbag.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    hbModelIdbags[i].setAttributeValue(Idbag.MANY_TO_MANY, "Class", newName);
                }
            }
        }
    }

    private void refactoringArrays(Array[] hbModelArrays) {
        for (int ai = 0; ai < hbModelArrays.length; ai++) {

            String oneToManyClsName = hbModelArrays[ai].getAttributeValue(Array.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(oneToManyClsName).equals(origName)) {
                    String newOneToManyClsName = oneToManyClsName.replaceFirst(origName, newName);
                    hbModelArrays[ai].setAttributeValue(Array.ONE_TO_MANY, "Class", newOneToManyClsName);
                } else if (oneToManyClsName.equals(origName)) {
                    hbModelArrays[ai].setAttributeValue(Array.ONE_TO_MANY, "Class", newName);
                }
            }

            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelArrays[ai].getAttributeValue(Array.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null) {
                if (packageOnly && HibernateRefactoringUtil.getPackageName(manyToManyClsName).equals(origName)) {
                    String newManyToManyClsName = manyToManyClsName.replaceFirst(origName, newName);
                    hbModelArrays[ai].setAttributeValue(Array.MANY_TO_MANY, "Class", newManyToManyClsName);
                } else if (manyToManyClsName.equals(origName)) {
                    hbModelArrays[ai].setAttributeValue(Array.MANY_TO_MANY, "Class", newName);
                }
            }

            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelArrays[ai].getCompositeElement());
        }
    }
}
