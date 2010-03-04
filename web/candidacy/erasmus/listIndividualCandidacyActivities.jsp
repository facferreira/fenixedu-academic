<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<%@ page import="net.sourceforge.fenixedu.presentationTier.servlets.filters.ChecksumRewriter"%>
<html:xhtml/>

<bean:define id="processName" name="processName" />
<bean:define id="parentProcessId" name="parentProcess" property="idInternal" />
<bean:define id="individualCandidacyProcess" name="process"/>

<em><bean:message key="label.candidacies" bundle="APPLICATION_RESOURCES"/></em>
<logic:notEmpty name="process">
	<h2><bean:write name="process" property="displayName" /> </h2>
</logic:notEmpty>

<html:link action='<%= "/caseHandling" + processName.toString() + ".do?method=listProcesses&amp;parentProcessId=" + parentProcessId.toString() %>'>
	� <bean:message key="label.back" bundle="APPLICATION_RESOURCES"/>	
</html:link>
<br/>

<logic:notEmpty name="process">
	<bean:define id="processId" name="process" property="idInternal" />
	
	<logic:notEmpty name="activities">
		<%-- list process activities --%>
		<ul>
		<logic:iterate id="activity" name="activities">
			<bean:define id="activityName" name="activity" property="class.simpleName" />
			<li>
				<html:link action='<%= "/caseHandling" + processName.toString() + ".do?method=prepareExecute" + activityName.toString() + "&amp;processId=" + processId.toString()%>'>
					<bean:message name="activity" property="class.name" bundle="CASE_HANDLING_RESOURCES" />
				</html:link>
			</li>
		</logic:iterate>
		</ul>
	</logic:notEmpty>
	
	<%-- student information --%>
	<logic:notEmpty name="process" property="personalDetails.student">
		<br/>
		<strong><bean:message key="label.studentDetails" bundle="APPLICATION_RESOURCES"/>:</strong>
		<fr:view name="process" property="personalDetails.student" schema="student.show.number.information">
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
			</fr:layout>
		</fr:view>
	</logic:notEmpty>
	
	<%-- show candidacy information --%>
	<br />
	<strong><bean:message key="label.candidacy.data" bundle="APPLICATION_RESOURCES"/>:</strong>
	<fr:view name="process" schema='<%= processName.toString() +  ".view" %>'>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
	        <fr:property name="columnClasses" value="width12em,,tdclear tderror1"/>
		</fr:layout>
	</fr:view>
	
	<%-- show person information --%>
	<br />
	<strong><bean:message key="label.candidacy.personalData" bundle="APPLICATION_RESOURCES" />:</strong>
	<fr:view name="process" property="personalDetails" schema="ErasmusCandidacyProcess.personalData">
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
	        <fr:property name="columnClasses" value="width12em,,tdclear tderror1"/>
		</fr:layout>
	</fr:view>


	<%-- show home institution data --%>
	<br/>
	<strong><bean:message key="label.erasmus.home.institution" bundle="ACADEMIC_OFFICE_RESOURCES" /></strong>
	<fr:view name="process" schema="ErasmusIndividualCandidacyProcess.home.institution.view">
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
	        <fr:property name="columnClasses" value="width12em,,tdclear tderror1"/>
		</fr:layout>
	</fr:view>


	<%-- show current study --%>
	<br/>
	<strong><bean:message key="label.erasmus.current.study" bundle="ACADEMIC_OFFICE_RESOURCES" /></strong>
	<em><bean:message key="label.erasmus.current.study.detailed" bundle="ACADEMIC_OFFICE_RESOURCES" /></em>
	<fr:view name="process" schema="ErasmusIndividualCandidacyProcess.current.study.view" >
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
	        <fr:property name="columnClasses" value="width12em,,tdclear tderror1"/>
		</fr:layout>
	</fr:view>
	
	<%-- show period of study --%>
	<br/>
	<strong><bean:message key="label.erasmus.period.of.study" bundle="ACADEMIC_OFFICE_RESOURCES" /></strong>
	<fr:view name="process" schema="ErasmusIndividualCandidacyProcess.period.of.study.view">
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4 thlight thright mtop025"/>
	        <fr:property name="columnClasses" value="width12em,,tdclear tderror1"/>
		</fr:layout>
	</fr:view>
	
	
	<%-- Choosen courses --%>
	<strong><bean:message key="label.erasmus.courses" bundle="ACADEMIC_OFFICE_RESOURCES"/>:</strong>
	<table class="tstyle2 thlight thcenter">
	<tr>
		<th><bean:message key="label.erasmus.course" bundle="ACADEMIC_OFFICE_RESOURCES"/></th>
	</tr>
	<logic:iterate id="course" name="process" property="sortedSelectedCurricularCourses" indexId="index">
		<bean:define id="curricularCourseId" name="course" property="externalId" />
	<tr>
		<td>
			<fr:view 	name="course"
						property="nameI18N">
<%-- 				<fr:layout name="flow"> <fr:property name="labelExcluded" value="true"/> </fr:layout>--%>
			</fr:view>
		</td>
	</tr>
	</logic:iterate>
	</table>	
	
	
	<%-- show documents--%>
	<br/>
	<h2 style="margin-top: 1em;"><bean:message key="label.documentation" bundle="CANDIDATE_RESOURCES"/></h2> 
	
	<logic:empty name="individualCandidacyProcess" property="candidacy.documents">
		<p><em><bean:message key="message.documents.empty" bundle="CANDIDATE_RESOURCES"/>.</em></p>
	</logic:empty>
	
	<logic:notEmpty name="individualCandidacyProcess" property="candidacy.documents">
	<table class="tstyle4 thlight thcenter">
		<tr>
			<th><bean:message key="label.candidacy.document.kind" bundle="CANDIDATE_RESOURCES"/></th>
			<th><bean:message key="label.dateTime.submission" bundle="CANDIDATE_RESOURCES"/></th>
			<th><bean:message key="label.document.file.name" bundle="CANDIDATE_RESOURCES"/></th>
			<th><bean:message key="label.document.file.active" bundle="CANDIDATE_RESOURCES"/></th>
			<th></th>
		</tr>
	
		
		<logic:iterate id="documentFile" name="individualCandidacyProcess" property="candidacy.documents">
		<tr>
			<td><fr:view name="documentFile" property="candidacyFileType"/></td>
			<td><fr:view name="documentFile" property="uploadTime"/></td>
			<td><fr:view name="documentFile" property="filename"/></td>
			<td><fr:view name="documentFile" property="candidacyFileActive"/></td>
			<td><fr:view name="documentFile" layout="link"/></td>
		</tr>	
		</logic:iterate>
	</table>
	</logic:notEmpty>

</logic:notEmpty>
