/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.model.validation;

import java.io.File;
import org.netbeans.api.project.Project;
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
    
}
