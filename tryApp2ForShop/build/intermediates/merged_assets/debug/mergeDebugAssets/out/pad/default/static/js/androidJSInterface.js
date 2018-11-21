    //打开新页面,index页面索引
    function openWindow(index, title, url){
        if (isIOS()) {
            var message = {'method':'openWindow',
                           'index':index.toString(),
                           'title':title,
                           'url':url,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.openWindow(index, title, url);
        }
    }
    
    //打开弹出页面，index页面索引
    function openPopWindow(index, title, url){
        if (isIOS()) {
            var message = {'method':'openPopWindow',
                           'index':index.toString(),
                           'title':title,
                           'url':url,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.openPopWindow(index, title, url);
        }
    } 


    
    
    //打开详情页
    function openDetailWindow(goodsId, url, isShop){
        if (isIOS()) {
            var message = {'method':'openDetailWindow',
                           'goodsId':goodsId,
                           'url':url,
                           'isShop':isShop.toString(),};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.openDetailWindow(goodsId, url, isShop);
        }
    }

    
    //打开对戒详情页
    function openDetailWindowInCoupleRing(goodsId, url, isShop){
        if (isIOS()) {
            var message = {'method':'openDetailWindowInCoupleRing',
                           'goodsId':goodsId,
                           'url':url,
                           'isShop':isShop.toString(),};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.openDetailWindowInCoupleRing(goodsId, url, isShop);
        }
    }




    
    //JIA选钻后打开详情页
    function openDetailWindowFromJIA(goodsId, url, jiaJson){
        if (isIOS()) {
            var message = {'method':'openDetailWindowFromJIA',
                           'goodsId':goodsId,
                           'url':url,
                           'jiaJson':jiaJson,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.openDetailWindowFromJIA(goodsId, url, jiaJson);
        }
    }
    
    //JIA选钻后添加至购物车
    function appendShoppingCartFromJIA(goodsId, jiaJson){
        if (isIOS()) {
            var message = {'method':'appendShoppingCartFromJIA',
                           'goodsId':goodsId,
                           'jiaJson':jiaJson,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.appendShoppingCartFromJIA(goodsId, jiaJson);
        }
    }
    
    //登录
    function login(){
        if (isIOS()) {
            var message = {'method':'login',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.login();
        }
    }
    
    //注册
    function register(){
        if (isIOS()) {
            var message = {'method':'register',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.register();
        }
    }
    
    //登录或注册完成
    function onLoginFinished(userName, userKey){
        if (isIOS()) {
            var message = {'method':'onLoginFinished',
                           'userName':userName,
                           'userKey':userKey,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.onLoginFinished(userName, userKey);
        }
    }
    
    //登录或注册失败
    function onLoginFailed(errorText){
        if (isIOS()) {
            var message = {'method':'onLoginFailed',
                           'errorText':errorText,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.onLoginFailed(errorText);
        }
    }
    
    //清理缓存
    function clearCache(){
        if (isIOS()) {
            var message = {'method':'clearCache',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.clearCache();
        }
    }
    
    //检查更新
    function checkVersion(){
        if (isIOS()) {
            var message = {'method':'checkVersion',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.checkVersion();
        }
    }
    
    //提示信息
    function showToast(text){
        if (isIOS()) {
            var message = {'method':'showToast',
                           'text':text,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.showToast(text);
        }
    }
    
    function isIOS()
    {
        var pda_user_agent_list = new Array("iPhone", "iPod", "iPad");
        var user_agent = navigator.userAgent.toString();
        for (var i=0; i < pda_user_agent_list.length; i++) {
            if (user_agent.indexOf(pda_user_agent_list[i]) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
    * 以下为本地代码调用JS接口
    **/
function deleteOrderFromNative(){
    refresh();
}



    //购物车删除
    function deleteShoppingCartFromNative() {
        //TODO

    var storage = window.localStorage;
    var key = storage["use_key"];
    var cartid='';
    var i=0;

    $('.selects').each(function() {

                
                if ($(this).is(":checked")) {
                   // alert($(this).data("price"));
                    if (cartid==''){
                        cartid=$(this).data("cartid");
                     }else{
                        cartid +=','+$(this).data("cartid"); 
                     }
                    
					

                  
                }



     })



     del_cart(cartid);

    }


    function del_cart(cartid){

          var storage = window.localStorage;
          var key = storage["use_key"];  
          //console.log('http://www.zsmtvip.com/app/index.php?i=2&c=entry&m=ewei_shop&v=pad&do=member&p=member_cart&key='+key+'&act=del&id='+cartid);
          $.ajax({
                    url : 'http://www.zsmtvip.com/app/index.php?i=2&c=entry&m=ewei_shop&v=pad&do=member&p=member_cart&key='+key+'&act=del&id='+cartid, 
                    type: "get",                   
                    dataType : 'jsonp',
                    success : function(data) {
                        

                            //console.log(JSON.stringify(data));

                            date_ajax('http://www.zsmtvip.com/app/index.php?i=2&c=entry&m=ewei_shop&v=pad&do=member&p=member_cart&key='+key);



                    },
                    error : function() {
                        ////showToast("error!");
                    }
                })




    }


    function updatePrice(id,v1,v2){
        var storage = window.localStorage;
        var key = storage["use_key"];
        //alert('http://192.168.0.83/app/index.php?i=2&c=entry&m=ewei_shop&v=pad&do=member&p=priceset&key='+key+'&id='+id+'&xs='+v1+'&act=edit');
        $.ajax({
        url: 'http://www.zsmtvip.com/app/index.php?i=2&c=entry&m=ewei_shop&v=pad&do=member&p=priceset&key='+key+'&id='+id+'&xs='+v1+'&ws='+v2+'&act=edit',
        type: "get",
        dataType: 'jsonp',
        success: function(data) {
            if (data.resultCode == "0") { 

            
            } else {
                //showToast(data.error);
            }
        },
        error: function() {
            ////showToast("error!");
        }
    })


    }



    //门店价格设置保存
    function saveShopPriceFromNative() {     
      $(".inputvalue").each(function(){
        var id=$(this).data('shopid');
        var v1=$(this).find('.inputvalue1').val();
        var v2=$(this).find('.inputvalue2').val();
        updatePrice(id,v1,v2);
      }); 
      
      showToast('修改完成');
    }


    //刷新当前页面
    function refresh(){
        if (isIOS()) {
            var message = {'method':'refresh',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.refresh();
        }
    }


        //加载动画
    function showLoadingIndicator(){
        if (isIOS()) {
            var message = {'method':'showLoadingIndicator',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.showLoadingIndicator();
        }
    }
    
    //隐藏加载动画
    function hideLoadingIndicator(){
        if (isIOS()) {
            var message = {'method':'hideLoadingIndicator',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.hideLoadingIndicator();
        }
    }

    //更新资源文件
    function updateResource(){
        if (isIOS()) {
            var message = {'method':'updateResource',};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.updateResource();
        }
    }

    //搜索产品
    function searchGoods(keyword) {
        if (isIOS()) {
            var message = {'method':'searchGoods',
                           'keyword':keyword,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.searchGoods(keyword);
        }
    }


        //下载pdf
    function downloadJIACer(url){
        if (isIOS()) {
        var message = {'method':'downloadJIACer',
                      'url':url,};
                    window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
                 window.android.downloadJIACer(url);
        }
    }
    
    //下载GIA证书
    function downloadGIACer(title, url){
        if (isIOS()) {
            var message = {'method':'downloadGIACer',
                           'title':title,
                           'url':url,};
            window.webkit.messageHandlers.iosapp.postMessage(message);
        } else {
            window.android.downloadGIACer(title, url);
        }
    }
    

    //购物车添加成功
    function onAddShoppingCartSucceed() {
    if (isIOS()) {
             var message = {'method':'onAddShoppingCartSucceed',};
                window.webkit.messageHandlers.iosapp.postMessage(message);
            } else {
             window.android.onAddShoppingCartSucceed();
            }
    }
    
        
    //购物车删除成功
    function onDeleteShoppingCartSucceed() {
    if (isIOS()) {
             var message = {'method':'onDeleteShoppingCartSucceed',};
                window.webkit.messageHandlers.iosapp.postMessage(message);
            } else {
             window.android.onDeleteShoppingCartSucceed();
            }
    }

