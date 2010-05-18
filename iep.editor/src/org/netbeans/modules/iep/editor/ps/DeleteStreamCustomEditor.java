package org.netbeans.modules.iep.editor.ps;

import java.awt.Component;
import java.util.logging.Logger;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.ReadOnlyNoExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.openide.explorer.propertysheet.PropertyEnv;

public class DeleteStreamCustomEditor extends DefaultCustomEditor {

    private static final Logger mLog = Logger.getLogger(DeleteStreamCustomEditor.class.getName());

    /** Creates a new instance of DeleteStreamCustomEditor */
    public DeleteStreamCustomEditor() {
        super();
    }

    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }

    private static class MyCustomizer extends DefaultCustomizer {

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }

        @Override
        protected SelectPanel createSelectPanel(OperatorComponent component) {
            return new MySelectPanel(component);
        }

        class MySelectPanel extends SelectPanel {

            public MySelectPanel(OperatorComponent component) {
                super(component);
            }

            @Override
            protected DefaultMoveableRowTableModel createTableModel() {
                return new ReadOnlyNoExpressionDefaultMoveableRowTableModel();
            }
        }
    }
}