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

package org.netbeans.modules.css.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Represents the content of a Css rule.
 *
 * @author Winston Prakash
 * @author Marek Fukala
 *
 * @version 2.0
 */
public class CssRuleContent {
    
    private static final Logger LOGGER = Logger.getLogger(org.netbeans.modules.css.Utilities.VISUAL_EDITOR_LOGGER);
    
    //TODO move this to the UI package
    public final static String NOT_SET = NbBundle.getMessage(CssRuleContent.class, "NOT_SET"); //NOI18N
    public final static String VALUE = NbBundle.getMessage(CssRuleContent.class, "VALUE"); //NOI18N
    
    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    //debug
    private HashMap<PropertyChangeListener, Exception> listenersCreators = new HashMap<PropertyChangeListener, Exception>();
    
    private List<CssRuleItem> items;
    
    private boolean immutable;
    
    CssRuleContent(List<CssRuleItem> items, boolean immutable) {
        this.items = items;
        this.immutable = immutable;
    }
    
    /** @return a list of Css rule items. */
    public List<CssRuleItem> ruleItems() {
        return items;
    }
    
    /**
     * Get the value of specified property from the rule items.
     *
     * @return Value of the specified property.
     */
    public String getProperty(String property) {
        CssRuleItem item = findItem(property);
        if(item != null) {
            return item.value().name();
        } else {
            return  null;
        }
    }
    
    //TODO remove this from the API
    public void modifyProperty(String property, String newValue) {
        CssRuleItem item = findItem(property);
        newValue = newValue.trim();
        if(item == null && newValue.length() == 0) {
            return ; //TODO: marek - should be fixed in the UI so it doesn't fire such stupid events
        }
        if (item != null && newValue.length() == 0) {
            //property remove
            if(!immutable) {
                items.remove(item);
            }
            firePropertyChange(item, null); //NOI18N
        } else {
            String oldVal = item == null ? null : item.value().name();
            //do not fire events when the old and new values are the same
            if(oldVal == null || !newValue.equals(oldVal)) {
                //property add or modify
                CssRuleItem newRuleItem = new CssRuleItem(property, -1, newValue, -1);
                if (!immutable) {
                    if (item == null) {
                        items.add(newRuleItem);
                    } else {
                        item.key = new CssRuleItem.Item(property, -1);
                        item.value = new CssRuleItem.Item(newValue, -1);
                    }
                }
                firePropertyChange(item, newRuleItem); //NOI18N
            }
        }
    }
    
    /** Returns a formated string with the rule items in the form key: value;
     *
     * @return formatted string representation of the rule items
     */
    public String getFormattedString(){
        StringWriter strWriter = new StringWriter();
        strWriter.write("\n"); //NOI18N
        
        for(CssRuleItem item : items) {
            String property = item.key().name();
            String propertyValue = item.value().name().trim();
            if(!(propertyValue.equals(NOT_SET) || propertyValue.equals(""))){
                strWriter.write("   " + property);
                strWriter.write(": ");
                strWriter.write(propertyValue);
                strWriter.write("; "); //NOI18N
            }
        }
        return strWriter.toString();
    }
    
    @Override
    public String toString(){
        return getFormattedString();
    }
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        //debug code >>>
        if(!listeners.isEmpty()) {
            Exception e = new IllegalStateException("Trying to add second listener to CssRuleContent.");
            LOGGER.throwing(CssRuleContent.class.getName(), "addPropertyChangeListener", e);
            LOGGER.log(Level.FINE, "Stacktraces of the previous listeners creators:");
            for(Exception ex : listenersCreators.values()) {
                LOGGER.throwing(CssRuleContent.class.getName(), "addPropertyChangeListener", ex);
            }
        }
        listenersCreators.put(listener, new Exception());
        //<<<<
        
        listeners.add(listener);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
        
        //debug
        listenersCreators.remove(listener);
    }
    
    private synchronized void firePropertyChange(CssRuleItem oldVal, CssRuleItem newVal) {
        List<PropertyChangeListener> copy = new ArrayList<PropertyChangeListener>(listeners);
        for(PropertyChangeListener l : copy) {
            l.propertyChange(new PropertyChangeEvent(this, "property", oldVal, newVal));
        }
    }
    
    private CssRuleItem findItem(String keyName) {
        for(CssRuleItem ri : items) {
            if(ri.key().name().equals(keyName)) {
                return ri;
            }
        }
        return null;
    }
    
}
