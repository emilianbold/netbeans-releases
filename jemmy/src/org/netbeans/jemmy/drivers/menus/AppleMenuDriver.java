/*
 * AppleMenu.java
 *
 * Created on October 4, 2005, 6:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.jemmy.drivers.menus;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.PathChooser;
import org.netbeans.jemmy.drivers.input.RobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author shura
 */
public class AppleMenuDriver extends RobotDriver implements MenuDriver {
    
    /**
     * Creates a new instance of AppleMenuDriver
     */
    public AppleMenuDriver() {
        super(new Timeout("apple.system.menu.delay", 100),
                new String[] {"org.netbeans.jemmy.operators.JMenuBarOperator"});
    }
    
    public Object pushMenu(ComponentOperator oper, PathChooser chooser) {
        Timeout maxTime = oper.getTimeouts().create("ComponentOperator.WaitComponentTimeout");
        JMenuBar bar = (JMenuBar)(oper.getSource());
        activateMenu(bar);
        System.out.println(getSelectedElement(bar));
        MenuElement menuObject;
        maxTime.start();
        while(!chooser.checkPathComponent(0, (menuObject = getSelectedElement(bar)))) {
            pressKey(KeyEvent.VK_RIGHT, 0);
            releaseKey(KeyEvent.VK_RIGHT, 0);
            if(maxTime.expired()) {
                throw(new TimeoutExpiredException("AppleMenuDriver: can not find an appropriate menu!"));
            }
        }
        for(int depth = 1; depth < chooser.getDepth(); depth++) {
            int elementIndex = getDesiredElementIndex(menuObject, chooser, depth);
            if(elementIndex == -1) {
                throw(new JemmyException("Unable to find menu (menuitem): " + chooser.getClass() + " at " + depth + " depht."));
            }
            System.out.println(elementIndex);
            for(int i = ((depth == 1) ? 0 : 1); i<=elementIndex; i++) {
                pressKey(KeyEvent.VK_DOWN, 0);
                releaseKey(KeyEvent.VK_DOWN, 0);
            }
            if(depth == chooser.getDepth() - 1) {
                pressKey(KeyEvent.VK_ENTER, 0);
                releaseKey(KeyEvent.VK_ENTER, 0);
                return(null);
            } else {
                pressKey(KeyEvent.VK_RIGHT, 0);
                releaseKey(KeyEvent.VK_RIGHT, 0);
                menuObject = menuObject.getSubElements()[0].getSubElements()[elementIndex];
            }
        }
        return(null);
    }
    
    private void activateMenu(JMenuBar bar) {
        if(getSelectedElement(bar) == null) {
            tryToActivate();
            if(getSelectedElement(bar) == null) {
                tryToActivate();
            }
        }
    }
    
    private void tryToActivate() {
        moveMouse(0, 0);
        pressMouse(Operator.getDefaultMouseButton(), 0);
        releaseMouse(Operator.getDefaultMouseButton(), 0);
        pressKey(KeyEvent.VK_RIGHT, 0);
        releaseKey(KeyEvent.VK_RIGHT, 0);
        pressKey(KeyEvent.VK_RIGHT, 0);
        releaseKey(KeyEvent.VK_RIGHT, 0);
    }
    
    private static MenuElement getSelectedElement(MenuElement bar) {
        MenuElement[] subElements = bar.getSubElements();
        for(int i = 0; i < subElements.length; i++) {
            if(subElements[i] instanceof JMenu &&
                    ((JMenu)subElements[i]).isSelected()) {
                return(subElements[i]);
            } else if(subElements[i] instanceof JMenuItem &&
                    ((JMenuItem)subElements[i]).isSelected()) {
                return(subElements[i]);
            }
        }
        return(null);
    }
    
    private static int getDesiredElementIndex(MenuElement bar, PathChooser chooser, int depth) {
        MenuElement[] subElements = bar.getSubElements()[0].getSubElements();
        for(int i = 0; i < subElements.length; i++) {
            if(chooser.checkPathComponent(depth, subElements[i])) {
                return(i);
            }
        }
        return(-1);
    }
}
