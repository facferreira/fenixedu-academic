<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<!-- NOTA: N�o foram incluidas tags do beans tipo <bean:message key="title.listClasses"/> -->
<%--<p><strong>&raquo; <bean:message key="group.enrolment"/></strong></p>--%>
<ul>
 <%-- <li><html:link page="/curricularCourseEnrolmentManager.do?method=start"><bean:message key="link.curricular.course.enrolment"/></html:link></li>--%>
  <li><html:link page="/studentShiftEnrolmentManager.do?method=start&firstTime=true"><bean:message key="link.shift.enrolment"/></html:link></li>
  <li><html:link page="/listAllSeminaries.do"> <bean:message key="link.seminaries.enrolment"/></html:link> <a href='<bean:message key="link.seminaries.rules" />' target="_blank"><bean:message key="label.seminairies.seeRules"/></a></li>
  <li><html:link page="/examEnrollmentManager.do?method=viewExamsToEnroll" ><bean:message key="link.exams.enrolment"/></html:link></li>
  <li><html:link page="/viewEnroledExecutionCourses.do" ><bean:message key="link.groupEnrolment" /></html:link></li>
</ul>
<ul>
	<li><html:link page="/studentTimeTable.do" target="_blank" >O Meu Hor�rio</html:link></li>
  	<li><html:link page="/viewCurriculum.do?method=getStudentCP" ><bean:message key="link.student.curriculum"/></html:link></li>
  	<%--<li><html:link page="/studentExecutionCourse.do?method=viewStudentExecutionCourses" ><bean:message key="link.myExecutionCourses"/></html:link></li>--%>
  	
</ul>