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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy.properties;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ActionProducer;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.MouseVisualizer;
import org.openide.explorer.propertysheet.PropertyPanel;

/**
 * Handle sheet button in IDE property sheets. SheetButton extends JPanel
 * and it can show property name, property value or customizer button ("..." button).
 * It is not recommended to use it directly but to use Property class and its
 * descendants.
 *
 * @deprecated JTable used instead of array of SheetButtons. 
 * Use {@link Property} to change value of property.
 */
public class SheetButtonOperator extends JComponentOperator {
    
    /** Index of name button (number of row) */
    private int nameButtonIndex;
    /** Temporary storage */
    private static int temporaryNameButtonIndex;
    
    /** Create new instance. Use getters below to construct a new instance. 
     * @deprecated JTable used instead of array of SheetButtons. 
     * Use {@link Property} to change value of property.
     */
    private SheetButtonOperator(Component button) {
        super((JComponent)button);
    }
    
    /** Waits for sheet button representing specified property name. It sets
     * index of button in sheet.
     * @param contOper where to find button
     * @param propertyName name of property
     * @return  SheetButtonOperator instance
     * @see #getNameButtonIndex()
     * @deprecated JTable used instead of array of SheetButtons. 
     * Use {@link Property} to change value of property.
     */
    public static synchronized SheetButtonOperator nameButton(ContainerOperator contOper,
                                                              String propertyName) {
        SheetButtonOperator sbo =
        new SheetButtonOperator(waitNameButton(contOper, propertyName, 0));
        sbo.nameButtonIndex = temporaryNameButtonIndex;
        return sbo;
    }
    
    /** Waits for index-th sheet button representing property name. It sets
     * index of button in sheet.
     * @param contOper where to find button
     * @param index index of button (row in sheet - starts at 0)
     * @return  SheetButtonOperator instance
     * @see #getNameButtonIndex()
     * @deprecated JTable used instead of array of SheetButtons. 
     * Use {@link Property} to change value of property.
     */
    public static SheetButtonOperator nameButton(ContainerOperator contOper, int index) {
        SheetButtonOperator sbo = new SheetButtonOperator(waitNameButton(contOper, null, index));
        sbo.nameButtonIndex = index;
        return sbo;
    }
    
    /** Waits for sheet button representing value of property. It is identified
     * by index which can be found by name button.
     * @param contOper where to find
     * @param nameButtonIndex index of property in sheet
     * @return  SheetButtonOperator instance
     * @deprecated JTable used instead of array of SheetButtons. 
     * Use {@link Property} to change value of property.
     */
    public static SheetButtonOperator valueButton(ContainerOperator contOper, int nameButtonIndex) {
        return new SheetButtonOperator(waitValueButton(contOper, nameButtonIndex));
    }
    
    /** Waits for customizer button ("..." button) in given ContainerOperator.
     * @param contOper where to find button
     * @return  SheetButtonOperator instance
     * @deprecated JTable used instead of array of SheetButtons. 
     * Use {@link Property} to change value of property.
     */
    public static SheetButtonOperator customizerButton(ContainerOperator contOper) {
        return new SheetButtonOperator(waitCustomizerButton(contOper));
    }

    /** Waits LazyToolTipSheetButton by its name and index. Sets index of
     * property in sheet. */
    private static Component waitNameButton(ContainerOperator contOper, String name, int index) {
        throw new JemmyException("Don't use this! SheetButton no more used in property sheet.");
        /*SheetButtonChooser chooser = new SheetButtonChooser(name, contOper.getComparator());
        Component comp = contOper.waitComponent((Container)contOper.getSource(), chooser, index);
        temporaryNameButtonIndex = chooser.getIndex();
        return comp;*/
    }
    
    /** Waits "..." button which is SheetButton class. */
    private static Component waitCustomizerButton(ContainerOperator contOper) {
        throw new JemmyException("Don't use this! SheetButton no more used in property sheet.");
        /*ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().indexOf("propertysheet.SheetButton") != -1;
            }
            
            public String getDescription() {
                return "SheetButton \"...\"";
            }
        };
        return contOper.waitComponent((Container)contOper.getSource(), chooser);*/
    }
    
