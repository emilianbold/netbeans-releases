/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.customizer;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.util.WeakListener;

import org.netbeans.tax.TreeObject;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class AbstractTreeCustomizer extends JPanel implements Customizer, PropertyChangeListener {

    /** Serial Version UID */
    private static final long serialVersionUID =7141277140374364170L;    
    
    /** */
    private static final String TEXT_DEFAULT = "<DEFAULT>"; // NOI18N
    
    /** */
    public static final String MIME_XML = "text/xml"; // NOI18N

    /** */
    public static final String MIME_DTD = "text/x-dtd"; // NOI18N

    /** */
    public static final String MIME_TXT = "text/plain"; // NOI18N


    /** Used to disable propertu changes etc. during initilizing. */
    protected boolean initializing;
    
    /** */
    private TreeObject treeObject;

    /** Does this registered itself listeners as TreeNode? */
    private boolean treeListening = false;


    //
    // init
    //

    /** We call virtual method from constructor. Use initializing to check stage. */
    public AbstractTreeCustomizer () {
	super();

        treeObject   = null;
        initializing = false;
    }


    //
    // from Customizer
    //
    
    /** Set the object to be customized.
     * @param bean The object to be customized.
     */    
    public final void setObject (Object bean) throws IllegalArgumentException {
        try {
            initializing = true;

            if (! (bean instanceof TreeObject))
                throw new IllegalArgumentException (bean + Util.getString ("PROP__invalid_instance"));   //!!!

            treeObject = (TreeObject)bean;

	    ownInitComponents();

            initValues();
        } finally {
            initializing = false;
        }
    }
    

    //
    // itself
    //

    /**
     */
    protected final TreeObject getTreeObject () {
        return treeObject;
    }
    
    /**
     */
    private final void initValues () {
        initComponentValues();
        updateReadOnlyStatus();
        initListeners();
    }

    /**
     */
    abstract protected void initComponentValues ();
    
    /**
     */
    protected void ownInitComponents () {
    }
    
    /**
     */
    private void updateReadOnlyStatus () {
        updateReadOnlyStatus (!!! getTreeObject().isReadOnly());
    }
    
    /**
     */
    abstract protected void updateReadOnlyStatus (boolean editable);
    

    //
    // events
    //

    /**
     */
    private void initListeners () {
        if (!treeListening) {
            treeObject.addPropertyChangeListener (WeakListener.propertyChange (this, treeObject));        
            treeListening = true;
        }
    }
    
    /** 
     * It will be called from AWT thread and it will never be caller during init stage.
     */
    protected void safePropertyChange (PropertyChangeEvent pche) {
	if (pche.getPropertyName().equals (TreeObject.PROP_READ_ONLY)) {
            updateReadOnlyStatus();
	}
    }

    /** 
     * Filter out notifications during selfinitialization stage and 
     * pass others in AWT thread.
     */
    public final void propertyChange (final PropertyChangeEvent e) {
        if (initializing)
            return;
            
        if (SwingUtilities.isEventDispatchThread()) {
            safePropertyChange(e);
        } else {
	    SwingUtilities.invokeLater (new Runnable() {
	        public void run() {
		    AbstractTreeCustomizer.this.safePropertyChange (e);
		}
            });
        }
    }

    
    //
    // Utils
    //

    protected static String text2null (String text) {
	if ( text.equals (TEXT_DEFAULT) )
	    return null;
	if ( text.length() == 0 )
	    return null;
	if ( text.trim().length() == 0 )
	    return null;
	return text;
    }

    protected static String null2text (String maybeNull) {
	if ( maybeNull == null )
	    return TEXT_DEFAULT;
	return maybeNull;
    }

    protected static boolean applyKeyPressed (KeyEvent evt) {
	return (evt.isControlDown() && (evt.getKeyCode() == KeyEvent.VK_ENTER));
    }

}
