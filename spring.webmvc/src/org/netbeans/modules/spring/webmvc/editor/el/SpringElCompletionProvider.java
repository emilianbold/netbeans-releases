
package org.netbeans.modules.spring.webmvc.editor.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.completion.api.ElCompletionItem;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author alexeybutenko
 */
public class SpringElCompletionProvider implements CompletionProvider{

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new CCQuery(),
                    component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    static final class CCQuery extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int offset) {
            final FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = null;
            if (fObject != null)
                wm = WebModule.getWebModule(fObject);
            if (wm != null){
                SpringElExpression elExpr = new SpringElExpression(doc);
                final ArrayList<CompletionItem> complItems = new ArrayList<CompletionItem>();
                int elParseType = elExpr.parse(offset);
                final int anchor = offset - elExpr.getReplace().length();

                switch (elParseType){
                    case SpringElExpression.EL_START:
                        final String replace = elExpr.getReplace();
                        SpringScope scope = SpringScope.getSpringScope(fObject);
                        for (SpringConfigModel model : scope.getAllConfigModels()) {
                            try {
                                final boolean[] isDone = new boolean[]{false};
                                model.runReadAction(new Action<SpringBeans>() {

                                    @Override
                                    public void run(SpringBeans beans) {
                                            for (SpringBean bean : beans.getBeans()) {
                                               String beanName = null;
                                               for(String name : bean.getNames()) {
                                                    beanName = name;
                                                    break;
                                               }
                                               if (beanName == null) {
                                                   beanName = bean.getId();
                                               }
                                               String className = bean.getClassName();
                                               if ((beanName != null) && beanName.startsWith(replace)) {
                                                   complItems.add(new SpringBeanCompletionItem(beanName, anchor, className));
                                               }
                                            }
                                            isDone[0]=true;
                                    }
                                });
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                        }
                        break;

                    case SpringElExpression.SPRING_BEAN:
                        List<CompletionItem> items = elExpr.getPropertyCompletionItems(
                                elExpr.getObjectClass(), anchor);
                        complItems.addAll(items);
                        items = elExpr.getMethodCompletionItems(
                                elExpr.getObjectClass(), anchor);
                        complItems.addAll(items);
                        break;
                }//switch
                resultSet.addAllItems(complItems);
            }
            resultSet.finish();
        }

    }

    private static class SpringBeanCompletionItem extends ElCompletionItem.ELBean {
        private static final String BEAN_PATH = "org/netbeans/modules/spring/beans/resources/spring-bean.png";  //NOI18N

        public SpringBeanCompletionItem(String text, int substitutionOffset, String type) {
            super(text, substitutionOffset, type);
        }

        @Override
        public int getSortPriority() {
            return 100;
        }

        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon(BEAN_PATH, false);
        }
    }
}
