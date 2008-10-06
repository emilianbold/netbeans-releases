/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.mobility.svgcore.composer;

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
import java.util.logging.Level;
import javax.microedition.m2g.ExternalResourceHandler;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedAnimationElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedTransformableElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.SVGComposerPrototypeFactory;
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
    
    private static final String [] ANIM_PATTERNS = new String [] {
        "." + SVGConstants.SVG_DOMFOCUSIN_EVENT_TYPE, //NOI18N
        "." + SVGConstants.SVG_DOMFOCUSOUT_EVENT_TYPE + //NOI18N
        "." + SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE}; //NOI18N
    private static final SVGMatrix IDENTITY_TRANSFORM = new Transform(null);
    
    
    private static int s_instanceCounter = 0;

    protected final SceneManager        m_sceneMgr;
    protected final String              m_id;
    protected       SVGAnimatorImpl     m_animator;
    protected       SVGImage            m_svgImage;
    protected       DocumentNode        m_svgDoc;
    protected       SVGLocatableElement m_viewBoxMarker;
    protected       int                 m_animationState = ANIMATION_NOT_AVAILABLE;
    protected       float               m_currentTime  = 0.0f;
    
    PerseusController(SceneManager sceneMgr) {
        m_sceneMgr = sceneMgr;
        m_id = "PC-" + (s_instanceCounter++) + "-" + sceneMgr;
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
        SceneManager.log(Level.INFO, toString() + " initialized."); //NOI18N
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
            SceneManager.error("Command execution failed.", ex); //NOI18N
        }
    }

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
        SceneManager.log(Level.INFO, "No object found for id " + id + "(" + toString() + ")"); //NOI18N
        return null;
    }
    
    public SVGElement getElementById(String id) {
        SVGElement elem = getElementById( (ModelNode) getSVGRootElement(), id);
        return elem;
    }
      
    public SVGObject [] getObjectsAt(int x, int y) {
        SVGLocatableElement elem = findElementAt(x,y);
        
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
    
    public void delete(final SVGElement elem) {
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

    public SVGLocatableElement findElementAt(int x, int y) {
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
                elem.setTrait(SVGConstants.SVG_VISIBILITY_ATTRIBUTE, "hidden"); //NOI18N
            }
        }
        return visibleChild;
    }
    
    public static SVGElement findElementById( SVGSVGElement root, String id) {
        SVGElement elem = getElementById( (ModelNode) root, id);
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
    
    public boolean isAnimatorStarted() {
        return m_animationState == ANIMATION_RUNNING || m_animationState == ANIMATION_PAUSED;
    }
    
    public void startAnimator(){
        if (m_animationState == ANIMATION_NOT_RUNNING ||
            m_animationState == ANIMATION_PAUSED){
            if (m_animator.getState() != SVGAnimatorImpl.STATE_PLAYING) {
                m_animator.play();
                m_sceneMgr.processEvent( new AWTEvent(this, SceneManager.EVENT_ANIM_STARTED){});
            }
            m_animationState = ANIMATION_RUNNING;
            m_sceneMgr.getScreenManager().repaint();
        }
    }
    
     public void getFocusableTargets(List<String> focusableTargets) {
        SVGElement root = getSVGRootElement();
        Set<String> ids = new HashSet<String>();
        collectFocusableElements(root, ids);
        orderFocusableElements(root, ids, focusableTargets);
        focusableTargets.add(0, null);        
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
            m_sceneMgr.processEvent( new AWTEvent(this, SceneManager.EVENT_ANIM_STOPPED){});            
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
            SceneManager.error( toString() + " - Wait interrupted.", ex); //NOI18N
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
                SceneManager.log(Level.SEVERE, toString() + " - Animator is stopped."); //NOI18N
            }
        }        
    }
    
    public void startAnimation(String id) {
        SVGElement elem = getElementById(id);
        if (elem != null && elem instanceof SVGAnimationElement) {
            ((SVGAnimationElement) elem).beginElementAt(0);
        } else {
            SceneManager.log(Level.SEVERE, toString() + " - Animation element not found: " + id); //NOI18N
        }       
    }

    public void stopAnimation(String id) {
        SVGElement elem = getElementById(id);
        if (elem != null && elem instanceof SVGAnimationElement) {
            ((SVGAnimationElement) elem).endElementAt(0);
        } else {
            SceneManager.log(Level.SEVERE, toString() + " - Animation element not found: " + id); //NOI18N
        }
    }

    public String toString() {
        return m_id;
    }
    
    private synchronized SVGObject getSVGObject(SVGLocatableElement elem) {
        assert elem != null : "Element must not be null"; //NOI18N
        if ( elem instanceof PatchedElement) {
            PatchedElement pelem = (PatchedElement) elem;
            SVGObject obj = pelem.getSVGObject();
            if (obj == null) {
                obj = new SVGObject(m_sceneMgr, elem);
                pelem.attachSVGObject(obj);                
            }
            return obj;
        } else {
            SceneManager.log(Level.SEVERE, toString() + " - PatchedElement must be used instead of " + elem.getClass().getName()); //NOI18N
            return null;
        }   
    }
    
    public static ModelNode getRootElement(ModelNode node) {
        ModelNode parent;
        
        while( (parent=node.getParent()) != null) {
            node = parent;
        }
        return node;
    }
        
    public static SVGImage createImage(InputStream stream) 
            throws IOException, InterruptedException 
    {
        if (stream == null) {
            throw new NullPointerException();
        }

        SVGImage img = loadDocument(stream, null);

        // Now, get image width/height from <svg> element and set it in
        // DocumentNode
        DocumentNode docNode = (DocumentNode) img.getDocument();
        Element      root    = docNode.getDocumentElement();
        if (!(root instanceof SVG)) {
            SceneManager.log(Level.SEVERE, "Missing SVG root element"); //NOI18N
            throw new IOException("Missing SVG root element"); //NOI18N
        }  

        SVG svg    = (SVG) root;
        int width  = (int) svg.getWidth();
        int height = (int) svg.getHeight();
        docNode.setSize(width, height);
        
        return img;        
    }
    
    protected static SVGImage loadDocument( final InputStream is, final ExternalResourceHandler handler) 
        throws IOException, InterruptedException 
    {

        DocumentNode documentNode   = new DocumentNode();
        UpdateAdapter updateAdapter = new UpdateAdapter();
        documentNode.setUpdateListener(updateAdapter);

        //long t = System.currentTimeMillis();
        ModelBuilder.loadDocument(is, documentNode,
                SVGComposerPrototypeFactory.getPrototypes(documentNode));
        //System.out.println("Load document took " + (System.currentTimeMillis() - t) + "[ms]");
        
        if (updateAdapter.hasLoadingFailed()) {
            if (updateAdapter.getLoadingFailedException() != null) {
                String message = updateAdapter.getLoadingFailedException().getMessage();
                if (message != null && message.startsWith(ModelBuilder.LOAD_INTERRUPTED)){
                    throw new InterruptedException(message);
                } else {
                    throw new IOException(message);
                }
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
                SceneManager.log(Level.SEVERE, "PatchedElement must be used instead of " + child.getClass().getName()); //NOI18N
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
    
    public static boolean isElementIdChar(char c) {
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
            SceneManager.log(Level.SEVERE, "PatchedElement must be used instead of " + elem.getClass().getName()); //NOI18N
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
            SceneManager.log(Level.SEVERE, "Null screen BBox for element:" + elem); //NOI18N
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
            SceneManager.log(Level.SEVERE, "Null BBox for element:" + elem); //NOI18N
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
    
    public static final boolean isIdentityTransform( SVGMatrix matrix, boolean ignoreTranslate) {
        int length = ignoreTranslate ? 4 : 6;
        for (int i = 0; i < length; i++) {
            if (Math.abs( IDENTITY_TRANSFORM.getComponent(i) - matrix.getComponent(i)) > 0.001) {
                return false;
            }
        }
        return true;
    }
}
