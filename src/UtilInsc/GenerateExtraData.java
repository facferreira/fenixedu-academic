package UtilInsc;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import Dominio.Branch;
import Dominio.CurricularCourse;
import Dominio.CurricularCourseScope;
import Dominio.Curso;
import Dominio.CursoExecucao;
import Dominio.DegreeCurricularPlan;
import Dominio.IBranch;
import Dominio.ICurricularCourse;
import Dominio.ICurricularCourseScope;
import Dominio.ICurso;
import Dominio.ICursoExecucao;
import Dominio.IDegreeCurricularPlan;
import Dominio.IExecutionYear;
import Dominio.ITeacher;
import ServidorAplicacao.GestorServicos;
import ServidorAplicacao.IUserView;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ICursoExecucaoPersistente;
import ServidorPersistente.ICursoPersistente;
import ServidorPersistente.IPersistentBranch;
import ServidorPersistente.IPersistentCurricularCourse;
import ServidorPersistente.IPersistentCurricularCourseScope;
import ServidorPersistente.IPersistentDegreeCurricularPlan;
import ServidorPersistente.IPersistentExecutionYear;
import ServidorPersistente.IPersistentTeacher;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;
import ServidorPersistente.exceptions.ExistingPersistentException;
import Util.DegreeCurricularPlanState;
import Util.TipoCurso;

/**
 * @author David Santos
 * 11/Jun/2003
 */

public class GenerateExtraData {

	private static final int MAX = 5;

	private static GestorServicos gestor = GestorServicos.manager();
	private static IUserView userView = null;
	private static ISuportePersistente persistentSupport =	null;

