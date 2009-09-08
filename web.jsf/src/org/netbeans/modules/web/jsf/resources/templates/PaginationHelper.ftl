<#if comment>

  TEMPLATE DESCRIPTION:

  This is Java template for 'JSF Pages From Entity Beans' PaginationHelper class. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    packageName - package name (String)
    comment - always (Boolean; always FALSE)

  This template is accessible via top level menu Tools->Templates and can
  be found in category JavaServer Faces->JSF from Entity.

</#if>

package ${packageName};

import javax.faces.model.DataModel;

public abstract class PaginationHelper {

    private int pageSize;
    private int page;

    public PaginationHelper(int pageSize) {
        this.pageSize = pageSize;
    }

    public abstract int getItemsCount();

    public abstract DataModel createPageDataModel();

    public int getPageFirstItem() {
        return page*pageSize;
    }

    public int getPageLastItem() {
        int i = getPageFirstItem() + pageSize -1;
        int count = getItemsCount();
        if (i > count) {
            i = count;
        }
        return i;
    }

    public boolean isHasNextPage() {
        return (page+1)*pageSize+1 <= getItemsCount();
    }

    public void nextPage() {
        if (isHasNextPage()) {
            page++;
        }
    }

    public boolean isHasPreviousPage() {
        return page > 0;
    }

    public void previousPage() {
        if (isHasPreviousPage()) {
            page--;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

}
