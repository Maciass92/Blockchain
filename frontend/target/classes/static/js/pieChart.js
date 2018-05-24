$("#personalModal").on('shown.bs.modal', function() {

var networkId = document.getElementById('networkId').value;

$.ajax({
    url: "/pooldata/" + networkId,
    dataType: 'json',
    success: function(poolData){

        var networkHashrate = poolData[0].networkHashrate.hashrate;
        var totalKnownPoolHashrate = 0;
        var unknownPools = 0;


        for (i = 0; i < poolData.length; i++) {
             totalKnownPoolHashrate += poolData[i].hashrate;
        }

        if(totalKnownPoolHashrate < networkHashrate)
            var unknownPools = Math.floor(networkHashrate - totalKnownPoolHashrate);

        var labels = poolData.map(function(e) {
           return e.poolDef.name;
        });
        labels.push("Unknown pools");

        var data = poolData.map(function(e) {
           return e.hashrate;
        });
        data.push(unknownPools);

    var colors = poolData.map(function(){

                var r = Math.floor(Math.random() * 200);
                var g = Math.floor(Math.random() * 200);
                var b = Math.floor(Math.random() * 200);
                var color = 'rgb(' + r + ', ' + g + ', ' + b + ')';

        return color;
     });

        console.log(labels);
        console.log(data);
        console.log(colors);

var ctx = document.getElementById('poolsChart').getContext('2d');
var chart = new Chart(ctx, {
    // The type of chart we want to create
    type: 'pie',

    // The data for our dataset
    data: {
        labels: labels,
        datasets: [{
            label: "My First dataset",
            backgroundColor: colors,
            borderColor: '#000000',
            borderWidth: 0.5,
            data: data,
        }]
    },

    // Configuration options go here
    options: {
        segmentShowStroke: false
    }
});

    }
});


});