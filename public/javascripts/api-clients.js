$('#editApiClientName').hide();
$(document).ready(function() {
    $('.api-client-name').click(changeApiClientName);
});

function changeApiClientName(event) {
    event.stopPropagation();
    var apiClientName = $(this);
    var editForm = $(this).next('#editApiClientName');
    editForm.show();
    editForm.find('.name-input')
        .on('blur', function () {
            if (!editForm.find('.edit-name').is(':hover')){
                editForm.hide();
                apiClientName.show();
            }
        })
        .focus();
    $(this).hide();
};