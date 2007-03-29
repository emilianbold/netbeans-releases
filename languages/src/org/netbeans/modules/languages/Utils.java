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

package org.netbeans.modules.languages;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class Utils {
    
    private static Logger logger = Logger.getLogger ("org.netbeans.modules.languages");
    
    public static void notify (String message) {
        logger.log (Level.WARNING, message);
    }
    
    public static void message (final String message) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                DialogDisplayer.getDefault ().notify (new Message (message));
            }
        });
    }
    
    public static void notify (Exception exception) {
        logger.log (Level.WARNING, null, exception);
    }
    
    public static void notify (String message, Exception exception) {
        logger.log (Level.WARNING, message, exception);
    }

    private static Map<String,WeakReference> collections;
    
    public static void startTest (String name, Collection c) {
        if (collections == null) {
            // init
            collections = new HashMap<String,WeakReference> ();
            start ();
        }
        collections.put (name, new WeakReference<Collection> (c));
    }
    
    public static void startTest (String name, Map m) {
        if (collections == null) {
            // init
            collections = new HashMap<String,WeakReference> ();
            start ();
        }
        collections.put (name, new WeakReference<Map> (m));
    }
    
    private static void start () {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Map<String,WeakReference> cs = new HashMap<String,WeakReference> (collections);
                Iterator<String> it = cs.keySet ().iterator ();
                while (it.hasNext ()) {
                    String name = it.next ();
                    Object o = cs.get (name).get ();
                    if (o == null)
                        collections.remove (name);
                    else
                        System.out.println (":" + name + " " + size (o));
                }
                start ();
            }
        }, 5000);
    }
    
    private static int size (Object o) {
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            int s = c.size ();
            Iterator it = c.iterator ();
            while (it.hasNext ()) {
                Object item = it.next ();
                if (item instanceof Collection ||
                    item instanceof Map
                )
                    s += size (item);
            }
            return s;
        }
        Map m = (Map) o;
        int s = m.size ();
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object key = it.next ();
            if (key instanceof Collection ||
                key instanceof Map
            )
                s += size (key);
            Object value = m.get (key);
            if (value instanceof Collection ||
                value instanceof Map
            )
                s += size (value);
        }
        return s;
    }
    
    public static Point findPosition (
        String      text, 
        int         offset
    ) {
        int current = 0;
        int next = text.indexOf ('\n', current);
        int lineNumber = 1;
        while (next >= 0) {
            if (next > offset)
                return new Point (lineNumber, offset - current + 1);
            lineNumber++;
            current = next + 1;
            next = text.indexOf ('\n', current);
        }
        throw new ArrayIndexOutOfBoundsException ();
    }
}