    /** Waits value button which is instance of PropertySheetButton. First
     * it finds nameButtonIndex-th PropertyPanel and inside it it tries to find
     * PropertySheetButton.
     */
    private static Component waitValueButton(ContainerOperator contOper, int nameButtonIndex) {
        throw new JemmyException("Don't use this! SheetButton no more used in property sheet.");
        /*ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().indexOf("PropertySheetButton") != -1;
            }
            
            public String getDescription() {
                return "PropertySheetButton";
            }
        };
        Component propertyPanel = waitPropertyPanel(contOper, nameButtonIndex);
        return contOper.waitComponent((Container)propertyPanel, chooser);*/
    }
    
    /** Waits org.openide.explorer.propertysheet.PropertyPanel with given index.
     * PropertyPanel represents container for a value both in editable and
     * non editable states.
     */
    static Component waitPropertyPanel(ContainerOperator contOper, int nameButtonIndex) {
        throw new JemmyException("Don't use this! SheetButton no more used in property sheet.");
        /*ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp instanceof PropertyPanel;
            }
            
            public String getDescription() {
                return "org.openide.explorer.propertysheet.PropertyPanel";
            }
        };
        return contOper.waitComponent((Container)contOper.getSource(), chooser, nameButtonIndex);*/
    }
    
    /** Chooser to find instance LazyToolTipSheetButton by property name.
     * getIndex() method can be used to get index of found button.
     * It is also used by PropertySheetTabOperator. */
    static class SheetButtonChooser implements ComponentChooser {
        private String propertyName;
        private int index = -1;
        private StringComparator comparator;
        
        public SheetButtonChooser(String propertyName, StringComparator comparator) {
            this.propertyName = propertyName;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp.getClass().getName().indexOf("LazyToolTipSheetButton") != -1) {
                index++;
                return comparator.equals(getButtonLabel(comp), propertyName);
            }
            return false;
        }
        
        public String getDescription() {
            return "\""+(propertyName == null ? "index-th" : propertyName)+"\" SheetButton";
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    /** Gets label of given sheet button by reflection call. */
    private static String getButtonLabel(Component sheetButton) {
        try {
            Class clazz = Class.forName("org.openide.explorer.propertysheet.SheetButton");
            Method getLabelMethod = clazz.getDeclaredMethod("getLabel", null);
            getLabelMethod.setAccessible(true);
            return getLabelMethod.invoke(sheetButton, null).toString();
        } catch (Exception e) {
            throw new JemmyException("SheetButton.getLabel() by reflection failed.", e);
        }
    }
    
    /** Gets label of this sheet button.
     * @return  label of this button
     */
    public String getLabel() {
        return getButtonLabel(getSource());
    }
    
    /** Gets index (row) of name button in property sheet. Each row represents
     * one property.
     * @return  index (row) of name button
     */
    public int getNameButtonIndex() {
        return nameButtonIndex;
    }
    
    /** If needed, it scrolls to be this button visible, and clicks on 
     * this button by mouse.
     */
    public void push() {
        // Possibly we don't need to scroll and makeVisible, if we use only
        // setText, setSelectedItem (not typeText, selectItem) in Property 
        // and its descendants
        MouseVisualizer mv = new MouseVisualizer(MouseVisualizer.TOP, 0.5, 10, false);
        mv.scroll(true);
        mv.makeVisible(this);
        clickMouse();
    }
    
    /** If needed, it scrolls to be this button visible, and clicks 
     * on this button by mouse and no block further execution. */
    public void pushNoBlock() {
        try {
            new ActionProducer(new Action() {
                public Object launch(Object obj) {
                    push();
                    return null;
                }
                public String getDescription() {
                    return("Push \""+getLabel()+"\" button - don't wait");
                }
            }, false).produceAction(null);
        } catch (Exception e) {
            throw new JemmyException("Push \""+getLabel()+"\" button action failed.", e);
        }
    }

    /** If needed, it scrolls to be this button visible, and clicks for popup
     * on this button.
     */
    public void clickForPopup() {
        MouseVisualizer mv = new MouseVisualizer(MouseVisualizer.TOP, 0.5, 10, false);
        mv.scroll(true);
        mv.makeVisible(this);
        super.clickForPopup();
    }

}
