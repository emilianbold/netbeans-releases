/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 */ 

/*
 * TableItem.java
 *
 * Created on July 26, 2005, 1:01 PM
 *
 */

package org.netbeans.microedition.lcdui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import org.netbeans.microedition.lcdui.laf.ColorSchema;
import org.netbeans.microedition.lcdui.laf.SystemColorSchema;
import org.netbeans.microedition.lcdui.laf.TableColorSchema;

/**
 * An item that visualizes a table from <code>TableModel</code>.
 * <p/>
 * The table can be larger than a screen in both  directions - user can use
 * a cursor to scroll in all directions.
 * @author breh
 */
public class TableItem extends CustomItem implements TableModelListener {

    public static final int VERTICAL_SELECTION_MODE = 1;
    public static final int HORIZONTAL_SELECTION_MODE = 2;
    
    private static final boolean DEBUG = false;
    
    
    private int tableRows = 0; // count of table rows (from model)
    private int tableCols = 0; // count of table columns (from model)
    
    private String title = null; // title of the table
    
    private boolean borders = true; // is using borders?
    private boolean usingHeaders = false; // is using usingHeaders (column names)?
    
    private final Display display; // /display used to get system colors
    
    private static final Font STATIC_TEXT_FONT = Font.getFont(Font.FONT_STATIC_TEXT); // standard font for items to draw text
    
    // default font for title - using standard font with Bold style
    private static final Font DEFAULT_TITLE_FONT = Font.getFont(STATIC_TEXT_FONT.getFace(),
                                STATIC_TEXT_FONT.getStyle() | Font.STYLE_BOLD,
				STATIC_TEXT_FONT.getSize());;
	
    // default font for title - using standard font with Bold style
    private static final Font DEFAULT_HEADERS_FONT = Font.getFont(STATIC_TEXT_FONT.getFace(),
                                STATIC_TEXT_FONT.getStyle() | Font.STYLE_BOLD,
                                STATIC_TEXT_FONT.getSize());;	
															
    // values are using standard font
    private static final Font DEFAULT_VALUES_FONT = STATIC_TEXT_FONT;
	
    private Font titleFont = DEFAULT_TITLE_FONT; // titleFont
    private Font headersFont = DEFAULT_HEADERS_FONT; // usingHeaders (column names) font
    private Font valuesFont = DEFAULT_VALUES_FONT; // values font
    
    private TableModel model; // model
    
    private int[] colWidths; // array of individual column widths
    private int[] rowHeights; // array of individual row height - currently using the same height for all
    private int totalColWidth; // total width of all columns
    
    private int defaultCellWidth, defaultCellHeight; // default cell width/heights
    
    private static final int CELL_PADDING = 2; // size text cell padding
    private static final int DOUBLE_CELL_PADDING = 2*CELL_PADDING; // double cell padding helper for computation
    private static final int BORDER_LINE_WIDTH = 1; // size of border line
    
    //private boolean alwaysDrawTitle = false; // always draw title even the table is scrolled // not used in this release
    //private boolean alwaysDrawHeaders = true; // always draw usingHeaders (column names) // not used in this release
    
    private boolean cursorOn = false; // should cursor be drawn?
    private int cursorCellX, cursorCellY; // coordinates of cursor
    
    private int viewCellX = 0; // X value of a cell which is visible on the left
    private int viewCellX2 = 0; // X value of a cell which is visible on the right
    
    private boolean tableFitsHorizontally = true; // does the whole table fit on the screen?
    
    private boolean firstPaint = true;
    
    private int sizeWidth = 0;   // current size assigned to this item - some implementations (Nokia) still
    private int sizeHeight = 0;  // keeps calling sizeChanged() with the same size, so I keep these values
    // to not call repaint() when it is not neccessary

    private int selectionMode = HORIZONTAL_SELECTION_MODE | VERTICAL_SELECTION_MODE;
    
    
    private ColorSchema colorSchema; // color schema in use
    private TablePaintStrategy paintStrategy;
    
