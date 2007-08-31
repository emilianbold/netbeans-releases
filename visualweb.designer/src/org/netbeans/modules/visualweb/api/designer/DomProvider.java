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

package org.netbeans.modules.visualweb.api.designer;


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;
import java.util.EventListener;
import javax.swing.JComponent;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.spi.palette.PaletteController;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;


/**
 *
 * @author Peter Zavadsky
 */
public interface DomProvider {


//    public interface DomProviderListener extends EventListener {
//        public void modelChanged();
//        public void modelRefreshed();
//        public void nodeChanged(Node rendered, Node parent, boolean wasMove);
//        public void nodeRemoved(Node previouslyRendered, Node parent);
//        public void nodeInserted(Node rendered, Node parent);
//        public void updateErrorsInComponent();
//        public void gridModeUpdated(boolean gridMode);
//        public void documentReplaced();
//        public void showDropMatch(MarkupDesignBean markupDesignBean, MarkupMouseRegion markupMouseRegion, int dropType);
//        public void showDropMatch(Element componentRootElement, Element regionElement, int dropType);
//        public void clearDropMatch();
//        public void select(DesignBean designBean);
//        public void select(Element componentRootElement);
//        public void refreshForm(boolean deep);
//        public void inlineEdit(DesignBean[] designBeans);
//        public void inlineEdit(Element[] componentRootElements);
//        public void designContextActivated(DesignContext designContext);
//        public void designContextDeactivated(DesignContext designContext);
//        public void designContextChanged(DesignContext designContext);
//        public void designBeanCreated(DesignBean designBean);
//        public void designBeanDeleted(DesignBean designBean);
//        public void designBeanMoved(DesignBean designBean, DesignBean designBean0, Position position);
//        public void designBeanContextActivated(DesignBean designBean);
//        public void designBeanContextDeactivated(DesignBean designBean);
//        public void designBeanNameChanged(DesignBean designBean, String string);
//        public void designBeanChanged(DesignBean designBean);
//        public void designPropertyChanged(DesignProperty designProperty, Object object);
//        public void designEventChanged(DesignEvent designEvent);
        // XXX Better name, better design needed.
//        public void designContextGenerationChanged();
//    } // End of DomProviderListener.


//    public void addDomProviderListener(DomProviderListener l);
//    public void removeDomProviderListener(DomProviderListener l);


    /** Gets html document. */
    public Document getHtmlDom();

    /** XXX Gets html document fragment. Containing the 'rendered' tree.
     * FIXME This should be in the document directly. */
    public DocumentFragment getHtmlDocumentFragment();

    /** XXX Gets the body element. */
    public Element getHtmlBody();

    /** Gets <code>PaletteController</code> associated with this <code>DomProvider</code>. */
    public PaletteController getPaletteController();

//    //////
//    // XXX Revise these methods
//    public void requestRefresh();
//    public void refreshModel(boolean deep);
//    public void refreshProject();
//    public void destroyDomSynchronizer();
//    /** Until all modification stuff is moved to designer/jsf (from designer). */
//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend);
//    public boolean isRefreshPending();
//    public void attachContext();
//    public void detachContext();
//    public DocumentFragment createSourceFragment(MarkupDesignBean bean);
//    public void requestChange(MarkupDesignBean bean);
//    public void beanChanged(MarkupDesignBean bean);
//    public void requestTextUpdate(MarkupDesignBean bean);
//    // XXX
//    //////

    // >>> DnD
//    public DataFlavor getImportFlavor(DataFlavor[] flavors);
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable);
//    public DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, CoordinateTranslator coordinateTranslator);
//    public Element[] pasteComponents(Transferable t, Element parentComponentRootElement, Point location);
//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension, Location location, CoordinateTranslator coordinateTranslator, int dropAction);
//    public void importString(String string, Location location, CoordinateTranslator coordinateTranslator);
//    public DesignBean findHtmlContainer(DesignBean parent);
//    public String[] getClassNames(DisplayItem[] displayItems);
//    public boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, Location location, CoordinateTranslator coordinateTranslator) throws IOException;
//    public MarkupPosition getDefaultPositionUnderParent(DesignBean parent);
//    public int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos);
    public int computeActions(Element dropeeComponentRootElement, Transferable transferable);
