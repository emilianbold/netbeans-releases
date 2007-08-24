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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigComponentFactoryImpl implements JSFConfigComponentFactory {
    
    private static final Logger LOGGER = Logger.getLogger(JSFConfigComponentFactoryImpl.class.getName());
    
    private final JSFConfigModelImpl model;
    
    /** Creates a new instance of JSFConfigComponentFactoruImpl */
    public JSFConfigComponentFactoryImpl(JSFConfigModelImpl model) {
        this.model = model;
    }
    
    public JSFConfigComponent create(Element element, JSFConfigComponent context) {
        LOGGER.fine( "Element: " +  element.getLocalName() +", JSFConfigComponent: " + context);
        JSFConfigComponent configComponent = null;
        if (context == null){
            if (areSameQName(JSFConfigQNames.FACES_CONFIG, element)){
                configComponent = new FacesConfigImpl(model, element);
            }
        } else {
            configComponent = new CreateVisitor().create(element, context);
        }
        return configComponent;
    }
    
    public FacesConfig createFacesConfig() {
        return new FacesConfigImpl(model);
    }
    
    public ManagedBean createManagedBean() {
        return new ManagedBeanImpl(model);
    }
    
    public NavigationRule createNavigationRule(){
        return new NavigationRuleImpl(model);
    }
    
    public NavigationCase createNavigationCase() {
        return new NavigationCaseImpl(model);
    }
    
    public Converter createConverter() {
        return new ConverterImpl(model);
    }
    
    public Description createDescription() {
        return new DescriptionImpl (model);
    }

    public DisplayName createDisplayName() {
        return new DisplayNameImpl(model);
    }

    public Icon createIcon() {
        return new IconImpl(model);
    }
    
    public Application createApplication() {
        return new ApplicationImpl(model);
    }
    
    public ViewHandler createViewHandler() {
        return new ViewHandlerImpl(model);
    }
    
    public LocaleConfig createLocaleConfig() {
        return new LocaleConfigImpl(model);
    }

    public DefaultLocaleImpl createDefatultLocale() {
        return new DefaultLocaleImpl(model);
    }

    public SupportedLocaleImpl createSupportedLocale() {
        return new SupportedLocaleImpl(model);
    }

    public static boolean areSameQName(JSFConfigQNames jsfqname,Element element) {
        QName qname = AbstractDocumentComponent.getQName(element);
        boolean aresame = false;
        JSFVersion version = JSFVersion.JSF_1_1;
        if (element.getNamespaceURI() != null && element.getNamespaceURI().length()>0)
            version = JSFVersion.JSF_1_2;
        if (element.getNamespaceURI() != null){
            aresame = jsfqname.getQName(version).equals(qname);
        } else {
            aresame = jsfqname.getLocalName().equals(qname.getLocalPart());
        }
        return aresame;
    }
    
    public static class CreateVisitor extends JSFConfigVisitor.Default {
        Element element;
        JSFConfigComponent created;
        
        JSFConfigComponent create(Element element, JSFConfigComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(JSFConfigQNames jsfqname) {
            return areSameQName(jsfqname, element);
        }
        
        @Override
        public void visit(FacesConfig context) {
            
            if (isElementQName(JSFConfigQNames.MANAGED_BEAN)) {
                created = new ManagedBeanImpl((JSFConfigModelImpl) context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.NAVIGATION_RULE)) {
                created = new NavigationRuleImpl((JSFConfigModelImpl) context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.CONVERTER)) {
                created = new ConverterImpl((JSFConfigModelImpl) context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.APPLICATION)) {
                created = new ApplicationImpl((JSFConfigModelImpl) context.getModel(), element);
            }
        }
        
        @Override
        public void visit(ManagedBean context) {
            if (isElementQName(JSFConfigQNames.MANAGED_BEAN)) {
                created = new ManagedBeanImpl((JSFConfigModelImpl)context.getModel(), element);
            } else {
                checkDescriptionGroup(context);
            }
        }
        
        @Override
        public void visit(NavigationRule context) {
            if (isElementQName(JSFConfigQNames.NAVIGATION_CASE)) {
                created = new NavigationCaseImpl((JSFConfigModelImpl)context.getModel(), element);
            } else {
                checkDescriptionGroup(context);
            }
        }
        
        @Override
        public void visit(Converter context) {
            if (isElementQName(JSFConfigQNames.NAVIGATION_CASE)) {
                created = new ConverterImpl((JSFConfigModelImpl)context.getModel(), element);
            } else {
                checkDescriptionGroup(context);
            }
        }
        
        @Override
        public void visit(Application context) {
            if (isElementQName(JSFConfigQNames.VIEW_HANDLER)) {
                created = new ViewHandlerImpl((JSFConfigModelImpl)context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.LOCALE_CONFIG)) {
                created = new LocaleConfigImpl((JSFConfigModelImpl)context.getModel(), element);
            }
        }
        
        @Override
        public void visit(LocaleConfig context) {
            if (isElementQName(JSFConfigQNames.DEFAULT_LOCALE)) {
                created = new DefaultLocaleImpl((JSFConfigModelImpl)context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.SUPPORTED_LOCALE)) {
                created = new SupportedLocaleImpl((JSFConfigModelImpl)context.getModel(), element);
            }
        }

        public void checkDescriptionGroup(JSFConfigComponent context){
            if (isElementQName(JSFConfigQNames.DESCRIPTION)){
                created = new DescriptionImpl((JSFConfigModelImpl)context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.DISPLAY_NAME)){
                created = new DisplayNameImpl((JSFConfigModelImpl)context.getModel(), element);
            } else if (isElementQName(JSFConfigQNames.ICON)){
                created = new IconImpl((JSFConfigModelImpl)context.getModel(), element);
            }
        }
    }

}
