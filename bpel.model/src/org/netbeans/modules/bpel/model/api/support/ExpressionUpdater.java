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
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathModelTracerVisitor;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;
import org.netbeans.modules.xml.validation.core.Expression;

/**
 * @author ads
 * @author nk160297
 */
public final class ExpressionUpdater {
    
    public static class ExpressionException extends Exception {

        private static final long serialVersionUID = -6309073089869606561L;

        /**
         * {@inheritDoc}
         */
        public ExpressionException() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        public ExpressionException( String message ) {
            super(message);
        }

        /**
         * {@inheritDoc}
         */
        public ExpressionException( String message, Throwable cause ) {
            super(message, cause);
        }

        /**
         * {@inheritDoc}
         */
        public ExpressionException( Throwable cause ) {
            super(cause);
        }
        
    }
    
    public static class InvalidExpressionException extends ExpressionException {

        private static final long serialVersionUID = -461547631006192178L;

        /**
         * {@inheritDoc}
         */
        public InvalidExpressionException() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        public InvalidExpressionException( String message ) {
            super(message);
        }

        /**
         * {@inheritDoc}
         */
        public InvalidExpressionException( String message, Throwable cause ) {
            super(message, cause);
        }

        /**
         * {@inheritDoc}
         */
        public InvalidExpressionException( Throwable cause ) {
            super(cause);
        }
        
    }
    
    private ExpressionUpdater(){
        myFactories.add( new VariableReferenceFactory() );
        //myFactories.add( new PartReferenceFactory() );
    }
    
    public static ExpressionUpdater getInstance(){
        return INSTANCE;
    }

    /**
     * Method returns true if <code>component</code> is present in 
     * <code>expression</code>.
     * @param expression Subject expression.
     * @param component Component that will be trying to find.
     * @return true if component is found in expression.
     */
    public boolean isPresent(String expression, Named component) {
        if (expression == null || expression.length() == 0) {
            return false;
        }
        for (RefactoringReferenceFactory factory : myFactories) {
            if (factory.isApplicable(component)) {
                return factory.isPresent(expression, component);
            }
        }
        return false;
    }

    /**
     * Update entrance of <code>component</code> in <code>expression</code>
     * with new <code>component</code> name.
     * @param expression Subject expression.
     * @param component Component that will be trying to find for update.
     * @param newName New name of component.
     * @return Updated expression or null if it was not updated.
     */
    public String update(String expression, Named component, String newName) {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        for (RefactoringReferenceFactory factory : myFactories) {
            if (factory.isApplicable( component)) {
                return factory.update(expression, component, newName);
            }
        }
        return null;
    }

    private static ExpressionUpdater INSTANCE = new ExpressionUpdater(); 
    private Set<RefactoringReferenceFactory> myFactories = new HashSet<RefactoringReferenceFactory>();
}

interface RefactoringReferenceFactory {
    
    boolean isApplicable( Named component );
    boolean isPresent(String expression, Named component);
    String update( String expression, Named component, String newName );
}

