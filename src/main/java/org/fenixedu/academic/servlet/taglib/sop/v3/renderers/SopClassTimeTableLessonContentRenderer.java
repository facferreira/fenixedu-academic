/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.servlet.taglib.sop.v3.renderers;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.FrequencyType;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.dto.InfoCurricularYear;
import org.fenixedu.academic.dto.InfoExecutionCourse;
import org.fenixedu.academic.dto.InfoExecutionDegree;
import org.fenixedu.academic.dto.InfoLesson;
import org.fenixedu.academic.dto.InfoLessonInstance;
import org.fenixedu.academic.dto.InfoLessonInstanceAggregation;
import org.fenixedu.academic.dto.InfoShift;
import org.fenixedu.academic.dto.InfoShowOccupation;
import org.fenixedu.academic.servlet.taglib.sop.v3.LessonSlot;
import org.fenixedu.academic.servlet.taglib.sop.v3.LessonSlotContentRenderer;
import org.fenixedu.academic.ui.struts.action.resourceAllocationManager.utils.PresentationConstants;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.LocalDate;

/**
 * @author Nuno Nunes &amp; David Santos
 */
public class SopClassTimeTableLessonContentRenderer extends LessonSlotContentRenderer {

    private InfoCurricularYear infoCurricularYear = null;

    private InfoExecutionDegree infoExecutionDegree = null;

    public SopClassTimeTableLessonContentRenderer(InfoExecutionDegree infoExecutionDegree,
            InfoCurricularYear infoCurricularYear) {
        super();
        this.infoCurricularYear = infoCurricularYear;
        this.infoExecutionDegree = infoExecutionDegree;
    }

