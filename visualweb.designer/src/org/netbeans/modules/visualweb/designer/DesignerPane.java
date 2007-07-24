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
package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;

import javax.swing.CellRendererPane;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.openide.util.NbBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


//import javax.swing.plaf.basic.BasicGraphicsUtils;

/** The form designer pane - a subclass of JEditorPane
 * which adds various specializations when editable=true,
 * such as grid mode rendering, component drag & drop handling,
 * etc.
 * <p>
 *
 * @author Tor Norbye
*/
public class DesignerPane extends org.netbeans.modules.visualweb.text.DesignerPaneBase
/*implements PropertyChangeListener, PreferenceChangeListener*/ {
    /** Whether or not to use alpha rendering in the GUI */
    static boolean useAlpha = true;
    private static int adjustX = 0;
    private static int adjustY = 0;

    /**
     * This rectangle indicates the current clipping rectangle used during
     * a paint. The recursive paint operation can bail when outside of
     * this clipping region.
     * May get clobbered by other webforms painting so use only for
     * the duration of a hierarchy paint operation.
     */
    public static final Rectangle clip = new Rectangle();

    /**
     * This point points to the bottom right corner of the clipping
     * rectangle (clip). So it's (clipx+clip.width,clip.y+clip.height).
     * This just simplifies code everywhere doing edge comparisons.
     */
    public static final Point clipBr = new Point(); // br for "bottom right"

    /**
     * This rectangle indicates the dirty region of the screen during
     * laoyut (e.g. needing repaint). We will be calling repaint with
     * this rectangle.
     * May get clobbered by other webforms painting so use only for
     * the duration of a layout hierarchy computation.
     */
    /*static*/private Rectangle dirty = new Rectangle(); // XXX move to PageBox

    /**
     * Flag which controls whether we should try to do smart clipping optimizations.
     * As soon as we're confident that this works it should be made unconditional.
     */
    public static boolean INCREMENTAL_LAYOUT = System.getProperty("rave.designer.noclip") == null; // NOI18N

    /** When true, optimize painting by clipping according to dirty regions only */
    public static final boolean DEBUG_REPAINT = false; // System.getProperty("rave.designer.debugclip") != null;

    // DEBUG:
    // Log info pertaining to component lookup
    //final private static boolean debugcomp = false;
    private DndHandler dndHandler;
    private final DesignerTransferHandler designerTransferHandler;
    
    private boolean grid = false;
    private DropTargetListener gridDropListener = null;
    
    private final WebForm webform;
    
    private FontMetrics metrics;
    private FontMetrics boldMetrics;

    public DesignerPane(final WebForm webform/*, Document document*/) {
        super(/*document*/);
        
        this.webform = webform;

        this.designerTransferHandler = new DesignerTransferHandler(webform);
        
        installActions();
	init();
    }
    
    
    private void installActions() {
        installEscapeAction();
    }
    
    private void installEscapeAction() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape-multiplex"); // NOI18N
        actionMap.put("escape-multiplex", new EscapeAction(this));
    }
    
    private void init() {
        //webform.setPane(this);

        /*
        Insets insets = getInsets();
        adjustX = insets.left;
        adjustY = insets.top;
        */
        setBorder(new EmptyBorder(0, 0, 0, 0));

//        CellRendererPane rendererPane = new CellRendererPane();
        CellRendererPane rendererPane = webform.getRenderPane();
        add(rendererPane);
//        webform.setRenderPane(rendererPane);

        // XXX Now set in designer/jsf.
//        // Set listener.
//        DesignerSettings settings = DesignerSettings.getInstance();
////        settings.addPropertyChangeListener(WeakListeners.propertyChange(this, settings));
//        settings.addWeakPreferenceChangeListener(this);

        setDropTarget(new DesignerPaneDropTarget(this));
        
        dndHandler = new DndHandler(webform);
//        setTransferHandler(dndHandler);
        setTransferHandler(designerTransferHandler);
        
        updateUI();

        //        if (!webform.getModel().isBusted() && 
        //            !webform.getDocument().isGridMode()) {
        //            showCaretAtBeginning();
        //        }
        setGridDropListener(true);
	
	initA11Y();
    }
    
    private void initA11Y() {
	getAccessibleContext().setAccessibleName(
		NbBundle.getMessage(DesignerPane.class, "ACSN_DesignerPane"));
	getAccessibleContext().setAccessibleDescription(
		NbBundle.getMessage(DesignerPane.class, "ACSD_DesignerPane"));
    }

    // We have our own UI for this

    /**
     * Reloads the pluggable UI.  The key used to fetch the
     * new interface is <code>getUIClassID()</code>.  The type of
     * the UI is <code>TextUI</code>.  <code>invalidate</code>
     * is called after setting the UI.
     */
    public void updateUI() {
        fine("Updating UI, ui=" + ui);
        // XXX #105443 To pass the page box.
        // XXX FIXME Move the pageBox field to the DesignerPane (wrong place in UI).
        PageBox pageBox = ui instanceof DesignerPaneUI ? ((DesignerPaneUI)ui).getPageBox() : null;
        fine("pageBox=" + pageBox); // TEMP
        
        //setUI((DesignerPaneUI)UIManager.getUI(this));
        setUI(DesignerPaneUI.createUI(this));
        fine("after update, ui=" + ui);
        
        if (pageBox != null && ui instanceof DesignerPaneUI) {
            ((DesignerPaneUI)ui).setPageBox(pageBox);
        }
        
        invalidate();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        /*
        ((Graphics2D)g).setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        */

//        // Draw "here's what you need to do" explanation on the canvas
//        // when the document is "empty".
//        Document doc = getDocument();
//        if (isDocumentEmpty(doc)) {
        if (isDocumentEmpty(webform.getHtmlBody())) {
            // Document is probably empty. TODO: Find a better and more
            // accurate way to quickly determine this! For now, if you
            // go and remove the title and header tags, you can end up
            // with a nonempty doc that has length < 4!)
            Graphics2D g2d = (Graphics2D)g;
            paintCenteredText(g2d);
        }
    }

    /** Paint the "help" text when the page is empty */
    private void paintCenteredText(Graphics2D g) {
        final int PADDING = 5; // Padding around text that's cleared
        final int LINESPACING = 3; // Extra pixels between text lines

        // This implementation is slow/inefficient, but since it's only run
        // when the page is empty we know we're not busy
        String text;

        if (isGridMode()) {
            text = NbBundle.getMessage(DesignerPane.class, "GridText"); // NOI18N
        } else {
            text = NbBundle.getMessage(DesignerPane.class, "FlowText"); // NOI18N
        }

        int textLines = 1;

        for (int i = 0, n = text.length(); i < n; i++) {
            if (text.charAt(i) == '\n') {
                textLines++;
            }
        }

        int width = getWidth();
        int height = getHeight();

//        DesignerSettings designerSettings = DesignerSettings.getInstance();
//        if (designerSettings.getPageSizeWidth() != -1) {
//            width = designerSettings.getPageSizeWidth();
//            height = designerSettings.getPageSizeHeight();
//        }
        if (webform.getPageSizeWidth() != -1) {
            width = webform.getPageSizeWidth();
            height = webform.getPageSizeHeight();
        }

        int center = height / 2;

        Font font = UIManager.getFont("Label.font"); // NOI18N
        g.setFont(font);

//        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        FontMetrics metrics = DesignerUtils.getFontMetrics(font);
        FontRenderContext frc = g.getFontRenderContext();
        int lineHeight = metrics.getHeight() + LINESPACING;

        int top = center - ((lineHeight * textLines) / 2);

        int minx = width;
        int maxx = 0;
        int nextLine = 0;

        for (int line = 0; line < textLines; line++) {
            int lineEnd = text.indexOf('\n', nextLine);
            String lineText;

            if (lineEnd != -1) {
                lineText = text.substring(nextLine, lineEnd);
                nextLine = lineEnd + 1;
            } else {
                lineText = text.substring(nextLine);
            }

            Rectangle2D bounds1 = font.getStringBounds(lineText, frc);
            int lx = (width - ((int)bounds1.getWidth())) / 2;

            if (lx < minx) {
                minx = lx;
            }

            int xw = lx + (int)bounds1.getWidth();

            if (xw > maxx) {
                maxx = xw;
            }
        }

        // Clear background under text
        Color background = null;

        if (webform.getHtmlBody() != null) {
            // TODO - look up the page box from the ui delegate instead,
            // and ask the page box for its bg - it has properly looked
            // up the background color, not just from the background-color
            // attribute, but the background shorthand property
//            background = CssLookup.getColor(webform.getBody(), XhtmlCss.BACKGROUND_COLOR_INDEX);
            background = CssProvider.getValueService().getColorForElement(webform.getHtmlBody(), XhtmlCss.BACKGROUND_COLOR_INDEX);
        }

        if (background == null) {
            background = getBackground();
        }

        g.setColor(background);

        int miny = top;
        int maxy = top + (textLines * lineHeight);
        g.fillRect(minx - PADDING, miny, maxx - minx + (2 * PADDING), maxy - miny + (2 * PADDING));

        // Draw text
        g.setColor(webform.getColors().gridColor);
        nextLine = 0;

        int y = top + (2 * PADDING); // XXX change to padding constant
        y += (metrics.getHeight() - metrics.getDescent());

        for (int line = 0; line < textLines; line++) {
            int lineEnd = text.indexOf('\n', nextLine);
            String lineText;

            if (lineEnd != -1) {
                lineText = text.substring(nextLine, lineEnd);
                nextLine = lineEnd + 1;
            } else {
                lineText = text.substring(nextLine);
            }

            Rectangle2D bounds1 = font.getStringBounds(lineText, frc);
            int lx = (width - ((int)bounds1.getWidth())) / 2;

            g.drawString(lineText, lx, y);
            y += lineHeight;
        }
    }

    void setGridMode(boolean on) {
        if (grid == on) {
            return;
        }

        grid = on;

        webform.getSelection().clearSelection(true);

        // Cursor depends on mode: in grid mode, show pointer, in flow mode,
        // show insert/text cursor.
        if (on) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }

        webform.getManager().setInsertBox(null, null);

        // Always do it, so we can drop on grid regions as well
        //setGridDropListener(on);
        // Unselect text before switching, since there's no
        // caret to move once you switch to grid mode.
