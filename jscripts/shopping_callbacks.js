function deleteRow(tableID,currentRow) {
    try {
		if (tableID=="Warenkorb") {
			var table = document.getElementById(tableID);
			var rowCount = table.rows.length;		
			for (var i=0; i<rowCount; i++) {
				var row = table.rows[i];
				if (row==currentRow.parentNode.parentNode) {
					// Call java
					invokeJava("delete_row",row.cells[0].innerText);	// used to be row.cells[1]
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

function deleteAll(event) {
	invokeJava("delete_all",0);
}

function loadCart(event) {
	invokeJava("load_cart",0);
}

function saveCart(event) {
	invokeJava("save_cart",0);
}

function createPdf(event) {
	invokeJava("create_pdf",0);
}

function createCsv(event) {
	invokeJava("create_csv",0);
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
						var qty = row.cells[3].firstChild.value;
						// Check if value is in safe bounds and call java
						var eanCode = row.cells[0].innerText;	// used to be row.cells[1]
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

function onSelect(tableID,currentRow,index) {
	if (tableID=="Warenkorb") {
		var table = document.getElementById(tableID);
		var rowCount = table.rows.length;
		for (var i=0; i<rowCount; i++) {
			var row = table.rows[i];
			if (row==currentRow.parentNode.parentNode) {
				var selectId = document.getElementById("selected" + index);
				var qty = selectId.value;
				var eanCode = row.cells[0].innerText;
				invokeJava("change_qty"+qty,eanCode);
			}
		}
	}
}

function changeMarge(tableID,currentRow) {
    try {
		var key = window.event.keyCode;
		if (key==9 || key==13) {
			var selectId = document.getElementById("marge");
			var marge = selectId.value;
			invokeJava("change_marge",marge);
		}
	} catch(e) {
		// alert(e);
	}
}