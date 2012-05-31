/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.KeyPresentationFactory;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.spi.KeyPresentationFactory.class)
public class KeyPresentationFactoryImpl implements KeyPresentationFactory {

    @Override
    public Key create(KeyDataPresentation presentation) {
        switch (presentation.getKindPresentation()) {
            case KeyObjectFactory.KEY_INCLUDED_FILE_STORAGE_KEY:
                return new IncludedFileStorageKey(presentation);
            case KeyObjectFactory.KEY_CLASSIFIER_CONTAINER_KEY:
                return new ClassifierContainerKey(presentation);
            case KeyObjectFactory.KEY_FILE_CONTAINER_KEY:
                return new FileContainerKey(presentation);
            case KeyObjectFactory.KEY_FILE_DECLARATIONS_KEY:
                return new FileDeclarationsKey(presentation);
            case KeyObjectFactory.KEY_FILE_INCLUDES_KEY:
                return new FileIncludesKey(presentation);
            case KeyObjectFactory.KEY_FILE_KEY:
                return new FileKey(presentation);
            case KeyObjectFactory.KEY_FILE_MACROS_KEY:
                return new FileMacrosKey(presentation);
            case KeyObjectFactory.KEY_FILE_REFERENCES_KEY:
                return new FileReferencesKey(presentation);
            case KeyObjectFactory.KEY_FILE_INSTANTIATIONS_KEY:
                return new FileInstantiationsKey(presentation);
            case KeyObjectFactory.KEY_GRAPH_CONTAINER_KEY:
                return new GraphContainerKey(presentation);
            case KeyObjectFactory.KEY_NS_DECLARATION_CONTAINER_KEY:
                return new NamespaceDeclarationContainerKey(presentation);
            case KeyObjectFactory.KEY_NAMESPACE_KEY:
                return new NamespaceKey(presentation);
            case KeyObjectFactory.KEY_PROJECT_DECLARATION_CONTAINER_KEY:
                return new ProjectDeclarationContainerKey(presentation);
            case KeyObjectFactory.KEY_PROJECT_KEY:
                return new ProjectKey(presentation);
            case KeyObjectFactory.KEY_PRJ_VALIDATOR_KEY:
                return new ProjectSettingsValidatorKey(presentation);
            case 'I':
                return new IncludeKey(presentation);
            case 'i':
                return new InstantiationKey(presentation);
            case 'h':
            case 'y':
            case 'H':
            case 'Y':
                return new InheritanceKey(presentation);
            case 'P':
                return new ParamListKey(presentation);
            default:
                CsmDeclaration.Kind kind = Utils.getCsmDeclarationKind((char)presentation.getKindPresentation());
                if (kind != null) {
                    switch (kind) {
                        case MACRO:
                            return new MacroKey(presentation);
                        case ASM:
                        case BUILT_IN:
                        case CLASS:
                        case ENUM:
                        case FUNCTION:
                        case NAMESPACE_DEFINITION:
                        case STRUCT:
                        case TEMPLATE_DECLARATION:
                        case UNION:
                        case VARIABLE:
                        case NAMESPACE_ALIAS:
                        case ENUMERATOR:
                        case FUNCTION_DEFINITION:
                        case FUNCTION_LAMBDA:
                        case FUNCTION_INSTANTIATION:
                        case USING_DIRECTIVE:
                        case TEMPLATE_PARAMETER:
                        case CLASS_FRIEND_DECLARATION:
                        case TEMPLATE_SPECIALIZATION:
                        case TYPEDEF:
                        case USING_DECLARATION:
                        case VARIABLE_DEFINITION:
                        case CLASS_FORWARD_DECLARATION:
                        case ENUM_FORWARD_DECLARATION:
                        case FUNCTION_FRIEND:
                        case FUNCTION_FRIEND_DEFINITION:
                            return new OffsetableDeclarationKey(presentation);
                    }
                }

        }
        return null;
    }
    
}
