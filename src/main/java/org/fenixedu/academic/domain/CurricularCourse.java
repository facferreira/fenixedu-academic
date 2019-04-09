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
package org.fenixedu.academic.domain;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.curriculum.CurricularCourseType;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseLevel;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.academic.domain.degreeStructure.CurricularStage;
import org.fenixedu.academic.domain.degreeStructure.DegreeModule;
import org.fenixedu.academic.domain.degreeStructure.RegimeType;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.log.CurriculumLineLog;
import org.fenixedu.academic.domain.organizationalStructure.DepartmentUnit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumModule;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicInterval;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicPeriod;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;

public class CurricularCourse extends CurricularCourse_Base {

    static final public Comparator<CurricularCourse> CURRICULAR_COURSE_COMPARATOR_BY_DEGREE_AND_NAME =
            new Comparator<CurricularCourse>() {
                @Override
                public int compare(CurricularCourse o1, CurricularCourse o2) {
                    final Degree degree1 = o1.getDegree();
                    final Degree degree2 = o2.getDegree();
                    final Collator collator = Collator.getInstance();
                    final int degreeTypeComp = degree1.getDegreeType().compareTo(degree2.getDegreeType());
                    if (degreeTypeComp != 0) {
                        return degreeTypeComp;
                    }
                    final int degreeNameComp = collator.compare(degree1.getNome(), degree2.getNome());
                    if (degreeNameComp == 0) {
                        return degreeNameComp;
                    }
                    return CurricularCourse.COMPARATOR_BY_NAME.compare(o1, o2);
                }
            };

    public static List<CurricularCourse> readCurricularCourses() {
        List<CurricularCourse> result = new ArrayList<CurricularCourse>();
        for (DegreeModule degreeModule : Bennu.getInstance().getDegreeModulesSet()) {
            if (degreeModule instanceof CurricularCourse) {
                result.add((CurricularCourse) degreeModule);
            }
        }
        return result;
    }

    public CurricularCourse() {
        super();
        final Double d = Double.valueOf(0d);
        setTheoreticalHours(d);
        setTheoPratHours(d);
        setLabHours(d);
        setPraticalHours(d);
        setCredits(d);
        setEctsCredits(d);
        setWeigth(d);
    }

    protected CurricularCourse(DegreeCurricularPlan degreeCurricularPlan, String name, String code, String acronym,
            Boolean enrolmentAllowed, CurricularStage curricularStage) {
        this();
        checkParameters(name, code, acronym);
        checkForCurricularCourseWithSameAttributes(degreeCurricularPlan, name, code, acronym);
        setName(name);
        setCode(code);
        setAcronym(acronym);
        setEnrollmentAllowed(enrolmentAllowed);
        setCurricularStage(curricularStage);
        setDegreeCurricularPlan(degreeCurricularPlan);
    }

    private void checkParameters(final String name, final String code, final String acronym) {
        if (StringUtils.isEmpty(name)) {
            throw new DomainException("error.curricularCourse.invalid.name");
        }
        if (StringUtils.isEmpty(code)) {
            throw new DomainException("error.curricularCourse.invalid.code");
        }
        if (StringUtils.isEmpty(acronym)) {
            throw new DomainException("error.curricularCourse.invalid.acronym");
        }
    }

    public CurricularCourse(Double weight, String prerequisites, String prerequisitesEn, CurricularStage curricularStage,
            CompetenceCourse competenceCourse, CourseGroup parentCourseGroup, CurricularPeriod curricularPeriod,
            ExecutionSemester beginExecutionPeriod, ExecutionSemester endExecutionPeriod) {

        this();
        setWeigth(weight);
        setPrerequisites(prerequisites);
        setPrerequisitesEn(prerequisitesEn);
        setCurricularStage(curricularStage);
        setCompetenceCourse(competenceCourse);
        setType(CurricularCourseType.NORMAL_COURSE);
        new Context(parentCourseGroup, this, curricularPeriod, beginExecutionPeriod, endExecutionPeriod);
    }

    public GradeScale getGradeScaleChain() {
        return super.getGradeScale() != null ? super.getGradeScale() : getDegreeCurricularPlan().getGradeScaleChain();
    }

