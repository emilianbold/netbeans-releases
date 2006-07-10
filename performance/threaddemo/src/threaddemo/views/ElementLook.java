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

package threaddemo.views;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.spi.looks.Look;
import org.openide.actions.DeleteAction;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

// XXX could have other operations - insert etc.

/**
 * Look for a DOM Document or one of its elements.
 * @author Jesse Glick
 */
public class ElementLook extends Look implements EventListener {
    
    private static final Logger logger = Logger.getLogger(ElementLook.class.getName());
    
    public ElementLook() {
        super("ElementLook");
    }
    
    public String getDisplayName() {
        return "DOM Elements";
    }
    
    protected void attachTo(Object o) {
        assert o instanceof Element;
        EventTarget et = (EventTarget)o;
        // Node{Inserted,Removed} is fired *before* the change. This is better;
        // fired *after* it has taken effect.
        et.addEventListener("DOMSubtreeModified", this, false);
    }
    
    protected void detachFrom(Object o) {
        EventTarget et = (EventTarget)o;
        et.removeEventListener("DOMSubtreeModified", this, false);
    }
    
    public void handleEvent(Event evt) {
        // XXX for some reason, sometimes if refactoring is done while an XML phadhail is
        // expanded, some infinite loop occurs and this method is called repeatedly
        try {
            Element parent = (Element)evt.getCurrentTarget();
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "ElementLook: event on {0}: {1}; co={2}", new Object[] {parent.getTagName(), evt, getChildObjects(parent, null)});
            }
            fireChange(parent, Look.GET_CHILD_OBJECTS | Look.GET_DISPLAY_NAME);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    
    public String getName(Object o, Lookup env) {
        return ((Element)o).getTagName();
    }
    
    public String getDisplayName(Object o, Lookup env) {
        return fullText((Element)o);
    }
    
    private static String fullText(Element el) {
        StringBuffer buf = new StringBuffer();
        buf.append('<');
        buf.append(el.getNodeName());
        NamedNodeMap attrs = el.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr)attrs.item(i);
            buf.append(' ');
            buf.append(attr.getName());
            buf.append('=');
            buf.append('"');
            try {
                buf.append(XMLUtil.toAttributeValue(attr.getValue()));
            } catch (CharConversionException e) {
                e.printStackTrace();
            }
            buf.append('"');
        }
        if (el.getElementsByTagName("*").getLength() > 0) {
            // Have some sub-elements.
            buf.append('>');
        } else {
            buf.append("/>");
        }
        return buf.toString();
    }
    
    public boolean isLeaf(Object o, Lookup env) {
        NodeList nl = ((Element)o).getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return false;
            }
        }
        return true;
    }
    
    public List getChildObjects(Object o, Lookup env) {
        NodeList nl = ((Element)o).getChildNodes();
        List<Element> l = new ArrayList<Element>(Math.max(nl.getLength(), 1));
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                l.add((Element) n);
            }
        }
        return l;
    }
    
    public boolean canDestroy(Object o, Lookup env) {
        Element e = (Element)o;
        return !(e.getParentNode() instanceof Document);
    }
    
    public void destroy(Object o, Lookup env) {
        Element e = (Element)o;
        e.getParentNode().removeChild(e);
    }
    
    public Action[] getActions(Object o, Lookup env) {
        return new Action[] {
            SystemAction.get(DeleteAction.class),
        };
    }
    
}
