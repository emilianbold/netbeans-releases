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

import org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering;
import org.netbeans.modules.web.jsf.api.facesmodel.ActionListener;
import org.netbeans.modules.web.jsf.api.facesmodel.After;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.Before;
import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.DisplayName;
import org.netbeans.modules.web.jsf.api.facesmodel.ElResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator;
import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.Factory;
import org.netbeans.modules.web.jsf.api.facesmodel.Icon;
import org.netbeans.modules.web.jsf.api.facesmodel.If;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle;
import org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ListEntries;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.MapEntries;
import org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.Ordering;
import org.netbeans.modules.web.jsf.api.facesmodel.Others;
import org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal;
import org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener;
import org.netbeans.modules.web.jsf.api.facesmodel.Property;
import org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.Redirect;
import org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKit;
import org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.StateManager;
import org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId;
import org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewParam;
import org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
class JSFConfigComponentFactoryImpl implements JSFConfigComponentFactory {
    
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
    
    public ActionListener createActionListener() {
        return new ActionListenerImpl( model );
    }
    
    public DefaultRenderKitId createDefaultRenderKitId(){
        return new DefaultRenderKitIdImpl( model );
    }
    
    public MessageBundle createMessageBundle(){
        return new MessageBundleImpl( model );
    }
    
    public NavigationHandler createNavigationHandler(){
        return new NavigationHandlerImpl( model );
    }
    
    public PartialTraversal createPartialTraversal(){
        return new PartialTraversalImpl( model );
    }
    
    public StateManager createStateManager(){
        return new StateManagerImpl( model );
    }
    
    public ElResolver createElResolver(){
        return new ElResolverImpl( model );
    }
    
    public PropertyResolver createPropertyResolver(){
        return new PropertyResolverImpl( model );
    }
    
    public VariableResolver createVariableResolver(){
        return new VariableResolverImpl( model );
    }
    
    public ResourceHandler createResourceHandler(){
        return new ResourceHandlerImpl( model );
    }
    
    public FacesSystemEventListener createSystemEventListener(){
        return new SystemEventListenerImpl( model );
    }
    
    public DefaultValidators createDefaultValidators(){
        return new DefaultValidatorsImpl( model );
    }
    
    public Ordering createOrdering(){
        return new OrderingImpl( model );
    }
    
    public Before createBefore(){
        return new BeforeImpl( model );
    }
    
    public After createAfter(){
        return new AfterImpl( model );
    }
    
    public Name createName(){
        return new NameImpl( model );
    }
    
    public Others createOthers(){
        return new OthersImpl( model );
    }
    
    public AbsoluteOrdering createAbsoluteOrdering(){
        return new AbsoluteOrderingImpl( model );
    }
    
    public FacesValidatorId createValidatorId(){
        return new ValidatorIdImpl( model );
    }
    
    public Factory createFactory(){
        return new FactoryImpl( model );
    }
    
    public ApplicationFactory createApplicationFactory (){
        return new ApplicationFactoryImpl( model );
    }
    
    public ExceptionHandlerFactory createExceptionHandlerFactory(){
        return new ExceptionHandlerFactoryImpl( model );
    }
    
    public ExternalContextFactory createExternalContextFactory( ){
        return new ExternalContextFactoryImpl( model );
    }
    
    public FacesContextFactory createFacesContextFactory(){
        return new FacesContextFactoryImpl( model );
    }
    
    public PartialViewContextFactory createPartialViewContextFactory( ){
        return new PartialViewContextFactoryImpl( model );
    }
    
    public LifecycleFactory createLifecycleFactory(){
        return new LifecycleFactoryImpl( model );
    }
    
    public ViewDeclarationLanguageFactory createViewDeclarationLanguageFactory( ){
        return new ViewDeclarationLanguageFactoryImpl( model );
    }
    
    public TagHandlerDelegateFactory createTagHandlerDelegateFactory( ){
        return new TagHandlerDelegateFactoryImpl( model );
    }
    
    public RenderKitFactory createRenderKitFactory( ){
        return new RenderKitFactoryImpl( model );
    }
    