    /**
     * Creates a new instance of <code>TableItem</code> without any model.
     * @param display non-null display parameter.
     *
     * @param label label for the item
     * @throws java.lang.IllegalArgumentException if the display parameter is null
     */
    public TableItem(Display display, String label) throws IllegalArgumentException {
        this(display, label, null);
    }
    
    
    /**
     * Creates a new instance of <code>TableItem</code> with a model.
     *
     * @param display non-null display parameter.
     * @param label label for the item
     * @param model a <code>TableModel</code> to be visualized by this item
     * @throws java.lang.IllegalArgumentException if the display parameter is null
     */
    public TableItem(Display display, String label, TableModel model) throws IllegalArgumentException {
        this(display,label,model,null);
    }
    
    
    /**
     * Creates a new instance of <code>TableItem</code> with a model.
     *
     * @param display non-null display parameter.
     * @param label label for the item
     * @param model a <code>TableModel</code> to be visualized by this item
     * @param colorSchema a color schema to be used. If set to null, SystemCOlorSchema will be used
     * @throws java.lang.IllegalArgumentException if the display parameter is null
     */
    public TableItem(Display display, String label, TableModel model, ColorSchema colorSchema) throws IllegalArgumentException {
        super(label);
        
        if (display == null) throw new IllegalArgumentException("display parameter cannot be null");
        this.display = display;
        
        
        updateDefaultCellSize();
        if (model != null) {
            setModel(model);
        } else {
            recomputeModelValues();
        }
        setColorSchemaImpl(display,colorSchema);
        
    }

    /**
     * Sets color schema to be used with this component. If set to null
     * SystemColorSchema will be used.
     * <code>colorSchema</code> argument could have TableColorSchema type.
     * In the latter case fucntionality of TableColorSchema will be applied.
     * @param colorSchema  color schema
     */
    public void setColorSchema(ColorSchema colorSchema) {
        setColorSchemaImpl(display,colorSchema);
        repaint();
    }    
    
    /**
     * Gets color schema currently in use
     */
    public ColorSchema getColorSchema() {
        return colorSchema;
    }
    
    
    /**
     * Gets title of the table.
     * @return title string or null if there is no title defined
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets title of the table. The difference between title and label
     * specified in the constructor is, that the title appears in the table (it
     * is surrounded by the borders). Title can be null if no title should
     * be shown.
     * <p/>
     * The table is automatically repainted when a new title is set.
     * @param tableTitle title for the table. Can be null
     */
    public void setTitle(String tableTitle) {
        if (this.title != tableTitle) {
            this.title = tableTitle;
            repaint();
        }
    }
    
    /**
     * Should the table use borders
     * @return true if the table will be painted with borders. False if it will be
     * borderless.
     */
    public boolean isBorders() {
        return borders;
    }
    
    /**
     * Sets state whether the table should be visualized with or without borders.
     *  <p/>
     * The table is automatically repainted when the state is changed.
     * @param showBorders true if the borders should be painted, false otherwise
     */
    public void setBorders(boolean showBorders) {
        if (this.borders != showBorders) {
            this.borders = showBorders;
            repaint();
        }
    }
    
    
    /**
     * Sets a new model to the table. The table gets automatically repainted
     * accordingly to a new model.
     * @param model a new model to be visualized - cannot be null
     * @throws java.lang.IllegalArgumentException if the model parameter is null
     */
    public void setModel(TableModel model) throws IllegalArgumentException {
        if (this.model != null) {
            this.model.removeTableModelListener(this);
        }
        if (model == null) throw new IllegalArgumentException("model parameter cannot be null");
        if (model != null) {
            if (model.getRowCount() < 0) throw new IllegalArgumentException("model cannot have negative number of rows");
            if (model.getColumnCount() < 0) throw new IllegalArgumentException("model cannot have negative number of cols");
            model.addTableModelListener(this);
        }
        this.model = model;
        recomputeModelValues();        
        invalidateTable();
    }
    
    
    
    /**
     * Gets font used to paint the table title
     * @return title font
     */
    public Font getTitleFont() {
        return titleFont;
    }
    
    /**
     * Sets the font to be used to paint title. If null is specified,
     * the default font (bold version of static text font) will be used.
     * <p/>
     * When the font changes, the table gets automatically repainted.
     * @param titleFont font to be used for painting table title, might be null.
     */
    public void setTitleFont(Font titleFont) {
        if (this.titleFont != titleFont) {
            if (titleFont != null) {
                this.titleFont = titleFont;
            } else {
                this.titleFont = DEFAULT_TITLE_FONT;
            }
            repaint();
        }
    }
    
    /**
     * Gets font used to paint the table cell values
     * @return values font
     */
    public Font getValuesFont() {
        return valuesFont;
    }
    
    /**
     * Sets the font to be used to paint title. If null is specified,
     * the default font (static text font) will be used.
     * <p/>
     * When the font changes, the table gets automatically repainted.
     * @param valuesFont font used for painting values, might be null
     */
    public void setValuesFont(Font valuesFont) {
        if (this.valuesFont != valuesFont) {
            if (valuesFont != null) {
                this.valuesFont = valuesFont;
            } else {
                this.valuesFont = DEFAULT_VALUES_FONT;
            }
            updateDefaultCellSize();
            recomputeModelValues();
            repaint();
        }
    }
    
    
    /**
     * Gets font used to paint the column names (headers) of the table
     * @return headers font
     */
    public Font getHeadersFont() {
        return headersFont;
    }
    
