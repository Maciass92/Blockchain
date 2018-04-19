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
    });
    }
});
}