//        Position pos = getCaretPosition();
        DomPosition pos = getCaretPosition();
        
        select(pos, pos);

        if (on) {
//            if (getCaret() != null) {
//                setCaret(null);
//            }
            hideCaret();
        } else {
            showCaretAtBeginning();
        }

        if (isShowing()) {
            invalidate();
            revalidate();
            getParent().validate();
            repaint();
        }
    }

    private void setGridDropListener(boolean on) {
        // The drop handler depends on the mode. In flow mode, we can
        // use Swing's default (which I think is
        // BasicTextUI.TextDropTargetListener). In grid mode, use our
        // own, since we need to know the pixel position at the drop.
        // TODO: we've gotta "remove" the builtin/flow one as well, it's
        // intefering on Windows (I see cursor flicker) and on OSX
        // it's downright breaking things.
        DropTarget dropTarget = getDropTarget();

        if (dropTarget != null) {
            if (on) {
                if (gridDropListener == null) {
                    gridDropListener = new DesignerDropHandler(this);
                }

                try {
                    dropTarget.addDropTargetListener(gridDropListener);
                } catch (TooManyListenersException tmle) {
                    // should not happen... swing drop target is multicast
                    tmle.printStackTrace();
                }
            } else {
                if (gridDropListener != null) {
                    dropTarget.removeDropTargetListener(gridDropListener);
                }
            }
        }
    }

    /**
     * Position the caret at the beginning of the document (and show the caret
     * too, if necessary)
     */
    public void showCaretAtBeginning() {
//        Position pos = ModelViewMapper.getFirstDocumentPosition(webform, true);
        DomPosition pos = ModelViewMapper.getFirstDocumentPosition(webform, true);

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            hideCaret();
        } else {
            showCaret(pos);
        }
    }

    public void hideCaret() {
//        DesignerCaret dc = getCaret();

//        if (dc != null) {
        if (hasCaret()) {
            setCaret(null);
        }
    }