    public VisitContextFactory createVisitContextFactory( ){
        return new VisitContextFactoryImpl( model );
    }
    
    public FacesComponent createComponent( ){
        return new ComponentImpl( model );
    }
    
    public Facet createFacet( ){
        return new FacetImpl( model );
    }
    
    public ConfigAttribute createAttribute( ){
        return new AttributeImpl( model );
    }
    
    public Property createProperty( ){
        return new PropertyImpl( model );
    }
    
    public FacesManagedProperty createManagedProperty( ){
        return new ManagedPropertyImpl( model );
    }
    
    public ListEntries createListEntries( ){
        return new ListEntriesImpl( model );
    }
    
    public MapEntries createMapEntries( ){
        return new MapEntriesImpl( model );
    }
    
    public If createIf( ){
        return new IfImpl( model );
    }
    
    public Redirect createRedirect( ){
        return new RedirectImpl( model );
    }
    
    public ViewParam createViewParam( ){
        return new ViewParamImpl( model );
    }
    
    public ReferencedBean createReferencedBean( ){
        return new ReferencedBeanImpl( model );
    }
    
    public RenderKit createRenderKit( ){
        return new RenderKitImpl( model );
    }
    
    public FacesRenderer createRenderer( ){
        return new RendererImpl( model );
    }
    
    public FacesClientBehaviorRenderer createClientBehaviorRenderer( ){
        return new ClientBehaviorRendererImpl( model );
    }
    
    public Lifecycle createLifecycle( ){
        return new LifecycleImpl( model );
    }
    
    public PhaseListener createPhaseListener( ){
        return new PhaseListenerImpl( model );
    }
    
    public FacesValidator createValidator( ){
        return new ValidatorImpl( model );
    }
    
    public FacesBehavior createBehavior( ){
        return new BehaviorImpl( model );
    }
    
    public static boolean areSameQName(JSFConfigQNames jsfqname,Element element) {
        QName qname = AbstractDocumentComponent.getQName(element);
        if (JSFConfigQNames.JSF_1_2_NS.equals(element.getNamespaceURI())){
            return jsfqname.getQName(JSFVersion.JSF_1_2).equals(qname);
        }
        else if (JSFConfigQNames.JSF_2_0_NS.equals(element.getNamespaceURI())){
            return jsfqname.getQName(JSFVersion.JSF_2_0).equals(qname);
        }
        return jsfqname.getLocalName().equals(qname.getLocalPart());
    }
    
    static class CreateVisitor extends JSFConfigVisitor.Default {
        Element element;
        JSFConfigComponent created;
        JSFConfigModelImpl myModel;
        
