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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.impl;

import org.netbeans.modules.editor.*;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.editor.WeakEventListenerList;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *  Editor Customizable Side Bar.
 *  Contains components for particular MIME type as defined in XML layer.
 *
 *  @author  Martin Roskanin
 */
public final class CustomizableSideBar extends JPanel {
    
    private static HashMap components = new HashMap(5);
    private static HashMap lookupResults = new HashMap(5);
    private static HashMap lookupListeners = new HashMap(5); //<kitClass, WeakListener>    
    private static HashMap changeListeners = new HashMap(5); //<kitClass, WeakEventListenerList>
    
    /** List of the registered changes listeners */
    //private static final WeakEventListenerList listenerList
//	= new WeakEventListenerList();
    
    
    private CustomizableSideBar(List/*<JComponent>*/ components, SideBarPosition position){
        BoxLayout bl = new javax.swing.BoxLayout(this, position.getAxis());
        setLayout(bl);
        
        for (Iterator i = components.iterator(); i.hasNext(); ){
            add((JComponent) i.next());
        }
    }

    
    private static WeakEventListenerList getListenerList(String mimeType){
        WeakEventListenerList listenerList;
        synchronized (changeListeners){
            listenerList = (WeakEventListenerList)changeListeners.get(mimeType);
        }
        return listenerList;
    }
    
    /** Add weak listener to listen to change of activity of documents or components.
     * The caller must
     * hold the listener object in some instance variable to prevent it
     * from being garbage collected.
     * @param l listener to add
     */
    public static void addChangeListener(String mimeType, ChangeListener l) {
        WeakEventListenerList listenerList;
        synchronized (changeListeners){
            listenerList = (WeakEventListenerList)changeListeners.get(mimeType);
            if (listenerList == null){
                listenerList = new WeakEventListenerList();
            }
            changeListeners.put(mimeType, listenerList);
        }
        
        listenerList.add(ChangeListener.class, l);
    }

    /** Remove listener for changes in activity. It's optional
     * to remove the listener. It would be done automatically
     * if the object holding the listener would be garbage collected.
     * @param l listener to remove
     */
    public static void removeChangeListener(String mimeType, ChangeListener l) {
        WeakEventListenerList listenerList = getListenerList(mimeType);
        if (listenerList == null){
            return;
        }
        listenerList.remove(ChangeListener.class, l);
    }

    private static void fireChange(String mimeType) {
        WeakEventListenerList listenerList = getListenerList(mimeType);
        if (listenerList == null){
            return;
        }
	ChangeListener[] listeners
	    = (ChangeListener[])listenerList.getListeners(ChangeListener.class);
	ChangeEvent evt = new ChangeEvent(CustomizableSideBar.class);
	for (int i = 0; i < listeners.length; i++) {
	    listeners[i].stateChanged(evt);
	}
    }
    
    
    public static Map/*<SideBarPosition, JComponent>*/ createSideBars(JTextComponent target) {
        Map/*<SideBarPosition, List<JComponent>>*/ components = getPanelComponents(target);
        Map/*<SideBarPosition, JComponent>*/ result = new HashMap();
        
        for (Iterator entries = components.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            SideBarPosition position = (SideBarPosition) entry.getKey();
            
            result.put(position, new CustomizableSideBar((List) entry.getValue(), position));
        }
        
        return result;
    }
        
    private static Map/*<SideBarPosition, List>*/ getInstanceCookiesPerKitClass(String mimeType){
        Map result = new HashMap();
        if (mimeType == null) return result;
        synchronized (components){
            if (components.containsKey(mimeType)){
                return (Map)components.get(mimeType);
            }else{
                Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
                
                Lookup.Result lookupResult;
                synchronized (lookupResults){
                    lookupResult = (Lookup.Result)lookupResults.get(mimeType);
                    if (lookupResult == null){
                        lookupResult = lookup.lookup(new Lookup.Template(SideBarFactoryProvider.class));
                        lookupResults.put(mimeType, lookupResult);
                    }
                }
                    
                Collection instances = lookupResult.allInstances();
                if (instances.isEmpty()){
                    return result; //empty
                }
                
                SideBarFactoryProvider provider =  (SideBarFactoryProvider) instances.iterator().next();

                synchronized (lookupListeners){
                    LookupListener lookupListener = (LookupListener) lookupListeners.get(mimeType);
                    if (lookupListener == null){
                        lookupListener = new MyLookupListener(mimeType);
                        LookupListener weakListener = (LookupListener) WeakListeners.create(
                                               LookupListener.class, lookupListener, lookupResult);
                        lookupResult.addLookupListener(weakListener);
                        lookupListeners.put(mimeType, lookupListener);
                    }
                }
                
                if (provider!=null){
                    result = provider.getProviders();
                }
                
                if (result!=null){
                    components.put(mimeType, result);
                }
                
                return result;
            }
        }
    }
    
