/*
 * CustomAIPModuleInstaller.java
 *
 * Created on February 1, 2007, 5:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.encoder.custom.aip;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.openide.modules.ModuleInstall;
import org.openide.util.WeakListeners;

/**
 * The ModuleInstall implementation of this module.
 *
 * @author Jun Xu
 */
public class CustomAIPModuleInstaller extends ModuleInstall {

    private static final SMFPropertyChangeListener mSMFPropertyChangeListener =
            new SMFPropertyChangeListener();

    @Override
    public void restored() {
        super.restored();
        SchemaModelFactory.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(mSMFPropertyChangeListener,
                    SchemaModelFactory.getDefault()));
    }
    
    private static class SMFPropertyChangeListener implements PropertyChangeListener {
        
        private static final SchemaModelPropListener mModelPropListener =
                new SchemaModelPropListener();
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (SchemaModelFactory.MODEL_LOADED_PROPERTY.equals(evt.getPropertyName())
                    && (evt.getNewValue() instanceof SchemaModel)) {
                SchemaModel model = (SchemaModel) evt.getNewValue();
                model.addPropertyChangeListener(WeakListeners.propertyChange(
                                mModelPropListener,
                                model));
            }
        }
        
    }
    
    private static class SchemaModelPropListener implements PropertyChangeListener {

        private static final QName TYPE_ATTR_NAME = new QName("type"); //NOI18N
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getOldValue() != null || !(evt.getNewValue() instanceof Element)
                    || (evt.getNewValue() instanceof ElementReference)
                    || !("content".equals(evt.getPropertyName()) //NOI18N
                        || "elements".equals(evt.getPropertyName()))) { //NOI18N
                return;
            }
            final Element elem = (Element) evt.getNewValue();
            List<SchemaComponent> children;
            String tv;
            if (elem == null || elem.getModel().inSync()
                    || !ModelUtils.isEncodedWith(elem.getModel(), CustomEncodingConst.STYLE)) {
                return;
            }
            boolean setTypeToString = true;
            if (((children = elem.getChildren()) != null && !children.isEmpty())
                    || ((tv = elem.getAnyAttribute(TYPE_ATTR_NAME)) != null && tv.length() > 0)) {
                setTypeToString = false;
            }
            final boolean setTypeToStringFinal = setTypeToString;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SchemaModel model = elem.getModel();
                    if (model.inSync()) {
                        return;
                    }

                    boolean startedTrans = false;
                    try {
                        if (!model.isIntransaction()) {
                            if (!model.startTransaction()) {
                                //TODO: how to handle
                            }
                            startedTrans = true;
                        }
                        processElementDefault(elem, setTypeToStringFinal);
                    } finally {
                        if (startedTrans) {
                            model.endTransaction();
                        }
                    }
                }
            });
        }
        
        private void processElementDefault(Element elem, boolean setTypeToString) {
            if (setTypeToString) {
                org.w3c.dom.Element domElem = elem.getPeer();
                if (domElem != null) {
                    String prefix = domElem.lookupPrefix(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    String value;
                    if (prefix != null && prefix.length() > 0) {
                        value = prefix + ":string";   //NOI18N
                    } else {
                        value = "string";  //NOI18N
                    }
                    elem.setAnyAttribute(TYPE_ATTR_NAME, value);
                }
            }
            Annotation anno = elem.getAnnotation();
            if (anno != null) {
                return;
            }
            anno = elem.getModel().getFactory().createAnnotation();
            elem.setAnnotation(anno);
            AppInfo appInfo = elem.getModel().getFactory().createAppInfo();
            appInfo.setURI(EncodingConst.URI);
            anno.addAppInfo(appInfo);
        }
    }
}
