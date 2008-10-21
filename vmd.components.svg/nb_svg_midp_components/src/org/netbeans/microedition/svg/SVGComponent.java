/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */ 

/*
 * SVGComponent.java
 * 
 * Created on Oct 4, 2007, 1:35:53 PM
 */

package org.netbeans.microedition.svg;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.input.PointerListener;
import org.netbeans.microedition.svg.meta.ChildrenAcceptor;
import org.netbeans.microedition.svg.meta.MetaData;
import org.netbeans.microedition.svg.meta.ChildrenAcceptor.Visitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public abstract class SVGComponent implements SVGForm.FocusListener {
    public static final    String SVG_NS = "http://www.w3.org/2000/svg";  // NOI18N
    
    public static final    String LABEL_FOR        = "labelFor";          // NOI18N
    public static final    String ENABLED          = "enabled";           // NOI18N
    
    protected static final String TRAIT_X          = "x";                 // NOI18N
    protected static final String TRAIT_Y          = "y";                 // NOI18N
    protected static final String TRAIT_VISIBILITY = "visibility";        // NOI18N
    protected static final String TRAIT_FILL       = "fill";              // NOI18N
    protected static final String TRAIT_TEXT       = "#text";             // NOI18N
    
    protected static final String TYPE             = "type";              // NOI18N
    protected static final String REF              = "ref";               // NOI18N
    
    protected static final String TR_VALUE_VISIBLE = "visible";           // NOI18N  
    protected static final String TR_VALUE_HIDDEN  = "hidden";            // NOI18N
    protected static final String TR_VALUE_INHERIT = "inherit";           // NOI18N
    
    protected static final String DASH             = "_";                 // NOI18N
    
    private Hashtable myProperties;
    
    protected final SVGForm             form;
    protected final SVGLocatableElement wrapperElement;
    protected       Vector              actionListeners;
    private         Vector              myPointerListeners;

    private boolean isFocusable      = true;

    public SVGComponent( SVGForm form, SVGLocatableElement element ) {
        this.form = form;
        wrapperElement = element;
    }
    
    public SVGComponent( SVGForm form, String elemId) {
        this.form = form;
        Document doc = form.getDocument();
        wrapperElement = (SVGLocatableElement) doc.getElementById(elemId);
    }
    
    public SVGLocatableElement getElement() {
        return wrapperElement;
    }
    
    public SVGForm getForm(){
        return form;
    }
    
    public void requestFocus() {
        form.requestFocus(this);
    }

    public void focusGained() {
    }

    public void focusLost() {
    }   
    
    public InputHandler getInputHandler() {
        return null;
    }

    public synchronized boolean isFocusable(){
        return isFocusable;
    }
    
    public synchronized void setFocusable( boolean focusable ){
        isFocusable = focusable;
    }
    
    public SVGRectangle getBounds(){
        SVGLocatableElement element = getElement();
        if ( element == null ){
            return null;
        }
        SVGRect rect = element.getScreenBBox();
        if ( rect == null ){
            return null;
        }
        return new SVGRectangle( rect );
    }
    
    public synchronized void addPointerListener( PointerListener listener){
        if ( myPointerListeners == null ){
            myPointerListeners = new Vector(1);
        }
        myPointerListeners.addElement( listener );
    }
    
    public synchronized void removePointerListener( PointerListener listener){
        if ( myPointerListeners != null ){
            myPointerListeners.removeElement( listener );
            if ( myPointerListeners.isEmpty() ){
                myPointerListeners = null;
            }
        }
    }
    
    public synchronized PointerListener[] getPointerListeners(){
        if ( myPointerListeners == null ){
            return new PointerListener[0];
        }
        else {
            PointerListener[] result = new PointerListener[ myPointerListeners.size()];
            Enumeration en = myPointerListeners.elements();
            int i=0;
            while ( en.hasMoreElements() ){
                result[ i ] = (PointerListener)en.nextElement();
                i++;
            }
            return result;
        }
    }
    
    public synchronized void addActionListener(SVGActionListener listener) {
        if ( actionListeners == null) {
            actionListeners = new Vector(1);
        }
        actionListeners.addElement(listener);
    }
    
    public synchronized void removeActionListener(SVGActionListener listener) {
        if (actionListeners != null) {
            actionListeners.removeElement(listener);
            if ( actionListeners.isEmpty()) {
                actionListeners = null;
            }
        }
    }
    
    protected Object getProperty( Object key ){
        if ( myProperties == null ){
            return null;
        }
        return myProperties.get( key );
    }
    
    protected void setProperty( Object key , Object value ){
        if ( myProperties == null ){
            myProperties = new Hashtable();
        }
        myProperties.put(key, value);
    }
    
    protected synchronized void fireActionPerformed() {
        if (actionListeners != null) {
            int listenersNum = actionListeners.size();
            for (int i = 0; i < listenersNum; i++) {
                ((SVGActionListener) actionListeners.elementAt(i)).actionPerformed(this);
            }
        }
    }
    
    protected SVGLabel getLabel(){
        return getForm().getLabelFor( this );
    }
    
    protected void setTraitSafely( final SVGElement element , final String trait, 
            final String value )
    {
        getForm().invokeAndWaitSafely(new Runnable() {
            public void run() {
                element.setTrait(trait, value);
            }
        });
    }
    
    protected void setTraitSafely( final SVGElement element , final String trait, 
            final float value )
    {
        getForm().invokeAndWaitSafely(new Runnable() {
            public void run() {
                element.setFloatTrait(trait, value);
            }
        });
    }
    
    protected final SVGElement getElementById( final SVGElement parent,
            final String childId )
    {
        final Vector ret = new Vector(1);
        final Document doc = getForm().getDocument();
        Runnable runnable = new Runnable() {

            public void run() {
                IdFinder finder = new IdFinder( childId );
                ChildrenAcceptor acceptor = new ChildrenAcceptor(finder);
                acceptor.accept(parent);
                SVGElement result = finder.getFound();
                if ( result == null ){
                    result = (SVGElement)doc.getElementById( childId );
                }
                ret.addElement( result );
            }
        };
        getForm().invokeAndWaitSafely(runnable);
        return (SVGElement) ret.elementAt(0);
    }   
    
    protected final SVGElement getElementByMeta( final SVGElement parent , 
            final String key, final String value , 
            boolean runInsideDocumentUpdateThread )
    {
        final Vector ret = new Vector(1);
        Runnable runnable = new Runnable() {
            public void run() {
                MetaFinder finder = new MetaFinder(key, value);
                ChildrenAcceptor acceptor = new ChildrenAcceptor(finder);
                acceptor.accept(parent);
                ret.addElement(finder.getFound());
            }
        };
        if ( runInsideDocumentUpdateThread ){
            runnable.run();
        }
        else {
            getForm().invokeAndWaitSafely(runnable);
        }
        return (SVGElement)ret.elementAt( 0 );
    }
    
    protected final SVGElement getElementByMeta( final SVGElement parent , 
            final String key, final String value )
    {
        return getElementByMeta(parent, key, value , false );
    }
    
    protected final SVGElement getNestedElementByMeta( final SVGElement parent , 
            final String key, final String value )
    {
        final Vector ret = new Vector(1);
        Runnable runnable = new Runnable() {
            public void run() {
                    MetaFinder finder = new MetaFinder( key , value );
                    ChildrenAcceptor acceptor = new ChildrenAcceptor( finder );
                    acceptor.accept(parent);
                    ret.addElement( finder.getNestedElement() );
            }
        };
        getForm().invokeAndWaitSafely(runnable);
        return (SVGElement)ret.elementAt( 0 );
    }
    
    private static class MetaFinder implements Visitor {
        
        MetaFinder( String key , String value ){
            myKey = key;
            myValue = value;
            myMeta = new MetaData();
        }

        public boolean visit( Element element ) {
            if ( !( element instanceof SVGElement )){
                return true;
            }
            myMeta.loadFromElement((SVGElement)element);
            if ( myValue== null && myMeta.get( myKey )==null){
                myFound = (SVGElement)element;
                myNested = myMeta.getNestedElement();
                return false;
            }
            if (myValue == null ){
                return true;
            }
            if ( myValue.equals( myMeta.get(myKey))){
                myFound = (SVGElement)element;
                myNested = myMeta.getNestedElement();
                return false;
            }
            return true;
        }
        
        SVGElement getFound(){
            return myFound;
        }
        
        SVGElement getNestedElement(){
            return myNested;
        }
        
        private String myKey;
        private String myValue;
        private MetaData myMeta;
        
        private SVGElement myFound;
        private SVGElement myNested;
    }
    
    private static class IdFinder implements Visitor {
        
        IdFinder( String id  ){
            myId = id;
        }

        public boolean visit( Element element ) {
            if ( !( element instanceof SVGElement )){
                return true;
            }
            String id = ((SVGElement)element).getId();
            if ( myId.equals( id ) ){
                myFound = (SVGElement)element;
                return false;
            }
            return true;
        }
        
        SVGElement getFound(){
            return myFound;
        }
        
        private String myId;
        private SVGElement myFound;
    }
}