    /**
     * Sets the font to be used to paint title. If null is specified,
     * the default font (bold static text font) will be used.
     * <p/>
     * When the font changes, the table gets automatically repainted.
     * @param headersFont font used for painting column names (headers), might be null
     */
    public void setHeadersFont(Font headersFont) {
        if (this.headersFont != headersFont) {
            if (headersFont != null) {
                this.headersFont = headersFont;
            } else {
                this.headersFont = DEFAULT_HEADERS_FONT;
            }
            repaint();
        }
    }
    
    
    /**
     * Gets the row position of the cursor in the table.
     * @return selected cell row
     */
    public int getSelectedCellRow() {
        return cursorCellY;
    }
    
    
    /**
     * Gets the column position of the cursor in the table.
     * @return selected cell column
     */
    public int getSelectedCellColumn() {
        return cursorCellX;
    }

    /**
     * Gets the currunt selection mode.
     * @return current selection mode
     */
    public int getSelectionMode(){
        return selectionMode;
    }

    /**
     * Sets selection mode.
     * @param  selection mode.
     */
    public void setSelectionMode( int mode){
        selectionMode = mode;
        repaint();
    }
        /*
        private static Font getSafeFont(Font font) {
                return font == null ? Font.getDefaultFont() : font;
        }*/
    
    
    /**
     * implementation of the abstract method
     * @return minimal content height
     */
    protected int getMinContentHeight() {
        int sum = (tableRows * (defaultCellHeight + BORDER_LINE_WIDTH)) + BORDER_LINE_WIDTH;
        if (title != null) {
            sum += getTitleFont().getHeight() + DOUBLE_CELL_PADDING + BORDER_LINE_WIDTH;
        }
        if (usingHeaders) {
            sum += getHeadersFont().getHeight() + DOUBLE_CELL_PADDING + BORDER_LINE_WIDTH;
        }
        /*
        if (getLabel() != null) { // this is rather hack - I don't know the size of the label !!!
            sum += Font.getDefaultFont().getHeight();
        }*/
        return sum;
    }
    
    
    
    protected int getMinContentWidth() {
        if (DEBUG) System.out.println("!!!!!!!!!!!!!!!!!! TableItem.getMinContentWidth(): XXXX returing -1");
        return -1;
    }
    

    
    /**
     * implementation of the abstract method
     * @param width
     * @return preferred contnent height
     */
    protected int getPrefContentHeight(int width) {
        if (DEBUG) System.out.println("TableItem.getPrefContentHeight: tentative width="+width);
        return getMinContentHeight();
    }
    
    /**
     * implementation of the abstract method
     * @param height 
     * @return preferred content width
     */
    protected int getPrefContentWidth(int height) {
        if (DEBUG) System.out.println("WTableItem.getPrefContentWidth: tentative heigth="+height);
        final Displayable currentDisplayable = display.getCurrent();
        final int displayWidth = currentDisplayable == null ? -1 : currentDisplayable.getWidth();
        if (DEBUG) System.out.println("TableItem.getPrefContentWidth(): Current display width = "+displayWidth);
        int sum = colWidths.length * BORDER_LINE_WIDTH + BORDER_LINE_WIDTH;
        int result = 0;
        for (int i=0; i < colWidths.length; i++) {
            sum += colWidths[i];
        }
        result = sum;        
        if (title != null) {
            int titleWidth = getTitleFont().stringWidth(title);
            if (titleWidth > result) {
                result = titleWidth + DOUBLE_CELL_PADDING;
            }
        }
        /*
        if (getLabel() != null) { // this is rather hack - I don't know the size of the label !!!
            int labelWidth = Font.getDefaultFont().stringWidth(getLabel());
            if (labelWidth > result) {
                result = labelWidth;
            }
        }
        if (DEBUG) System.out.println("TableItem.getPrefContentWidth(): label result= "+result);
         */
        // I should never return a width larger than the display width
        if ((displayWidth > 0) && (result > displayWidth)) {
            result = displayWidth;
        }
        if (DEBUG) System.out.println("TableItem.getPrefContentWidth():  returning preffered width = "+result);
        return result;        
    }
    
