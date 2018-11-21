function isDefine(para) {
	//console.log(para);
    if ( typeof para == 'undefined' || para == "" || para == null || para == undefined || para == 'undefined')
        return false;
    else
        return true;
}


function getUrlParam(name)
{
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r!=null) return unescape(r[2]); return null; 
} 

function getKey() {        
    var storage = window.localStorage;
    return  key = storage["use_key"] || getUrlParam('key');
}