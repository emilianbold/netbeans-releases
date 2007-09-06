/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.awt.SVGAnimatorImpl;
import com.sun.perseus.builder.ModelBuilder;
import com.sun.perseus.j2d.Point;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.model.AbstractAnimate;
import com.sun.perseus.model.CompositeGraphicsNode;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.SVG;
import com.sun.perseus.model.SVGImageImpl;
import com.sun.perseus.model.Time;
import com.sun.perseus.model.UpdateAdapter;
import com.sun.perseus.util.SVGConstants;
import java.awt.AWTEvent;
import java.awt.AWTEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.microedition.m2g.ExternalResourceHandler;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedAnimationElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedTransformableElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.SVGComposerPrototypeFactory;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author Pavel Benes
 */
public final class PerseusController {
    public static final int ANIMATION_NOT_AVAILABLE = 0;
    public static final int ANIMATION_NOT_RUNNING   = 1;
    public static final int ANIMATION_RUNNING       = 2;
    public static final int ANIMATION_PAUSED        = 3;

    public static final float ANIMATION_DEFAULT_DURATION = 30.0f;
    public static final float ANIMATION_DEFAULT_STEP     = 0.1f;
    public static final String ID_VIEWBOX_MARKER         = "$VIEWBOX$"; //NOI18N
    public static final String ID_BBOX_MARKER            = "$BBOX$"; //NOI18N

    public static final int        EVENT_ANIM_STARTED = AWTEvent.RESERVED_ID_MAX + 534;
    public static final int        EVENT_ANIM_STOPPED = EVENT_ANIM_STARTED + 1;
    
    protected final SceneManager        m_sceneMgr;
    protected       SVGAnimatorImpl     m_animator;
    protected       SVGImage            m_svgImage;
    protected       DocumentNode        m_svgDoc;
    protected       SVGLocatableElement m_viewBoxMarker;
    protected       int                 m_animationState = ANIMATION_NOT_AVAILABLE;
    protected       float               m_currentTime  = 0.0f;
    

    PerseusController(SceneManager sceneMgr) {
        m_sceneMgr = sceneMgr;
    }

    private boolean containsAnimation(ModelNode node) {
        if ( node instanceof AbstractAnimate) {
            return true;
        }

        ModelNode child = node.getFirstChildNode();
        while( child != null) {
            if (containsAnimation(child)) {
                return true;
            } else {
                child = child.getNextSiblingNode();
            }
        }
        return false;        
    }
    
    void initialize() {
        m_svgImage       = m_sceneMgr.getSVGImage();
        m_svgDoc         = (DocumentNode) m_svgImage.getDocument();        
        m_animator       = (SVGAnimatorImpl) SVGAnimator.createAnimator( m_svgImage, "javax.swing.JComponent"); //NOI18N        
        m_animationState = containsAnimation(m_svgDoc) ? ANIMATION_NOT_RUNNING : ANIMATION_NOT_AVAILABLE;
        
        SVGSVGElement svg  = getSVGRootElement();
        SVGRect       rect = svg.getRectTrait(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
        
        if (rect != null) {
            m_viewBoxMarker = (SVGLocatableElement) m_svgDoc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                    SVGConstants.SVG_RECT_TAG);
            m_viewBoxMarker.setId(ID_VIEWBOX_MARKER);
            m_viewBoxMarker.setTrait(SVGConstants.SVG_FILL_ATTRIBUTE, "none"); //NOI18N
            m_viewBoxMarker.setTrait(SVGConstants.SVG_STROKE_ATTRIBUTE, "none"); //NOI18N
            m_viewBoxMarker.setFloatTrait(SVGConstants.SVG_X_ATTRIBUTE, rect.getX());
            m_viewBoxMarker.setFloatTrait(SVGConstants.SVG_Y_ATTRIBUTE, rect.getY());
            m_viewBoxMarker.setFloatTrait(SVGConstants.SVG_WIDTH_ATTRIBUTE, rect.getWidth());
            m_viewBoxMarker.setFloatTrait(SVGConstants.SVG_HEIGHT_ATTRIBUTE, rect.getHeight());

            svg.appendChild(m_viewBoxMarker);
        } else {
            m_viewBoxMarker = null;
        }
        
        // we need to get the animator into the 'paused' state so that
        // all changes are immediately visible
        m_animator.play();
        m_animator.pause();
    }
    