    /**
     * implementation of the abstract method
     * @param g
     * @param width
     * @param height
     */
    protected void paint(Graphics g, int width, int height) {
        
        //if (doPaint) {
        firstPaint = false;
        
        if (DEBUG) System.out.println("\n@@@@@@@@@@@@@@ paint: width="+width+" height="+height+""+"::: "+this);
        if (DEBUG) System.out.println("@@@@@@@@@@@@@@ paint: Clip: X="+g.getClipX()+" Y="+g.getClipY()+" W="+g.getClipWidth()+" H="+g.getClipHeight());
        
        final int paintWidth = sizeWidth > 0 ? sizeWidth : width;
        final int paintHeight = sizeHeight > 0 ? sizeHeight : height;
        
        tableFitsHorizontally = totalColWidth <= paintWidth; // does the table horizontally fit to the given drawing area
        if (DEBUG) System.out.println("@@@@@@@@@@@@@@ paint: totalColWidth = "+totalColWidth+", paintWidth="+paintWidth);
        
        boolean rightmostColumnFullyVisible = true; // is the table scrolled to the right (i.e. the rightmost column is fully visible?)
        
        int currentColor = g.getColor();
        
        // paint the background based on color schema
        getColorSchema().paintBackground(g, false);
        // clear the area
        //g.setColor(getColorSchema().getColor(Display.COLOR_BACKGROUND));
        //g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
        //g.fillRect(0, 0, width, height);
        
        int hy = paintHeight - 1; // actual height ?
        int wx = paintWidth - 1; // actual width ?
        
        // get height of title
        int titleHeight = 0;
        if (title != null) {
            titleHeight = getTitleFont().getHeight() + DOUBLE_CELL_PADDING; // title height
        }
        
        // get height of headers
        int headersHeight = 0;
        if (usingHeaders) {
            headersHeight =  getHeadersFont().getHeight() + DOUBLE_CELL_PADDING; // headers height
        }
        
        int viewWidth = wx; // current visible width of the table
        
        int actualTableWidth = paintWidth; // actual table width (based on cell computation), used to draw borders
        
        if ((model != null) && (tableRows != 0) && (tableCols != 0)) {
            
            // update visible width of the table based on columns to be drawn
            //viewWidth = wx;
            if (viewCellX2 < (tableCols - 1)) {
                viewWidth = wx;
            } else {
                viewWidth = 0;
                for (int i=viewCellX; (i <= viewCellX2) && (i < tableCols); i++) {
                    viewWidth+= colWidths[i];
                }
                if (viewWidth > wx) {
                    viewWidth = wx;
                }
            }
            
            // paint cursor
            if (cursorOn) {
                int x = getCursorX();
                int y = getCursorY();
                
                int yAddon = 0;
                // add title height if to be drawn
                if (title != null) {
                    yAddon+= titleHeight + BORDER_LINE_WIDTH;
                }
                // add headers height if to be drawn
                if (usingHeaders) {
                    yAddon += headersHeight + BORDER_LINE_WIDTH;
                }
                
                y+=yAddon;
                
                int w = colWidths[cursorCellX];
                int h = defaultCellHeight;
                if ( selectionMode == VERTICAL_SELECTION_MODE ){
                    x= BORDER_LINE_WIDTH;
                    w = getCellX(model.getColumnCount()) -BORDER_LINE_WIDTH;
                }
                else if ( selectionMode == HORIZONTAL_SELECTION_MODE ){
                    y = BORDER_LINE_WIDTH +yAddon;
                    h = getCellY( model.getRowCount() )-BORDER_LINE_WIDTH;
                }
                //g.setColor(cursorColor);
                // draw cursor ...
                g.setColor(getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND));
                
                if ( (selectionMode & VERTICAL_SELECTION_MODE) != 0 || 
                        (selectionMode & HORIZONTAL_SELECTION_MODE) != 0){
                    g.fillRect(x, y,  w, h);
                }
            }
            
            
            int y = 0; // y coordinate to be used to draw headers/values
            // add title height if to be drawn
            if (title != null) {
                y += titleHeight + BORDER_LINE_WIDTH;
            }
            // add headers height if to be drawn
            
            // draw headers
            if (usingHeaders) {
                //g.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
                g.setFont(getHeadersFont());
                
                int x = BORDER_LINE_WIDTH;
                final int gy = y + CELL_PADDING + BORDER_LINE_WIDTH; // actual y used to be draw the text (icludes padding)
                int heightCell = headersHeight;
                int yCell = y;
                if ( !isBorders() ){
                    heightCell = heightCell +BORDER_LINE_WIDTH;
                    yCell-=BORDER_LINE_WIDTH;
                }
                
                for (int j=viewCellX; j < model.getColumnCount(); j++) {
                    viewCellX2 = j;
                    
                    final Object value = model.getColumnName(j);
                    final int colWidth = colWidths[j];

                    int xCell = x;
                    int widthCell = colWidth;
                    
                    if ( !isBorders() ){
                        xCell = x - BORDER_LINE_WIDTH;
                        widthCell = widthCell + BORDER_LINE_WIDTH;
                    }
                    getPaintStrategy().drawHeaderBackground( g , j , 
                            xCell, yCell , widthCell, heightCell );
                    int headerColor = getPaintStrategy().getForegroundHeaderColor( j );
                    g.setColor( headerColor );
                   
                    
                    if (value != null) {
                        g.drawString(value.toString(), x+colWidth/2, gy, Graphics.TOP | Graphics.HCENTER);
                    }
                    x += colWidth + BORDER_LINE_WIDTH;
                    if (x > paintWidth) {
                        rightmostColumnFullyVisible = false;
                        break;
                    }
                }
                y += headersHeight + BORDER_LINE_WIDTH;
            }
            
            //  draw values
            //g.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
            g.setFont(getValuesFont());
            
            int heightCell = defaultCellHeight;
            if ( !isBorders() ){
                heightCell = heightCell +BORDER_LINE_WIDTH;
            }
            for (int i=0; (i < model.getRowCount()); i++) {
                
                int x = BORDER_LINE_WIDTH + CELL_PADDING;
                final int gy = y + CELL_PADDING + BORDER_LINE_WIDTH; // actual y used to be draw the text (icludes padding)
                int xCell = BORDER_LINE_WIDTH;
                int yCell = y;
                if ( !isBorders() ){
                    yCell-=BORDER_LINE_WIDTH;
                }
                
                for (int j=viewCellX; j < model.getColumnCount(); j++) {
                    viewCellX2 = j;
                    Object value = model.getValue(j,i);
                    
                    if (value != null) {
                        int widthCell = colWidths[j];
                        
                        if ( !isBorders() ){
                            xCell = xCell - BORDER_LINE_WIDTH;
                            widthCell = widthCell + BORDER_LINE_WIDTH;
                        }
                        boolean highlightBg = false;
                        if ( cursorOn ) {
                            if ( selectionMode == (VERTICAL_SELECTION_MODE |HORIZONTAL_SELECTION_MODE)
                                    && i == cursorCellY && j == cursorCellX )
                            {
                                //g.setColor(getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
                                g.setColor( getPaintStrategy().getColor( j, i , Display.COLOR_HIGHLIGHTED_FOREGROUND));
                                g.drawString(value.toString(), x, gy, Graphics.TOP | Graphics.LEFT);
                                //g.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
                                highlightBg = true;
                            }
                            else if ( selectionMode == VERTICAL_SELECTION_MODE && 
                                    i==cursorCellY )
                            {
                                g.setColor( getPaintStrategy().getColor( j, i , Display.COLOR_HIGHLIGHTED_FOREGROUND));
                                g.drawString(value.toString(), x, gy, Graphics.TOP | Graphics.LEFT);
                                highlightBg = true;
                            }
                            else if ( selectionMode == HORIZONTAL_SELECTION_MODE && 
                                    j==cursorCellX )
                            {
                                g.setColor( getPaintStrategy().getColor( j, i , Display.COLOR_HIGHLIGHTED_FOREGROUND));
                                g.drawString(value.toString(), x, gy, Graphics.TOP | Graphics.LEFT);
                                highlightBg = true;
                            }
                        }
                        if ( !highlightBg ) {
                            getPaintStrategy().drawCell( g, j, i , xCell, yCell , widthCell, 
                                    heightCell , Display.COLOR_BACKGROUND);
                            g.setColor( getPaintStrategy().getColor( j, i , Display.COLOR_FOREGROUND));
                            g.drawString(value.toString(), x, gy, Graphics.TOP | Graphics.LEFT);
                        }
                    }
                    x += colWidths[j] + BORDER_LINE_WIDTH;
                    xCell = x - CELL_PADDING;
                    if (x > width) {
                        rightmostColumnFullyVisible = false;
                        break;
                    }
                }
                y += defaultCellHeight + BORDER_LINE_WIDTH;
                yCell = y- CELL_PADDING;
            }
            
            
            
            // finally draw borders (if applicable)
            if (isBorders()) {
                g.setColor(getColorSchema().getColor(Display.COLOR_BORDER));
                int currentStrokeStyle = g.getStrokeStyle();
                g.setStrokeStyle(display.getBorderStyle(false));
                
                y = titleHeight;
                
                int totalTableHeight = tableRows * (defaultCellHeight + BORDER_LINE_WIDTH) + titleHeight; // total totalTableHeight
                if (usingHeaders) {
                    totalTableHeight += headersHeight + BORDER_LINE_WIDTH; // add it to total table height
                }
                
                // vertical lines
                int x = 0;
                for (int i=viewCellX; (i < tableCols) && (x < width); i++) {
                    x+=colWidths[i] + BORDER_LINE_WIDTH;
                    g.drawLine(x, titleHeight, x, totalTableHeight);
                    actualTableWidth = x; // getting value of the leftmost table line - out actualTableWidth
                }
                if ((titleHeight > 0) && rightmostColumnFullyVisible) {
                    g.drawLine(x, 0, x, titleHeight);
                }
                
                // horizontal lines
                
                // first line on the top
                g.drawLine(0,0,actualTableWidth,0);
                
                // draw header line
                if (usingHeaders) {
                    g.drawLine(0, y, actualTableWidth, y);
                    y += headersHeight + BORDER_LINE_WIDTH;
                }
                
                // draw value lines
                for (int i=0; (i <= tableRows) && (y <= totalTableHeight); i++, y+=defaultCellHeight+BORDER_LINE_WIDTH) {
                    g.drawLine(0, y, actualTableWidth, y);
                }
                
                
                
                
                
                // draw the remaining left and right line
                g.drawLine(0, 0,  0, totalTableHeight);
                g.setStrokeStyle(currentStrokeStyle);
            }
        }
        
