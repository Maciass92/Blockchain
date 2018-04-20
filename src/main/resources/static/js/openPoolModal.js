function openPoolModal(id){

$.ajax({
    url: "/" + id,
    success: function(data){
    $("#PoolModalHolder").html(data);
    $("#personalModal").modal();
    $("#personalModal").modal('show');
    $('#poolTable').DataTable({
            paging: false,
            info: false
            "aoColumns": [
                       { "orderSequence": [ "asc", "desc" ] },
                       { "orderSequence": [ "asc", "desc" ] },
                       { "orderSequence": [ "desc", "asc" ] }
                         ]
    });
    }
});
}