        JSFConfigComponent create(Element element, JSFConfigComponent context) {
            this.element = element;
            created = null;
            myModel = (JSFConfigModelImpl)context.getModel();
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(JSFConfigQNames jsfqname) {
            return areSameQName(jsfqname, element);
        }
        
        @Override
        public void visit(FacesConfig context) {
            
            if (isElementQName(JSFConfigQNames.MANAGED_BEAN)) {
                created = new ManagedBeanImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.NAVIGATION_RULE)) {
                created = new NavigationRuleImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.CONVERTER)) {
                created = new ConverterImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.APPLICATION)) {
                created = new ApplicationImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.ORDERING)) {
                created = new OrderingImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.ABSOLUTE_ORDERING)){
                created = new AbsoluteOrderingImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.FACTORY)){
                created = new FactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.COMPONENT)){
                created = new ComponentImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.NAME)){
                created = new NameImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.REFERENCED_BEAN)){
                created = new ReferencedBeanImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.RENDER_KIT)){
                created = new RenderKitImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.LIFECYCLE)){
                created = new LifecycleImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.VALIDATOR)){
                created = new ValidatorImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.BEHAVIOR)){
                created = new BehaviorImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(ManagedBean context) {
            if (isElementQName(JSFConfigQNames.MANAGED_PROPERTY)) {
                created = new ManagedPropertyImpl(
                        myModel, element);
            } 
            else if (isElementQName(JSFConfigQNames.MAP_ENTRIES)) {
                created = new MapEntriesImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.LIST_ENTRIES)) {
                created = new ListEntriesImpl(
                        myModel, element);
            }
            else {
                checkDescriptionGroup(context);
            }
        }
        
        @Override
        public void visit(NavigationRule context) {
            if (isElementQName(JSFConfigQNames.NAVIGATION_CASE)) {
                created = new NavigationCaseImpl(
                        myModel, element);
            } else {
                checkDescriptionGroup(context);
            }
        }
        
        @Override
        public void visit(NavigationCase caze) {
            if (isElementQName(JSFConfigQNames.IF)) {
                created = new IfImpl(
                        myModel, element);
            } 
            else if (isElementQName(JSFConfigQNames.REDIRECT)) {
                created = new RedirectImpl(
                        myModel, element);
            } 
            else {
                checkDescriptionGroup( caze);
            }
        }
        
        @Override
        public void visit(Redirect redirect) {
            if (isElementQName(JSFConfigQNames.VIEW_PARAM)) {
                created = new ViewParamImpl(
                        myModel, element);
            } 
        }
        
        @Override
        public void visit(ReferencedBean bean) {
            checkDescriptionGroup( bean);
        }
        
        @Override
        public void visit(RenderKit kit) {
            if (isElementQName(JSFConfigQNames.RENDERER)) {
                created = new RendererImpl(
                        myModel, element);
            } 
            else if (isElementQName(JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER)) {
                created = new ClientBehaviorRendererImpl(
                        myModel, element);
            } 
            else {
                checkDescriptionGroup( kit);
            }
        }
        
        @Override
        public void visit(Property property ) {
            checkDescriptionGroup( property );
        }
        
        @Override
        public void visit(FacesRenderer renderer) {
            if (isElementQName(JSFConfigQNames.FACET)) {
                created = new FacetImpl(
                        myModel, element);
            } 
            else {
                checkDescriptionGroup( renderer );
                visitAttributeContainer();
            }
        }
        
        @Override
        public void visit(FacesBehavior behavior) {
            checkDescriptionGroup( behavior  );
            visitAttributeContainer();
            visitPropertyContainer();
        }
        
        @Override
        public void visit(FacesValidator validator) {
            checkDescriptionGroup( validator );
            visitAttributeContainer();
            visitPropertyContainer();
        }
        
        @Override
        public void visit(Facet facet) {
            checkDescriptionGroup( facet );
        }
        
        @Override
        public void visit(Converter context) {
            if (isElementQName(JSFConfigQNames.NAVIGATION_CASE)) {
                created = new ConverterImpl(
                        myModel, element);
            } else {
                checkDescriptionGroup(context);
                visitAttributeContainer();
                visitPropertyContainer();
            }
        }
        
        @Override
        public void visit(Application context) {
            if (isElementQName(JSFConfigQNames.VIEW_HANDLER)) {
                created = new ViewHandlerImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.LOCALE_CONFIG)) {
                created = new LocaleConfigImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.RESOURCE_BUNDLE)) {
                created = new ResourceBundleImpl(
                        myModel, element);
            }
            else if ( isElementQName( JSFConfigQNames.ACTION_LISTENER)){
                created = new ActionListenerImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.DEFAULT_RENDER_KIT_ID)){
                created = new DefaultRenderKitIdImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.MESSAGE_BUNDLE)){
                created = new MessageBundleImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.NAVIGATION_HANDLER)){
                created = new NavigationHandlerImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.PARTIAL_TRAVERSAL)){
                created = new PartialTraversalImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.STATE_MANAGER)){
                created = new StateManagerImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.STATE_MANAGER)){
                created = new ElResolverImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.PROPERTY_RESOLVER)){
                created = new PropertyResolverImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.VARIABLE_RESOLVER)){
                created = new VariableResolverImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.RESOURCE_HANDLER)){
                created = new ResourceHandlerImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.SYSTEM_EVENT_LISTENER)){
                created = new SystemEventListenerImpl( myModel , element );
            }
            else if ( isElementQName( JSFConfigQNames.DEFAULT_VALIDATORS)){
                created = new DefaultValidatorsImpl( myModel , element );
            }
            
        }
        
        @Override
        public void visit(LocaleConfig context) {
            if (isElementQName(JSFConfigQNames.DEFAULT_LOCALE)) {
                created = new DefaultLocaleImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.SUPPORTED_LOCALE)) {
                created = new SupportedLocaleImpl(
                        myModel, element);
            }
        }

        public void checkDescriptionGroup(JSFConfigComponent context){
            if (isElementQName(JSFConfigQNames.DESCRIPTION)){
                created = new DescriptionImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.DISPLAY_NAME)){
                created = new DisplayNameImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.ICON)){
                created = new IconImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(ResourceBundle context) {
            checkDescriptionGroup(context);
        }
        
        @Override
        public void visit(Lifecycle context) {
            if (isElementQName(JSFConfigQNames.PHASE_LISTENER)) {
                created = new PhaseListenerImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(Ordering context) {
            if (isElementQName(JSFConfigQNames.AFTER)){
                created = new AfterImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.BEFORE)){
                created = new BeforeImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(Before context) {
            visitOrderingElement(  );
        }
        
        @Override
        public void visit(After context) {
            visitOrderingElement( );
        }
        
        @Override
        public void visit(DefaultValidators validators) {
            if (isElementQName(JSFConfigQNames.VALIDATOR_ID)){
                created = new ValidatorIdImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(Factory factory) {
            if (isElementQName(JSFConfigQNames.APPLICATION_FACTORY)){
                created = new ApplicationFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.EXCEPTION_HANDLER_FACTORY)){
                created = new ExceptionHandlerFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.EXTERNAL_CONTEXT_FACTORY)){
                created = new ExternalContextFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.FACES_CONTEXT_FACTORY)){
                created = new FacesContextFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.PARTIAL_VIEW_CONTEXT_FACTORY)){
                created = new PartialViewContextFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.LIFECYCLE_FACTORY)){
                created = new LifecycleFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.VIEW_DECLARATION_LANGUAGE_FACTORY)){
                created = new ViewDeclarationLanguageFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.TAG_HANDLER_DELEGATE_FACTORY)){
                created = new TagHandlerDelegateFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.RENDER_KIT_FACTORY)){
                created = new RenderKitFactoryImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.VISIT_CONTEXT_FACTORY)){
                created = new VisitContextFactoryImpl(
                        myModel, element);
            }
        }
        
        @Override
        public void visit(FacesComponent component) {
            if (isElementQName(JSFConfigQNames.FACET)){
                created = new FacetImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.ATTRIBUTE)){
                created = new AttributeImpl(
                        myModel, element);
            }
            else if (isElementQName(JSFConfigQNames.PROPERTY)){
                created = new PropertyImpl(
                        myModel, element);
            }
            else {
                checkDescriptionGroup(component);
                visitAttributeContainer();
                visitPropertyContainer();
            }
        }
        
        @Override
        public void visit(ConfigAttribute attr) {
            checkDescriptionGroup(attr);
        }
        
        @Override
        public void visit(FacesManagedProperty property) {
            if (isElementQName(JSFConfigQNames.VALIDATOR_ID)){
                created = new ValidatorIdImpl(
                        myModel, element);
            }
            else {
                checkDescriptionGroup(property);
            }
        }
        
        private void visitOrderingElement( ){
            if (isElementQName(JSFConfigQNames.NAME)){
                created = new NameImpl(
                        myModel, element);
            } else if (isElementQName(JSFConfigQNames.OTHERS)){
                created = new OthersImpl(
                        myModel, element);
            }
        }
        
        private void visitAttributeContainer(){
            if (isElementQName(JSFConfigQNames.ATTRIBUTE)){
                created = new AttributeImpl(
                        myModel, element);
            } 
        }
        
        private void visitPropertyContainer(){
            if (isElementQName(JSFConfigQNames.PROPERTY)){
                created = new PropertyImpl(
                        myModel, element);
            } 
        }
    } 

}