//    public void showCaret(Position dot) {
    public void showCaret(DomPosition dot) {
//        if (dot == Position.NONE) {
        if (dot == DomPosition.NONE) {
            hideCaret();

            return;
        }

//        if (getCaret() == null) {
//            DesignerCaret dc = getPaneUI().createCaret();
//            setCaret(dc);
//        }
        if (!hasCaret()) {
            createCaret();
        }

//        setCaretPosition(dot);
        setCaretDot(dot);
    }

    /*
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if (debugpaints) {
            if (getWidth()/2 < width && getHeight()/2 < height) {
                System.out.print("  repaint request(size=" + ((width*height)/1000) + " kbpix2, x="  + x + ", y=" + y + ", w=" + width + ",h=" + height + ") - ");

                Throwable t = new Throwable();
                t.fillInStackTrace();
                StackTraceElement stack[] = t.getStackTrace();
                int frames = 0;
                for (int i = 1; i < stack.length; i++) {
                    StackTraceElement caller = stack[i];
                    String className = caller.getClassName();
                    if (!className.startsWith("com.sun.rave.")) {
                        continue;
                    }
                    className = className.substring(className.lastIndexOf('.')+1);
                    String methodName = caller.getMethodName();
                    System.out.print("-> " + className + "." + methodName + "():" + caller.getLineNumber());
                    frames++;
                    if (frames == 2) {
                        break;
                    }
                }
                System.out.println("");
            }
        }
    }
     */
    public void repaint() {
        super.repaint();

        if (DEBUG_REPAINT) {
            System.out.print("FULL REPAINT REQUEST: ");

            Throwable t = new Throwable();
            t.fillInStackTrace();

            StackTraceElement[] stack = t.getStackTrace();
            int frames = 0;

            for (int i = 1; i < stack.length; i++) {
                StackTraceElement caller = stack[i];
                String className = caller.getClassName();

                if (!className.startsWith("com.sun.rave.")) {
                    continue;
                }

                className = className.substring(className.lastIndexOf('.') + 1);

                String methodName = caller.getMethodName();
                System.out.print("-> " + className + "." + methodName + "():" +
                    caller.getLineNumber());
                frames++;

                if (frames == 3) {
                    break;
                }
            }

            System.out.println("");
        }
    }

    /** Return true if this editor pane is using grid mode */
    public boolean isGridMode() {
        return grid;
    }

    /** Return distance from the top left corner to the top left view
     * canvas. This is the amount that mouse positions need to get corrected
     * for in order for pixels in the web page space to correspond to mouse
     * coordinates.  Since this distance may be different between the
     * horizontal and the vertical directions, separate methods provide
     * these distances.
     */
    public static int getAdjustX() {
        return adjustX;
    }

    /** Return distance from the top left corner to the top left view
     * canvas. This is the amount that mouse positions need to get corrected
     * for in order for pixels in the web page space to correspond to mouse
     * coordinates.  Since this distance may be different between the
     * horizontal and the vertical directions, separate methods provide
     * these distances.
     */
    public static int getAdjustY() {
        return adjustY;
    }

    public WebForm getWebForm() {
        return webform;
    }

    public DesignerPaneUI getPaneUI() {
        DesignerPaneUI ui = (DesignerPaneUI)getUI();

        return ui;
    }

    /*
    public void addNotify() {
        System.out.println("addNotify: updating viewport");
        super.addNotify();
        DesignerPaneUI ui = (DesignerPaneUI)getUI();
        ui.updateViewport();
    }

    public void removeNotify() {
        System.out.println("removeNotify: updating viewport");
        super.removeNotify();
    }
    */
    public void updateViewport() {
        DesignerPaneUI ui = (DesignerPaneUI)getUI();
        ui.updateViewport();
    }

    /** Return the drag & drop handler associated with this pane */
    public DndHandler getDndHandler() {
        return dndHandler;
    }

    /** Return the default font to use */
    public FontMetrics getMetrics() {
        if (metrics == null) {
            Font font = getFont();
//            metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            metrics = DesignerUtils.getFontMetrics(font);
        }

        return metrics;
    }

    /** Return the bold font to use */
    public FontMetrics getBoldMetrics() {
        if (boldMetrics == null) {
            Font font = getFont();
            font = font.deriveFont(Font.BOLD);
//            boldMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            boldMetrics = DesignerUtils.getFontMetrics(font);
        }

        return boldMetrics;
    }

    public PageBox getPageBox() {
        return ((DesignerPaneUI)getUI()).getPageBox();
    }

    /**
     * Return true if the rectangle spanning from (x,y) to (x2,y2) is completely
     * outside the clipping rectangle (so can be culled).  Note it's assumed that
     * x < x2 and y < y2.
     */
    public static boolean isOutsideClip(int x, int y, int x2, int y2) {
        return ((clip != null) && (y2 < clip.y)) || (x2 < clip.x) || (clipBr.y < y) ||
        (clipBr.x < x);
    }

    /**
     * Clear the dirty rectangle such that the next addDirtyPoint
     * call will establish a corner point for the new dirty rectangle.
     * (In particular, we don't just want to do "new Rectangle()" followed
     * by some addDirtyPoint calls because that will include the initial "point"
     * from the new Rectangle() at (0,0) so we'll end up with a dirty rectangle
     * from (0,0) to (maxx, maxy) instead of a dirty rectangle from
     * (minx,miny) to (maxx,maxy).
     */
    /*public static*/ void clearDirty() {
        dirty = null;
    }

    /** Add the given point to the dirty rectangle. */
    /*public static*/ void addDirtyPoint(int x, int y) {
        if (dirty == null) {
            dirty = new Rectangle(x, y, 0, 0);
        } else {
            dirty.add(x, y);
        }
    }

    /** Add the given rectangle to the dirty rectangle. */
    /*public static*/ void addDirtyRectangle(int x, int y, int w, int h) {
        if (dirty == null) {
            dirty = new Rectangle(x, y, w, h);
        } else {
            dirty.add(x, y);
            dirty.add(x + w + 1, y + h + 1);
        }
    }

    /**
     * Request a repaint for the regions marked dirty since the last repaintDirty or
     * clearDirty calls. Note that the the dirty rectangle is shared among all
     * instances of DesignerPane so use for self contained contiguous blocks of code only.
     * @param force Has no effect if a dirty rectangle has been initialized, but if
     *   dirty is null, it will force a full repaint if force is true, otherwise it
     *   will do nothing.
     */
    public void repaintDirty(boolean force) {
        if (dirty != null) {
            if (DEBUG_REPAINT) {
                System.out.println("Requesting a repaint using a dirty region of " + dirty);
            }

            repaint(dirty);
            clearDirty();
        } else if (force) {
            repaint();
        }
    }
    
    /** Indicates whether the document is "empty",
     * A document is considered empty if it does not contain a body tag,
     * or if it contains any tags below the body which is not a <XXXform> tag
     * (typically <h:form>) or more than one form tag (<XXXform>).
     * Also, the form tag can not contain any other elements. */
    private static boolean isDocumentEmpty(/*Document doc*/ Element body) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(DesignerUtils.class.getName() + ".isDocumentEmpty(Document)");
        }
