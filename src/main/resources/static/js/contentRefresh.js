function reload_table(){
var table = $('#main-table').DataTable();
    table.destroy();
    var table = $('#sample_1').DataTable( {
        "processing": true,
        "dataType": 'json',
        ajax: '/refresh',
        deferRender: true,
        columns: [
            { data: 'id' },
            { data: 'repDate.year' },
            { data: 'hashrate' }
        ]
    } );
}