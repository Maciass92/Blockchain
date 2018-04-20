$(document).ready(function() {
    $('#main-table').DataTable({
               paging: false,
               info: false,
               searching: false,
               "aoColumns": [
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "desc", "asc" ] }
               ],
               "order": [[ 0, "asc" ]]
});
} );