        // draw title
        if (title != null) {
            g.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
            g.setFont(titleFont);
            // first is table title
            g.drawString(title, actualTableWidth/2, BORDER_LINE_WIDTH + CELL_PADDING, Graphics.TOP | Graphics.HCENTER);
            int half = titleHeight / 2;
            if (viewCellX > 0) {
                // draw triangle on the left
                g.drawLine(2, half, 5, half - 2);
                g.drawLine(2, half, 5, half + 2);
            }
            if (! rightmostColumnFullyVisible) {
                // draw triangle on the right
                int rx = actualTableWidth - 2;
                g.drawLine(rx, half, rx - 3, half - 2);
                g.drawLine(rx, half, rx - 3, half + 2);
            }
        }
        
        g.setColor(currentColor);
                /*} else {
                        if (DEBUG) System.out.println("\n@@@@@@@@@@@@@@ NO PAINT: width="+width+" height="+height+"");
                        if (DEBUG) System.out.println("@@@@@@@@@@@@@@ NO PAINT : Clip: X="+g.getClipX()+" Y="+g.getClipY()+" W="+g.getClipWidth()+" H="+g.getClipHeight());
                }*/
    }
    
    /**
     * implementation of the abstract method - if the item size has changed, simply repaint the table
     * @param w
     * @param h
     */
    protected void sizeChanged(int w, int h) {
        if (DEBUG) System.out.println("^^^^^^^^^^ sizeChanged : w="+w+" h="+h+" ::: "+this);
        if ((! firstPaint) && (w > 0) && (h > 0) && ( w != sizeWidth) && ( h != sizeHeight )) {
            sizeWidth = w;
            sizeHeight = h;
            //repaint();
        }
    }
    
    /**
     * implementation of the abstract method
     */
    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
        if (DEBUG) System.out.println("\n****************************\nTraversal occured: dir = "+dir+" ::: "+this);
        if (DEBUG) System.out.println("traverse:viewportWidth = "+viewportWidth+", viewportHeight = "+viewportHeight);
        if (DEBUG) System.out.println("traverse:visRect = ["+visRect_inout[0]+","+visRect_inout[1]+","+visRect_inout[2]+","+visRect_inout[3]+"]");
        if (DEBUG) System.out.println("traverse: cursorCellX = "+cursorCellX+", cursorCellY = "+cursorCellY);
        final int visibleWidth = visRect_inout[2];
        final int visibleHeight = visRect_inout[3];
               
        
        if (model == null) {
            // model is not set - do not traverse ...
            if (DEBUG) System.out.println("traverse: model is not set - returning\n\n");
            return false;
        } // else
        boolean retValue = false; // what should be returned - traversal occured (true), did not occured (false)
        boolean repaint = false; // should be the component repainted?
        if ( !cursorOn) {
            if (DEBUG) System.out.println("traverse: cusorOn was false, entering item ..., dir = "+dir);
            //cursorCellX = 0;
            if (dir == Canvas.UP) {
                cursorCellY = tableRows - 1;
            } else if (dir == Canvas.DOWN) {
                cursorCellY = 0;
            } else if (dir == Canvas.RIGHT) {
                cursorCellY = 0;
                cursorCellX = 0;
            } else if (dir == Canvas.LEFT) {
                cursorCellY = tableRows - 1;
                cursorCellX = tableCols - 1;
                viewCellX2 = cursorCellX;
            } else {
                // this can happen only when a traverseIn was called
                // after traverseOut
                // do not move cursor - simply return back to the item and repaint
            }
            cursorOn = true; // cursor is on !!!
            retValue = true;
            repaint = true;
        } else {
            // move cursor
            if (dir == Canvas.UP) {
                cursorCellY--;
            } else if (dir == Canvas.DOWN) {
                cursorCellY++;
            } else if (dir == Canvas.LEFT) {
                cursorCellX--;
            } else if (dir == Canvas.RIGHT) {
                cursorCellX++;
            }
            // now check if we are inbounds
            if (cursorCellX < 0) {
                cursorCellX = 0;
                retValue =  true;
                repaint = false;
            } else if (cursorCellX >= tableCols) {
                cursorCellX = tableCols - 1;
                retValue =  true;
                repaint = false;
            }
            if ((cursorCellY >= 0) && (cursorCellY < tableRows)) {
                if (DEBUG) System.out.println("traverse: cursorY in bounds");
                retValue = true;
                repaint = true;
            } else {
                if (DEBUG) System.out.println("traverse: cursorY out of bounds");
                retValue = false;
                repaint = false;
                if (cursorCellY < 0) {
                    cursorCellY = 0;
                } else {
                    cursorCellY = tableRows - 1;
                }
            }
        }
                
        
        int cursorY = getCursorY();
        int cursorHeight = defaultCellHeight;
        int headersHeight = 0;
        if (title != null) {
            headersHeight += getTitleFont().getHeight() + DOUBLE_CELL_PADDING + BORDER_LINE_WIDTH;
        }
        // add headers height if to be drawn
        if (usingHeaders) {
            headersHeight += getHeadersFont().getHeight() + DOUBLE_CELL_PADDING + BORDER_LINE_WIDTH;
        }
        
        //if ((cursorY + cursorHeight + headersHeight) > viewportHeight) {
        // y
        visRect_inout[1] = cursorY + headersHeight + 2 * BORDER_LINE_WIDTH;
        visRect_inout[3] = defaultCellHeight;
        // x
        visRect_inout[0] = getCursorX();
        visRect_inout[2] = defaultCellWidth;
                /*} else {
                        //visRect_inout[1] = 0;
                        visRect_inout[0] = getCursorX();
                        visRect_inout[2] = defaultCellWidth;
                        //visRect_inout[3] = viewportHeight;
                        visRect_inout[3] = defaultCellHeight;
                }*/
        
        
        
                /*
                 visRect_inout[1] = getCursorY() + defaultCellHeight;
                int horizontalShift = 0;
                if (title != null) {
                        horizontalShift = getTitleFont().getHeight() + CELL_PADDING + BORDER_LINE_WIDTH;
                }
                if (usingHeaders) {
                        horizontalShift += getHeadersFont().getHeight() + CELL_PADDING + BORDER_LINE_WIDTH;
                }
                if (dir == Canvas.UP) {
                        visRect_inout[1] -= horizontalShift;
                } else if (dir == Canvas.DOWN) {
                        visRect_inout[1] += horizontalShift;
                }
                 
                // visRect_inout[2] = colWidths[cursorCellX]; // we don't care about X
                visRect_inout[3] = defaultCellHeight;
                 */
        if (DEBUG) System.out.println("traverse: tableFits = "+tableFitsHorizontally);
        if (! tableFitsHorizontally) {
            repaint = true;
            if (cursorCellX >= viewCellX2) {
                int sum = BORDER_LINE_WIDTH + CELL_PADDING;;
                int i = viewCellX2;
                while ((sum <= visibleWidth) && (i >= 0)) {
                    sum += colWidths[i] + BORDER_LINE_WIDTH;
                    i--;
                }
                viewCellX = i + 2;
//				if (viewCellX >= tableCols) {
//					viewCellX = tableCols - 1;
//				}
                
                if (viewCellX > viewCellX2) {
                    viewCellX = cursorCellX;
                }
            } else if (cursorCellX < viewCellX) {
                viewCellX --;
            }
        }
        if (repaint) {
            repaint();
        }
        if (DEBUG) System.out.println("traverse: returning: "+retValue+", cursorCellX = "+cursorCellX+", cursorCellY = "+cursorCellY);
        if (DEBUG) System.out.println("traverse: returning visRect = ["+visRect_inout[0]+","+visRect_inout[1]+","+visRect_inout[2]+","+visRect_inout[3]+"]+\n\n");
        return retValue;
    }
    
    /**
     * implementation of the abstract method
     */
    protected void traverseOut() {
        if (DEBUG) System.out.println("---------  traverseOut !!! ::: "+this);
        super.traverseOut();
        cursorOn = false;
        repaint();
    }
    
    
    
    
    /*****
     *
     * private methods
     *
     ******/
    
    private int getCursorX() {
        return getCellX(cursorCellX);
    }
    
    private int getCellX( int column) {
        int x = BORDER_LINE_WIDTH;
        for (int i=viewCellX; i < column; i++) {
            x += colWidths[i] + BORDER_LINE_WIDTH;
        }
        return x;
    }
    
    private TablePaintStrategy getPaintStrategy(){
        return paintStrategy;
    }
    
    
    private int getCursorY() {
        return getCellY(cursorCellY);
    }
    
    private int getCellY( int row ) {
        return row * (defaultCellHeight + BORDER_LINE_WIDTH);
    }
    
    /**
     * recomputes cell sizes based on set data and fonts
     */
    private void recomputeModelValues() {
        if (model != null) {
            tableRows = model.getRowCount();
            tableCols = model.getColumnCount();
        } else {
            tableCols = 0;
            tableRows = 0;
            usingHeaders = false;
        }
        // cells
        colWidths = new int[tableCols];
        for (int i=0; i < tableCols; i++) {
            colWidths[i] = defaultCellWidth;
        }
        if (model != null) {
            // values
            final int columnCount = model.getColumnCount();
            final int rowCount = model.getRowCount();
            for (int i=0; i < columnCount; i++) {
                for (int j=0; j < rowCount; j++) {
                    Object value = model.getValue(i, j);
                    if (value != null) {
                        int width = getValuesFont().stringWidth(value.toString()) + DOUBLE_CELL_PADDING;
                        if (width > colWidths[i]) {
                            colWidths[i] = width;
                        }
                    }
                }
            }
            // column header (they might be bigger)
            usingHeaders = model.isUsingHeaders();
            if (model.isUsingHeaders()) {
                for (int i=0; i < columnCount; i++) {
                    String columnName = model.getColumnName(i);
                    if (columnName != null) {
                        int width = getHeadersFont().stringWidth(columnName.toString()) + DOUBLE_CELL_PADDING;
                        if (width > colWidths[i]) {
                            colWidths[i] = width;
                        }
                    }
                }
            }
        }
        
        // compute total column widthj
        totalColWidth = BORDER_LINE_WIDTH;
        for (int i=0; i < colWidths.length; i++) {
            totalColWidth += colWidths[i] + BORDER_LINE_WIDTH;
        }
        
    }
    
        /*
         * recomputes base defaultCellWidth, defaultCellHeight based on current values font
         */
    private void updateDefaultCellSize() {
        defaultCellWidth = getValuesFont().stringWidth("X") + DOUBLE_CELL_PADDING;
        defaultCellHeight = getValuesFont().getHeight() + DOUBLE_CELL_PADDING;
    }
    
    
    /**
     * Sets color schema. If null, creates a new SystemColorSchema based on the display
     */
    private void setColorSchemaImpl(Display display, ColorSchema colorSchema) {
        if (colorSchema != null) {
            this.colorSchema = colorSchema;
        } else {
            this.colorSchema = SystemColorSchema.getForDisplay(display);
        }
        if ( this.colorSchema instanceof TableColorSchema ){
            paintStrategy = new TableColorSchemaStrategy( 
                    (TableColorSchema)this.colorSchema );
        }
        else {
            paintStrategy = new BaseColorSchemaStrategy( this.colorSchema );
        }
        
        
    }
    
    /**
     * Workaround for SE phones - they throw NPE from invalidate() method call,
     * when the CustomItem component hasn't been added to the form, therefore 
     * it does not have any sizes set.
     */
    private void invalidateTable() {
        if ((sizeWidth > 0) && (sizeHeight > 0)) {
            invalidate();
        }
    }
    
    
    /**
     * Listener for changes of the model. Just repaints the table when
     * any change happened to the table model of this table.
     * @param changedModel
     */
    public void tableModelChanged(TableModel changedModel) {
        if (changedModel == model) {
            recomputeModelValues();
            invalidateTable();
            //repaint();
        }
    }
    
}
