/*
 * Open-T XML Forms
 * Copyright 2010-2013, Open-T B.V., and individual contributors as indicated
 * by the @author tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License
 * version 3 published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package org.open_t.forms
import java.text.*;
import org.apache.commons.lang.WordUtils
class FormTagLib {
	static namespace="form"
	def solrService
	
	def form = { attrs, body ->
		def enctype=""
		def style=""
		def outcomes = attrs.outcomes
		
		def handlerIsRequestor = "false"
		
		if (attrs.enctype) { enctype="""enctype="${attrs.enctype}" """}
		if (attrs.style) { style="""style="${attrs.style}" """}
		def formHead="""<div title="${attrs.title}" ${style} id="${attrs.name}" class="xml-form modal hide xfade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
						<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
								<div id="myModalLabel"><span class="modal-header">${attrs.title}</span>&nbsp;<span class="modal-explanation">${attrs.explanation}</span></div></div>
						"""
		// explanation
		formHead+="""<form class="form-horizontal xml-form" action="${attrs.action}" id="form" name="form" method="post" ${enctype} >"""
		formHead+="""<div class="modal-body">"""
				
        formHead+="""<fieldset>"""
		formHead+="""<input type="hidden" name="form" value="${attrs.name}" />"""
		formHead+="""<input type="hidden" name="process" value="${attrs.process}" />"""
		
		
			out << formHead
			out << body()
			out << """</div><div class="aaform-actions modal-footer">"""
			
		switch (attrs.type) {
			case "request":							
				out << """<input type="submit" id="submit" value="${message(code:'submit')}" name="submit" class="btn btn-primary" role="button"  ></input>"""				
			break
			
			case "edit":							
					out << """<input type="submit" id="submit" value="${message(code:'save',default:'Save')}" name="submit" class="btn" role="button"  ></input>"""				
			break
			
			case "show" :
				out <<""
			break
			
			case "task" :			
				if(attrs.outcomes) {
					attrs.outcomes.split(",").each { outcome ->
						out << """<input type="submit" id="submit" value="${outcome}" name="submit" class="button btn" role="button"  />"""
					}
			    }				
			break			
		}
	    out << """</div></fieldset></form></div>"""
	}
	
	def attributeString(attributeName,attributeValue) {
		if (attributeValue && attributeValue.length()>0) { 
			return """${attributeName}="${attributeValue}" """
		} else {
			return ""
		}
	}
	
	def expandAttribs(attrs) {
		def attribs=""
		attrs.each { attrKey, attrValue -> attribs+=""" ${attrKey}="${attrValue}" """ }
		return attribs
	}
	
	def input = { attrs, body ->
		
		// Copy all extra attributes, skip the ones that are only meaningful for textField or are handled manually
		def newAttrs=attrs.clone()
		def attribs=""
		def skipAttrs=['class','type','value','name','id']
		attrs.each { attrKey, attrValue ->
			if (!skipAttrs.contains(attrKey))
			{
				attribs+=""" ${attrKey}="${attrValue}" """
			 }
		}
			
		out << """<div class="dialog-horizontal-wrapper">"""
		switch (attrs.type) {
			
			case "text":				
				newAttrs.name="update-${attrs.gpath}"
				newAttrs.id="update-${attrs.gpath}"				
				out << """<input ${expandAttribs(newAttrs)} />"""
			break
			case "date":
			  def xsDateformatter = new SimpleDateFormat("yyyy-MM-dd")
			  def dateFormatter = new SimpleDateFormat(g.message(code: 'input.date.format',default:"yyyy-MM-dd HH:mm:ss z"))
			  def dateValue
			  if (attrs.value.text().length() > 0) {
			    dateValue = xsDateformatter.parse(attrs.value.text())
			  }
			  def classes = attrs.class.split(" \\s*")
			  def inputClass = ""
			  def hiddenClass = ""
			  classes.each {
			    if (it == "required") {
		        hiddenClass += "${it} "
			    }
			    else {
			      inputClass += "${it} "
			    }
			  }
				out << """<input id="entry-${attrs.gpath}" name="entry-${attrs.gpath}" type="text" class="${inputClass} datepicker" ${attribs} value="${dateValue ? dateFormatter.format(dateValue) : ''}"  />"""
				out << """<input id="update-${attrs.gpath}" name="update-${attrs.gpath}" type="hidden" class="${hiddenClass}" value="${attrs.value}" />"""
			break
			case "textarea":
				newAttrs.name="update-${attrs.gpath}"
				newAttrs.id="update-${attrs.gpath}"
				newAttrs.remove('value')				
				out << """<textarea ${expandAttribs(newAttrs)} >${attrs.value?:""}</textarea>"""
			break
			case "checkbox":
				def checked=""
				if (attrs.value=="true") {
					checked="""checked="checked" """
				}
				out << """<input ${checked} class="${attrs.class}" name="entry-${attrs.gpath}" value="${attrs.value}" id="entry-${attrs.gpath}" type="checkbox"  />"""
				out << """<input name="update-${attrs.gpath}" value="${attrs.value}" id="update-${attrs.gpath}" type="hidden" />"""
			break
			case "datetime":
			  def xsDateTimeformatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
			  def dateTimeFormatter = new SimpleDateFormat(g.message(code: 'input.dateTime.format'))
			  def dateTimeValue
			  if (attrs.value.text().length() > 0) {
			    dateTimeValue = xsDateTimeformatter.parse(attrs.value.text())
			  }
			  def classes = attrs.class.split(" \\s*")
			  def inputClass = ""
			  def hiddenClass = ""
			  classes.each {
			    if (it == "required") {
					hiddenClass += "${it} "
			    }
			    else {
			      inputClass += "${it} "
			    }
			  }
				out << """<input id="hidden-${attrs.gpath}" name="hidden-${attrs.gpath}" type="text" class="${inputClass}" ${attribs} value="${dateTimeValue ? dateTimeFormatter.format(dateTimeValue) : ''}" >"""
				out << """<input id="update-${attrs.gpath}" name="update-${attrs.gpath}" type="hidden" class="${hiddenClass}" value="${attrs.value}" ${title} />"""
			break
			case "time":
			  def xsTimeformatter = new SimpleDateFormat("HH:mm:ss")
			  def timeFormatter = new SimpleDateFormat(g.message(code: 'input.time.format'))
			  def timeValue
			  if (attrs.value.text().length() > 0) {
			    timeValue = xsTimeformatter.parse(attrs.value.text())
			  }
			  def classes = attrs.class.split(" \\s*")
			  def inputClass = ""
			  def hiddenClass = ""
			  classes.each {
			    if (it == "required") {
		        hiddenClass += "${it} "
			    }
			    else {
			      inputClass += "${it} "
			    }
			  }
				out << """<input id="hidden-${attrs.gpath}" name="hidden-${attrs.gpath}" type="text" class="${inputClass}" ${attribs} value="${timeValue ? timeFormatter.format(timeValue) : ''}" />"""
				out << """<input id="update-${attrs.gpath}" name="update-${attrs.gpath}" type="hidden" class="${hiddenClass}" value="${attrs.value}" />"""
			break
			
			
		}
		
		if (attrs.helpTitle || attrs.helpBody) {
		  out << """&nbsp;<span class="help-icon help action" title="${attrs.helpTitle}" data-content="${attrs.helpBody}" href="#">&nbsp;</span>"""
		}
		
		out <<"</div>"
		
	}
	
	def select = { attrs, body ->
        	def title = attrs.title ? """title="${attrs.title}" """ : ""
		
		def options
		attrs.options.each { def item ->
			String attrValue=attrs.value ? attrs.value : ""
			String key = item.getKey() ? item.getKey() : ""
			String value=item.getValue() ? item.getValue() : ""
			options = """${options ? options : ""}<option value="${key}"${key == attrValue ? ' selected' : ''}>${value}</option>"""
		}
		
		out << """<select class="${attrs.class}" name="update-${attrs.gpath}" id="update-${attrs.gpath}" ${title}>${options}</select>"""
		
		if (attrs.helpTitle || attrs.helpBody) {
		  out << """&nbsp;<a class="help-icon help action" title="${attrs.helpTitle}" data-content="${attrs.helpBody}"  href="#">&nbsp;</a>"""
		}
	}
	
	def radioButtonList = { attrs, body ->
		def title = attrs.title ? """title="${attrs.title}" """ : ""
		
		def radioButtons
		
		attrs.options.each { def item ->
			if (item.getKey() != "") {
				radioButtons = """${radioButtons ? radioButtons : ""}<tr><td><input class="${attrs.class}" name="update-${attrs.gpath}" value="${item.getKey()}" id="update-${attrs.gpath}" ${title} type="radio" ${item.getKey() == attrs.value ? "selected" : ""}/></td><td>${item.getValue()}</td><td>${!radioButtons && (attrs.helpTitle || attrs.helpBody) ? """<a class="help-icon help action" title="${attrs.helpTitle}|${attrs.helpBody}" href="#">&nbsp;</a>""" : ""}</td></tr>"""
			}
		}
		
		if (!radioButtons) {
			radioButtons = """<tr><td>&nbsp;<a class="help-icon help action" title="${attrs.helpTitle}" data-content="${attrs.helpBody}"  href="#">&nbsp;</a></td></tr>"""
		}
		
		out << """<table>${radioButtons}</table>"""
	}

	def output = { attrs, body ->
		out << """<div class="dialog-horizontal-wrapper">"""
 
	switch (attrs.type) {
		case "text":				
			out << """<span class="${attrs.class}" id="output-${attrs.gpath}">${attrs.value.toString().replaceAll('\n', '<br />')}</span>"""				
		break
		case "date":
			def xsDateformatter = new SimpleDateFormat("yyyy-MM-dd")
			def dateFormatter = new SimpleDateFormat(g.message(code: 'output.date.format'))
			def dateValue
			if (attrs.value.text().length() > 0) {
			  dateValue = xsDateformatter.parse(attrs.value.text())
			}
			out << """<span class="${attrs.class}" id="output-${attrs.gpath}">${dateValue ? dateFormatter.format(dateValue) : ''}</span>"""
		break
		case "textarea":
			out << """<span class="${attrs.class}" id="output-${attrs.gpath}">${attrs.value.toString().replaceAll('\n', '<br />')}</span>"""
		break
		case "datetime":
			def xsDateTimeformatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
			def dateTimeFormatter = new SimpleDateFormat(g.message(code: 'output.dateTime.format'))
			def dateTimeValue
			if (attrs.value.text().length() > 0) {
			  dateTimeValue = xsDateTimeformatter.parse(attrs.value.text())
			}
			out << """<span class="${attrs.class}" id="output-${attrs.gpath}">${dateTimeValue ? dateTimeFormatter.format(dateTimeValue) : ''}</span>"""
		break
		case "time":
			def xsTimeformatter = new SimpleDateFormat("HH:mm:ss")
			def timeFormatter = new SimpleDateFormat(g.message(code: 'output.time.format'))
			def timeValue
			if (attrs.value.text().length() > 0) {
			  timeValue = xsTimeformatter.parse(attrs.value.text())
			}
			out << """<span class="${attrs.class}" id="output-${attrs.gpath}">${timeValue ? timeFormatter.format(timeValue) : ''}</span>"""
		break
		}
		
		if (attrs.helpTitle || attrs.helpBody) {
		  out << """&nbsp;<a class="help-icon help action" title="${attrs.helpTitle}" data-content="${attrs.helpBody}"  href="#">&nbsp;</a>"""
		}
		out << """</div>"""
	
	}

	def comment={attrs,body ->
	
		def formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
		
		def rows=attrs.rows?:10
		def cols=attrs.cols?:80
	
		def html="""<ul class="comment">"""
		def doc=attrs.document
		
		doc.header.comments.comment.each { comment ->
			def formattedCommentDate="unknown"
			if(comment.dateTime) {
				def commentDate=(Date)formatter.parse(comment.dateTime.text())
				formattedCommentDate=commentDate.dateTimeString
			}
		    html+="""<li><div class="comment-item">
								<div class="comment-author"><img src="${createLink(action:'avatar',id:comment.user.text())}"/><span class="fn">${comment.user.text()}</span></div>
								<div class="comment-text">${comment.text.text()}</div>
								<div class="comment-date">${message(code:'form.comment.date.label')} <span class="date">${formattedCommentDate}</span></div>
							</div></li>"""
		}
		def value = attrs.value ? attrs.value : ""
		if (attrs.mode && (attrs.mode=="edit" || attrs.mode=="create")) {
			html+="""<li><div class="newcomment"><h5>${message(code:'form.comment.add.label')}</h5><textarea class="input-xxlarge" rows="${rows}" cols="${cols}" name="comment" >${value}</textarea>"""
			if (attrs.helpTitle || attrs.helpBody) {
				html+= """&nbsp;<a href="#" class="help-icon help action" title="${attrs.helpTitle}" data-content="${attrs.helpBody}"  >&nbsp;</a>"""
			}
			html +="</div></li>"
				
		}
		html+="</ul>"
	  
		
		
		out << html
	
		
	  
	
  }
  //Determine if current user has said feature
  def hasFeature = {attrs,body ->
	  println "The features are: ${session.features}"
	  if (session.features && session.features.contains(attrs.feature)) {
		  out << body()
	  }
  }
  
  def outcome = {attrs,body ->
  	out << """<input type="submit" id="submit" value="${body()}" name="submit" class="button ui-button ui-widget ui-state-default ui-corner-all" role="button" aria-disabled="false" />"""
  }
  /* 
   * Select a value from a Solr search result
   */
  def solrSelect = { attrs,body ->
	  def query=attrs.query?:"*:*"
	  def max=attrs.max?:100
	  def mockListConfig=[core:'Document']
	  
	  def keyField=attrs.keyField?:"documentDescription"
	  def valueField=attrs.valueField?:"documentDescription"
	  
	  def rsp=solrService.search(attrs.authenticationService,mockListConfig,query,[max:max]).collectEntries { item ->
		  def key =  item."${keyField}"
		  def value =  item."${valueField}"
		  [key,value]
	  }
	  attrs.options=rsp
	  out <<form.select(attrs,body)
  }
  /**
   * Create a bootstrap form line
   * attributes
   * id    - the id of the input element. Used for the for= and as default key for the label
   * label - Label to be shown
   */
  def line= {attrs,body ->
	def id=attrs.id?:""
	
	def label=attrs.label?:message(code:'formline.'+id,default:WordUtils.capitalize(id))
	def html = """
	<div class="control-group" >
	<label class="control-label " for="${id}">${label}</label>
	<div class="controls ">"""	
	out << html
    out << body()
    out << """</div></div>"""
  }
  
}
