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
		var key = window.event.keyCode;
		if (key==9 || key==13) {
			if (tableID=="Warenkorb") {
				var table = document.getElementById(tableID);
				var rowCount = table.rows.length;		
				for (var i=0; i<rowCount; i++) {
					var row = table.rows[i];
					if (row==currentRow.parentNode.parentNode) {
						var qty = row.cells[0].firstChild.value;
						// Check if value is in safe bounds and call java
						var eanCode = row.cells[1].innerText;
						if (qty>=0 && qty<=99999) {
							// alert(document.forms[0].elements.length);						
							invokeJava("change_qty"+qty,eanCode);
							/*
							if (i<(document.forms[0].elements.length-1))
								document.forms[0].elements[i+1].focus();
							else
								document.forms[0].elements[0].focus();
								*/
						}
					}
				}
			}			
		}
    } catch (e) {
        // alert(e);
    }
}