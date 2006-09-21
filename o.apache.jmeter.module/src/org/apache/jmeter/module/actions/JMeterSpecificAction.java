package org.apache.jmeter.module.actions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import org.apache.jmeter.module.cookies.JMeterCookie;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;

public final class JMeterSpecificAction extends CookieAction implements Presenter.Popup {
  
  protected void performAction(Node[] activatedNodes) {
//    JMeterCookie c = (JMeterCookie) activatedNodes[0].getCookie(JMeterCookie.class);
//    // TODO use c
  }
  
  protected int mode() {
    return CookieAction.MODE_EXACTLY_ONE;
  }
  
  public String getName() {
    return NbBundle.getMessage(JMeterSpecificAction.class, "CTL_JMeterSpecificAction");
  }
  
  protected Class[] cookieClasses() {
    return new Class[] {
      JMeterCookie.class
    };
  }
  
  protected void initialize() {
    super.initialize();
    // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
    putValue("noIconInMenu", Boolean.TRUE);
  }
  
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }
  
  protected boolean asynchronous() {
    return false;
  }
  
  public JMenuItem getPopupPresenter() {
    JMenu retValue = new JMenu(getName());
        
    Node[] nodes = getActivatedNodes();
    try {
      if (nodes.length > 0) {
        JMeterCookie cookie = (JMeterCookie)nodes[0].getCookie(JMeterCookie.class);
        JPopupMenu menu = JMeterIntegrationEngine.getDefault().getElementMenu(cookie.getElement());
        
        boolean submenuAdded = false;
        for(MenuElement submenu : menu.getSubElements()) {
          if (submenu instanceof JMenu) {
            retValue.add(submenu.getComponent());
            submenuAdded = true;
          }
        }
        
        if (!submenuAdded) {
          return null;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
//
//    return rootItem;
    return retValue;
  }
}