class VariableReferenceFactory implements RefactoringReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.RefactoringReferenceFactory#isApplicable(org.netbeans.modules.xml.xam.Named)
     */
    public boolean isApplicable( Named component ) {
        return component instanceof Variable || component instanceof Named;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.RefactoringReferenceFactory#isPresent(java.lang.String, org.netbeans.modules.xml.xam.Named)
     */
    public boolean isPresent( String expression, Named component ) {
        XPathModel model = XPathModelHelper.getInstance().newXPathModel();

        if (expression == null || component.getName() == null) {
            return false;
        }
        try {
            XPathExpression exp = model.parseExpression(expression);
            Expression.FindVaribleVisitor visitor = new Expression.FindVaribleVisitor(component.getName()); 
            exp.accept(visitor);
            return visitor.isFound(); 
        }
        catch (XPathException e) {
            // in the case when we cannot parse expression we return false
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.RefactoringReferenceFactory#update(java.lang.String, org.netbeans.modules.xml.xam.Named, java.lang.String)
     */
    public String update( String expression, Named component, String newName ) {
        XPathModel model = XPathModelHelper.getInstance().newXPathModel();

        if (expression == null || component.getName() == null) {
            return null;
        }
        try {
            XPathExpression exp = model.parseExpression( expression );
            UpdateVaribleVisitor visitor = new UpdateVaribleVisitor( component.getName() , newName , exp ); 
            exp.accept(visitor);
            // FIX for # 80076
            String ret = visitor.getExpressionString();
            if ( expression.trim().equals( ret ) ){
                // we don't want to update not changed expression.
                return null;
            }
            return ret;
        }
        catch (XPathException e) {
            // in the case when we cannot parse expression we return false
            return null;
        }
    }
}

/**
 * It updates expression with new value for either just variable or part with 
 *  specified variable. 
 * @author ads
 *
 */
class UpdateVaribleVisitor extends XPathModelTracerVisitor {
    
    UpdateVaribleVisitor( String name , String newName , 
            XPathExpression expression )
    {
        myName = name;
        myNewName = newName;
        myMap = new IdentityHashMap<XPathExpression,XPathExpression>();
        myExpression = expression;
        myUpdater = new ExpressionUpdaterVisitor( myMap , expression );
    }
    
    UpdateVaribleVisitor( String varName , String partName, String newName , 
            XPathExpression expression ) 
    {
        this( varName , newName , expression );
        assert partName!= null;
        myPartName = partName;
    }
    
    public void visit(LocationStep locationStep) {
        super.visit(locationStep);
        updateExpression( locationStep );
    }

    public void visit(XPathCoreFunction coreFunction) {
        visitChildren( coreFunction );
        updateExpression( coreFunction );
    }

    public void visit(XPathCoreOperation coreOperation) {
        visitChildren( coreOperation );
        updateExpression( coreOperation );
    }

    public void visit(XPathExpressionPath expressionPath) {
        super.visit(expressionPath);
        updateExpression( expressionPath );
    }

    public void visit(XPathExtensionFunction extensionFunction) {
        visitChildren( extensionFunction );
        updateExpression( extensionFunction );
    }

    public void visit(XPathLocationPath locationPath) {
        super.visit(locationPath);
        updateExpression( locationPath );
    }
    
    @Override
    public void visit(XPathVariableReference variableRef ) {
        QName qName = variableRef.getVariableName();
        if ( qName != null   
                //&& BpelEntity.BUSINESS_PROCESS_NS_URI.equals( qName.getNamespaceURI()) TODO !
                )
        {
            String local = qName.getLocalPart();
            if ( local == null ){
                return;
            }
            int index = local.indexOf("."); 
            /*
             * trying to devide variable in two part : bpel variable name and part.
             * ( the part could be absent )
             */ 
            XPathModelFactory factory = myExpression.getModel().getFactory();
            if (index < 0) {
                if ( local.equals( myName ) && myPartName == null) {
                    XPathVariableReference newRef = 
                            factory.newXPathVariableReference(
                            new QName(myNewName));
                    myMap.put( variableRef , newRef );
                }
            }
            else {
                String varName = local.substring( 0, index );
                String part =(index<(local.length()-1)) ? 
                        local.substring( index+1 ):null;
                
                if ( !varName.equals( myName ) ){
                    return;
                }
                
                String newName;
                if ( myPartName != null) {
                    newName = myName+"."+myNewName;
                }
                else {
                    newName = myNewName+"."+part;
                }
                XPathVariableReference newRef = 
                        factory.newXPathVariableReference(
                        new QName(newName));
                myMap.put( variableRef , newRef );
            }
        }
        updateExpression( variableRef );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathStringLiteral)
     */
    public void visit( XPathStringLiteral stringLiteral ) {
        updateExpression( stringLiteral );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathNumericLiteral)
     */
    public void visit( XPathNumericLiteral numericLiteral ) {
        updateExpression( numericLiteral );         
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathPredicateExpression)
     */
    public void visit( XPathPredicateExpression predicateExpression ) {
        updateExpression( predicateExpression );         
    }
    
    // FIX for #IZ80076
    String getExpressionString() {
        if ( myExpression == null ) {
            return null;
        }
        return myExpression.getExpressionString();
    }
    
    // FIX for #IZ80076
    private void updateExpression( XPathExpression expression){
        expression.accept( myUpdater );
        myExpression = myUpdater.getExpression();
    }
    
    private String myName;
    private String myPartName;
    private String myNewName;
    private ExpressionUpdaterVisitor myUpdater;
    private Map<XPathExpression,XPathExpression> myMap;
    private XPathExpression myExpression;
}

/**
 * This is generic children updater.
 * It is constracted with Map that contains in key old expression, 
 * in value - new expression . This visitor will change 
 * old expression in parent to new expression and remove key from Map. 
 * 
 * @author ads
 *
 */
class ExpressionUpdaterVisitor implements XPathVisitor {
    
    ExpressionUpdaterVisitor( Map<XPathExpression,XPathExpression> oldNewMap ,
            XPathExpression expression )
    {
        myMap = oldNewMap;
        myExpression = expression;
    }
    
    public void visit(LocationStep locationStep) {
        updateExpression( locationStep );
        XPathPredicateExpression[] expressions = locationStep.getPredicates();
        boolean hasChanges = false;
        if ( expressions!= null ){
            int i = 0 ;
            for (XPathPredicateExpression expression : expressions) {
                XPathExpression expr = myMap.remove( expression );
                if ( expr!= null ){
                    hasChanges = true;
                    expressions[i]=(XPathPredicateExpression)expr;
                }
                i++;
            }
        }
        if ( hasChanges ){
            locationStep.setPredicates( expressions );
        }
    }
    
    public void visit(XPathCoreFunction coreFunction) {
        updateExpression( coreFunction );
        visitChildren( coreFunction );
    }
    
    public void visit(XPathCoreOperation coreOperation) {
        updateExpression( coreOperation );
        visitChildren( coreOperation );
    }
    
    public void visit(XPathExpressionPath expressionPath) {
        updateExpression( expressionPath );
        XPathExpression expression = expressionPath.getRootExpression();
        XPathExpression expr =  myMap.remove( expression );
        if ( expr!=null ){
            expressionPath.setRootExpression( expr );
        }

        boolean hasChanges = false;
        LocationStep[] steps = expressionPath.getSteps();
        if ( steps != null ){
            int i = 0 ;
            for (LocationStep step : steps) {
                expr = myMap.remove( step );
                if ( expr!= null ){
                    hasChanges = true;
                    steps[i]= (LocationStep)expr;
                }
                i++;
            }
        }
        if ( hasChanges ){
            expressionPath.setSteps( steps );
        }
    }

    public void visit(XPathExtensionFunction extensionFunction) {
        updateExpression( extensionFunction );
        visitChildren( extensionFunction );
    }

    public void visit(XPathLocationPath locationPath) {
        updateExpression( locationPath );
        LocationStep[] steps = locationPath.getSteps();
        boolean hasChanges = false;
        if ( steps != null ){
            int i = 0 ;
            for (LocationStep step : steps) {
                XPathExpression expr = myMap.remove( step );
                if ( expr!= null ){
                    hasChanges = true;
                    steps[i]= (LocationStep)expr;
                }
                i++;
            }
        }
        if ( hasChanges ){
            locationPath.setSteps( steps );
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathStringLiteral)
     */
    public void visit( XPathStringLiteral stringLiteral ) {
        /*XPathExpression expression = myMap.remove( stringLiteral );
        if  (expression != null ) {
            stringLiteral.setValue( ((XPathStringLiteral)expression).getValue() );
        }*/
        updateExpression( stringLiteral );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathNumericLiteral)
     */
    public void visit( XPathNumericLiteral numericLiteral ) {
        /*XPathExpression expression = myMap.remove( numericLiteral );
        if  (expression != null ) {
            numericLiteral.setValue( ((XPathNumericLiteral)expression).getValue() );
        }*/
        updateExpression( numericLiteral );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathVariableReference)
     */
    public void visit( XPathVariableReference variableReference ) {
        updateExpression(variableReference);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xpath.visitor.XPathVisitor#visit(org.netbeans.modules.xml.xpath.XPathPredicateExpression)
     */
    public void visit( XPathPredicateExpression predicateExpression ) {
        updateExpression( predicateExpression );
    }
    
    XPathExpression getExpression() {
        return myExpression;
    }
    
    protected void visitChildren(XPathOperationOrFuntion expr) {
         Collection<XPathExpression> children = expr.getChildren();
         Collection<XPathExpression> newChildren = null;
         boolean hasChanges = false;
         if(children != null) {
             newChildren = new ArrayList<XPathExpression>( children.size() );
             Iterator<XPathExpression> it = children.iterator();
             while(it.hasNext()) {
                 XPathExpression child = it.next();
                 XPathExpression expression = myMap.remove( child );
                 if ( expression!= null ){
                     hasChanges = true;
                     newChildren.add(expression); // Fix for IZ#80079
                 }
                 else {
                     newChildren.add( child );
                 }
             }
         }
         if ( hasChanges ){
             expr.clearChildren();
             for( XPathExpression expression : newChildren ){
                 expr.addChild( expression );
             }
         }
    }
    
    private void updateExpression( XPathExpression expression ) {
        XPathExpression updated = myMap.get( expression );
        /*
         *  only when ALL expression should be changed ( because
         *  it doesn't have mutation method ) we replace
         *  this expression to new from map. This could 
         *  be applicable ONLY for expression that 
         *  is oroginal expression from which we start.
         *  Any subexpression will be handled as child in
         *  appropriate container.
         *  This is FIX for #IZ80076
         */ 
        if ( updated != null && myExpression == expression ) {
            myExpression = updated;
        }
    }
    
    private Map<XPathExpression,XPathExpression> myMap = 
        new IdentityHashMap<XPathExpression,XPathExpression>();
    
    // this need when expression itself needs to be changed.
    private XPathExpression myExpression;
}