//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp);
//    public int processLinks(Element origElement, Class[] classes, List beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget);
    public int processLinks(Element origElement, Element componentRootElement);
//    public boolean setDesignProperty(DesignBean bean, String attribute, int length);
//    // XXX
//    public boolean isBraveheartPage();
//    // XXX
//    public boolean isWoodstockPage();
    
    public boolean canPasteTransferable(Transferable trans);

//    public void updateGridMode();
    public boolean isGridMode();
    
//    // XXX
//    public interface CoordinateTranslator {
//        public Point translateCoordinates(Element parent, int x, int y);
//        public int snapX(int x);
//        public int snapY(int y);
//    }
    
//    // XXX
//    public static class Location {
//        public DesignBean droppee;
//
//        /** If true, the droppee was deliberately chosen rather than having been
//         * inferred from for example a drop point in the canvas. This is typically
//         * the case when you point at a bean in the application outline. */
//
//        //boolean droppeeChosen;
//        public String facet;
////        RaveElement droppeeElement;
//        public Element droppeeElement;
//        public MarkupPosition pos;
//        public Point coordinates;
//        public Dimension size;
//    }
    // <<< DnD
    
//    /** XXX TEMP. */
//    public FacesModel getFacesModel();

//    public boolean isFragment();
//    public boolean isPortlet();
    
//    public DataObject getJspDataObject();

    
    public URL getBaseUrl();
    public URL resolveUrl(String urlString);

//    public DocumentFragment renderHtmlForMarkupDesignBean(MarkupDesignBean markupDesignBean);
    
//    public Document getJspDom();
    
//    public void clearHtml();
    
//    // XXX
//    public List<FileObject> getWebPageFileObjectsInThisProject();

//    // XXX
////    public boolean editEventHandlerForDesignBean(DesignBean designBean);
//    public boolean editEventHandlerForComponent(Element componentRootElement);

//    public boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node);
    public boolean canDropComponentsAtNode(Element[] componentRootElements, Node node);

//    public boolean handleMouseClickForElement(Element element, int clickCount);

    // XXX
//    public boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean);
    public boolean isNormalAndHasFacesComponent(Element componentRootElement);

//    public boolean canHighlightMarkupDesignBean(MarkupDesignBean markupDesignBean);

//    public DesignBean createBean(String className, Node parent, Node before);
//    // XXX Get rid of this too, there may not be any explicit modification in designer.
//    /** @return Source element! */
//    public Element createComponent(String className, Node parent, Node before);

//    public boolean isFormBean(DesignBean designBean);

//    // XXX Returns source element, get rid of it.
//    public Element getDefaultParentMarkupBeanElement();

//    public boolean moveBean(DesignBean bean, Node parentNode, Node before);

//    public boolean setPrerenderedBean(MarkupDesignBean markupDesignBean, DocumentFragment documentFragment);

//    // XXX
//    public MarkupDesignBean getMarkupDesignBeanEquivalentTo(MarkupDesignBean oldBean);

//    public org.openide.nodes.Node getRootBeanNode();

//    public void deleteBean(DesignBean designBean);
//    public void deleteComponent(Element componentRootElement);

//    public boolean canCreateBean(String className, DesignBean parent, Position pos);

//    public DesignBean getDefaultParentBean();
    public Element getDefaultParentComponent();

//    // XXX
//    public Exception getRenderFailure();
//    public MarkupDesignBean getRenderFailureMarkupDesignBean();
//    // XXX
//    public void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException);
//    public void setRenderFailureValues();
//    public boolean hasRenderFailure();
//    public Exception getRenderFailureException();
//    public MarkupDesignBean getRenderFailureComponent();
//    public boolean hasRenderingErrors();

    // XXX  Bad architecture, model itself should take care of its consistency.
