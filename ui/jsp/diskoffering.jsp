<%@ page import="java.util.*" %>

<%@ page import="com.cloud.utils.*" %>

<%
    Locale browserLocale = request.getLocale();
    CloudResourceBundle t = CloudResourceBundle.getBundle("resources/resource", browserLocale);
%>



<div class="main_title" id="right_panel_header">
    
    <div class="main_titleicon">
        <img src="images/title_diskofferingicon.gif" /></div>
    
    <h1>
        Disk Offering
    </h1>
</div>
<div class="contentbox" id="right_panel_content">
    <div class="info_detailbox errorbox" id="after_action_info_container_on_top" style="display: none">
        <p id="after_action_info">
        </p>
    </div>
    <div class="tabbox" style="margin-top: 15px;">
        <div class="content_tabs on">
            <%=t.t("details")%></div>
    </div>
    <div id="tab_content_details">
    	<div id="tab_spinning_wheel" class="rightpanel_mainloader_panel" style="display: none;">
            <div class="rightpanel_mainloaderbox">
                <div class="rightpanel_mainloader_animatedicon">
                </div>
                <p>
                    Loading &hellip;</p>
            </div>
        </div>
        <div id="tab_container"> 
	        <div class="grid_container">
	        	<div class="grid_header">
	            	<div id="grid_header_title" class="grid_header_title">(title)</div>
	                <div id="action_link" class="grid_actionbox"><p>Actions</p>
	                    <div class="grid_actionsdropdown_box" id="action_menu" style="display: none;">
	                        <ul class="actionsdropdown_boxlist" id="action_list">
	                        	<li><%=t.t("no.available.actions")%></li>
	                        </ul>
	                    </div>
	                </div>
	                <div class="gridheader_loaderbox" id="spinning_wheel" style="border: 1px solid #999;
	                display: none;">
	                    <div class="gridheader_loader" id="Div1">
	                    </div>
	                    <p id="description">
	                        Waiting &hellip;</p>
	                </div>
	            </div>
	            <div class="grid_rows odd">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("ID")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="id">
	                    </div>
	                </div>
	            </div>
	            <div class="grid_rows even">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("name")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="name">
	                    </div>                    
	                    <input class="text" id="name_edit" style="width: 200px; display: none;" type="text" />
	                    <div id="name_edit_errormsg" style="display:none"></div>                       
	                </div>
	            </div>
	            <div class="grid_rows odd">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("display.text")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="displaytext">
	                    </div>                    
	                    <input class="text" id="displaytext_edit" style="width: 200px; display: none;" type="text" />
	                    <div id="displaytext_edit_errormsg" style="display:none"></div>                       
	                </div>
	            </div>
	            <div class="grid_rows even">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("disk.size")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="disksize">
	                    </div>
	                </div>
	            </div>  
	            <div class="grid_rows odd">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("tags")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="tags">
	                    </div>
	                    <input class="text" id="tags_edit" style="width: 200px; display: none;" type="text" />
	                    <div id="tags_edit_errormsg" style="display:none"></div>  
	                </div>
	            </div>
	            <div class="grid_rows even">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("domain")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="domain">
	                    </div>
	                    <select class="select" id="domain_edit" style="width: 202px; display: none;">	                       
	                    </select>	
	                </div>
	            </div>      
	            <!-- 	            
	            <div class="grid_rows odd">
	                <div class="grid_row_cell" style="width: 20%;">
	                    <div class="row_celltitles">
	                        <%=t.t("customizable.during.VM.creation")%>:</div>
	                </div>
	                <div class="grid_row_cell" style="width: 79%;">
	                    <div class="row_celltitles" id="isCustomized">
	                    </div>
	                </div>
	            </div>  
	             -->   
	        </div>        
	        <div class="grid_botactionpanel">
	        	<div class="gridbot_buttons" id="save_button" style="display:none;">Save</div>
	            <div class="gridbot_buttons" id="cancel_button" style="display:none;">Cancel</div>
	        </div>  
        </div>
    </div>
</div>

<!--  top buttons (begin) -->
<div id="top_buttons">
    <div class="actionpanel_button_wrapper" id="add_diskoffering_button">
        <div class="actionpanel_button">
            <div class="actionpanel_button_icons">
                <img src="images/addvm_actionicon.png" alt="Add Disk Offering" /></div>
            <div class="actionpanel_button_links">
                Add Disk Offering
            </div>
        </div>
    </div>
</div>
<!--  top buttons (end) -->

<!-- Add Disk Offering Dialog -->
<div id="dialog_add_disk" title="Add Disk Offering" style="display:none">
	<p>Please fill in the following data to add a new disk Offering.</p>
	<div class="dialog_formcontent">
		<form action="#" method="post" id="form1">
			<ol>
				<li>
					<label for="user_name">Name:</label>
					<input class="text" type="text" name="add_disk_name" id="add_disk_name"/>
					<div id="add_disk_name_errormsg" class="dialog_formcontent_errormsg" style="display:none;" ></div>
				</li>
				<li>
					<label for="user_name">Description:</label>
					<input class="text" type="text" name="add_disk_description" id="add_disk_description"/>
					<div id="add_disk_description_errormsg" class="dialog_formcontent_errormsg" style="display:none;" ></div>
				</li>	
				<li>
					<label>Custom disk size?:</label>
					<select class="select" id="customized">						
						<option value="false">No</option>
						<option value="true">Yes</option>
					</select>
				</li>				
				<li id="add_disk_disksize_container">
					<label for="user_name">Disk size (in GB):</label>
					<input class="text" type="text" name="add_disk_disksize" id="add_disk_disksize"/>
					<div id="add_disk_disksize_errormsg" class="dialog_formcontent_errormsg" style="display:none;" ></div>
				</li>				
				<li id="add_disk_tags_container">
                    <label for="add_disk_tags">
                        Tags:</label>
                    <input class="text" type="text" id="add_disk_tags" />
                    <div id="add_disk_tags_errormsg" class="dialog_formcontent_errormsg" style="display: none;">
                    </div>
                </li>	
                <li>
				    <label>
				        Public?:</label>
				    <select class="select" id="public_dropdown">
				        <option value="true">Yes</option>
				        <option value="false">No</option>
				    </select>
				</li>
				<li id="domain_dropdown_container" style="display: none">
				    <label>
				        Domain:</label>
				    <select class="select" id="domain_dropdown">
				    </select>
				</li>    	
			</ol>
		</form>
	</div>
</div>


<div id="hidden_container">
    <!-- advanced search popup (begin) -->
    <div class="adv_searchpopup_bg" id="advanced_search_popup" style="display: none;">
        <div class="adv_searchformbox">
            <form action="#" method="post">
            <ol>               
                <li id="adv_search_domain_li" style="display: none;">
                    <select class="select" id="adv_search_domain">
                    </select>
                </li>
            </ol>
            </form>
        </div>
    </div>
    <!-- advanced search popup (end) -->
</div>
