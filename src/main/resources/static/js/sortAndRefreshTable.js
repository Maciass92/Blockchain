   var table;

$(document).ready(function() {

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
               "columnDefs": [
                     {
                     "className": "text-center",
                     "targets": 0,
                     "data": "id",
                     },

                     {
                     "className": "text-center",
                     "targets": 1,
                     "data" : function(data){

                         var seconds = data.repDate.second < 10 ? seconds = "0" + data.repDate.second : seconds = data.repDate.second;
                         var minutes = data.repDate.minute < 10 ? minutes = "0" + data.repDate.minute : minutes = data.repDate.minute;
                         var months = data.repDate.monthValue < 10 ? months = "0" + data.repDate.monthValue : months = data.repDate.monthValue;
                         var days = data.repDate.dayOfMonth < 10 ? days = "0" + data.repDate.dayOfMonth : days = data.repDate.dayOfMonth;
                         var hours = data.repDate.hour < 10 ? hours = "0" + data.repDate.hour : hours = data.repDate.hour;

                         return data.repDate.year + "-" + months + "-" + days + " / " + hours + ":" + minutes + ":" + seconds;
                        },
                     },

                     {
                     "className": "text-center",
                     "targets": 2,
                     "data": function(data){
                        return data.hashrate/1000.0;
                        },
                     }
               ],
               "aoColumns": [
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "asc", "desc" ] },
                 { "orderSequence": [ "desc", "asc" ] }
               ],
               "order": [[ 0, "asc" ]]
});

$(document).on('click', '#main-table tbody tr', function () {
  var id = $(this).find("td").eq(0).text();
  openPoolModal(id);
});

$('table').css('cursor','pointer');

});

setInterval(function(){
    table.ajax.reload(null, false);
}, 10000);