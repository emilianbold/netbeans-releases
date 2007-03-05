function Scheduler(id, fieldId) { 
    this.dateFieldId = fieldId;
    this.setDateValue = scheduler_setDateValue; 
} 

function scheduler_setDateValue(value) {
    field_setValue(this.dateFieldId, value); 
} 
