/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.test.guitracker;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Tracks activity within a GUI program, this activity loosely being the
 * major events within the lifetime of the application.  The activity is 
 * recorded in a simple object (@see ActionTracker.Tuple).  Activity is
 * tracked in "groups" of actions (@see ActionTracker.EventList), and a 
 * new "group" is begun for each TRACK_START received.
 */
public class ActionTracker {
    
    /** Start of a sequence of recorded events. */
    public final static int TRACK_START = 1;
    /** Painting happened.  @see Painter */
    public final static int TRACK_PAINT = 2;
    
    /** MOUSE_PRESSED event. */
    public final static int TRACK_MOUSE_PRESS = 3;
    /** MOUSE_RELEASED event. */
    public final static int TRACK_MOUSE_RELEASE = 4;
    /** MOUSE_DRAGGED event. */
    public final static int TRACK_MOUSE_DRAGGED = 5;
    /** MOUSE_MOVED event. */
    public final static int TRACK_MOUSE_MOVED = 6;
    
    /** KEY_PRESSED event. */
    public final static int TRACK_KEY_PRESS = 7;
    /** KEY_RELEASED event. */
    public final static int TRACK_KEY_RELEASE = 8;
    
    /** COMPONENT_SHOWN event happened on a Frame or JFrame. */
    public static final int TRACK_FRAME_SHOW = 9;
    /** COMPONENT_HIDDEN event happened on a Frame or JFrame. */
    public static final int TRACK_FRAME_HIDE = 10;
    
    /** COMPONENT_SHOWN event happened on a Dialog or JDialog. */
    public final static int TRACK_DIALOG_SHOW = 11;
    /** COMPONENT_HIDDEN event happened on a Dialog or JDialog. */
    public final static int TRACK_DIALOG_HIDE = 12;
    
    /** COMPONENT_SHOWN event happened on a Component. */
    public final static int TRACK_COMPONENT_SHOW = 13;
    /** COMPONENT_HIDDEN event happened on a Component. */
    public final static int TRACK_COMPONENT_HIDE = 14;
    
    /** Any messages the application wants to send. */
    public final static int TRACK_APPLICATION_MESSAGE = 15;
    
    /** FOCUS_GAINED event */
    public final static int TRACK_FOCUS_GAINED = 21;
    /** FOCUS_LOST event */
    public final static int TRACK_FOCUS_LOST = 22;
    /** unknown event */
    public final static int TRACK_INVOCATION = 23;
    /** unknown event */
    public final static int TRACK_UNKNOWN = 24;
    
    /** The name of the root element in generated XML. */
    public final static String TN_ROOT_ELEMENT = "action-tracking";
    /** The name of the event-list element in generated XML. */
    public final static String TN_EVENT_LIST   = "event-list";
    /** The name of each event element in generated XML. */
    public final static String TN_EVENT        = "event";
    /** The attribute name for start time. */
    public final static String ATTR_START      = "start";
    /** The attribute name for descriptive phrase. */
    public final static String ATTR_NAME       = "name";
    /** The attribute name for the node-type. */
    public final static String ATTR_TYPE       = "type";
    /** The attribute name for the timestamp. */
    public final static String ATTR_TIME       = "time";
    /** The attribute name for calculated time difference since the start. */
    public final static String ATTR_TIME_DIFF_START = "diff";
    /** The attribute name for calculated time difference since the last MOUSE_DRAGGED event. */
    public final static String ATTR_TIME_DIFF_DRAG  = "diffdrag";
    
    private static ActionTracker instance = null;
    
    /**
     * Retrieves the ActionTracker instance for this application.  Rather
     * than constructing your own ActionTracker (note that the constructor
     * is <code>private</code>, meaning you can't construct your own) use
     * this method to get the instance.
     */
    public static ActionTracker getInstance() {
        if (instance == null)
            instance = new ActionTracker();
        return instance;
    }
    
    /** List of event lists. */
    private LinkedList/*EventList*/ eventLists = null;
    
    /** Events gathered during one event tracking period. */
    private EventList/*Tuple*/ currentEvents = null;
    
    private OurAWTEventListener awt_listener = null;
    private boolean connected = false;
    
    private String fnActionOutput = null;
    private boolean exportXmlWhenScenarioFinished = false;
    
    private boolean allowRecording = true;
    
    /** Creates a new instance of RepaintTracker, private so that
     * this is a singleton class. */
    private ActionTracker() {
        setDefaultAWTEventListeningMask();
    }
    
