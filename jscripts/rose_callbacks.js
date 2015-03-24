function sortCart(event,index) {
	invokeJava("sort_cart", index);
}

function showAll(event) {
	invokeJava("show_all", 0);
}

function uploadArticle(object) {
	invokeJava("upload_article",object.id);
}

function uploadToServer(event) {
	invokeJava("upload_to_server",0);
}

function changeColorEven(tableRow, highLight) {
	if (highLight)
		tableRow.style.backgroundColor = '#dcfac9';
    else
      tableRow.style.backgroundColor = '#ffebcd';
}

function changeColorOdd(tableRow, highLight) {
	if (highLight)
		tableRow.style.backgroundColor = '#dcfac9';
    else
      tableRow.style.backgroundColor = '#f5f5f5';
}