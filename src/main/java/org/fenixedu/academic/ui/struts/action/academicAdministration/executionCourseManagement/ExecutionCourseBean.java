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
package org.fenixedu.academic.ui.struts.action.academicAdministration.executionCourseManagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.CurricularYear;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.interfaces.HasExecutionDegree;
import org.fenixedu.academic.domain.interfaces.HasExecutionSemester;

import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class ExecutionCourseBean implements Serializable, HasExecutionSemester, HasExecutionDegree {

    private ExecutionDegree executionDegree;
    private Degree degree;
    private CurricularYear curricularYear;
    private ExecutionCourse sourceExecutionCourse;
    private ExecutionCourse destinationExecutionCourse;
    private ExecutionSemester executionSemester;
    private Boolean chooseNotLinked;

    @Override
    public ExecutionSemester getExecutionPeriod() {
        return getExecutionSemester();
    }

    @Override
    public ExecutionDegree getExecutionDegree() {
        return executionDegree;
    }

    public void setExecutionDegree(final ExecutionDegree input) {
        if (input != null && getDegree() != null && getDegree() != input.getDegree()) {
            throw new IllegalStateException();
        }

        this.executionDegree = input;
    }

    public Degree getDegree() {
        return this.degree;
    }

    public void setDegree(final Degree input) {
        if (input != null && getExecutionDegree() != null && getExecutionDegree().getDegree() != input) {
            throw new IllegalStateException();
        }

        this.degree = input;
    }

    public CurricularYear getCurricularYear() {
        return curricularYear;
    }

    public void setCurricularYear(CurricularYear curricularYear) {
        this.curricularYear = curricularYear;
    }

    public ExecutionSemester getExecutionSemester() {
        return executionSemester;
    }

    public void setExecutionSemester(ExecutionSemester executionSemester) {
        this.executionSemester = executionSemester;
    }

    public ExecutionCourse getSourceExecutionCourse() {
        return sourceExecutionCourse;
    }

    public void setSourceExecutionCourse(ExecutionCourse sourceExecutionCourse) {
        this.sourceExecutionCourse = sourceExecutionCourse;
    }

    public ExecutionCourse getDestinationExecutionCourse() {
        return destinationExecutionCourse;
    }

    public void setDestinationExecutionCourse(ExecutionCourse destinationExecutionCourse) {
        this.destinationExecutionCourse = destinationExecutionCourse;
    }

    public Boolean getChooseNotLinked() {
        return this.chooseNotLinked;
    }

    public void setChooseNotLinked(Boolean chooseNotLinked) {
        this.chooseNotLinked = chooseNotLinked;
    }

    public ExecutionCourseBean() {
    }

    public ExecutionCourseBean(ExecutionCourse executionCourse) {
        setSourceExecutionCourse(executionCourse);
    }

    public Collection<ExecutionCourse> getExecutionCourses() {
        List<ExecutionCourse> result = new ArrayList<ExecutionCourse>();
        if (this.chooseNotLinked) {
            result = this.getExecutionSemester().getExecutionCoursesWithNoCurricularCourses();
        } else {
            for (final CurricularCourse curricularCourse : getDegreeCurricularPlan().getCurricularCourses(getExecutionSemester())) {
                if (curricularCourse.hasScopeInGivenSemesterAndCurricularYearInDCP(getCurricularYear(),
                        getDegreeCurricularPlan(), getExecutionSemester())) {
                    result.addAll(curricularCourse.getExecutionCoursesByExecutionPeriod(getExecutionSemester()));
                }
            }
        }
        TreeSet<ExecutionCourse> finalResult = new TreeSet<ExecutionCourse>(ExecutionCourse.EXECUTION_COURSE_NAME_COMPARATOR);
        finalResult.addAll(result);
        return finalResult;

    }

    private DegreeCurricularPlan getDegreeCurricularPlan() {
        return getExecutionDegree().getDegreeCurricularPlan();
    }

    public String getSourcePresentationName() {
        String result = StringUtils.EMPTY;

        if (getSourceExecutionCourse() != null) {
            result += getSourceExecutionCourse().getNameI18N().getContent();

            final Set<DegreeCurricularPlan> plans;
            if (getDegree() != null) {
                plans =
                        Sets.intersection(getDegree().getDegreeCurricularPlansSet(),
                                Sets.newHashSet(getSourceExecutionCourse().getAssociatedDegreeCurricularPlans()));
            } else {
                plans = Sets.newHashSet(getSourceExecutionCourse().getAssociatedDegreeCurricularPlans());
            }

            result += getDegreeCurricularPlansPresentationString(plans);
        }

        return result;
    }

    public String getDestinationPresentationName() {
        String result = StringUtils.EMPTY;

        if (getDestinationExecutionCourse() != null) {
            result += getDestinationExecutionCourse().getNameI18N().getContent();

            final Set<DegreeCurricularPlan> plans;
            if (getDegree() != null) {
                plans =
                        Sets.intersection(getDegree().getDegreeCurricularPlansSet(),
                                Sets.newHashSet(getDestinationExecutionCourse().getAssociatedDegreeCurricularPlans()));
            } else {
                plans = Sets.newHashSet(getDestinationExecutionCourse().getAssociatedDegreeCurricularPlans());
            }

            result += getDegreeCurricularPlansPresentationString(plans);
        }

        return result;
    }

    static private String getDegreeCurricularPlansPresentationString(final Set<DegreeCurricularPlan> input) {
        String result = " [ ";

        for (Iterator<DegreeCurricularPlan> iterator = input.iterator(); iterator.hasNext();) {
            final DegreeCurricularPlan iter = iterator.next();
            result += iter.getName();
            if (iterator.hasNext()) {
                result += " , ";
            }
        }

        result += " ]";

        return result;
    }

}