    public void stopRecording() {
        allowRecording = false;
    }
    
    public void startRecording() {
        allowRecording = true;
    }
    
    public boolean isRecording() {
        return allowRecording;
    }
    
    /**
     * Set the default file name to output to for <code>outputAsXML</code>.
     */
    public void setOutputFileName(String fn) {
        fnActionOutput = fn;
    }
    
    public void setExportXMLWhenScenarioFinished(boolean export) {
        exportXmlWhenScenarioFinished = export;
    }
    
    /**
     * Get the <i>current</i> <code>EventList</code> into which events
     * are being recorded.
     */
    public EventList getCurrentEvents() {
        return currentEvents;
    }
    
    /**
     * Remove memory of the <i>current</i> <code>EventList</code>.
     */
    public void forgetCurrentEvents() {
        currentEvents = null;
        if (eventLists != null)
            eventLists.removeLast();
    }
    
    public LinkedList getEventLists() {
        return eventLists;
    }
    
    /**
     * Remove memory of all recorded events.
     */
    public void forgetAllEvents() {
        if (eventLists != null) eventLists.clear();
        if (currentEvents != null) currentEvents.clear();
        currentEvents = null;
    }
    
    /**
     * Record a TRACK_START event.  This causes a clean, fresh, and new 
     * EventList to be begun (with the previous current EventList to be
     * saved away).
     */
    public void startNewEventList(String name) {
        if (eventLists == null)
            eventLists = new LinkedList();
        
        currentEvents = new EventList(name);
        eventLists.add(currentEvents);
        currentEvents.start();
        startRecording();
        add(TRACK_START, "START", currentEvents.startMillies);
    }
    
    long default_awt_event_mask = AWTEvent.COMPONENT_EVENT_MASK 
                                | AWTEvent.MOUSE_EVENT_MASK
                                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                                | AWTEvent.KEY_EVENT_MASK;
    long awt_event_mask = -1;
    
    public void setDefaultAWTEventListeningMask() {
         awt_event_mask = default_awt_event_mask;
    }
    
    public long getDefaultAWTEventListengingMask() {
        return default_awt_event_mask;
    }
    
    public void setAWTEventListeningMask(long mask) {
        awt_event_mask = mask;
    }
    
    public long getAWTEventListengingMask() {
        return awt_event_mask;
    }
    
    /**
     * Manage the connection to the AWT <code>EventQueue</code>, recording
     * interesting events that go by.
     */
    public void connectToAWT(boolean connect) {
        if (connect) {
            if (awt_listener == null)
                awt_listener = new OurAWTEventListener(this);
            if (!connected) {
                Toolkit.getDefaultToolkit().addAWTEventListener(awt_listener, 
                                                                awt_event_mask);
            }
            connected = true;
        } else {
            if (awt_listener != null)
                Toolkit.getDefaultToolkit().removeAWTEventListener(awt_listener);
            connected = false;
        }
    }
    
    /**
     * Add the <code>Tuple</code> to the current EventList.
     */
    public void add(Tuple t) {
        if (!isRecording()) 
            return;
        if (currentEvents != null)
            currentEvents.add(t);
    }
    
    /**
     * Add a <code>Tuple</code> matching these parameters
     * to the current EventList.
     */
    public void add(int code, String name, long millies) {
        EventList ce = getCurrentEvents();
        add(new Tuple(code, name, millies, ce != null ? ce.getStartMillis() : (long)-1));
    }
    
    /**
     * Add a <code>Tuple</code> matching these parameters
     * to the current EventList.  The <code>time</code> parameter is
     * derived from the current time.
     */
    public void add(int code, String name) {
        EventList ce = getCurrentEvents();
        add(new Tuple(code, name, ce != null ? ce.getStartMillis() : (long)-1));
    }
    