//    public void syncModel();
    public boolean isModelValid();
    public boolean isModelBusted();

//    // XXX  Bad architecture, model itself should take care of its consistency.
//    public boolean isSourceDirty();

//    public Transferable copyBeans(DesignBean[] beans);
//    public Transferable copyComponents(Element[] componentRootElements);
    
//    // XXX ErrorPanels
//    // FIXME There should be cleaner mechanism provided.
//    public ErrorPanel getErrorPanel(ErrorPanelCallback errorPanelCallback);
//
//    // XXX
//    public interface ErrorPanel {
//        public void updateErrors();
//    } // End of  ErrorPanel.
//    // XXX Hack for the impls. Ged rid of this.
//    public interface ErrorPanelCallback {
//        public void updateTopComponentForErrors();
//        public void setRenderFailureShown(boolean shown);
////        public Exception getRenderFailure();
////        public MarkupDesignBean getRenderFailureComponent();
//        public void handleRefresh(boolean showErrors);
//    } // End of ErrorPanelCallback.
    
    // XXX Lock hack
//    public WriteLock writeLock(String message);
//    public void writeUnlock(WriteLock writeLock);
//    public boolean isWriteLocked();
//    public interface WriteLock {
//    }
    public void readLock();
    public void readUnlock();

//    // XXX Get rid of this.
//    public void setModelActivated(boolean activated);

//    public UndoRedo getUndoManager();

//    public DesignBean[] getBeansOfType(Class clazz);

//    public Project getProject();

//    // XXX Get rid of this.
//    public Class getBeanClass(String className) throws ClassNotFoundException;

//    public boolean isPage();

//    public boolean isAlive();

//    // XXX Designer shoudn't know about class names at all.
//    public String getImageComponentClassName();

//    // XXX Suspicous this way, provide better interface.
//    public void paintVirtualForms(Graphics2D g, RenderContext renderContext);
//    
//    /** XXX Render Context. */
//    public interface RenderContext {
////        public DesignBean[] getBeansOfType(Class clazz);
//        public Dimension getVieportDimension();
//        public Point getViewportPosition();
//        public int getNonTabbedTextWidth(char[] s, int offset, int length, FontMetrics metrics);
////        public Rectangle getBoundsForDesignBean(DesignBean designBean);
//        public Rectangle getBoundsForComponent(Element componentRootElement);
//    } // End of RenderContext.


    public boolean isFormComponent(Element componentRootElement);

    // XXX
    /** State indicating that a drop is not allowed */
    public static final int DROP_DENIED = 0;
    /** State indicating that the drop is allowed and will cause a link */
    public static final int DROP_PARENTED = 1;
    /** State indicating that the drop is allowed and the bean will be
     *  parented by one of the beans under the cursor */
    public static final int DROP_LINKED = 2;
    // XXX
    public int getDropType(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Transferable t, boolean linkOnly);
//    public int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly);
    public int getDropTypeForComponent(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Element componentRootElement, boolean linkOnly);
    
    public Element getComponentRootElementEquivalentTo(Element oldComponentRootElement);

    public boolean canHighlightComponentRootElmenet(Element componentRootElement);
    
    public boolean moveComponent(Element componentRootElement, Node parentNode, Node before);
    
//    // XXX Get rid of this (after all modifications of model are out of designer).
//    public void setUpdatesSuspended(Element componentRootElement, boolean suspend);
    
    // XXX TEMP How to provide the inline editing correctly.
    public InlineEditorSupport createInlineEditorSupport(Element componentRootElement, String propertyName);

