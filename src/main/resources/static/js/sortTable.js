   var table;

$(document).ready(function() {

$.extend( true, $.fn.dataTable.defaults, {

} );

    table = $('#main-table').DataTable({
                ajax: {
                    url: '/refresh',
                    dataSrc:''
                },
               paging: true,
               lengthChange: false,
               pageLength: 20,
               stateSave: true,
               info: true,
               searching: false,

               "columns": [
                    {"data" : "id"},
                    {"data" : "repDate.year"},
                    {"data" : "hashrate"}
               ],

               /*"columnDefs": [
                                   {
                                       "className": "text-center",
                                       "targets": 0,
                                       "data": "id",
                                   },
                                   {
                                       "className": "text-center",
                                       "targets": 1,
                                       "data": "repDate.year",
                                   },
                                   {
                                       "className": "text-center",
                                       "targets": 2,
                                       "data": "hashrate",
                                   }
                               ],*/
               "aoColumns": [
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "desc", "asc" ] }
               ],
               "order": [[ 0, "asc" ]]


});
});

setInterval(function(){
    table.ajax.reload(null, false);
}, 8000);