//        if(doc == null) {
//            throw(new IllegalArgumentException("Null document"));
//        }
	// TODO Better would be if document can tell whether it is modified or not (comparing
	// to the original template).
	
//	WebForm webform = doc.getWebForm();
	
//        RaveElement b = webform.getBody();
//        Element b = webform.getHtmlBody();
        Element b = body;

        if (b == null) {
            return true;
        }

        if (!b.getTagName().equals(HtmlTag.BODY.name)) {
            // Document fragments
            return false;
        }

        // XXX #6347037, when background is set, don't draw the text.
        // When the attribute is invalid, then the text is missing,
        // but seems to be better then the having text when there is a background.
        if(b.hasAttribute(HtmlAttribute.BACKGROUND)) {
            return false;
        }
        // Operate on the source DOM nodes since it's harder to know what kinds
        // of random junk will be rendered into the HTML... such as hidden
        // inputs etc.
//        if (b.isRendered()) {
//            b = b.getSource();
//        }
        
        // It is the rendered element (retrieved from #getHtmlBody).
//        if (MarkupService.isRenderedNode(b)) {
            b = MarkupService.getSourceElementForElement(b);
//        }
        // XXX Possible NPE (experienced with fragment).
        if (b == null) {
            return false;
        }

        NodeList list = b.getChildNodes();
        int len = list.getLength();

	int formsCount = 0;
	
        for (int i = 0; i < len; i++) {
            Node child = list.item(i);

            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element form = (Element)child;

                String tag = form.getTagName();

                if (tag.equals(HtmlTag.BR.name)) {
                    // Ok to have a couple of <br's> - we put those in ourselves
		    continue;
                } else if (!tag.endsWith("form")) { // NOI18N
                    return false;
                }

		// The tag is form:
		formsCount++;
		if(formsCount > 1) {
		    // #6330716 When other form is added, the doc is considered non-empty.
		    return false;
		}
		
                if (form.getChildNodes().getLength() != 0) {
                    NodeList list2 = form.getChildNodes();
                    int len2 = list2.getLength();

                    int brs = 0;

                    for (int j = 0; j < len2; j++) {
                        Node child2 = list2.item(j);

                        if (child2.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            // Accept a couple of <br>'s since we put those there
                            // ourselves
                            if (HtmlTag.BR.name.equals(((Element)child2).getTagName())) {
                                brs++;

                                if (brs > 2) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else if (child2.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                            if (!DesignerUtils.onlyWhitespace(child2.getNodeValue())) {
                                return false;
                            }
                        }
                    }
                }
            } else if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                if (!DesignerUtils.onlyWhitespace(child.getNodeValue())) {
                    return false;
                }
            }
        }

        return true;
    }

    
    /** Adding listing of <code>CssBox</code>es to standard <codde>Component</code>s list.
     * @see java.awt.Container#list(java.io.PrintStream, int) */
    public void list(PrintStream out, int indent) {
	super.list(out, indent);
        
        PageBox pageBox = getPageBox();
        if(pageBox == null) {
            return;
        }
        pageBox.list(out, indent + 1);
    }

    /** Adding listing of <code>CssBox</code>es to standard <codde>Component</code>s list.
     * @see java.awt.Container#list(java.io.PrintWriter, int) */
    public void list(PrintWriter out, int indent) {
	super.list(out, indent);
        
        PageBox pageBox = getPageBox();
        if(pageBox == null) {
            return;
        }
        pageBox.list(out, indent + 1);
    }

