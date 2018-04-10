function openPoolModal(id){

    console.log("I've been called");

$.ajax({
    url: "/" + id,
    success: function(data){
    $("#PoolModalHolder").html(data);
    $("#PoolModal").modal("show");
    }
});
}