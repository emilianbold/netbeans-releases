/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package controller;

import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import model.Item;

/**
 *
 * @author carol mcdonald
 */
@ManagedBean
@SessionScoped
public class Catalog implements Serializable {

    @EJB
    private ItemFacade itemFacade;
    private Item item = null;
    private List<Item> items = null;
    private PagingInfo pagingInfo = null;

    /**
     * Creates a new instance of Catalog
     */
    public Catalog() {
        pagingInfo = new PagingInfo();
    }

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(getItemCount());
        }
        return pagingInfo;
    }

    public List<Item> getNextItems(int maxResults, int firstResult) {
        return itemFacade.findRange(maxResults, firstResult);
    }

    public List<Item> getItems() {
        if (items == null) {
            getPagingInfo();
            items = getNextItems(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return items;
    }

    public Item getItem() {
        return item;

    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "list";
    }

    public String showDetail(Item item) {
        this.item = item;
        return "detail";
    }

    public int getItemCount() {
        return itemFacade.getItemCount();
    }

    private void reset(boolean resetFirstItem) {
        item = null;
        items = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }
}



