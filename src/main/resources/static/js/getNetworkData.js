function getNetworkData(){
$.getJSON('../json/networks/network-list.json', function(data) {

    var hashrates = {};

data.networks.forEach(function(network){

    var rep_date = new Date();
    var network_id = network.id;

    var networkJson = [{"rep_date" : rep_date}, {"id" : network_id}];

    var api_url = network.hashrate_url;
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function() {
         if (xmlHttp.readyState == XMLHttpRequest.DONE){
             var result = xmlHttp.responseText;
             networkJson.push({"Network_hashrate" : result});
             console.log("Network hashrate: " + result);
         }
    }

    xmlHttp.open("GET", api_url);
    try {
        xmlHttp.send();

    } catch (ex) {
        console.error(ex);
    }

    console.log(networkJson);

var myTimer = setInterval(function(){
    if (jQuery.active == 0){
        $.ajax({
                  type: "POST",
                  contentType : 'application/json; charset=utf-8',
                  dataType : 'json',
                  url: "/saveNetworkData",
                  data: JSON.stringify(networkJson),
                  success :function(result) {
                      console.log("Success!");
                 }
              });
        clearInterval(myTimer); // stop the interval once you the get calls finished and you send the ajax call
    }
}, 500);

});

});
}
