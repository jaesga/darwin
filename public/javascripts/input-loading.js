$(document).ready(function() {
    $('.submit').click(displayLoadingIcon);
});

function displayLoadingIcon(event) {
    $(this).parents("form").find(".loading").show();
};