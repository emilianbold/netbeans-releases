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

package org.netbeans.modules.soa.mapper.basicmapper.util;


import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;

import javax.swing.JSplitPane;

import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;

/**
 * <p>
 *
 * Title: MapperUtilities </p> <p>
 *
 * Description: Provide common functions for all the mapper classes. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 */
public class MapperUtilities {
    /**
     * The jvm local object data flavor.
     */
    private static DataFlavor mLocalObjectDataFlavors[] = new DataFlavor[1];

    static {
        try {
            mLocalObjectDataFlavors[0] =
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            System.err.println(
                "This should not happen! "
                + "Can't found DataFlavor.javaJVMLocalObjectMimeType.");
            e.printStackTrace(System.err);
        }
    }


    /**
     * No class should instaniate this class.
     */
    private MapperUtilities() { }


    /**
     * Return the JVM local object data flavor. The data flavor is primary for
     * default drag and drop data flavor of the mapper. This data flavor is
     * loaded at the time class is loaded. It is null if
     * DataFlavor.javaJVMLocalObjectMineType cannot be found during creation of
     * this data flavor.
     *
     * @return   the JVM local object data flavor
     */
    public static DataFlavor getJVMLocalObjectDataFlavor() {
        return mLocalObjectDataFlavors[0];
    }

    /**
     * Return the JGoSelection DataFlavor for drag and drop JGoObject operations.
     * JGo uses JGoSelection as its default transfable object in dnd.
     *
     * @return   the JGoSelection DataFlavor for drag and drop JGoObject operations.
     */
    public static DataFlavor getJGoSelectionDataFlavor() {
        return com.nwoods.jgo.JGoDocument.getStandardDataFlavor();
    }


    /**
     * Return a new mapper event by specifying the properties of the event.
     *
     * @param source        the source of the event.
     * @param transferData  the data object to be transfer.
     * @param eventType     the type of the event.
     * @param eventDesc     the description of the event.
     * @return              a new mapper event by specifying the properties of
     *      the event.
     */
    public static IMapperEvent getMapperEvent(
        final Object source,
        final Object transferData,
        final String eventType,
        final String eventDesc) {
        return
            new IMapperEvent() {

                public String getDesc() {
                    return eventDesc;
                }


                public String getEventType() {
                    return eventType;
                }


                public Object getSource() {
                    return source;
                }


                public Object getTransferObject() {
                    return transferData;
                }


                public String toString() {
                    return eventDesc;
                }
            };
    }


    /**
     * Add a divider location property listener to explicitly set the divider
     * location with the specified proportion, for the specified JSplitPane, as
     * the first time the divider location moves.
     *
     * @param spliter     the JSplitPane to be set.
     * @param proportion  the proportion to change the divider location
     */
    public static void addDividerLocationInitializer(
        JSplitPane spliter,
        double proportion) {
        new DividerLocationListener(spliter, proportion);
    }


    /**
     * A generic method to fire a property change event for the specified
     * listerns.
     *
     * @param listeners     the listeners that registers
     * @param source        the source of the property change event
     * @param propertyName  the property name that change of the property change
     *      event
     * @param newValue      the new value object of the property change event
     * @param oldValue      the old value object of the property change event
     */
    public static void firePropertyChanged(
        PropertyChangeListener listeners[],
        Object source,
        String propertyName,
        Object newValue,
        Object oldValue) {
        if ((listeners == null) || (listeners.length == 0)) {
            return;
        }

        PropertyChangeEvent event =
            new PropertyChangeEvent(source, propertyName, oldValue, newValue);

        int i = listeners.length - 1;

        for (; i >= 0; i--) {
            listeners[i].propertyChange(event);
        }
    }


    /**
     * This class listens on the frist divider location change and explicitly
     * set the divider location with the specified proportion, for a specified
     * JSplitPane.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    public static class DividerLocationListener
         implements PropertyChangeListener {
        /**
         * DOCUMENT ME!
         */
        private double mProportion = 0.5d;