	public static void main(String[] args) {
		
		System.out.println("PROSSES STARTED");

		for(int i = 1; i <= GenerateExtraData.MAX; i++) {

// ESCREVO O DEGREE:
			turnOnPersistentSuport();
			ICurso degree = new Curso();
			degree.setDegreeCurricularPlans(null);
			degree.setIdInternal(null);
			degree.setNome("Curso Da Tanga " + i);
			degree.setSigla("CDT " + i);
			degree.setTipoCurso(TipoCurso.LICENCIATURA_OBJ);
			ICursoPersistente persistentdegree = persistentSupport.getICursoPersistente();
			try {
				persistentdegree.lockWrite(degree);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			turnOffPersistentSuport();

// ESCREVO O DEGREE_CURRICULARR_PLAN:
			turnOnPersistentSuport();
			persistentdegree = persistentSupport.getICursoPersistente();
			ICurso degree2 = null;
			try {
				degree2 = (ICurso) persistentdegree.readDomainObjectByCriteria(degree);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			IDegreeCurricularPlan degreeCurricularPlan = new DegreeCurricularPlan();
			degreeCurricularPlan.setCurricularCourses(null);
			degreeCurricularPlan.setDegree(degree2);
			degreeCurricularPlan.setDegreeDuration(new Integer(5));
			degreeCurricularPlan.setEndDate(new Date());
			degreeCurricularPlan.setInitialDate(new Date());
			degreeCurricularPlan.setMinimalYearForOptionalCourses(new Integer(3));
			degreeCurricularPlan.setName("Plano Curricular do Curso Da Tanga " + i);
			degreeCurricularPlan.setState(DegreeCurricularPlanState.ACTIVE_OBJ);
			IPersistentDegreeCurricularPlan persistentDegreeCurricularPlan = persistentSupport.getIPersistentDegreeCurricularPlan();
			try {
				persistentDegreeCurricularPlan.lockWrite(degreeCurricularPlan);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			turnOffPersistentSuport();

// ESCREVO O BRANCH:
			turnOnPersistentSuport();
			persistentDegreeCurricularPlan = persistentSupport.getIPersistentDegreeCurricularPlan();
			IDegreeCurricularPlan degreeCurricularPlan2 = null;
			try {
				degreeCurricularPlan2 = (IDegreeCurricularPlan) persistentDegreeCurricularPlan.readDomainObjectByCriteria(degreeCurricularPlan);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			IBranch branch = new Branch();
			branch.setCode("");
			branch.setName("");
			branch.setScopes(null);
			branch.setDegreeCurricularPlan(degreeCurricularPlan2);
			IPersistentBranch persistentBranch = persistentSupport.getIPersistentBranch();
			try {
				persistentBranch.lockWrite(branch);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			turnOffPersistentSuport();
			
// ESCREVO O EXECUTION_DEGREE:
			turnOnPersistentSuport();
			persistentDegreeCurricularPlan = persistentSupport.getIPersistentDegreeCurricularPlan();
			IPersistentExecutionYear persistentExecutionYear = persistentSupport.getIPersistentExecutionYear();
			ICursoExecucaoPersistente persistentExecutionDegree = persistentSupport.getICursoExecucaoPersistente();
			IPersistentTeacher persistentTeacher = persistentSupport.getIPersistentTeacher();
			try {
				degreeCurricularPlan2 = (IDegreeCurricularPlan) persistentDegreeCurricularPlan.readDomainObjectByCriteria(degreeCurricularPlan);
				IExecutionYear executionYear = persistentExecutionYear.readActualExecutionYear();
				ITeacher teacher = (ITeacher) persistentTeacher.readTeacherByNumber(new Integer(1));
				ICursoExecucao executionDegree = new CursoExecucao();
				executionDegree.setCoordinator(teacher);
				executionDegree.setCurricularPlan(degreeCurricularPlan2);
				executionDegree.setExecutionYear(executionYear);
				persistentExecutionDegree.lockWrite(executionDegree);
			} catch (ExcepcaoPersistencia e1) {
				e1.printStackTrace(System.out);
			}
			turnOffPersistentSuport();

			generateCurricularCourses(degreeCurricularPlan, branch, i);
		}

		System.out.println("PROSSES ENDED");
	}

	private static void turnOnPersistentSuport() {
		try {
			persistentSupport =	SuportePersistenteOJB.getInstance();
			persistentSupport.iniciarTransaccao();
		} catch (ExcepcaoPersistencia e) {
			e.printStackTrace(System.out);
		}
	}

	private static void turnOffPersistentSuport() {
		try {
			persistentSupport.confirmarTransaccao();
		} catch (ExcepcaoPersistencia e) {
			e.printStackTrace(System.out);
		}
	}

	private static void generateCurricularCourses(IDegreeCurricularPlan degreeCurricularPlan, IBranch branch, int i) {
		List curricularCoursesList = null;
		IDegreeCurricularPlan degreeCurricularPlan2 = null;
		IBranch branch2 = null;

		turnOnPersistentSuport();
		IPersistentCurricularCourse persistentCurricularCourse = persistentSupport.getIPersistentCurricularCourse();
		IPersistentDegreeCurricularPlan persistentDegreeCurricularPlan = persistentSupport.getIPersistentDegreeCurricularPlan();
		IPersistentBranch persistentBranch = persistentSupport.getIPersistentBranch();
		IPersistentCurricularCourseScope persistentCurricularCourseScope = persistentSupport.getIPersistentCurricularCourseScope();
		try {
			DegreeCurricularPlan degreeCurricularCriteria = new DegreeCurricularPlan();
			degreeCurricularCriteria.setIdInternal(new Integer(1));
			IDegreeCurricularPlan oldDegreeCurricularPlan = (IDegreeCurricularPlan) persistentDegreeCurricularPlan.readDomainObjectByCriteria(degreeCurricularCriteria);
			curricularCoursesList = persistentCurricularCourse.readCurricularCoursesByDegreeCurricularPlan(oldDegreeCurricularPlan);
			degreeCurricularPlan2 = (IDegreeCurricularPlan) persistentDegreeCurricularPlan.readDomainObjectByCriteria(degreeCurricularPlan);
			branch2 = (IBranch) persistentBranch.readDomainObjectByCriteria(branch);
		} catch (ExcepcaoPersistencia e) {
			e.printStackTrace(System.out);
		}
		turnOffPersistentSuport();

		Iterator iterator1 = curricularCoursesList.iterator();
		while(iterator1.hasNext()) {
			ICurricularCourse curricularCourse = (ICurricularCourse) iterator1.next();

// ESCREVO A CURRICULAR_COURSE:
			turnOnPersistentSuport();
			ICurricularCourse curricularCourseToWrite = new CurricularCourse();
			curricularCourseToWrite.setAssociatedExecutionCourses(null);
			curricularCourseToWrite.setCredits(null);
			curricularCourseToWrite.setDepartmentCourse(null);
			curricularCourseToWrite.setIdInternal(null);
			curricularCourseToWrite.setLabHours(null);
			curricularCourseToWrite.setPraticalHours(null);
			curricularCourseToWrite.setTheoPratHours(null);
			curricularCourseToWrite.setTheoreticalHours(null);
			curricularCourseToWrite.setScopes(null);

			curricularCourseToWrite.setCode(curricularCourse.getCode() + i);
			curricularCourseToWrite.setCurricularCourseExecutionScope(curricularCourse.getCurricularCourseExecutionScope());
			curricularCourseToWrite.setDegreeCurricularPlan(degreeCurricularPlan2);
			curricularCourseToWrite.setMandatory(curricularCourse.getMandatory());
			curricularCourseToWrite.setName(curricularCourse.getName() + " - Plano Da Tanga " + i);
			curricularCourseToWrite.setType(curricularCourse.getType());
			curricularCourseToWrite.setUniversityCode(curricularCourse.getUniversityCode());

			persistentCurricularCourse = persistentSupport.getIPersistentCurricularCourse();
			try {
				persistentCurricularCourse.lockWrite(curricularCourseToWrite);
			} catch (ExcepcaoPersistencia e) {
				e.printStackTrace(System.out);
			}
			turnOffPersistentSuport();

			Iterator iterator2 = curricularCourse.getScopes().iterator();
			while(iterator2.hasNext()) {
				ICurricularCourseScope curricularCourseScope = (ICurricularCourseScope) iterator2.next();

// ESCREVO O CURRICULAR_COURSE_SCOPE:
				turnOnPersistentSuport();
				ICurricularCourseScope curricularCourseScopeToWrite = new CurricularCourseScope();
				curricularCourseScopeToWrite.setIdInternal(null);
				curricularCourseScopeToWrite.setLabHours(null);
				curricularCourseScopeToWrite.setPraticalHours(null);
				curricularCourseScopeToWrite.setTheoPratHours(null);
				curricularCourseScopeToWrite.setTheoreticalHours(null);
				
				curricularCourseScopeToWrite.setMaxIncrementNac(curricularCourseScope.getMaxIncrementNac());
				curricularCourseScopeToWrite.setMinIncrementNac(curricularCourseScope.getMinIncrementNac());
				curricularCourseScopeToWrite.setWeigth(curricularCourseScope.getWeigth());

				try {
					persistentCurricularCourse = persistentSupport.getIPersistentCurricularCourse();
					ICurricularCourse curricularCourse2 = (ICurricularCourse) persistentCurricularCourse.readDomainObjectByCriteria(curricularCourseToWrite);

					curricularCourseScopeToWrite.setBranch(branch2);
					curricularCourseScopeToWrite.setCurricularCourse(curricularCourse2);
					curricularCourseScopeToWrite.setCurricularSemester(curricularCourseScope.getCurricularSemester());
				
					persistentCurricularCourseScope.lockWrite(curricularCourseScopeToWrite);
				} catch (ExistingPersistentException e1) {
					e1.printStackTrace(System.out);
				} catch (ExcepcaoPersistencia e1) {
					e1.printStackTrace(System.out);
				}
				turnOffPersistentSuport();
			}
		}
	}
}