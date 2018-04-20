setInterval("contentRefresh();", 10000 );
function contentRefresh() {
    $.ajax({
        url: "/refresh",
        success: function(data) {
             $("#main-content").html(data);
            }
    });
}