//    public void dumpHtmlMarkupForNode(org.openide.nodes.Node node);

    public void importString(Designer designer, String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement/*, DomProvider.CoordinateTranslator coordinateTranslator*/);

    public boolean importData(Designer designer, JComponent comp, Transferable t, /*Object transferData,*/ Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement/*, DomProvider.CoordinateTranslator coordinateTranslator*/, int dropAction);
    
    // XXX
    public Designer[] getExternalDesigners(URL url);
    public boolean hasCachedExternalFrames();

    
    // XXX TEMP How to provide the inline editing correctly.
    public interface InlineEditorSupport {
        public String getValueSource();
        public boolean isEditingAllowed();
        public void unset();
        public void setValue(String value);
        public String getName();
        public DocumentFragment createSourceFragment();
        public String expandHtmlEntities(String value, boolean warn);
        public Element getRenderedElement();
        
        // XXX AttributeInlineEditor only.
        public String getSpecialInitValue();
        public String getValue();
        public String getDisplayName();
//        public Method getWriteMethod();
        public void setViaWriteMethod(String value);
        public boolean isEscaped();
        public void handleEvent(Event e);
        public void beanChanged();
        public void requestChange();
        public void clearPrerendered();
        public boolean setPrerendered(DocumentFragment fragment);
        public void setStyleParent(DocumentFragment fragment);
        // XXX For now it attaches it to the source document, it should change to the rendered document.
        public DocumentFragment renderDomFragment();
    } // End of InlineEditorSupport.
    
    
    ///////////////////
    // Text support >>>
//    public DomDocument getDomDocument();
    
    // XXX For now in its original bad state
    public interface DomDocument {
        public void addDomDocumentListener(DomDocumentListener listener);
        public void removeDomDocumentListener(DomDocumentListener listener);
        
//        public void insertString(DomPosition domPosition, String content);
        public boolean insertString(Designer designer, DomRange domRange, String content);
        public boolean deleteRangeContents(DomRange domRange);
//        public boolean reparentComponent(Element componentRootElement, DomPosition pos);
        
        public boolean deleteNextChar(Designer designer, DomRange domRange);
        public boolean deletePreviousChar(Designer designer, DomRange domRange);
        
        public void deleteComponents(Element[] componentRootElements);
        
        public String getRangeText(DomRange domRange);

        // TODO Designer should provide listener, informing about user changes.
        public void moveComponents(Designer designer, Box[] boxes, Point[] offstePoints, DomPosition pos, int newX, int newY, boolean snapEnabled);
//        public void moveComponentTo(Box box, int x, int y);
        
        public void resizeComponent(Designer designer, Element componentRootElement, int newX, boolean xMoved, int newY, boolean yMoved,
                int newWidth, boolean widthChanged, int newHeight, boolean heightChanged, Box box, boolean snapEnabled);
        
        public void frontComponents(Box[] boxes);
        public void backComponents(Box[] boxes);
    } // End of DomDocument.
    
    public interface DomPosition {
        
        public final DomPosition NONE = new NoneDomPosition();
        
        public enum Bias {
            FORWARD,
            BACKWARD
        }
        
        public Node getNode();
        public int getOffset();
        public Bias getBias();

        public boolean isEarlierThan(DomPosition domPosition);
        public boolean isLaterThan(DomPosition domPosition);
        public boolean isStrictlyEarlierThan(DomPosition domPosition);
        
        public Element getTargetElement();
        public boolean isInside(Element targetSourceElement);
        
        // XXX Get rid of this, there should be rendered positions only.
        public boolean isRenderedPosition();
        public DomPosition getRenderedPosition();
        public boolean isSourcePosition();
        public DomPosition getSourcePosition();
        
    
        static class NoneDomPosition implements DomPosition {
            
            private NoneDomPosition() {
            }
            
            public Node getNode() {
                return null;
            }

            public int getOffset() {
                return -1;
            }

            public Bias getBias() {
                return Bias.FORWARD;
            }

            public boolean isEarlierThan(DomPosition domPosition) {
                return false;
            }

            public boolean isLaterThan(DomPosition domPosition) {
                return false;
            }

            public boolean isStrictlyEarlierThan(DomPosition domPosition) {
                return false;
            }

            public Element getTargetElement() {
                return null;
            }

            public boolean isInside(Element targetElement) {
                return false;
            }

            public boolean isRenderedPosition() {
                return false;
            }

            public DomPosition getRenderedPosition() {
                return this;
            }

            public boolean isSourcePosition() {
                return false;
            }

            public DomPosition getSourcePosition() {
                return this;
            }
        }; // End of NoneDomPosition.
        
    } // End of DomPosition.
 
    public interface DomRange {
        public DomPosition getDot();
        public DomPosition getMark();

        public void setDot(Node node, int offset, DomPosition.Bias bias);
        public void setMark(Node node, int offset, DomPosition.Bias bias);
        
        public void setRange(Node dotNode, int dotOffset, Node markNode, int markOffset);
        
        public void detach();

        public DomPosition getFirstPosition();
        public DomPosition getLastPosition();

        public boolean isDot(DomPosition dot);
        public boolean isEmpty();
        public boolean isReadOnlyRegion();
    } // End of DomRange.
    
    public interface DomDocumentListener extends EventListener {
        public void insertUpdate(DomDocumentEvent evt);
        public void componentMoved(DomDocumentEvent evt);
        public void componentsMoved(DomDocumentEvent evt);
        public void componentMovedTo(DomDocumentEvent evt);
    } // End of DomDocumentListener.
    
    public interface DomDocumentEvent {
        public DomDocument getDomDocument();
        public DomPosition getDomPosition();
    } // End of DomDocumentEvent.
    
    public DomDocument getDomDocument();
    public int compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB);
    public DomPosition createDomPosition(Node node, int offset, DomPosition.Bias bias);
    public DomPosition createDomPosition(Node node, boolean after);
    public DomRange createDomRange(Node dotNode, int dotOffset, Node markNode, int markOffset);
    public DomPosition first(DomPosition dot, DomPosition mark);
    public DomPosition last(DomPosition dot, DomPosition mark);
    // Text support <<<
    ///////////////////

    public void reuseCssStyle(DomProvider domProvider);

    // XXX Get rid of this. There should be only rendered nodes here.
    public boolean isRenderedNode(Node node);
    
    // XXX Temp till the TopComp move is cleaned up.
