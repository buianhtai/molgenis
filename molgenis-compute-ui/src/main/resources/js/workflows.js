$(function() {
	$('#submitFormButton').on('click', function() {
		var form = $('#workflowForm');
		if (form.valid()) {
			showSpinner(function() {
				form.submit();
			});
		}
	});
});