////    public void propertyChange(PropertyChangeEvent evt) {
//    public void preferenceChange(PreferenceChangeEvent evt) {
////        if (DesignerSettings.PROP_SHOW_DECORATIONS.equals(evt.getPropertyName())) {
//        if (DesignerSettings.PROP_SHOW_DECORATIONS.equals(evt.getKey())) {
//            // XXX maybe better to have a copy of the value and in that case to reset it.
//            repaint();
//        }
//    }
  

    class DesignerDropHandler implements DropTargetListener {
        Component pane;
        Cursor org_cursor;

        public DesignerDropHandler(Component pane) {
            this.pane = pane;
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            // XXX #6245208 To ignore CnC when DnD is in progress.
//            InteractionManager.setIgnoreCnC(true);
            webform.getManager().setIgnoreCnC(true);
            
            if (webform.isGridMode()) {
                ImageIcon imgIcon =
                    new ImageIcon(this.getClass().getResource("/org/netbeans/modules/visualweb/designer/resources/drop_position.gif")); // TODO get marquee icon
//                StatusDisplayer_RAVE.getRaveDefault().setPositionLabelIcon(imgIcon);
                org_cursor = pane.getCursor();
                pane.setCursor(DragSource.DefaultCopyDrop);
            }
        }

        public void dragExit(DropTargetEvent dte) {
            // XXX #6245208 We can't differenciate between ESC and plain drag exit here.
////            InteractionManager.setIgnoreCnC(false);
//            webform.getManager().setIgnoreCnC(false);
            
            pane.setCursor(org_cursor);
            dndHandler.clearDropMatch();
        }

        public void dragOver(DropTargetDragEvent dtde) {
            Point p = dtde.getLocation();

//            // Annoyingly we can't get the transferable out of the drag event
//            // (we need it to get the list of classnames being dropped so
//            // we can check if the drop is permitted etc.) so use a
//            // transferable registry hack instead:
//            Transferable transferable = DndHandler.getActiveTransferable();
            // #6457267.
            Transferable transferable = dtde.getTransferable();

            if (transferable != null) {
                try {
                    int dropType = webform.getManager().updateDropState(p, false, transferable);

                    // TODO - consult the drop action too and see if applicable! Use
                    // DndHandler.computeActions to compute bitmask to compare with
                    if (dropType == DndHandler.DROP_DENIED) {
                        dtde.rejectDrag();
                    } else {
                        // TODO - get the actual permitted actions used in dropType
                        // computations and do bitwise compare with dtde.getDropAction()
                        // such that we for example correctly indicate if you're
                        // permitted to link if that's what you're trying to do.
                        dtde.acceptDrag(dtde.getDropAction());
                    }
                } catch (Throwable t) {
                    log(t);
                }
            }

            //StatusDisplayer.getDefault().setStatusText("(" + p.x + ", " + p.y + ")");
            // TODO : compute the component position itself, not the mouse position
//            StatusDisplayer_RAVE.getRaveDefault().setPositionLabelText(p.x + "," + p.y);
        }

        public void drop(DropTargetDropEvent dtde) {
            // XXX #6245208 To ignore CnC.
//            InteractionManager.setIgnoreCnC(false);
            webform.getManager().setIgnoreCnC(false);
            
            // Grid mode: stash away the drag position for the transfer
            // handler
//            assert getTransferHandler() == dndHandler;

            // XXX #6482668 This has to be after updating the drop state.
//            dndHandler.clearDropMatch();

            // Determine if you're dropping on a grid area
            // or on a flow area
            Point ep = dtde.getLocation();
//            webform.getManager().updateDropState(ep, true, DndHandler.getActiveTransferable());
            // XXX #6457267. Revise this part.
            webform.getManager().updateDropState(ep, true, null);

            dndHandler.setDropAction(dtde.getDropAction());

            dndHandler.clearDropMatch();

            /*
            if (getTransferHandler() instanceof DndHandler) {
                DndHandler handler =
                    (DndHandler)getTransferHandler();
                handler.setDropPoint(dtde.getLocation());
            }
             */
//            StatusDisplayer_RAVE.getRaveDefault().clearPositionLabel();
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
    }

    
    /** Horrible hack necessary because I get notified of the escape key
     * twice: sometimes by my key handler above, sometimes by the default key
     * handler, and sometimes by both!
     * @todo This may no longer be an issue now that I'm using
     *   a better input map (ANCESTOR_OF.... instead of FOCUSED_)
     */
    private long lastEscape = -1;    
    private boolean seenEscape(long when) {
        if (lastEscape == when) {
            return true;
        }

        lastEscape = when;

        return false;
    }
    
    public void escape(long when) {
        // XXX Revise this handling, it is very suspicious.
        if (!seenEscape(when)) {
//            webform.performEscape();
            webform.getManager().getMouseHandler().escape();
        }
    }
    
    private abstract static class DesignerPaneAction extends AbstractAction {
        private final DesignerPane designerPane;
        
        public DesignerPaneAction(DesignerPane designerPane) {
            this.designerPane = designerPane;
        }
        
        protected DesignerPane getDesignerPane() {
            return designerPane;
        }
    } // End of JsfTopComponentAction.
    
    
    /** */
    private static class EscapeAction extends DesignerPaneAction {
        public EscapeAction(DesignerPane designerPane) {
            super(designerPane);
        }
        public void actionPerformed(ActionEvent evt) {
            getDesignerPane().escape(evt.getWhen());
        }
    } // End of EscapeAction.

    
    private static void fine(String message) {
        Logger logger = getLogger();
        logger.fine(message);
    }
    
    private static void log(Throwable throwable) {
        Logger logger = getLogger();
        logger.log(Level.INFO, null, throwable);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(DesignerPane.class.getName());
    }
}