        /**
         * DOCUMENT ME!
         */
        private JSplitPane mSpliter;


        /**
         * Constructor a DividerLocationListener with a specified JSplitPane to
         * listen on and a specified proportion to change the divider location.
         * If the proportion is not between 0.0 and 1.0, an
         * IllegalArgumentException will be threw.
         *
         * @param spliter     the JSplitPane this listener listens on.
         * @param proportion  the proportion to change the divider location.
         */
        public DividerLocationListener(
            JSplitPane spliter,
            double proportion) {
            if ((proportion < 0.0d) || (proportion > 1.0d)) {
                throw new java.lang.IllegalArgumentException(
                    "proportion is not between 0.0 and 1.0: " + proportion);
            }

            mProportion = proportion;
            mSpliter = spliter;

            mSpliter.addPropertyChangeListener(
                JSplitPane.DIVIDER_LOCATION_PROPERTY,
                this);
        }


        /**
         * Invoke when the divider location change on the specified JSplitPane,
         * to explicitly set the divider location to a specified proportion.
         * This method first remove itself from the JSplitPane. It calculates
         * the divider location base on the specified proportion and the split
         * style of the JSplitPane (Horizontal or Vertical).
         *
         * @param e  the PropertyChangeEvent event
         */
        public void propertyChange(PropertyChangeEvent e) {
            mSpliter.removePropertyChangeListener(
                JSplitPane.DIVIDER_LOCATION_PROPERTY,
                this);

            int length = 0;

            if (mSpliter.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                length = mSpliter.getHeight();
            } else {
                length = mSpliter.getWidth();
            }

            mSpliter.setDividerLocation((int) (length * mProportion));
        }
    }
    
    public static boolean isLinkAlreadyConnected(IMapperLink link, IMapperNode node) {
        // Determines if the link is fully connected (i.e. both start and end
        // nodes are set). This means that the link is already connected for any
        // party interested in knowing such a thing.
        List links = node.getLinks();
        if (link.getStartNode() != null) {
            for (Iterator iter=links.iterator(); iter.hasNext();) {
                IMapperLink iterLink = (IMapperLink) iter.next();
                if (iterLink.getEndNode() != null) {
                    return true;
                }
            }
        }
        if (link.getEndNode() != null) {
            for (Iterator iter=links.iterator(); iter.hasNext();) {
                IMapperLink iterLink = (IMapperLink) iter.next();
                if (iterLink.getStartNode() != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Activates inline mnemonics defined in a container and its children.
     *  @param  owner   Parent owner container to use.
     *  @since  5.5
     */
    public static void activateInlineMnemonics(Container owner) {
        Component[] comps = owner.getComponents();
        for (int i = 0; ((comps != null) && (i < comps.length)); i++) {
            Component comp = comps[i];
            if ((comp instanceof JLabel) || (comp instanceof AbstractButton)) {
                activateInlineMnemonics(comp);
            } else if (comp instanceof Container) {
                activateInlineMnemonics((Container) comp);
            }
        }
    }
     
    /** Activates inline mnemonics defined in a component.
     *  @param  comp    Component to use.
     *  @since  5.5
     */
    public static void activateInlineMnemonics(Component comp) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            Mnemonics.setLocalizedText(label, label.getText());
        } else if (comp instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) comp;
            Mnemonics.setLocalizedText(button, button.getText());
        }
    }
   
    /** Tests if a string is empty.
     *  @param  str     String to test.
     *  @return <code>true</code> if so.
     *  @since  5.5
     */
    public static boolean isEmpty(String str) {
        return ((null == str) || (str.trim().length() == 0));
    }
    
    /** @see org.openide.awt.Actions#cutAmpersand(java.lang.String)
     */
    public static String cutAmpersand(String string) {
        return Actions.cutAmpersand(string);
    }
}
