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
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.SVG;
import com.sun.perseus.model.SVGImageImpl;
import com.sun.perseus.model.Time;
import com.sun.perseus.model.UpdateAdapter;
import com.sun.perseus.util.SVGConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.ExternalResourceHandler;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.mobility.svgcore.SVGDataLoader;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.SVGComposerPrototypeFactory;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
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
public class PerseusController {
    public static final int ANIMATION_NOT_AVAILABLE = 0;
    public static final int ANIMATION_NOT_RUNNING   = 1;
    public static final int ANIMATION_RUNNING       = 2;
    public static final int ANIMATION_PAUSED        = 3;

    public static final String ATTR_ID              = "id";
    
    public static final float DEFAULT_MAX           = 30.0f;
    public static final float DEFAULT_STEP          = 0.1f;
    public static final String ID_VIEWBOX_MARKER    = "$VIEWBOX$";
    public static final String ID_BBOX_MARKER       = "$BBOX$";
        
    protected final SceneManager        m_sceneMgr;
    protected       SVGAnimatorImpl     m_animator;
    protected       SVGImage            m_svgImage;
    protected       DocumentNode        m_svgDoc;
    protected       SVGLocatableElement m_viewBoxMarker;
    //protected       SVGLocatableElement m_bBoxMarker;
    protected       int                 m_animationState = ANIMATION_NOT_AVAILABLE;
    protected       float               m_currentTime  = 0.0f;
    

    PerseusController(SceneManager sceneMgr) {
        m_sceneMgr = sceneMgr;
    }

    void initialize() {
        m_svgImage   = m_sceneMgr.getSVGImage();
        m_svgDoc     = (DocumentNode) m_svgImage.getDocument();
        //System.out.println("Before rendering: " + m_svgImage);
        //PerseusController.printTree( (DocumentNode) m_svgImage.getDocument(), 0);
        
        m_animator = (SVGAnimatorImpl) SVGAnimator.createAnimator( m_svgImage, "javax.swing.JComponent"); //NOI18N        
        m_animationState = m_sceneMgr.getDataObject().getModel().containsAnimations() ? 
            ANIMATION_NOT_RUNNING : ANIMATION_NOT_AVAILABLE;
        
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
        
        /*
        rect = svg.getBBox();
        m_bBoxMarker = (SVGLocatableElement) m_svgDoc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                SVGConstants.SVG_RECT_TAG);
        m_bBoxMarker.setId(ID_BBOX_MARKER);
        m_bBoxMarker.setTrait(SVGConstants.SVG_FILL_ATTRIBUTE, "none"); //NOI18N
        m_bBoxMarker.setTrait(SVGConstants.SVG_STROKE_ATTRIBUTE, "red"); //NOI18N
        m_bBoxMarker.setFloatTrait(SVGConstants.SVG_X_ATTRIBUTE, rect.getX());
        m_bBoxMarker.setFloatTrait(SVGConstants.SVG_Y_ATTRIBUTE, rect.getY());
        m_bBoxMarker.setFloatTrait(SVGConstants.SVG_WIDTH_ATTRIBUTE, rect.getWidth());
        m_bBoxMarker.setFloatTrait(SVGConstants.SVG_HEIGHT_ATTRIBUTE, rect.getHeight());
        svg.appendChild(m_bBoxMarker);
         */
        
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
/*
    public SVGLocatableElement wrapElement(final SVGObject svgObj) {
        SVGLocatableElement elem = svgObj.getSVGElement();
        Node parent = elem.getParentNode();
        assert m_svgDoc == getOwnerDocument(parent) : "Perseus node belongs to another document";
        // HACK - clear all elements' ids so that the element removal is possible
        setNullIds(elem, true);
        parent.removeChild(elem);
        PatchedGroup wrapper = (PatchedGroup) m_svgDoc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                SVGConstants.SVG_G_TAG);
        wrapper.attachSVGObject(svgObj);
        wrapper.appendChild(elem);
        // HACK - restore element ids
        parent.appendChild(wrapper);
        setNullIds(elem, false);
        wrapper.setId( m_sceneMgr.getDataObject().getModel().createUniqueId("", true));
        return wrapper;
    }
  */
    
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
        assert elem != null : "Element must not be null";
        if ( elem instanceof PatchedElement) {
            PatchedElement pelem = (PatchedElement) elem;
            SVGObject obj = pelem.getSVGObject();
            if (obj == null) {
                obj = new SVGObject(m_sceneMgr, elem);
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
    /*
    public boolean isAnimationStopped() {
        return m_animationState == ANIMATION_NOT_AVAILABLE ||
               m_animationState == ANIMATION_NOT_RUNNING;
    }*/
    
    public int getAnimatorState() {
        return m_animationState;
    }
    
    public void startAnimator(){
        if (m_animationState == ANIMATION_NOT_RUNNING ||
            m_animationState == ANIMATION_PAUSED){
            if (m_animator.getState() != SVGAnimatorImpl.STATE_PLAYING) {
                m_animator.play();
            }
            m_animationState = ANIMATION_RUNNING;
            m_sceneMgr.getScreenManager().repaint();
        }
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
            m_animationState == ANIMATION_PAUSED){
            if (m_animator.getState() != SVGAnimatorImpl.STATE_PAUSED) {
                m_animator.pause();
            }
            setAnimatorTime(0);                    
            m_animationState = ANIMATION_NOT_RUNNING;
            m_sceneMgr.getScreenManager().repaint();            
        }
    }

    public void setAnimatorTime(float time) {
        if (m_animator != null ){
            m_currentTime = time;
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
  */  
    
    //TODO use more robust mechanism for id replacement
    private static final String [] REPLACE_PATTERNS = {
        "id=\"{0}\"",
        "begin=\"{0}.",
        "end=\"{0}."
    };
        
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
        
    protected static SVGImage loadDocument( final InputStream is, final ExternalResourceHandler handler) 
        throws IOException {

        DocumentNode documentNode   = new DocumentNode();
        UpdateAdapter updateAdapter = new UpdateAdapter();
        documentNode.setUpdateListener(updateAdapter);

        ModelBuilder.loadDocument(is, documentNode,
                SVGComposerPrototypeFactory.getPrototypes(documentNode));

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
            System.out.print("    ");
        }
        System.out.println( node.getClass());
        ModelNode child = node.getFirstChildNode();
        while(child != null) {
            printTree(child, level+1);
            child = child.getNextSiblingNode();
        }               
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
}
