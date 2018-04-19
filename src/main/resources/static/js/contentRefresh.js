setInterval("contentRefresh();", 10000 );
function contentRefresh() {
    $.ajax({
        url: "/refresh",
        success: function(data) {
            console.log(data);
             $("#main-content").html(data);
            }
    });
}