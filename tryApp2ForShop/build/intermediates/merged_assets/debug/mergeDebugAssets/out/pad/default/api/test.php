<?php 
/* // 1. 初始化
 $ch = curl_init();
 // 2. 设置选项，包括URL
 curl_setopt($ch,CURLOPT_URL,"http://www.devdo.net");
 curl_setopt($ch,CURLOPT_RETURNTRANSFER,1);
 curl_setopt($ch,CURLOPT_HEADER,0);
 // 3. 执行并获取HTML文档内容
 $output = curl_exec($ch);
 if($output === FALSE ){
 echo "CURL Error:".curl_error($ch);
 }else{
	 echo $output;
 }
 // 4. 释放curl句柄
 curl_close($ch); */
 
 
 

$cookie_file = tempnam('./temp','cookie');
//echo  $cookie_file;
$login_url  = 'http://www.zsagia.com/login_new';
$post_fields = 'user_name=13640875688&user_pwd=159753';

$ch = curl_init($login_url);
curl_setopt($ch, CURLOPT_HEADER, 0);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $post_fields);
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookie_file);
curl_exec($ch);
curl_close($ch);


$url='http://www.zsagia.com/diamond_new/1--16--16--4--0--0--1--0.html?q_carat1=0.3&q_carat2=0.33&q_id=&q_id_type=id&q_perpage=25';
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_HEADER, 0);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookie_file);
$contents = curl_exec($ch);
echo $contents;

curl_close($ch);

 
 
 
 
 
 
 
 
 
 
 
 
 
?> 

