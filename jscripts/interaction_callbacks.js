function deleteRow(tableID,currentRow) {
    try {
		if (tableID=="Delete_all") {
			invokeJava("delete_all",0);
		} else if (tableID=="Interaktionen") {
			var table = document.getElementById(tableID);
			var rowCount = table.rows.length;		
			for (var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				if (row==currentRow.parentNode.parentNode) {
					// Call java
					invokeJava("delete_row",row.cells[1].innerText);
					// Delete row				
					table.deleteRow(i);		
					// Update counters
					rowCount--;
					i--;
				}
			}
        }
    } catch (e) {
        // alert(e);
    }
}