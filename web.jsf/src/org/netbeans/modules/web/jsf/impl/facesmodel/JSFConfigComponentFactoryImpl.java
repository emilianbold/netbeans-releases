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

    public ResourceBundleImpl createResourceBundle() {
        return new ResourceBundleImpl(model);
    }
    
    public static boolean areSameQName(JSFConfigQNames jsfqname,Element element) {
        QName qname = AbstractDocumentComponent.getQName(element);
        if (JSFConfigQNames.JSF_1_2_NS.equals(element.getNamespaceURI())){
            return jsfqname.getQName(JSFVersion.JSF_1_2).equals(qname);
        }
        return jsfqname.getLocalName().equals(qname.getLocalPart());
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
            } else if (isElementQName(JSFConfigQNames.RESOURCE_BUNDLE)) {
                created = new ResourceBundleImpl((JSFConfigModelImpl)context.getModel(), element);
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
        
        @Override
        public void visit(ResourceBundle context) {
            checkDescriptionGroup(context);
        }
    }

}
