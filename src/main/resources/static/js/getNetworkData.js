//The very first version included reading APIs via frontend. Had to switch to backend.

function getNetworkData(){
$.getJSON('../json/networks/network-list.json', function(data) {

    var hashrates = {};

    var rep_date = new Date();
    var network_list = {"rep_date" : rep_date};
    var network_hashrates = [];

data.networks.forEach(function(network){

    var network_id = network.id;

    var api_url = network.hashrate_url;
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function() {
         if (xmlHttp.readyState == XMLHttpRequest.DONE){
             var result = xmlHttp.responseText;
             network_hashrates.push({"Network_hashrate" : result, "Network_id" : network_id});

             console.log("Network hashrate: " + result);
         }
    }
    xmlHttp.open("GET", api_url);
    try {
        xmlHttp.send();

    } catch (ex) {
        console.error(ex);
    }

$.extend(network_list, {"networks: " : network_hashrates});
console.log(network_list);
});

var myTimer = setInterval(function(){
    if (jQuery.active == 0){
        $.ajax({
                  type: "POST",
                  contentType : 'application/json; charset=utf-8',
                  dataType : 'json',
                  url: "/saveNetworkData",
                  data: JSON.stringify(network_list),
                  success :function(result) {
                      console.log("Success!");
                 }
              });
        clearInterval(myTimer);
    }
}, 1000);

});
}
