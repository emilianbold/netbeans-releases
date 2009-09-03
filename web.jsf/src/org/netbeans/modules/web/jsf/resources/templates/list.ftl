<#if comment>

  TEMPLATE DESCRIPTION:

  This is XHTML template for 'JSF Pages From Entity Beans' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    entityName - name of entity being modified (type: String)
    managedBean - name of managed choosen in UI (type: String)
    managedBeanProperty - name of managed bean property choosen in UI (type: String)
    item - name of property used for dataTable iteration (type: String)
    comment - always set to "false" (type: boolean)
    entityDescriptors - list of beans describing individual entities. Bean has following properties:
        label - field label (type: String)
        name - field property name (type: String)
        dateTimeFormat - date/time/datetime formatting (type: String)
        blob - does field represents a large block of text? (type: boolean)
        relationshipOne - does field represent one to one or many to one relationship (type: boolean)
        relationshipMany - does field represent one to many relationship (type: boolean)
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)

</#if>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://java.sun.com/jsf/facelets"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core">

    <ui:composition template="/template.xhtml">
        <ui:define name="title">
            <h:outputText value="List"></h:outputText>
        </ui:define>
        <ui:define name="body">
        <h:form styleClass="jsfcrud_list_form">
            <h:outputText escape="false" value="(No ${entityName} Items Found)" rendered="${r"#{"}${managedBean}${r".items.rowCount == 0}"}"/>
            <h:panelGroup rendered="${r"#{"}${managedBean}${r".items.rowCount > 0}"}">
                <h:outputText value="Item ${r"#{"}${managedBean}${r".pagination.pageFirstItem + 1}"}..${r"#{"}${managedBean}${r".pagination.pageLastItem + 1}"} of ${r"#{"}${managedBean}${r".pagination.itemsCount}"}"/>&nbsp;
                <h:commandLink action="${r"#{"}${managedBean}${r".previous}"}" value="Previous ${r"#{"}${managedBean}${r".pagination.pageSize}"}" rendered="${r"#{"}${managedBean}${r".pagination.hasPreviousPage}"}"/>&nbsp;
                <h:commandLink action="${r"#{"}${managedBean}${r".next}"}" value="Next ${r"#{"}${managedBean}${r".pagination.pageSize}"}" rendered="${r"#{"}${managedBean}${r".pagination.hasNextPage}"}"/>&nbsp;
                <h:dataTable value="${r"#{"}${managedBeanProperty}${r"}"}" var="${item}" border="0" cellpadding="2" cellspacing="0" rowClasses="jsfcrud_odd_row,jsfcrud_even_row" rules="all" style="border:solid 1px">
<#list entityDescriptors as entityDescriptor>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="${entityDescriptor.label}"/>
                        </f:facet>
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}">
                            <f:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
                        </h:outputText>
    <#else>
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}"/>
    </#if>
                    </h:column>
</#list>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="&nbsp;"/>
                        </f:facet>
                        <h:commandLink action="${r"#{"}${managedBean}${r".prepareView}"}" value="View"/>
                        <h:outputText value=" "/>
                        <h:commandLink action="${r"#{"}${managedBean}${r".prepareEdit}"}" value="Edit"/>
                        <h:outputText value=" "/>
                        <h:commandLink action="${r"#{"}${managedBean}${r".destroy}"}" value="Destroy"/>
                    </h:column>
                </h:dataTable>
            </h:panelGroup>
            <br />
            <h:commandLink action="${r"#{"}${managedBean}${r".prepareCreate}"}" value="Create New ${entityName}"/>
            <br />
            <br />
            <h:commandLink value="Index" action="/index" immediate="true" />
        </h:form>
        </ui:define>
    </ui:composition>

</html>
