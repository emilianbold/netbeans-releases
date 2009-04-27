/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileContainer.MyFile;

/**
 *
 * @author Alexander Simon
 */
public final class Tracer {

    public static void dumpProjectContainers(ProjectBase project){
        PrintStream printStream = System.out;
        dumpProjectContainers(project.getClassifierSorage(), printStream);
        dumpProjectContainers(project, printStream);
        dumpProjectContainers(project.getDeclarationsSorage(), printStream);
        dumpProjectContainers(project.getFileContainer(), printStream);
    }

    private static void dumpProjectContainers(ClassifierContainer container, PrintStream printStream){
        printStream.println("\n========== Dumping Dump Project Classifiers");
        for(Map.Entry<CharSequence, CsmClassifier> entry : container.getClassifiers().entrySet()){
            printStream.print("\t"+entry.getKey().toString()+" ");
            if (entry.getValue() == null){
                printStream.println("null");
            } else {
                printStream.println(entry.getValue().getUniqueName());
            }
        }
        printStream.println("\n========== Dumping Dump Project Typedefs");
        for(Map.Entry<CharSequence, CsmClassifier> entry : container.getTypedefs().entrySet()){
            printStream.print("\t"+entry.getKey().toString()+" ");
            if (entry.getValue() == null){
                printStream.println("null");
            } else {
                printStream.println(entry.getValue().getUniqueName());
            }
        }
    }

    private static void dumpProjectContainers(ProjectBase project, PrintStream printStream){
        GraphContainer container  = project.getGraphStorage();
        Map<CharSequence, CsmFile> map = new TreeMap<CharSequence, CsmFile>();
        for (CsmFile f : project.getAllFiles()){
            map.put(f.getAbsolutePath(), f);
        }
        for (CsmFile file : map.values()){
            printStream.println("\n========== Dumping links for file "+file.getAbsolutePath());
            Map<CharSequence, CsmFile> set = new TreeMap<CharSequence, CsmFile>();
            for (CsmFile f : container.getInLinks(file)){
                set.put(f.getAbsolutePath(), (FileImpl)f);
            }
            if (set.size()>0) {
                printStream.println("\tInput");
                for (CsmFile f : set.values()){
                    printStream.println("\t\t"+f.getAbsolutePath());
                }
                set.clear();
            }
            for (CsmFile f : container.getOutLinks(file)){
                set.put(f.getAbsolutePath(), (FileImpl)f);
            }
            if (set.size()>0) {
                printStream.println("\tOutput");
                for (CsmFile f : set.values()){
                    printStream.println("\t\t"+f.getAbsolutePath());
                }
            }
        }
    }

    private static void dumpProjectContainers(DeclarationContainer container, PrintStream printStream){
        printStream.println("\n========== Dumping Project declarations");
        for(Map.Entry<CharSequence, Object> entry : container.testDeclarations().entrySet()){
            printStream.println("\t"+entry.getKey().toString());
            TreeMap<CharSequence, CsmDeclaration> set = new TreeMap<CharSequence, CsmDeclaration>();
            Object o = entry.getValue();
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                for(CsmUID<CsmDeclaration> uidt : uids){
                    set.put(((CsmOffsetableDeclaration)uidt.getObject()).getContainingFile().getAbsolutePath(), uidt.getObject());
                }
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                set.put(((CsmOffsetableDeclaration)uidt.getObject()).getContainingFile().getAbsolutePath(), uidt.getObject());
            }
            for(Map.Entry<CharSequence, CsmDeclaration> f : set.entrySet()){
                printStream.print("\t\t"+f.getValue());
            }
        }
        printStream.println("\n========== Dumping Project friends");
        for(Map.Entry<CharSequence, Set<CsmUID<? extends CsmFriend>>> entry : container.testFriends().entrySet()){
            printStream.print("\t"+entry.getKey().toString()+" ");
            TreeMap<CharSequence, CsmFriend> set = new TreeMap<CharSequence, CsmFriend>();
            for(CsmUID<? extends CsmFriend> uid : entry.getValue()) {
                CsmFriend f = uid.getObject();
                set.put(f.getQualifiedName(), f);
            }
            for(Map.Entry<CharSequence, CsmFriend> f : set.entrySet()){
                printStream.print("\t\t"+f.getKey().toString()+" "+f.getValue());
            }
        }
    }

    private static void dumpProjectContainers(FileContainer fileContainer, PrintStream printStream) {
        printStream.println("\n========== Dumping File container");
        Map<CharSequence, Object/*CharSequence or CharSequence[]*/> names = fileContainer.getCanonicalNames();
        //for unit test only
        Map<CharSequence, MyFile> files = fileContainer.getFileStorage();
        for(Map.Entry<CharSequence, MyFile> entry : files.entrySet()){
            CharSequence key = entry.getKey();
            printStream.println("\tFile "+key.toString());
            Object name = names.get(key);
            if (name instanceof CharSequence[]) {
                for(CharSequence alt : (CharSequence[])name) {
                    printStream.println("\t\tAlias "+alt.toString());
                }
            } else if (name instanceof CharSequence) {
                printStream.println("\t\tAlias "+name.toString());
            }
            MyFile file = entry.getValue();
            CsmFile csmFile = file.getFileUID().getObject();
            printStream.println("\t\tModel File "+csmFile.getAbsolutePath());
            printStream.println("\t\tNumber of states "+file.getPrerocStates().size());
            for (FileContainer.StatePair statePair : file.getStatePairs()) {
                StringTokenizer st = new StringTokenizer(FilePreprocessorConditionState.toStringBrief(statePair.pcState),"\n");
                boolean first = true;
                while (st.hasMoreTokens()) {
                    if (first) {
                        printStream.println("\t\tState "+st.nextToken());
                        first = false;
                    } else {
                        printStream.println("\t\t\t"+st.nextToken());
                    }
                }
            }
        }
    }


    private Tracer() {
    }
}
