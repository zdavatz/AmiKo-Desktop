function deleteRow(tableID,currentRow) {
    try {
		if (tableID=="Delete_all") {
			invokeJava("delete_all",0);
		} else if (tableID=="Warenkorb") {
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

function createPdf(currentRow) {
	invokeJava("create_pdf",0);
}

function changeQty(tableID,currentRow) {
    try {
		if (window.event.keyCode==13) {
			if (tableID=="Warenkorb") {
				var table = document.getElementById(tableID);
				var rowCount = table.rows.length;		
				for (var i=0; i<rowCount; i++) {
					var row = table.rows[i];
					if (row==currentRow.parentNode.parentNode) {
						var qty = row.cells[0].firstChild.value;
						// Check if value is in safe bounds and call java
						if (qty>=0 && qty<=99999)
							invokeJava("change_qty"+qty,row.cells[1].innerText);
					}
				}
			}
		}
    } catch (e) {
        // alert(e);
    }
}