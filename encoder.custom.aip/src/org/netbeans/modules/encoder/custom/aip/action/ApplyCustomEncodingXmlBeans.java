/*
 * ApplyCustomEncodingXmlBeans.java
 *
 * Created on February 7, 2007, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.encoder.custom.aip.action;

import java.io.File;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.AppinfoDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexRestrictionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.Group;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.netbeans.modules.encoder.ui.basic.EncodingConst;

/**
 * Class used for applying custom encoding offline.
 *
 * @author Jun Xu
 */
public class ApplyCustomEncodingXmlBeans {
    
    /**
     * Applies custom encoding to an XSD file.
     *
     * @param xsdFile the XSD file
     * @return returns true if the XSD file is modified, otherwise false
     */
    public static boolean applyDetailCustomEncoding(File xsdFile)
            throws XmlException, IOException {
        
        SchemaDocument schemaDoc = SchemaDocument.Factory.parse(xsdFile);
        
        NamedGroup[] modelGroups = schemaDoc.getSchema().getGroupArray();
        for (int i = 0; modelGroups != null && i < modelGroups.length; i++) {
            applyDetailCustomEncoding(modelGroups[i]);
        }
        TopLevelComplexType[] complexTypes = schemaDoc.getSchema().getComplexTypeArray();
        for (int i = 0; complexTypes != null && i < complexTypes.length; i++) {
            applyDetailCustomEncoding(complexTypes[i]);
        }
        TopLevelElement[] elemDecls = schemaDoc.getSchema().getElementArray();
        for (int i = 0; elemDecls != null && i < elemDecls.length; i++) {
            applyDetailCustomEncoding(elemDecls[i]);
        }
        
        schemaDoc.save(xsdFile);
        return true;
    }
    
    private static void applyDetailCustomEncoding(Element elemDecl) {
        if (elemDecl.isSetComplexType()) {
            applyDetailCustomEncoding(elemDecl.getComplexType());
        }
        if (!elemDecl.isSetAnnotation()) {
            elemDecl.addNewAnnotation();
        }
        AnnotationDocument.Annotation anno = elemDecl.getAnnotation();
        AppinfoDocument.Appinfo[] appinfos = anno.getAppinfoArray();
        for (int i = 0; appinfos != null && i < appinfos.length; i++) {
            if (EncodingConst.URI.equals(appinfos[i].getSource())) {
                return;
            }
        }
        anno.addNewAppinfo();
        anno.getAppinfoArray(anno.sizeOfAppinfoArray() - 1).setSource(EncodingConst.URI);
    }
    
    private static void applyDetailCustomEncoding(Group group) {
        ExplicitGroup[] sequences = group.getSequenceArray();
        for (int i = 0; sequences != null && i < sequences.length; i++) {
            applyDetailCustomEncoding(sequences[i]);
        }
        ExplicitGroup[] choices = group.getChoiceArray();
        for (int i = 0; choices != null && i < choices.length; i++) {
            applyDetailCustomEncoding(choices[i]);
        }
        All[] alls = group.getAllArray();
        for (int i = 0; alls != null && i < alls.length; i++) {
            applyDetailCustomEncoding(alls[i]);
        }
        LocalElement[] elemDecls = group.getElementArray();
        for (int i = 0; elemDecls != null && i < elemDecls.length; i++) {
            applyDetailCustomEncoding(elemDecls[i]);
        }
    }
    
    private static void applyDetailCustomEncoding(ComplexType complexType) {
        if (complexType.isSetComplexContent()) {
            if (complexType.getComplexContent().isSetExtension()) {
                ExtensionType extType = complexType.getComplexContent().getExtension();
                if (extType.isSetSequence()) {
                    applyDetailCustomEncoding(extType.getSequence());
                } else if (extType.isSetChoice()) {
                    applyDetailCustomEncoding(extType.getChoice());
                } else if (extType.isSetAll()) {
                    applyDetailCustomEncoding(extType.getAll());
                }
            } else if (complexType.getComplexContent().isSetRestriction()) {
                ComplexRestrictionType resType = complexType.getComplexContent().getRestriction();
                if (resType.isSetSequence()) {
                    applyDetailCustomEncoding(resType.getSequence());
                } else if (resType.isSetChoice()) {
                    applyDetailCustomEncoding(resType.getChoice());
                } else if (resType.isSetAll()) {
                    applyDetailCustomEncoding(resType.getAll());
                }
            }
        } else if (complexType.isSetSequence()) {
            applyDetailCustomEncoding(complexType.getSequence());
        } else if (complexType.isSetChoice()) {
            applyDetailCustomEncoding(complexType.getChoice());
        } else if (complexType.isSetAll()) {
            applyDetailCustomEncoding(complexType.getAll());
        }
    }
}
