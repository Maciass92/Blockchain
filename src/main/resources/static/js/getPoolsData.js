function getPoolsData(){
$.getJSON('../json/networks/Turtlecoin/turtlecoin-pools.json', function(data) {

    var date_from = new Date();
    var pools_hashrates = [{"date_from" : date_from}];

data.pools.forEach(function(pool){

    var api_url = pool.api;
    var poolName = pool.name;

    if(pool.type == "forknote"){

        $.getJSON(api_url + 'stats', function(data) {

                var poolHashrate = data.pool.hashrate;

                pools_hashrates.push({"poolName" : poolName, "hashrate" : poolHashrate});

                console.log("Pool name: " + poolName + " Pool hashrate: " + parseInt(poolHashrate));
        });
    }
    else{
        $.getJSON(api_url + 'pool/stats', function(data) {

                var poolHashrate = data.pool_statistics.hashRate;

                console.log("Pool name: " + poolName + " Pool hashrate: " + parseInt(poolHashrate));

                pools_hashrates.push({"poolName" : poolName, "hashrate" : poolHashrate});

        });
    }
});

var myTimer = setInterval(function(){
    if (jQuery.active == 0){
        $.ajax({
              type: "POST",
              contentType : 'application/json; charset=utf-8',
              dataType : 'json',
              url: "/savePoolsData",
              data: JSON.stringify(pools_hashrates),
              success :function(result) {
                  console.log("Success!");
             }
          });
        clearInterval(myTimer); // stop the interval once you the get calls finished and you send the ajax call
    }
}, 500);
});
}