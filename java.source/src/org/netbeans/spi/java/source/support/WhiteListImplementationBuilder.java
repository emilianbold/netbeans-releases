/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.spi.java.source.support;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.source.WhiteListQueryImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class WhiteListImplementationBuilder {

    private static final byte INVOKE = 1;
    private static final byte OVERRIDE = INVOKE << 1;

    private final Model model;

    private WhiteListImplementationBuilder() {
        this.model = new Model();
    }

    @NonNull
    public WhiteListImplementationBuilder addInvocableClass(@NonNull final String classBinaryName) {
        Parameters.notNull("classBinaryName", classBinaryName); //NOI18N
        model.addClass(classBinaryName, INVOKE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addSubclassableClass(@NonNull final String classBinaryName) {
        Parameters.notNull("classBinaryName", classBinaryName); //NOI18N
        model.addClass(classBinaryName, OVERRIDE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addInvocableMethod(
            @NonNull final String classBinaryName,
            @NonNull final String methodName,
            @NonNull final String... argumentTypes) {
        Parameters.notNull("classBinaryName", classBinaryName);    //NOI18N
        Parameters.notNull("methodName", methodName);   //NOI18N
        Parameters.notNull("argumentTypes", argumentTypes); //NOI18N
        model.addMethod(classBinaryName, methodName, argumentTypes, INVOKE);
        return this;
    }


    @NonNull
    public WhiteListImplementationBuilder addOverridableMethod(
            @NonNull final String classBinaryName,
            @NonNull final String methodName,
            @NonNull final String... argumentTypes) {
        Parameters.notNull("classBinaryName", classBinaryName);    //NOI18N
        Parameters.notNull("methodName", methodName);   //NOI18N
        Parameters.notNull("argumentTypes", argumentTypes); //NOI18N
        model.addMethod(classBinaryName, methodName, argumentTypes, OVERRIDE);
        return this;
    }

    @NonNull
    public WhiteListQueryImplementation.WhiteListImplementation build() {
        return new WhiteList(model);
    }

    @NonNull
    public static WhiteListImplementationBuilder create() {
        return new WhiteListImplementationBuilder();
    }

    private static final class WhiteList implements WhiteListQueryImplementation.WhiteListImplementation {

        private final Model model;

        private WhiteList(@NonNull final Model model) {
            assert model != null;
            this.model = model;
        }

        @Override
        public boolean canInvoke(@NonNull ElementHandle<?> element) {
            assert element != null;
            return model.isAllowed(element,INVOKE);
        }

        @Override
        public boolean canOverride(@NonNull ElementHandle<?> element) {
            assert element != null;
            return model.isAllowed(element,OVERRIDE);
        }
    }

    private static final class Model {
        private int counter = 0;
        private final Map<String,Integer> names = new HashMap<String, Integer>();   //Todo: Replace by NameTbl
        private final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>> root =
                new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>>();


        void addClass(
                @NonNull final String binaryName,
                final byte mode) {
            final String[] pkgNamePair = splitName(binaryName,'/');
            final Integer pkgId = putName(FileObjects.convertFolder2Package(pkgNamePair[0]));
            @SuppressWarnings("RedundantStringConstructorCall")
            final Integer clsId = putName(new String(pkgNamePair[1]));
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode =
                root.putIfAbsent(pkgId, new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>());
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode =
                pkgNode.putIfAbsent(clsId, new IntermediateCacheNode<IntermediateCacheNode<CacheNode>>());
            clsNode.state |= mode;
        }

        void addMethod(
                @NonNull final String clsBinaryName,
                @NonNull final String methodName,
                @NonNull final String[] argTypes,
                @NonNull final byte mode) {
            final String[] pkgNamePair = splitName(clsBinaryName,'/');
            final Integer pkgId = putName(FileObjects.convertFolder2Package(pkgNamePair[0]));
            @SuppressWarnings("RedundantStringConstructorCall")
            final Integer clsId = putName(new String(pkgNamePair[1]));
            final Integer methodNameId = putName(methodName);
            final Integer metodSigId = putName(vmSignature(argTypes));
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode =
                root.putIfAbsent(pkgId, new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>());
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode =
                pkgNode.putIfAbsent(clsId, new IntermediateCacheNode<IntermediateCacheNode<CacheNode>>());
            final IntermediateCacheNode<CacheNode> methodNameNode =
                clsNode.putIfAbsent(methodNameId, new IntermediateCacheNode<CacheNode>());
            final CacheNode methodSigNode =
                methodNameNode.putIfAbsent(metodSigId, new CacheNode());
            methodSigNode.state |= mode;
        }

        boolean isAllowed(
                @NonNull final ElementHandle<?> element,
                final byte mode) {
            final String[] vmSignatures = SourceUtils.getJVMSignature(element);
            final String[] pkgNamePair = splitName(vmSignatures[0],'.');  //NOI18N
            final Integer pkgId = getName(pkgNamePair[0]);
            final Integer clsId = getName(pkgNamePair[1]);
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode = root.get(pkgId);
            if (pkgNode == null) {
                return false;
            }
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode = pkgNode.get(clsId);
            if (clsNode == null) {
                return false;
            }
            if ((clsNode.state & mode) == mode) {
                return true;
            }
            if (element.getKind() == ElementKind.METHOD ||
                element.getKind() == ElementKind.CONSTRUCTOR) {
                final Integer methodNameId = getName(vmSignatures[1]);
                final Integer methodSigId = getName(paramsOnly(vmSignatures[2]));
                final IntermediateCacheNode<CacheNode> methodNameNode = clsNode.get(methodNameId);
                if (methodNameNode == null) {
                    return false;
                }
                final CacheNode methodSigNode = methodNameNode.get(methodSigId);
                if (methodSigNode == null) {
                    return false;
                }
                return (methodSigNode.state & mode) == mode;
            } else if ((element.getKind().isClass() ||
                element.getKind().isInterface()) && clsNode.hasChildren()) {
                //If the request is for type and it has at least one alowed method
                //allow it. It would be strange to black list type usage which method is allowed
                return true;
            }
            return false;
        }

        private static class CacheNode {
            byte state;
        }

        private static class IntermediateCacheNode<T extends CacheNode> extends CacheNode {
            private Map<Integer,T> nodes = new HashMap<Integer, T>();

            @NonNull
            final T putIfAbsent (
                    @NonNull Integer id,
                    @NonNull T node) {
                assert id != null;
                assert node != null;
                T result = nodes.get(id);
                if (result == null) {
                    result = node;
                    nodes.put (id, result);
                }
                return result;
            }

            @CheckForNull
            final T get(@NullAllowed Integer id) {
                return id == null ? null : nodes.get(id);
            }

            final boolean hasChildren() {
                return !nodes.isEmpty();
            }
        }

        @NonNull
        private String[] splitName(
                @NonNull final String qName,
                final char separator) {
            int index = qName.lastIndexOf(separator);    //NOI18N
            String pkg, name;
            if (index == -1) {
                pkg = "";   //NOI18N
                name = qName;
            } else {
                pkg = qName.substring(0, index);
                name = qName.substring(index+1);
            }
            return new String[] {pkg, name};
        }

        @NonNull
        private String paramsOnly(
                @NonNull final String name) {
            assert name.charAt(0) == '(';   //NOI18N;
            int index = name.lastIndexOf(')');  //NOI18N
            assert index > 0;
            return name.substring(1, index);
        }

        @NonNull
        private Integer putName(@NonNull final String name) {
            assert name != null;
            Integer result = names.get(name);
            if (result == null) {
                result = counter++;
                names.put(name, result);
            }
            return result;
        }

        @CheckForNull
        private Integer getName(@NonNull final String name) {
            assert name != null;
            return names.get(name);
        }

        @NonNull
        private String vmSignature(
                @NonNull final String[] types) {
            final StringBuilder sb = new StringBuilder();
            for (String type : types) {
                encodeType(type,sb);
            }
            return sb.toString();
        }

        private void encodeType(
                @NonNull final String type,
                @NonNull StringBuilder sb) {
            assert type != null;
            assert sb != null;
            if ("void".equals(type)) {  //NOI18N
                sb.append('V');	    // NOI18N
            } else if ("boolean".equals(type)) {    //NOI18N
                sb.append('Z');	    // NOI18N
            } else if ("byte".equals(type)) {   //NOI18N
                sb.append('B');     //NOI18N
            } else if ("short".equals(type)) {    //NOI18N
                sb.append('S');	    // NOI18N
            } else if ("int".equals(type)) { //NOI18N
                sb.append('I');	    // NOI18N
            } else if ("long".equals(type)) {
                sb.append('J');	    // NOI18N
            } else if ("char".equals(type)) { //NOI18N
                sb.append('C');	    // NOI18N
            } else if ("float".equals(type)) {  //NOI18N
                sb.append('F');	    // NOI18N
            } else if ("double".equals(type)) {
                sb.append('D');	    // NOI18N
            } else if (type.charAt(type.length()-1) == ']') {   //NOI18N
                sb.append('[');
                encodeType(type.substring(0,type.length()-2), sb);
            } else {
                sb.append('L'); //NOI18N
                sb.append(type);
                sb.append(';'); //NOI18N
            }
        }
    }
}
