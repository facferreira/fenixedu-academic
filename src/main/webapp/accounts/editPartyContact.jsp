<%--

    Copyright © 2002 Instituto Superior Técnico

    This file is part of FenixEdu Academic.

    FenixEdu Academic is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Academic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page isELIgnored="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<html:xhtml />
<bean:define id="partyContactClass" scope="request" name="partyContactClass" />
<bean:define id="contactType" name="partyContact" property="type.name" />

<h2><bean:message key="<%= "label.partyContacts.edit" +  partyContactClass %>" bundle="ACADEMIC_ADMIN_OFFICE"/></h2>

<html:messages id="message" message="true" bundle="ACADEMIC_ADMIN_OFFICE">
    <p><span class="error0"><!-- Error messages go here --><bean:write name="message" /></span>
    </p>
</html:messages>
<bean:define id="personID" name="partyContact" property="party.externalId" />

<logic:equal name="partyContactClass" value="PhysicalAddress">
		<bean:define id="physicalAddressBean"  name="partyContact" type="org.fenixedu.academic.dto.contacts.PhysicalAddressBean" />

		<logic:equal name="partyContact" property="contact.fiscalAddress" value="true">
			<p><em class="infoop2"><bean:message key="message.PhysicalAddress.cannot.change.countryOfResidence.in.fiscal.address" bundle="ACADEMIC_ADMIN_OFFICE" /></em></p>
		</logic:equal>

		<fr:edit id="edit-contact" name="partyContact" action="<%="/accounts/partyContacts.do?method=editPartyContact&personID=" + personID %>">
	       	<fr:schema type="org.fenixedu.academic.dto.contacts.PhysicalAddressBean" bundle="ACADEMIC_ADMIN_OFFICE">
				<fr:slot name="type" required="true" />
				<fr:slot name="defaultContact" key="label.partyContacts.defaultContact" />
				
				<logic:equal name="partyContact" property="contact.fiscalAddress" value="true">
				<fr:slot name="countryOfResidence" layout="menu-select-postback" validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator" readOnly="true">
					<fr:property name="providerClass" value="org.fenixedu.academic.ui.renderers.providers.DistinctCountriesProvider" />
					<fr:property name="format" value="${localizedName.content}" />
				</fr:slot>
				</logic:equal>

				<logic:equal name="partyContact" property="contact.fiscalAddress" value="false">
				<fr:slot name="countryOfResidence" layout="menu-select-postback" validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
					<fr:property name="providerClass" value="org.fenixedu.academic.ui.renderers.providers.DistinctCountriesProvider" />
					<fr:property name="format" value="${localizedName.content}" />
					<fr:property name="destination" value="postback-select-country" />
					<fr:property name="sortBy" value="name=asc" />
				</fr:slot>
				</logic:equal>
	       		
				<fr:slot name="address" required="true">
					<fr:property name="size" value="50" />
				</fr:slot>
				
				<%-- 
				<fr:slot name="area">
					<% if(physicalAddressBean.getCountryOfResidence() != null && physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
					<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator" />
					<% } %>
				</fr:slot>
				--%>
				
				<fr:slot name="areaCode">
					<fr:property name="size" value="10" />
					<% if(physicalAddressBean.getCountryOfResidence() != null && physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
					<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RegexpValidator">
						<fr:property name="regexp" value="\d{4}-\d{3}" />
						<fr:property name="message" value="error.areaCode.invalidFormat" />
						<fr:property name="key" value="true" />
						<fr:property name="bundle" value="ACADEMIC_ADMIN_OFFICE" />
					</fr:validator>
					<% } %>
				</fr:slot>

				<% if(physicalAddressBean.getCountryOfResidence() != null && physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
				<fr:slot name="parishOfResidence" required="true" />
				<% } %>

				<% if(physicalAddressBean.getCountryOfResidence() == null || physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
				<fr:slot name="districtSubdivisionOfResidence" required="true" />
				<% } %>

				<% if(physicalAddressBean.getCountryOfResidence() != null && !physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
				<fr:slot name="districtSubdivisionOfResidence" required="true" key="label.districtSubdivisionOfResidence.city" bundle="ACADEMIC_ADMIN_OFFICE" />
				<% } %>

				<% if(physicalAddressBean.getCountryOfResidence() != null && physicalAddressBean.getCountryOfResidence().isDefaultCountry()) { %>
				<fr:slot name="districtOfResidence" required="true" />
				<% } %>
				
	       	</fr:schema>
	        <fr:layout name="tabular-editable">
	            <fr:property name="classes" value="tstyle5 thlight thright mtop025 thmiddle" />
	            <fr:property name="columnClasses" value=",,tdclear tderror1" />
	        </fr:layout>
	        <fr:destination name="postback-select-country" path="/accounts/partyContacts.do?method=postbackSelectCountry&form=edit" />
	        <fr:destination name="invalid" path="/accounts/partyContacts.do?method=invalid&form=edit"/>
		    <fr:destination name="cancel" path="/accounts/partyContacts.do?method=backToShowInformation" />
		</fr:edit>
			
</logic:equal>


<logic:notEqual name="partyContactClass" value="PhysicalAddress">
<fr:edit id="edit-contact" name="partyContact" action="<%="/accounts/partyContacts.do?method=editPartyContact&personID=" + personID %>"
    schema="<%= "contacts." + (contactType.equals("INSTITUTIONAL") ? "Institutional." : "") + partyContactClass + ".manage" %>">
    <fr:layout name="tabular-editable">
        <fr:property name="classes" value="tstyle5 thlight thright thmiddle" />
        <fr:property name="columnClasses" value=",,tdclear tderror1" />
    </fr:layout>
    <fr:destination name="postback-set-public"
        path="/accounts/partyContacts.do?method=postbackSetPublic&form=edit" />
    <fr:destination name="postback-set-elements"
        path="/accounts/partyContacts.do?method=postbackSetElements&form=edit" />
    <fr:destination name="invalid" path="/accounts/partyContacts.do?method=invalid&form=edit"/>
    <fr:destination name="cancel" path="<%="/accounts/partyContacts.do?method=backToShowInformation&personID"+personID%>" />
</fr:edit>
</logic:notEqual>