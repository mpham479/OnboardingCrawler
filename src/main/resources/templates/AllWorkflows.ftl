<html>
	<style>
	body{
		color: #353833;
		font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif;
		background-color: #ffffff;
		font-size: 14px;
		margin: 0 20px;
		padding: 10px 20px;
		position: relative;
	}
	
	tr{
		min-height: 25px;
	}

	td{
		padding: 8px 3px 3px 15px;
		white-space: nowrap;
		display: table-cell;
		border-collapse: collapse;
		border-spacing: 2px;
	}

	td.default{
		vertical-align:top;
		text-align: left;
		min-width:150px;
	}

	td.moreInfo{
		vertical-align:top;
		text-align: center;
		width:50px;
	}

	th.colHeader{
		vertical-align:top;
		text-align: left;
		padding: 8px 3px 3px 7px;
		white-space: nowrap;
		font-size: 16px;
		height:25px;
		background: #dee3e9;
		font-weight: bold;
		display: table-cell;
		border-collapse: collapse;
		border-spacing: 2px;
		min-width:150px;
	}

	.noSelect {
	    -webkit-touch-callout: none;
	    -webkit-user-select: none;
	    -khtml-user-select: none;
	    -moz-user-select: none;
	    -ms-user-select: none;
	    user-select: none;
	}

	tr.odd{
		vertical-align:top;
		font-family: 'DejaVu Sans Mono', monospace;
		font-size: 14px;
		margin-top: 8px;
		line-height: 1.4em;
		white-space: nowrap;
		border-collapse: collapse;
		border-spacing: 2px;
		text-align: left;
		padding-right: 0px;
		padding-top: 8px;
		padding-bottom: 3px;
		padding-left: 10px;
		background-color: #FFFFFF;
		display: table-row;
		vertical-align: inherit;
		border-color: inherit;
	}

	tr.even{
		vertical-align:top;
		font-family: 'DejaVu Sans Mono', monospace;
		font-size: 14px;
		margin-top: 8px;
		line-height: 1.4em;
		white-space: nowrap;
		border-collapse: collapse;
		border-spacing: 2px;
		text-align: left;
		padding-right: 0px;
		padding-top: 8px;
		padding-bottom: 3px;
		padding-left: 10px;
		background-color: #EEEEEF;
		display: table-row;
		vertical-align: inherit;
		border-color: inherit;
	}

	.infoImg{
		width: 30px;
		height: 30px;
		cursor: pointer;
	}

	.imgOff, .imgHover:hover .imgOn{
   		display:none;
	}
	.imgOn, .imgHover:hover .imgOff{
	   display:inline-block;
	}


	.clickableHeader:hover{
		color:#807FEF;
		cursor:pointer;
	}

	div.tableBlock{
		padding: 0px 0px 0px 0px;
		border: 1px solid #ededed;
		background-color: #f8f8f8;
		margin-bottom: 15px;
	}

	table.data{
		display: table;
		border-collapse: collapse;
		border-spacing: 2px;
		border-color: grey;
		width: 100%;
		border-left: 1px solid #EEE;
		border-right: 1px solid #EEE;
		border-bottom: 1px solid #EEE;
		overflow-x: scroll;
	}
	
	iframe{
		display:block;
		border:0;
		width:100%;
		height:100%;
	}
	
	 /* The Modal (background) */
	.modal {
		display: none; /* Hidden by default */
		position: fixed; /* Stay in place */
		z-index: 1; /* Sit on top */
		padding-top: 50px; /* Location of the box */
		left: 0;
		top: 0;
		width: 100%; /* Full width */
		height: 100%; /* Full height */
		overflow: auto; /* Enable scroll if needed */
		background-color: rgb(0,0,0); /* Fallback color */
		background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
	}

	#loading{
		width:100%;
		margin:auto;
		display:none;
		text-align:center;
	}

	/* Modal Content */
	.modal-content {
		background-color: #fefefe;
		margin: auto;
		padding: 10px 20px 20px 20px;
		border: 1px solid #888;
		width: 80%;
		height: 700px;
	}
	
	/* Adjustable Modal Content */
	.adjusted-modal-content {
		background-color: #fefefe;
		margin: 20px 0px 0px -5px;
		padding: 0px;
		border: 0px;
		width: 100%;
		height: 100%;
		display: inherit;
	}

	.closeicon{
		width:30px;
		height:30px;
	}

	.closeicon:hover,
	.closeicon:focus{
		cursor: pointer;
	}
	
	.arrow{
		padding:0px;
		margin:0px;
	}

	.h1copy { 
	    display: block;
	    font-size: 2em;
	    margin-top: 0.67em;
	    margin-bottom: 0.67em;
	    margin-left: 0;
	    margin-right: 0;
	    font-weight: bold;
	}

	</style>
	<head>
	  <title>Workflow List</title>
	</head>
	<body>
	  <span class="h1copy">Workflow List</span>
	  <span>Documentation generated on:</span>
	  <span>${.now?date}</span>

	  <div class="tableBlock">
		  <table class="data" id="data">
		  	<thead>
				<tr>
					<th class="colHeader noSelect clickableHeader imgHover" onclick="sortTable(0)" sortdir="" data="Name">Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
					<th class="colHeader noSelect clickableHeader imgHover" onclick="sortTable(1)" sortdir="" data="System Id">System Id <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
					<th class="colHeader">Description</th>
					<th class="colHeader"></th>
				</tr>
			</thead>
			<tbody>
			<#foreach workflow in workflows?values>                  
				<tr class='${["odd", "even"][workflow_index%2]}' systemid='${workflow.systemId}'>
					<td class="default">
						<#if fileBasedLinks>
							<a href="file:workflows/${workflow.systemId}.${baseExt}">${(workflow.name)!" "}</a>
		  				<#else>
		  					<a href="${articleBaseUrl + workflow.systemId?replace('_',replaceSpacesInUrlsWith,'r')}">${(workflow.name)!" "}</a>
		  				</#if>
					</td>
					<td class="default">${(workflow.systemId) !" "}</td>
					<td class="default" style="white-space:normal;">${(workflow.description)!" "}</td>
					<td class="moreInfo">
						<#if fileBasedLinks>
		  					<a class="imgHover"><img src="images/more_info.png" class="infoImg imgOn" onclick="openModalInfo(this)"/><img src="images/more_info_hover.png" class="infoImg imgOff" onclick="openModalInfo(this)"/></a>
		  				<#else>
		  					<a class="imgHover"><img src="${imgBaseUrl}more_info.png" class="infoImg imgOn" onclick="openModalInfo(this)"/><img src="${imgBaseUrl}more_info_hover.png" class="infoImg imgOff" onclick="openModalInfo(this)"/></a>
		  				</#if>
					</td>
				</tr>
			</#foreach> 
			</tbody>
		  </table>
		</div>
		
		<!-- The Modal -->
		<div id="myModal" class="modal">

		  <!-- Modal content -->
		  <#if fileBasedLinks>
		  <div class="modal-content" style="height:80%!important">
		  <#else>
		  <div class="modal-content">
		  </#if>
			<div width="100%" align="right">
				<#if fileBasedLinks>
					<span class="imgHover"><img src = "images/close.png" class="closeicon imgOn" onclick="closeModal()"/><img src = "images/close_hover.png" class="closeicon imgOff" onclick="closeModal()"/></span>
		  		<#else>
		  			<span class="imgHover"><img src = "${imgBaseUrl}close.png" class="closeicon imgOn" onclick="closeModal()"/><img src = "${imgBaseUrl}close_hover.png" class="closeicon imgOff" onclick="closeModal()"/></span>
		  		</#if>
			</div>

			<#if fileBasedLinks>
		  		<div id="contents" class="adjusted-modal-content" style="height:90%!important">
		  			<div id="loading"><img src="images/catLoading.gif"/></div>
		  			<iframe id='iframe' src='' style='display:inherit;' onload=""></iframe>
		  		</div>
		  	<#else>
		  		<#if usedInClickHelp>
		  			<div id="contents" class="adjusted-modal-content">
		  				<div id="loading"><img src="${imgBaseUrl}catLoading.gif"/></div>
		  				<iframe id="displayedFrame" src="" style="display:none"></iframe>
		  			</div>
					<iframe id='iframe' src='' style='display:none;' onload=""></iframe>
		  		<#else>
		  			<div id="contents" class="adjusted-modal-content">
		  				<div id="loading"><img src="${imgBaseUrl}catLoading.gif"/></div>
		  			</div>
					<iframe id='iframe' src='' style='display:inherit;' onload=""></iframe>
		  		</#if>
		  	</#if>
		  </div>
		</div>

		<script>
		// Get the modal
		var modal = document.getElementById('myModal');

		var loading = document.getElementById("loading");
		
		// Get the <span> element that closes the modal
		var span = document.getElementsByClassName("close")[0];

		//get close
		var close = document.getElementById('close');
		
		function openModalInfo(ele){
			var frameUrl = '';
			<#if fileBasedLinks>
			frameUrl = "file:workflows/" + ele.parentElement.parentElement.parentElement.getAttribute("systemid") + ".${baseExt}"
		  		modal.style.display = "block"
		  		loading.style.display = "none"
				document.getElementById('iframe').setAttribute("src",frameUrl);
				document.getElementById('iframe').setAttribute("onload","iframeLoaded()");
		  	<#else>
		  		<#if usedInClickHelp>
		  			frameUrl = "${articleBaseUrl}" + ele.parentElement.parentElement.parentElement.getAttribute("systemid").replace(/_/g,'${replaceSpacesInUrlsWith}');
			  		modal.style.display = "block"
		  			loading.style.display = "inline"
					document.getElementById('iframe').setAttribute("src",frameUrl);
				    document.getElementById('iframe').setAttribute("data-url-changed",frameUrl);
					document.getElementById('iframe').setAttribute("onload","loadScriptInClickHelp(this)");
		  		<#else>
		  			frameUrl = "${articleBaseUrl}" + ele.parentElement.parentElement.parentElement.getAttribute("systemid").replace(/_/g,'${replaceSpacesInUrlsWith}');
		  			modal.style.display = "block"
					loading.style.display = "inline"
					document.getElementById('iframe').setAttribute("src",frameUrl);
				    document.getElementById('iframe').setAttribute("onload","iframeLoaded()");
		  		</#if>
		  	</#if>
		};

		function iframeLoaded(){

		}

		// When the user clicks on <span> (x), close the modal
		function closeModal() {
			modal.style.display = "none";
			loading.style.display = "none";
			<#if fileBasedLinks>
				document.getElementById('iframe').setAttribute("src","");
		  	<#else>
		  		<#if usedInClickHelp>
					document.getElementById('iframe').setAttribute("src","");
				    document.getElementById('iframe').setAttribute("data-url-changed","");
				    document.getElementById('iframe').setAttribute("onload","");
				    document.getElementById('displayedFrame').setAttribute("style","display:none");
		  		<#else>
		  			document.getElementById('iframe').setAttribute("src","");
				    document.getElementById('iframe').setAttribute("data-url-changed","");
				    document.getElementById('iframe').setAttribute("onload","");
				    document.getElementById('displayedFrame').setAttribute("style","display:none");
		  		</#if>
		  	</#if>
		};

		// When the user clicks anywhere outside of the modal, close it
		window.onclick = function(event) {
			if (event.target == modal) {
				modal.style.display = "none";
				loading.style.display = "none";

				<#if fileBasedLinks>
					document.getElementById('iframe').setAttribute("src","");
			  	<#else>
			  		<#if usedInClickHelp>
						document.getElementById('iframe').setAttribute("src","");
					    document.getElementById('iframe').setAttribute("data-url-changed","");
					    document.getElementById('iframe').setAttribute("onload","");
					    document.getElementById('displayedFrame').setAttribute("style","display:none");
			  		<#else>
			  			document.getElementById('iframe').setAttribute("src","");
					    document.getElementById('iframe').setAttribute("data-url-changed","");
					    document.getElementById('iframe').setAttribute("onload","");
					    document.getElementById('displayedFrame').setAttribute("style","display:none");
			  		</#if>
			  	</#if>
			}
		};
		</script>

		<script>
			//will only apply to clickhelp
	      function loadScriptInClickHelp(ele){
	        var maxInterval = 500;
	        var intervalCounter = 0;
	        var checkIFramesExist = setInterval(function() {
            //ensure element exists
	           if (ele.contentDocument.getElementsByTagName("iframe").length) {
	              console.log("Exists!");
	             intervalCounter = 0; //reset counter
	              clearInterval(checkIFramesExist);
	           }else if(intervalCounter > maxInterval){
               //counter max reached
	              console.log("Never meets criteria!");
	              clearInterval(checkIFramesExist);
	           }else{
               //increment 
	             intervalCounter++;
	           }
	        }, 100); // check every 100ms
	        
	        var item;
	        for(i = 0; i < ele.contentDocument.getElementsByTagName("iframe").length; i++){
	          var iframe = ele.contentDocument.getElementsByTagName("iframe").item(i);
	          if(iframe.id == "cphMain_articleEditor_pnlCallback_pnlContentFrame"){
	            item = i;
	            break;
	          }
	        }
	        if(iframe){
            var gotStyle = false;
              var gotScripts = false;
              var head = document.createElement('head');
              var body = document.createElement('body');
              var scripts = [];
            var doc = document.getElementById('displayedFrame').contentDocument;
	          var checkExist = setInterval(function() {
                            
              if(ele.contentDocument.getElementsByTagName("iframe")[i].contentDocument.getElementById('scriptData') && !gotScripts){
                   
                   var src = ele.contentDocument.getElementsByTagName("iframe")[i].contentDocument;              
                
                   for(k = 0; k < src.getElementsByTagName("script").length; k++){
                     if(src.getElementsByTagName("script")[k].innerHTML){
                       var script = document.createElement('script');
                       script.type  = "text/javascript";
                       script.text  = src.getElementsByTagName("script")[k].innerHTML
                        console.log(src.getElementsByTagName("script")[k].innerHTML)
                           scripts.push(script);
                     }
                   }
              
                    gotScripts = true;
              
              }else if (ele.contentDocument.getElementsByTagName("iframe")[i].contentDocument.getElementById('scriptData') && !gotStyle) {
                 var src = ele.contentDocument.getElementsByTagName("iframe")[i].contentDocument;
                  var styles = document.createElement('style');
                  
                 for(j = 0; j < src.getElementsByTagName("style").length; j++){
                   if(src.getElementsByTagName("style")[j].innerHTML.indexOf('clickableHeader') !== -1){
                     styles.appendChild(document.createTextNode(src.getElementsByTagName("style")[j].innerHTML));
                   }else{
                     head.appendChild(src.getElementsByTagName("style")[j])
                   }
                 }
                 
                 head.appendChild(styles);
                 
                
                for(x = 0; x < scripts.length; x++){
                  body.appendChild(scripts[x]);
                }
                body.appendChild(src.getElementById('scriptData'));
                              
                 doc.head.parentNode.replaceChild(head, doc.head);
                 gotStyle = true;
                     //doc.body = ele.contentDocument.getElementsByTagName("iframe")[item].contentWindow.document.body.cloneNode(true)
                     //doc.head = ele.contentDocument.getElementsByTagName("iframe")[item].contentWindow.document.head.cloneNode(true)
                   
                     //doc.head.innerHTML = src.head.innerHTML;
                     //doc.body.innerHTML = src.body.innerHTML;
                     //console.log(ele.contentDocument.getElementsByTagName("iframe")[item].contentWindow.document.body)
                 
	                
	             }else if(gotStyle && gotScripts){
                 doc.body.parentNode.replaceChild(body, doc.body);

                 //hide loading
                 document.getElementById("loading").style.display = "none"
                 
                 //show iframe
                 document.getElementById('displayedFrame').setAttribute("style","display:inline");
                 
                 intervalCounter = 0; //reset counter 
	                console.log("Exists2!");
	                clearInterval(checkExist);
                 
               }else if(intervalCounter > maxInterval){
	              console.log("Never meets criteria2!");
	              clearInterval(checkExist);
	             }else{
	               intervalCounter++;
	             }
	          }, 100); // check every 100ms
	        }
	      }
	  </script>
	 <script>
		function sortTable(column) {
        var table = document.getElementById("data");
        var thead = table.getElementsByTagName("thead")[0];
        var tbody = table.getElementsByTagName("tbody")[0];
        var dir = table.getElementsByTagName("TH")[column].getAttribute("sortdir");
            if(dir){
              if(dir.toUpperCase() == "ASC"){
                dir = "desc"
              }else{
                dir = "asc"
              }
            }else{
              dir = "asc"
            }
        
        var rows = Array.prototype.slice.call(tbody.rows, 0);
      
        rows = rows.sort(function(a, b) {
        
        var dataA;
        var dataB;
          
          if(a.cells.item(column).getElementsByTagName("a").length > 0){
            dataA = (a.cells.item(column).getElementsByTagName("a")[0].innerHTML) ? a.cells.item(column).getElementsByTagName("a")[0].innerHTML : "";
            dataB = (b.cells.item(column).getElementsByTagName("a")[0].innerHTML) ? b.cells.item(column).getElementsByTagName("a")[0].innerHTML : "";
          }else{
            dataA = (a.cells.item(column).innerHTML) ? a.cells.item(column).innerHTML : "";
            dataB = (b.cells.item(column).innerHTML) ? b.cells.item(column).innerHTML : "";
          }
          
          dataA = dataA.replace(/[&\/\\#,+()$~%.'":*?<>{} _-]/g,"").toUpperCase();
          dataB = dataB.replace(/[&\/\\#,+()$~%.'":*?<>{} _-]/g,"").toUpperCase();
      
        if (dataA === dataB) {
              return 0;
          }
          else {
            if(dir.toUpperCase() == "ASC"){
              return (dataA < dataB) ? -1 : 1;
            }else{
              return (dataA > dataB) ? -1 : 1;
            }
              
          }
      });
        
      for (i = 0; i < rows.length; i++) {
        var tempRow = rows[i]
        var className = rows[i].className;
        var additionalIdentifier = null;
        //check if script values
	        if(className.toUpperCase().indexOf("SCRIPT") > -1){
	        	additionalIdentifier = "script"
	        }
	    //check if even or odd
            if(i%2){
              tempRow.setAttribute("class",(additionalIdentifier) ? additionalIdentifier + "Even" : "even");
            }else{
              tempRow.setAttribute("class",(additionalIdentifier) ? additionalIdentifier + "Odd" : "odd");
            }
        tbody.appendChild(tempRow);
       }
      
        
        for(j = 0; j < thead.getElementsByTagName("TH").length; j++){
          if(j == column){
            try{
            	thead.getElementsByTagName("TH")[j].setAttribute("sortdir",dir)
				<#if fileBasedLinks>
					thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOn")[0].setAttribute("src","../images/" + dir + "SortArrow.png");
					thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOff")[0].setAttribute("src","../images/" + dir + "SortArrowHover.png");
			  	<#else>
					thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOn")[0].setAttribute("src","${imgBaseUrl}" + dir + "SortArrow.png");
					thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOff")[0].setAttribute("src","${imgBaseUrl}" + dir + "SortArrowHover.png");
			  	</#if>
             }catch(error){
              
             }
          }else{
            try{
             	thead.getElementsByTagName("TH")[j].setAttribute("sortdir","")
	            thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOn")[0].setAttribute("src","");
	            thead.getElementsByTagName("TH")[j].getElementsByClassName("imgOff")[0].setAttribute("src","");
             }catch(error){
               
             }
            
          }
        }
    }
  </script>
	</body>
</html>