    @Override
    public void print(StringBuilder dcp, String tabs, Context previousContext) {
        String tab = tabs + "\t";
        dcp.append(tab);
        dcp.append("[CC ").append(getExternalId()).append("][");
        dcp.append(previousContext.getCurricularPeriod().getOrderByType(AcademicPeriod.YEAR)).append("Y,");
        dcp.append(previousContext.getCurricularPeriod().getOrderByType(AcademicPeriod.SEMESTER)).append("S]\t");
        dcp.append("[B:").append(previousContext.getBeginExecutionInterval().getBeginDateYearMonthDay());
        dcp.append(" E:").append(previousContext.getEndExecutionInterval() != null ? previousContext.getEndExecutionInterval()
                .getEndDateYearMonthDay() : "          ");
        dcp.append("]\t");
        dcp.append(getName()).append("\n");
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public DegreeCurricularPlan getParentDegreeCurricularPlan() {
        return !getParentContextsSet().isEmpty() ? getParentContextsSet().iterator().next().getParentCourseGroup()
                .getParentDegreeCurricularPlan() : null;
    }

    @Override
    final public DegreeCurricularPlan getDegreeCurricularPlan() {
        return getParentDegreeCurricularPlan();
    }

    public void edit(Double weight, String prerequisites, String prerequisitesEn, CurricularStage curricularStage,
            CompetenceCourse competenceCourse) {

        setWeigth(weight);
        setPrerequisites(prerequisites);
        setPrerequisitesEn(prerequisitesEn);
        setCurricularStage(curricularStage);
        setCompetenceCourse(competenceCourse);
    }

    /**
     * - This method is used to edit a 'special' curricular course that will
     * represent any curricular course according to a rule
     * 
     * @deprecated This method sets attributes that are no longer used in regular CurricularCourse objects.
     *             Use
     *             {@link org.fenixedu.academic.domain.degreeStructure.OptionalCurricularCourse#edit(String, String, CurricularStage)
     *             edit}
     *             instead.
     */
    @Deprecated
    public void edit(String name, String nameEn, CurricularStage curricularStage) {
        setName(name);
        setNameEn(nameEn);
        setCurricularStage(curricularStage);
    }

    /**
     * - Edit Pre-Bolonha CurricularCourse
     * 
     * @deprecated This method sets attributes that are no longer used. Set the corresponding attributes in the
     *             appropriate
     *             {@link org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation CompetenceCourseInformation}
     *             object.
     */
    public void edit(String name, String nameEn, String code, String acronym, Double weigth, Double credits, Double ectsCredits,
            Integer enrolmentWeigth, Integer minimumValueForAcumulatedEnrollments, Integer maximumValueForAcumulatedEnrollments,
            final Double theoreticalHours, final Double labHours, final Double praticalHours, final Double theoPratHours,
            final GradeScale gradeScale) {
        checkForCurricularCourseWithSameAttributes(getDegreeCurricularPlan(), name, code, acronym);
        setName(name);
        setNameEn(nameEn);
        setCode(code);
        setAcronym(acronym);
        setWeigth(weigth);
        setCredits(credits);
        setEctsCredits(ectsCredits);

        setEnrollmentWeigth(enrolmentWeigth);
        setMinimumValueForAcumulatedEnrollments(minimumValueForAcumulatedEnrollments);
        setMaximumValueForAcumulatedEnrollments(maximumValueForAcumulatedEnrollments);

        setTheoreticalHours(theoreticalHours);
        setLabHours(labHours);
        setPraticalHours(praticalHours);
        setTheoPratHours(theoPratHours);

        super.setGradeScale(gradeScale);
    }

    private void checkForCurricularCourseWithSameAttributes(DegreeCurricularPlan degreeCurricularPlan, String name, String code,
            String acronym) {
        for (final CurricularCourse curricularCourse : degreeCurricularPlan.getCurricularCoursesSet()) {
            if (curricularCourse == this) {
                continue;
            }
            if (curricularCourse.getName().equals(name) && curricularCourse.getCode().equals(code)) {
                throw new DomainException("error.curricularCourseWithSameNameAndCode");
            }
            if (curricularCourse.getAcronym().equals(acronym)) {
                throw new DomainException("error.curricularCourseWithSameAcronym");
            }
        }
    }

    @Override
    public void delete() {
        super.delete();
        getCurriculumLineLogsSet().forEach(CurriculumLineLog::delete);
        setDegreeCurricularPlan(null);
        setCompetenceCourse(null);
        setRootDomainObject(null);
        super.deleteDomainObject();
    }

    public boolean hasAnyActiveDegreModuleScope(final ExecutionSemester executionSemester) {
        for (final DegreeModuleScope degreeModuleScope : getDegreeModuleScopes()) {
            if (degreeModuleScope.isActiveForExecutionPeriod(executionSemester)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyActiveContext(final ExecutionSemester executionSemester) {
        for (final Context context : getParentContextsSet()) {
            if (context.isValid(executionSemester)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public List<DegreeModuleScope> getActiveDegreeModuleScopesInExecutionPeriod(final ExecutionSemester executionSemester) {
        final List<DegreeModuleScope> activeScopesInExecutionPeriod = new ArrayList<DegreeModuleScope>();
        for (final DegreeModuleScope scope : getDegreeModuleScopes()) {
            if (scope.isActiveForExecutionPeriod(executionSemester)) {
                activeScopesInExecutionPeriod.add(scope);
            }
        }
        return activeScopesInExecutionPeriod;
    }

    public List<DegreeModuleScope> getActiveDegreeModuleScopesInAcademicInterval(AcademicInterval academicInterval) {
        final List<DegreeModuleScope> activeScopesInExecutionPeriod = new ArrayList<DegreeModuleScope>();
        for (final DegreeModuleScope scope : getDegreeModuleScopes()) {
            if (scope.isActiveForAcademicInterval(academicInterval)) {
                activeScopesInExecutionPeriod.add(scope);
            }
        }
        return activeScopesInExecutionPeriod;
    }

    // -------------------------------------------------------------
    // BEGIN: Only for enrollment purposes
    // -------------------------------------------------------------

    public String getCurricularCourseUniqueKeyForEnrollment() {
        final DegreeType degreeType =
                (getDegreeCurricularPlan() != null && getDegreeCurricularPlan().getDegree() != null) ? getDegreeCurricularPlan()
                        .getDegree().getDegreeType() : null;
        return constructUniqueEnrollmentKey(getCode(), getName(), degreeType);
    }

    // -------------------------------------------------------------
    // END: Only for enrollment purposes
    // -------------------------------------------------------------

    private String constructUniqueEnrollmentKey(String code, String name, DegreeType degreeType) {
        StringBuilder stringBuffer = new StringBuilder(50);
        stringBuffer.append(code);
        stringBuffer.append(name);
        if (degreeType != null) {
            stringBuffer.append(degreeType.toString());
        }
        return StringUtils.lowerCase(stringBuffer.toString());
    }

    @SuppressWarnings("unchecked")
    public List<ExecutionCourse> getExecutionCoursesByExecutionPeriod(final ExecutionInterval executionInterval) {
        return getAssociatedExecutionCoursesSet().stream().filter(ec -> ec.getExecutionInterval() == executionInterval)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<ExecutionCourse> getExecutionCoursesByExecutionYear(final ExecutionYear executionYear) {
        return (List<ExecutionCourse>) CollectionUtils.select(getAssociatedExecutionCoursesSet(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                ExecutionCourse executionCourse = (ExecutionCourse) o;
                return executionCourse.getExecutionInterval().getExecutionYear().equals(executionYear);
            }
        });
    }

    final public double getProblemsHours() {
        return getProblemsHours(null);
    }

    private Double getProblemsHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getProblemsHours(executionInterval) : 0.0d;
    }

    final public double getLaboratorialHours() {
        return getLaboratorialHours(null);
    }

    private Double getLaboratorialHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getLaboratorialHours(executionInterval) : 0.0d;
    }

    final public Double getSeminaryHours() {
        return getSeminaryHours(null);
    }

    private Double getSeminaryHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getSeminaryHours(executionInterval) : 0.0d;
    }

    final public double getFieldWorkHours() {
        return getFieldWorkHours(null);
    }

    private Double getFieldWorkHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getFieldWorkHours(executionInterval) : 0.0d;
    }

    final public double getTrainingPeriodHours() {
        return getTrainingPeriodHours(null);
    }

    private Double getTrainingPeriodHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getTrainingPeriodHours(executionInterval) : 0.0d;
    }

    final public double getTutorialOrientationHours() {
        return getTutorialOrientationHours(null);
    }

    private Double getTutorialOrientationHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getTutorialOrientationHours(executionInterval) : 0.0d;
    }

    final public double getOtherHours() {
        return getOtherHours(null);
    }

    private Double getOtherHours(final ExecutionInterval executionInterval) {
        return isBolonhaDegree() && getCompetenceCourse() != null ? getCompetenceCourse().getOtherHours(executionInterval) : 0.0d;
    }

    final public double getAutonomousWorkHours() {
        return getAutonomousWorkHours((CurricularPeriod) null, (ExecutionSemester) null);
    }

    final public double getAutonomousWorkHours(final ExecutionInterval executionInterval) {
        return getAutonomousWorkHours((CurricularPeriod) null, executionInterval);
    }

    final public Double getAutonomousWorkHours(final CurricularPeriod curricularPeriod, final ExecutionInterval executionYear) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getAutonomousWorkHours(curricularPeriod.getChildOrder(),
                executionYear) : 0d;
    }

