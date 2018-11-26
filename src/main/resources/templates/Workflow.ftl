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
	
	td{
		vertical-align:top;
		text-align: left;
		padding: 8px 3px 3px 15px;
		white-space: nowrap;
		display: table-cell;
		border-collapse: collapse;
		border-spacing: 2px;
		min-width:150px;
	}

	td.colHeader{
		vertical-align:top;
		text-align: left;
		padding: 8px 3px 3px 7px;
		white-space: nowrap;
		font-size: 16px;
		background: #dee3e9;
		font-weight: bold;
		display: table-cell;
		border-collapse: collapse;
		border-spacing: 2px;
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
	}

	td.scriptInfoHeader{
		background-color: #FFFFFF;
		vertical-align:top;
		text-align: left;
		padding: 8px 3px 3px 7px;
		white-space: nowrap;
		font-size: 14px;
		font-weight: bold;
		display: table-cell;
		border-collapse: collapse;
		border-spacing: 2px;
	}

	td.scriptInfo{
		vertical-align:top;
		font-family: 'DejaVu Sans Mono', monospace;
		font-size: 14px;
		margin-top: 8px;
		line-height: 1.4em;
		white-space: nowrap;
		border-collapse: collapse;
		border-spacing: 2px;
		text-align: left;
		padding: 8px 3px 3px 7px;
		background-color: #FFFFFF;
		display: table-cell;
		vertical-align: inherit;
		border-color: inherit;
	}

	tr.scriptOdd{
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

	tr.scriptEven{
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
		table-layout: fixed;
		word-wrap: break-word;
	}

	.h2Copy{
	    display: block;
	    margin-top: 0.83em;
	    margin-bottom: 0.83em;
	    margin-right: 0;
	    font-weight: bold;
		font-size: 16px;
	    font-style: italic;
	    margin-left: 7px;
	    width:100%;
	}

	.arrow{
		padding:0px;
		margin:0px;
	}

	.noSelect {
	    -webkit-touch-callout: none;
	    -webkit-user-select: none;
	    -khtml-user-select: none;
	    -moz-user-select: none;
	    -ms-user-select: none;
	    user-select: none;
	}

	.collapsible{
		margin: 0px;
		padding:10px 0px;
		display:block;
	}

	.collapsible:hover {
	  background-color: #807FEF;
	  cursor: pointer;
	}

	#expandSymbol{
	  color: black;
	  font-weight: bold;
	  float: right;
	  padding-right: 10px;
	}

	.content {
	  padding: 0 18px;
	  max-height: 0;
	  overflow: hidden;
	  transition: max-height 0.2s ease-out;
	  background-color: white;
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
	  <title>${(name)! systemId}</title>
	</head>
	<body>
	  <div id="scriptData">
	  <base target="_parent">

	  <span class="h1copy">${(name)! systemId}</span>
	  <span>Documentation generated on:</span>
	  <span>${.now?date}</span>

	  <div class="tableBlock">
	  	<span class="h2Copy">Basic Info</span>
	  	<table class="data">
	  		<tr>
	  			<td class="scriptInfoHeader">Name: </td>
	  		</tr>

	  		<tr>
	  			<td class="scriptInfo">${(name)!" "}</td>
	  		</tr>

	  		<tr>
	  			<td class="scriptInfoHeader">System Id: </td>
	  		</tr>

	  		<tr>
	  			<td class="scriptInfo">${(systemId)!" "}</td>
	  		</tr>

	  		<tr>
	  			<td class="scriptInfoHeader">Description: </td>
	  		</tr>

	  		<tr>
	  			<td class="scriptInfo">${(description)!" "}</td>
	  		</tr>
	  	</table>
	</div>

	<#if usesCustomParams?has_content>	
		<div class="tableBlock">
			<span class="h2Copy">Uses the following custom params:</span>
			<table class="data">
				<thead>
					<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(0,this.parentElement.parentElement.parentElement)" sortdir="">Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
				</thead>
				<tbody>
				<#foreach uses in usesCustomParams?values> 
					<tr class='${["scriptOdd", "scriptEven"][uses_index%2]}'>
		  				<td class="default">
		  					<#if fileBasedLinks>
		  						<a href="file:../customparams/${uses.name}.html">${uses.name}</a>
		  					<#else>
		  						<a href="${articleBaseUrl + uses.name?replace('_',replaceSpacesInUrlsWith,'r')}">${uses.name}</a>
		  					</#if>
		  				</td>
		  			</tr>
				</#foreach>
			</tbody>
	  		</table>
			 
		</div>
	</#if>

	<#if usesScripts?has_content>	
		<div class="tableBlock">
			<span class="h2Copy">Uses the following scripts:</span>
			<table class="data">
				<thead>
					<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(0,this.parentElement.parentElement.parentElement)" sortdir="">Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
					<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(1,this.parentElement.parentElement.parentElement)" sortdir="">System Id <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
				</thead>
				<tbody>
					<#foreach uses in usesScripts?values> 
						<tr class='${["scriptOdd", "scriptEven"][uses_index%2]}'>
			  				<td class="default">
			  					<#if fileBasedLinks>
			  						<a href="file:../scripts/${uses.systemId}.html">${(uses.name)!uses.systemId}</a>
			  					<#else>
			  						<a href="${articleBaseUrl + uses.systemId?replace('_',replaceSpacesInUrlsWith,'r')}">${(uses.name)!uses.systemId}</a>
			  					</#if>
			  				</td>
			  				<td class="default">${(uses.systemId)!" "}</td>
			  			</tr>
					</#foreach>
				</tbody>
	  		</table>
			 
		</div>
	</#if>

	<#if usesCustomFields?has_content>	
		<div class="tableBlock">
			<span class="h2Copy">Uses the following custom fields:</span>
			<table class="data">
				<thead>
					<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(0,this.parentElement.parentElement.parentElement)" sortdir="">Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
					<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(1,this.parentElement.parentElement.parentElement)" sortdir="">System Id <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
				</thead>
				<tbody>
					<#foreach cf in usesCustomFields?values> 
						<tr class='${["scriptOdd", "scriptEven"][cf_index%2]}'>
							<td class="default">
								<#if fileBasedLinks>
			  						<a href="file:../customfields/${cf.systemId}.html">${(cf.name)!" "}</a>
			  					<#else>
			  						<a href="${articleBaseUrl + cf.systemId?replace('_',replaceSpacesInUrlsWith,'r')}">${(cf.name)!" "}</a>
			  					</#if>
			  				</td>
			  				<td class="default">${(cf.systemId)!" "}</td>
			  			</tr>
					</#foreach>
				</tbody>
	  		</table>
			 
		</div>
	</#if>

	<#if actionUsages?has_content>	
		<div class="tableBlock">
			<span class="h2Copy">Used in the following actions:</span>
			<#if type == "Workflow">
				<table class="data" id="data">
					<thead>
						<tr>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(0,this.parentElement.parentElement.parentElement)" sortdir="">Workflow <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(1,this.parentElement.parentElement.parentElement)" sortdir="">Action Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(2,this.parentElement.parentElement.parentElement)" sortdir="">Action Type <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(3,this.parentElement.parentElement.parentElement)" sortdir="">Start Status <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(4,this.parentElement.parentElement.parentElement)" sortdir="">End Status <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
						</tr>
					</thead>
					<tbody>
						<#list actionUsages as usage>
							<#if usage.Workflow?? && usage.ActionName?? && usage.ActionType?? && usage.StartStatus?? && usage.EndStatus??>
								<p>${usage.Workflow}</p>
								<tr class='${["scriptOdd", "scriptEven"][usage_index%2]}'>
									<td class="default">
										<#if fileBasedLinks>
											<a href="file:../workflows/${(workflowNames[usage.Workflow].systemId)}.html">${usage.Workflow}</a>
					  					<#else>
					  						<a href="${articleBaseUrl + workflowNames[usage.Workflow].systemId?replace('_',replaceSpacesInUrlsWith,'r')}">usage.Workflow</a>
					  					</#if>
									</td>
									<td class="default"  style="white-space:normal;">${(usage.ActionName)!" "}</td>
									<td class="default">${(usage.ActionType)!" "}</td>
									<td class="default">${(usage.StartStatus)!" "}</td>
									<td class="default">${(usage.EndStatus)!" "}</td>
								</tr>
							</#if>
						</#list>
					</tbody>
		  		</table>
		  	<#else>
		  		<table class="data" id="data">
		  			<thead>
						<tr>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(0,this.parentElement.parentElement.parentElement)" sortdir="">Workflow <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(1,this.parentElement.parentElement.parentElement)" sortdir="">Action Name <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(2,this.parentElement.parentElement.parentElement)" sortdir="">Action Type <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(3,this.parentElement.parentElement.parentElement)" sortdir="">Start Status <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(4,this.parentElement.parentElement.parentElement)" sortdir="">End Status <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
							<th class="colHeader clickableHeader noSelect imgHover" onclick="sortTable(5,this.parentElement.parentElement.parentElement)" sortdir="">When Executed <img src="" class="imgOn" style='width:14px'><img src="" class="imgOff" style='width:14px'></th>
						</tr>
					</thead>
					<tbody>
						<#list actionUsages as usage>
							<#if usage.Workflow?? && usage.ActionName?? && usage.ActionType?? && usage.StartStatus?? && usage.EndStatus??>
								<tr class='${["scriptOdd", "scriptEven"][usage_index%2]}'>
									<td class="default">
										<#if fileBasedLinks>
											<a href="file:../workflows/${(workflowNames[usage.Workflow].systemId)}.html">${usage.Workflow}</a>
					  					<#else>
					  						<a href="${articleBaseUrl + workflowNames[usage.Workflow].systemId?replace('_',replaceSpacesInUrlsWith,'r')}">${usage.Workflow}</a>
					  					</#if>
									</td>
									<td class="default" style="white-space:normal;">${(usage.ActionName)!" "}</td>
									<td class="default">${(usage.ActionType)!" "}</td>
									<td class="default">${(usage.StartStatus)!" "}</td>
									<td class="default">${(usage.EndStatus)!" "}</td>
									<td class="default"  style="white-space:normal;">${(usage.WhenExecuted)!" "}</td>
								</tr>
							</#if>
						</#list>
					</tbody>
		  		</table>
		  	</#if>
		</div>
	</#if>

	<script>
		function toggleCollapse(ele){
		    ele.classList.toggle("active");
		    var content = ele.nextElementSibling;
		    var expandSymbol = document.getElementById("expandSymbol")
		    if (content.style.maxHeight){
		      content.style.maxHeight = null;
		      expandSymbol.innerHTML = "+"
		    } else {
		      content.style.maxHeight = content.scrollHeight + "px";
		      expandSymbol.innerHTML = "-"
		    } 
		}
		
	</script>

		<script>
      function sortTable(column, table) {
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
          
          dataA = dataA.replace(/[&\/\\#,+()$~%.'":*?<>{} _-]/g," ").replace(/\s+/g," ").toUpperCase();
          dataB = dataB.replace(/[&\/\\#,+()$~%.'":*?<>{} _-]/g," ").replace(/\s+/g," ").toUpperCase();
      
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
	</div>
	</body>
</html>