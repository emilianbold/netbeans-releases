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

import java.util.Hashtable;
import java.util.Vector;

import org.netbeans.microedition.svg.input.InputHandler;
import org.netbeans.microedition.svg.meta.ChildrenAcceptor;
import org.netbeans.microedition.svg.meta.MetaData;
import org.netbeans.microedition.svg.meta.ChildrenAcceptor.Visitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public abstract class SVGComponent implements SVGForm.FocusListener {
    public static final    String SVG_NS = "http://www.w3.org/2000/svg";  // NOI18N
    
    public static final    String LABEL_FOR        = "labelFor";          // NOI18N
    
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
    
    private Hashtable myProperties;
    
    protected final SVGForm             form;
    protected final SVGLocatableElement wrapperElement;
    protected       Vector              actionListeners;

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
    
    protected static final SVGElement getElementById( SVGElement parent, String childId) {
        Element elem = parent.getFirstElementChild();
        while( elem != null && elem instanceof SVGElement) {
            SVGElement svgElem = (SVGElement) elem;
            if ( childId.equals( svgElem.getId())) {
                return svgElem;
            } else {
                SVGElement result = getElementById(svgElem, childId);
                if ( result != null) {
                    return result;
                }
            }
            elem = svgElem.getNextElementSibling();
        }
        
        return null;
    }   
    
    protected static final SVGElement getElementByMeta( SVGElement parent , 
            String key, String value )
    {
        MetaFinder finder = new MetaFinder( key , value );
        ChildrenAcceptor acceptor = new ChildrenAcceptor( finder );
        acceptor.accept(parent);
        return finder.getFound();
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
                return false;
            }
            if (myValue == null ){
                return true;
            }
            if ( myValue.equals( myMeta.get(myKey))){
                myFound = (SVGElement)element;
                return false;
            }
            return true;
        }
        
        SVGElement getFound(){
            return myFound;
        }
        
        private String myKey;
        private String myValue;
        private MetaData myMeta;
        private SVGElement myFound;
    }
}
