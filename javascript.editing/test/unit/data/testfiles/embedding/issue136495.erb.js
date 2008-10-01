
  YAHOO.util.Event.addListener(window, "load", function() {
    YAHOO.example.EnhanceFromMarkup = new function() {
      var myColumnDefs = [
        {key:"name",label:"Name"},
        {key:"obs_rollup",label:"OBS Rollup"},
 __UNKNOWN__ 
          {key:" __UNKNOWN__ ",label:" __UNKNOWN__ "},
 __UNKNOWN__ 
          {key:"totals_to_date",label:"Project Totals to Date"},
          {key:"remaining_hours",label:"Remaining Hours"},
          {key:"budgeted_hours",label:"Budgeted Hours"}
        ];

        this.myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("charge"));
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
        this.myDataSource.responseSchema = {
          fields: [
            {key:"name"},
            {key:"obs_rollup"},
 __UNKNOWN__ 
              {key:" __UNKNOWN__ "},
 __UNKNOWN__ 
              {key:"totals_to_date"},
              {key:"remaining_hours"},
              {key:"budgeted_hours"}
            ]
          };

          this.myDataTable = new YAHOO.widget.DataTable("actualsData", myColumnDefs, this.myDataSource,
          {sortedBy:{key:"name",dir:"desc"}}
        );
        };
      });