    /**
     * Process an AWTEvent, and if it's interesting recording it in
     * the current EventList.
     */
    public void add(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) event;
            int mod = me.getModifiers();
            int id = me.getID();
            if ((mod & MouseEvent.BUTTON1_MASK) != 0
             && (id == MouseEvent.MOUSE_PRESSED
              || id == MouseEvent.MOUSE_RELEASED)) {
                
                String mr = id == MouseEvent.MOUSE_PRESSED
                        ? "MOUSE_PRESSED" : "MOUSE_RELEASED";
                int bmask = me.getButton();
                
                add(id == MouseEvent.MOUSE_PRESSED
                     ? TRACK_MOUSE_PRESS : TRACK_MOUSE_RELEASE,
                  mr
                    + " bmask=" + Integer.toString(bmask)
                    + " modifiers=" + MouseEvent.getMouseModifiersText(me.getModifiers())
                );
            }
            if (id == MouseEvent.MOUSE_MOVED
             || id == MouseEvent.MOUSE_DRAGGED) {
                 String mm = id == MouseEvent.MOUSE_MOVED
                    ? "MOUSE_MOVED" : "MOUSE_DRAGGED";
                 
                 add(id == MouseEvent.MOUSE_MOVED
                        ? TRACK_MOUSE_MOVED : TRACK_MOUSE_DRAGGED,
                     mm + " " 
                        + Integer.toString(me.getX())
                        + ","
                        + Integer.toString(me.getY())
                 );
            }
        }
        else if (event instanceof KeyEvent) {
            KeyEvent ke = (KeyEvent) event;
            int id = ke.getID();
            if (id == KeyEvent.KEY_PRESSED
             || id == KeyEvent.KEY_RELEASED) {
                 
                String kr = id == KeyEvent.KEY_PRESSED
                        ? "KEY_PRESSED" : "KEY_RELEASED";
                int kc = ke.getKeyCode();
                
                add(id == KeyEvent.KEY_PRESSED
                  ? ActionTracker.TRACK_KEY_PRESS
                  : ActionTracker.TRACK_KEY_RELEASE,
                  "KeyEvent " + kr 
                + " keycode=" + Integer.toString(kc)
                + " keytext=" + KeyEvent.getKeyText(kc)
                + " modtext=" + KeyEvent.getKeyModifiersText(ke.getModifiers()));
            }
        }
        else if (event instanceof WindowEvent) {
            WindowEvent we = (WindowEvent) event;
            //System.out.println("WindowEvent " + we.paramString());
        }
        else if (event instanceof FocusEvent) {
            FocusEvent fe = (FocusEvent) event;
            int id = fe.getID();
            Component opposite = fe.getOppositeComponent();
            Component thisone  = fe.getComponent();
            boolean   temp     = fe.isTemporary();
            if (id == FocusEvent.FOCUS_GAINED) {
                add(ActionTracker.TRACK_FOCUS_GAINED,
                    (temp ? "temp " : "perm ")
                    + "opp " + opposite
                    + "this " + thisone);
            } else if (id == FocusEvent.FOCUS_LOST) {
                add(ActionTracker.TRACK_FOCUS_LOST,
                    (temp ? "temp " : "perm ")
                    + "opp " + opposite
                    + "this " + thisone);
            }
        }
        else if (event instanceof ComponentEvent) {
            ComponentEvent ce = (ComponentEvent) event;
            int id = ce.getID();
            // ignore ComponentEvent.COMPONENT_MOVED & ComponentEvent.COMPONENT_RESIZED
            if (id == ComponentEvent.COMPONENT_HIDDEN
             || id == ComponentEvent.COMPONENT_SHOWN) {
                 Component c = ce.getComponent();
                 if (c instanceof Frame  || c instanceof JFrame) {
                      add(id == ComponentEvent.COMPONENT_HIDDEN
                        ? ActionTracker.TRACK_FRAME_HIDE
                        : ActionTracker.TRACK_FRAME_SHOW,
                        ce.paramString());
                 }
                 else if (c instanceof Dialog || c instanceof JDialog) {
                      add(id == ComponentEvent.COMPONENT_HIDDEN
                        ? ActionTracker.TRACK_DIALOG_HIDE
                        : ActionTracker.TRACK_DIALOG_SHOW,
                        ce.paramString());
                 }
                 else if (c instanceof Window || c instanceof JWindow) {
                      add(id == ComponentEvent.COMPONENT_HIDDEN
                        ? ActionTracker.TRACK_COMPONENT_HIDE
                        : ActionTracker.TRACK_COMPONENT_SHOW,
                        ce.paramString());
                 }
            }
        }
        else if (event instanceof InvocationEvent) {
            // there is way too many InvocationEvents
//            InvocationEvent ie = (InvocationEvent)event;
//            add(TRACK_INVOCATION, ie.paramString());
        }
        else {
            add(TRACK_UNKNOWN, "unknown event: " + event.paramString());
        }
    }
    
    /**
     * Record a message from the application, recording a 
     * <code>TRACK_APPLICATION_MESSAGE</code> event.
     */
    public void applicationMessage(String msg) {
        add(TRACK_APPLICATION_MESSAGE, msg);
    }
   
    /**
     * Record a painting notification from the application, recording a 
     * <code>TRACK_PAINT</code> event.
     */
    public void paintHappened(String name) {
        add(TRACK_PAINT, name);
    }
    
    Object paintWaiter = new Object();
    
    /**
     * Notify the ActionTracker that the scenario is finishing, and is to be 
     * called by the scenario, when it's <code>run</code> method is finishing.
     * Otherwise nothing in the system knows that it's finished.
     */
    public void scenarioFinished() {
        applicationMessage("ScenarioFinished");
        if (exportXmlWhenScenarioFinished) {
            try { exportAsXML(); } catch (Exception e) {
                System.err.println("Unable to export to XML because " + e);
                e.printStackTrace();
            }
        }
    }
    
    
    DocumentBuilder dbld = null;
    DocumentBuilderFactory dbfactory = null;
    TransformerFactory tfactory = null;
    
    TransformerFactory getTransformerFactory()
        throws TransformerConfigurationException
    {
        if (tfactory == null) tfactory = TransformerFactory.newInstance();
        return tfactory;
    }
    
    DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        if (dbfactory == null) dbfactory = DocumentBuilderFactory.newInstance();
        if (dbld == null)      dbld      = dbfactory.newDocumentBuilder();
        return dbld;
    }
    
    /**
     * Write all recorded event information, in XML format,
     * to <code>System.out</code>.
     */
    public void exportAsXML()
        throws ParserConfigurationException, TransformerConfigurationException,
               TransformerException
    {
        PrintStream out = System.out; // Default
        if (fnActionOutput != null) {
            try {
                out = new PrintStream(
                        new FileOutputStream(
                            new File(fnActionOutput)
                        )
                );
            } catch (Exception e) {
                out = System.out;
            }
        }
        exportAsXML(null, out);
    }
    
    /**
     * Write all recorded event information, in XML format,
     * to the given <code>PrintStream</code>.
     */
    public void exportAsXML(PrintStream out)
        throws ParserConfigurationException, TransformerConfigurationException,
               TransformerException
    {
        exportAsXML(null, out);
    }
    
    /**
     * Write all recorded event information, in XML format,
     * to the given <code>PrintStream</code>.  In addition, it is transformed
     * by the given XSLT script.
     */
    public void exportAsXML(Document style, PrintStream out)
        throws ParserConfigurationException, TransformerConfigurationException,
               TransformerException
    {
        Document doc = getDocumentBuilder()
                      .getDOMImplementation()
                      .createDocument(null, TN_ROOT_ELEMENT, null);
        Element root = doc.getDocumentElement();
        
        // Construct the DOM contents by scanning through all EventLists,
        // and for each event adding everything to the DOM.
        
        ListIterator liLists = eventLists != null ? eventLists.listIterator() : null;
        while (liLists != null && liLists.hasNext()) {
            // For each EventList
            EventList evlist = (EventList)liLists.next();
            Element evlistElement = doc.createElement(TN_EVENT_LIST);
            root.appendChild(evlistElement);
            evlistElement.setAttribute(ATTR_START, Long.toString(evlist.getStartMillis()));
            evlistElement.setAttribute(ATTR_NAME, evlist.getName());
            ListIterator liEvents = evlist.listIterator();
            while (liEvents.hasNext()) {
                // For each Event
                Tuple t = (Tuple)liEvents.next();
                Element eventElement = doc.createElement(TN_EVENT);
                evlistElement.appendChild(eventElement);
                eventElement.setAttribute(ATTR_TYPE, t.getCodeName());
                eventElement.setAttribute(ATTR_NAME, t.getName());
                eventElement.setAttribute(ATTR_TIME, Long.toString(t.getTimeMillis()));
                eventElement.setAttribute(ATTR_TIME_DIFF_START, Long.toString(t.getTimeDifference()));
            }
        }
        
        // Now, transform it out
        
        Transformer tr = style != null
            ? getTransformerFactory().newTransformer(new DOMSource(style))
            : getTransformerFactory().newTransformer();
            
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource docSrc = new DOMSource(doc);
        StreamResult rslt = new StreamResult(out);
        tr.transform(docSrc, rslt);
    }
    
    public static String getNameForCode(int code) {
        String cname = "unk";
        switch (code) {
            case TRACK_START:         cname = "TRACK_START"; break;
            case TRACK_PAINT:         cname = "TRACK_PAINT"; break;
            case TRACK_MOUSE_PRESS:   cname = "TRACK_MOUSE_PRESS"; break;
            case TRACK_MOUSE_RELEASE: cname = "TRACK_MOUSE_RELEASE"; break;
            case TRACK_MOUSE_DRAGGED: cname = "TRACK_MOUSE_DRAGGED"; break;
            case TRACK_MOUSE_MOVED:   cname = "TRACK_MOUSE_MOVED"; break;
            case TRACK_KEY_PRESS:     cname = "TRACK_KEY_PRESS"; break;
            case TRACK_KEY_RELEASE:   cname = "TRACK_KEY_RELEASE"; break;
            case TRACK_FRAME_SHOW:    cname = "TRACK_FRAME_SHOW"; break;
            case TRACK_FRAME_HIDE:    cname = "TRACK_FRAME_HIDE"; break;
            case TRACK_DIALOG_SHOW:   cname = "TRACK_DIALOG_SHOW"; break;
            case TRACK_DIALOG_HIDE:   cname = "TRACK_DIALOG_HIDE"; break;
            case TRACK_COMPONENT_SHOW:cname = "TRACK_COMPONENT_SHOW"; break;
            case TRACK_COMPONENT_HIDE:cname = "TRACK_COMPONENT_HIDE"; break;
            case TRACK_INVOCATION:    cname = "TRACK_INVOCATION"; break;
            case TRACK_UNKNOWN:       cname = "TRACK_UNKNOWN"; break;
            case TRACK_APPLICATION_MESSAGE: cname = "TRACK_APPLICATION_MESSAGE"; break;
        }
        return cname;
    }
    
    /**
     * Record a list of @see ActionTracker.Tuple objects.  This is
     * a <code>LinkedList</code> in disguise, so to access objects in
     * the list just use that mechanism.
     */
    public final class EventList extends LinkedList {
        
        private String name = "unknown";
        private long startMillies = -1;

        EventList() { 
            super();
        }
        
        public EventList(String name) {
            this();
            if (name == null || name.length() <= 0)
                throw new RuntimeException("Must provide a name");
            this.name = name;
        }
        
        /** Cause the "start time" to be recorded.  This value will
         * only be recorded once, and comes from
         * <code>System.currentTimeMillis()</code>.
         */
        public void start() {
            if (startMillies == -1) 
                startMillies = System.currentTimeMillis();
        }
        
        /** Return the recorded "start time" for this list. */
        public long getStartMillis() {
            return startMillies;
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return getName() 
                + " (" + this.size() + ") " 
                + new Date(getStartMillis()).toString();
        }
    }
    
    /** Events to record into an EventList.  The code is one of the 
     * ActionTracker.TRACK_xxx values, and the name can be any String
     * that makes sense to you.  The time (millies) comes from
     * <code>System.currentTimeMillis()</code>.
     */
    public final class Tuple {
//        public Tuple(int code, String name) {
//            this(code, name, System.currentTimeMillis(), (long)0);
//        }
        public Tuple(int code, String name, long start) {
            this(code, name, System.currentTimeMillis(), start);
        }
        public Tuple(int code, String name, long millies, long start) {
            this.code = code;
            this.name = name;
            this.millies = millies;
            this.diffies = millies - start;
            //System.err.println("new ActionTracker.Tuple " + toString());
        }
        int code;
        String name;
        long millies;
        long diffies; // Difference from a "start" time
        
        /** Get the translation of the code into a String. */
        public String getCodeName() {
            return ActionTracker.getNameForCode(code);
        }
        
        public int getCode() {
            return code;
        }
        
        public String getName() { return name; }
        
        public long getTimeMillis() { return millies; }
        
        public long getTimeDifference() { return diffies; }
        
        public String toString() {
            return getCodeName() 
                + " " + name 
                + " " + Long.toString(millies)
                + " " + Long.toString(diffies);
        }
    }
    
    class OurAWTEventListener implements AWTEventListener {
        ActionTracker tracker = null;
        public OurAWTEventListener(ActionTracker t) {
            tracker = t;
        }
        
        public void eventDispatched(AWTEvent event) {
            tracker.add(event);
        }
    }
        
}
