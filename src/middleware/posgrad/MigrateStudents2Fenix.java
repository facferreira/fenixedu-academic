/*
 * Created on 28/Mai/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package middleware.posgrad;

import java.util.Iterator;
import java.util.List;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;

import Dominio.Funcionario;
import Dominio.IPersonRole;
import Dominio.IPessoa;
import Dominio.IStudent;
import Dominio.IStudentKind;
import Dominio.Pessoa;
import Dominio.Student;
import Dominio.StudentKind;
import Util.RoleType;
import Util.StudentState;
import Util.StudentType;
import Util.TipoCurso;
import Util.TipoDocumentoIdentificacao;

/**
 * @author Nuno Nunes (nmsn@rnl.ist.utl.pt)
 *         Joana Mota (jccm@rnl.ist.utl.pt)
 */
public class MigrateStudents2Fenix {


	PersistenceBroker broker = null;
	
	
	public MigrateStudents2Fenix() {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
	}


	public static void main(String args[]) throws Exception{
		MigrateStudents2Fenix migrateStudents2Fenix = new MigrateStudents2Fenix();
		
		migrateStudents2Fenix.broker.beginTransaction();
		migrateStudents2Fenix.broker.clearCache();
		migrateStudents2Fenix.migrateStudents2Fenix();
		
		migrateStudents2Fenix.broker.commitTransaction();
	}

	private void migrateStudents2Fenix() throws Exception{
		IStudent student2Write = null;
		Posgrad_aluno_mestrado student2Convert = null;
		List result = null;
		Query query = null;
		Criteria criteria = null;
		QueryByCriteria queryByCriteria = null;
		int studentsWritten = 0;
		int rolesWritten = 0;
		
		try {
			System.out.print("A Ler Alunos de Pos-Graduacao ...");
			List studentsPG = getStudents();
			System.out.println("  Done !");
			
			System.out.println("A Converter " + studentsPG.size() + " alunos de Pos-Graduacao para o Fenix ...");

			// Cria informacao sobre um grupo de alunos
			IStudentKind studentGroupInfo = new StudentKind();
			
			studentGroupInfo.setStudentType(new StudentType (StudentType.NORMAL));
			queryByCriteria = new QueryByCriteria(studentGroupInfo);
			
			result = (List) broker.getCollectionByQuery(queryByCriteria);
			
			if (result.size() == 0){
			
				studentGroupInfo = new StudentKind();			
				studentGroupInfo.setMaxCoursesToEnrol(new Integer(7));
				studentGroupInfo.setMaxNACToEnrol(new Integer(10));
				studentGroupInfo.setMinCoursesToEnrol(new Integer(3));
				studentGroupInfo.setStudentType(new StudentType (StudentType.NORMAL));
				broker.store(studentGroupInfo);
			} else {
				studentGroupInfo = (IStudentKind) result.get(0);
			}

			Iterator iterator = studentsPG.iterator();
			while(iterator.hasNext()){
				student2Convert = (Posgrad_aluno_mestrado) iterator.next();
		
				criteria = new Criteria();
				criteria.addEqualTo("number", new Integer(String.valueOf(student2Convert.getNumero())));
				criteria.addEqualTo("degreeType", new TipoCurso(TipoCurso.MESTRADO));
				query = new QueryByCriteria(Student.class,criteria);
				List resultStudent = (List) broker.getCollectionByQuery(query);		
		
					
				// Read the person old person
				
				criteria = new Criteria();
				criteria.addEqualTo("codigointerno", new Integer(String.valueOf(student2Convert.getCodigopessoa())));

				query = new QueryByCriteria(Posgrad_pessoa.class,criteria);
				result = (List) broker.getCollectionByQuery(query);		

				if (result.size() != 1)
					throw new Exception("Erro a ler a Pessoa da Pos-Graduacao!");
				
				Posgrad_pessoa personOld = (Posgrad_pessoa) result.get(0);
				
				// Verificar o Tipo de Documento
				TipoDocumentoIdentificacao identificationDocumentType = null;
				if (personOld.getTipodocumentoidentificacao().equalsIgnoreCase("BILHETE DE IDENTIDADE")){
					identificationDocumentType = new TipoDocumentoIdentificacao(TipoDocumentoIdentificacao.BILHETE_DE_IDENTIDADE);
				} else if (personOld.getTipodocumentoidentificacao().equalsIgnoreCase("PASSAPORTE")){
					identificationDocumentType = new TipoDocumentoIdentificacao(TipoDocumentoIdentificacao.PASSAPORTE);
				} else identificationDocumentType = new TipoDocumentoIdentificacao(TipoDocumentoIdentificacao.OUTRO);

				criteria = new Criteria();
				criteria.addEqualTo("numeroDocumentoIdentificacao",personOld.getNumerodocumentoidentificacao());
				criteria.addEqualTo("tipoDocumentoIdentificacao", identificationDocumentType);
				query = new QueryByCriteria(Pessoa.class,criteria);
				result = (List) broker.getCollectionByQuery(query);

				if (result.size() != 1)
					throw new Exception("Erro a ler a Pessoa do Fenix ! BI: " + personOld.getNumerodocumentoidentificacao());
				
				IPessoa person = (IPessoa) result.get(0);


				if (resultStudent.size() == 0){
							
					// Create a new Student
					student2Write = new Student();
					student2Write.setNumber(new Integer(String.valueOf(student2Convert.getNumero())));
					student2Write.setDegreeType(new TipoCurso(TipoCurso.MESTRADO));
					student2Write.setPerson(person);
					student2Write.setState(new StudentState(StudentState.INSCRITO));
					student2Write.setStudentKind(studentGroupInfo);
					broker.store(student2Write);
					studentsWritten++;
					
				} 

				// Give the Student Role (This student may not exist but the person may already be a 
				// Graduate Student) 
					
				IPersonRole personRole = RoleUtils.readPersonRole((Pessoa) person, RoleType.STUDENT, broker);
				if (personRole == null){
					rolesWritten++;
					person.getPersonRoles().add(RoleUtils.readRole(RoleType.STUDENT, broker));												
				}
					
				// Check if the person is a Employee
				
				criteria = new Criteria();
				
				criteria.addEqualTo("chavePessoa", person.getIdInternal());
				query = new QueryByCriteria(Funcionario.class,criteria);
				result = (List) broker.getCollectionByQuery(query);

				if (result.size() == 0){
					// Update The Username
					person.setUsername("M" + student2Convert.getNumero());
					broker.store(person);
				} 
			}
			System.out.println("  Students Written: " + studentsWritten);
			System.out.println("  Roles Written: " + rolesWritten);
			System.out.println("  Done !");
		} catch(Exception e) {;
			System.out.println("Error converting Student " + student2Convert.getNumero());
			throw new Exception(e);
		}
		
		
	}

	private List getStudents() throws Exception {
		Criteria criteria = new Criteria();
		Query query = new QueryByCriteria(Posgrad_aluno_mestrado.class,criteria);
		return (List) broker.getCollectionByQuery(query);
	}


}