    private static Map/*<SideBarPosition, List<JComponent>>*/ getPanelComponents(JTextComponent target){
        Map result = new HashMap();
        Map icMap = getInstanceCookiesPerKitClass(NbEditorUtilities.getMimeType(target));

        try{
            for (Iterator entries = icMap.entrySet().iterator(); entries.hasNext(); ) {
                Map.Entry entry = (Map.Entry) entries.next();
                List icList = (List) entry.getValue();
                List retList = new ArrayList();
                
                for (int i = 0; i<icList.size(); i++){
                    InstanceCookie ic = (InstanceCookie)icList.get(i);
                    Object obj = ic.instanceCreate();
                    JComponent sideBarObj = ((SideBarFactory)obj).createSideBar(target);
                    if (sideBarObj!=null){
                        retList.add(sideBarObj);
                    }
                }
                result.put(entry.getKey(), retList);
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
        
        return result;
    }
    
    public static final class SideBarPosition {
        public static final int WEST  = 1;
        public static final int NORTH = 2;
        public static final int SOUTH = 3;
        public static final int EAST  = 4;
        
        public static final String WEST_NAME   = "West"; // NOI18N
        public static final String NORTH_NAME  = "North"; // NOI18N
        public static final String SOUTH_NAME  = "South"; // NOI18N
        public static final String EAST_NAME   = "East"; // NOI18N
        
        private int position;
        private boolean scrollable;
        
        SideBarPosition(FileObject fo) {
            Object position = fo.getAttribute("position"); // NOI18N
            
            if (position != null && position instanceof String) {
                String positionName = (String) position;
                
                if (WEST_NAME.equals(positionName)) {
                    this.position = WEST;
                } else {
                    if (NORTH_NAME.equals(positionName)) {
                        this.position = NORTH;
                    } else {
                        if (SOUTH_NAME.equals(positionName)) {
                            this.position = SOUTH;
                        } else {
                            if (EAST_NAME.equals(positionName)) {
                                this.position = EAST;
                            } else {
                                if (ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL))
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unsupported position: " + positionName);
                                
                                this.position = WEST;
                            }
                        }
                    }
                }
            } else {
                this.position = WEST;
            }
            
            Object scrollable = fo.getAttribute("scrollable"); // NOI18N
            
            if (scrollable != null && scrollable instanceof Boolean) {
                this.scrollable = ((Boolean) scrollable).booleanValue();
            } else {
                this.scrollable = true;
            }
            
            if (this.scrollable && (this.position == SOUTH || this.position == EAST)) {
                if (ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL))
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unsupported combination: scrollable == true, position=" + getBorderLayoutPosition());
            }
        }
        
        public int hashCode() {
            return scrollable ? position : - position;
        }
        
        public boolean equals(Object o) {
            if (o instanceof SideBarPosition) {
                SideBarPosition p = (SideBarPosition) o;
                
                if (scrollable != p.scrollable)
                    return false;
                
                if (position != p.position)
                    return false;
                
                return true;
            }
            
            return false;
        }
        
        public int getPosition() {
            return position;
        }
        
        private static String[] borderLayoutConstants = new String[] {"", BorderLayout.WEST, BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST};
        
        public String getBorderLayoutPosition() {
            return borderLayoutConstants[getPosition()];
        }
        
        private static int[] axisConstants = new int[] {-1, BoxLayout.X_AXIS, BoxLayout.Y_AXIS, BoxLayout.Y_AXIS, BoxLayout.X_AXIS};
        
        private int getAxis() {
            return axisConstants[getPosition()];
        }
        
        public boolean isScrollable() {
            return scrollable;
        }
        
        public String toString() {
            return "[SideBarPosition: scrollable=" + scrollable + ", position=" + position + "]"; // NOI18N
        }
    }

    public static class SideBarFactoryProvider implements InstanceProvider{

        List ordered;

        public SideBarFactoryProvider(){
        }

        public SideBarFactoryProvider(List ordered){
            this.ordered = ordered;
        }

        public Map getProviders(){
            Map result = new HashMap();
            for (int i = 0; i<ordered.size(); i++){
                FileObject fo = (FileObject) ordered.get(i);
                
                DataObject dob;
                try {
                    dob = DataObject.find(fo);
                } catch (DataObjectNotFoundException dnfe) {
                    // ignore
                    continue;
                }
                
                InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                if (ic!=null){
                        try{
                            if (SideBarFactory.class.isAssignableFrom(ic.instanceClass() )){
                                SideBarPosition position = new SideBarPosition(dob.getPrimaryFile());
                                
                                List retList = (List) result.get(position);
                                
                                if (retList == null) {
                                    result.put(position, retList = new ArrayList());
                                }
                                
                                retList.add(ic);
                            }
                        }catch(IOException ioe){
                            ioe.printStackTrace();
                        }catch(ClassNotFoundException cnfe){
                            cnfe.printStackTrace();
                        }
                }
            }
            return result;
        }

        public Object createInstance(List ordered) {
            return new SideBarFactoryProvider(ordered);
        }
    }    
    
    private static class MyLookupListener implements LookupListener{
        private String mimeType;
        public MyLookupListener(String mimeType){
            this.mimeType = mimeType;
        }
        
        public void resultChanged(LookupEvent ev) {
            synchronized (components){
                components.remove(mimeType);
            }
            fireChange(mimeType);
        }
    }

}