    public SVGPoint convertCoords(float x, float y) {
        SVGMatrix m = getSVGRootElement().getScreenCTM().inverse();
        return new Point( m.getComponent(0) * x + m.getComponent(2) * y + m.getComponent(4),
                          m.getComponent(1) * x + m.getComponent(3) * y + m.getComponent(5));        
    }
    
    public SVGLocatableElement getViewBoxMarker() {
        return m_viewBoxMarker;
    }
        
    public JComponent getAnimatorGUI() {
        return (JComponent) m_animator.getTargetComponent();
    }

    public void execute(Runnable command) {
        try {
            m_animator.invokeAndWait(command);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //TODO use only one of the methods bellow
    public DocumentNode getSVGDocument() {
        return m_svgDoc;        
    }
        
    public SVGSVGElement getSVGRootElement() {
        return (SVGSVGElement) m_svgDoc.getDocumentElement();        
    }
    
    public SVGObject getObjectById(String id) {
        SVGElement elem = getElementById(id);
        if ( elem != null && elem instanceof SVGLocatableElement) {
            SVGLocatableElement locElem = (SVGLocatableElement) elem;
            if ( getSafeScreenBBox(locElem) != null) {
                return getSVGObject( locElem);
            }
        }
        return null;
    }
      
    public SVGObject [] getObjectsAt(int x, int y) {
        SVGLocatableElement elem = _findElementAt(x,y);
        
        if (elem != null) {
            //try to find an parent envelope node if any
            Node node = elem;
            while( node != null) {
                if ( PatchedGroup.isWrapper(node)) {
                    elem = (SVGLocatableElement) node;
                    break;
                } 
                node = node.getParentNode();
            }
                        
            SVGObject obj = getSVGObject(elem);
            if (obj != null) {
                return new SVGObject [] {obj};
            }
        }
        return null;
    }
    
    public void delete(final SVGLocatableElement elem) {
        execute(new Runnable() {
            public void run() {
                Node parent = elem.getParentNode();
                // HACK - clear all elements' ids so that the element removal is possible
                setNullIds(elem, true);
                try {
                    parent.removeChild(elem);
                } finally {
                    setNullIds(elem, false);
                }
            }            
        });
    }

    public void moveToBottom(final SVGLocatableElement elem) {
        final Node parent     = elem.getParentNode();
        final ModelNode firstChild = ((ModelNode)parent).getFirstChildNode();
        
        if (firstChild != elem) {
            execute(new Runnable() {
                public void run() {
                    // HACK - clear all elements' ids so that the element removal is possible
                    setNullIds(elem, true);
                    try {
                        parent.removeChild(elem);
                        parent.insertBefore(elem, (Node) firstChild);
                    } finally {
                        setNullIds(elem, false);
                    }
                }            
            });
        }
    }

    public void moveToTop(final SVGLocatableElement elem) {
        final Node parent         = elem.getParentNode();
        final ModelNode lastChild = ((ModelNode)parent).getLastChildNode();
        
        if (lastChild != elem) {
            execute(new Runnable() {
                public void run() {
                    // HACK - clear all elements' ids so that the element removal is possible
                    setNullIds(elem, true);
                    try {
                        parent.removeChild(elem);
                        parent.appendChild(elem);
                    } finally {
                        setNullIds(elem, false);
                    }
                }            
            });
        }
    }

    public void moveBackward(final SVGLocatableElement elem) {
        final Node parent          = elem.getParentNode();
        final ModelNode firstChild = ((ModelNode)parent).getFirstChildNode();
        
        if (firstChild != elem) {
            execute(new Runnable() {
                public void run() {
                    // HACK - clear all elements' ids so that the element removal is possible
                    setNullIds(elem, true);
                    try {
                        ModelNode previousChild = ((ModelNode)elem).getPreviousSiblingNode();
                        assert previousChild != null;
                        parent.removeChild(elem);
                        
                        parent.insertBefore(elem, (Node) previousChild);
                    } finally {
                        setNullIds(elem, false);
                    }
                }            
            });
        }
    }
    
    public void moveForward(final SVGLocatableElement elem) {
        final Node parent         = elem.getParentNode();
        final ModelNode lastChild = ((ModelNode)parent).getLastChildNode();
        
        if (lastChild != elem) {
            execute(new Runnable() {
                public void run() {
                    // HACK - clear all elements' ids so that the element removal is possible
                    setNullIds(elem, true);
                    try {
                        ModelNode nextChild = ((ModelNode)elem).getNextSiblingNode();                        
                        assert nextChild != null;
                        parent.removeChild(elem);
                        if (nextChild == lastChild) {
                            parent.appendChild(elem);
                        } else {
                            parent.insertBefore(elem, (Node)nextChild.getNextSiblingNode());
                        }
                    } finally {
                        setNullIds(elem, false);
                    }
                }            
            });
        }
    }
        
    private synchronized SVGObject getSVGObject(SVGLocatableElement elem) {
        assert elem != null : "Element must not be null"; //NOI18N
        if ( elem instanceof PatchedElement) {
            PatchedElement pelem = (PatchedElement) elem;
            SVGObject obj = pelem.getSVGObject();
            if (obj == null) {
                obj = new SVGObject(m_sceneMgr, elem);
                //System.out.println("Object created: " + obj);
                pelem.attachSVGObject(obj);                
            }
            return obj;
        } else {
            System.err.println("PatchedElement must be used instead of " + elem.getClass().getName());
            return null;
        }   
    }

    public SVGLocatableElement _findElementAt(int x, int y) {
        float[] pt = {x, y};
        ModelNode target = m_svgDoc.nodeHitAt(pt);
        SVGLocatableElement elt = null;
        if (target != null) {
            while (elt == null && target != null) {
                if (target instanceof SVGLocatableElement) {
                    elt = (SVGLocatableElement) target;
                } else {
                    target = target.getParent();
                }
            }
        }
        return elt;
    }
    
    public static SVGElement hideAllButSubtree( ModelNode node, String id) {
        if ( node instanceof SVGElement) {
            SVGElement elem = (SVGElement) node;
            if ( id.equals(elem.getId())) {
                return elem;
            } 
        }
        
        ModelNode child = node.getFirstChildNode();
        SVGElement visibleChild = null;
        
        while( child != null) {
            SVGElement e;
            if ( (e=hideAllButSubtree(child, id)) != null) {
                visibleChild = e;
            }
            child = child.getNextSiblingNode();
        }
        if ( node instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode elem = (CompositeGraphicsNode) node;
            if (visibleChild == null) {
                elem.setVisibility(false);
                elem.setTrait(SVGConstants.SVG_VISIBILITY_ATTRIBUTE, "hidden");
            }
        }
        return visibleChild;
    }
    
    public static SVGElement findElementById( SVGSVGElement root, String id) {
        SVGElement elem = getElementById( (ModelNode) root, id);
        return elem;
    }

    protected SVGElement getElementById(String id) {
        SVGElement elem = getElementById( (ModelNode) getSVGRootElement(), id);
        //assert elem != null;
        return elem;
    }
    
    protected static SVGElement getElementById( ModelNode node, String id) {
        if (node instanceof SVGElement) {
            SVGElement svgElem = (SVGElement) node;
            if ( id.equals(svgElem.getId())) {
                return svgElem;
            }
        }

        ModelNode child = node.getFirstChildNode();
        while( child != null) {
            SVGElement res = getElementById(child, id);
            if (res != null) {
                return res;
            }
            child = child.getNextSiblingNode();
        }
        return null;
    }
    
    public int getAnimatorState() {
        return m_animationState;
    }
    
    
    public void startAnimator(){
        if (m_animationState == ANIMATION_NOT_RUNNING ||
            m_animationState == ANIMATION_PAUSED){
            if (m_animator.getState() != SVGAnimatorImpl.STATE_PLAYING) {
                m_animator.play();
                m_sceneMgr.processEvent( new AWTEvent(this, EVENT_ANIM_STARTED){});
            }
            m_animationState = ANIMATION_RUNNING;
            m_sceneMgr.getScreenManager().repaint();
        }
    }
    
     public void getFocusableTargets(List<String> focusableTargets) {
        SVGElement root = getSVGRootElement();
        Set<String> ids = new HashSet<String>();
        collectFocusableElements(root, ids);
        focusableTargets.add(null);
        orderFocusableElements(root, ids, focusableTargets);
        
    }
    public void pauseAnimator(){
        if (m_animationState == ANIMATION_RUNNING){
            if (m_animator.getState() == SVGAnimatorImpl.STATE_PLAYING) {
                m_animator.pause();
            }
            m_animationState = ANIMATION_PAUSED;
        }
    }
    
    public void stopAnimator(){
        if (m_animationState == ANIMATION_RUNNING ||
            m_animationState == ANIMATION_PAUSED) {
            m_animator.stop();
            m_animator.play();
            m_animator.pause();
            setAnimatorTime(0);
            m_animationState = ANIMATION_NOT_RUNNING;
            m_sceneMgr.processEvent( new AWTEvent(this, EVENT_ANIM_STOPPED){});            
            m_sceneMgr.getScreenManager().repaint();            
        }
    }

    public float getAnimatorTime() {
        try {
            if (m_animator.getState() != SVGAnimatorImpl.STATE_STOPPED) {
                m_animator.invokeAndWait(new Runnable() {
                    public void run() {
                        m_currentTime = getSVGRootElement().getCurrentTime();
                        m_sceneMgr.updateAnimationDuration(m_currentTime);
                    }
                });
            } else {
                m_currentTime = 0;
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return m_currentTime;
    }
    
    public void setAnimatorTime(float time) {
        if (m_animator != null ){
            m_currentTime = time;
            m_sceneMgr.updateAnimationDuration(m_currentTime);
            if ( m_animator.getState() != SVGAnimatorImpl.STATE_STOPPED) {
                m_animator.invokeLater( new Runnable() {
                    public void run() {
                        getSVGRootElement().setCurrentTime(m_currentTime);
                    }    
                });
            } else {
                System.err.println("Animator is stopped!");
            }
        }        
    }
    
    public void startAnimation(String id) {
        SVGElement elem = getElementById(id);
        if (elem != null && elem instanceof SVGAnimationElement) {
            //System.out.println("Starting animation ...");
            ((SVGAnimationElement) elem).beginElementAt(0);
        } else {
            System.err.println("Animation element not found: " + id);
        }       
    }

    public void stopAnimation(String id) {
        SVGElement elem = getElementById(id);
        if (elem != null && elem instanceof SVGAnimationElement) {
            //System.out.println("Stopping animation ...");
            ((SVGAnimationElement) elem).endElementAt(0);
        } else {
            System.err.println("Animation element not found: " + id);
        }
    }
           
    public static ModelNode getRootElement(ModelNode node) {
        ModelNode parent;
        
        while( (parent=node.getParent()) != null) {
            node = parent;
        }
        return node;
    }
        
/*    
    public void _mergeImage(File file) throws FileNotFoundException {
        FileInputStream     fin = new FileInputStream(file);
        BufferedInputStream in  = new BufferedInputStream(fin);

        try {
            ModelBuilder.loadDocument(in, m_svgDoc,
                    SVGComposerPrototypeFactory.getPrototypes(m_svgDoc));
        } finally {
            try {
                in.close();
            } catch( IOException e) {
                e.printStackTrace();
            }
        }
        
        SVG svgRoot = (SVG)getSVGRootElement();
        System.out.println("Before children transfer");
        printTree(m_svgDoc, 0);
        
        ModelNode sibling = svgRoot;
        while( (sibling=sibling.getNextSiblingNode()) != null) {
            if (sibling instanceof  SVG) {
                transferChildren(svgRoot, (SVG)sibling);
            }
        }
        System.out.println("After children transfer");
        printTree(m_svgDoc, 0);
    }
    
    //TODO use more robust mechanism for id replacement
    private static final String [] REPLACE_PATTERNS = {
        "id=\"{0}\"",
        "begin=\"{0}.",
        "end=\"{0}."
    };
  */  
        
    public static SVGImage createImage(InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        }

        SVGImage img = loadDocument(stream, null);

        // Now, get image width/height from <svg> element and set it in
        // DocumentNode
        DocumentNode docNode = (DocumentNode) img.getDocument();
        Element      root    = docNode.getDocumentElement();
        if (!(root instanceof SVG)) {
            //TODO better reporting
            throw new IOException("Problem");
        }  

        SVG svg    = (SVG) root;
        int width  = (int) svg.getWidth();
        int height = (int) svg.getHeight();
        docNode.setSize(width, height);
        
        return img;        
    }
    
/*    
    protected static void collectIDs(DocumentElement de, Set<String> ids) {
        
        AttributeSet attrs = de.getAttributes();
        String       id    = (String) attrs.getAttribute(ATTR_ID);
        if (id != null) {
            if ( !ids.add(id)) {
                System.err.println("Duplicated id: " + id);
            }
        }
        
        List<DocumentElement> children = de.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            collectIDs( children.get(i), ids);
        }
    }
    
    protected static void collectIDs(ModelNode node, Set<String> ids) {
        if (node instanceof SVGElement) {
            String id = ((SVGElement) node).getId();
            if (id != null) {
                if ( !ids.add(id)) {
                    System.err.println("Duplicated id: " + id);
                }
            }
        }
        ModelNode child = node.getFirstChildNode();
        while(child != null) {
            collectIDs(child, ids);
            child = child.getNextSiblingNode();
        }               
    }

    protected void replaceAllOccurences(BaseDocument doc, String oldStr, String newStr) throws BadLocationException {
        String text      = doc.getText(0, doc.getLength());
        int    newLength = newStr.length();
        int    oldLength = oldStr.length();
        int    pos  = 0;
        int    diff = 0;
        
        while( (pos=text.indexOf(oldStr, pos)) != -1) {
            doc.replace(pos+diff, oldLength, newStr, null);
            pos += newLength;
            diff += newLength - oldLength;
        }        
    }
*/        
    protected static SVGImage loadDocument( final InputStream is, final ExternalResourceHandler handler) 
        throws IOException {

        DocumentNode documentNode   = new DocumentNode();
        UpdateAdapter updateAdapter = new UpdateAdapter();
        documentNode.setUpdateListener(updateAdapter);

        //long t = System.currentTimeMillis();
        ModelBuilder.loadDocument(is, documentNode,
                SVGComposerPrototypeFactory.getPrototypes(documentNode));
        //System.out.println("Load document took " + (System.currentTimeMillis() - t) + "[ms]");
        
        if (updateAdapter.hasLoadingFailed()) {
            if (updateAdapter.getLoadingFailedException() != null) {
                throw new IOException
                    (updateAdapter.getLoadingFailedException().getMessage());
            }
            throw new IOException("Loading of SVG document failed.");
        }

        SVGImageImpl img = new SVGImageImpl(documentNode, null);

        // Now, initialize the timing engine and sample at zero.
        documentNode.initializeTimingEngine();
        documentNode.sample(new Time(0));
        
        return img;        
    }
    
    protected static int getChildrenCount( ModelNode node) {
        int count = 0;
        ModelNode child = node.getFirstChildNode();
        while( child != null) {
            count++;
            child = child.getNextSiblingNode();
        }
        return count;        
    }
    
    protected static void transferChildren(SVGElement target, SVGElement source) {
        ModelNode child = ((ModelNode) source).getFirstChildNode();
        while(child != null) {
            if (child instanceof PatchedElement) {
                PatchedElement pe = (PatchedElement) child;
                child = child.getNextSiblingNode();
                setNullIds( (SVGElement) pe, true);
                source.removeChild((Node)pe);
                target.appendChild((Node)pe);
                setNullIds( (SVGElement) pe, false);
            } else {
                System.err.println("The PatchedElement must be used instead of " + child.getClass().getName());
            }
        }
    }
    
    public static void printTree( ModelNode node, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    "); //NOI18N
        }
        System.out.println( node.getClass());
        ModelNode child = node.getFirstChildNode();
        while(child != null) {
            printTree(child, level+1);
            child = child.getNextSiblingNode();
        }               
    }
    
    private static final String [] ANIM_PATTERNS = new String [] {
        "." + SVGConstants.SVG_DOMFOCUSIN_EVENT_TYPE, 
        "." + SVGConstants.SVG_DOMFOCUSOUT_EVENT_TYPE + 
        "." + SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE};
    
    /*
     * Collect all element ids referenced by animation begin trait.
     */
    private void collectFocusableElements(SVGElement elem, Set<String> elemIds) {
        if (elem instanceof PatchedAnimationElement ) {
            String beginTrait = elem.getTrait(SVGConstants.SVG_BEGIN_ATTRIBUTE);
            for (String pattern : ANIM_PATTERNS) {
                int p = 0;
                while ( (p=beginTrait.indexOf(pattern, p)) != -1) {
                    int i = p - 1;
                    while( i >= 0 && isElementIdChar(beginTrait.charAt(i))) {
                        i--;
                    }
                    String id = beginTrait.substring(i+1, p);
                    if ( getElementById(id) != null) {
                        elemIds.add(id);
                    }
                    
                    p += pattern.length();
                }
            }
        }
        
        SVGElement child = (SVGElement) elem.getFirstElementChild();
        while(child != null) {
            collectFocusableElements( child, elemIds);
            child = (SVGElement)child.getNextElementSibling();
        }
    }
    
    /*
     * Order element ids by their occurence in SVG document.
     */
    private boolean orderFocusableElements(SVGElement elem, Set<String>ids, List<String>orderedIds) {
        String id = elem.getId();
        if (id != null) {
            if ( ids.remove(id)) {
                orderedIds.add(id);
                if (ids.isEmpty()) {
                    return true;
                }
            }
        }
        SVGElement child = (SVGElement) elem.getFirstElementChild();
        while(child != null) {
            if (orderFocusableElements( child, ids, orderedIds)) {
                return true;
            }
            child = (SVGElement)child.getNextElementSibling();
        }
        return false;
    }
    
    private static boolean isElementIdChar(char c) {
        return Character.isLetter(c) ||
             Character.isDigit(c) ||
             c == '.' ||
             c == '_' ||
             c == '-' ||
             c == ':';
    }
    
    public static void setNullIds(SVGElement elem, boolean isNull) {
        if (elem instanceof PatchedElement) {
            ((PatchedElement) elem).setNullId(isNull);
        } else if ( elem.getId() != null) {
            System.err.println("The patched element must be used instead of " + elem.getClass().getName() + "[" + elem.getId() + "]");
        }

        SVGElement child = (SVGElement) elem.getFirstElementChild();
        while(child != null) {
            setNullIds( child, isNull);
            child = (SVGElement)child.getNextElementSibling();
        }
    }
    
    public static boolean isViewBoxMarker( ModelNode node) {
        return node instanceof SVGElement &&
               ID_VIEWBOX_MARKER.equals(((SVGElement) node).getId());
    }

    public static SVGRect getSafeScreenBBox(SVGLocatableElement elem) {
        SVGRect bBox = elem.getScreenBBox();
        if ( bBox == null) {
            //TODO solve the issue with null bounding box
            System.err.println("Null screen BBox for element:" + elem);
            ModelNode child = ((ModelNode)elem).getFirstChildNode();
            if (child != null && child instanceof SVGLocatableElement) {
                bBox = ((SVGLocatableElement) child).getScreenBBox();
            }
        }
        return bBox;
    }
    
    public static SVGRect getSafeBBox(SVGLocatableElement elem) {
        SVGRect bBox = elem.getBBox();
        if ( bBox == null) {
            //TODO solve the issue with null bounding box
            System.err.println("Null BBox for element:" + elem);
            ModelNode child = ((ModelNode)elem).getFirstChildNode();
            if (child != null && child instanceof SVGLocatableElement) {
                bBox = ((SVGLocatableElement) child).getBBox();
            }
        }
        return bBox;
    }
        
    protected static Document getOwnerDocument(Node elem) {
        Node parent;
        
        while( (parent=elem.getParentNode()) != null) {
            elem = parent;
        }
        return (Document) elem;
    }   

    public static SVGMatrix getParentTransformation(Node node) {
        List<SVGMatrix> transforms = null;
        
        
        while(node != null) {
            if ( (node instanceof PatchedTransformableElement) ) {
                Transform temp = ((PatchedTransformableElement) node).getTransform();
                if (temp != null) {
                    if (transforms == null) {
                        transforms = new ArrayList<SVGMatrix>();
                    }
                    transforms.add( new Transform(temp));
                }
            }
            node = node.getParentNode();
       }
        SVGMatrix total = null;
        
        if (transforms != null) {
            total = transforms.get(0);
            for (int i = 1; i < transforms.size(); i++) {
                total = total.mMultiply(transforms.get(i));
            }
        }

        return total;
    }
    
    /*
    public void mergeImage(File file) throws FileNotFoundException, IOException, DocumentModelException, BadLocationException {
        DocumentModel docModel = SVGFileModel.loadDocumentModel(file);
              
        DocumentElement svgElem = SVGFileModel.getSVGRoot(docModel);
        int childElemNum;
        
        SVGFileModel fileModel = m_sceneMgr.getDataObject().getModel();
        
        if (svgElem != null && (childElemNum=svgElem.getElementCount()) > 0) {
            int startOff = svgElem.getElement(0).getStartOffset();
            int endOff   = svgElem.getElement(childElemNum - 1).getEndOffset();

            String insertedText = docModel.getDocument().getText(startOff, endOff - startOff + 1);
            
            Set<String> oldIds = new HashSet<String>();
            collectIDs(m_svgDoc, oldIds);

            Set<String> newIds = new HashSet<String>();
            collectIDs( svgElem, newIds);

            Set<String> conflicts = new HashSet<String>();
            for (String id  : newIds) {
                if (oldIds.contains(id)) {
                    conflicts.add(id);
                }
            }

            BaseDocument doc  = (BaseDocument) docModel.getDocument();
            if ( !conflicts.isEmpty()) {
                for (String id : conflicts) {
                    String newID = fileModel.createUniqueId(id, false);
                    for (String pattern : REPLACE_PATTERNS) {
                        String oldStr = MessageFormat.format(pattern, id);
                        String newStr = MessageFormat.format(pattern, newID);
                        replaceAllOccurences(doc, oldStr, newStr);
                    }
                }
            }
            System.out.println("Before children transfer");
            printTree(m_svgDoc, 0);

            String       text = doc.getText(0, doc.getLength());
            java.io.StringBufferInputStream in = new java.io.StringBufferInputStream(text);
            SVGImage svgImage = createImage(in);
            
            System.out.println("Inserted document");
            printTree( (DocumentNode) svgImage.getDocument(), 0);
            Node nodeToImport = (Node) ((com.sun.perseus.model.ModelNode) svgImage.getDocument().getDocumentElement()).getFirstChildNode();
            
            System.out.println("Node document " +nodeToImport.getOwnerDocument());
            ModelNode firstChild = ((ModelNode)nodeToImport).getFirstChildNode();
            System.out.println("First child node: " + firstChild);
            System.out.println("First child document: " + firstChild.getOwnerDocument());
            System.out.println("First child parent: " + firstChild.getParent());
            
            System.out.println("Node children count before: " + getChildCount((ModelNode)nodeToImport));
            nodeToImport = m_svgDoc.adoptNode(nodeToImport, false);
            System.out.println("Node children count after: " + getChildCount((ModelNode)nodeToImport));
            System.out.println("Node document " +nodeToImport.getOwnerDocument());
            System.out.println("First child node: " + firstChild);
            System.out.println("First child document: " + firstChild.getOwnerDocument());
            System.out.println("First child parent: " + firstChild.getParent());
            m_svgDoc.getDocumentElement().appendChild(nodeToImport);

            System.out.println("After children transfer");
            printTree(m_svgDoc, 0);  
            m_sceneMgr.getScreenManager().repaint();
        }       
    }    
    */
}