//    public void tcUpdateErrors(Designer designer);
//    public void tcDesignContextGenerationChanged(Designer designer);
//    public void tcRequestActive(Designer designer);
    
    // XXX
//    public void tcEnableCutCopyDelete(Designer designer);
//    public void tcDisableCutCopyDelete(Designer designer);
//    public void tcSetActivatedNodes(Designer designer, org.openide.nodes.Node[] nodes);
//    public org.openide.nodes.Node[] tcGetActivatedNodes(Designer designer);
//    public void tcShowPopupMenu(Designer designer, int x, int y);
//    public void tcShowPopupMenu(Designer designer, JPopupMenu popup, int x, int y);
//    public void tcShowPopupMenuForEvent(Designer designer, MouseEvent evt);
    
//    public boolean tcImportComponentData(Designer designer, JComponent comp, Transferable t);
//    public Point tcGetPastePosition(Designer designer);
//    public void tcRepaint(Designer designer);
//    public boolean tcSeenEscape(Designer designer, ActionEvent evt);
    
//    public void tcDeleteSelection(Designer designer);

    // XXX
    public void paintDesignerDecorations(Graphics2D g, Designer designer);

    // Decorations
    // XXX Provider corresponding property in the designer.
    public Decoration getDecoration(Element element);
    // Preferences
    // XXX Rather provide corresponding properties in the Designer.
    public boolean isShowDecorations();
    public int getDefaultFontSize();
    public int getPageSizeWidth();
    public int getPageSizeHeight();
    public boolean isGridShow();
    public boolean isGridSnap();
    public int getGridWidth();
    public int getGridHeight();
    public int getGridTraceWidth();
    public int getGridTraceHeight();
    public int getGridOffset();
}
