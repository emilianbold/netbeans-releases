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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author Dongmei Cao
 */
public class JavaClassRenameTransaction extends RenameTransaction {

    public JavaClassRenameTransaction(java.util.Set<FileObject> files, String oldName, String newName) {
        super(files, oldName, newName);
    }

    /**
     * Do the actual changes
     * 
     */
    public void doChanges() {

        for (FileObject mappingFileObject : getToBeModifiedFiles()) {

            OutputStream outs = null;
            try {
                InputStream is = mappingFileObject.getInputStream();
                HibernateMapping hbMapping = HibernateMapping.createGraph(is);
                
                // The class attribute of <import>s
                for (int i = 0; i < hbMapping.sizeImport(); i++) {
                    String importClsName = hbMapping.getAttributeValue(HibernateMapping.IMPORT, i, "Class");
                    if (importClsName != null && importClsName.equals(origName)) {
                        hbMapping.setAttributeValue(HibernateMapping.IMPORT, i, "Class", newName);
                    }
                }
                
                // Change all the occurrences in <class> elements
                refactoringMyClasses(hbMapping.getMyClass());
                
                // Change all the occurrences in <subclass> elements
                refactoringSublasses(hbMapping.getSubclass());
                
                // Change all the occurrences in <joined-subclass> elements
                refactoringJoinedSubclasses(hbMapping.getJoinedSubclass());
                
                // Change all the occurrences in <union-subclass> elements
                refactoringUnionSubclasses(hbMapping.getUnionSubclass());
                
                outs = mappingFileObject.getOutputStream();
                hbMapping.write(outs);
                
            } catch (FileAlreadyLockedException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    outs.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
    
    private void refactoringMyClasses(MyClass[] myClazz) {
        for (int ci = 0; ci < myClazz.length; ci++) {

            MyClass thisClazz = myClazz[ci];

            // The name attribute of <class> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName != null && clsName.equals(origName)) {
                myClazz[ci].setAttributeValue("Name", newName);
            }
            
            // The class attribute of <composite-id> element
            if( thisClazz.getCompositeId() != null ) {
                String compositeIdClsName = thisClazz.getCompositeId().getAttributeValue("Class");
                if (compositeIdClsName != null && compositeIdClsName.equals(origName)){
                    thisClazz.getCompositeId().setAttributeValue("Class", newName);
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
    
    private void refactoringOneToOnes(OneToOne[] hbModelOneToOnes) {
        for( int i = 0; i < hbModelOneToOnes.length; i ++) {
            String clsName = hbModelOneToOnes[i].getAttributeValue("Class");
            if(clsName != null && clsName.equals(origName)) {
                hbModelOneToOnes[i].setAttributeValue("Class", newName);
            }
        }
    }
    
    private void refactoringNaturalId(NaturalId nId) {
        if( nId == null )
            return;
        
        // The class attribute of <many-to-one> in <natural-id>
        refactoringManyToOnes(nId.getManyToOne());
        
        // <component><one-to-many class="">
        refactoringComponents(nId.getComponent());
    }
    
    private void refactoringPropertiez(Properties[] hbModelPropertiez) {
        for( int i = 0; i < hbModelPropertiez.length; i ++ ) {
            
             // The class attribute of <many-to-one>
            refactoringManyToOnes(hbModelPropertiez[i].getManyToOne());
            
            // <component><one-to-many class="">
            refactoringComponents(hbModelPropertiez[i].getComponent());
        }
    }
    
    private void refactoringJoins(Join[] hbModelJoins) {
        for( int i = 0; i < hbModelJoins.length; i ++ ) {
            
            // The class attribute of <many-to-one>
            Join theJoin = hbModelJoins[i];
            refactoringManyToOnes(theJoin.getManyToOne());
            
            // <component><one-to-many class="">
            refactoringComponents(theJoin.getComponent());
        }
    }
    
    private void refactoringComponents(Component[] hbModelComponents){
        for( int i = 0; i < hbModelComponents.length; i ++ ) {
            
            Component thisComp = hbModelComponents[i];
            
            // The class attribute of itself
            String clsName = thisComp.getAttributeValue("Class");
            if( clsName != null && clsName.equals(origName)) {
                thisComp.setAttributeValue("Class", newName);
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
        for( int i = 0; i < hbModelDynComps.length; i ++ ) {
            
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
        for( int i = 0; i < hbModelManyToOnes.length; i ++ ) {
            String clsName = hbModelManyToOnes[i].getAttributeValue("Class");
            if( clsName != null && clsName.equals(origName)) {
                hbModelManyToOnes[i].setAttributeValue("Class", newName);
            }
        }
    }
    
    private void refactoringMaps(Map[] hbModelMaps) {
        for (int mi = 0; mi < hbModelMaps.length; mi++) {
            
            Map theMap = hbModelMaps[mi];
            
            // The class attribute of <key-many-to-one> in <composite-map-key>
            if( theMap.getCompositeMapKey() != null ) {
                refactoringKeyManyToOnes(theMap.getCompositeMapKey().getKeyManyToOne());
            }
            
            // The class attribute of <key-many-to-one> in <composite-index>
            if( theMap.getCompositeIndex() != null ) {
                refactoringKeyManyToOnes(theMap.getCompositeIndex().getKeyManyToOne());
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(theMap.getCompositeElement());
            
            // The class attribute in <one-to-many>
            String oneToManyClsName = theMap.getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null && oneToManyClsName.equals(origName)) {
                theMap.setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = theMap.getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                theMap.setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
            }
        }
    }
    
    private void refactoringCompositeElement(CompositeElement compositeElement) {
        if( compositeElement == null )
            return;
        
        String clsName = compositeElement.getAttributeValue("Class");
        if( clsName != null && clsName.equals(origName)) {
            compositeElement.setAttributeValue("Class", newName);
        }
        
        // The class attribute of <many-to-one> in <nested-composite-element>
        refactoringNestedCompositeElements(compositeElement.getNestedCompositeElement());
    }
    
    private void refactoringNestedCompositeElements(NestedCompositeElement[] nestedCompElems) {
        for( int i = 0; i < nestedCompElems.length; i ++ ) {
            refactoringManyToOnes(nestedCompElems[i].getManyToOne());
        }
    }
    
    private void refactoringKeyManyToOnes(KeyManyToOne[] keyManyToOnes) {
        for (int i = 0; i < keyManyToOnes.length; i ++) {
            KeyManyToOne theOne = keyManyToOnes[i];
            String clsName = theOne.getAttributeValue("Class");
            if(clsName != null && clsName.equals(origName)) {
                theOne.setAttributeValue("Class", newName);
            }
        }
    }
    
    private void refactoringSets(Set[] hbModelSets) {
        for (int si = 0; si < hbModelSets.length; si++) {
            
            String oneToManyClsName = hbModelSets[si].getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null && oneToManyClsName.equals(origName)) {
                hbModelSets[si].setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelSets[si].getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                hbModelSets[si].setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelSets[si].getCompositeElement());
        }
    }
    
    private void refactoringLists(List[] hbModelLists) {
        for (int li = 0; li < hbModelLists.length; li++) {
            
            String oneToManyClsName = hbModelLists[li].getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null && oneToManyClsName.equals(origName)) {
                hbModelLists[li].setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelLists[li].getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                hbModelLists[li].setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelLists[li].getCompositeElement());
        }
    }

    private void refactoringBags(Bag[] hbModelBags) {
        for (int bi = 0; bi < hbModelBags.length; bi++) {
            
            String oneToManyClsName = hbModelBags[bi].getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null && oneToManyClsName.equals(origName)) {
                hbModelBags[bi].setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
            }
            
           // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelBags[bi].getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                hbModelBags[bi].setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
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
            String manyToManyClsName = hbModelIdbags[i].getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                hbModelIdbags[i].setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
            }
        }
    }

    private void refactoringArrays(Array[] hbModelArrays) {
        for (int ai = 0; ai < hbModelArrays.length; ai++) {
            
            String oneToManyClsName = hbModelArrays[ai].getAttributeValue(Map.ONE_TO_MANY, "Class"); // NOI18N
            if (oneToManyClsName != null && oneToManyClsName.equals(origName)) {
                hbModelArrays[ai].setAttributeValue(Map.ONE_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-many>
            String manyToManyClsName = hbModelArrays[ai].getAttributeValue(Map.MANY_TO_MANY, "Class"); // NOI18N
            if (manyToManyClsName != null && manyToManyClsName.equals(origName)) {
                hbModelArrays[ai].setAttributeValue(Map.MANY_TO_MANY, "Class", newName);
            }
            
            // The class attribute of <many-to-one> in <composite-element>
            refactoringCompositeElement(hbModelArrays[ai].getCompositeElement());
        }
    }


    private void refactoringSublasses(Subclass[] subclazz) {
        for (int ci = 0; ci < subclazz.length; ci++) {

            Subclass thisClazz = subclazz[ci];
            
            // The name attribute of <subclass> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName.equals(origName)) {
                thisClazz.setAttributeValue("Name", newName);
            }
            
            // The extends attribute of <subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if( extendsClsName != null && extendsClsName.equals(origName)) {
                thisClazz.setAttributeValue("Extends", newName);
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
    
    private void refactoringJoinedSubclasses(JoinedSubclass[] joinedSubclazz) {
        for (int ci = 0; ci < joinedSubclazz.length; ci++) {

            JoinedSubclass thisClazz = joinedSubclazz[ci];

            // The name attribute of <joined-subclass> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName.equals(origName)) {
                joinedSubclazz[ci].setAttributeValue("Name", newName);
            }
            
            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());
            
            // The extends attribute of <joined-subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if( extendsClsName != null && extendsClsName.equals(origName)) {
                thisClazz.setAttributeValue("Extends", newName);
            }
            
            // The persister attribute of <joined-subclass> element
            String persisterClsName = thisClazz.getAttributeValue("Persister");
            if( persisterClsName != null && persisterClsName.equals(origName)) {
                thisClazz.setAttributeValue("Persister", newName);
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
    
    private void refactoringUnionSubclasses(UnionSubclass[] unionSubclazz) {
        for (int ci = 0; ci < unionSubclazz.length; ci++) {

            UnionSubclass thisClazz = unionSubclazz[ci];

            // The name attribute of <sub-class> element
            String clsName = thisClazz.getAttributeValue("Name");
            if (clsName.equals(origName)) {
                unionSubclazz[ci].setAttributeValue("Name", newName);
            }
            
            // The extends attribute of <union-subclass> element
            String extendsClsName = thisClazz.getAttributeValue("Extends");
            if( extendsClsName != null && extendsClsName.equals(origName)) {
                thisClazz.setAttributeValue("Extends", newName);
            }
            
            // The class attribute of <one-to-one>
            refactoringOneToOnes(thisClazz.getOneToOne());
            
            // The persister attribute of <joined-subclass> element
            String persisterClsName = thisClazz.getAttributeValue("Persister");
            if( persisterClsName != null && persisterClsName.equals(origName)) {
                thisClazz.setAttributeValue("Persister", newName);
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
    
}
