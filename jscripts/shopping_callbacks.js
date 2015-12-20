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

function loadCart(event,index) {
	invokeJava("load_cart",index);
}

function createPdf(event) {
	invokeJava("create_pdf",0);
}

function createCsv(event) {
	invokeJava("create_csv",0);
}

function checkOut(event) {
	invokeJava("check_out", 0);
}

function sendOrder(event) {
	invokeJava("send_order",0);
}

function agbsAccepted(event) {
	invokeJava("agbs_accepted",event.checked);
}

function showAgbs() {
	invokeJava("show_agbs",0);
}

function changeQty(tableID,currentRow,cellIdx) {
    try {
		var key = window.event.keyCode;
		if (key==9 || key==13) {
			if (tableID=="Warenkorb") {
				var table = document.getElementById(tableID);
				var rowCount = table.rows.length;		
				for (var i=0; i<rowCount; i++) {
					var row = table.rows[i];
					if (row==currentRow.parentNode.parentNode) {
						var qty = row.cells[cellIdx].firstChild.value;
						// Check if value is in safe bounds and call java
						var eanCode = row.cells[0].innerText;	// used to be row.cells[1]
						if (qty>=0 && qty<=99999) {
							invokeJava("change_qty"+qty,eanCode);
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

function assortList(tableID,currentRow) {
	if (tableID=="Warenkorb") {
		var table = document.getElementById(tableID);
		var rowCount = table.rows.length;
		for (var i=0; i<rowCount; i++) {
			var row = table.rows[i];
			if (row==currentRow.parentNode) {
				var eanCode = row.cells[0].innerText;
				invokeJava("assort_list", eanCode);			
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

function changeShipping(tableID,currentRow,index) {
	if (tableID=="Checkout") {
		var table = document.getElementById(tableID);
		var rowCount = table.rows.length;
		for (var i=0; i<rowCount; i++) {
			var row = table.rows[i];
			if (row==currentRow.parentNode.parentNode) {
				var selectId = document.getElementById("selected" + index);
				var qty = selectId.value;
				var eanCode = row.cells[0].innerText;
				invokeJava("change_shipping"+qty,eanCode);
			}
		}
	}
}

function changeColor(tableRow, highLight) {
	if (highLight)
		tableRow.style.backgroundColor = '#dcfac9';
	else
		tableRow.style.backgroundColor = 'ffebcd';
}

function changeAddress(tableRow, type) {
	invokeJava("change_address", type);
}

function changeColorEven(tableRow, highLight) {
	if (highLight) {
		tableRow.style.backgroundColor = '#e6d6ea';
	} else
		tableRow.style.backgroundColor = '#e6e6fa';	// lavender
}

function changeColorOdd(tableRow, highLight) {
	if (highLight) {
		tableRow.style.backgroundColor = '#e6d6ea';
	} else
		tableRow.style.backgroundColor = '#ffe4e1';	// mistyrose
}

function swapArticles(tableID, eanSrc, eanDst) {
    try {
		if (tableID=="Warenkorb") {
			invokeJava("swap_articles"+eanDst, eanSrc.toString());
		}
    } catch (e) {
        // alert(e);
    }
}

function selectArticle(object) {
	invokeJava("select_article", object.id);
}

function showAll(event) {
	invokeJava("show_all", 0);
}
