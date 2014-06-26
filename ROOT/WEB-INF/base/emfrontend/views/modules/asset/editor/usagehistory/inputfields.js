	var usingIE = navigator.appVersion.indexOf("MSIE") > 0? true : false;
	if ( usingIE )
	{
		document.body.onload = function() { buildArrays();changeSelect(document.frm.use); };
	}
	else
	{
		window.onload = function() { buildArrays();changeSelect(document.frm.use); };
	}


var arr = new Array(
["State Farm Internal", 
"Binder Cover", 
"0"] , 
["State Farm Internal", 
"Brochure Cover", 
"0"] , 
["State Farm Internal", 
"Brochure Inside", 
"0"] , 
["State Farm Internal", 
"Display/Poster", 
"0"] , 
["State Farm Internal", 
"Intranet", 
"0"] , 
["State Farm Internal", 
"Multi Media", 
"0"] , 
["State Farm Internal", 
"Flyer", 
"0"] , 
["State Farm Internal", 
"Publication/Newsletter cover", 
"0"] , 
["State Farm Internal", 
"Publication/Newsletter inside", 
"0"] , 
["State Farm Internal", 
"Reflector Cover", 
"365"] , 
["State Farm Internal", 
"Claims Quarterly Cover", 
"365"] , 
["Local Media", 
"Billboard", 
"0"] , 
["Local Media", 
"Brochure Cover", 
"0"] , 
["Local Media", 
"Brochure Inside", 
"0"] , 
["Local Media", 
"Interactive Media", 
"0"] , 
["Local Media", 
"Mailer", 
"0"] , 
["Local Media", 
"Newspaper Ad", 
"0"] , 
["Local Media", 
"Postcard", 
"0"] , 
["Local Media", 
"Publication Cover", 
"0"] , 
["Local Media", 
"Publication Inside", 
"0"] , 
["Local Media", 
"TV Commercial", 
"0"] , 
["Local Media", 
"Video", 
"0"] , 
["Regional Media", 
"Billboard", 
"0"] , 
["Regional Media", 
"Brochure Cover", 
"0"] , 
["Regional Media", 
"Brochure Inside", 
"0"] , 
["Regional Media", 
"Interactive Media", 
"0"] , 
["Regional Media", 
"Mailer", 
"0"] , 
["Regional Media", 
"Newspaper Ad", 
"0"] , 
["Regional Media", 
"Postcard", 
"0"] , 
["Regional Media", 
"Publication Cover", 
"0"] , 
["Regional Media", 
"Publication Inside", 
"0"] , 
["Regional Media", 
"TV Commercial", 
"0"] , 
["Regional Media", 
"Video", 
"0"] , 
["National Media", 
"Billboard", 
"365"] , 
["National Media", 
"Brochure Cover", 
"365"] , 
["National Media", 
"Brochure Inside", 
"182"] , 
["National Media", 
"Display/Point of Purchase", 
"365"] , 
["National Media", 
"Interactive Media", 
"0"] , 
["National Media", 
"Internet", 
"0"] , 
["National Media", 
"Mailer", 
"365"] , 
["National Media", 
"Newspaper Ad", 
"365"] , 
["National Media", 
"Postcard", 
"365"] , 
["National Media", 
"Publication Cover", 
"365"] , 
["National Media", 
"Publication Inside", 
"182"] , 
["National Media", 
"TV Commercial", 
"0"] , 
["National Media", 
"Video", 
"0"] , 
[]);

		function buildArrays(){
			var obj = ""
			var arrCnt = -1;
			var arrA = new Array();
			for(i=0;i<arr.length-3;i++){
				if(String(obj) != arr[i][0]){
					obj = arr[i][0];
					arrCnt++;
					arrA[arrCnt] = obj;
				}
			}
			for(i=0;i<arrA.length;i++){
				document.frm.use.options[i] = new Option(arrA[i], arrA[i]);
			}
		}
		function openWin(num){
			window.open(num, 'explain', 'height=400,width=500,scrollbars=yes');
		}
		function changeSelect(level){
			for(i=0;i<document.frm.use2.length;i++){
					document.frm.use2.options[i] = null;
				}
			var cnt = -1;
			for(i=0;i<arr.length-1;i++){
				if(level[level.selectedIndex].value == arr[i][0]){
					cnt++;
					document.frm.use2.options[cnt] = new Option(arr[i][1],arr[i][1]);
				}
			}
			changeDays();
		}
		function changeDays(){
			var now = new Date();
			document.frm.date.value = now; //CB
			var theDiv = document.getElementById("days");
			var rd, mn, dy, yr;
			
			theDiv.innerHTML = "";
			
			for(i=0;i<arr.length-1;i++){
				if(document.frm.use[document.frm.use.selectedIndex].value == arr[i][0] && document.frm.use2[document.frm.use2.selectedIndex].value == arr[i][1]){
					rd = new Date(new Date().getTime() + (arr[i][2]*1000*60*60*24));
					mn = rd.getMonth() + 1;
					dy = rd.getDate();
					yr = rd.getYear();
					frm.rdays.value = arr[i][2];
					break;
					//theDiv.innerHTML = "Will be available after " + mn + "/" + dy + "/" + yr; //CB
				}
			}
		}
		function checkVals(){
			var frm = document.frm
			/*if(frm.use.selectedIndex == 0){
				alert("Please select an audience.");
				return false;
			}else if(frm.use2.selectedIndex == 0){
				alert("Please select an audience.");
				return false;
			}else*/
			if(String(frm.comments.value) == ""){
				alert("Please enter a Description.");
				frm.comments.focus();
				return false;
			}
			if(String(frm.formID.value) == ""){
				alert("Please enter a Form number or type 'None'.");
				frm.formID.focus();
				return false;
			}                    
			//parent.invisibleFrame.location="removeBasket.jsp?recordView=CollBasket_DetailsView&ac=lybys&arr=2,5,6,";
//			document.getElementById('sub1').style.display = 'none';
//			document.getElementById('sub2').style.display = '';
			
//			openProgress('DownloadAll.jsp','Download','autozip',-1,-1);
//			if(String.valueOf(DLtype).equals("Download for Final")){
//				out.println(dsPage);
//			}else{
//				out.println();
//			}
//			openProgress('DownloadAll.jsp','Download','autozip',-1,-1,'&comp=false');

			return true;
		}
		
		function openProgress(jspPage,pageTitle,packaging,catalogID,recordID,dlType)
			{
				s = jspPage + "?recordView=CollBasket_DetailsView"+dlType;
				if(catalogID != -1)
					s += "&catalogID=" + catalogID;
				if(recordID != -1)
					s += "&recordID=" + recordID;
				s += "&packaging=autozip";
				alert (s);
				sOpt = "toolbar=no,scrollbars=no,status=no,menubar=no,resizable=no,location=no,dependent=yes,width=300,height=200";
				window.open(s, pageTitle, sOpt);
			}
