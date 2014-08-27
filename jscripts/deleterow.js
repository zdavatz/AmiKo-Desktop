function deleteRow(tableID,currentRow) {
    try {
		if (tableID=="Delete_all") {
			invokeJava("Delete all",0);
		} else if (tableID=="Interaktionen") {
			var table = document.getElementById(tableID);
			var rowCount = table.rows.length;		
			for (var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				if (row==currentRow.parentNode.parentNode) {
					// Call java
					invokeJava(row.cells[1].innerText,rowCount);
					// Delete row				
					table.deleteRow(i);		
					// Update counters
					rowCount--;
					i--;
				}
			}
        } else if (tableID=="Warenkorb") {
			var table = document.getElementById(tableID);
			var rowCount = table.rows.length;		
			for (var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				if (row==currentRow.parentNode.parentNode) {
					// Call java
					invokeJava(row.cells[1].innerText,rowCount);
					// Delete row				
					table.deleteRow(i);		
					// Update counters
					rowCount--;
					i--;
				}
			}
		} else if (tableID="Change_quantity") {
			var table = document.getElementById(tableID);
			var rowCount = table.rows.length;		
			for (var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				if (row==currentRow.parentNode.parentNode) {
					// Call java
					invokeJava(row.cells[1].innerText,rowCount);
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