    @Override
    public StringBuilder render(String context, LessonSlot lessonSlot) {
        StringBuilder strBuffer = new StringBuilder();

        InfoShowOccupation showOccupation = lessonSlot.getInfoLessonWrapper().getInfoShowOccupation();

        if (showOccupation instanceof InfoLesson) {
            InfoLesson lesson = (InfoLesson) showOccupation;

            strBuffer.append(lesson.getInfoShift().getInfoDisciplinaExecucao().getSigla());

            final InfoShift infoShift = lesson.getInfoShift();
            InfoExecutionCourse infoExecutionCourse = infoShift.getInfoDisciplinaExecucao();

            strBuffer.append("&nbsp;(");
            strBuffer.append("<a href='");
            strBuffer.append(context).append("/resourceAllocationManager/")
                    .append("manageShift.do?method=prepareEditShift&amp;page=0").append("&amp;shift_oid=")
                    .append(infoShift.getExternalId()).append("&amp;execution_course_oid=")
                    .append(infoExecutionCourse.getExternalId()).append("&amp;" + PresentationConstants.ACADEMIC_INTERVAL + "=")
                    .append(infoExecutionCourse.getAcademicInterval().getResumedRepresentationInStringFormat())
                    .append("&amp;curricular_year_oid=").append(infoCurricularYear.getExternalId())
                    .append("&amp;execution_degree_oid=").append(infoExecutionDegree.getExternalId()).append("'>")
                    .append(lesson.getInfoShift().getShiftTypesCodePrettyPrint()).append("</a>").append(")&nbsp;");

            final Space allocatableSpace = lesson.getAllocatableSpace();
            if (allocatableSpace != null) {
                /*strBuffer.append(" <a href='");
                strBuffer.append(context).append("/resourceAllocationManager/");
                strBuffer.append("pesquisarSala.do?name=").append(allocatableSpace.getName()).append("'>")
                        .append(allocatableSpace.getName()).append("</a>");*/
                strBuffer.append(allocatableSpace.getName());
            }

            if (lesson.getFrequency().equals(FrequencyType.BIWEEKLY)) {
                strBuffer.append("&nbsp;&nbsp;[Q]");
            }

        } else if (showOccupation instanceof InfoLessonInstance) {

            InfoLessonInstance lesson = (InfoLessonInstance) showOccupation;

            strBuffer.append(lesson.getInfoShift().getInfoDisciplinaExecucao().getSigla());

            final InfoShift infoShift = lesson.getInfoShift();
            InfoExecutionCourse infoExecutionCourse = infoShift.getInfoDisciplinaExecucao();

            strBuffer.append("&nbsp;(");
            strBuffer.append("<a href='");
            strBuffer.append(context).append("/resourceAllocationManager/")
                    .append("manageShift.do?method=prepareEditShift&amp;page=0").append("&amp;shift_oid=")
                    .append(infoShift.getExternalId()).append("&amp;execution_course_oid=")
                    .append(infoExecutionCourse.getExternalId()).append("&amp;" + PresentationConstants.ACADEMIC_INTERVAL + "=")
                    .append(infoExecutionCourse.getAcademicInterval().getResumedRepresentationInStringFormat())
                    .append("&amp;curricular_year_oid=").append(infoCurricularYear.getExternalId())
                    .append("&amp;execution_degree_oid=").append(infoExecutionDegree.getExternalId()).append("'>")
                    .append(lesson.getShiftTypeCodesPrettyPrint()).append("</a>").append(")&nbsp;");

            if (lesson.getInfoRoomOccupation() != null) {
                /*strBuffer.append(" <a href='");
                strBuffer.append(context).append("/resourceAllocationManager/");
                strBuffer.append("pesquisarSala.do?name=").append(lesson.getInfoRoomOccupation().getInfoRoom().getNome())
                .append("'>").append(lesson.getInfoRoomOccupation().getInfoRoom().getNome()).append("</a>");*/
                strBuffer.append(lesson.getInfoRoomOccupation().getInfoRoom().getNome());
            }

        } else if (showOccupation instanceof InfoLessonInstanceAggregation) {
            final InfoLessonInstanceAggregation aggregation = (InfoLessonInstanceAggregation) showOccupation;

            final Shift shift = aggregation.getShift();
            final ExecutionCourse executionCourse = shift.getExecutionCourse();
            strBuffer.append(executionCourse.getSigla());

            strBuffer.append("&nbsp;(");
            strBuffer.append("<a href='");
            strBuffer.append(context).append("/resourceAllocationManager/")
                    .append("manageShift.do?method=prepareEditShift&amp;page=0").append("&amp;shift_oid=")
                    .append(shift.getExternalId()).append("&amp;execution_course_oid=").append(executionCourse.getExternalId())
                    .append("&amp;" + PresentationConstants.ACADEMIC_INTERVAL + "=")
                    .append(executionCourse.getAcademicInterval().getResumedRepresentationInStringFormat())
                    .append("&amp;curricular_year_oid=").append(infoCurricularYear.getExternalId())
                    .append("&amp;execution_degree_oid=").append(infoExecutionDegree.getExternalId()).append("'>")
                    .append(shift.getShiftTypesCodePrettyPrint()).append("</a>").append(")&nbsp;");

            final Space allocatableSpace = aggregation.getAllocatableSpace();
            if (allocatableSpace != null) {
//                strBuffer.append(" <a href='");
//                strBuffer.append(context).append("/resourceAllocationManager/");
//                strBuffer.append("pesquisarSala.do?name=").append(allocatableSpace.getName()).append("'>")
//                .append(allocatableSpace.getName()).append("</a>");
                strBuffer.append(allocatableSpace.getName());
            }

        }

        return strBuffer;
    }

    @Override
    public String renderSecondLine(final String context, final LessonSlot lessonSlot) {
        final StringBuilder builder = new StringBuilder();
        final InfoShowOccupation showOccupation = lessonSlot.getInfoLessonWrapper().getInfoShowOccupation();
        if (showOccupation instanceof InfoLessonInstanceAggregation) {
            final InfoLessonInstanceAggregation infoLessonInstanceAggregation = (InfoLessonInstanceAggregation) showOccupation;
            if (!infoLessonInstanceAggregation.availableInAllWeeks()) {
                builder.append("<span>");
                builder.append(BundleUtil.getString(Bundle.CANDIDATE, "label.weeks"));
                builder.append(": &nbsp;&nbsp;");
                builder.append(infoLessonInstanceAggregation.prettyPrintWeeks());
                builder.append("&nbsp;");
                builder.append("</span>");
            }
        }
        builder.append(super.renderSecondLine(context, lessonSlot));
        return builder.toString();
    }

    @Override
    public String renderTitleText(final LessonSlot lessonSlot) {
        final StringBuilder builder = new StringBuilder(super.renderTitleText(lessonSlot));

        final InfoShowOccupation occupation = lessonSlot.getInfoLessonWrapper().getInfoShowOccupation();
        if (occupation instanceof InfoLessonInstanceAggregation) {
            final InfoLessonInstanceAggregation aggregation = (InfoLessonInstanceAggregation) occupation;
            for (final LocalDate localDate : aggregation.getDates()) {
                builder.append('\n');
                builder.append(localDate.toString("yyyy-MM-dd"));
            }
        }

        return builder.toString();
    }

}