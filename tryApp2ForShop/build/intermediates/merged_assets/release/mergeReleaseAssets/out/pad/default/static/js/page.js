function  creatPageBar(obj,vCurrentPage,vSingleCount,vPageCount,vid) 
	{
		var RJPageBar=document.getElementById(obj);
		var CurrentPage=vCurrentPage;
		var SingleCount=vSingleCount;
		var PageCount=vPageCount;
		var stc_id=vid;
		//var mark=document.getElementById('mark');
		RJPageBar.innerHTML="";
		RJ_Pagebar({
			id:obj, //容器ID : 必选参数
			CurrentPage:CurrentPage,   //当前页 ： 可选参数，默认值为1
			SingleCount:SingleCount,   //显示数目： 可选参数，只能为奇数，默认值为7，
			PageCount:PageCount,   //必选参数
			stc_id:stc_id,
			//callback : function(pagenow,pagecount){		
				//mark.innerHTML='当前页:' + pagenow +',总共页:'+pagecount;
			//	}
			})
	}


function RJ_Pagebar(opt){
		if(!opt.id){ return false};
		if(!opt.PageCount){return false};
		var _obj = document.getElementById(opt.id);
		var _cp = parseInt(opt.CurrentPage)>parseInt(opt.PageCount)?1:parseInt(opt.CurrentPage)||1;
		var _sc = parseInt(opt.SingleCount)>parseInt(opt.PageCount)?parseInt(opt.PageCount):parseInt(opt.SingleCount)||parseInt(opt.PageCount);
		var _pc = parseInt(opt.PageCount);
		var _stc_id=parseInt(opt.stc_id);
		if(_sc%2==0){_sc=_sc-1};
		var callback = opt.callback || function(){};
		
		if(_cp!=1)
		{
			var oA=document.createElement('a');
			oA.href="javascript:date_ajax("+(_cp-1)+","+_stc_id+")";
			oA.innerHTML="上一页";
			_obj.appendChild(oA);
		}
		else
		{
			var oS=document.createElement('span');
			oS.className="RU-pagedisabled";
			oS.innerHTML="上一页";
			_obj.appendChild(oS);
		}
		
		if(_cp<=(_sc-1)/2)
		{
			for(i=1;i<=_sc;i++)
			{
				if(i==_cp)
				{
					var oS=document.createElement('span');
					oS.className='RU-pagenow';
					oS.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oS);
				}
				else
				{
					var oA=document.createElement('a');
					oA.href="javascript:date_ajax("+i+","+_stc_id+")";
					oA.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oA);
				}
			}
			var oS=document.createElement('span');
			oS.innerHTML="…";
			_obj.appendChild(oS);
		}
		else if(_cp<=_pc&&_cp>=_pc-(_sc-1)/2)
		{
			var oS=document.createElement('span');
			oS.innerHTML="…";
			_obj.appendChild(oS);
			for(i=_pc-_sc+1;i<=_pc;i++)
			{
				if(i==_cp)
				{
					var oS=document.createElement('span');
					oS.className='RU-pagenow';
					oS.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oS);
				}
				else
				{
					var oA=document.createElement('a');
					oA.href="javascript:date_ajax("+i+","+_stc_id+")";
					oA.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oA);
				}
			}
		}
		else
		{
			var oS=document.createElement('span');
			oS.innerHTML="…";
			_obj.appendChild(oS);
			
			for(i=_cp-(_sc-1)/2;i<(parseInt(_cp)+parseInt(_sc)-(_sc-1)/2);i++)
			{
				if(i==_cp)
				{
					var oS=document.createElement('span');
					oS.className='RU-pagenow';
					oS.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oS);
				}
				else
				{
					var oA=document.createElement('a');
					oA.href="javascript:date_ajax("+i+","+_stc_id+")";
					oA.innerHTML=i.toString().length==1?"0"+i:i;
					_obj.appendChild(oA);
				}
			}
			var oS=document.createElement('span');
			oS.innerHTML="…";
			_obj.appendChild(oS);
		}
		
		if(_cp!=_pc)
		{
			var oA=document.createElement('a');
			oA.href="javascript:date_ajax("+(_cp+1)+","+_stc_id+")";
			oA.innerHTML="下一页";
			_obj.appendChild(oA);
		}
		else
		{
			var oS=document.createElement('span');
			oS.className="RU-pagedisabled";
			oS.innerHTML="下一页";
			_obj.appendChild(oS);
		}
	}