    final public double getContactLoad() {
        return getContactLoad((CurricularPeriod) null, (ExecutionInterval) null);
    }

    final public double getContactLoad(final ExecutionInterval executionInterval) {
        return getContactLoad((CurricularPeriod) null, executionInterval);
    }

    final public Double getContactLoad(final CurricularPeriod curricularPeriod, final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse()
                .getContactLoad(curricularPeriod == null ? null : curricularPeriod.getChildOrder(), executionInterval) : 0.0d;
    }

    final public double getTotalLoad() {
        return getTotalLoad((CurricularPeriod) null, (ExecutionSemester) null);
    }

    final public double getTotalLoad(final ExecutionInterval executionInterval) {
        return getTotalLoad((CurricularPeriod) null, executionInterval);
    }

    final public Double getTotalLoad(final CurricularPeriod curricularPeriod, final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse()
                .getTotalLoad(curricularPeriod == null ? null : curricularPeriod.getChildOrder(), executionInterval) : 0.0d;
    }

    @Override
    final public Double getLabHours() {
        return getLabHours(null);
    }

    private Double getLabHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getLaboratorialHours(executionInterval) : 0.0d;
    }

    @Override
    final public Double getTheoreticalHours() {
        return getTheoreticalHours(null);
    }

    private Double getTheoreticalHours(final ExecutionInterval executionInterval) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getTheoreticalHours(executionInterval) : 0.0d;
    }

    @Override
    final public Double getPraticalHours() {
        final Double praticalHours = super.getPraticalHours();
        return praticalHours == null ? 0.0d : praticalHours;
    }

    @Override
    final public Double getTheoPratHours() {
        final Double theoPratHours = super.getTheoPratHours();
        return theoPratHours == null ? 0.0d : theoPratHours;
    }

    @Override
    final public Double getCredits() {
        return getEctsCredits();
    }

    @Override
    public Double getEctsCredits() {
        return getEctsCredits(null);
    }

    public Double getEctsCredits(final ExecutionInterval executionInterval) {
        return getEctsCredits((CurricularPeriod) null, executionInterval);
    }

    public Double getEctsCredits(final CurricularPeriod curricularPeriod, final ExecutionInterval executionInterval) {
        return getEctsCredits(curricularPeriod == null ? null : curricularPeriod.getChildOrder(), executionInterval);
    }

    public Double getEctsCredits(final Integer order, final ExecutionInterval executionInterval) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getEctsCredits(order, executionInterval);
        } else if (isOptionalCurricularCourse()) {
            return 0.0d;
        }
        throw new DomainException("CurricularCourse.with.no.ects.credits");
    }

    @Override
    public Double getMaxEctsCredits(final ExecutionSemester executionSemester) {
        return getEctsCredits(executionSemester);
    }

    @Override
    public Double getMinEctsCredits(final ExecutionSemester executionSemester) {
        return getEctsCredits(executionSemester);
    }

    @Override
    @Deprecated()
    // typo: use getWeight() instead
    final public Double getWeigth() {
        return getWeight();
    }

    final public Double getWeight() {
        return getEctsCredits();
    }

    final public Double getWeight(ExecutionSemester semester) {
        return getEctsCredits(semester);
    }

    private List<Enrolment> getActiveEnrollments(ExecutionSemester executionSemester, Registration registration) {
        final List<Enrolment> results = new ArrayList<Enrolment>();
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                Enrolment enrollment = (Enrolment) curriculumModule;
                boolean filters = true;
                filters &= !enrollment.isAnnulled();
                filters &= executionSemester == null || enrollment.getExecutionPeriod().equals(executionSemester);
                filters &= registration == null || enrollment.getStudentCurricularPlan().getRegistration().equals(registration);

                if (filters) {
                    results.add(enrollment);
                }
            }
        }
        return results;
    }

    @Deprecated
    private void addActiveEnrollments(final Collection<Enrolment> enrolments, final ExecutionSemester executionSemester) {
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                final Enrolment enrolment = (Enrolment) curriculumModule;
                if (!enrolment.isAnnulled() && enrolment.getExecutionPeriod() == executionSemester) {
                    enrolments.add(enrolment);
                }
            }
        }
    }

    /**
     * @deprecated {@link #getEnrolmentsByAcademicInterval(AcademicInterval)}
     */
    @Deprecated
    public List<Enrolment> getEnrolmentsByExecutionPeriod(final ExecutionSemester executionSemester) {
        List<Enrolment> result = new ArrayList<Enrolment>();
        addActiveEnrollments(result, executionSemester);
        return result;
    }

    public List<Enrolment> getEnrolmentsByAcademicInterval(AcademicInterval academicInterval) {
        List<Enrolment> result = new ArrayList<Enrolment>();
        addActiveEnrollments(result, academicInterval);
        return result;
    }

    private void addActiveEnrollments(List<Enrolment> enrolments, AcademicInterval academicInterval) {
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                final Enrolment enrolment = (Enrolment) curriculumModule;
                if (!enrolment.isAnnulled() && (enrolment.getExecutionPeriod().getAcademicInterval().equals(academicInterval)
                        || enrolment.getExecutionPeriod().getExecutionYear().getAcademicInterval().equals(academicInterval))) {
                    enrolments.add(enrolment);
                }
            }
        }
    }

    public List<Enrolment> getEnrolments() {
        final List<Enrolment> result = new ArrayList<Enrolment>();
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                result.add((Enrolment) curriculumModule);
            }
        }
        return result;
    }

    public List<Enrolment> getEnrolmentsByYear(String year) {
        return getEnrolmentsByExecutionYear(ExecutionYear.readExecutionYearByName(year));
    }

    public List<Enrolment> getEnrolmentsByExecutionYear(ExecutionYear executionYear) {
        List<Enrolment> result = new ArrayList<Enrolment>();
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                final Enrolment enrolment = (Enrolment) curriculumModule;
                if (enrolment.getExecutionPeriod().getExecutionYear().equals(executionYear)) {
                    result.add(enrolment);
                }
            }
        }
        return result;
    }

    public List<Enrolment> getActiveEnrollments(Registration registration) {
        return getActiveEnrollments(null, registration);
    }

    public List<Enrolment> getActiveEnrollments() {
        return getActiveEnrollments(null, null);
    }

    public List<Enrolment> getActiveEnrollments(ExecutionSemester executionSemester) {
        List<Enrolment> enrolments = new ArrayList<Enrolment>();
        addActiveEnrollments(enrolments, executionSemester);
        return enrolments;
    }

    public List<Dismissal> getDismissals(ExecutionSemester executionSemester) {
        List<Dismissal> dismissals = new ArrayList<Dismissal>();
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isDismissal()) {
                final Dismissal dismissal = (Dismissal) curriculumModule;
                if (dismissal.getExecutionPeriod() == executionSemester) {
                    dismissals.add(dismissal);
                }
            }
        }
        return dismissals;
    }

    public List<Enrolment> getActiveEnrollments(ExecutionYear executionYear) {
        List<Enrolment> results = new ArrayList<Enrolment>();

        for (ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            results.addAll(getActiveEnrollments(executionSemester));
        }

        return results;
    }

    protected String getBaseName() {
        return super.getName();
    }

    protected String getBaseNameEn() {
        return super.getNameEn();
    }

    @Override
    public String getName(ExecutionSemester period) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getName(period) : null;
    }

    @Override
    public String getName() {
        return getName(null);
    }

    @Override
    public String getNameEn(ExecutionSemester period) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getNameEn(period) : null;
    }

    @Override
    public String getNameEn() {
        return getNameEn(null);
    }

    public String getAcronym(ExecutionSemester period) {
        return getCompetenceCourse() != null ? getCompetenceCourse().getAcronym(period) : null;
    }

    @Override
    public String getAcronym() {
        return getAcronym(null);
    }

    public DepartmentUnit getDepartmentUnit(ExecutionSemester semester) {
        return getCompetenceCourse().getDepartmentUnit(semester);
    }

    public DepartmentUnit getDepartmentUnit() {
        return getCompetenceCourse().getDepartmentUnit();
    }

    public Boolean getBasic(ExecutionSemester period) {
        return getCompetenceCourse() != null && getCompetenceCourse().isBasic(period);
    }

    @Override
    public Boolean getBasic() {
        return getBasic(null);
    }

    public String getObjectives(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getObjectives(period);
        }
        return null;
    }

    public String getObjectives() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getObjectives();
        }
        return null;
    }

    public String getObjectivesEn(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getObjectivesEn(period);
        }
        return null;
    }

    public String getObjectivesEn() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getObjectivesEn();
        }
        return null;
    }

    public LocalizedString getObjectivesI18N(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getObjectivesI18N(period);
        }
        return new LocalizedString();
    }

    public String getProgram(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getProgram(period);
        }
        return null;
    }

    public String getProgram() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getProgram();
        }
        return null;
    }

    public String getProgramEn(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getProgramEn(period);
        }
        return null;
    }

    public String getProgramEn() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getProgramEn();
        }
        return null;
    }

    public LocalizedString getProgramI18N(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getProgramI18N(period);
        }
        return new LocalizedString();
    }

    public LocalizedString getPrerequisitesI18N() {
        return new LocalizedString(org.fenixedu.academic.util.LocaleUtils.PT, getPrerequisites())
                .with(org.fenixedu.academic.util.LocaleUtils.EN, getPrerequisitesEn());
    }

    public String getEvaluationMethod(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getEvaluationMethod(period);
        }
        if (hasAnyExecutionCourseIn(period)) {
            return getExecutionCoursesByExecutionPeriod(period).iterator().next().getEvaluationMethodText();
        } else {
            return null;
        }
    }

    public String getEvaluationMethod() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getEvaluationMethod();
        }
        return null;
    }

    public String getEvaluationMethodEn(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getEvaluationMethodEn(period);
        }
        if (hasAnyExecutionCourseIn(period)) {
            return getExecutionCoursesByExecutionPeriod(period).iterator().next().getEvaluationMethodTextEn();
        } else {
            return null;
        }
    }

    public String getEvaluationMethodEn() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getEvaluationMethodEn();
        }
        return null;
    }

    public LocalizedString getEvaluationMethodI18N(ExecutionSemester period) {
        if (getCompetenceCourse() != null) {
            return new LocalizedString(org.fenixedu.academic.util.LocaleUtils.PT,
                    getCompetenceCourse().getEvaluationMethod(period)).with(org.fenixedu.academic.util.LocaleUtils.EN,
                            getCompetenceCourse().getEvaluationMethodEn(period));
        }
        List<ExecutionCourse> courses = getExecutionCoursesByExecutionPeriod(period);
        if (courses.isEmpty()) {
            return new LocalizedString();
        }
        return new LocalizedString(org.fenixedu.academic.util.LocaleUtils.PT, courses.iterator().next().getEvaluationMethodText())
                .with(org.fenixedu.academic.util.LocaleUtils.EN, courses.iterator().next().getEvaluationMethodTextEn());
    }

    public RegimeType getRegime(final ExecutionSemester period) {
        final CompetenceCourse competenceCourse = getCompetenceCourse();
        return competenceCourse == null ? null : competenceCourse.getRegime(period);
    }

    public RegimeType getRegime(final ExecutionYear executionYear) {
        final CompetenceCourse competenceCourse = getCompetenceCourse();
        return competenceCourse == null ? null : competenceCourse.getRegime(executionYear);
    }

    public RegimeType getRegime() {
        if (getCompetenceCourse() != null) {
            return getCompetenceCourse().getRegime();
        }
        return isOptionalCurricularCourse() ? RegimeType.SEMESTRIAL : null;
    }

    public boolean hasRegime() {
        return getRegime() != null;
    }

    public boolean hasRegime(final ExecutionYear executionYear) {
        return getRegime(executionYear) != null;
    }

    /**
     * Maintened for legacy code compatibility purposes only. Makes no sense to
     * check an Enrolment concept in a CurricularCourse.
     * 
     * @return true if CurricularCourseType checks accordingly
     */
    @Deprecated
    final public boolean isPropaedeutic() {
        // if (isBolonhaDegree()) {
        // throw new
        // DomainException("CurricularCourse.must.check.propaedeutic.status.in.enrolment.in.bolonha.degrees");
        // }
        return getType().equals(CurricularCourseType.P_TYPE_COURSE);
    }

    public boolean isOptionalCurricularCourse() {
        return false;
    }

    @Override
    final public boolean isOptional() {
        return getType() == CurricularCourseType.OPTIONAL_COURSE;
    }

    final public boolean isTFC() {
        return getType() == CurricularCourseType.TFC_COURSE;
    }

    @Override
    public boolean isDissertation() {
        CompetenceCourse competenceCourse = getCompetenceCourse();
        return competenceCourse == null ? false : competenceCourse.isDissertation();
    }

    public boolean isAnual() {
        return getCompetenceCourse() != null && getCompetenceCourse().isAnual();
    }

    public boolean isAnual(final ExecutionYear executionYear) {
        return getCompetenceCourse() != null && getCompetenceCourse().isAnual(executionYear);
    }

    public boolean isSemestrial(final ExecutionYear executionYear) {
        return getCompetenceCourse() != null && getCompetenceCourse().isSemestrial(executionYear);
    }

    public boolean isEquivalent(CurricularCourse oldCurricularCourse) {
        return equals(oldCurricularCourse) || (getCompetenceCourse() != null
                && getCompetenceCourse().getAssociatedCurricularCoursesSet().contains(oldCurricularCourse));
    }

    public boolean hasScopeForCurricularYear(final Integer curricularYear, final ExecutionSemester executionSemester) {
        for (final DegreeModuleScope degreeModuleScope : getDegreeModuleScopes()) {
            if (degreeModuleScope.isActiveForExecutionPeriod(executionSemester)
                    && degreeModuleScope.getCurricularYear().equals(curricularYear)) {
                return true;
            }
        }
        return false;
    }

    static public List<CurricularCourse> readByCurricularStage(final CurricularStage curricularStage) {
        final List<CurricularCourse> result = new ArrayList<CurricularCourse>();
        for (CurricularCourse curricularCourse : CurricularCourse.readCurricularCourses()) {
            if (curricularCourse.getCurricularStage() != null && curricularCourse.getCurricularStage().equals(curricularStage)) {
                result.add(curricularCourse);
            }
        }
        return result;
    }

    public boolean hasScopeInGivenSemesterAndCurricularYearInDCP(CurricularYear curricularYear,
            DegreeCurricularPlan degreeCurricularPlan, final ExecutionSemester executionSemester) {

        if (degreeCurricularPlan == null || getDegreeCurricularPlan().equals(degreeCurricularPlan)) {
            for (DegreeModuleScope degreeModuleScope : getDegreeModuleScopes()) {
                if (degreeModuleScope.isActiveForExecutionPeriod(executionSemester)
                        && (curricularYear == null || degreeModuleScope.getCurricularYear().equals(curricularYear.getYear()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGradeSubmissionAvailableFor(ExecutionSemester executionSemester) {
        return EvaluationSeason.all().anyMatch(s -> s.isGradeSubmissionAvailable(this, executionSemester));
    }

    public ExecutionDegree getExecutionDegreeFor(AcademicInterval academicInterval) {
        return getDegreeCurricularPlan().getExecutionDegreeByAcademicInterval(academicInterval);
    }

    @Deprecated
    public ExecutionDegree getExecutionDegreeFor(ExecutionYear executionYear) {
        return getDegreeCurricularPlan().getExecutionDegreeByYear(executionYear);
    }

    public List<DegreeModuleScope> getDegreeModuleScopes() {
        return DegreeModuleScope.getDegreeModuleScopes(this);
    }

    private int countAssociatedStudentsByExecutionPeriodAndEnrolmentNumber(ExecutionSemester executionSemester,
            int enrolmentNumber) {
        int curricularCourseAndExecutionPeriodAssociatedStudents = 0;

        for (CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                Enrolment enrolmentsEntry = (Enrolment) curriculumModule;
                if (enrolmentsEntry.getExecutionPeriod() == executionSemester) {

                    StudentCurricularPlan studentCurricularPlanEntry = enrolmentsEntry.getStudentCurricularPlan();
                    int numberOfEnrolmentsForThatCurricularCourseAndExecutionPeriod = 0;

                    for (Enrolment enrolmentsFromStudentCPEntry : studentCurricularPlanEntry.getEnrolmentsSet()) {
                        if (enrolmentsFromStudentCPEntry.getCurricularCourse() == this
                                && (enrolmentsFromStudentCPEntry.getExecutionPeriod().compareTo(executionSemester) <= 0)) {
                            ++numberOfEnrolmentsForThatCurricularCourseAndExecutionPeriod;
                        }
                    }

                    if (numberOfEnrolmentsForThatCurricularCourseAndExecutionPeriod == enrolmentNumber) {
                        curricularCourseAndExecutionPeriodAssociatedStudents++;
                    }
                }
            }
        }

        return curricularCourseAndExecutionPeriodAssociatedStudents;
    }

    public Integer getTotalEnrolmentStudentNumber(ExecutionSemester executionSemester) {
        int curricularCourseAndExecutionPeriodStudentNumber = 0;

        for (CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                Enrolment enrolmentsEntry = (Enrolment) curriculumModule;
                if (enrolmentsEntry.getExecutionPeriod() == executionSemester) {
                    curricularCourseAndExecutionPeriodStudentNumber++;
                }
            }
        }

        return curricularCourseAndExecutionPeriodStudentNumber;
    }

    public Integer getFirstTimeEnrolmentStudentNumber(ExecutionSemester executionSemester) {
        return countAssociatedStudentsByExecutionPeriodAndEnrolmentNumber(executionSemester, 1);
    }

    public Integer getSecondTimeEnrolmentStudentNumber(ExecutionSemester executionSemester) {
        return getTotalEnrolmentStudentNumber(executionSemester) - getFirstTimeEnrolmentStudentNumber(executionSemester);
    }

    public boolean isActive(final ExecutionYear executionYear) {
        for (final ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            if (isActive(executionSemester)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActive(final ExecutionSemester executionSemester) {
        return getActiveDegreeModuleScopesInExecutionPeriod(executionSemester).size() > 0;
    }

    public boolean hasEnrolmentForPeriod(final ExecutionSemester executionSemester) {
        for (final CurriculumModule curriculumModule : getCurriculumModulesSet()) {
            if (curriculumModule.isEnrolment()) {
                final Enrolment enrolment = (Enrolment) curriculumModule;
                if (!enrolment.isAnnulled() && enrolment.getExecutionPeriod() == executionSemester) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void getAllDegreeModules(final Collection<DegreeModule> degreeModules) {
        degreeModules.add(this);
    }

    @Override
    public boolean isCurricularCourse() {
        return true;
    }

    public DegreeModuleScope getOldestDegreeModuleScope() {
        List<DegreeModuleScope> scopes = new ArrayList<DegreeModuleScope>(getDegreeModuleScopes());
        Collections.sort(scopes, DegreeModuleScope.COMPARATOR_BY_CURRICULAR_YEAR_AND_SEMESTER_AND_CURRICULAR_COURSE_NAME);
        return scopes.iterator().next();
    }

    @Override
    public Integer getMinimumValueForAcumulatedEnrollments() {
        return super.getMinimumValueForAcumulatedEnrollments() == null ? Integer
                .valueOf(0) : super.getMinimumValueForAcumulatedEnrollments();
    }

    @Override
    public Integer getMaximumValueForAcumulatedEnrollments() {
        return super.getMaximumValueForAcumulatedEnrollments() == null ? Integer
                .valueOf(0) : super.getMaximumValueForAcumulatedEnrollments();
    }

    public BigDecimal getTotalHoursByShiftType(ShiftType type, ExecutionInterval executionInterval) {
        if (type != null) {
            Double hours = null;
            switch (type) {
            case TEORICA:
                hours = getTheoreticalHours(executionInterval);
                break;
            case TEORICO_PRATICA:
                hours = getTheoPratHours();
                break;
            case PRATICA:
                hours = getPraticalHours();
                break;
            case PROBLEMS:
                hours = getProblemsHours(executionInterval);
                break;
            case LABORATORIAL:
                hours = getLabHours(executionInterval);
                break;
            case TRAINING_PERIOD:
                hours = getTrainingPeriodHours(executionInterval);
                break;
            case SEMINARY:
                hours = getSeminaryHours(executionInterval);
                break;
            case TUTORIAL_ORIENTATION:
                hours = getTutorialOrientationHours(executionInterval);
                break;
            case FIELD_WORK:
                hours = getFieldWorkHours(executionInterval);
                break;
            case OTHER:
                hours = getOtherHours(executionInterval);
                break;
            default:
                break;
            }
            return hours != null ? BigDecimal.valueOf(hours) : null;
        }
        return null;
    }

    public boolean hasAnyExecutionCourseIn(ExecutionSemester executionSemester) {
        for (ExecutionCourse executionCourse : getAssociatedExecutionCoursesSet()) {
            if (executionCourse.getExecutionInterval().equals(executionSemester)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<CurricularCourse> getAllCurricularCourses() {
        return Collections.singleton(this);
    }

    @Override
    public Set<CurricularCourse> getAllCurricularCourses(ExecutionSemester executionSemester) {
        return getAllCurricularCourses();
    }

    public CompetenceCourseLevel getCompetenceCourseLevel() {
        return getCompetenceCourse() != null ? getCompetenceCourse().getCompetenceCourseLevel() : null;
    }

    public boolean hasCompetenceCourseLevel() {
        return getCompetenceCourseLevel() != null;
    }

    public boolean hasExecutionDegreeByYearAndCampus(ExecutionYear executionYear, Space campus) {
        return getDegreeCurricularPlan().hasExecutionDegreeByYearAndCampus(executionYear, campus);
    }

    public boolean hasAnyExecutionDegreeFor(ExecutionYear executionYear) {
        return getDegreeCurricularPlan().hasAnyExecutionDegreeFor(executionYear);
    }

    @Override
    public void applyToCurricularCourses(final ExecutionYear executionYear, final Predicate predicate) {
        predicate.evaluate(this);
    }

    @Override
    public void addAssociatedExecutionCourses(final ExecutionCourse associatedExecutionCourses) {
        Collection<ExecutionCourse> executionCourses = getAssociatedExecutionCoursesSet();

        for (ExecutionCourse executionCourse : executionCourses) {
            if (associatedExecutionCourses != executionCourse
                    && executionCourse.getExecutionInterval() == associatedExecutionCourses.getExecutionInterval()) {
                throw new DomainException("error.executionCourse.curricularCourse.already.associated");
            }
        }
        super.addAssociatedExecutionCourses(associatedExecutionCourses);
    }

    public String getCodeAndName(final ExecutionInterval executionInterval) {
        final String code = getCode();
        final String name = getNameI18N(executionInterval).getContent();

        return (StringUtils.isEmpty(code) ? "" : code + " - ") + name;
    }

    @Override
    public String getCode() {
        return getCompetenceCourse() != null ? getCompetenceCourse().getCode() : null;
    }

    public void setWeight(final BigDecimal input) {
        super.setWeigth(input == null ? null : input.doubleValue());
    }

    @Deprecated
    /**
     * @deprecated Use {@link #getWeight()} instead.
     * 
     */
    public Double getBaseWeight() {
        return super.getWeigth();
    }
}
