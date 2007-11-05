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
package org.netbeans.modules.xslt.tmap.model.validation;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XmlUtil;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XsltMapConst;
import org.netbeans.modules.xslt.tmap.util.TMapUtil;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformmapValidatorImpl implements TransformmapValidator {
    
    private static TransformmapValidator INSTANCE = new TransformmapValidatorImpl();

    private ValidationRule[] myValidationRules;
    
    private ComplexValidationRule[] myComplexValidationRules;

    private TransformmapValidatorImpl() {
        myValidationRules = new ValidationRule[] {
            new NoTransformationDescriptor(),
            new IsXsltMap(),
            new IsCorrectNs(),
            new IsOldTransformmap()
            
        };

        myComplexValidationRules = new ComplexValidationRule[] {
            new CantResolveTransform(),
            new CantResolveInputType(),
            new CantResolveOutputType()
        };
    }
    
    
    public static TransformmapValidator getInstance() {
        return INSTANCE;
    }
    
    private String validate(Object valObject) {
        String valResult = null;
        for (TransformmapValidatorImpl.ValidationRule rule : myValidationRules) {
            valResult = rule.validate(valObject);
            if (valResult != null) {
                break;
            }
        }
        return valResult;
    }
    
    private String validate(Object obj1, Object obj2) {
        String valResult = null;
        for (TransformmapValidatorImpl.ComplexValidationRule rule : myComplexValidationRules) {
            valResult = rule.validate(obj1, obj2);
            if (valResult != null) {
                break;
            }
        }
        return valResult;
    }

    public String validate(File transformDescriptor) {
        return validate((Object)transformDescriptor);
    }

    public String validate(Project project) {
        return validate((Object)project);
    }

    public String validate(TMapModel model, FileObject xsltFo) {
        return validate((Object)model, (Object)xsltFo);
    }

    private interface ValidationRule {
        boolean accept(Object valObject);
        String validate(Object valObject);
    }

    private interface ComplexValidationRule {
        boolean accept(Object obj1, Object obj2);
        String validate(Object obj1, Object obj2);
    }

    private class NoTransformationDescriptor implements ValidationRule {
        private String validate(Project project) {
            if (project == null) {
                return null;
            }
            
            File tDescriptor = Util.getTransformationDescriptor(project);
            if (tDescriptor == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, "Msg_NoTransformationDescriptor");
            }
            
            return null;
        }

        public boolean accept(Object valObject) {
            return valObject instanceof Project;
        }

        public String validate(Object valObject) {
            if (!accept(valObject)) {
                return null;
            }
            return validate((Project)valObject);
        }
    }
    
    private class IsXsltMap implements ValidationRule {
        private String validate(File transformDescriptor) {
            if (transformDescriptor == null) {
                return null;
            }
            
            String xsltMap = XsltMapConst.XSLTMAP+"."+XsltMapConst.XML;
            if (xsltMap.equals(transformDescriptor.getName())) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, "Msg_OldXsltmap");
            }
            return null;
        }

        private String validate(Project project) {
            if (project == null) {
                return null;
            }
            return validate(Util.getTransformationDescriptor(project));
        }

        public boolean accept(Object valObject) {
            return valObject instanceof File || valObject instanceof Project;
        }

        public String validate(Object valObject) {
            if (!accept(valObject)) {
                return null;
            }
            return valObject instanceof File ?  validate((File)valObject) 
                    : validate((Project)valObject);
        }
    }
    
    private class IsCorrectNs implements ValidationRule {
        private String validate(File transformDescriptor) {
            if (transformDescriptor == null) {
                return null;
            }
            
            Document descriptorDocument = XmlUtil.getDocument(transformDescriptor, true);
            if (descriptorDocument == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, "Msg_CantGetDescriptorDocument");
            }

            Element rootElement = descriptorDocument.getDocumentElement();
            if (rootElement == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, "Msg_CantGetRootElement");
            }

            String ns = rootElement.getNamespaceURI();
            ns = ns == null ? "" : ns;
            if (!TMapComponent.TRANSFORM_MAP_NS_URI.equals(ns)) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, "Msg_IncorrectNamespace", ns);
            }
            return null;
        }

        public boolean accept(Object valObject) {
            return valObject instanceof File;
        }

        public String validate(Object valObject) {
            if (!accept(valObject)) {
                return null;
            }
            return validate((File)valObject);
        }
    }

    private class IsOldTransformmap implements ValidationRule {
        private String validate(File transformDescriptor) {
            // TODO a
            return null;
        }

        private String validate(Project project) {
            return validate(Util.getTransformationDescriptor(project));
        }

        public boolean accept(Object valObject) {
            return valObject instanceof File || valObject instanceof Project;
        }

        public String validate(Object valObject) {
            if (!accept(valObject)) {
                return null;
            }
            return valObject instanceof File ? validate((File)valObject) 
                    : validate((Project)valObject);
        }
    }

    private class CantResolveTransform implements ComplexValidationRule {
        private String validate(TMapModel model, FileObject xsltFo) {
            if (model == null || xsltFo == null) {
                return null;
            }
            if (TMapUtil.getTransform(model, xsltFo) == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, 
                        "Msg_CantResolveTransformation",xsltFo.getNameExt()); // NOI18N
            }
            return null;
        }

        public boolean accept(Object obj1, Object obj2) {
            return obj1 instanceof TMapModel && obj2 instanceof FileObject;
        }

        public String validate(Object obj1, Object obj2) {
            if (!accept(obj1, obj2)) {
                return null;
            }
            return validate((TMapModel)obj1, (FileObject)obj2);
        }
    }

    private class CantResolveInputType implements ComplexValidationRule {
        private String validate(TMapModel model, FileObject xsltFo) {
            if (model == null || xsltFo == null) {
                return null;
            }
            
            Transform transform = TMapUtil.getTransform(model, xsltFo);
            if (transform == null) {
                return null;
            }
            
            if (TMapUtil.getSourceComponent(transform) == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, 
                        "Msg_CantResolveSourceType",xsltFo.getNameExt()); // NOI18N
            }
            return null;
        }

        public boolean accept(Object obj1, Object obj2) {
            return obj1 instanceof TMapModel && obj2 instanceof FileObject;
        }

        public String validate(Object obj1, Object obj2) {
            if (!accept(obj1, obj2)) {
                return null;
            }
            return validate((TMapModel)obj1, (FileObject)obj2);
        }
    }

    private class CantResolveOutputType implements ComplexValidationRule {
        private String validate(TMapModel model, FileObject xsltFo) {
            if (model == null || xsltFo == null) {
                return null;
            }
            
            Transform transform = TMapUtil.getTransform(model, xsltFo);
            if (transform == null) {
                return null;
            }
            
            if (TMapUtil.getTargetComponent(transform) == null) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, 
                        "Msg_CantResolveTargetType",xsltFo.getNameExt()); // NOI18N
            }
            return null;
        }

        public boolean accept(Object obj1, Object obj2) {
            return obj1 instanceof TMapModel && obj2 instanceof FileObject;
        }

        public String validate(Object obj1, Object obj2) {
            if (!accept(obj1, obj2)) {
                return null;
            }
            return validate((TMapModel)obj1, (FileObject)obj2);
        }
    }

    public String validate(AXIComponent axiComp, String typeParam) {
            if (axiComp == null || axiComp.getModel() == null || axiComp.getModel().getState() != XslModel.State.VALID) {
                return NbBundle.getMessage(TransformmapValidatorImpl.class, 
                        "MSG_Error_BadSchema",typeParam); // NOI18N
            }
        return null;
    }
    
}
