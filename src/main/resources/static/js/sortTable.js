$(document).ready(function() {
    $('#main-table').DataTable({
               paging: true,
               lengthChange: false,
               pageLength: 20,
               stateSave: true,
               info: true,
               searching: false,
               "aoColumns": [
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "desc", "asc" ] }
               ],
               "order": [[ 0, "asc" ]]
});
} );