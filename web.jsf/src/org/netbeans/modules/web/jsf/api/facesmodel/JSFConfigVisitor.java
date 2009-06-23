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

package org.netbeans.modules.web.jsf.api.facesmodel;



/**
 *
 * @author Petr Pisl
 */
public interface JSFConfigVisitor {

    void visit(FacesConfig component);
    void visit(ManagedBean component);
    void visit(NavigationRule component);
    void visit(NavigationCase component);
    void visit(Converter component);
    void visit(Description component);
    void visit(DisplayName compoent);
    void visit(Icon component);
    void visit(ViewHandler component);
    void visit(Application component);
    void visit(LocaleConfig component);
    void visit(DefaultLocale component);
    void visit(SupportedLocale component);
    void visit(ResourceBundle component);
    void visit( ActionListener listener );
    void visit( DefaultRenderKitId id );
    void visit( MessageBundle bundle );
    void visit( NavigationHandler handler );
    void visit( PartialTraversal traversal );
    void visit( StateManager traversal );
    void visit( ElResolver traversal );
    void visit( PropertyResolver traversal );
    void visit( VariableResolver traversal );
    void visit( ResourceHandler traversal );
    void visit( FacesSystemEventListener listener );
    void visit( DefaultValidators validators );
    void visit( Ordering ordering );
    void visit( After after);
    void visit( Before before );
    void visit( Name name);
    void visit( Others others );
    void visit( AbsoluteOrdering absoluteOrdering );
    void visit( Factory factory );
    void visit( FacesValidatorId id );
    void visit( ApplicationFactory factory );
    void visit( ExceptionHandlerFactory factory );
    void visit( ExternalContextFactory factory );
    void visit( FacesContextFactory factory);
    void visit( PartialViewContextFactory factory );
    void visit( LifecycleFactory factory );
    void visit( ViewDeclarationLanguageFactory factory );
    void visit( TagHandlerDelegateFactory factory );
    void visit( RenderKitFactory factory );
    void visit( VisitContextFactory factory );
    void visit( FacesComponent component );
    void visit( Facet component );
    void visit( ConfigAttribute attr );
    void visit( Property property );
    void visit( FacesManagedProperty property );
    void visit( ListEntries entries );
    void visit( MapEntries entries );
    void visit( If iff );
    void visit( Redirect redirect);
    void visit( ViewParam param);
    void visit( ReferencedBean bean );
    void visit( RenderKit kit );
    void visit( FacesRenderer renderer );
    void visit( FacesClientBehaviorRenderer renderer );
    void visit( Lifecycle lifecycle );
    void visit( PhaseListener listener );
    void visit( FacesValidator validator );
    void visit ( FacesBehavior behavior );
    
    /**
     * Default shallow visitor.
     */
    public static class Default implements JSFConfigVisitor {
        public void visit(FacesConfig component) {
            visitChild();
        }
        public void visit(ManagedBean component) {
            visitChild();
        }
        public void visit(NavigationRule component) {
            visitChild();
        }
        public void visit(NavigationCase component) {
            visitChild();
        }
        public void visit(Converter component) {
            visitChild();
        }
        public void visit(Description component) {
            visitChild();
        }
        public void visit(DisplayName component) {
            visitChild();
        }
        public void visit(Icon component) {
            visitChild();
        }
        public void visit(ViewHandler component) {
            visitChild();
        }
        public void visit(Application component) {
            visitChild();
        }

        public void visit(LocaleConfig component) {
            visitChild();
        }

        public void visit(DefaultLocale component) {
            visitChild();
        }

        public void visit(SupportedLocale component) {
            visitChild();
        }
        
        public void visit(ResourceBundle component) {
            visitChild();
        }
        
        public void visit( ActionListener listener ) {
            visitChild();            
        }
        
        public void visit( DefaultRenderKitId id ) {
            visitChild();
        }
        
        public void visit( MessageBundle id ) {
            visitChild();
        }
        
        public void visit( NavigationHandler handler ){
            visitChild();
        }
        
        public void visit( PartialTraversal traversal ) {
            visitChild();
        }
        
        public void visit( StateManager manager ) {
            visitChild();
        }
        
        public void visit( ElResolver resolver ) {
            visitChild();
        }
        
        public void visit( PropertyResolver resolver ) {
            visitChild();
        }
        
        public void visit( VariableResolver resolver ) {
            visitChild();
        }
        
        public void visit( ResourceHandler handler ) {
            visitChild();
        }
        
        public void visit( FacesSystemEventListener listener ) {
            visitChild();
        }
        
        public void visit( DefaultValidators validators ) {
            visitChild();
        }
        
        public void visit( Ordering ordering ) {
            visitChild();
        }
        
        public void visit( After after ) {
            visitChild();
        }
        
        public void visit( Before before ) {
            visitChild();
        }
        
        public void visit( Name name ) {
            visitChild();
        }
        
        public void visit( Others others ) {
            visitChild();
        }
        
        public void visit( AbsoluteOrdering ordering ) {
            visitChild();
        }
        
        public void visit( Factory factory ) {
            visitChild();
        }
        
        public void visit( FacesValidatorId id ) {
            visitChild();
        }
        
        public void visit(ApplicationFactory factory ){
            visitChild();
        }
        
        public void visit( ExceptionHandlerFactory factory ){
            visitChild();
        }
        
        public void visit(ExternalContextFactory factory ){
            visitChild();
        }
        
        public void visit( FacesContextFactory factory){
            visitChild();
        }
        
        public void visit( PartialViewContextFactory factory ){
            visitChild();
        }
        
        public void visit( LifecycleFactory factory ){
            visitChild();
        }
        
        public  void visit( ViewDeclarationLanguageFactory factory ){
            visitChild();
        }
        
        public void visit( TagHandlerDelegateFactory factory ){
            visitChild();
        }
        
        public void visit( RenderKitFactory factory ){
            visitChild();
        }
        
        public void visit( VisitContextFactory factory ){
            visitChild();
        }
        
        public void visit( FacesComponent component ){
            visitChild();
        }
        
        public void visit( Facet facet ){
            visitChild();
        }
        
        public void visit( ConfigAttribute attribute ){
            visitChild();
        }
        
        public void visit( Property property ){
            visitChild();
        }
        
        public void visit( FacesManagedProperty property ){
            visitChild();
        }
        
        public void visit( ListEntries entries ){
            visitChild();
        }
        
        public void visit( MapEntries entries ){
            visitChild();
        }
        
        public void visit( If iff ){
            visitChild();
        }
        
        public void visit( Redirect redirect ){
            visitChild();
        }
        
        public void visit( ViewParam param ){
            visitChild();
        }
        
        public void visit( ReferencedBean bean ){
            visitChild();
        }
        
        public void visit( RenderKit kit ){
            visitChild();
        }
        
        public void visit( FacesRenderer render ){
            visitChild();
        }
        
        public void visit( FacesClientBehaviorRenderer render ){
            visitChild();
        }
        
        public void visit( Lifecycle lifecycle ){
            visitChild();
        }
        
        public void visit( PhaseListener listener ){
            visitChild();
        }
        
        public void visit( FacesValidator validator ){
            visitChild();
        }
        
        public void visit( FacesBehavior behavior ){
            visitChild();
        }
        
        protected void visitChild() {
        }
    }
    
    /**
     * Deep visitor.
     */
    public static class Deep extends Default {
        protected void visitChild(JSFConfigComponent component) {
            for (JSFConfigComponent child : component.getChildren()) {
                child.accept(this);
            }
        }
    }
    
}
